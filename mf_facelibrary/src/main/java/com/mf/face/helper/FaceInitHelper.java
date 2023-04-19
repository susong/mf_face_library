package com.mf.face.helper;

import android.content.Context;
import android.hardware.Camera;

import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;
import com.mf.base.config.GlobalProperty;
import com.mf.face.listener.SdkInitListener;
import com.mf.face.manager.FaceAuthManager;
import com.mf.face.manager.FaceSDKManager;
import com.mf.face.model.SingleBaseConfig;
import com.mf.face.utils.ConfigUtils;
import com.mf.log.LogUtils;
import com.mf.face.view.PreviewTexture;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FaceInitHelper {
    private static final String TAG = "face";

    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    private boolean isDBLoad;
    private Future future;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;

    private FaceInitHelper() {
    }

    private static class HolderClass {
        private static final FaceInitHelper instance = new FaceInitHelper();
    }

    public static FaceInitHelper getInstance() {
        return HolderClass.instance;
    }

    public void init(Context context, SdkInitListener sdkInitListener) {
        initConfig(context);
//        initRGBCheck(context);
        initDBApi(context, sdkInitListener);
    }

    private void initConfig(Context context) {
        boolean isConfigExit = ConfigUtils.createConfig(context);
        boolean isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
//            Toast.makeText(context, "初始配置加载成功", Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG,"初始配置加载成功");
        } else {
//            Toast.makeText(context, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            LogUtils.w(TAG,"初始配置失败,将重置文件内容为默认配置");
            ConfigUtils.modityJson();
        }
    }

    private void initRGBCheck(Context context) {
        if (isSetCameraId()) {
            return;
        }
        int mCameraNum = Camera.getNumberOfCameras();
        LogUtils.d(TAG, "CameraNum:" + mCameraNum);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
        }
        if (mCameraNum > 1) {
            try {
                mCamera = new Camera[mCameraNum];
                previewTextures = new PreviewTexture[mCameraNum];
                mCamera[0] = Camera.open(0);
                previewTextures[0] = new PreviewTexture(context, null);
                previewTextures[0].setCamera(mCamera[0], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(0);
                        }
                        release(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera[1] = Camera.open(1);
                previewTextures[1] = new PreviewTexture(context, null);
                previewTextures[1].setCamera(mCamera[1], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(1);
                        }
                        release(1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setRgbCameraId(0);
        }
    }

    private void setRgbCameraId(int index) {
        SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        ConfigUtils.modityJson();
    }

    private boolean isSetCameraId() {
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1) {
            return false;
        } else {
            return true;
        }
    }

    private void release(int id) {
        if (mCamera != null && mCamera[id] != null) {
            if (mCamera[id] != null) {
                mCamera[id].setPreviewCallback(null);
                mCamera[id].stopPreview();
                previewTextures[id].release();
                mCamera[id].release();
                mCamera[id] = null;
            }
        }
    }

    private void initDBApi(Context context, SdkInitListener sdkInitListener) {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        isDBLoad = false;
        future = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                FaceApi.getInstance().init(new DBLoadListener() {

                    @Override
                    public void onStart(int successCount) {
                        LogUtils.d(TAG, "initDB onStart successCount:" + successCount);
                    }

                    @Override
                    public void onLoad(final int finishCount, final int successCount, final float progress) {
                        LogUtils.d(TAG, "initDB onLoad successCount:" + successCount);
                    }

                    @Override
                    public void onComplete(final List<User> users, final int successCount) {
                        LogUtils.d(TAG, "initDB onComplete successCount:" + successCount);
                        FaceApi.getInstance().setUsers(users);
                        isDBLoad = true;
                        initLicense(context, sdkInitListener);
                    }

                    @Override
                    public void onFail(final int finishCount, final int successCount, final List<User> users) {
                        LogUtils.d(TAG, "initDB onFail finishCount:" + finishCount + " successCount:" + successCount);
                        FaceApi.getInstance().setUsers(users);
//                        ToastUtils.toast(context,
//                                "人脸库加载失败,共" + successCount + "条数据, 已加载" + finishCount + "条数据");
                        isDBLoad = true;
                        initLicense(context, sdkInitListener);
                    }
                }, context);
            }
        });
    }

    private void initLicense(Context context, SdkInitListener sdkInitListener) {
        PreferencesUtil.initPrefs(context);
        PreferencesUtil.putString("activate_online_key",
                GlobalProperty.getInstance().getString("baidu.face.serialnumber", ""));
        FaceAuthManager.getInstance().init(context, new SdkInitListener() {
            @Override
            public void initStart() {
                LogUtils.d(TAG, "initStart");
                if (sdkInitListener != null) {
                    sdkInitListener.initStart();
                }
            }

            @Override
            public void initLicenseSuccess() {
                LogUtils.d(TAG, "initLicenseSuccess");
                if (sdkInitListener != null) {
                    sdkInitListener.initLicenseSuccess();
                }
                initModel(context, sdkInitListener);
            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                LogUtils.d(TAG, "initLicenseFail errorCode:" + errorCode + " msg:" + msg);
                if (sdkInitListener != null) {
                    sdkInitListener.initLicenseFail(errorCode, msg);
                }
            }

            @Override
            public void initModelSuccess() {
                LogUtils.d(TAG, "initModelSuccess");
            }

            @Override
            public void initModelFail(int errorCode, String msg) {
                LogUtils.d(TAG, "initModelFail errorCode:" + errorCode + " msg:" + msg);
            }
        });
    }

    private void initModel(Context context, SdkInitListener sdkInitListener) {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(context, sdkInitListener);
        } else {
            if (sdkInitListener != null) {
                sdkInitListener.initModelSuccess();
            }
        }
    }

}
