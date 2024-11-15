package com.milanac007.demo.im.fragment;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.db.config.DataConstants;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/** 群组加减成员
 * Created by zqguo on 2017/4/13.
 */
public class ModifyGroupMemFragment extends ContactsBaseFragment {

    private int type; //0:添加;  1：减人
    private List<UserEntity> groupMemberList;
    private int groupId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        String sessionKey = bundle.getString(DataConstants.BUDDY_ID);
        String[] sessionInfo = sessionKey.split("_");
        type = Integer.parseInt(sessionInfo[0]);
        groupId = Integer.parseInt(sessionInfo[1]);
    }


    @Override
    protected void initView(View view) {
        super.initView(view);
        mTitle.setText(getResources().getString(R.string.g_create_group_chat));

        search_layout.setVisibility(View.VISIBLE);
        mMyFriendLayout.setVisibility(View.GONE);

        isSingle = false;
        mAdapter.setMode(R.id.MULTIPLE_CHOICE_MODE);

        if(type == 0){
            mMyGroupChatLayout.setVisibility(View.GONE);
        } else if(type == 1){
            Drawable drawable = getActivity().getResources().getDrawable(R.drawable.btn_red_style);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mFinish.setBackgroundDrawable(drawable);
            mMyGroupChatLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_CREATE_MODIFY_GROUP_MEM;
    }

    protected void onFinishClick(){

        if(!mAdapter.allSelectedObject().isEmpty()){
            showDialog();
        }
        setFinishText(false);
        mAdapter.notifyDataSetChanged();
    }

    protected void setFinishText(boolean isSingle){
        String str = null;
        if(type == 0){
            str = getString(R.string.g_confirm);
        } else if(type == 1){
            str = getString(R.string.del);
        }
        if(mAdapter.allSelectedObject().isEmpty()){
            mFinish.setText(str);
            mFinish.setEnabled(false);
        }else {
            mFinish.setText(String.format("%s(%d)", str,  mAdapter.allSelectedObject().size()));
            mFinish.setEnabled(true);
        }
    }

    protected void setData(){
        groupMemberList = imService.getGroupManager().getGroupMembers(groupId);

        if(type == 0){
            List<UserEntity> contactList = contactMgr.getContactSortedList();
            // 没有任何的联系人数据
            if (contactList.size() <= 0) {
                return;
            }
            mAdapter.bindData(contactList, null, groupMemberList);

        }else {//删人，排除自己
            UserEntity groupAdmin = imService.getContactManager().findContact(loginer);
            group_chat_img.setImageBitmap(BitmapFactory.decodeFile(groupAdmin.getAvatarLocalPath()));
            group_chat_name.setText(groupAdmin.getMainName());

            //隐藏字母索引和SideBar
            mAdapter.setShowCharIndex(false);
            setShowSideBar(false);
            groupMemberList.remove(groupAdmin);
            mAdapter.updateData(groupMemberList);
        }

    }

    protected void showDialog(){

        final List<UserEntity> allSelectedList = (List<UserEntity>)mAdapter.allSelectedObject();
        if(allSelectedList == null)
            return;

        CommonFunction.showProgressDialog(getActivity(), "处理中...");
        List<Integer> ids = new ArrayList<>();
        for(UserEntity user : allSelectedList){
            ids.add(user.getPeerId());
        }

        if(type == 0) {
            imService.getGroupManager().reqAddGroupMember(groupId, ids);
        }else {
            imService.getGroupManager().reqRemoveGroupMember(groupId, ids);
        }
    }


    public void onEventMainThread(GroupEvent event){
        if(event.getGroupId() != groupId)
            return;

        switch (event.getEvent()){
            case CHANGE_GROUP_MEMBER_SUCCESS:
                if(event.getOperateId() == loginer){
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast(R.string.change_group_member_success);
                    onBack();
                }else {
                    setData();
                }
                break;

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:
                if(event.getOperateId() == loginer){
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast(R.string.change_group_member_failed);
                }
                break;

        }
    }

    public void onEventMainThread(UserInfoEvent event){
        switch (event.event) {
            case USER_INFO_UPDATE:
                setData();
        }
    }

}
