package com.mf.face.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.mf.face.callback.CameraDataCallback;
import com.mf.face.model.SingleBaseConfig;
import com.mf.log.LogUtils;

import java.util.List;

/**
 * Time: 2019/1/24
 * Author: v_chaixiaogang
 * Description:
 */
public class CameraPreviewManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = "face";


    AutoTexturePreviewView mAutoTexturePreviewView;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;

    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_USB = 2;

    public static final int CAMERA_ORBBEC = 3;

    /**
     * 垂直方向
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    public static final int ORIENTATION_HORIZONTAL = 1;

    /**
     * 当前相机的ID。
     */
    private int cameraFacing = CAMERA_FACING_FRONT;

    private int previewWidth;
    private int previewHeight;

    private int videoWidth;
    private int videoHeight;

    private int tempWidth;
    private int tempHeight;

    private int textureWidth;
    private int textureHeight;

    private Camera mCamera;
    private int mCameraNum;

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int mirror = 1; // 镜像处理
    private boolean isNir = false; // 是否是红外摄像头

    private CameraDataCallback mCameraDataCallback;
    private static volatile CameraPreviewManager instance = null;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public CameraPreviewManager() {
    }

    private static CameraPreviewManager getInstance() {
        synchronized (CameraPreviewManager.class) {
            if (instance == null) {
                instance = new CameraPreviewManager();
            }
        }
        return instance;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public boolean isNir() {
        return isNir;
    }

    public void setNir(boolean nir) {
        isNir = nir;
    }

    /**
     * 开启预览
     */
    public void startPreview(Context context, AutoTexturePreviewView autoTexturePreviewView, int width,
                             int height, CameraDataCallback cameraDataCallback) {
        LogUtils.w(TAG, "开启预览模式 isNir:" + isNir);
        Context mContext = context;
        this.mCameraDataCallback = cameraDataCallback;
        this.mAutoTexturePreviewView = autoTexturePreviewView;
        this.previewWidth = width;
        this.previewHeight = height;

        if (mAutoTexturePreviewView != null) {
            LogUtils.w(TAG, "mAutoTexturePreviewView != null");
            mSurfaceTexture = mAutoTexturePreviewView.getTextureView().getSurfaceTexture();
            mAutoTexturePreviewView.getTextureView().setSurfaceTextureListener(this);
            if (mSurfaceTexture != null && mAutoTexturePreviewView.getTextureView().isAttachedToWindow()) {
                LogUtils.w(TAG, "mSurfaceTexture != null && mAutoTexturePreviewView.getTextureView().isAttachedToWindow()");
                onSurfaceTextureAvailable(mSurfaceTexture, mAutoTexturePreviewView.getWidth(), mAutoTexturePreviewView.getHeight());
            }
        } else {
            LogUtils.w(TAG, "mAutoTexturePreviewView is null");
            mSurfaceTexture = new SurfaceTexture(1);
            onSurfaceTextureAvailable(mSurfaceTexture, 0, 0);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        LogUtils.w(TAG, "--surfaceTexture--SurfaceTextureAvailable texture:" + texture);
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        textureWidth = width;
        textureHeight = height;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        LogUtils.w(TAG, "--surfaceTexture--TextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        LogUtils.w(TAG, "--surfaceTexture--destroyed");
        mSurfaceCreated = false;
        if (mCamera != null) {
            if (mAutoTexturePreviewView != null) {
                mAutoTexturePreviewView.getTextureView().setSurfaceTextureListener(null);
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
//        LogUtils.e(TAG, "--surfaceTexture--Updated");
    }


    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            try {
                if (mAutoTexturePreviewView != null) {
                    mAutoTexturePreviewView.getTextureView().setSurfaceTextureListener(null);
                }
                LogUtils.w(TAG, "camera stopPreview isNir:" + isNir);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
//                mCamera.setPreviewTexture(null);
                mSurfaceCreated = false;
                mAutoTexturePreviewView = null;
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                LogUtils.e(TAG, "camera destory error");
                e.printStackTrace();
            }
        }
    }


    /**
     * 开启摄像头
     */

    private void openCamera() {

        try {
            if (mCamera == null) {
//                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
//                    Camera.getCameraInfo(i, cameraInfo);
//                    if (cameraInfo.facing == cameraFacing) {
//                        cameraId = i;
//                    }
//                }
                cameraId = cameraFacing;
                mCamera = Camera.open(cameraId);
                LogUtils.w(TAG, "initCamera---open camera");
            }

            // 摄像头图像预览角度
            int cameraRotation;
            int isRgbRevert;
            if (!isNir) {
                cameraRotation = SingleBaseConfig.getBaseConfig().getRgbVideoDirection();
                isRgbRevert = SingleBaseConfig.getBaseConfig().getMirrorVideoRGB();
            } else {
                cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                isRgbRevert = SingleBaseConfig.getBaseConfig().getMirrorVideoNIR();
            }
            switch (cameraFacing) {
                case CAMERA_FACING_FRONT: {
//                    cameraRotation = ORIENTATIONS.get(displayOrientation);
//                    cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId);
                    mCamera.setDisplayOrientation(cameraRotation);
                    break;
                }

                case CAMERA_FACING_BACK: {
//                    cameraRotation = ORIENTATIONS.get(displayOrientation);
//                    cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId);
                    mCamera.setDisplayOrientation(cameraRotation);
                    break;
                }

                case CAMERA_USB: {
                    mCamera.setDisplayOrientation(cameraRotation);
                    break;
                }

                default:
                    break;

            }

            if (mAutoTexturePreviewView != null) {
                if (cameraRotation == 90 || cameraRotation == 270) {
                    if (isRgbRevert == 1) {
                        mAutoTexturePreviewView.setRotationY(180);
                    } else {
                        mAutoTexturePreviewView.setRotationY(0);
                    }
                    // 旋转90度或者270，需要调整宽高
                    mAutoTexturePreviewView.setPreviewSize(previewHeight, previewWidth);
                } else {
                    if (isRgbRevert == 1) {
                        mAutoTexturePreviewView.setRotationY(180);
                    } else {
                        mAutoTexturePreviewView.setRotationY(0);
                    }
                    mAutoTexturePreviewView.setPreviewSize(previewWidth, previewHeight);
                }
            }

            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, previewWidth,
                    previewHeight); // 获取一个最为适配的camera.size
            if (optionSize.width == previewWidth && optionSize.height == previewHeight) {
                videoWidth = previewWidth;
                videoHeight = previewHeight;
            } else {
                videoWidth = optionSize.width;
                videoHeight = optionSize.height;
            }
            params.setPreviewSize(videoWidth, videoHeight);

            mCamera.setParameters(params);
            try {
                LogUtils.w(TAG, "initCamera---setPreviewTexture mSurfaceTexture:" + mSurfaceTexture);
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(bytes, camera,
                                    videoWidth, videoHeight);
                        }
                    }
                });
                mCamera.startPreview();

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.getMessage());
            }
        } catch (RuntimeException e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }


    private int getCameraDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }


    /**
     * 解决预览变形问题
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
