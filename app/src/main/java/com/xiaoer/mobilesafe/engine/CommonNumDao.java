package com.xiaoer.mobilesafe.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonNumDao {


    private String mPath;
    private SQLiteDatabase mSqLiteDatabase;

    /**
     * @param context 上下文对象
     * @return  返回ExpandableListView里组的信息
     */
    public List<Group> getGroup(Context context){
        mPath = context.getFilesDir().getPath() + File.separator + "commonnum.db";
        mSqLiteDatabase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        @SuppressLint("Recycle")
        Cursor query = mSqLiteDatabase.query("classlist", new String[]{"name","idx"},
                null, null, null, null, null);
        ArrayList<Group> groups = new ArrayList<>();
        while ((query.moveToNext())){
            Group group = new Group();
            group.name = query.getString(0);
            group.idx = query.getString(1);
            group.childList = getChild(group.idx);
            groups.add(group);
        }
        query.close();
        mSqLiteDatabase.close();
        return groups;
    }

    /**
     * @param idx 组别
     * @return 返回该组别下的子数据
     */
    private List<Child> getChild(String idx){
        mSqLiteDatabase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        @SuppressLint("Recycle")
        Cursor query = mSqLiteDatabase.query("table" + idx, new String[]{"_id", "name", "number"},
                null, null, null, null, null);
        ArrayList<Child> children = new ArrayList<>();
        while (query.moveToNext()){
            Child child = new Child();
            child._id = query.getString(0);
            child.name = query.getString(1);
            child.number = query.getString(2);
            children.add(child);
        }
        query.close();
        mSqLiteDatabase.close();
        return children;
    }

    public static class Group{
        public String name;
        public String idx;
        public List<Child> childList;
    }

    public static class Child{
        public String name;
        public String _id;
        public String number;
    }
}
