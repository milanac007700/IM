package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.milanac007.demo.im.interfaces.OnActionListener;
//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.DB.DBInterface;
//import com.mogujie.tt.DB.dao.UserDao;
//import com.mogujie.tt.protobuf.IMBuddy;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.manager.IMLoginManager;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.event.SelfInfoChangeEvent;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;

import java.io.IOException;

import de.greenrobot.event.EventBus;


/**
 * Created by zqguo on 2016/12/5.
 */
public class ModifyPersonalInfoFragment extends BaseFragment implements View.OnClickListener{

    private View mView;
    private EditText mEdit;
    private View mDelImg;
    private View mBack;
    private TextView mFinish;
    private int mBuddyid;
    private UserEntity imBuddy;
    private String mType = "username";

    private IMService imService;
    private UserEntity userEntity;
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
        if(bundle != null){
            mType = bundle.getString("type");
        }
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mofify_nickname_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_MODIFY_PERSONALINFO;
    }

    protected void setTitle(TextView mTitle){
        if(mType.equals("username")){
            mTitle.setText("更改昵称");
        }else if(mType.equals("phone")){
            mTitle.setText("更改手机号");
        }
    }

    protected void initView(View view){
        mView = view;
        view.findViewById(R.id.search_content_cancel).setVisibility(View.GONE);
        view.findViewById(R.id.search_content_img).setVisibility(View.GONE);

        mBack = view.findViewById(R.id.fragment_head1_back);

        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        setTitle(mTitle);

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        mFinish.setText("保存");

        mEdit = (EditText)view.findViewById(R.id.content_edittext);
        mEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if(mType.equals("username")){
            mEdit.setHint("请输入昵称");
        }else if(mType.equals("phone")){
            mEdit.setHint("请输入11位手机号");
        }

        InputFilter[] filters = {new InputFilter.LengthFilter(30)};  //最多30个字符
        mEdit.setFilters(filters);
        mEdit.requestFocus();
        autoKey();
        mDelImg = view.findViewById(R.id.del_content_img);

    }

    private void setData(){
        //TODO setdata
        if(Preferences.getCurrentLoginer() == null){
            CommonFunction.showToast("请登录后重试");
            return;
        }

        mBuddyid = Preferences.getCurrentLoginer().getUuid();
        imBuddy = imService.getContactManager().findContact(mBuddyid);
        if(imBuddy == null)
            return;

        if(mType.equals("username")){
            mEdit.setText(imBuddy.getMainName());
        }else if(mType.equals("phone")){
            mEdit.setText(imBuddy.getPhone());
        }

        mEdit.setSelection(mEdit.getText().length());
    }


    private void onFinishClick(){
        hideKey();
        final String inputStr = mEdit.getText().toString().trim();
        if(CommonFunction.isStringEmpty(inputStr)){
            if(mType.equals("username")){
                CommonFunction.showToast("昵称不能为空");
            }else if(mType.equals("phone")){
                CommonFunction.showToast("手机号不能为空");
            }
            return;
        }

        //TODO
//        IMBaseDefine.PreferenceType type = IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_PHONE;
//        if(mType.equals("username")){
//            type = IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_REMARK;
//        }else if(mType.equals("phone")){
//
//        }
//
//        CommonFunction.showProgressDialog(getActivity(), "提交中...");
//
//        imService.getContactManager().ReqChangeBuddyInfo(mBuddyid, type, CommonFunction.encode(inputStr), new Packetlistener() {
//            @Override
//            public void onSuccess(Object response) {
//                CommonFunction.dismissProgressDialog();
//                try{
//                    IMBuddy.IMChangeBuddyInfoRsp imChangeBuddyInfoRsp = IMBuddy.IMChangeBuddyInfoRsp.parseFrom((CodedInputStream) response);
//                    onRspChangeBuddyInfo(imChangeBuddyInfoRsp);
//                }catch (IOException e) {
//                    e.printStackTrace();
//                    CommonFunction.showToast("修改失败: " + e.getMessage());
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


    //TODO
//    public void onRspChangeBuddyInfo(IMBuddy.IMChangeBuddyInfoRsp imChangeBuddyInfoRsp){
//        int resultCode = imChangeBuddyInfoRsp.getResultCode(); //结果码，0:successed 1:failed
//        if(resultCode == 0 && imChangeBuddyInfoRsp.getChgUserId() == mBuddyid){
//            IMBaseDefine.PreferenceType type = imChangeBuddyInfoRsp.getInfoType();
//            String changeInfo = imChangeBuddyInfoRsp.getChangeInfo();
//
//            if(type == IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_REMARK){
//                imBuddy.setMainName(CommonFunction.decode(changeInfo));
//                DBInterface.instance().updateUserSingleColumn(mBuddyid, UserDao.Properties.Nickame, imBuddy.getMainName());
//            }else if(type == IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_PHONE){
//                imBuddy.setPhone(changeInfo);
//                DBInterface.instance().updateUserSingleColumn(mBuddyid, UserDao.Properties.Phone, changeInfo);
//            }
//            imService.getContactManager().putContact(imBuddy);
//
//            // UPDATE DB
//            if(type == IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_REMARK){
//                User.updateSingleColumn("mName", imBuddy.getMainName());
//            }else if(type == IMBaseDefine.PreferenceType.PREFERENCE_TYPE_BUDDY_PHONE){
//                User.updateSingleColumn("mTelephone", changeInfo);
//            }
//            User lastUser = User.getUserLastLogin();
//            User.setImConfigUser(lastUser);
//            Preferences.setUser(lastUser);
//
//            IMLoginManager.instance().setLoginInfo(imBuddy);
//
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
//                    EventBus.getDefault().post(new SelfInfoChangeEvent());
//                    CommonFunction.showToast("修改成功");
//                }
//            });
//
//        }else {
//            CommonFunction.showToast("修改失败: resultCode=" + resultCode);
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_head1_finish:{
                onFinishClick();
            }break;
            case R.id.fragment_head1_back:{
                hideKey();
                onBack();
            }break;
            case R.id.del_content_img:{//向左删除一个字符
                Editable searchEditable = mEdit.getText();
                int endIndex = mEdit.getSelectionStart();
                searchEditable.delete(0, endIndex);
            }break;
            default:break;
        }
    }

    public void setListener() {

        View[] views = {mBack, mDelImg, mFinish};
        for (View view : views) {
            view.setOnClickListener(this);
        }

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKey();
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String editStr = editable.toString();
                if (CommonFunction.isStringEmpty(editStr)) {
                    mDelImg.setVisibility(View.GONE);
                } else {
                    mDelImg.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
