/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.mf.face.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

/**
 * 基于 系统TextureView实现的预览View。
 *
 * @Time: 2019/1/28
 * @Author: v_chaixiaogang
 */
public class AutoTexturePreviewView extends FrameLayout {

    private static final String TAG = "face";

    public TextureView textureView;

    private int videoWidth = 0;
    private int videoHeight = 0;


    public int previewWidth = 0;
    private int previewHeight = 0;
    private int scale = 2;

    public float circleRadius;
    public float circleX;
    public float circleY;
    private Path path;
    private boolean isRegister = false;   // 注册

    public AutoTexturePreviewView(Context context) {
        super(context);
        init();
    }

    public AutoTexturePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoTexturePreviewView(Context context, AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private void init() {
        path = new Path();
        setWillNotDraw(false);
        textureView = new TextureView(getContext());
        addView(textureView);
    }

    public void setIsRegister(boolean isRegister) {
//        this.isRegister = isRegister;
        invalidate();
    }

    public TextureView getTextureView() {
        return textureView;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewSize(int width, int height) {
        if (this.videoWidth == width && this.videoHeight == height) {
            return;
        }
        this.videoWidth = width;
        this.videoHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        previewWidth = getWidth();
        previewHeight = getHeight();

        if (videoWidth == 0 || videoHeight == 0 || previewWidth == 0 || previewHeight == 0) {
            return;
        }

        if (previewWidth * videoHeight > previewHeight * videoWidth) {
            int scaledChildHeight = videoHeight * previewWidth / videoWidth;
            textureView.layout(0, (previewHeight - scaledChildHeight) / scale,
                    previewWidth, (previewHeight + scaledChildHeight) / scale);
        } else {
            int scaledChildWidth = videoWidth * previewHeight / videoHeight;
            textureView.layout((previewWidth - scaledChildWidth) / scale, 0,
                    (previewWidth + scaledChildWidth) / scale, previewHeight);

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 圆的半径
        circleRadius = (float) getHeight() / 2.3f;
        // 圆心的X坐标
        circleX = (float) getWidth() / 2;
        // 圆心的Y坐标
        circleY = (float) getHeight() / 2;
        if (isRegister) {
            // 设置裁剪的圆心坐标，半径
            path.reset();
            path.addCircle(circleX, circleY, circleRadius, Path.Direction.CW);
            // 裁剪画布，并设置其填充方式
            if (Build.VERSION.SDK_INT >= 28) {
                canvas.clipPath(path);
            } else {
                canvas.clipPath(path, Region.Op.REPLACE);
            }
        }
        super.onDraw(canvas);
    }
}
