package com.xiaoer.mobilesafe.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaoer.mobilesafe.R;

/**
 * 手机防盗设置的第一个界面，用来展示手机防盗的功能
 */
public class Setup1Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup1, container, false);
    }
}