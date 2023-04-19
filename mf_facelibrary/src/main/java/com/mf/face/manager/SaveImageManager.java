package com.mf.face.manager;

import android.graphics.Bitmap;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.mf.face.model.LivenessModel;
import com.mf.face.model.FaceConstant;
import com.mf.face.model.SingleBaseConfig;
import com.mf.face.utils.BitmapUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SaveImageManager {
    private static class HolderClass {
        private static final SaveImageManager SAVE_IMAGE_MANAGER = new SaveImageManager();
    }

    public static SaveImageManager getInstance() {
        return HolderClass.SAVE_IMAGE_MANAGER;
    }

    private ExecutorService saveImageExecutorService = Executors.newSingleThreadExecutor();
    private Future saveImageFuture;

    public void saveImage(final LivenessModel livenessModel) {
        // 检测结果输出
        if (saveImageFuture != null && !saveImageFuture.isDone()) {
            return;
        }

        saveImageFuture = saveImageExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                String currentTime = System.currentTimeMillis() + "";
                BDFaceImageInstance rgbImage = livenessModel.getBdFaceImageInstance();
                BDFaceImageInstance nirImage = livenessModel.getBdNirFaceImageInstance();
                BDFaceImageInstance depthImage = livenessModel.getBdDepthFaceImageInstance();
                if (rgbImage != null) {
                    Bitmap bitmap = BitmapUtils.getInstaceBmp(rgbImage);
                    if (livenessModel.getRgbLivenessScore() > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_RGB_Feature");
                    } else {

                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_RGB_Live");
                    }
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    rgbImage.destory();
                }
                if (nirImage != null) {
                    Bitmap bitmap = BitmapUtils.getInstaceBmp(nirImage);
                    if (livenessModel.getIrLivenessScore() > SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_NIR_Feature");
                    } else {
                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_NIR_Live");
                    }
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    nirImage.destory();
                }
                if (depthImage != null) {
                    Bitmap bitmap = BitmapUtils.getInstaceBmp(depthImage);
                    if (livenessModel.getDepthLivenessScore() > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_Depth_Feature");
                    } else {
                        saveImage(bitmap, FaceConstant.FaceSaveImageDir + "/" + currentTime,
                                currentTime + "_Depth_Live");
                    }
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    depthImage.destory();
                }

            }
        });
    }

    private void saveImage(Bitmap bitmap, String url, String name) {
        BitmapUtils.saveRgbBitmap(bitmap, url, name);
    }
}
