package com.baidu.idl.face.main.attribute.activity.attribute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.attribute.activity.AttributeBaseActivity;
import com.baidu.idl.face.main.attribute.callback.CameraDataCallback;
import com.baidu.idl.face.main.attribute.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.attribute.camera.AttrbuteAutoTexturePreviewView;
import com.baidu.idl.face.main.attribute.camera.CameraPreviewManager;
import com.baidu.idl.face.main.attribute.listener.SdkInitListener;
import com.baidu.idl.face.main.attribute.manager.FaceSDKManager;
import com.baidu.idl.face.main.attribute.manager.SaveImageManager;
import com.baidu.idl.face.main.attribute.model.LivenessModel;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.mf.face.model.SingleBaseConfig;
import com.baidu.idl.face.main.attribute.setting.AttributeSettingActivity;
import com.baidu.idl.face.main.attribute.utils.BitmapUtils;
import com.baidu.idl.face.main.attribute.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.attribute.utils.ToastUtils;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.attrbutelibrary.R;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.mf.face.utils.ConfigUtils;
import com.mf.face.view.PreviewTexture;

/**
 * author : shangrong
 * date : 2020-02-11 10:30
 * description :
 */
public class FaceAttributeRgbActivity extends AttributeBaseActivity {
    private AttrbuteAutoTexturePreviewView autoTexturePreviewView;
    // RGB摄像头图像宽和高
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private ImageView faceAttribute;
    private Bitmap roundBitmap;
    private RelativeLayout showAtrMessage;
    private ImageView btn_back;
    private Paint paint;
    private Paint paintBg;
    private RectF rectF;
    private TextureView mDrawDetectFaceView;
    private TextView atrDetectTime;
    private TextView atrToalTime;
    private TextView atrSex;
    private TextView atrAge;
    private TextView atrAccessory;
    private TextView atrEmotion;
    private TextView atrMask;
    private RelativeLayout atrRlDisplay;
    private RelativeLayout atrLinerTime;
    private TextView previewText;
    private TextView developText;
    private TextView homeBaiduTv;
    private ImageView btnSetting;
    private ImageView previewView;
    private ImageView developView;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_face_rgb_attribute);

        SingleBaseConfig.getBaseConfig().setAttribute(true);
        FaceSDKManager.getInstance().initConfig();
        FaceSDKManager.isDetectMask = true;
        initConfig(this);
        initRGBCheck(this);
        init();
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    FaceSDKManager.initModelSuccess = true;
                    ToastUtils.toast(FaceAttributeRgbActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = true;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceAttributeRgbActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    public void init() {
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        homeBaiduTv = findViewById(R.id.home_baiduTv);
        previewView = findViewById(R.id.preview_view);
        developView = findViewById(R.id.develop_view);
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        showAtrMessage = findViewById(R.id.showAtrMessage);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }
        btn_back = findViewById(R.id.btn_back);
        autoTexturePreviewView = findViewById(R.id.fa_auto);
        faceAttribute = findViewById(R.id.face_attribute);
        atrDetectTime = findViewById(R.id.atrDetectTime);
        atrToalTime = findViewById(R.id.atrToalTime);
        atrSex = findViewById(R.id.atrSex);
        atrAge = findViewById(R.id.atrAge);
        atrAccessory = findViewById(R.id.atrAccessory);
        atrEmotion = findViewById(R.id.atrEmotion);
        atrMask = findViewById(R.id.atrMask);
        atrRlDisplay = findViewById(R.id.atrRlDisplay);
        atrLinerTime = findViewById(R.id.atrLinerTime);
        previewText = findViewById(R.id.preview_text);
        developText = findViewById(R.id.develop_text);
        btnSetting = findViewById(R.id.btn_setting);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSaveImage = !isSaveImage;
                if (isSaveImage){
                    ToastUtils.toast(FaceAttributeRgbActivity.this, "存图功能已开启再次点击可关闭");
                    spot.setVisibility(View.VISIBLE);
                }else {
                    spot.setVisibility(View.GONE);
                }
            }
        });
        spot = findViewById(R.id.spot);

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(FaceAttributeRgbActivity.this, "SDK正在加载模型，请稍后再试",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(FaceAttributeRgbActivity.this, AttributeSettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
        previewText.setTextColor(Color.parseColor("#FFFFFF"));

        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeBaiduTv.setVisibility(View.VISIBLE);
                atrRlDisplay.setVisibility(View.GONE);
                atrLinerTime.setVisibility(View.GONE);
                previewText.setTextColor(Color.parseColor("#FFFFFF"));
                developText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.VISIBLE);
                developView.setVisibility(View.GONE);
                saveCamera.setVisibility(View.GONE);
                isSaveImage = false;
                spot.setVisibility(View.GONE);
            }
        });

        developText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeBaiduTv.setVisibility(View.GONE);
                atrRlDisplay.setVisibility(View.VISIBLE);
                atrLinerTime.setVisibility(View.VISIBLE);
                developText.setTextColor(Color.parseColor("#FFFFFF"));
                previewText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.GONE);
                developView.setVisibility(View.VISIBLE);
                saveCamera.setVisibility(View.VISIBLE);
                judgeFirst();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(FaceAttributeRgbActivity.this, "SDK正在加载模型，请稍后再试",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }
        });
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isAttrbuteFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isAttrbuteFirstSave", false);
            editor.commit();
        }
    }
    private void setFirstView(int visibility){
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreview();
    }


    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        CameraPreviewManager.getInstance().startPreview(this, autoTexturePreviewView,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        dealRgb(rgbData);
                        showDetectImage(rgbData);
                    }
                });
    }


    private void dealRgb(byte[] rgbData) {
        if (rgbData != null) {
            FaceSDKManager.getInstance().onAttrDetectCheck(rgbData, null, null, RGB_HEIGHT,
                    RGB_WIDTH, 1, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 检测结果输出
                            if (livenessModel != null) {
                                showAtrDetailMessage(livenessModel.getFaceInfo(), livenessModel.getMaskScore());
                            }
                            showResult(livenessModel);
                            if (isSaveImage){
                                SaveImageManager.getInstance().saveImage(livenessModel);
                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {

                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
        }
    }

//    public String getMsg(FaceInfo faceInfo) {
//        StringBuilder msg = new StringBuilder();
//        if (faceInfo != null) {
//            msg.append(faceInfo.age);
//            msg.append(",").append(faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_CALM ?
//                    "平静"
//                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_SMILE ? "笑"
//                    : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_FROWN ? "皱眉" : "没有表情");
//            msg.append(",").append(faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_FEMALE ? "女性" :
//                    faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_MALE ? "男性" : "婴儿");
//            msg.append(",").append(faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES ? "无眼镜"
//                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_GLASSES ? "有眼镜"
//                    : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_SUN_GLASSES ? "墨镜" : "太阳镜");
//            msg.append(",").append(faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_YELLOW ? "黄种人"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_WHITE ? "白种人"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_BLACK ? "黑种人"
//                    : faceInfo.race == BDFaceSDKCommon.BDFaceRace.BDFACE_RACE_INDIAN ? "印度人"
//                    : "地球人");
//        }
//        return msg.toString();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSDKManager.isDetectMask = false;
        CameraPreviewManager.getInstance().stopPreview();
        // 关闭属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(false);
        FaceSDKManager.getInstance().initConfig();
    }

    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        roundBitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                faceAttribute.setVisibility(View.VISIBLE);
                faceAttribute.setImageBitmap(roundBitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }

//    // bitmap转成圆角
//    private Bitmap bimapRound(Bitmap mBitmap, float index) {
//        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
//
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//
//        // 设置矩形大小
//        Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
//        RectF rectf = new RectF(rect);
//
//        // 相当于清屏
//        canvas.drawARGB(0, 0, 0, 0);
//        // 画圆角
//        canvas.drawRoundRect(rectf, index, index, paint);
//        // 取两层绘制，显示上层
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//
//        // 把原生的图片放到这个画布上，使之带有画布的效果
//        canvas.drawBitmap(mBitmap, rect, rect, paint);
//        return bitmap;
//    }

    /**
     * 绘制人脸框
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    showArtLinerGone();
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    showArtLinerGone();
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    showArtLinerGone();
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        autoTexturePreviewView, model.getBdFaceImageInstance());

                paint.setColor(Color.parseColor("#00baf2"));
                paintBg.setColor(Color.parseColor("#00baf2"));

                paint.setStyle(Paint.Style.FILL);
                paintBg.setStyle(Paint.Style.FILL);
                // 画笔粗细
                paint.setStrokeWidth(8);
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAntiAlias(true);
                paintBg.setAlpha(90);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (showAtrMessage.getVisibility() == View.GONE) {
                            showAtrMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });

                if (faceInfo.width > faceInfo.height) {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.height() / 2);
                        } else {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    } else {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth()
                                    - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    }
                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    } else {

                        FaceOnDrawTexturViewUtil.drawRect(canvas,
                                rectF, paint, 5f, 50f , 25f);
                        if (rectF.centerY() < autoTexturePreviewView.getHeight() * 0.6) {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() + rectF.width() / 2);
                        } else {
                            showAtrMessage.setTranslationX(autoTexturePreviewView.getWidth() - rectF.centerX());
                            showAtrMessage.setTranslationY(rectF.centerY() -
                                    rectF.width() - showAtrMessage.getHeight());
                        }
                    }
                }

                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    public void showResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel != null) {
                    atrDetectTime.setText("检测耗时：" + livenessModel.getRgbDetectDuration() + "ms");
                    atrToalTime.setText("总耗时：" + livenessModel.getAllDetectDuration() + "ms");
                } else {
                    atrDetectTime.setText("检测耗时：" + 0 + "ms");
                    atrToalTime.setText("总耗时：" + 0 + "ms");
                }
            }
        });
    }

    public void showAtrDetailMessage(final FaceInfo faceInfo, final float maskScore) {
        if (faceInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String sex = faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_FEMALE ? "女" :
                            faceInfo.gender == BDFaceSDKCommon.BDFaceGender.BDFACE_GENDER_MALE ? "男" : "婴儿";
//                    String accessory = faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES ? "否"
//                            : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_GLASSES ? "是"
//                            : faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_SUN_GLASSES ? "墨镜" : "太阳镜";
                    String emotion = faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_CALM ?
                            "平静"
                            : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion.BDFACE_EMOTION_SMILE ? "笑"
                            : faceInfo.emotionThree == BDFaceSDKCommon.BDFaceEmotion
                            .BDFACE_EMOTION_FROWN ? "皱眉" : "没有表情";

                    atrSex.setText("性别：" + sex);
                    atrAge.setText("年龄：" + faceInfo.age + "岁");
                    if (faceInfo.glasses == BDFaceSDKCommon.BDFaceGlasses.BDFACE_NO_GLASSES) {
                        atrAccessory.setText("眼镜：否");
                    } else {
                        atrAccessory.setText("眼镜：是");
                    }
//                    atrAccessory.setText("眼镜：" + accessory);
                    atrEmotion.setText("表情：" + emotion);
                    if (maskScore > 0.9) {
                        atrMask.setText("口罩：" + "是");
                    } else {
                        atrMask.setText("口罩：" + "否");
                    }
                }
            });

        }
    }

    public void showArtLinerGone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAtrMessage.setVisibility(View.GONE);
            }
        });
    }


    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;

    private void initRGBCheck(Context context) {
        if (isSetCameraId()) {
            return;
        }
        int mCameraNum = Camera.getNumberOfCameras();
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

    private void initConfig(Context context) {
        boolean isConfigExit = ConfigUtils.createConfig(context);
        boolean isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
            Toast.makeText(context, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            ConfigUtils.modityJson();
        }
    }
}
