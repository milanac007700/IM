package com.milanac007.demo.im.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
//import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;

import de.greenrobot.event.EventBus;

/** 设置 群名称/自己的昵称
 * Created by zqguo on 2017/4/12.
 */
public class GroupNameModifyFragment extends BaseFragment implements View.OnClickListener{

    private View mView;
    private EditText mEdit;
    private View mDelImg;
    private View mBack;
    private TextView mFinish;
    private IMService imService;
    private int groupId;
    private int loginerId;
    private TextView mTitle;
    private int modifyType;
    private TextView tip_label;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            loginerId = imService.getLoginManager().getLoginId();
            setData();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    private void setData(){

        String sessionKey = bundle.getString("sessionKey");
        String[] sessionInfo = sessionKey.split("_");
        modifyType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);

        if(modifyType == DBConstant.SESSION_TYPE_SINGLE){ //个人群昵称
            GroupMemberEntity groupMember =  imService.getGroupManager().findGroupMember(peerId, loginerId);
            mEdit.setText(groupMember == null ? "" : groupMember.getNickName());
            mTitle.setText("我在本群的昵称");
            tip_label.setText("设置你在群里的昵称，这个昵称只会在这个群内显示");
            tip_label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }else { //群名片
            GroupEntity groupEntity = imService.getGroupManager().findGroup(peerId);
            mEdit.setText(groupEntity == null ? "" : groupEntity.getMainName());
        }

        groupId = peerId;
        int len = mEdit.getText().length();
        mEdit.setSelection(len);
        mFinish.setEnabled(len > 0 ? true : false);
    }

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
        return inflater.inflate(R.layout.group_name_modify_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    private void initView(View view){
            mView = view;
            mBack = view.findViewById(R.id.fragment_head1_back);
             mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText("群名片");

            mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
            mFinish.setVisibility(View.VISIBLE);
            mFinish.setText("保存");

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CommonFunction.dip2px(65), CommonFunction.dip2px(30));
            params.rightMargin = CommonFunction.dip2px(15);
            mFinish.setLayoutParams(params);
            Drawable drawable = getResources().getDrawable(R.drawable.green_btn_style);
            mFinish.setBackgroundDrawable(drawable);

            tip_label = (TextView)view.findViewById(R.id.tip_label);
            mEdit = (EditText)view.findViewById(R.id.remarkname_edittext);

            InputFilter[] filters = {new InputFilter.LengthFilter(30)};  //最多30个字符
            mEdit.setFilters(filters);
            mEdit.requestFocus();
            autoKey();
            mDelImg = view.findViewById(R.id.remarkname_img);
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
                    mFinish.setEnabled(false);
                } else {
                    mDelImg.setVisibility(View.VISIBLE);
                    mFinish.setEnabled(true);
                }
            }
        });
    }

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
            case R.id.remarkname_img:{//全清
                Editable searchEditable = mEdit.getText();
                int endIndex = mEdit.getSelectionStart();
                searchEditable.delete(0, endIndex);
            }break;
            default:break;
        }
    }

    private void onFinishClick(){
        hideKey();
         String inputStr = mEdit.getText().toString().trim();

        if(CommonFunction.isStringEmpty(inputStr)){
            if(modifyType == DBConstant.SESSION_TYPE_SINGLE){//自己的群昵称
                CommonFunction.showToast("昵称不能为空");
            }else { //群名称
                CommonFunction.showToast("群名称不能为空");
            }
            return;
        }

        CommonFunction.showProgressDialog(getActivity(), "提交中...");
        //TODO
        if(modifyType == DBConstant.SESSION_TYPE_SINGLE){//自己的群昵称
//            imService.getGroupManager().reqModifyGroupMemberInfo(groupId, loginerId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NICK_VALUE, CommonFunction.encode(inputStr));
        }else { //群名称
//            imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NAME_VALUE, CommonFunction.encode(inputStr));
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_NAME_SETTING;
    }

    public void onEventMainThread(GroupEvent event){
        if(event.getGroupId() != groupId)
            return;

        switch (event.getEvent()){
            case GROUP_INFO_UPDATED:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NAME_VALUE && event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                    CommonFunction.showToast("群名称修改成功");
                }
            }break;

            case CHANGE_GROUP_MEMBER_SUCCESS:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NICK_VALUE &&  event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                    CommonFunction.showToast("修改成功");
                }else {
                    setData();
                }
            }break;

            case GROUP_INFO_UPDATED_FAIL:
            case GROUP_INFO_UPDATED_TIMEOUT:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NAME_VALUE && event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群名称修改失败");
                }
            }break;

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NICK_VALUE && event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("修改失败");
                }
            }break;
        }
    }

}
