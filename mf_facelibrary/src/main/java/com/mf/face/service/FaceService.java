package com.mf.face.service;

import static com.mf.face.model.FaceConstant.CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION;
import static com.mf.face.model.FaceConstant.CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION_RESULT;
import static com.mf.face.model.FaceConstant.CODE_ERROR;
import static com.mf.face.model.FaceConstant.CODE_FACE_RECOGNITION;
import static com.mf.face.model.FaceConstant.CODE_FACE_RECOGNITION_RESULT;
import static com.mf.face.model.FaceConstant.CODE_FACE_RECOGNITION_TIPS_RESULT;
import static com.mf.face.model.FaceConstant.CODE_FACE_REGISTER;
import static com.mf.face.model.FaceConstant.CODE_FACE_REGISTER_RESULT;
import static com.mf.face.model.FaceConstant.CODE_FACE_REGISTER_TIPS_RESULT;
import static com.mf.face.model.FaceConstant.CODE_MANUAL_CANCEL_FACE_RECOGNITION;
import static com.mf.face.model.FaceConstant.CODE_MANUAL_CANCEL_FACE_REGISTER;
import static com.mf.face.model.FaceConstant.CODE_PICK_CAR_BY_BERTH;
import static com.mf.face.model.FaceConstant.CODE_PICK_CAR_BY_PLATE;
import static com.mf.face.model.FaceConstant.CODE_REMOVE_FACE_DATA;
import static com.mf.face.model.FaceConstant.CODE_REMOVE_FACE_DATA_RESULT;
import static com.mf.face.model.FaceConstant.CODE_SYNC_FACE_DATA;
import static com.mf.face.model.FaceConstant.CODE_SYNC_FACE_DATA_RESULT;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.datalibrary.api.FaceApi;
import com.mf.base.config.GlobalProperty;
import com.mf.base.utils.FileUtil;
import com.mf.face.R;
import com.mf.face.entity.FaceRecognitionEntity;
import com.mf.face.entity.FaceRegisterEntity;
import com.mf.face.entity.FaceTipsEntity;
import com.mf.face.helper.FaceInitHelper;
import com.mf.face.helper.FaceRecognitionHelper;
import com.mf.face.helper.FaceRegisterHelper;
import com.mf.face.helper.FaceViewHelper;
import com.mf.face.listener.FaceRecognitionListener;
import com.mf.face.listener.FaceRegisterListener;
import com.mf.face.listener.FaceTipsListener;
import com.mf.face.listener.SdkInitListenerImpl;
import com.mf.face.manager.FaceAuthManager;
import com.mf.face.model.FaceConstant;
import com.mf.log.LogConfig;
import com.mf.log.LogUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FaceService extends Service implements FaceTipsListener, FaceRegisterListener, FaceRecognitionListener {
    private static final String TAG = "face";


    // 使用类型，1-只开启RGB，2-只开启NIR，3-分别开启RGB和NIR，4-同时开启RGB和NIR
    private int useType = FaceConstant.USE_TYPE_RGB;

    // 回复客户端信息,该对象由客户端传递过来
    private Messenger clientMessenger;

    // 用于接收从客户端传递过来的数据
    private Messenger messenger;

    private Handler handler;

    private FaceRegisterHelper faceRegisterHelper;
    private FaceRecognitionHelper faceRecognitionHelper;
    private FaceRegisterHelper faceNirRegisterHelper;
    private FaceRecognitionHelper faceNirRecognitionHelper;
    private FaceViewHelper faceRegisterViewHelper;
    private FaceViewHelper faceRecognitionViewHelper;
    private final Set<String> faceTagSet = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // 配置文件
        FileUtil.copyFromAssetsIfNotExist(getApplicationContext(), "mf-global-config.json", GlobalProperty.JSON_CONFIG_FILE);
        GlobalProperty.getInstance().init();
        String host = GlobalProperty.getInstance().getString("mec.http.server.host", "");
        String deviceId = GlobalProperty.getInstance().getString("device.id", "0");
        LogUtils.init(
                this,
                new LogConfig.Builder()
                        .logBasePath(LogUtils.getSdPath() + "/mf/log")
                        .logDir("face")
                        .isEnableStackTrace(true)
                        .isEnableThreadInfo(true)
                        .isLogToFileByXLog(true)
//                        .isLogToFileByMars(true)
//                        .isUploadLogFile(true)
//                        .uploadConfig(new LogConfig.UploadConfig.Builder()
//                                .url(host + "/v1/private/terminal-api/terminal-log-upload/upload")
//                                .deviceId(deviceId)
//                                .project("face")
//                                .target(LogConfig.UploadConfig.Target.MEC)
//                                .build())
                        .build()
        );
        LogUtils.d(TAG, "onCreate");

        handler = new Handler(getMainLooper());
        messenger = new Messenger(new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                clientMessenger = msg.replyTo;
                onHandleMessage(msg);
            }
        });

        faceRegisterViewHelper = new FaceViewHelper();
        faceRegisterViewHelper.setContext(this)
                .setRegister(true)
                .setOnCancelClickListener(() -> {
                    cancelAll();
                    sendMessage(CODE_MANUAL_CANCEL_FACE_REGISTER);
                })
                .loadView(R.layout.face_register_view);
        faceRecognitionViewHelper = new FaceViewHelper();
        faceRecognitionViewHelper.setContext(this)
                .setRegister(false)
                .setOnCancelClickListener(() -> {
                    cancelAll();
                    sendMessage(CODE_MANUAL_CANCEL_FACE_RECOGNITION);
                })
                .setOnPickCarByPlateClickListener(() -> {
                    cancelAll();
                    sendMessage(CODE_PICK_CAR_BY_PLATE);
                })
                .setOnPickCarByBerthClickListener(() -> {
                    cancelAll();
                    sendMessage(CODE_PICK_CAR_BY_BERTH);
                })
                .loadView(R.layout.face_recognition_view);

        faceRegisterHelper = new FaceRegisterHelper();
        faceRecognitionHelper = new FaceRecognitionHelper();
        if (useType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
            faceNirRegisterHelper = new FaceRegisterHelper();
            faceNirRecognitionHelper = new FaceRecognitionHelper();
        }
        int tmpUseType = useType;
        if (tmpUseType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
            tmpUseType = FaceConstant.USE_TYPE_RGB;
        }
        faceRegisterHelper.setContext(FaceService.this)
                .setUseType(tmpUseType)
                .setFaceViewHelper(faceRegisterViewHelper)
                .setListener(FaceService.this)
                .setTipsListener(FaceService.this)
                .init();
        faceRecognitionHelper.setContext(FaceService.this)
                .setUseType(tmpUseType)
                .setFaceViewHelper(faceRecognitionViewHelper)
                .setRecognitionListener(FaceService.this)
                .setTipsListener(FaceService.this)
                .init();
        if (faceNirRegisterHelper != null) {
            faceNirRegisterHelper.setContext(FaceService.this)
                    .setUseType(FaceConstant.USE_TYPE_RGB_AND_NIR)
                    .setFaceViewHelper(faceRegisterViewHelper)
                    .setListener(FaceService.this)
                    .setTipsListener(FaceService.this)
                    .init();
        }
        if (faceNirRecognitionHelper != null) {
            faceNirRecognitionHelper.setContext(FaceService.this)
                    .setUseType(FaceConstant.USE_TYPE_RGB_AND_NIR)
                    .setFaceViewHelper(faceRecognitionViewHelper)
                    .setRecognitionListener(FaceService.this)
                    .setTipsListener(FaceService.this)
                    .init();
        }
        // 百度人脸识别
        FaceInitHelper.getInstance().init(this, new SdkInitListenerImpl() {
            @Override
            public void initLicenseFail(int errorCode, String msg) {
                sendMessage(CODE_ERROR, errorCode, msg);
            }

            @Override
            public void initModelFail(int errorCode, String msg) {
                sendMessage(CODE_ERROR, errorCode, msg);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind");
        cancelAll();
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d(TAG, "onUnbind");
        cancelAll();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
    }

    private void onHandleMessage(Message msg) {
        Bundle bundle = msg.getData();
        boolean isShowPreviewView = bundle.getBoolean("isShowPreviewView", false);
        switch (msg.what) {
            case CODE_FACE_REGISTER: {
                String licenseNo = bundle.getString("licenseNo");
                faceRegister(isShowPreviewView, licenseNo);
                break;
            }
            case CODE_FACE_RECOGNITION:
                faceRecognition(isShowPreviewView);
                break;
            case CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION:
                cancelFaceRegisterAndRecognition();
                break;
            case CODE_SYNC_FACE_DATA:
                String faceId = bundle.getString("faceId");
                String faceFeature = bundle.getString("faceFeature");
                syncFaceData(faceId, faceFeature);
                break;
            case CODE_REMOVE_FACE_DATA:
                removeFaceData();
                break;
            default:
                LogUtils.d(TAG, "unknown msg.what:" + msg.what);
                break;
        }
    }

    private void faceRegister(boolean isShowPreviewView, String licenseNo) {
        LogUtils.d(TAG, "faceRegister isShowPreviewView:" + isShowPreviewView + " licenseNo:" + licenseNo);

        //  如果未初始化成功，尝试重新初始化
        if (!FaceAuthManager.initModelSuccess) {
            FaceAuthManager.getInstance().init(this, null);
        }

        cancelAll();
        handler.removeMessages(0);
        handler.postDelayed(() -> {
            if (isShowPreviewView) {
                faceRegisterViewHelper.showView();
            }
            String tag = UUID.randomUUID().toString();
            faceTagSet.add(tag);
            faceRegisterHelper.onFaceRegister(isShowPreviewView, false, licenseNo, tag);
            if (useType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
                faceNirRegisterHelper.onFaceRegister(isShowPreviewView, true, licenseNo, tag);
            }
        }, 100);
    }

    private void faceRecognition(boolean isShowPreviewView) {
        LogUtils.d(TAG, "faceRecognition isShowPreviewView:" + isShowPreviewView);

        //  如果未初始化成功，尝试重新初始化
        if (!FaceAuthManager.initModelSuccess) {
            FaceAuthManager.getInstance().init(this, null);
        }

        cancelAll();
        handler.removeMessages(0);
        handler.postDelayed(() -> {
            if (isShowPreviewView) {
                faceRecognitionViewHelper.showView();
            }
            String tag = UUID.randomUUID().toString();
            faceTagSet.add(tag);
            faceRecognitionHelper.onFaceRecognition(isShowPreviewView, tag);
            if (useType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
                faceNirRecognitionHelper.onFaceRecognition(isShowPreviewView, tag);
            }
        }, 100);
    }

    private void cancelFaceRegisterAndRecognition() {
        LogUtils.d(TAG, "cancelFaceRegisterAndRecognition");

        //  如果未初始化成功，尝试重新初始化
        if (!FaceAuthManager.initModelSuccess) {
            FaceAuthManager.getInstance().init(this, null);
        }

        cancelAll();
        sendMessage(CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION_RESULT, FaceConstant.CODE_SUCCESS,
                FaceConstant.TIPS_CANCEL_FACE_REGISTER_AND_RECOGNITION_SUCCESS);
    }

    private void syncFaceData(String faceId, String faceFeature) {
        LogUtils.d(TAG, "syncFaceData faceId:" + faceId);
        if (faceId == null || faceFeature == null) {
            sendMessage(CODE_SYNC_FACE_DATA_RESULT, FaceConstant.CODE_FAILED,
                    FaceConstant.TIPS_SYNC_FACE_DATA_FAILED);
            return;
        }
        FaceApi.getInstance().registerUserIntoDBmanager(faceId, faceId,
                faceId + ".jpg", null, EncodeUtils.base64Decode(faceFeature));
        // 数据变化，更新内存
        faceRegisterHelper.getFaceSDKManager().initDataBases(this);
        sendMessage(CODE_SYNC_FACE_DATA_RESULT, FaceConstant.CODE_SUCCESS,
                FaceConstant.TIPS_SYNC_FACE_DATA_SUCCESS);
    }

    private void removeFaceData() {
        LogUtils.d(TAG, "removeFaceData");
        cancelAll();
        FaceApi.getInstance().userClean();
        // 数据变化，更新内存
        faceRegisterHelper.getFaceSDKManager().initDataBases(this);
        sendMessage(CODE_REMOVE_FACE_DATA_RESULT, FaceConstant.CODE_SUCCESS,
                FaceConstant.TIPS_REMOVE_FACE_DATA_SUCCESS);
    }

    private void cancelAll() {
        faceRegisterHelper.onCancel();
        faceRecognitionHelper.onCancel();
        if (faceNirRegisterHelper != null) {
            faceNirRegisterHelper.onCancel();
        }
        if (faceNirRecognitionHelper != null) {
            faceNirRecognitionHelper.onCancel();
        }
        faceRegisterViewHelper.hideView();
        faceRecognitionViewHelper.hideView();
        faceTagSet.clear();
    }

    private void sendMessage(int what) {
        sendMessage(what, 0, null);
    }

    private void sendMessage(int what, int code, String msg) {
        sendMessage(what, false, code, msg, null, null, null, null, null);
    }

    private void sendMessage(int what, boolean isShowPreviewView, int code, String msg, String detail, String faceId) {
        sendMessage(what, isShowPreviewView, code, msg, detail, null, faceId, null, null);
    }

    private void sendMessage(int what, boolean isShowPreviewView, int code, String msg, String detail, String licenseNo, String faceId, String faceFeature, String faceImgPath) {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isShowPreviewView", isShowPreviewView);
            bundle.putInt("code", code);
            if (!TextUtils.isEmpty(msg)) {
                bundle.putString("msg", msg);
            }
            if (!TextUtils.isEmpty(detail)) {
                bundle.putString("detail", detail);
            }
            if (!TextUtils.isEmpty(licenseNo)) {
                bundle.putString("licenseNo", licenseNo);
            }
            if (!TextUtils.isEmpty(faceId)) {
                bundle.putString("faceId", faceId);
            }
            if (!TextUtils.isEmpty(faceFeature)) {
                bundle.putString("faceFeature", faceFeature);
            }
            if (!TextUtils.isEmpty(faceImgPath)) {
                bundle.putString("faceImgPath", faceImgPath);
            }
            Message message = Message.obtain();
            message.what = what;
            message.setData(bundle);
            if (clientMessenger != null) {
                clientMessenger.send(message);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "send message error", e);
        }
    }

    private void sendMessage(int what, Serializable entity) {
        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable("entity", entity);
            Message message = Message.obtain();
            message.what = what;
            message.setData(bundle);
            if (clientMessenger != null) {
                clientMessenger.send(message);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "send message error", e);
        }
    }

    private HashMap<Integer, Long> map = new HashMap<>();

    @Override
    public void onFaceTipsCallback(FaceTipsEntity entity) {
        // 过滤提示消息
        long millis = System.currentTimeMillis();
        Long time = map.get(entity.getCode());
        if (time == null) {
            time = 0L;
        }
        if (millis - time < 2000) {
            return;
        }
        map.put(entity.getCode(), millis);
        LogUtils.d(TAG, "onFaceTipsCallback :" + entity);

        String msg = entity.getMsg();
        if (StringUtils.isEmpty(msg)) {
            msg = "请将人脸对准上方摄像头";
        }
        if (entity.getWhich() == FaceConstant.FACE_REGISTER) {
            faceRegisterViewHelper.setTips(msg);
            sendMessage(CODE_FACE_REGISTER_TIPS_RESULT, entity);
        } else if (entity.getWhich() == FaceConstant.FACE_RECOGNITION) {
            faceRecognitionViewHelper.setTips(msg);
            sendMessage(CODE_FACE_RECOGNITION_TIPS_RESULT, entity);
        }
    }

    @Override
    public void onFaceRegisterCallback(FaceRegisterEntity entity) {
        LogUtils.d(TAG, "onFaceRegisterCallback :" + entity);
        int code = entity.getCode();
        String tag = entity.getTag();
        if (code == FaceConstant.CODE_SUCCESS || code == FaceConstant.CODE_ALREADY_REGISTER) {
            if (!StringUtils.isEmpty(tag) && faceTagSet.contains(tag)) {
                faceTagSet.remove(tag);
                LogUtils.e(TAG, "@@aa@@ tag 注册成功:" + tag);
            } else {
                LogUtils.e(TAG, "@@aa@@ tag 注册标识已被移除:" + tag);
            }
            faceRegisterViewHelper.hideView();
        }
        String msg = entity.getMsg();
        if (StringUtils.isEmpty(msg)) {
            faceRegisterViewHelper.setTips("请将人脸对准上方摄像头");
        } else {
            faceRegisterViewHelper.setTips(msg);
        }
        sendMessage(CODE_FACE_REGISTER_RESULT, entity);
    }

    @Override
    public void onFaceRecognitionCallback(FaceRecognitionEntity entity) {
        LogUtils.d(TAG, "onFaceRecognitionCallback :" + entity);
        int code = entity.getCode();
        String tag = entity.getTag();
        if (code == FaceConstant.CODE_SUCCESS || code == FaceConstant.CODE_ALREADY_REGISTER) {
            if (!StringUtils.isEmpty(tag) && faceTagSet.contains(tag)) {
                faceTagSet.remove(tag);
                LogUtils.e(TAG, "@@aa@@ tag 识别成功:" + tag);
            } else {
                LogUtils.e(TAG, "@@aa@@ tag 识别标识已被移除:" + tag);
            }
        }
        String msg = entity.getMsg();
        if (StringUtils.isEmpty(msg)) {
            faceRecognitionViewHelper.setTips("请将人脸对准上方摄像头");
        } else {
            faceRecognitionViewHelper.setTips(msg);
        }
        sendMessage(CODE_FACE_RECOGNITION_RESULT, entity);
    }
}
