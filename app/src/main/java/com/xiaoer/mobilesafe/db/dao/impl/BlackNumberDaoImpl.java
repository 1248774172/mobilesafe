package com.xiaoer.mobilesafe.db.dao.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xiaoer.mobilesafe.db.BlackNumberOpenHelper;
import com.xiaoer.mobilesafe.db.dao.IBlackNumberDao;
import com.xiaoer.mobilesafe.entity.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

public class BlackNumberDaoImpl implements IBlackNumberDao {

    private static BlackNumberDaoImpl sBnd;
    private static BlackNumberOpenHelper mBlackNumberOpenHelper;

    /**
     * 单例模式私有构造方法
     */
    private BlackNumberDaoImpl(Context context){
        mBlackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }

    /**
     * @return BlackNumberDao实例
     */
    public static BlackNumberDaoImpl getInstance(Context context){
        if(sBnd == null){
            sBnd = new BlackNumberDaoImpl(context);
        }
        return sBnd;
    }

    /**
     * @return 返回数据库中的数据数量
     */
    @SuppressLint("Recycle")
    @Override
    public int getTotalCount() {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(1) from blacknumber", null);
        if(cursor.moveToNext()){
            return cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return 0;
    }

    /**
     * @param phone 电话号
     * @param model 拦截模式 1：拦截短信 2：拦截电话 3：拦截所有
     * @return  插入是否成功
     */
    @Override
    public boolean insert(String phone, int model) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone",phone);
        values.put("model",model);
        long blacknumber = db.insert("blacknumber", null, values);
        db.close();
        return blacknumber != -1;
    }

    /**
     * @param phone 电话号
     * @return 删除是否成功
     */
    @Override
    public boolean deleteByPhone(String phone) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        int back = db.delete("blacknumber", "phone = ?", new String[]{phone});
        db.close();
        return back != 0;
    }

    /**
     * @param phone 电话号
     * @param model 拦截模式 1：拦截短信 2：拦截电话 3：拦截所有
     * @return 更新拦截模式是否成功
     */
    @Override
    public boolean updateModelByPhone(String phone,int model) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("model",model);
        int back = db.update("blacknumber", values, "phone = ?", new String[]{phone});
        db.close();
        return back > 0;
    }

    /**
     * @return 所有数据的集合
     */
    @SuppressLint("Recycle")
    @Override
    public List<BlackNumberInfo> queryAll() {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber", new String[]{"phone", "model"},
                null, null, null, null, "_id desc");
        ArrayList<BlackNumberInfo> blackNumberList = new ArrayList<>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            String phone = cursor.getString(0);
            int model = cursor.getInt(1);
            blackNumberInfo.setPhone(phone);
            blackNumberInfo.setModel(model);
            blackNumberList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberList;
    }

    @SuppressLint("Recycle")
    @Override
    public List<BlackNumberInfo> queryByIndex(int index) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        String sql = "select phone,model from blacknumber order by _id desc limit ?,20";
        Cursor cursor = db.rawQuery(sql, new String[]{index+""});
        ArrayList<BlackNumberInfo> blackNumberList = new ArrayList<>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            String phone = cursor.getString(0);
            int model = cursor.getInt(1);
            blackNumberInfo.setPhone(phone);
            blackNumberInfo.setModel(model);
            blackNumberList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberList;
    }

    @SuppressLint("Recycle")
    @Override
    public boolean queryByPhone(String phone) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber", new String[]{"model"}, "phone = ?", new String[]{phone},
                null, null, null);
        //获取行数
        int count = cursor.getCount();

        cursor.close();
        db.close();
        return count != 0;
    }

    @Override
    public int queryModelByPhone(String phone) {
        SQLiteDatabase db = mBlackNumberOpenHelper.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.query("blacknumber", new String[]{"model"}, "phone = ?", new String[]{phone},
                null, null, null);
        int model = 0;
        while (cursor.moveToNext()){
            model = cursor.getInt(0);
        }
        return model;
    }
}
