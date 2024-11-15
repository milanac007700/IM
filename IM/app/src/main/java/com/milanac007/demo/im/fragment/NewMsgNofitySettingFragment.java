package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.db.sp.ConfigurationSp;
import com.milanac007.demo.im.db.config.SysConstant;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.SwitchView;

/**
 * Created by zqguo on 2016/11/17.
 */
public class NewMsgNofitySettingFragment extends BaseFragment implements SwitchView.OnStateChangedListener {

    private IMService imService;
    private SwitchView newMsgNotifySwitch;
    private SwitchView showMsgDetailSwitch;
    private SwitchView playVoiceSwitch;
    private SwitchView vibrateSwitch;
    private View subSettingLayout;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            setData();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }

    private void setData(){
        boolean isOpen = imService.getConfigSp().getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.NOTIFICATION);
        newMsgNotifySwitch.toggleSwitch(isOpen);
        subSettingLayout.setVisibility(isOpen ? View.VISIBLE:View.GONE);

        isOpen = imService.getConfigSp().getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.SHOW_MSG_DETAIL);
        showMsgDetailSwitch.toggleSwitch(isOpen);

        isOpen = imService.getConfigSp().getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.SOUND);
        playVoiceSwitch.toggleSwitch(isOpen);

        isOpen = imService.getConfigSp().getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION);
        vibrateSwitch.toggleSwitch(isOpen);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_msg_nofity_setting_layout, null);
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_NEW_MSG_NOTIFY_SETTING;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View mBack = view.findViewById(R.id.fragment_head1_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText("新消息提醒");

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        subSettingLayout = view.findViewById(R.id.subSettingLayout);
        newMsgNotifySwitch = (SwitchView)view.findViewById(R.id.newMsgNotifySwitch);
        showMsgDetailSwitch = (SwitchView)view.findViewById(R.id.showMsgDetailSwitch);
        playVoiceSwitch = (SwitchView)view.findViewById(R.id.playVoiceSwitch);
        vibrateSwitch = (SwitchView)view.findViewById(R.id.vibrateSwitch);

        SwitchView[] switchViews = {newMsgNotifySwitch, showMsgDetailSwitch, playVoiceSwitch, vibrateSwitch};
        for(SwitchView view1 : switchViews){
            view1.setOnStateChangedListener(this);
        }
    }
    private void handleNewMsgNotifySwitch(boolean isOpened){
        newMsgNotifySwitch.toggleSwitch(isOpened);
        imService.getConfigSp().setCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.NOTIFICATION, isOpened);
        subSettingLayout.setVisibility(isOpened ? View.VISIBLE:View.GONE);
    }

    private void handleShowMsgDetailSwitch(boolean isOpened){
        showMsgDetailSwitch.toggleSwitch(isOpened);
        imService.getConfigSp().setCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SHOW_MSG_DETAIL, isOpened);
    }

    private void handlePlayVoiceSwitch(boolean isOpened){
        playVoiceSwitch.toggleSwitch(isOpened);
        imService.getConfigSp().setCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SOUND, isOpened);
    }


    private void handleVibrateSwitch(boolean isOpened){
        vibrateSwitch.toggleSwitch(isOpened);
        imService.getConfigSp().setCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.VIBRATION, isOpened);
    }

    @Override
    public void toggleToOn(SwitchView view) {
        switch (view.getId()){
            case R.id.newMsgNotifySwitch:{
                handleNewMsgNotifySwitch(true);
            }break;
            case R.id.showMsgDetailSwitch:{
                handleShowMsgDetailSwitch(true);
            }break;
            case R.id.playVoiceSwitch:{
                handlePlayVoiceSwitch(true);
            }break;
            case R.id.vibrateSwitch:{
                handleVibrateSwitch(true);
            }break;
        }
    }

    @Override
    public void toggleToOff(SwitchView view) {
        switch (view.getId()){
            case R.id.newMsgNotifySwitch:{
                handleNewMsgNotifySwitch(false);
            }break;
            case R.id.showMsgDetailSwitch:{
                handleShowMsgDetailSwitch(false);
            }break;
            case R.id.playVoiceSwitch:{
                handlePlayVoiceSwitch(false);
            }break;
            case R.id.vibrateSwitch:{
                handleVibrateSwitch(false);
            }break;
        }
    }
}
