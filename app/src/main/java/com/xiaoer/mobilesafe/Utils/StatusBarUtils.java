package com.xiaoer.mobilesafe.Utils;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class StatusBarUtils {
    /**
     *  @describe 隐藏状态栏
     */
    public static void hideStatusBar(AppCompatActivity activity){
        //获得当前窗体对象
        Window window = activity.getWindow();
        //设置当前窗体为全屏显示
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);
    }

    /**
     *  @describe 设置状态栏颜色
     */
    public static void setWindowStatusBarColor(AppCompatActivity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
