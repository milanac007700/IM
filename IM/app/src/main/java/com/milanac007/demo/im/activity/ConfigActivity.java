package com.milanac007.demo.im.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.databinding.ActivityConfigBinding;
import com.milanac007.demo.im.databinding.TitleLayoutBinding;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.utils.Preferences;

public class ConfigActivity extends BaseActivity {
    private static final String TAG = "ConfigActivity";
    private ActivityConfigBinding activityConfigBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityConfigBinding = ActivityConfigBinding.inflate(LayoutInflater.from(this));
        setContentView(activityConfigBinding.getRoot());
        TitleLayoutBinding titleLayout  = activityConfigBinding.titleLayout;
        titleLayout.backBtn.setVisibility(View.VISIBLE);

        titleLayout.titleTextView.setText("配置");

        View[] views = {titleLayout.backBtn, activityConfigBinding.addressDelView, activityConfigBinding.btnConfirm};
        for(View view : views) {
            view.setOnClickListener(this);
        }

        activityConfigBinding.etAddress.setText(NetConstants.HostName);
    }

    @Override
    public void onIMServiceConnected() {

    }

    private void processSaveConfig() {
        String hostName = activityConfigBinding.etAddress.getText().toString().trim();
        Preferences.setHostName(hostName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:{
                setHideKey();
                finish();
            }break;
            case R.id.et_address: {
                activityConfigBinding.etAddress.setText("");
            }
            break;
            case R.id.btn_confirm: {
                setHideKey();
                processSaveConfig();
                finish();
            }
            break;
            default:
                break;
        }
    }
}