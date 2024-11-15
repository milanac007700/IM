package com.milanac007.demo.im.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.DB.DBInterface;
//import com.mogujie.tt.DB.dao.UserDao;
//import com.mogujie.tt.protobuf.IMBuddy;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.utils.Preferences;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by milanac007 on 2016/11/27.
 */
public class ModifyBuddyExtraFragment extends BaseFragment implements View.OnClickListener{

    private UserEntity userEntity;
    private View mBack;
    private TextView mFinish;
    protected IMService imService;
    protected IMContactManager contactMgr;
    protected int loginer;

    private EditText remarkname_edit;
    private ImageView remarkname_img;
    private TextView emailaddress;
    private TextView phone_edit_default;
    private View phone_edit_default_layout;
    private View phoneAddLayout_1;
    private View phoneAddLayout_2;
    private EditText phone_edit_1;
    private EditText phone_edit_2;
    private ImageView phone_del_1;
    private ImageView phone_del_2;
    private ScrollView detail_scrollView;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            contactMgr = imService.getContactManager();
            loginer = imService.getLoginManager().getLoginId();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.personal_info_modify_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    protected void initView(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        mBack = view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText("设置邮箱和备注");

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setText("保存");
        mFinish.setVisibility(View.VISIBLE);
        mFinish.setGravity(Gravity.CENTER);
        mFinish.setTextColor(getActivity().getResources().getColor(R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, CommonFunction.dip2px(30));
        params.rightMargin = CommonFunction.dip2px(15);
        mFinish.setLayoutParams(params);
        mFinish.setMinWidth(CommonFunction.dip2px(75));
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.green_btn_style);
        mFinish.setBackgroundDrawable(drawable);

        remarkname_edit = (EditText)view.findViewById(R.id.remarkname_edit);
        remarkname_img = (ImageView)view.findViewById(R.id.remarkname_img);
        emailaddress = (TextView)view.findViewById(R.id.emailaddress);
        phone_edit_default = (TextView) view.findViewById(R.id.phone_edit_default);
        phone_edit_default_layout = view.findViewById(R.id.phone_edit_default_layout);
        phoneAddLayout_1 = view.findViewById(R.id.phoneAddLayout_1);
        phoneAddLayout_2 = view.findViewById(R.id.phoneAddLayout_2);

        phone_edit_1 = (EditText)view.findViewById(R.id.phone_edit_1);
        phone_edit_2 = (EditText)view.findViewById(R.id.phone_edit_2);
        phone_del_1 = (ImageView)view.findViewById(R.id.phone_del_1);
        phone_del_2 = (ImageView)view.findViewById(R.id.phone_del_2);

        detail_scrollView = (ScrollView)view.findViewById(R.id.detail_scrollView);
    }


    private void setListener(){
        View[] views = {mBack, mFinish, remarkname_img, phone_del_1, phone_del_2};
        for(View view : views){
            view.setOnClickListener(this);
        }
        remarkname_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){ //完成键
                    hideKey();
                    return true;
                }
                return false;
            }
        });


        phone_edit_1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){ //完成键
                    if(TextUtils.isEmpty(phone_edit_1.getText())){
                        hideKey();
                    }else {
                        phoneAddLayout_2.setVisibility(View.VISIBLE);
                        phone_edit_2.requestFocus();
                    }

                    return true;
                }
                return false;
            }
        });

        phone_edit_2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){ //完成键
                    hideKey();
                    String textStr = phone_edit_2.getText().toString();
                    if(TextUtils.isEmpty(textStr)){
                        phoneAddLayout_2.setVisibility(View.GONE);
                    }else {
                        phoneAddLayout_2.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });


        remarkname_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFinishEnabled();
                String editStr = s.toString();
                if (CommonFunction.isStringEmpty(editStr)) {
                    remarkname_img.setVisibility(View.GONE);
                } else {
                    remarkname_img.setVisibility(View.VISIBLE);
                }
            }
        });


        phone_edit_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFinishEnabled();
                String editStr = s.toString();
                if (CommonFunction.isStringEmpty(editStr)) {
                    phone_del_1.setVisibility(View.GONE);
                } else {
                    phone_del_1.setVisibility(View.VISIBLE);
                }
            }
        });

        phone_edit_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFinishEnabled();
                String editStr = s.toString();
                if (CommonFunction.isStringEmpty(editStr)) {
                    phone_del_2.setVisibility(View.GONE);
                } else {
                    phone_del_2.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setFinishEnabled(){
        if(!remarkname_edit.getText().toString().equals(userEntity.getNickName())
                || !TextUtils.isEmpty(phone_edit_1.getText()) || !TextUtils.isEmpty(phone_edit_2.getText())){
            mFinish.setEnabled(true);
        }else {
            mFinish.setEnabled(false);
        }
    }

    private void setData(){
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        String sessionKey = bundle.getString(DataConstants.BUDDY_ID);
        String[] sessionInfo = sessionKey.split("_");
        int type = Integer.parseInt(sessionInfo[0]);
        if(type == DBConstant.SESSION_TYPE_SINGLE){
            int peerId = Integer.parseInt(sessionInfo[1]);
            userEntity = contactMgr.findContact(peerId);
            if(userEntity != null){
                String nameStr = !TextUtils.isEmpty(userEntity.getNickName()) ? userEntity.getNickName() : userEntity.getMainName();
                remarkname_edit.setText(nameStr);
                emailaddress.setText(TextUtils.isEmpty(userEntity.getEmail()) ? "暂无" : userEntity.getEmail());
                phone_edit_default.setText(TextUtils.isEmpty(userEntity.getPhone()) ? "" : userEntity.getPhone());

                if(TextUtils.isEmpty(userEntity.getPhone())){
                    phone_edit_default_layout.setVisibility(View.GONE);
                }else {
                    phone_edit_default_layout.setVisibility(View.VISIBLE);
                }
                phoneAddLayout_1.setVisibility(View.VISIBLE);

                setFinishEnabled();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_head1_back:{
               onBack();
            }break;
            case R.id.fragment_head1_finish:{
                //TODO 保存修改
                onFinishClick();
            }break;
            case R.id.remarkname_img:{
                if(remarkname_edit.hasFocus()){
                    Editable editable = remarkname_edit.getText();
                    int endIndex = remarkname_edit.getSelectionStart();
                    editable.delete(0, endIndex);
                }else {
                    remarkname_edit.setText("");
                }
            }break;

            case R.id.phone_del_1:{
                if(phone_edit_1.hasFocus()){
                    Editable editable = phone_edit_1.getText();
                    int endIndex = phone_edit_1.getSelectionStart();
                    editable.delete(0, endIndex);
                }else {
                    phone_edit_1.setText("");
                }

            }break;
            case R.id.phone_del_2:{
                if(phone_edit_2.hasFocus()){
                    Editable editable = phone_edit_2.getText();
                    int endIndex = phone_edit_2.getSelectionStart();
                    editable.delete(0, endIndex);
                }else {
                    phone_edit_2.setText("");
                }
            }break;

        }
    }

    private void onFinishClick(){
        hideKey();

        //暂时只处理备注名
        String remarkName = remarkname_edit.getText().toString();
        if(userEntity.getNickName().equals(remarkName)){
            CommonFunction.showToast("备注名称没有变化");
           return;
        }

        //TODO
//        CommonFunction.showProgressDialog(getActivity(), "提交中...");
//        imService.getContactManager().ReqChangeBuddyInfo(userEntity.getPeerId(), IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_REMARK, CommonFunction.encode(remarkName), new Packetlistener() {
//            @Override
//            public void onSuccess(Object response) {
//                CommonFunction.dismissProgressDialog();
//                try{
//                    IMBuddy.IMChangeBuddyInfoRsp imChangeBuddyInfoRsp = IMBuddy.IMChangeBuddyInfoRsp.parseFrom((CodedInputStream) response);
//                    onRspChangeBuddyInfo(imChangeBuddyInfoRsp);
//
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mListener.OnAction(getPageNumber(), ImContainer.ACTION_RETURN, 0, null);
//                            EventBus.getDefault().post(new UserInfoEvent(UserInfoEvent.Event.USER_INFO_UPDATE));
//                            CommonFunction.showToast("修改成功");
//                        }
//                    });
//
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFaild() {
//                CommonFunction.dismissProgressDialog();
//                CommonFunction.showToast("修改失败");
//            }
//
//            @Override
//            public void onTimeout() {
//                CommonFunction.dismissProgressDialog();
//                CommonFunction.showToast("修改失败：超时");
//            }
//        });

    }


//    public void onRspChangeBuddyInfo(IMBuddy.IMChangeBuddyInfoRsp imChangeBuddyInfoRsp){
//        int resultCode = imChangeBuddyInfoRsp.getResultCode(); //结果码，0:successed 1:failed
//        if(resultCode == 0 && imChangeBuddyInfoRsp.getChgUserId() == userEntity.getPeerId()){
//            IMBaseDefine.PreferenceType type = imChangeBuddyInfoRsp.getInfoType();
//            String changeInfo = imChangeBuddyInfoRsp.getChangeInfo();
//
//            if(type == IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_REMARK){
//                userEntity.setNickName(CommonFunction.decode(changeInfo));
//                DBInterface.instance().updateUserSingleColumn(userEntity.getPeerId(), UserDao.Properties.Nickame, userEntity.getNickName());
//            }
//            imService.getContactManager().putContact(userEntity);
//
//        }else {
//            CommonFunction.showToast("修改失败");
//        }
//    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_MODIFY_BUDDY_EXTRA;
    }


}
