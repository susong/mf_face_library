package com.mf.face.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;
import com.blankj.utilcode.util.EncodeUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;
import com.mf.face.callback.CameraDataCallback;
import com.mf.face.callback.FaceDetectCallBack;
import com.mf.face.callback.FaceFeatureCallBack;
import com.mf.face.camera.AutoTexturePreviewView;
import com.mf.face.camera.CameraPreviewManager;
import com.mf.face.entity.FaceRegisterEntity;
import com.mf.face.entity.FaceTipsEntity;
import com.mf.face.listener.FaceRegisterListener;
import com.mf.face.listener.FaceTipsListener;
import com.mf.face.manager.FaceSDKManager;
import com.mf.face.manager.FaceTrackManager;
import com.mf.face.model.FaceConstant;
import com.mf.face.model.LivenessModel;
import com.mf.face.model.SingleBaseConfig;
import com.mf.face.utils.BitmapUtils;
import com.mf.face.utils.FaceOnDrawTexturViewUtil;
import com.mf.face.utils.FileUtils;
import com.mf.log.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FaceRegisterHelper {

    private static final String TAG = "face";

    // RGB摄像头图像宽和高
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PREFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();


    private Context mContext;
    // 摄像头预览界面
    private AutoTexturePreviewView mCameraPreviewView;
    // 包含适配屏幕后的人脸的x坐标，y坐标，width和height
    private final float[] mPointXY = new float[4];
    // 人脸特征
    private final byte[] mFeatures = new byte[512];
    // 摄像头个数
    private int mCameraNum;
    // 是否有红外摄像头
    private boolean mHasNirCamera = false;
    // 摄像头采集数据
    private volatile byte[] mRgbData;
    private volatile byte[] mIrData;
    // 抠取的人脸头像
    private Bitmap mCropBitmap;
    // 是否采集人脸成功
    private boolean mCollectSuccess = false;
    // 是否显示摄像头预览界面，不显示预览界面时，直接为快速注册
    private boolean mIsShowPreviewView = true;
    private boolean mIsShowNirPreviewView = false;
    // 是否是快速人脸注册，否：人脸必须在指定的圆框里面，是：无此限制
    private boolean mIsFastRegister = false;
    // 车牌号
    private String mLicenseNo = "";
    // 标识
    private String mTag = "";
    // 人脸注册回调
    private FaceRegisterListener registerListener;
    private FaceTipsListener tipsListener;
    // 使用类型，1-只开启RGB，2-只开启NIR，3-分别开启RGB和NIR，4-同时开启RGB和NIR
    private int mUseType = FaceConstant.USE_TYPE_RGB;

    private CameraPreviewManager rgbCameraPreviewManager;
    private CameraPreviewManager nirCameraPreviewManager;
    private FaceSDKManager faceSDKManager;
    private FaceTrackManager faceTrackManager;
    private FaceViewHelper mFaceViewHelper;

    public FaceRegisterHelper() {
    }

    public FaceRegisterHelper setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public FaceRegisterHelper setUseType(int useType) {
        this.mUseType = useType;
        return this;
    }

    public FaceRegisterHelper setFaceViewHelper(FaceViewHelper faceViewHelper) {
        this.mFaceViewHelper = faceViewHelper;
        return this;
    }

    public FaceRegisterHelper setListener(FaceRegisterListener listener) {
        this.registerListener = listener;
        return this;
    }

    public FaceRegisterHelper setTipsListener(FaceTipsListener tipsListener) {
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

    public FaceSDKManager getFaceSDKManager() {
        return faceSDKManager;
    }

    /**
     * 人脸注册
     *
     * @param isShowPreviewView 是否显示摄像头预览界面，不显示预览界面时，直接为快速注册
     * @param isFastRegister    是否是快速人脸注册，否：人脸必须在指定的圆框里面，是：无此限制
     * @param licenseNo         车牌号
     * @param tag               标识
     */
    public void onFaceRegister(boolean isShowPreviewView, boolean isFastRegister, String licenseNo, String tag) {
        LogUtils.d(TAG, "onFaceRegister isShowPreviewView:" + isShowPreviewView
                + " isFastRegister:" + isFastRegister + " licenseNo:" + licenseNo
                + " useType:" + mUseType + " tag:" + tag);
        this.mIsShowPreviewView = isShowPreviewView;
        this.mIsFastRegister = isFastRegister;
        this.mLicenseNo = licenseNo;
        this.mTag = tag;
        // 摄像头图像预览
        if (mCameraNum == 1) {
            mFaceViewHelper.showMask(isShowPreviewView && !isFastRegister);
            mFaceViewHelper.getRgbCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
            startCameraPreview();
        } else if (mCameraNum > 1) {
            // 双摄像头，默认另一个是红外摄像头
            mHasNirCamera = true;
            if (mUseType == FaceConstant.USE_TYPE_RGB) {
                mFaceViewHelper.showMask(isShowPreviewView && !isFastRegister);
                mFaceViewHelper.getRgbCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
                startCameraPreview();
            } else if (mUseType == FaceConstant.USE_TYPE_NIR) {
                mFaceViewHelper.showMask(isShowPreviewView && !isFastRegister);
                mFaceViewHelper.getRgbCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
                startNirCameraPreview();
            } else if (mUseType == FaceConstant.USE_TYPE_RGB_AND_NIR) {
                mFaceViewHelper.getNirCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
                startNirCameraPreview();
            } else if (mUseType == FaceConstant.USE_TYPE_RGB_WITH_NIR) {
                mFaceViewHelper.showMask(isShowPreviewView && !isFastRegister);
                mFaceViewHelper.getRgbCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
                startCameraPreview();
                if (SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
                    mFaceViewHelper.getNirCameraPreviewView().setIsRegister(isShowPreviewView && !isFastRegister);
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
        if (mCropBitmap != null) {
            if (!mCropBitmap.isRecycled()) {
                mCropBitmap.recycle();
            }
            mCropBitmap = null;
        }
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        mCollectSuccess = false;
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
                        if (mCollectSuccess) {
                            return;
                        }
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
        mCollectSuccess = false;
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
                        if (mCollectSuccess) {
                            return;
                        }
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
        faceDetect();
    }

    private void dealNir(byte[] data) {
        mIrData = data;
        faceDetect();
    }


    /**
     * 摄像头数据处理
     */
    private void faceDetect() {
        if (mCollectSuccess) {
            return;
        }

        // 摄像头预览数据进行人脸检测
        faceSDKManager.onDetectCheck(mRgbData, mIrData, null, PREFER_HEIGHT,
                PREFER_WIDTH, 2, 2, new FaceDetectCallBack() {
                    @Override
                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                        checkFaceBound(livenessModel);
                    }

                    @Override
                    public void onTip(int code, final String msg) {
                        if (code == 0) {
                            onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY, msg);
                        } else {
                            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED, msg);
                        }
                    }

                    @Override
                    public void onFaceDetectDrawCallback(LivenessModel livenessModel) {

                    }
                });
    }

    /**
     * 摄像头数据处理
     */
    private void faceDetect(byte[] data, final int width, final int height) {
        if (mCollectSuccess) {
            return;
        }

        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();

        if (liveType == FaceConstant.LIVE_TYPE_NONE) { // 无活体检测
            faceTrackManager.setAliving(false);
        } else if (liveType >= FaceConstant.LIVE_TYPE_RGB) { // 活体检测
            faceTrackManager.setAliving(true);
        }

        // 摄像头预览数据进行人脸检测
        faceTrackManager.faceTrack(faceSDKManager, data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                if (mIsShowPreviewView && !mIsFastRegister) {
                    // 检查人脸边界
                    checkFaceBound(livenessModel);
                } else {
                    // 检验活体分值
                    checkLiveScore(livenessModel);
                }
            }

            @Override
            public void onTip(final int code, final String msg) {
                if (code == 0) {
                    onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY, msg);
                } else {
                    onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED, msg);
                }
            }

            @Override
            public void onFaceDetectDrawCallback(LivenessModel livenessModel) {

            }
        });
    }

    /**
     * 检查人脸边界
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkFaceBound(final LivenessModel livenessModel) {

        // 当未检测到人脸UI显示
        if (mCollectSuccess) {
            return;
        }

        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
            onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY);
            return;
        }

        if (livenessModel.getFaceSize() > 1) {
            onTips(FaceConstant.CODE_MANY, FaceConstant.TIPS_MANY);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }


        mPointXY[0] = livenessModel.getFaceInfo().centerX;   // 人脸X坐标
        mPointXY[1] = livenessModel.getFaceInfo().centerY;   // 人脸Y坐标
        mPointXY[2] = livenessModel.getFaceInfo().width;     // 人脸宽度
        mPointXY[3] = livenessModel.getFaceInfo().height;    // 人脸高度

        FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, mCameraPreviewView,
                livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);

        float leftLimitX = mCameraPreviewView.circleX - mCameraPreviewView.circleRadius;
        float rightLimitX = mCameraPreviewView.circleX + mCameraPreviewView.circleRadius;
        float topLimitY = mCameraPreviewView.circleY - mCameraPreviewView.circleRadius;
        float bottomLimitY = mCameraPreviewView.circleY + mCameraPreviewView.circleRadius;
        float previewWidth = mCameraPreviewView.circleRadius * 2;

        if (mPointXY[2] < 50 || mPointXY[3] < 50) {
            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }

        if (mPointXY[2] > previewWidth || mPointXY[3] > previewWidth) {
            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }

        if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }
        // 检验活体分值
        checkLiveScore(livenessModel);
    }

    /**
     * 检验活体分值
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkLiveScore(LivenessModel livenessModel) {
        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
            onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY);
            return;
        }

        // 获取活体类型
        int liveType = SingleBaseConfig.getBaseConfig().getType();

        if (!livenessModel.isQualityCheck()) {
            onTips(FaceConstant.CODE_COVERED, FaceConstant.TIPS_COVERED);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        } else if (liveType == FaceConstant.LIVE_TYPE_NONE) { // 无活体
            // 提取特征值
            getFeatures(livenessModel);
        } else if (liveType >= FaceConstant.LIVE_TYPE_RGB) { // RGB活体检测
            float rgbLivenessScore = livenessModel.getRgbLivenessScore();
            float irLivenessScore = livenessModel.getIrLivenessScore();
            float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            float liveIrThreadHold = SingleBaseConfig.getBaseConfig().getNirLiveScore();
            if (rgbLivenessScore < liveThreadHold ||
                    (mHasNirCamera && (SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR)
                            && (irLivenessScore < liveIrThreadHold))) {
                onTips(FaceConstant.CODE_FAKE, FaceConstant.TIPS_FAKE);
                // 释放内存
                destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                return;
            }
            // 提取特征值
            getFeatures(livenessModel);
        }
    }

    /**
     * 提取特征值
     *
     * @param model 人脸数据
     */
    private void getFeatures(final LivenessModel model) {
        if (model == null) {
            return;
        }
        if (mCollectSuccess) {
            return;
        }
        if (mHasNirCamera && SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
            float ret = model.getFeatureCode();
            displayCompareResult(ret, model.getFeature(), model);
        } else {
            // 获取选择的特征抽取模型
            int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
            if (modelType == 1) {
                // 生活照
                faceSDKManager.onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                            @Override
                            public void onFaceFeatureCallBack(final float featureSize, final byte[] feature, long time) {
                                if (mCollectSuccess) {
                                    return;
                                }
                                displayCompareResult(featureSize, feature, model);
                            }
                        });
            }
        }
    }

    // 根据特征抽取的结果 注册人脸
    private void displayCompareResult(float ret, byte[] faceFeature, LivenessModel model) {
        if (model == null) {
            onTips(FaceConstant.CODE_NOBODY, FaceConstant.TIPS_NOBODY);
            return;
        }

        // 特征提取成功
        if (ret == 128) {
            // 抠图
            BDFaceImageInstance cropInstance;
            if (mHasNirCamera && SingleBaseConfig.getBaseConfig().getType() == FaceConstant.LIVE_TYPE_RGB_NIR) {
                cropInstance = model.getBdFaceImageInstanceCrop();
            } else {
                BDFaceImageInstance imageInstance = model.getBdFaceImageInstanceCrop();
                AtomicInteger isOutoBoundary = new AtomicInteger();
                cropInstance = faceSDKManager.getFaceCrop()
                        .cropFaceByLandmark(imageInstance, model.getLandmarks(),
                                2.0f, false, isOutoBoundary);
            }
            if (cropInstance == null) {
                onTips(FaceConstant.CODE_CROP_FACE_FAILED, FaceConstant.TIPS_CROP_FACE_FAILED);
                // 释放内存
                destroyImageInstance(model.getBdFaceImageInstanceCrop());
                return;
            }
            mCropBitmap = BitmapUtils.getInstaceBmp(cropInstance);
            cropInstance.destory();
            // 获取头像
            if (mCropBitmap != null) {
                for (int i = 0; i < faceFeature.length; i++) {
                    mFeatures[i] = faceFeature[i];
                }
            } else {
                onTips(FaceConstant.CODE_GET_FACE_FAILED, FaceConstant.TIPS_GET_FACE_FAILED);
                // 释放内存
                destroyImageInstance(model.getBdFaceImageInstanceCrop());
                return;
            }


            mCollectSuccess = true;
            // 保存人脸图片
            String faceId = UUID.randomUUID().toString();
            LogUtils.d(TAG, "faceId:" + faceId);

            // 判断是否已经注册
            ArrayList<Feature> featureResult =
                    faceSDKManager.getFaceFeature()
                            .featureSearch(faceFeature,
                                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                                    1, true);
            if (featureResult != null && featureResult.size() > 0) {
                // 获取第一个数据
                Feature topFeature = featureResult.get(0);
                // 判断第一个阈值是否大于设定阈值，如果大于，检索成功
                float threholdScore = 0;
                if (SingleBaseConfig.getBaseConfig().getActiveModel() == 1) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getLiveThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 2) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getIdThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 3) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getRgbAndNirThreshold();
                }
                if (topFeature != null && topFeature.getScore() >
                        threholdScore) {
                    // 当前featureEntity 只有id+feature 索引，在数据库中查到完整信息
                    User user = FaceApi.getInstance().getUserListById(topFeature.getId());
                    if (user != null) {
                        // 人脸已经注册过，返回注册过的userId和人脸特征值，及新的人脸截图
                        String faceImgPath = saveFacePic(faceId, FileUtils.getFaceCacheDir().getPath(), mCropBitmap);
                        String faceFeatureBase64 = EncodeUtils.base64Encode2String(user.getFeature());
                        LogUtils.d(TAG, "faceImgPath:" + faceImgPath);
                        LogUtils.d(TAG, "faceFeature:" + faceFeatureBase64);

                        onCancel();
                        onFaceRegisterResult(FaceConstant.CODE_ALREADY_REGISTER, FaceConstant.TIPS_ALREADY_REGISTER,
                                null, user.getUserId(), faceFeatureBase64, faceImgPath);
                        this.mLicenseNo = "";
                        // 释放内存
                        destroyImageInstance(model.getBdFaceImageInstanceCrop());
                        return;
                    }
                }
            }

            // 保存人脸数据到数据库
            String faceImgPath = saveFacePic(faceId, FileUtils.getFaceCacheDir().getPath(), mCropBitmap);
            String faceFeatureBase64 = EncodeUtils.base64Encode2String(mFeatures);
            saveFaceFeatureToDatabase(faceId, mFeatures);
            // 数据变化，更新内存
            faceSDKManager.initDataBases(mContext);
            LogUtils.d(TAG, "faceImgPath:" + faceImgPath);
            LogUtils.d(TAG, "faceFeature:" + faceFeatureBase64);

            onCancel();
            onFaceRegisterResult(FaceConstant.CODE_SUCCESS, FaceConstant.TIPS_REGISTER_SUCCESS,
                    null, faceId, faceFeatureBase64, faceImgPath);
            this.mLicenseNo = "";

            // 释放内存
            destroyImageInstance(model.getBdFaceImageInstanceCrop());
        } else {
            onTips(FaceConstant.CODE_GET_FACE_FEATURE_FAILED, FaceConstant.TIPS_GET_FACE_FEATURE_FAILED);
            // 释放内存
            destroyImageInstance(model.getBdFaceImageInstanceCrop());
        }
    }

    /**
     * 释放图像
     *
     * @param imageInstance
     */
    private void destroyImageInstance(BDFaceImageInstance imageInstance) {
        if (imageInstance != null) {
            imageInstance.destory();
        }
    }

    /**
     * 保存人脸数据到数据库
     */
    private boolean saveFaceFeatureToDatabase(String faceId, byte[] faceFeature) {
        // 注册到人脸库
        boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(faceId,
                faceId, faceId + ".jpg", null, faceFeature);
        if (!isSuccess) {
            LogUtils.d(TAG, "保存数据库失败，可能是用户名格式不正确");
        }
        return isSuccess;
    }

    /**
     * 保存人脸图片到本地
     */
    private String saveFacePic(String faceId, String facePath, Bitmap bitmap) {
        // 先清空文件夹
        FileUtils.cleanDir(facePath);
        // 保存人脸图片
        File faceDir = new File(facePath);
        if (!faceDir.exists()) {
            faceDir.mkdirs();
        }
        File file = new File(faceDir, faceId + ".jpg");
        String imagePath = file.getAbsolutePath();
        FileUtils.saveBitmap(file, bitmap);
        return imagePath;
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
            entity.setWhich(FaceConstant.FACE_REGISTER);
            tipsListener.onFaceTipsCallback(entity);
        }
    }

    private void onFaceRegisterResult(int code, String msg, String detail, String faceId, String faceFeature, String faceImgPath) {
        if (registerListener != null) {
            FaceRegisterEntity entity = new FaceRegisterEntity();
            entity.setShowPreviewView(mIsShowPreviewView);
            entity.setCode(code);
            entity.setMsg(msg);
            entity.setDetail(detail);
            entity.setLicenseNo(mLicenseNo);
            entity.setFaceId(faceId);
            entity.setFaceFeature(faceFeature);
            entity.setFaceImgPath(faceImgPath);
            entity.setUseType(mUseType);
            entity.setTag(mTag);
            registerListener.onFaceRegisterCallback(entity);
        }
    }
}
