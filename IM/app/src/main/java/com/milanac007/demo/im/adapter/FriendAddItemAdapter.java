package com.milanac007.demo.im.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.MyFriendFragment;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.FriendAdapterListItem;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqguo on 2016/10/28.
 */
public class FriendAddItemAdapter  extends BaseAdapter {

    private LayoutInflater mInflater;
    private Activity mActivity;
    private List<FriendAdapterListItem> mFriendItems;
    private final BaseFragment mFragment;
    private  IMContactManager imService;
    private IMContactManager contactMgr;

    public FriendAddItemAdapter(BaseFragment fragment, IMContactManager contactMgr){
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mInflater = LayoutInflater.from(mActivity);
        mFriendItems = new ArrayList<>();
        this.contactMgr = contactMgr;
    }

    @Override
    public int getCount() {
        return mFriendItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriendItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendAddItemHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.friend_add_item, parent, false);
            holder = new FriendAddItemHolder(convertView);
            setViewHolder(convertView, holder);
        } else {
            holder = (FriendAddItemHolder) convertView.getTag();
        }
        setData(holder,position);
        setListener(holder, position);
        return convertView;
    }

    private void setListener(FriendAddItemHolder holder,int postion){
        final FriendAdapterListItem friend = mFriendItems.get(postion);
        holder.statusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //接受  可点击     其他都不可点击
                if(friend.getStatus() == 2) {
                    if(mFragment instanceof MyFriendFragment) {
                        MyFriendFragment myFriendFragment = (MyFriendFragment)mFragment;
                        myFriendFragment.handleAcceptFriendRequest(friend.getBuddyId());
                    }

                }
            }
        });
    }



    private void setData(FriendAddItemHolder holder,int position){
        FriendAdapterListItem friendItem = mFriendItems.get(position);
        UserEntity userEntity = contactMgr.findContact(friendItem.getBuddyId());
        App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                CommonFunction.setHeadIconImageView(holder.friendHeadImg, userEntity);
            }
        });
        holder.friendName.setText(userEntity.getMainName());
        if(!TextUtils.isEmpty(userEntity.getUserCode())) {
            holder.friendAccountId.setText(userEntity.getUserCode());
        }else {
            holder.friendAccountId.setText("");
        }

        // 0(wait for accept), 1(对方已同意)，  2(wait for me accept), 3(我已接受)
        if (friendItem.getStatus() == 2 || friendItem.getStatus() == 3) {
            holder.friendVerifyMsg.setText(friendItem.getVerifyMsg());
        }else if (friendItem.getStatus() == 0 || friendItem.getStatus() == 1) {
            holder.friendVerifyMsg.setText("您请求添加对方为合作伙伴");
        }

        //TODO 不同状态，按钮背景不同
        if (friendItem.getStatus() == 2) {  //接受
            holder.statusView.setText("接受");
            holder.statusView.setBackgroundResource(R.drawable.add_friend_bg);
            holder.statusView.setTextColor(mActivity.getResources().getColor(R.color.white));
            holder.statusView.setEnabled(true);
        }else if (friendItem.getStatus() == 1 || friendItem.getStatus() == 3) {  //已添加   不可点击
            holder.statusView.setText("已添加");
            holder.statusView.setBackgroundResource(R.drawable.add_friend_grey_bg);
            holder.statusView.setTextColor(mActivity.getResources().getColor(R.color.line_e3));
            holder.statusView.setEnabled(false);
        }else if (friendItem.getStatus() == 0) {  //等待验证  不可点击
            holder.statusView.setText("等待验证");
            holder.statusView.setBackgroundResource(R.drawable.add_friend_grey_bg);
            holder.statusView.setTextColor(mActivity.getResources().getColor(R.color.line_e3));
            holder.statusView.setEnabled(false);
        }

    }

    private void setViewHolder(View view,FriendAddItemHolder holder){
        holder.friendHeadImg = (CircleImageView) view.findViewById(R.id.friend_head_img);
        holder.friendName = (TextView) view.findViewById(R.id.friend_name_textview);
        holder.friendAccountId = (TextView) view.findViewById(R.id.item_account_textview);
        holder.friendAccountId.setTextColor(mActivity.getResources().getColor(R.color.label_green));
        holder.friendVerifyMsg = (TextView) view.findViewById(R.id.verify_msg_textview);
        holder.statusView = (TextView)view.findViewById(R.id.friend_status_textview);
    }

    class FriendAddItemHolder extends BaseViewHolder{

        CircleImageView friendHeadImg;
        TextView friendName;
        TextView friendAccountId;
        TextView friendVerifyMsg;
        TextView statusView;

        public FriendAddItemHolder(View view) {
            super(view);
        }
    }

    public void updateData(List<FriendAdapterListItem> listitems) {
        if (listitems != null) {
            mFriendItems.clear();
            mFriendItems.addAll(listitems);
        }
        notifyDataSetChanged();
    }

    public void updateData(int position, FriendAdapterListItem listitems) {
        if (position < 0 || position >= mFriendItems.size()) {
            return;
        }
        mFriendItems.set(position, listitems);
        notifyDataSetChanged();
    }

    public void appendData(List<FriendAdapterListItem> listitems) {
        if (listitems != null) {
            mFriendItems.addAll(listitems);
        }
        notifyDataSetChanged();
    }

    public void appendData(FriendAdapterListItem listitem) {
        if (listitem != null) {
            mFriendItems.add(listitem);
        }
        notifyDataSetChanged();
    }

    public void clearAll(){
        mFriendItems.clear();
        notifyDataSetChanged();
    }

    public void removeDataByPosition(int position) {
        if (position < 0 || position >= mFriendItems.size()) {
            return;
        }
        mFriendItems.remove(position);
        notifyDataSetChanged();
    }

    public void setDataByPosition(int position,FriendAdapterListItem listItem){
        if (position < 0 || position >= mFriendItems.size()) {
            return;
        }
        mFriendItems.set(position, listItem);
        notifyDataSetChanged();
    }

    public List<FriendAdapterListItem> getAllFriends(){
        return mFriendItems;
    }
}
