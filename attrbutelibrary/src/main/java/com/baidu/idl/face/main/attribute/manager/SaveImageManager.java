package com.baidu.idl.face.main.attribute.manager;

import android.graphics.Bitmap;

import com.baidu.idl.face.main.attribute.model.LivenessModel;
import com.mf.face.model.SingleBaseConfig;
import com.baidu.idl.face.main.attribute.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;

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

    private ExecutorService es3 = Executors.newSingleThreadExecutor();
    private Future future3;
    public void saveImage(final LivenessModel livenessModel){
        // 检测结果输出
        if (future3 != null && !future3.isDone()) {
            return;
        }

        future3 = es3.submit(new Runnable() {
            @Override
            public void run() {
                String currentTime = System.currentTimeMillis() + "";
                BDFaceImageInstance rgbImage = livenessModel.getBdFaceImageInstance();
                if (rgbImage != null){
                    Bitmap bitmap = BitmapUtils.getInstaceBmp(rgbImage);
                    if (livenessModel.getRgbLivenessScore() > SingleBaseConfig.getBaseConfig().getRgbLiveScore()){
                        saveImage(bitmap, "Save-Image" + "/" + currentTime,
                                currentTime + "_RGB_Feature");
                    }else {

                        saveImage(bitmap, "Save-Image" + "/" + currentTime,
                                currentTime + "_RGB_Live");
                    }
                    if (!bitmap.isRecycled()){
                        bitmap.recycle();
                    }
                    rgbImage.destory();
                }
            }
        });
    }
    private void saveImage(Bitmap bitmap, String url, String name) {
        BitmapUtils.saveRgbBitmap(bitmap, url, name);
    }
}
