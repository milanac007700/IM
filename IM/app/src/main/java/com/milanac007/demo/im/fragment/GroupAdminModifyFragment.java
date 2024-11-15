package com.milanac007.demo.im.fragment;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
//import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.db.config.DataConstants;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 群管理员移交
 * Created by zqguo on 2017/4/27.
 */
public class GroupAdminModifyFragment extends ContactsBaseFragment {

    private int groupId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        groupId = Integer.parseInt(bundle.getString(DataConstants.BUDDY_ID));
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_ADMIN_MODIFY;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mFinish.setVisibility(View.GONE);

        mTitle.setText(getResources().getString(R.string.g_select_new_group_admin));

        search_layout.setVisibility(View.VISIBLE);
        mMyFriendLayout.setVisibility(View.GONE);
        mMyGroupChatLayout.setVisibility(View.GONE);

        isSingle = true;
        mAdapter.setMode(R.id.SINGLE_CHOICE_MODE);

    }

    protected void setData(){
        List<UserEntity> groupMemberList = imService.getGroupManager().getGroupMembers(groupId);
        UserEntity groupAdmin = imService.getContactManager().findContact(loginer);

        //隐藏字母索引和SideBar
        mAdapter.setShowCharIndex(false);
        setShowSideBar(true);
        groupMemberList.remove(groupAdmin);
        mAdapter.updateData(groupMemberList);

    }

    protected void showDialog(){

        final List<UserEntity> allSelectedList = (List<UserEntity>)mAdapter.allSelectedObject();
        if(allSelectedList == null || allSelectedList.isEmpty())
            return;

        CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mActivity);
        builder.setTitle("移交群主");
        final UserEntity imBuddy = allSelectedList.get(0);
        builder.setMessage(String.format("确定选择 %s 为新群主，您将自动放弃群主身份。", imBuddy.getMainName()));

        builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                CommonFunction.showProgressDialog(getActivity(), "正在转让");
                //TODO
//                imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_ADMIN_VALUE, String.valueOf(imBuddy.getPeerId()));

            }
        });

        builder.setNegativeBtn("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        CustomConfirmDialog dialog = builder.create();
        dialog.show();
    }


    public void onEventMainThread(GroupEvent event){
        if(event.getGroupId() != groupId)
            return;

        switch (event.getEvent()){
            case GROUP_INFO_UPDATED:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_ADMIN_VALUE && event.getOperateId() == loginer) {
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                    CommonFunction.showToast("转让成功");
                    setData();
                }
            }break;

            case GROUP_INFO_UPDATED_FAIL:
            case GROUP_INFO_UPDATED_TIMEOUT:{
                if(event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_ADMIN_VALUE && event.getOperateId() == loginer) {
                    CommonFunction.dismissProgressDialog();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                    CommonFunction.showToast("转让失败");
                    setData();
                }
            }break;
            case CHANGE_GROUP_MEMBER_SUCCESS:
                setData();
                break;

        }
    }

}
