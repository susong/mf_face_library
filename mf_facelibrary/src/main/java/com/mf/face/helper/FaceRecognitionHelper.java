package com.mf.face.helper;


import android.content.Context;
import android.hardware.Camera;

import com.example.datalibrary.model.User;
import com.mf.face.callback.CameraDataCallback;
import com.mf.face.callback.FaceDetectCallBack;
import com.mf.face.camera.AutoTexturePreviewView;
import com.mf.face.camera.CameraPreviewManager;
import com.mf.face.entity.FaceRecognitionEntity;
import com.mf.face.entity.FaceTipsEntity;
import com.mf.face.listener.FaceRecognitionListener;
import com.mf.face.listener.FaceTipsListener;
import com.mf.face.manager.FaceSDKManager;
import com.mf.face.manager.FaceTrackManager;
import com.mf.face.manager.SaveImageManager;
import com.mf.face.model.FaceConstant;
import com.mf.face.model.LivenessModel;
import com.mf.face.model.SingleBaseConfig;
import com.mf.log.LogUtils;

public class FaceRecognitionHelper {

    private static final String TAG = "face";

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PREFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private Context mContext;
    // 摄像头预览界面
    private AutoTexturePreviewView mCameraPreviewView;

    // 摄像头个数
    private int mCameraNum;
    // 是否有红外摄像头
    private boolean mHasNirCamera = false;
    // 摄像头采集数据
    private volatile byte[] mRgbData;
    private volatile byte[] mIrData;

    // 是否显示摄像头预览界面
    private volatile boolean mIsShowPreviewView = true;
    private volatile boolean mIsShowNirPreviewView = false;

    // 检测到的人脸用户信息
    private volatile User mUser;
    // 计算检测到人脸的时间
    private volatile boolean isTime = true;
    // 检测到人脸开始的时间
    private volatile long startTime;
    // 检测人脸最后提示时间
    private volatile long lastTipsTime;
    // 是否第一次检测到人脸
    private volatile boolean detectCount;
    // 是否保存人脸检测中的图片
    private volatile boolean isSaveImage;
    // 标识
    private volatile String mTag = "";
    // 人脸识别回调
    private FaceRecognitionListener recognitionListener;
    private FaceTipsListener tipsListener;
    // 使用类型，1-只开启RGB，2-只开启NIR，3-分别开启RGB和NIR，4-同时开启RGB和NIR
    private int mUseType = FaceConstant.USE_TYPE_RGB;

    private CameraPreviewManager rgbCameraPreviewManager;
    private CameraPreviewManager nirCameraPreviewManager;
    private FaceSDKManager faceSDKManager;
    private FaceTrackManager faceTrackManager;
    private LivenessModel faceAdoptModel;
    private FaceViewHelper mFaceViewHelper;

    public FaceRecognitionHelper() {
    }

    public FaceRecognitionHelper setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public FaceRecognitionHelper setUseType(int useType) {
        this.mUseType = useType;
        return this;
    }

    public FaceRecognitionHelper setFaceViewHelper(FaceViewHelper faceViewHelper) {
        this.mFaceViewHelper = faceViewHelper;
        return this;
    }

    public FaceRecognitionHelper setRecognitionListener(FaceRecognitionListener recognitionListener) {
        this.recognitionListener = recognitionListener;
        return this;
    }

    public FaceRecognitionHelper setTipsListener(FaceTipsListener tipsListener) {
        this.tipsListener = tipsListener;
        return this;
    }

    public void init() {
        // 摄像头数目
        mCameraNum = Camera.getNumberOfCameras();
        faceSDKManager = FaceSDKManager.getInstance();
        faceTrackManager = FaceTrackManager.getInstance();
        if (mCameraNum == 0 || mUseType != FaceConstant.USE_TYPE_NIR) {
            rgbCameraPreviewManager = new CameraPreviewManager();
        }
        if (mCameraNum > 1 && (mUseType == FaceConstant.USE_TYPE_NIR
                || mUseType == FaceConstant.USE_TYPE_RGB_AND_NIR
                || mUseType == FaceConstant.USE_TYPE_RGB_WITH_NIR)) {
            nirCameraPreviewManager = new CameraPreviewManager();
            nirCameraPreviewManager.setNir(true);
        }
    }

    /**
     * 人脸注册
     *
     * @param isShowPreviewView 是否显示摄像头预览界面
     */
    public void onFaceRecognition(boolean isShowPreviewView, String tag) {
        LogUtils.d(TAG, "onFaceRecognition isShowPreviewView:" + isShowPreviewView
                + " useType:" + mUseType + " tag:" + tag);
        this.mIsShowPreviewView = isShowPreviewView;
        this.mTag = tag;
        // 摄像头图像预览
        if (mCameraNum == 1) {
            startCameraPreview();
        } else if (mCameraNum > 1) {
            // 双摄像头，默认另一个是红外摄像头
            mHasNirCamera = true;
            if (mUseType == FaceConstant.USE_TYPE_RGB) {
                startCameraPreview();
            } else if (mUseType == FaceConstant.USE_TYPE_NIR || mUseType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
                startNirCameraPreview();
            } else if (mUseType == FaceConstant.USE_TYPE_RGB_WITH_NIR) {
                startCameraPreview();
                if (SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
                    startNirCameraPreview();
                }
            }
        }
    }

    public void onCancel() {
        // 关闭摄像头
        if (rgbCameraPreviewManager != null) {
            rgbCameraPreviewManager.stopPreview();
        }
        if (nirCameraPreviewManager != null) {
            nirCameraPreviewManager.stopPreview();
        }
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            rgbCameraPreviewManager.setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            // 设置前置摄像头
            // rgbCameraPreviewManager.setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
            // 设置后置摄像头
            // rgbCameraPreviewManager.setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
            // 设置USB摄像头
            rgbCameraPreviewManager.setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        if (mIsShowPreviewView) {
            mCameraPreviewView = mFaceViewHelper.getRgbCameraPreviewView();
        } else {
            mCameraPreviewView = null;
        }
        rgbCameraPreviewManager.startPreview(mContext, mCameraPreviewView,
                PREFER_WIDTH, PREFER_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        if (mHasNirCamera && SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
                            dealRgb(data);
                        } else {
                            // 摄像头数据处理
                            faceDetect(data, width, height);
                        }
                    }
                });
    }


    /**
     * 红外摄像头图像预览
     */
    private void startNirCameraPreview() {
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            nirCameraPreviewManager.setCameraFacing(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
        } else {
            nirCameraPreviewManager.setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        if (mUseType == FaceConstant.USE_TYPE_NIR && mIsShowPreviewView) {
            mCameraPreviewView = mFaceViewHelper.getRgbCameraPreviewView();
        } else if (mIsShowPreviewView && mIsShowNirPreviewView) {
            mCameraPreviewView = mFaceViewHelper.getNirCameraPreviewView();
        } else {
            mCameraPreviewView = null;
        }
        nirCameraPreviewManager.startPreview(mContext, mCameraPreviewView,
                PREFER_WIDTH, PREFER_HEIGHT, new CameraDataCallback() {

                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        if (mHasNirCamera && SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
                            dealNir(data);
                        } else {
                            // 摄像头数据处理
                            faceDetect(data, width, height);
                        }
                    }
                });
    }


    private void dealRgb(byte[] data) {
        mRgbData = data;
        checkData();
    }

    private void dealNir(byte[] data) {
        mIrData = data;
        checkData();
    }

    private synchronized void checkData() {
        if (mRgbData != null && mIrData != null) {

            faceSDKManager.onDetectCheck(mRgbData, mIrData, null,
                    PREFER_HEIGHT, PREFER_WIDTH, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            if (faceAdoptModel == livenessModel) {
                                return;
                            }
                            faceAdoptModel = livenessModel;
                            // 人脸检测结果
                            faceDetectResult(livenessModel);
                            if (isSaveImage) {
                                SaveImageManager.getInstance().saveImage(livenessModel);
                            }

                        }

                        @Override
                        public void onTip(int code, String msg) {
                            lastTipsTime = System.currentTimeMillis();
                            if (code == 0) {
                                onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY, msg);
                            } else {
                                onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED, msg);
                            }
                        }

                        @Override
                        public void onFaceDetectDrawCallback(LivenessModel livenessModel) {
                            if (mIsShowPreviewView && mUseType == FaceConstant.USE_TYPE_RGB) {
                                // 绘制人脸框
                                mFaceViewHelper.showFrame(livenessModel);
                            }
                        }
                    });
            mRgbData = null;
            mIrData = null;
        }
    }


    /**
     * 摄像头数据处理
     */
    private void faceDetect(byte[] data, final int width, final int height) {
        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();

        if (liveType == FaceConstant.LIVE_TYPE_NONE) { // 无活体检测
            faceTrackManager.setAliving(false);
        } else if (liveType >= FaceConstant.LIVE_TYPE_RGB) { // 活体检测
            faceTrackManager.setAliving(true);
        }

        // 摄像头预览数据进行人脸检测
        faceSDKManager.onDetectCheck(data, null, null,
                height, width, liveType, new FaceDetectCallBack() {
                    @Override
                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                        // 人脸检测结果
                        faceDetectResult(livenessModel);
                        if (isSaveImage) {
                            SaveImageManager.getInstance().saveImage(livenessModel);
                        }
                    }

                    @Override
                    public void onTip(int code, String msg) {
                        lastTipsTime = System.currentTimeMillis();
                        if (code == 0) {
                            onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY, msg);
                        } else {
                            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED, msg);
                        }
                    }

                    @Override
                    public void onFaceDetectDrawCallback(LivenessModel livenessModel) {
                        if (mIsShowPreviewView && mUseType == FaceConstant.USE_TYPE_RGB) {
                            // 绘制人脸框
                            mFaceViewHelper.showFrame(livenessModel);
                        }
                    }
                });
    }

    /**
     * 人脸检测结果
     */
    private void faceDetectResult(final LivenessModel livenessModel) {
        if (livenessModel == null) {
            // 未检测到人脸
            if (isTime) {
                // 开始计算未检测到人脸的时间
                isTime = false;
                startTime = System.currentTimeMillis();
            }
            detectCount = true;
            long endTime = System.currentTimeMillis() - startTime;

            if (endTime < 1000) {
                // 未检测到人脸在指定时间内
            } else {
                // 未检测到人脸超过指定时间
                onTips(FaceConstant.CODE_NO_FACE_DETECTED, FaceConstant.TIPS_NO_FACE_DETECTED);
            }
            return;
        }

        // 检测到人脸
        isTime = true;
        if (detectCount) {
            detectCount = false;
            // 第一次检测到人脸
            onTips(FaceConstant.CODE_FACE_DETECTED, FaceConstant.TIPS_FACE_DETECTED);
        } else {
            // 非第一次检测到人脸
        }

        User user = livenessModel.getUser();
        if (user == null) {
            // 数据库不存在匹配的人脸信息
            mUser = null;
            long endTime = System.currentTimeMillis() - lastTipsTime;
            if (endTime > 300) {
                // 一段时间后，没有报错提示，则提示不存在匹配的人脸信息
                onTips(FaceConstant.CODE_NO_MATCHING_FACE_EXISTS, FaceConstant.TIPS_NO_MATCHING_FACE_EXISTS);
            }
        } else {
            // 数据库存在匹配的人脸信息
            mUser = user;
            onFaceRecognitionResult(FaceConstant.CODE_SUCCESS, FaceConstant.TIPS_RECOGNITION_SUCCESS, null, user.getUserId());
        }
    }

    private void onTips(int code, String msg) {
        onTips(code, msg, null);
    }

    private void onTips(int code, String msg, String detail) {
        if (tipsListener != null) {
            FaceTipsEntity entity = new FaceTipsEntity();
            entity.setShowPreviewView(mIsShowPreviewView);
            entity.setCode(code);
            entity.setMsg(msg);
            entity.setDetail(detail);
            entity.setUseType(mUseType);
            entity.setTag(mTag);
            entity.setWhich(FaceConstant.FACE_RECOGNITION);
            tipsListener.onFaceTipsCallback(entity);
        }
    }

    private void onFaceRecognitionResult(int code, String msg, String detail, String faceId) {
        if (recognitionListener != null) {
            FaceRecognitionEntity entity = new FaceRecognitionEntity();
            entity.setShowPreviewView(mIsShowPreviewView);
            entity.setCode(code);
            entity.setMsg(msg);
            entity.setDetail(detail);
            entity.setFaceId(faceId);
            entity.setUseType(mUseType);
            entity.setTag(mTag);
            entity.setWhich(FaceConstant.FACE_RECOGNITION);
            recognitionListener.onFaceRecognitionCallback(entity);
        }
    }
}
