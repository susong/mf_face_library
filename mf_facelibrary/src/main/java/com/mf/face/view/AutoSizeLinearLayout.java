package com.mf.face.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import me.jessyan.autosize.AutoSizeCompat;

public class AutoSizeLinearLayout extends LinearLayout {
    public AutoSizeLinearLayout(Context context) {
        super(context);
    }

    public AutoSizeLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSizeLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoSizeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public AutoSizeLinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        AutoSizeCompat.autoConvertDensity(getResources(), 1080, true);
        return super.generateLayoutParams(attrs);
    }
}
