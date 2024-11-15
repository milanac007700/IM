package com.milanac007.demo.im.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.GroupChatSettingFragment;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqguo on 2017/4/11.
 */
public class GroupMemAdapter extends BaseAdapter {

    private Activity mContext;
    private final LayoutInflater mInflater;
    public int mItemWidth;
    public int mItemHeight;
    private LinearLayout.LayoutParams mItemParams;
    private IMService imService;
    private List<GroupMemberEntity> groupMemberEntityList = new ArrayList<>();
    private final BaseFragment currentFragment;
    private boolean isAdmin;
    private int groupId;

    public GroupMemAdapter(BaseFragment fragment, IMService imService, int groupId, boolean isAdmin){
        super();
        currentFragment = fragment;
        mContext = currentFragment.getActivity();
        mInflater = LayoutInflater.from(mContext);
        this.imService = imService;
        this.groupId = groupId;
        this.isAdmin = isAdmin;

        int srceenWidth = CommonFunction.getWidthPx() - 2*CommonFunction.dip2px(10);
        mItemWidth = (srceenWidth - 4*CommonFunction.dip2px(10))/5; //间隔10个像素
        mItemHeight = mItemWidth + CommonFunction.dip2px(5);
    }

    public void updateData(){

        GroupEntity groupEntity = imService.getGroupManager().findGroup(groupId);
        if(groupEntity != null) {
            groupMemberEntityList.addAll(imService.getGroupManager().loadAllGroupMembersByGroupId(groupId));
            GroupMemberEntity addMemEntity = new GroupMemberEntity();
            addMemEntity.setPeerId(-1); //加人
            groupMemberEntityList.add(addMemEntity);
            if(isAdmin){
                GroupMemberEntity delMemEntity = new GroupMemberEntity();
                delMemEntity.setPeerId(-2); //减人
                groupMemberEntityList.add(delMemEntity);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return groupMemberEntityList.size();
    }

    @Override
    public GroupMemberEntity getItem(int position) {
        if(position < 0 || position > getCount())
            return null;

        return groupMemberEntityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView ==  null){
            convertView = mInflater.inflate(R.layout.group_member_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.member_ico = (CircleImageView) convertView.findViewById(R.id.group_member_ico);
            viewHolder.member_nickname = (TextView)convertView.findViewById(R.id.group_member_nickname);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(mItemParams == null) {
            mItemParams = new LinearLayout.LayoutParams(mItemWidth, mItemWidth);
            mItemParams.gravity = Gravity.CENTER_HORIZONTAL;
        }

        GroupMemberEntity item = getItem(position);
        int memberId = item.getPeerId();
        viewHolder.member_ico.setLayoutParams(mItemParams);
        if(memberId == -1){ //grp_add
            Bitmap cameraBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.add_user_for_group_chat);
            viewHolder.member_ico.setImageBitmap(cameraBitmap);
        }else  if(memberId == -2){ //grp_minus
            Bitmap cameraBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.grp_minus_on);
            viewHolder.member_ico.setImageBitmap(cameraBitmap);
        }else{
            UserEntity userEntity =  imService.getContactManager().findContact(item.getPeerId());
            if(userEntity != null){
                ViewHolder finalViewHolder = viewHolder;
                App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                    @Override
                    public void run() {
                        CommonFunction.setHeadIconImageView(finalViewHolder.member_ico, userEntity);
                    }
                });
                viewHolder.member_nickname.setText(!TextUtils.isEmpty(userEntity.getNickName()) ? userEntity.getNickName(): !TextUtils.isEmpty(item.getNickName()) ? item.getNickName() : userEntity.getMainName());
            }

        }

        setListener(viewHolder, memberId);
        return convertView;

    }

    private void setListener(final ViewHolder viewHolder, final int memberId){

        viewHolder.member_ico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentFragment instanceof GroupChatSettingFragment){
                    GroupChatSettingFragment fragment = (GroupChatSettingFragment)currentFragment;

                    if(memberId == -1){
                        fragment.modifyGroupMem(0);
                    }else if(memberId == -2){
                        fragment.modifyGroupMem(1);
                    }else {
                        fragment.fetchPersonalInfo(memberId);
                    }
                }
            }
        });
    }

    private static class ViewHolder{
        CircleImageView member_ico;
        TextView member_nickname;
    }
}

