package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.view.View;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.RecentInfo;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.ui.ISelectUserHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqguo on 2017/10/24.
 */

public class TransmitMsgFragment extends ContactsBaseFragment implements ISelectUserHandler{
    @Override
    protected void initView(View view) {
        super.initView(view);
        mTitle.setText(getResources().getString(R.string.g_transmit));

        search_layout.setVisibility(View.VISIBLE);
        my_friend_img.setVisibility(View.GONE);
        my_friend_name.setText("从通讯录中选择");
        group_chat_img.setVisibility(View.GONE);
        group_chat_name.setText("从群组中选择");

        isSingle = false;
        mAdapter.setMode(R.id.MULTIPLE_CHOICE_MODE);
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_TRANSMIT;
    }

    @Override
    protected void onFinishClick(){
        if(!mAdapter.allSelectedObject().isEmpty()){
            showDialog();
        }
        setFinishText(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void setFinishText(boolean isSingle){
        if(isSingle){
            mFinish.setVisibility(View.GONE);
        }else {
            mFinish.setVisibility(View.VISIBLE);
            String str = getString(R.string.g_confirm);
            if(mAdapter.allSelectedObject().isEmpty()){
                mFinish.setText(str);
                mFinish.setEnabled(false);
            }else {
                mFinish.setText(String.format("%s(%d)", str,  mAdapter.allSelectedObject().size()));
                mFinish.setEnabled(true);
            }
        }
    }

    @Override
    protected void onMyFriendClick() {
        this.mListener.OnAction(this.getPageNumber(), OnActionListener.Action.ACTION_SELECT_BUDDY, 0, TransmitMsgFragment.class.getName());
    }


    @Override
    protected void onMyGroupChatClick() {
        this.mListener.OnAction(this.getPageNumber(), OnActionListener.Action.ACTION_SELECT_GROUP_CHAT, 0, TransmitMsgFragment.class.getName());
    }

    @Override
    protected void setData() {
        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();
        List<PeerEntity> recentList = new ArrayList<>();
        for(RecentInfo recentInfo : recentSessionList){
            int sessionType = recentInfo.getSessionType();
            if(sessionType == DBConstant.SESSION_TYPE_SINGLE){
                UserEntity user = imService.getContactManager().findContact(recentInfo.getPeerId());
                recentList.add(user);
            }else if(sessionType == DBConstant.SESSION_TYPE_GROUP){
                GroupEntity group = imService.getGroupManager().findGroup(recentInfo.getPeerId());
                recentList.add(group);
            }
        }

        List<PeerEntity> allSelectedItem = new ArrayList<>();
        allSelectedItem.addAll((List<PeerEntity>)mAdapter.allSelectedObject());
        mAdapter.bindData(recentList, allSelectedItem);

        //隐藏字母索引和SideBar
        mAdapter.setShowCharIndex(false);
        setShowSideBar(false);
        setFinishText(isSingle);
    }

    @Override
    public void handleSelectedUser(ArrayList<? extends Object> selectedItems, Boolean isChanged, Bundle extraData) {
        if(selectedItems == null)
            return;

        mAdapter.addSelectedObject(selectedItems);
        setData();
    }

    public List<PeerEntity> allSelectedEntity(){
        if(mAdapter == null)
            return null;

        return (List<PeerEntity>)mAdapter.allSelectedObject();
    }
}
