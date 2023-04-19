package com.mf.face.listener;

import com.mf.log.LogUtils;

public class SdkInitListenerImpl implements SdkInitListener {
    private static final String TAG = "face";

    @Override
    public void initStart() {
        LogUtils.d(TAG, "initStart");
    }

    @Override
    public void initLicenseSuccess() {
        LogUtils.d(TAG, "initLicenseSuccess");
    }

    @Override
    public void initLicenseFail(int errorCode, String msg) {
        LogUtils.d(TAG, "initLicenseFail errorCode:" + errorCode + " msg:" + msg);
    }

    @Override
    public void initModelSuccess() {
        LogUtils.d(TAG, "initModelSuccess");
    }

    @Override
    public void initModelFail(int errorCode, String msg) {
        LogUtils.d(TAG, "initModelFail errorCode:" + errorCode + " msg:" + msg);
    }
}
