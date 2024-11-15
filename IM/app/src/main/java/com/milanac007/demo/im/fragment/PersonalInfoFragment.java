package com.milanac007.demo.im.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.BuddyVerifyMessage;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2016/10/11.
 */
public class PersonalInfoFragment extends BaseFragment implements View.OnClickListener {

    private View mBack;
    private View mHeader;
    private CircleImageView buddyHeadIcon;
    private TextView buddyNickName;
    private TextView buddyName;
    private TextView buddyAccount;
    private TextView buddyMobile;
    private TextView buddyEmail;
    private LinearLayout ll_mail;
    private TextView writeEmailBtn;
    private TextView send_text_msg_btn;
    private TextView send_voice_msg_btn;
    private View mView;
    private TextView add_friend_msg_btn;
    private int buddyid;
    private TextView mFinish;
    private String phoneStr;

//    private IMService imService;
    private IMContactManager contactMgr;
    private UserEntity imBuddy;
//    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
//        @Override
//        public void onIMServiceConnected() {
//            logger.d("contactUI#onIMServiceConnected");
//
//            imService = imServiceConnector.getIMService();
//            if (imService == null) {
//                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
//                return;
//            }
//            contactMgr = imService.getContactManager();
//            EventBus.getDefault().registerSticky(PersonalInfoFragment.this);
//            Bundle bundle = PersonalInfoFragment.this.mListener.getArguments(PersonalInfoFragment.this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
//            buddyid = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
//            setData();
//        }
//
//        @Override
//        public void onServiceDisconnected() {
//            if (EventBus.getDefault().isRegistered(PersonalInfoFragment.this)) {
//                EventBus.getDefault().unregister(PersonalInfoFragment.this);
//            }
//        }
//    };


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        imServiceConnector.connect(getActivity());
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        if (EventBus.getDefault().isRegistered(PersonalInfoFragment.this)) {
//            EventBus.getDefault().unregister(PersonalInfoFragment.this);
//        }
//        imServiceConnector.disconnect(getActivity());
//    }

    @Override
    public void onIMServiceConnected() {
        super.onIMServiceConnected();
        if (getActivity() == null) {
            Log.e(TAG(), "onIMServiceConnected(), getActivity() == null, return.");
            return;
        }

        contactMgr = imService.getContactManager();
        Bundle bundle = mListener.getArguments(getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        buddyid = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
//        buddyid = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID).split("_")[1]);
        setData();
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event.event) {
            case USER_INFO_UPDATE:
                setData();
                break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.personal_info_fragment, null);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    protected void setTitle(TextView mTitle){
        mTitle.setText("详细资料");
    }

    private void initView(View view){
        mView = view;
        mBack = view.findViewById(R.id.fragment_head1_back);
        mHeader = view.findViewById(R.id.fragment_head1);

        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        setTitle(mTitle);

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.mipmap.more_operation);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);
        mFinish.setOnClickListener(this);

        buddyHeadIcon = (CircleImageView)view.findViewById(R.id.buddyHeadIcon);
        buddyNickName = (TextView)view.findViewById(R.id.nickName);
        buddyName = (TextView)view.findViewById(R.id.buddyName);
        buddyAccount = (TextView)view.findViewById(R.id.buddyAccount);
        buddyMobile = (TextView)view.findViewById(R.id.buddyMobile);
        buddyEmail = (TextView)view.findViewById(R.id.buddyEmail);
        writeEmailBtn = (TextView)view.findViewById(R.id.write_email);
        ll_mail = (LinearLayout) view.findViewById(R.id.ll_mail);
        add_friend_msg_btn = (TextView)view.findViewById(R.id.add_friend_msg_btn);
        send_text_msg_btn = (TextView)view.findViewById(R.id.send_text_msg_btn);
        send_voice_msg_btn = (TextView)view.findViewById(R.id.voice_chat_btn);
    }

    private void setListener(){
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; //不向下传递
            }
        });

        View[] views = {mBack, mFinish, send_text_msg_btn, send_voice_msg_btn, buddyMobile, writeEmailBtn, add_friend_msg_btn};
        for(View view : views){
            view.setOnClickListener(this);
        }

    }

    private void sendIM(final String extNo) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, extNo);
    }


    private void showSipCallDialog() {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SHOW_CALL_DIALOG, 0, imBuddy.getPeerId()+"");
    }


    private String[] getRightMenu() {
        imService.getLoginManager().getLoginId();
        if(imBuddy.isFriend()){
            String[] menuStr = {"设置邮箱和备注", "分享该名片", "解除好友", "加入黑名单"};
            return menuStr;
        }else {
            String[] menuStr = {"设置邮箱和备注", "加入黑名单"};
            return menuStr;
        }
    }

    OperateListDialog operateListDialog;
    private ArrayList<OperateListDialog.OperateItem> operateItems = new ArrayList<>();

    private void showMoreOperationDialog() {

        final String[] menuStr = getRightMenu();
        if (menuStr == null || menuStr.length <= 0) {
            return;
        }

        if(operateListDialog == null) {
            operateListDialog = new OperateListDialog(getActivity());
            operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
        }
        operateItems.clear();


        int size = menuStr.length;
        for (int i = 0; i< size; i++) {
            final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
            item.setmItemNameStr(menuStr[i]);
            item.setmOperateKey(String.valueOf(i));

            item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
                @Override
                public void clickItem(int position) {
                    String itemStr = item.getmItemNameStr();
                    if("设置邮箱和备注".equals(itemStr)){
                        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_MODIFY_BUDDY_EXTRA, imBuddy.getSessionKey());
                    }else if("分享该名片".equals(itemStr)){
                        CommonFunction.showToast(R.string.func_developing);
                    }else if("解除好友".equals(itemStr)){
                        showDelBuddyDialog();
                    }else if("加入黑名单".equals(itemStr)){
                        CommonFunction.showToast(R.string.func_developing);
                    }

                    if (operateListDialog != null) {
                        operateListDialog.dismiss();
                    }
                }
            });
            operateItems.add(item);
        }

        operateListDialog.setGravityType(1); //底部居中显示
//        operateListDialog.setTitle("请选择");
        operateListDialog.showTitle(false);
        operateListDialog.updateOperateItems(operateItems);

        operateListDialog.show();
    }


    private void showDelBuddyDialog(){
        if(imBuddy == null)
            return;

        CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(getActivity());
        builder.setTitle("删除好友");
        builder.setMessage(String.format("将好友%s删除,将同时删除与该好友的聊天记录", imBuddy.getMainName()));
        builder.setPositiveBtn("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DelFriendRequest(buddyid);
            }
        });

        builder.setNegativeBtn("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void DelFriendRequest(final int buddyId){
//        if(buddyId == 0)
//            return;
//
//        CommonFunction.showProgressDialog(getContext(), "请稍候...");
//        contactMgr.ReqDelBuddy(buddyId, "", new Packetlistener() {
//            @Override
//            public void onSuccess(Object response) {
//                CommonFunction.dismissProgressDialog();
//                try{
//                    IMBuddy.IMDelBuddyRsp rsp = IMBuddy.IMDelBuddyRsp.parseFrom((CodedInputStream) response);
//                    int result = imService.getContactManager().onRepDelBuddy(rsp);
//                    if(result == 0){
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mListener.OnAction(getPageNumber(), ImContainer.ACTION_DELBUDDY, 0, buddyId+"");
//                            }
//                        });
//
//                    }else {
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                CommonFunction.showToast("解除好友失败");
//                            }
//                        });
//                    }
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFaild() {
//                CommonFunction.dismissProgressDialog();
//                showToast("解除好友失败");
//            }
//
//            @Override
//            public void onTimeout() {
//                CommonFunction.dismissProgressDialog();
//                showToast("解除好友失败\n" + "处理超时");
//            }
//        });

    }


    private void showCallDialog() {

        final String[] menuStr = new String[]{"呼叫"};
        if (menuStr == null || menuStr.length <= 0) {
            return;
        }

        if (operateListDialog == null) {
            operateListDialog = new OperateListDialog(getActivity());
            operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
        }
        operateItems.clear();

        int size = menuStr.length;
        for (int i = 0; i< size; i++) {
            final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
            item.setmItemNameStr(menuStr[i]);
            item.setmOperateKey(String.valueOf(i));

            item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
                @Override
                public void clickItem(int position) {
                    switch (Integer.valueOf(item.getmOperateKey())) {
                        case 0: {
                            onClickPhoneBtn();
                        }
                        break;

                        default:
                            break;
                    }

                    if (operateListDialog != null) {
                        operateListDialog.dismiss();
                    }
                }
            });
            operateItems.add(item);
        }

        operateListDialog.showTitle(false);
        operateListDialog.setGravityType(0); //居中显示
        operateListDialog.updateOperateItems(operateItems);
        operateListDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;
            case R.id.fragment_head1_finish:{
                showMoreOperationDialog();
            }break;
            case R.id.send_text_msg_btn:{
                sendIM(imBuddy.getSessionKey());
            }break;
            case R.id.voice_chat_btn:{
                showSipCallDialog();
            }break;
            case R.id.write_email:{

            }break;
            case R.id.add_friend_msg_btn:{
                onClickAddFriendBtn();
            }break;
            case R.id.buddyMobile:{
                showCallDialog();
            }break;
            default:
                break;
        }
    }

    private void onClickPhoneBtn(){
        if(!TextUtils.isEmpty(phoneStr)){
            //TODO 权限
            Uri uri = Uri.parse(String.format("tel:%s", phoneStr));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }else {
            CommonFunction.showToast("手机号为空时不能拨打电话");
        }
    }

    private void  onClickAddFriendBtn(){
        int mVerifyMstStatus = fetchVerifyMsgStatus();
        if(mVerifyMstStatus == 0){
            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ADD_FRIEND_VERIFY_MSG, buddyid+"");
        }else if(mVerifyMstStatus == 2){
            handleAcceptFriendRequest();
        }
    }

    public void handleAcceptFriendRequest() {
        String acceptMsg = DBConstant.SHOWTEXT_SYS_ACCEPT_ADDBUDDY;
        CommonFunction.showProgressDialog(getActivity(), "确认中...");
        imService.getContactManager().ReqAddBuddyAccept(buddyid, acceptMsg, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                CommonFunction.dismissProgressDialog();
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int result = imService.getContactManager().onRepAddBuddyAccept(rspObject);
                if(result == 0){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_GUARANTEEBUDDY, 0, buddyid+"");
                        }
                    });

                }else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonFunction.showToast("添加好友失败");
                        }
                    });
                }
            }

            @Override
            public void onTimeout() {
                CommonFunction.dismissProgressDialog();
            }

            @Override
            public void onFail(String error) {
                CommonFunction.dismissProgressDialog();
            }
        });
    }


    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_PERSONALINFO;
    }

    private void setData(){
        imBuddy = contactMgr.findContact(buddyid);
        if(buddyid == imService.getLoginManager().getLoginId()) {
            mFinish.setVisibility(View.GONE);
        }

        if(imBuddy != null){
            buddyAccount.setText(TextUtils.isEmpty(imBuddy.getUserCode()) ? "" : imBuddy.getUserCode());
            phoneStr = TextUtils.isEmpty(imBuddy.getPhone()) ? "" : imBuddy.getPhone();
            buddyMobile.setText(phoneStr);


            if(TextUtils.isEmpty(imBuddy.getNickName())){ //给好友添加备注
                buddyNickName.setVisibility(View.GONE);
            }else {
                buddyNickName.setVisibility(View.VISIBLE);
                buddyNickName.setText(imBuddy.getNickName());
            }

            buddyName.setVisibility(View.VISIBLE);
            if(TextUtils.isEmpty(imBuddy.getMainName())){ //好友昵称
                buddyName.setText(String.format("昵称：%s",imBuddy.getUserCode()));
            }else {
                buddyName.setText(String.format("昵称：%s",imBuddy.getMainName()));
            }

            if (TextUtils.isEmpty(imBuddy.getEmail())){
                ll_mail.setVisibility(View.GONE);
            }else{
                ll_mail.setVisibility(View.VISIBLE);
                buddyEmail.setText(imBuddy.getEmail());
            }

            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(buddyHeadIcon, imBuddy);
                }
            });

            if(!imBuddy.isFriend()){
                add_friend_msg_btn.setVisibility(View.VISIBLE);
                int mVerifyMstStatus = fetchVerifyMsgStatus();
                if(mVerifyMstStatus == 0){
                    add_friend_msg_btn.setText("添加到通讯录");
                }else if(mVerifyMstStatus == 2){
                    add_friend_msg_btn.setText("接受");
                }
                send_text_msg_btn.setVisibility(View.GONE);
                send_voice_msg_btn.setVisibility(View.GONE);
            }else {
                add_friend_msg_btn.setVisibility(View.GONE);
                send_text_msg_btn.setVisibility(View.VISIBLE);
                if(buddyid == imService.getLoginManager().getLoginId()) {
                    send_voice_msg_btn.setVisibility(View.GONE);
                }else {
                    send_voice_msg_btn.setVisibility(View.VISIBLE);
                }
            }
        }else {
            add_friend_msg_btn.setVisibility(View.VISIBLE);
            add_friend_msg_btn.setText("添加到通讯录");
            send_text_msg_btn.setVisibility(View.GONE);
            send_voice_msg_btn.setVisibility(View.GONE);
        }

    }

    // 0(wait for accept), 1(对方已同意)，  2(wait for me accept), 3(我已接受)
    private int fetchVerifyMsgStatus(){
        int mVerifyMstStatus = 0;
        BuddyVerifyMessage verifyMsg = BuddyVerifyMessage.getVerifyMsgBySessionId(buddyid);
        if(verifyMsg == null)
            return mVerifyMstStatus;

        int msgType = verifyMsg.getMsgType();
        if(verifyMsg.getFromId() == imService.getLoginManager().getLoginId()){
            if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST){
                mVerifyMstStatus = 0;
            }else if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT){
                mVerifyMstStatus = 3;
            }
        }else {
            if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST){
                mVerifyMstStatus = 2;
            }else if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT){
                mVerifyMstStatus = 1;
            }
        }

        return mVerifyMstStatus;
    }

}
