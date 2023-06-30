package com.mf.face.helper;

import static android.os.Looper.getMainLooper;
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.mf.face.entity.FaceRecognitionEntity;
import com.mf.face.entity.FaceRegisterEntity;
import com.mf.face.entity.FaceTipsEntity;
import com.mf.face.listener.FaceHelperListener;
import com.mf.face.service.FaceService;
import com.mf.log.LogUtils;

public class FaceHelper {
    private static final String TAG = "face";

    private Context context;
    // 用于接收服务端返回的消息
    private final Messenger serviceMessenger;
    // 与服务端交互的Messenger
    private Messenger messenger;
    private final ServiceConnection connection;
    private boolean isBound = false;
    private FaceHelperListener listener;
    // 记录最后一次操作事件，用于防抖
    private long faceRegisterOperateTime = 0;
    private long faceRecognitionOperateTime = 0;

    private FaceHelper() {
        serviceMessenger = new Messenger(new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                onHandleMessage(msg);
            }
        });
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtils.d(TAG, "onServiceConnected");
                messenger = new Messenger(service);
                isBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.d(TAG, "onServiceDisconnected");
                isBound = false;
                // 服务断开后，立刻重新连接
                startFaceService(context);
            }
        };
    }

    private static class HolderClass {
        private static final FaceHelper instance = new FaceHelper();
    }

    public static FaceHelper getInstance() {
        return FaceHelper.HolderClass.instance;
    }

    public void setListener(FaceHelperListener listener) {
        this.listener = listener;
    }

    public synchronized void startFaceService(Context context) {
        try {
            if (!isBound) {
                LogUtils.d(TAG, "startFaceService");
                this.context = context;
                Intent intent = new Intent(context, FaceService.class);
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } else {
                LogUtils.d(TAG, "FaceService is bound");
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    public synchronized void stopFaceService(Context context) {
        try {
            if (connection != null) {
                context.unbindService(connection);
            }
            isBound = false;
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    public void faceRegister(boolean isShowPreviewView, String licenseNo) {
        LogUtils.d(TAG, "faceRegister isShowPreviewView:" + isShowPreviewView + " licenseNo:" + licenseNo);
        if (antiFaceRegisterShake()) {
            sendMessage(CODE_FACE_REGISTER, isShowPreviewView, licenseNo);
        }
    }

    public void faceRecognition(boolean isShowPreviewView) {
        LogUtils.d(TAG, "faceRecognition isShowPreviewView:" + isShowPreviewView);
        if (antiFaceRecognitionShake()) {
            sendMessage(CODE_FACE_RECOGNITION, isShowPreviewView);
        }
    }

    public void cancelFaceRegisterAndRecognition() {
        LogUtils.d(TAG, "cancelFaceRegisterAndRecognition");
        sendMessage(CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION);
    }

    public void syncFaceData(String faceId, String faceFeature) {
        LogUtils.d(TAG, "syncFaceData faceId:" + faceId);
        sendMessage(CODE_SYNC_FACE_DATA, false, null, faceId, faceFeature);
    }

    public void removeFaceData() {
        LogUtils.d(TAG, "removeFaceData");
        sendMessage(CODE_REMOVE_FACE_DATA);
    }

    private synchronized void sendMessage(int code) {
        sendMessage(code, false, null);
    }

    private synchronized void sendMessage(int code, boolean isShowPreviewView) {
        sendMessage(code, isShowPreviewView, null);
    }

    private synchronized void sendMessage(int code, boolean isShowPreviewView, String licenseNo) {
        sendMessage(code, isShowPreviewView, licenseNo, null, null);
    }

    private synchronized void sendMessage(int code, boolean isShowPreviewView, String licenseNo, String faceId, String faceFeature) {
        if (!isBound) {
            LogUtils.d(TAG, "FaceService is not bind");
            return;
        }
        try {
            Message msg = Message.obtain();
            msg.what = code;
            // 把接收服务器端的回复的Messenger通过Message的replyTo参数传递给服务端
            msg.replyTo = serviceMessenger;
            Bundle bundle = new Bundle();
            bundle.putBoolean("isShowPreviewView", isShowPreviewView);
            if (!TextUtils.isEmpty(licenseNo)) {
                bundle.putString("licenseNo", licenseNo);
            }
            if (!TextUtils.isEmpty(faceId)) {
                bundle.putString("faceId", faceId);
            }
            if (!TextUtils.isEmpty(faceFeature)) {
                bundle.putString("faceFeature", faceFeature);
            }
            msg.setData(bundle);
            messenger.send(msg);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "send message error", e);
        }
    }

    private void onHandleMessage(Message message) {
        Bundle bundle = message.getData();
        int what = message.what;
        int code = bundle.getInt("code", 0);
        String msg = bundle.getString("msg", null);
        switch (what) {
            case CODE_ERROR: {
                onError(code, msg);
                break;
            }
            case CODE_FACE_REGISTER_TIPS_RESULT: {
                FaceTipsEntity entity = (FaceTipsEntity) bundle.getSerializable("entity");
                onFaceRegisterTipsResult(entity);
                break;
            }
            case CODE_FACE_RECOGNITION_TIPS_RESULT: {
                FaceTipsEntity entity = (FaceTipsEntity) bundle.getSerializable("entity");
                onFaceRecognitionTipsResult(entity);
                break;
            }
            case CODE_FACE_REGISTER_RESULT: {
                FaceRegisterEntity entity = (FaceRegisterEntity) bundle.getSerializable("entity");
                onFaceRegisterResult(entity);
                break;
            }
            case CODE_FACE_RECOGNITION_RESULT: {
                FaceRecognitionEntity entity = (FaceRecognitionEntity) bundle.getSerializable("entity");
                onFaceRecognitionResult(entity);
                break;
            }
            case CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION_RESULT:
                onCancelFaceRegisterAndRecognitionResult(code, msg);
                break;
            case CODE_SYNC_FACE_DATA_RESULT:
                onSyncFaceDataResult(code, msg);
                break;
            case CODE_REMOVE_FACE_DATA_RESULT:
                onRemoveFaceDataResult(code, msg);
                break;
            case CODE_MANUAL_CANCEL_FACE_REGISTER:
                onManualCancelFaceRegister();
                break;
            case CODE_MANUAL_CANCEL_FACE_RECOGNITION:
                onManualCancelFaceRecognition();
                break;
            case CODE_PICK_CAR_BY_PLATE:
                onPickCarByPlate();
                break;
            case CODE_PICK_CAR_BY_BERTH:
                onPickCarByBerth();
                break;
            default:
                LogUtils.d(TAG, "unknown what:" + what);
                break;
        }
    }

    private void onError(int code, String msg) {
        LogUtils.d(TAG, "onError code:" + code + " msg:" + msg);
        if (listener != null) {
            listener.onError(code, msg);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onFaceRegisterTipsResult(FaceTipsEntity entity) {
        LogUtils.d(TAG, "onFaceRegisterTipsResult :" + entity);
        if (listener != null) {
            listener.onFaceRegisterTipsResult(entity);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onFaceRecognitionTipsResult(FaceTipsEntity entity) {
        LogUtils.d(TAG, "onFaceRecognitionTipsResult :" + entity);
        if (listener != null) {
            listener.onFaceRecognitionTipsResult(entity);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onFaceRegisterResult(FaceRegisterEntity entity) {
        LogUtils.d(TAG, "onFaceRegisterResult :" + entity);
        if (listener != null) {
            listener.onFaceRegisterResult(entity);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onFaceRecognitionResult(FaceRecognitionEntity entity) {
        LogUtils.d(TAG, "onFaceRecognitionResult :" + entity);
        if (listener != null) {
            listener.onFaceRecognitionResult(entity);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onCancelFaceRegisterAndRecognitionResult(int code, String msg) {
        LogUtils.d(TAG, "onCancelFaceRegisterAndRecognitionResult code:" + code + " msg:" + msg);
        if (listener != null) {
            listener.onCancelFaceRegisterAndRecognitionResult(code, msg);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onSyncFaceDataResult(int code, String msg) {
        LogUtils.d(TAG, "onSyncFaceDataResult code:" + code + " msg:" + msg);
        if (listener != null) {
            listener.onSyncFaceDataResult(code, msg);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onRemoveFaceDataResult(int code, String msg) {
        LogUtils.d(TAG, "onRemoveFaceDataResult code:" + code + " msg:" + msg);
        if (listener != null) {
            listener.onRemoveFaceDataResult(code, msg);
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onManualCancelFaceRegister() {
        LogUtils.d(TAG, "onManualCancelFaceRegister");
        if (listener != null) {
            listener.onManualCancelFaceRegister();
        }
    }

    private void onManualCancelFaceRecognition() {
        LogUtils.d(TAG, "onManualCancelFaceRecognition");
        if (listener != null) {
            listener.onManualCancelFaceRecognition();
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onPickCarByPlate() {
        LogUtils.d(TAG, "onPickCarByPlate");
        if (listener != null) {
            listener.onPickCarByPlate();
        } else {
            LogUtils.w("listener is null");
        }
    }

    private void onPickCarByBerth() {
        LogUtils.d(TAG, "onPickCarByBerth");
        if (listener != null) {
            listener.onPickCarByBerth();
        } else {
            LogUtils.w("listener is null");
        }
    }

    private boolean antiFaceRegisterShake() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - faceRegisterOperateTime < 200) {
            LogUtils.w(TAG, "antiFaceRegisterShake");
            return false;
        }
        faceRegisterOperateTime = currentTimeMillis;
        return true;
    }

    private boolean antiFaceRecognitionShake() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - faceRecognitionOperateTime < 200) {
            LogUtils.w(TAG, "antiFaceRecognitionShake");
            return false;
        }
        faceRecognitionOperateTime = currentTimeMillis;
        return true;
    }
}
