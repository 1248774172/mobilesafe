package com.xiaoer.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoer.mobilesafe.R;

public class SettingItemView extends RelativeLayout {

    private CheckBox cb_set;
    private TextView tv_bigTitle;
    private TextView tv_smallTitle;

    public SettingItemView(Context context) {
        this(context,null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View inflate = View.inflate(context, R.layout.setting_item_view, this);
        cb_set = inflate.findViewById(R.id.cb_set);
        tv_bigTitle = inflate.findViewById(R.id.tv_bigTitle);
        tv_smallTitle = inflate.findViewById(R.id.tv_smallTitle);
    }

    public boolean isChecked(){
        return cb_set.isChecked();
    }

    public void setChecked(boolean b){
        cb_set.setChecked(b);
        if(b){
            tv_smallTitle.setText("自动更新已开启");
        }else {
            tv_smallTitle.setText("自动更新已关闭");
        }
    }

    public void setTitle(String title){
        tv_bigTitle.setText(title);
    }
}
