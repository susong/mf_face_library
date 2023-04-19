package com.mf.face.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.mf.face.utils.DensityUtils;

public class PolygonLayout extends FrameLayout {
    private Path path;
    private int width = 0;
    private int height = 0;
    // 这是根据设计图测量出的值
    private final int subWidth = DensityUtils.dip2px(getContext(), 61);
    private final int subHeight = DensityUtils.dip2px(getContext(), 40);

    public PolygonLayout(Context context) {
        super(context);
        init();
    }

    public PolygonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PolygonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PolygonLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        path = new Path();
        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        path.moveTo(width, 0);
        path.lineTo(width, height - DensityUtils.dip2px(getContext(), subHeight));
        path.lineTo(width - DensityUtils.dip2px(getContext(), subWidth), height);
        path.lineTo(0, height);
        path.lineTo(0, DensityUtils.dip2px(getContext(), subHeight));
        path.lineTo(DensityUtils.dip2px(getContext(), subWidth), 0);
        path.close();

        // 裁剪画布，并设置其填充方式
        if (Build.VERSION.SDK_INT >= 28) {
            canvas.clipPath(path);
        } else {
            canvas.clipPath(path, Region.Op.REPLACE);
        }
        super.onDraw(canvas);
    }
}
