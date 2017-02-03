package com.zet.enterprises.multimediapicker.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by admin on 03/02/2017.
 */

public class CustomFrame extends FrameLayout {
    public CustomFrame(Context context) {
        super(context);
    }

    public CustomFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}

