package com.xiaoer.mobilesafe.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {

    private static SharedPreferences sConfig;

    /**
     * 添加boolean类型的节点数据
     * @param context 上下文
     * @param key   节点名称
     * @param value 节点数据
     */
    public static void putBoolean(Context context, String key, boolean value){
        if(sConfig == null)
            sConfig = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        sConfig.edit().putBoolean(key,value).commit();
    }

    /**
     * 获取boolean节点的数据
     * @param context 上下文对象
     * @param key   节点名称
     * @param defValue  默认值
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defValue){
        if(sConfig == null)
            sConfig = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sConfig.getBoolean(key,defValue);
    }

    /**
     * 添加String类型的节点数据
     * @param context 上下文对象
     * @param key 节点名称
     * @param value 节点数据
     */
    public static void putString(Context context, String key, String value){
        if(sConfig == null)
            sConfig = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        sConfig.edit().putString(key,value).commit();
    }

    /**
     * 获取String节点的数据
     * @param context 上下文对象
     * @param key   节点名称
     * @param defValue  默认值
     * @return
     */
    public static String getString(Context context, String key, String defValue){
        if(sConfig == null)
            sConfig = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sConfig.getString(key,defValue);
    }

    /**
     * 移除某个节点
     * @param context 上下文对象
     * @param key 节点名称
     */
    public static void remove(Context context,String key){
        if(sConfig == null)
            sConfig = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        sConfig.edit().remove(key).apply();
    }
}
