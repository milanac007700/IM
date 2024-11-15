package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.event.SelfInfoChangeEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2016/11/17.
 */
public class AccountSecurityFragment extends BaseFragment implements View.OnClickListener{

    private TextView account;
    private TextView myhoneNumber;
    private TextView emailbox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.account_security_layout, null);
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_ACCOUNT_SECURITY;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View mBack = view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText("账号与安全");

        View phoneNumberLayout = view.findViewById(R.id.phoneNumberLayout);
        View modifyPasswordLayout = view.findViewById(R.id.modifyPasswordLayout);
        account = (TextView) view.findViewById(R.id.account);
        myhoneNumber = (TextView)view.findViewById(R.id.my_phone_number);

        emailbox = (TextView)view.findViewById(R.id.emailbox);

        View[] views = {mBack, phoneNumberLayout, modifyPasswordLayout};
        for(View v : views){
            v.setOnClickListener(this);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        setData();

    }

    private void setData(){
        account.setText(Preferences.getCurrentLoginer() == null || TextUtils.isEmpty(Preferences.getCurrentLoginer().getUserCode()) ? "暂无" : Preferences.getCurrentLoginer().getUserCode());
        myhoneNumber.setText(Preferences.getCurrentLoginer() == null || TextUtils.isEmpty(Preferences.getCurrentLoginer().getTelephone()) ? "暂无" : Preferences.getCurrentLoginer().getTelephone());
        emailbox.setText(Preferences.getCurrentLoginer() == null || TextUtils.isEmpty(Preferences.getCurrentLoginer().getEmailAddress()) ? "暂无" : Preferences.getCurrentLoginer().getEmailAddress());
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;

            case R.id.phoneNumberLayout:{
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_MODIFY_PERSONALINFO, 0, "phone");
            }break;

            case R.id.modifyPasswordLayout:{
			mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_RESETPWD, null);
            }break;
            default:break;
        }
    }

    public void onEventMainThread(SelfInfoChangeEvent event){
        setData();
    }

}
