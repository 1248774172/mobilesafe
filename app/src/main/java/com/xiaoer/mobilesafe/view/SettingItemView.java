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
public class SettingItemView extends RelativeLayout {

    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private TriStateToggleButton ttb_set;
    private TextView tv_bigTitle;
    private TextView tv_smallTitle;
    private String mSmallTitleOn;
    private String mSmallTitleOff;
    private View v_line;

    public SettingItemView(Context context) {
        this(context,null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View inflate = View.inflate(context, R.layout.setting_item_view, this);
        ttb_set = inflate.findViewById(R.id.ttb_set);
        tv_bigTitle = inflate.findViewById(R.id.tv_bigTitle);
        tv_smallTitle = inflate.findViewById(R.id.tv_smallTitle);
        v_line = inflate.findViewById(R.id.v_line);

        //设置按钮不处理事件
        ttb_set.setClickable(false);
        ttb_set.setFocusable(false);
        ttb_set.setFocusableInTouchMode(false);

        //初始化标题
        initTitle(attrs);

    }

    /**
     * @param attrs 属性集合
     */
    private void initTitle(AttributeSet attrs) {
        String bigTitle = attrs.getAttributeValue(NAMESPACE, "bigTitle");
        mSmallTitleOff = attrs.getAttributeValue(NAMESPACE,"smallTitleOff");
        mSmallTitleOn = attrs.getAttributeValue(NAMESPACE, "smallTitleOn");
        tv_bigTitle.setText(bigTitle);
    }

    /**
     *  @describe 根据按钮的状态来判断条目的状态
     */
    public boolean isChecked(){
        TriStateToggleButton.ToggleStatus toggleStatus = ttb_set.getToggleStatus();
        return toggleStatus == TriStateToggleButton.ToggleStatus.on;
    }

    /**
     *  @describe 设置条目的状态
     */
    public void setChecked(boolean b){
        ttb_set.setToggleStatus(b);
        if(b){
            tv_smallTitle.setText(mSmallTitleOn);
        }else {
            tv_smallTitle.setText(mSmallTitleOff);
        }
    }

    public void hideLine(){
        v_line.setVisibility(INVISIBLE);
    }
}
