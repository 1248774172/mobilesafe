package com.xiaoer.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoer.mobilesafe.R;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

/**
 *
 */
public class SettingClickView extends RelativeLayout {

    private TextView tv_bigTitle;
    private TextView tv_smallTitle;
    private View v_line;

    public SettingClickView(Context context) {
        this(context,null);
    }

    public SettingClickView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View inflate = View.inflate(context, R.layout.setting_click_view, this);
        tv_bigTitle = inflate.findViewById(R.id.tv_bigTitle);
        tv_smallTitle = inflate.findViewById(R.id.tv_smallTitle);
        v_line = inflate.findViewById(R.id.v_line);

    }

    /**
     * @param title 标题
     */
    private void initTitle(String title) {
        tv_bigTitle.setText(title);
    }

    public void hideLine(){
        v_line.setVisibility(INVISIBLE);
    }
}
