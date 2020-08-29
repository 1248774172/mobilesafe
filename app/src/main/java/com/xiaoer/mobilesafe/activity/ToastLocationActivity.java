package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;

import static android.content.ContentValues.TAG;

public class ToastLocationActivity extends Activity{

    /**
     * 按住提示框拖拽到任意位置
     */
    private Button toast_location_top;
    private ImageView iv_drag;
    /**
     * 按住提示框拖拽到任意位置
     */
    private Button toast_location_bottom;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_location);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();

        //初始化ui
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        toast_location_top = findViewById(R.id.toast_location_top);
        iv_drag = findViewById(R.id.iv_drag);
        toast_location_bottom = findViewById(R.id.toast_location_bottom);

        //获取储存的位置信息
        int toast_location_x = SpUtil.getInt(getApplicationContext(), SpKey.TOAST_LOCATION_X, 0);
        int toast_location_y = SpUtil.getInt(getApplicationContext(), SpKey.TOAST_LOCATION_Y, 0);
//        iv_drag.layout(toast_location_x,toast_location_y,
//                toast_location_x+iv_drag.getWidth(),toast_location_y+iv_drag.getHeight());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = toast_location_x;
        layoutParams.topMargin = toast_location_y;
        if(toast_location_y > mScreenHeight / 2) {
            toast_location_bottom.setVisibility(View.INVISIBLE);
            toast_location_top.setVisibility(View.VISIBLE);
        }else {
            toast_location_bottom.setVisibility(View.VISIBLE);
            toast_location_top.setVisibility(View.INVISIBLE);
        }
        iv_drag.setLayoutParams(layoutParams);

        //设置双击事件
        iv_drag.setOnClickListener(new View.OnClickListener() {
            long [] hits = new long[2];
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ----------------------------点击一次");
                System.arraycopy(hits,1,hits,0,hits.length - 1);
                hits[hits.length - 1] = SystemClock.uptimeMillis();
                if(hits[hits.length - 1] - hits[0] < 300){
                    Log.d(TAG, "onClick: -----------------------双击生效");
                    int left = mScreenWidth / 2 - iv_drag.getWidth() / 2;
                    int top = mScreenHeight / 2 - iv_drag.getHeight() / 2;
                    int right = left + iv_drag.getWidth();
                    int bottom = top + iv_drag.getHeight();
                    iv_drag.layout(left, top, right, bottom);
                    SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_X,iv_drag.getLeft());
                    SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_Y,iv_drag.getTop());
                }
            }
        });

        //设置触摸事件
        iv_drag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    //当点击时
                    case MotionEvent.ACTION_DOWN:
                        //记录起始位置
//                        getX是以组件左上角为坐标原点，获取X坐标轴上的值。
//                        getRawX是以屏幕左上角为左脚做预案，获取X坐标轴上的值。
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    //当移动时
                    case MotionEvent.ACTION_MOVE:
                        //记录移动完的位置
                        mEndX = (int) event.getRawX();
                        mEndY = (int) event.getRawY();

                        //计算差值
                        int disX = mEndX - mStartX;
                        int disY = mEndY - mStartY;

//                        int left = mStartX + disX;
//                        int top = mStartY + disY;
//                        int right = left + iv_drag.getWidth();
//                        int bottom = top + iv_drag.getHeight();

                        //得到移动完的具体位置
                        int left = iv_drag.getLeft() + disX;
                        int top = iv_drag.getTop() + disY;
                        int right = iv_drag.getRight() + disX;
                        int bottom = iv_drag.getBottom() + disY;

                        //容错处理
                        if(left < 0 || left > mScreenWidth - iv_drag.getWidth() || top < 0 || bottom > mScreenHeight){
                            return true;
                        }

                        //设置上下提示文本的显示状态
                        if(top > mScreenHeight / 2) {
                            toast_location_bottom.setVisibility(View.INVISIBLE);
                            toast_location_top.setVisibility(View.VISIBLE);
                        }else {
                            toast_location_bottom.setVisibility(View.VISIBLE);
                            toast_location_top.setVisibility(View.INVISIBLE);
                        }

                        //更新位置
                        iv_drag.layout(left, top, right, bottom);

                        //重置起始位置 将最后的位置当成起始位置
                        mStartX = mEndX;
                        mStartY = mEndY;
                        break;
                    //当抬起时
                    case MotionEvent.ACTION_UP:
                        //保存位置
                        SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_X,iv_drag.getLeft());
                        SpUtil.putInt(getApplicationContext(), SpKey.TOAST_LOCATION_Y,iv_drag.getTop());
                        break;
                }
                //只有触摸事件时返回值是true才能响应触摸事件
                //既有触摸事件又有点击事件时 返回false 点击事件才能生效   P964
                return false;
            }
        });
    }

}