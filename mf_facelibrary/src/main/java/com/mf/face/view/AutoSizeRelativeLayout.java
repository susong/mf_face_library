package com.mf.face.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import me.jessyan.autosize.AutoSizeCompat;

public class AutoSizeRelativeLayout extends RelativeLayout {
    public AutoSizeRelativeLayout(Context context) {
        super(context);
    }

    public AutoSizeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSizeRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoSizeRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        AutoSizeCompat.autoConvertDensity(getResources(), 1080, true);
        return super.generateLayoutParams(attrs);
    }
}
