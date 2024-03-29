package com.mf.face.manager;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.mf.face.callback.FaceDetectCallBack;
import com.mf.face.model.LivenessModel;
import com.mf.face.model.SingleBaseConfig;
import com.mf.log.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Time: 2019/1/25
 * Author: v_chaixiaogang
 * rgb单目检测管理类
 */
public class FaceTrackManager {
    private static final String TAG = "face";

    private static volatile FaceTrackManager instance = null;

    private ExecutorService faceTrackExecutorService;
    private Future faceTrackFuture;
    // 活体检测
    private boolean isAliving;

    private volatile int mProcessCount = 0;


    public boolean isAliving() {
        return isAliving;
    }

    public void setAliving(boolean aliving) {
        isAliving = aliving;
    }

    private FaceTrackManager() {
        faceTrackExecutorService = Executors.newSingleThreadExecutor();
    }

    public static FaceTrackManager getInstance() {
        synchronized (FaceTrackManager.class) {
            if (instance == null) {
                instance = new FaceTrackManager();
            }
        }
        return instance;
    }

    public void faceTrack(final FaceSDKManager faceSDKManager, final byte[] argb, final int width, final int height,
                          final FaceDetectCallBack faceDetectCallBack) {

        if (faceTrackFuture != null && !faceTrackFuture.isDone()) {
            return;
        }

        if (mProcessCount > 0) {
            return;
        }
        mProcessCount++;

        faceTrackFuture = faceTrackExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                faceDataDetect(faceSDKManager, argb, width, height, faceDetectCallBack);
                mProcessCount--;
            }
        });
    }

    /**
     * 人脸检测
     *
     * @param argb
     * @param width
     * @param height
     * @param faceDetectCallBack
     */
    private void faceDataDetect(FaceSDKManager faceSDKManager, final byte[] argb, int width, int height, FaceDetectCallBack faceDetectCallBack) {
        LivenessModel livenessModel = new LivenessModel();

        BDFaceImageInstance rgbInstance;
        if (SingleBaseConfig.getBaseConfig().getType() == 4
                && SingleBaseConfig.getBaseConfig().getCameraType() == 6) {
            rgbInstance = new BDFaceImageInstance(argb, height, width,
                    BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                    SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                    SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
        } else {
            rgbInstance = new BDFaceImageInstance(argb, height, width,
                    BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                    SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                    SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
        }

        // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
        livenessModel.setBdFaceImageInstanceCrop(rgbInstance);

        // 检查函数调用，返回检测结果
        long startTime = System.currentTimeMillis();

        // 快速检测获取人脸信息，仅用于绘制人脸框，详细人脸数据后续获取
        FaceInfo[] faceInfos = faceSDKManager.getFaceDetect()
                .track(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);

        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startTime);

        // getImage() 获取送检图片
        livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

        if (faceInfos != null && faceInfos.length > 0) {
            livenessModel.setFaceSize(faceInfos.length);
            livenessModel.setTrackFaceInfo(faceInfos);
            FaceInfo faceInfo = faceInfos[0];
            livenessModel.setFaceInfo(faceInfo);
            livenessModel.setLandmarks(faceInfo.landmarks);
            LogUtils.w(TAG, "ldmk = " + faceInfo.landmarks.length);

            // 质量检测，针对模糊度、遮挡、角度
            if (onQualityCheck(livenessModel, faceDetectCallBack)) {
                livenessModel.setQualityCheck(true);
                // 活体检测
                if (isAliving) {
                    startTime = System.currentTimeMillis();
                    float rgbScore = faceSDKManager.getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                            rgbInstance, faceInfo.landmarks);
                    LogUtils.w(TAG, "score = " + rgbScore);
                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startTime);
                }
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                // rgbInstance.destory();
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                }
            } else {
                livenessModel.setQualityCheck(false);
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
            }

        } else {
            // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
            rgbInstance.destory();
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onTip(0, "未检测到人脸");
                faceDetectCallBack.onFaceDetectCallback(null);
                faceDetectCallBack.onFaceDetectDrawCallback(null);
            }
        }
    }


    /**
     * 质量检测
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {

            // 角度过滤
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                faceDetectCallBack.onTip(-1, "人脸左右偏转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                faceDetectCallBack.onTip(-1, "人脸平行平面内的头部旋转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                faceDetectCallBack.onTip(-1, "人脸上下偏转角超出限制");
                return false;
            }


            // 模糊结果过滤
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "图片模糊");
                return false;
            }

            // 光照结果过滤
            float illum = livenessModel.getFaceInfo().illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "图片光照不通过");
                return false;
            }

            // 遮挡结果过滤
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    faceDetectCallBack.onTip(-1, "鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}
