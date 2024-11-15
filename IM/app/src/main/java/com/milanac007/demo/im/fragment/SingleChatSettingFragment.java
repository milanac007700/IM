package com.milanac007.demo.im.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.manager.IMSessionManager;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;

import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.utils.CommonFunction;

/**
 * Created by milanac007 on 2016/11/26.
 */
public class SingleChatSettingFragment extends BaseFragment implements View.OnClickListener{

    private View view1;
    private TextView mBack;
    private View mDelAllSingleChatsView;
    private View mAddUserToChatView;
    private CircleImageView mBuddyIco;
    private int buddyid;
    private UserEntity imBuddy;

    private IMService imService;
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

    private void setData(){
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        buddyid = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
        imBuddy = imService.getContactManager().findContact(buddyid);
        if(imBuddy != null){
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(mBuddyIco, imBuddy);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.single_chat_setting_layout, null);
    }

    protected void setTitle(TextView mTitle){
        mTitle.setText("设置");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view1 = view.findViewById(R.id.single_chat_setting_layout);
        mBack = (TextView)view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mDelAllSingleChatsView = view.findViewById(R.id.del_all_single_chats);
        mAddUserToChatView = view.findViewById(R.id.add_user_to_group);
        mBuddyIco = (CircleImageView)view.findViewById(R.id.buddy_ico);

        setTitle(mTitle);
        setListener();
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_SINGLE_CHAT_SETTING;
    }

    private void setListener(){
        View[] views = {mBack, mDelAllSingleChatsView, mAddUserToChatView, mBuddyIco};
        for (View view1: views){
            view1.setOnClickListener(this);
        }

        view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });
    }

    private void onClickDelChatsBtn(){

        CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mActivity);
        builder.setTitle("删除")
                .setMessage(String.format("确定删除和%s的聊天记录吗？", imBuddy.getMainName()));
        builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String sessionKey = DBConstant.SESSION_TYPE_SINGLE + "_" + buddyid;
                SessionEntity sessionEntity = IMSessionManager.instance().findSession(sessionKey);
                imService.getSessionManager().clearMsgBySession(sessionEntity);

                ChatFragment.newInstance().clearData();
                CommonFunction.showToast("已删除");
            }
        });

        builder.setNegativeBtn("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        CustomConfirmDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back: {
                onBack();
            }break;
            case R.id.del_all_single_chats: {
                onClickDelChatsBtn();
            }break;
            case R.id.add_user_to_group: {
                //TODO 加人 建立群组
                JSONObject param = new JSONObject();
                param.put("hideHeader", true);
                param.put("fromSingleChatSetting", true);
                JSONArray selected = new JSONArray();
                selected.add(buddyid);
                param.put("selected", selected);
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_CREATE_GROUP_CHAT, 0, param.toJSONString());
            }break;
            case R.id.buddy_ico: {
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, buddyid+"");
            }break;
            default:
                break;
        }
    }

}
