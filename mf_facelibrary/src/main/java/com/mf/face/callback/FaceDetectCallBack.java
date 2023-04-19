/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.mf.face.callback;


import com.mf.face.model.LivenessModel;

/**
 * 人脸检测回调接口。
 *
 * @Time: 2019/1/25
 * @Author: v_chaixiaogang
 */
public interface FaceDetectCallBack {
    void onFaceDetectCallback(LivenessModel livenessModel);

    void onTip(int code, String msg);

    void onFaceDetectDrawCallback(LivenessModel livenessModel);
}
