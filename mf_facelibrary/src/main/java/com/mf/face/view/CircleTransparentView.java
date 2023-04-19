package com.mf.face.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mf.face.R;

public class CircleTransparentView extends FrameLayout {
    private Path path;
    private Paint paint;
    private int width = 0;
    private int height = 0;

    public CircleTransparentView(Context context) {
        super(context);
        init();
    }

    public CircleTransparentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleTransparentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircleTransparentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.transparentcc));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5f);
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
        // 画透明圆形，外围是半透明
        path.reset();
        path.addRect(0f, 0f, width, height, Path.Direction.CW);
        path.addCircle((float) width / 2, (float) height / 2, (float) height / 2.3f, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }
}
