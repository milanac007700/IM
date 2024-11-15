package com.milanac007.demo.im.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.adapter.GroupMemAdapter;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.manager.IMSessionManager;
import com.milanac007.demo.im.db.sp.ConfigurationSp;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.ui.SwitchView;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2017/4/11.
 */
public class GroupChatSettingFragment extends BaseFragment implements View.OnClickListener, SwitchView.OnStateChangedListener {

    private View view1;
    private TextView mBack;
    private int groupId;
    private GridView member_item_gridview;
    private View group_name_layout;
    private View group_qrcode_layout;
    private View group_notice_layout;
    private View group_admin_layout;

    private SwitchView group_top_chat;
    private SwitchView change_to_normal_group;
    private View my_nickname_layout;
    private TextView my_nickname;
    private SwitchView show_member_nickname;
    private TextView del_all_group_chats;
    private TextView del_and_quit;
    private TextView group_name;
    private int loginer;
    private TextView notice_textview;
    private boolean isAdmin;

    private GroupEntity groupEntity;
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


    private void setData() {
        if(groupId == 0){
            Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
            groupId = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
        }

        groupEntity = imService.getGroupManager().findGroup(groupId);
        group_name.setText(TextUtils.isEmpty(groupEntity.getMainName()) ? "未命名" : groupEntity.getMainName());
        loginer = imService.getLoginManager().getLoginId();
        GroupMemberEntity memberEntity = imService.getGroupManager().findGroupMember(groupId, loginer);
        my_nickname.setText(TextUtils.isEmpty(memberEntity.getNickName()) ? "未命名" : memberEntity.getNickName());
        if(TextUtils.isEmpty(groupEntity.getNotice())){
            notice_textview.setVisibility(View.GONE);
        }else {
            notice_textview.setVisibility(View.VISIBLE);
            notice_textview.setText(groupEntity.getNotice());
        }

        boolean isShow = ConfigurationSp.instance(getActivity(), Preferences.getCurrentLoginer().getUuid()).isShowNick(groupId);
        show_member_nickname.toggleSwitch(isShow);

        boolean isNormalGroup = (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_NORMAL || groupEntity.getGroupType() == DBConstant.GROUP_TYPE_AUTH_NORMAL);
        change_to_normal_group.toggleSwitch(isNormalGroup);

        isAdmin = groupEntity.getCreatorId() == imService.getLoginManager().getLoginId() ? true : false;
        GroupMemAdapter adapter = new GroupMemAdapter(this, imService, groupId, isAdmin);
        member_item_gridview.setAdapter(adapter);
        adapter.updateData();

        if(isAdmin){
            group_admin_layout.setVisibility(View.VISIBLE);
        }else {
            group_admin_layout.setVisibility(View.GONE);
        }

        //需设置GridView的height
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) member_item_gridview.getLayoutParams();
        int rowNum = (int)Math.ceil(adapter.getCount() / 5.0); //行数  Math.ceil返回大于参数x的最小整数,即对浮点数向上取整.
        params.height = rowNum * adapter.mItemHeight + CommonFunction.dip2px(10) *(rowNum+1);//Math.ceil(x) 对一个数进行上取整, 4为padding
        member_item_gridview.setLayoutParams(params);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_chat_setting_layout, null);
    }

    protected void setTitle(TextView mTitle) {
        mTitle.setText("聊天设置");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view1 = view.findViewById(R.id.group_chat_setting_layout);
        mBack = (TextView) view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);

        member_item_gridview = (GridView)view.findViewById(R.id.member_item_gridview);
        group_name_layout = view.findViewById(R.id.group_name_layout);
        group_name = (TextView)view.findViewById(R.id.group_name);
        group_qrcode_layout = view.findViewById(R.id.group_qrcode_layout);
        group_notice_layout = view.findViewById(R.id.group_notice_layout);
        notice_textview = (TextView)view.findViewById(R.id.notice_textview);
        group_admin_layout = view.findViewById(R.id.group_admin_layout);
        my_nickname_layout = view.findViewById(R.id.my_nickname_layout);
        my_nickname = (TextView)view.findViewById(R.id.my_nickname);
        del_all_group_chats = (TextView)view.findViewById(R.id.del_all_group_chats);
        del_and_quit = (TextView)view.findViewById(R.id.del_and_quit);

        group_top_chat = (SwitchView)view.findViewById(R.id.group_top_chat);
        change_to_normal_group = (SwitchView)view.findViewById(R.id.change_to_normal_group);
        show_member_nickname = (SwitchView)view.findViewById(R.id.show_member_nickname);

        setTitle(mTitle);
        setListener();
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
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_CHAT_SETTING;
    }

    private void setListener() {
        View[] views = {mBack, group_name_layout, group_qrcode_layout, group_notice_layout, group_admin_layout,
                my_nickname_layout, del_all_group_chats, del_and_quit};
        for (View view1 : views) {
            view1.setOnClickListener(this);
        }

        view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        SwitchView[] switchViews = {change_to_normal_group, show_member_nickname, group_top_chat};
        for (SwitchView view : switchViews){
            view.setOnStateChangedListener(this);
        }

    }

    @Override
    public void toggleToOn(SwitchView view) {
        switch (view.getId()){
            case R.id.change_to_normal_group:
//                CommonFunction.showProgressDialog(getActivity(), "提交中...");
//                imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NORMAL_VALUE, String.valueOf(1));
                //TODO
                break;
            case R.id.show_member_nickname:
                setShowNickName(true);
                break;
            case R.id.group_top_chat:
                //TODO
                CommonFunction.showToast(R.string.func_developing);
                group_top_chat.toggleSwitch(!group_top_chat.isOpened());
                break;
            default:
                break;
        }
    }

    @Override
    public void toggleToOff(SwitchView view) {
        switch (view.getId()){
            case R.id.change_to_normal_group:
//                CommonFunction.showProgressDialog(getActivity(), "提交中...");
//                imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NORMAL_VALUE, String.valueOf(0));
                //TODO
                break;
            case R.id.show_member_nickname:
                setShowNickName(false);
                break;
            case R.id.group_top_chat:
                //TODO
                CommonFunction.showToast(R.string.func_developing);
                group_top_chat.toggleSwitch(!group_top_chat.isOpened());
                break;
            default:
                break;
        }
    }

    private void setShowNickName(boolean showNickName){
        imService.getConfigSp().setShowGroupNick(groupId, showNickName);
        show_member_nickname.toggleSwitch(!show_member_nickname.isOpened());
    }


    private void onClickDelChatsBtn() {

        CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mActivity);
        builder.setTitle("删除")
                .setMessage("确定删除群的聊天记录吗？");
        builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String sessionKey = DBConstant.SESSION_TYPE_GROUP + "_" + groupId;
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

    private void onClickGroupNoticeBtn(){
        if(isAdmin){
            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_NOTICE_EDIT, groupId+"");
        }else {
            if(TextUtils.isEmpty(groupEntity.getNotice())){
                CommonFunction.showToast("只有群主可以编辑群公告");
            }else {
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_NOTICE_EDIT, groupId+"");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_head1_back: {
                onBack();
            }
            break;
            case R.id.group_name_layout: {
                String sesstionKey = DBConstant.SESSION_TYPE_GROUP + "_" + groupId;
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_GROUP_NAME_SETTING, 0, sesstionKey);
            }break;
            case R.id.group_qrcode_layout:
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SHOW_QRCODE, 0, groupEntity.getSessionKey());
                break;
            case R.id.group_notice_layout:
                onClickGroupNoticeBtn();
                break;
            case R.id.group_admin_layout:
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_ADMIN, groupId+"");
                break;
            case R.id.my_nickname_layout: {
                String sesstionKey = DBConstant.SESSION_TYPE_SINGLE + "_" + groupId;
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_GROUP_NAME_SETTING, 0, sesstionKey);
            }break;
            case R.id.del_all_group_chats:
                onClickDelChatsBtn();
                break;
            case R.id.del_and_quit:
                delAndQuit();
                break;
            default:
                break;
        }
    }


    private void delAndQuit(){
        CommonFunction.showProgressDialog(getActivity(), "请稍后...");
        imService.getGroupManager().reqLeaveGroupMember(groupId, loginer);
    }

    /**
     *
     * @param type 0:add  1:del
     */
    public void modifyGroupMem(int type){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_CREATE_MODIFY_GROUP_MEM, type+"_"+groupId);
    }

    public void fetchPersonalInfo(int peerId){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, peerId+"");
    }

    public void onEventMainThread(GroupEvent event){
        if(event.getGroupId() != groupId)
            return;

        switch (event.getEvent()){
            case GROUP_INFO_UPDATED:{
                CommonFunction.dismissProgressDialog();
                setData();
            }break;
            case CHANGE_GROUP_MEMBER_SUCCESS:{
                CommonFunction.dismissProgressDialog();
                setData();
            }break;
            case GROUP_INFO_DISBANDED:{//退群
                if(event.getOperateId() == loginer){
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN_IM, 0, null); //直接回到消息列表
                }else {
                    setData();
                }
            }break;

            case GROUP_INFO_UPDATED_FAIL:
            case GROUP_INFO_UPDATED_TIMEOUT:{
                if(event.getOperateId() == loginer){
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群信息更新失败");
                }
            }break;
            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:{
                if(event.getOperateId() == loginer){
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群成员更新失败");
                }
            }break;
        }
    }

    public void onEventMainThread(UserInfoEvent event){
        switch (event.event) {
            case USER_INFO_UPDATE:
                setData();
        }
    }

}