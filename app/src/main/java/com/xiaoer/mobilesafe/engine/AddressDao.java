package com.xiaoer.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

import static android.content.ContentValues.TAG;

public class AddressDao {

    private static String sLocation = "未知号码";

    public static String getAddress(Context context, String number) {
        Log.d(TAG, "getAddress: --------------------------------传来的电话号：" + number);
        //获取数据库的地址
        String path = context.getFilesDir().getPath() + File.separator + "address.db";
        //以只读的方式打开从数据库
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        //判断手机号是否是11位并且是否正确
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
        if (number.matches(regex)) {
            number = number.substring(0,7);
            Log.d(TAG, "getAddress: ---------------------------------电话号合法，前七位是："+number);
            //根据传来的number查询data1获得对应的外键
            @SuppressLint("Recycle")
            Cursor data1 = sqLiteDatabase.query("data1", new String[]{"outkey"}, "id = ?", new String[]{number},
                    null, null, null);
            while (data1.moveToNext()) {
                Log.d(TAG, "getAddress: ---------------------------------表一查询到了数据");
                String outKey = data1.getString(0);
                //根据data1查到的outKey查询data2获得具体地址
                @SuppressLint("Recycle")
                Cursor data2 = sqLiteDatabase.query("data2", new String[]{"location"}, "id = ?", new String[]{outKey},
                        null, null, null);
                while (data2.moveToNext()) {
                    sLocation = data2.getString(0);
                }
            }
            return sLocation;
        }else{
            //说明用户输入的电话号不是11位或者不正确
            int length = number.length();
            Log.d(TAG, "getAddress: -------------------------------电话号不合法，长度是："+length);
            switch (length){
                case 3:
                    sLocation = "报警电话";
                    return sLocation;
                case 4:
                    sLocation = "模拟器";
                    return sLocation;
                case 5:
                    sLocation = "服务电话";
                    return sLocation;
                case 7:
                    sLocation = "固定电话";
                    return sLocation;
                case 11:  //3+8
                    number = number.substring(1,3);
                    @SuppressLint("Recycle")
                    Cursor data1 = sqLiteDatabase.query("data2", new String[]{"location"}, "area = ?", new String[]{number}
                            , null, null, null);
                    if(data1.moveToNext()){
                        String location = data1.getString(0);
                        sLocation = location;
                    }else{
                        sLocation = "未知号码";
                    }
                    return sLocation;
                case 12:  //4+8
                    number = number.substring(1,4);
                    @SuppressLint("Recycle")
                    Cursor data2 = sqLiteDatabase.query("data2", new String[]{"location"}, "area = ?", new String[]{number}
                            , null, null, null);
                    if(data2.moveToNext()){
                        String location = data2.getString(0);
                        sLocation = location;
                    }else{
                        sLocation = "未知号码";
                    }
                    return sLocation;
            }
        }
        return "未知号码";
    }
}
