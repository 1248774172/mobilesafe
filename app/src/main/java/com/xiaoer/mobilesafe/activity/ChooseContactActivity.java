package com.xiaoer.mobilesafe.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.entity.Contact;

import java.util.ArrayList;

public class ChooseContactActivity extends AppCompatActivity {

    private static final String TAG = "ChooseContactActivity";
    private ListView lv_contact;
    private ArrayList<Contact> mContacts = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            //数据已经准备完毕 给ListView设置数据适配器
            myAdapter myAdapter = new myAdapter();
            lv_contact.setAdapter(myAdapter);
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        //初始化ui
        initUi();
        //准备数据
        initData();
    }

    private void initData() {
        //查看手机联系人 耗时操作 开子线程
        new Thread(){
            @Override
            public void run() {
                mContacts.clear();
                ContentResolver contentResolver = getContentResolver();
                Cursor query = contentResolver.query(Uri.parse("content://com.android.contacts/raw_contacts"),
                        new String[]{"contact_id"}, null, null, null);
                while(query.moveToNext()){
                    //联系人的实体类
                    Contact contact = new Contact();
                    String id = query.getString(0);
                    Cursor query1 = contentResolver.query(Uri.parse("content://com.android.contacts/data"),
                            new String[]{"data1", "mimetype"},
                            "contact_id = ?",
                            new String[]{id},
                            null);

                    assert query1 != null;
                    while (query1.moveToNext()) {
                        String data = query1.getString(0);
                        Log.d(TAG, "run: --------------data----------"+data);
                        if (data != null) {
                            data = data.replace(" ", "").replace("-", "").trim();
                            String mimetype = query1.getString(1);
                            if (mimetype.equals("vnd.android.cursor.item/name")) {
                                contact.setName(data);
                            } else if (mimetype.equals("vnd.android.cursor.item/phone_v2")) {
                                contact.setPhone(data);
                            }
                            //将联系人信息储存在list中
                        }
                    }
                    mContacts.add(contact);
                    query1.close();
                }
                query.close();
                Log.d(TAG, "run: -----------------------"+mContacts.size());
                //发消息 通知适配器数据已经准备完毕
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initUi() {
        lv_contact = findViewById(R.id.lv_contact);

        //给每个条目增加点击事件
        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_phone = view.findViewById(R.id.tv_phone);
                String phone = tv_phone.getText().toString().trim();
                Log.d(TAG, "onItemClick: -----------------"+phone);
                //返回电话号信息
                Intent intent = new Intent();
                intent.putExtra("phone",phone);
                setResult(0,intent);
                finish();
            }
        });

    }

    class myAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public Contact getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder") View view = View.inflate(getApplicationContext(), R.layout.contact_item_view, null);
            TextView tv_name = view.findViewById(R.id.tv_name);
            TextView tv_phone = view.findViewById(R.id.tv_phone);
            Contact contact = getItem(position);
            tv_name.setText(contact.getName());
            tv_phone.setText(contact.getPhone());
            return view;
        }
    }
}