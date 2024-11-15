package com.milanac007.demo.im.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
//import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.db.config.DataConstants;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2017/4/13.
 */
public class GroupNoticeEditFragment extends BaseFragment implements View.OnClickListener{

    private View mView;
    private EditText mEdit;
    private View mBack;
    private TextView mFinish;
    private IMService imService;
    private int groupId;
    private TextView mTitle;
    private View group_admin_layout;
    private CircleImageView iv_photo;
    private TextView nick_name;
    private TextView updated_time;
    private TextView admin_label;
    private boolean isAdmin;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            setData();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };
    private View edit_mask;


    private void setData(){
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        groupId = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
        GroupEntity groupEntity = imService.getGroupManager().findGroup(groupId);

        isAdmin = groupEntity.getCreatorId() == imService.getLoginManager().getLoginId() ? true : false;

        if( TextUtils.isEmpty(groupEntity.getNotice())){
            group_admin_layout.setVisibility(View.GONE);
            edit_mask.setVisibility(View.GONE);
            mEdit.setText("");
            mEdit.requestFocus();
            autoKey();
        }else {
            mEdit.setText(groupEntity.getNotice());
            int len = mEdit.getText().length();
            mEdit.setSelection(len);
            mFinish.setEnabled(len > 0 ? true : false);
            edit_mask.setVisibility(View.VISIBLE);

            if(!isAdmin){
                admin_label.setVisibility(View.VISIBLE);
                mFinish.setVisibility(View.GONE);
            }else {
                admin_label.setVisibility(View.GONE);
                mFinish.setText("编辑");
            }

            group_admin_layout.setVisibility(View.VISIBLE);
            UserEntity groupAdmin =  imService.getContactManager().findContact(groupEntity.getCreatorId());
            GroupMemberEntity memberEntity = imService.getGroupManager().findGroupMember(groupId, groupAdmin.getPeerId());
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(iv_photo, groupAdmin);
                }
            });
            nick_name.setText(memberEntity.getNickName());
            updated_time.setText(""); //TODO
        }
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
        return inflater.inflate(R.layout.group_notice_edit_layout, null);
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
        mTitle.setText("群公告");

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        mFinish.setText("完成");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CommonFunction.dip2px(65), CommonFunction.dip2px(30));
        params.rightMargin = CommonFunction.dip2px(15);
        mFinish.setLayoutParams(params);
        Drawable drawable = getResources().getDrawable(R.drawable.green_btn_style);
        mFinish.setBackgroundDrawable(drawable);

        group_admin_layout = view.findViewById(R.id.group_admin_layout);
        iv_photo = (CircleImageView)view.findViewById(R.id.iv_photo);
        nick_name = (TextView)view.findViewById(R.id.nick_name);
        updated_time = (TextView)view.findViewById(R.id.updated_time);

        mEdit = (EditText)view.findViewById(R.id.group_notice_edit);
        InputFilter[] filters = {new InputFilter.LengthFilter(500)};  //最多500个字符
        mEdit.setFilters(filters);

        edit_mask = view.findViewById(R.id.edit_mask);
        admin_label = (TextView)view.findViewById(R.id.admin_label);
    }

    public void setListener() {

        View[] views = {mBack, mFinish};
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

        edit_mask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKey();
                return true; /*屏蔽edit的touch事件 */
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
                    mFinish.setEnabled(false);
                } else {
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
            default:break;
        }
    }

    private void onFinishClick(){
        if(mFinish.getText().toString().equals("编辑")){
            mFinish.setText("完成");
            group_admin_layout.setVisibility(View.GONE);
            edit_mask.setVisibility(View.GONE);
            mEdit.requestFocus();
            autoKey();
        }else {
            hideKey();
            final String inputStr = CommonFunction.encode(mEdit.getText().toString().trim());
            CommonFunction.showProgressDialog(getActivity(), "提交中...");
            //TODO
//            imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NOTICE_VALUE, inputStr);
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_NOTICE_EDIT;
    }

    public void onEventMainThread(GroupEvent event){
        if(event.getGroupId() != groupId)
            return;

        int loginId = imService.getLoginManager().getLoginId();

        switch (event.getEvent()){
            case GROUP_INFO_UPDATED:{
                if( event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NOTICE_VALUE && event.getOperateId() == loginId) {
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                    CommonFunction.showToast("群公告设置成功");
                }
            }break;
            case GROUP_INFO_UPDATED_FAIL:
            case GROUP_INFO_UPDATED_TIMEOUT:{
                if( event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NOTICE_VALUE && event.getOperateId() == loginId) {
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群公告设置失败");
                }
            }break;
        }
    }

}
