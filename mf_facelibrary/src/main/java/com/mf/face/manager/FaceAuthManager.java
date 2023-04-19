package com.mf.face.manager;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.mf.face.listener.SdkInitListener;
import com.mf.face.model.SingleBaseConfig;
import com.mf.log.LogUtils;

public class FaceAuthManager {

    private static final String TAG = "face";

    // SDK状态
    public static final int SDK_UNACTIVATION = 0;
    public static final int SDK_INIT_SUCCESS = 1;
    public static final int SDK_MODEL_LOAD_SUCCESS = 2;

    // SDK当前状态
    public static volatile int initStatus = SDK_UNACTIVATION;
    // SDK是否初始化成功
    public static volatile boolean initModelSuccess = false;

    // 鉴权
    private FaceAuth faceAuth;

    private FaceAuthManager() {
        // 鉴权
        faceAuth = new FaceAuth();
        setActiveLog();
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_NO_BIND, 2);
    }

    public void setActiveLog() {
        if (faceAuth != null) {
            if (SingleBaseConfig.getBaseConfig().isLog()) {
                // 日志输出等级
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_ERROR_MESSAGE, 1);
            } else {
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 0);
            }
        }
    }

    private static class HolderClass {
        private static final FaceAuthManager instance = new FaceAuthManager();
    }

    public static FaceAuthManager getInstance() {
        return HolderClass.instance;
    }


    /**
     * 初始化鉴权，如果鉴权通过，直接初始化模型
     *
     * @param context
     * @param listener
     */
    public void init(final Context context, final SdkInitListener listener) {
        PreferencesUtil.initPrefs(context.getApplicationContext());
        final boolean licenseOfflineKeyExists = FileUitls.fileIsExists("/sdcard/License.zip");
        final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");
        final String licenseBatchlineKey = PreferencesUtil.getString("activate_batchline_key", "");

        String deviceId = faceAuth.getDeviceId(context);
        LogUtils.d(TAG, "人脸识别 faceDeviceId:" + deviceId);

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (!licenseOfflineKeyExists && TextUtils.isEmpty(licenseOnlineKey)
                && TextUtils.isEmpty(licenseBatchlineKey)) {
            LogUtils.d(TAG, "未授权设备，请完成授权激活");
//            ToastUtils.toast(context, "未授权设备，请完成授权激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
            return;
        }
        if (listener != null) {
            listener.initStart();
        }

        if (licenseOfflineKeyExists) {
            // 离线激活
            faceAuth.initLicenseOffLine(context, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        initModelSuccess = true;
                        LogUtils.d(TAG, "离线激活成功");
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                    } else {
                        initStatus = SDK_UNACTIVATION;
                        initModelSuccess = false;
                        LogUtils.d(TAG, "离线激活失败");
                        if (listener != null) {
                            listener.initLicenseFail(code, response);
                        }
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseOnlineKey)) {
            // 在线激活
            faceAuth.initLicenseOnLine(context, licenseOnlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        initModelSuccess = true;
                        LogUtils.d(TAG, "在线激活成功");
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                    } else {
                        initStatus = SDK_UNACTIVATION;
                        initModelSuccess = false;
                        LogUtils.d(TAG, "在线激活失败");
                        if (listener != null) {
                            listener.initLicenseFail(code, response);
                        }
                        // 离线激活
                        faceAuth.initLicenseOffLine(context, new Callback() {
                            @Override
                            public void onResponse(int code, String response) {
                                if (code == 0) {
                                    initStatus = SDK_INIT_SUCCESS;
                                    initModelSuccess = true;
                                    LogUtils.d(TAG, "离线激活成功");
                                    if (listener != null) {
                                        listener.initLicenseSuccess();
                                    }
                                } else {
                                    initStatus = SDK_UNACTIVATION;
                                    initModelSuccess = false;
                                    LogUtils.d(TAG, "离线激活失败");
                                    if (listener != null) {
                                        listener.initLicenseFail(code, response);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseBatchlineKey)) {
            // 应用批量激活
            faceAuth.initLicenseBatchLine(context, licenseBatchlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        initModelSuccess = true;
                        LogUtils.d(TAG, "应用批量激活失败");
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                    } else {
                        initStatus = SDK_UNACTIVATION;
                        initModelSuccess = false;
                        LogUtils.d(TAG, "应用批量激活失败");
                        if (listener != null) {
                            listener.initLicenseFail(code, response);
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
        }
    }
}
