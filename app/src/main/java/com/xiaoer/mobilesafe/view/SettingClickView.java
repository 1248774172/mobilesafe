package com.xiaoer.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoer.mobilesafe.R;

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
     * 设置条目的大标题
     * @param bigTitle 大标题
     */
    public void setBigTitle(String bigTitle){
        tv_bigTitle.setText(bigTitle);
    }

    /**
     * 设置条目的小标题
     * @param smallTitle 小标题
     */
    public void setSmallTitle(String smallTitle){
        tv_smallTitle.setText(smallTitle);
    }


    /**
     * 隐藏横线
     */
    public void hideLine(){
        v_line.setVisibility(INVISIBLE);
    }
}
