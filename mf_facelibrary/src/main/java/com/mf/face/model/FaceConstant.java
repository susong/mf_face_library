package com.mf.face.model;

public interface FaceConstant {
    String FaceImportDir = "mf/cache/face/Face-Import";
    String FaceImportSuccessDir = "mf/cache/face/Face-Import-Success";
    String FaceCacheDir = "mf/cache/face/Face-Cache";
    String FaceImportFailDir = "mf/cache/face/Face-Import-Fail";
    String FaceSaveImageDir = "mf/cache/face/Face-Save-Image";

    int CODE_ERROR = 0;
    int CODE_FACE_REGISTER = 1;
    int CODE_FACE_REGISTER_RESULT = 2;
    int CODE_FACE_REGISTER_TIPS_RESULT = 3;
    int CODE_FACE_RECOGNITION = 4;
    int CODE_FACE_RECOGNITION_RESULT = 5;
    int CODE_FACE_RECOGNITION_TIPS_RESULT = 6;
    int CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION = 7;
    int CODE_CANCEL_FACE_REGISTER_AND_RECOGNITION_RESULT = 8;
    int CODE_SYNC_FACE_DATA = 9;
    int CODE_SYNC_FACE_DATA_RESULT = 10;
    int CODE_REMOVE_FACE_DATA = 11;
    int CODE_REMOVE_FACE_DATA_RESULT = 12;
    int CODE_MANUAL_CANCEL_FACE_REGISTER = 13;
    int CODE_MANUAL_CANCEL_FACE_RECOGNITION = 14;
    int CODE_PICK_CAR_BY_PLATE = 15;
    int CODE_PICK_CAR_BY_BERTH = 16;


    int CODE_SUCCESS = 1;
    int CODE_ALREADY_REGISTER = 2;
    int CODE_NO_FACE_DETECTED = 3;
    int CODE_FACE_DETECTED = 4;
    int CODE_NO_MATCHING_FACE_EXISTS = 5;
    int CODE_FAILED = 0;
    int CODE_NOBODY = -1;
    int CODE_COVERED = -2;
    int CODE_FAKE = -3;
    int CODE_MANY = -4;
    int CODE_CROP_FACE_FAILED = -5;
    int CODE_GET_FACE_FEATURE_FAILED = -6;
    int CODE_GET_FACE_FAILED = -7;

    String TIPS_REGISTER_SUCCESS = "人脸注册成功";
    String TIPS_RECOGNITION_SUCCESS = "人脸识别成功";
    String TIPS_ALREADY_REGISTER = "人脸已经注册过";
    String TIPS_NO_FACE_DETECTED = "检测不到人脸了";
    String TIPS_FACE_DETECTED = "开始检测到人脸";
    String TIPS_NO_MATCHING_FACE_EXISTS = "不存在匹配的人脸信息";
    String TIPS_NOBODY = "请保持面部在取景框内";
    String TIPS_COVERED = "请保证人脸区域清晰无遮挡";
    String TIPS_FAKE = "请保证采集对象为真人";
    String TIPS_MANY = "请保证取景框内只有一个人脸";
    String TIPS_CROP_FACE_FAILED = "人脸抠图失败";
    String TIPS_GET_FACE_FEATURE_FAILED = "提取人脸特征失败";
    String TIPS_GET_FACE_FAILED = "获取头像失败";
    String TIPS_CANCEL_FACE_REGISTER_AND_RECOGNITION_SUCCESS = "取消人脸识别成功";
    String TIPS_SYNC_FACE_DATA_SUCCESS = "同步人脸信息成功";
    String TIPS_REMOVE_FACE_DATA_SUCCESS = "清空人脸信息成功";
    String TIPS_CANCEL_FACE_REGISTER_AND_RECOGNITION_FAILED = "取消人脸识别失败";
    String TIPS_SYNC_FACE_DATA_FAILED = "同步人脸信息失败";
    String TIPS_REMOVE_FACE_DATA_FAILED = "清空人脸信息失败";

    /**
     * 人脸注册
     */
    int FACE_REGISTER = 0;
    /**
     * 人脸识别
     */
    int FACE_RECOGNITION = 1;

    /**
     * 活体检测类型，不使用活体
     */
    int LIVE_TYPE_NONE = 0;
    /**
     * 活体检测类型，RGB活体
     */
    int LIVE_TYPE_RGB = 1;
    /**
     * 活体检测类型，RGB+NIR活体
     */
    int LIVE_TYPE_RGB_NIR = 2;
    /**
     * 活体检测类型，RGB+Depth活体
     */
    int LIVE_TYPE_RGB_DEPTH = 3;
    /**
     * 活体检测类型，RGB+NIR+Depth活体
     */
    int LIVE_TYPE_RGB_NIR_DEPTH = 4;

    /**
     * 使用类型，只开启RGB
     */
    int USE_TYPE_RGB = 1;
    /**
     * 使用类型，只开启NIR
     */
    int USE_TYPE_NIR = 2;
    /**
     * 使用类型，分别开启RGB和NIR
     */
    int USE_TYPE_RGB_AND_NIR = 3;
    /**
     * 使用类型，同时开启RGB和NIR
     */
    int USE_TYPE_RGB_WITH_NIR = 4;
}
