package com.milanac007.demo.im.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.mengle.lib.wiget.ConfirmDialog;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.ui.CustomConfirmDialog;

/**
 * Created by zqguo on 2016/11/10.
 */
public class SetupFragment extends  BaseFragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setup_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View mBack = view.findViewById(R.id.fragment_head1_back);
        View mNewMsgNotify = view.findViewById(R.id.newMsgNotify);
        View mAccountSecity = view.findViewById(R.id.accountSecurity);
        View mAboutOA = view.findViewById(R.id.aboutOA);
        View mLogout = view.findViewById(R.id.rl_logout);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setText("设置");
        mTitle.setVisibility(View.VISIBLE);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        View[] views = {mBack, mNewMsgNotify, mAccountSecity, mAboutOA, mLogout, };
        for (View v : views){
            v.setOnClickListener(this);
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_SETUP;
    }

    private void logout() {
        ConfirmDialog.open(getActivity(), "退出", "是否确定？",
                new ConfirmDialog.OnClickListener() {

                    @Override
                    public void onPositiveClick() {
                        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_EXIT, 0, null);
                    }

                    @Override
                    public void onNegativeClick() {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;
            case R.id.newMsgNotify:{
                this.mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_NEW_MSG_NOTIFY_SETTING, null);
            }break;
            case R.id.accountSecurity:{
                this.mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ACCOUNT_SECURITY, null);
            }break;
            case R.id.aboutOA:{
                this.mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ABOUT_OA, null);
            }break;
            case R.id.rl_logout:{
                logout();
            }break;
        }

    }

    private void showSipSettingDialog(){
        final CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(getActivity());
        builder.setTitle("sip video settings");
        builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                writeSpSipSetting();
            }
        });

        builder.setNegativeBtn("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private void writeSpSipSetting(){
        SharedPreferences sp  = getActivity().getSharedPreferences("sipSettingFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sp.edit();

        editor.commit();
    }

}
