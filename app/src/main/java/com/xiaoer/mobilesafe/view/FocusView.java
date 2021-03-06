package com.xiaoer.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;

public class FocusView extends androidx.appcompat.widget.AppCompatTextView {

    public FocusView(Context context) {
        this(context,null);
    }

    public FocusView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FocusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
