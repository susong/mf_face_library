package com.mf.face.helper;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.mf.face.R;
import com.mf.face.camera.AutoTexturePreviewView;
import com.mf.face.model.LivenessModel;
import com.mf.face.model.SingleBaseConfig;
import com.mf.face.utils.DensityUtils;
import com.mf.face.utils.FaceOnDrawTexturViewUtil;
import com.mf.face.view.CircleTransparentView;
import com.mf.log.LogUtils;

public class FaceViewHelper implements View.OnClickListener {

    private static final String TAG = "face";

    private Context mContext;
    private boolean isRegister = false;// true:人脸注册，false：人脸识别
    private View mView;
    private View mRootView;
    private Button mBtnCancel;
    private Button mBtnPickCarByPlate;
    private Button mBtnPickCarByBerth;
    private TextView mtvTips;
    private CircleTransparentView mCircleTransparentView;
    private ImageView mIvFaceMask;
    // 摄像头预览界面
    private AutoTexturePreviewView mRgbCameraPreviewView;
    private AutoTexturePreviewView mNirCameraPreviewView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    // 画人脸框
    private TextureView mDrawDetectFaceView;
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    private int x = -1;
    private int y = -1;
    private int width = -1;
    private int height = -1;

    private OnCancelClickListener onCancelClickListener;
    private OnPickCarByPlateClickListener onPickCarByPlateClickListener;
    private OnPickCarByBerthClickListener onPickCarByBerthClickListener;

    public FaceViewHelper() {
    }

    public FaceViewHelper setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public FaceViewHelper setRegister(boolean register) {
        isRegister = register;
        return this;
    }

    public FaceViewHelper setOnCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
        return this;
    }

    public FaceViewHelper setOnPickCarByPlateClickListener(OnPickCarByPlateClickListener listener) {
        this.onPickCarByPlateClickListener = listener;
        return this;
    }

    public FaceViewHelper setOnPickCarByBerthClickListener(OnPickCarByBerthClickListener listener) {
        this.onPickCarByBerthClickListener = listener;
        return this;
    }

    public AutoTexturePreviewView getRgbCameraPreviewView() {
        return mRgbCameraPreviewView;
    }

    public AutoTexturePreviewView getNirCameraPreviewView() {
        return mNirCameraPreviewView;
    }

    /**
     * 手动设置view，可用于给Flutter显示用
     *
     * @param view
     */
    public void setView(View view) {
        this.mView = view;
        initView(this.mView);
    }

    /**
     * 自动加载预定view
     */
    public void loadView() {
        if (mView == null) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.face_view, null, false);
            initView(mView);
            setViewLayoutParams();
        }
    }

    public void loadView(int resource) {
        if (mView == null) {
            mView = LayoutInflater.from(mContext).inflate(resource, null, false);
            initView(mView);
            setViewLayoutParams();
        }
    }

    private void initView(View view) {
        // 获取整个布局
        mRootView = view.findViewById(R.id.root_view);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = view.findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            mDrawDetectFaceView.setRotationY(180);
        }
        mtvTips = view.findViewById(R.id.tv_tips);
        // 单目摄像头RGB 图像预览
        mRgbCameraPreviewView = view.findViewById(R.id.rgb_camera_preview_view);
        // 双目摄像头IR 图像预览
        mNirCameraPreviewView = view.findViewById(R.id.nir_camera_preview_view);
        mCircleTransparentView = view.findViewById(R.id.circle_transparent_view);
        mIvFaceMask = view.findViewById(R.id.iv_face_mask);
//        mBtnCancel = view.findViewById(R.id.btn_cancel);
//        mBtnPickCarByPlate = view.findViewById(R.id.btn_pick_car_by_plate);
//        mBtnPickCarByBerth = view.findViewById(R.id.btn_pick_car_by_berth);
        if (mBtnCancel != null) {
            mBtnCancel.setOnClickListener(this);
        }
        if (mBtnPickCarByPlate != null) {
            mBtnPickCarByPlate.setOnClickListener(this);
        }
        if (mBtnPickCarByBerth != null) {
            mBtnPickCarByBerth.setOnClickListener(this);
        }
        if (isRegister) {
            if (mBtnCancel != null) {
                mBtnCancel.setText("跳过");
            }
        } else {
            if (mBtnCancel != null) {
                mBtnCancel.setText("");
            }
        }

        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的高小于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            mRootView.setLayoutParams(params);
        }
    }

    private void setViewLayoutParams() {
        int width = ScreenUtils.getScreenWidth();
        int height = ScreenUtils.getScreenHeight();

        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 设置悬浮窗的大小，位置，是否可点击
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.width = (int) (width * (0.85f));
        mLayoutParams.height = (int) (width * (0.85f));
        mLayoutParams.type = layoutType;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // FLAG_LAYOUT_IN_SCREEN / FLAG_LAYOUT_INSET_DECOR：要一起使用；前者设置后窗口布局在StableFullScreen区域进行，但结果不一定是全屏；后者设置后可以避免此窗口与系统UI发生冲突。
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // 允许window扩展值屏幕之外
                | WindowManager.LayoutParams.FLAG_FULLSCREEN // 设置后窗口是全屏的
                | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER // 设置后此窗口的下面是壁纸
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE // 设置后，不管触摸事件是否在这个窗口范围内，都不会被这个窗口消费，也不会传递到下个窗口。
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL // 设置后，在窗口范围外的触摸事件会传递到下个窗口，如果还设置了FLAG_WATCH_OUTSIDE_TOUCH，那么还会在第一次DOWN时收到一个MotionEvent.OUT_SIDE事件。
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 设置后输入事件会略过这个窗口，传递到下个窗口；窗口范围内的触摸事件失效，范围外的会传递到下个窗口；这个窗口会置于软键盘之上，如果又设置FLAG_ALT_FOCUSABLE_IM则不会。
        ;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.BOTTOM;
        mLayoutParams.x = 0;
        mLayoutParams.y = DensityUtils.dip2px(mContext, 350);
    }

    private void setPosition() {
        if (width >= 0) {
            mLayoutParams.width = DensityUtils.dip2px(mContext, width);
        }
        if (height >= 0) {
            mLayoutParams.height = DensityUtils.dip2px(mContext, height);
        }
        if (x >= 0) {
            mLayoutParams.x = DensityUtils.dip2px(mContext, x);
        }
        if (y >= 0) {
            mLayoutParams.y = DensityUtils.dip2px(mContext, y);
        }
    }

    private void addView(View view) {
        if (view == null) {
            LogUtils.d(TAG, "view is null");
            return;
        }
        if (!view.isAttachedToWindow()) {
            mWindowManager.addView(view, mLayoutParams);
        }
        if (mtvTips != null) {
            mtvTips.setText("请将人脸对准上方摄像头");
        }
    }

    private void removeView(View view) {
        if (view == null) {
            return;
        }
        if (view.isAttachedToWindow()) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            windowManager.removeView(view);
        }
    }

    public void showView() {
        ThreadUtils.runOnUiThread(() -> addView(mView));
    }

    public void showView(int x, int y) {
        this.x = x;
        this.y = y;
        setPosition();
        showView();
    }

    public void showView(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        setPosition();
        showView();
    }

    public void hideView() {
        ThreadUtils.runOnUiThread(() -> removeView(mView));
    }

    public void setBtnCancelText(String text) {
        ThreadUtils.runOnUiThread(() -> {
            if (mBtnCancel != null) {
                mBtnCancel.setText(text);
            }
        });
    }

    public void setTips(String tips) {
        ThreadUtils.runOnUiThread(() -> {
            if (mtvTips != null) {
                mtvTips.setText(tips);
            }
        });
    }

    public void showMask(boolean isShow) {
        ThreadUtils.runOnUiThread(() -> {
            if (mCircleTransparentView != null) {
                mCircleTransparentView.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
            if (mIvFaceMask != null) {
//                mIvFaceMask.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * 绘制人脸框
     */
    public void showFrame(final LivenessModel model) {
        mView.post(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));

                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mRgbCameraPreviewView, model.getBdFaceImageInstance());
                // 人脸框颜色
                FaceOnDrawTexturViewUtil.drawFaceColor(model.getUser(), paint, paintBg, model);
                // 绘制人脸框
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }


    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_cancel) {
//            if (onCancelClickListener != null) {
//                onCancelClickListener.onCancelClick();
//            }
//        } else if (v.getId() == R.id.btn_pick_car_by_plate) {
//            if (onPickCarByPlateClickListener != null) {
//                onPickCarByPlateClickListener.onPickCarByPlateClick();
//            }
//        } else if (v.getId() == R.id.btn_pick_car_by_berth) {
//            if (onPickCarByBerthClickListener != null) {
//                onPickCarByBerthClickListener.onPickCarByBerthClick();
//            }
//        } else {
//            LogUtils.w(TAG, "unknown view click");
//        }
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public interface OnPickCarByPlateClickListener {
        void onPickCarByPlateClick();
    }

    public interface OnPickCarByBerthClickListener {
        void onPickCarByBerthClick();
    }
}
