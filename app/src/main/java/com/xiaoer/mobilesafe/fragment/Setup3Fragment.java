package com.xiaoer.mobilesafe.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.xiaoer.mobilesafe.R;
import com.xiaoer.mobilesafe.Utils.SpKey;
import com.xiaoer.mobilesafe.Utils.SpUtil;
import com.xiaoer.mobilesafe.activity.SetupActivity;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

import static android.content.ContentValues.TAG;

public class Setup3Fragment extends Fragment {

    private View mView;
    private TextView tv_safe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup3, container, false);

        //初始化ui
        initUI();

        return mView;
    }

    /**
     * 初始化ui
     */
    private void initUI() {
        TriStateToggleButton ttb_safe = mView.findViewById(R.id.ttb_safe);
        Button bt_setupOver = mView.findViewById(R.id.bt_setupOver);
        tv_safe = mView.findViewById(R.id.tv_safe);

        boolean safe_open = SpUtil.getBoolean(getContext(), SpKey.SAFE_OPEN, false);
        ttb_safe.setToggleStatus(safe_open);
        if(safe_open){
            tv_safe.setText("手机防盗开启");
        }
        ttb_safe.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean b, int i) {
                switch (i){
                    case 0:
                        tv_safe.setText("手机防盗关闭");
                        SpUtil.putBoolean(getContext(), SpKey.SAFE_OPEN, false);
                        break;
                    case 2:
                        tv_safe.setText("手机防盗开启");
                        SpUtil.putBoolean(getContext(), SpKey.SAFE_OPEN, true);
                        break;
                }
            }
        });

        bt_setupOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                assert activity != null;
                activity.finish();
                //完成设置 进入手机防盗
                Log.d(TAG, "onClick: -----------------------------设置完成");
                Intent intent = new Intent(getContext(), SetupActivity.class);
                intent.putExtra("setup_over",true);
                startActivity(intent);
            }
        });
    }
}