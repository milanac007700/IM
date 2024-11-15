
package com.milanac007.demo.im.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.RecentInfo;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.milanac007.demo.im.service.IMService;

public class SessionListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<RecentInfo> recentSessionList = new ArrayList<>();
    private IMService imService;

    public SessionListAdapter(Context context, IMService imService) {
        this.context = context;
        inflater = LayoutInflater.from(this.context);
        this.imService = imService;
    }

    public SessionListAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    public void updateList(List<RecentInfo> recentInfoList){
        recentSessionList.clear();
        recentSessionList.addAll(recentInfoList);
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return recentSessionList.size();
    }

    @Override
    public Object getItem(int position) {
        return recentSessionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentInfo session = (RecentInfo) getItem(position);
        if (session.getSessionType()== DBConstant.SESSION_TYPE_SINGLE){
            return getIMSingleSessionView(convertView, session);
        } else if(session.getSessionType()==DBConstant.SESSION_TYPE_GROUP){
            return getIMGroupSessionView(convertView, session);
        }
        return null;
    }


    private View getIMSingleSessionView(View convertView, RecentInfo session){
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_session, null);
            holder = new UserViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof UserViewHolder){
                holder = (UserViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.listitem_session, null);
                holder = new UserViewHolder(convertView);
            }
        }

        setSessionData(session, holder);
        return convertView;
    }


    private View getIMGroupSessionView(View convertView, RecentInfo session){
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_listitem_session_layout, null);
            holder = new GroupViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof GroupViewHolder){
                holder = (GroupViewHolder)object;
            }else {
                holder = new GroupViewHolder(convertView);
            }
        }

        setSessionData(session, holder);
        return convertView;
    }


    private void setSessionData(RecentInfo session, ViewHolder holder) {
        if (imService == null || session == null)
            return;


        if(session.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
             UserEntity userEntity = imService.getContactManager().findContact(session.getPeerId());
            String nameStr = !TextUtils.isEmpty(userEntity.getNickName()) ? userEntity.getNickName() : !TextUtils.isEmpty(userEntity.getMainName()) ? userEntity.getMainName() : userEntity.getUserCode();
            holder.tv_session_name.setText(nameStr);
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.headerIco, userEntity);
                }
            });

        } else if(session.getSessionType() == DBConstant.SESSION_TYPE_GROUP){
            GroupEntity groupEntity = imService.getGroupManager().findGroup(session.getPeerId());
            holder.tv_session_name.setText(groupEntity.getMainName());
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.headerIco, groupEntity);
                }
            });
        }

        holder.ll_session_item.setVisibility(View.VISIBLE);
        if (session.getUnReadCnt() == 0) {
            holder.tv_unread.setVisibility(View.INVISIBLE);
            holder.tv_unread.setText("");
        } else {
            holder.tv_unread.setVisibility(View.VISIBLE);
            holder.tv_unread.setText(String.valueOf(session.getUnReadCnt()));
        }


        if(session != null){
            int msgType = session.getLatestMsgType();
            String showText = "";
            switch (msgType){
                case DBConstant.MSG_TYPE_SINGLE_AUDIO:
                    showText = DBConstant.DISPLAY_FOR_AUDIO;break;

                case DBConstant.MSG_TYPE_SINGLE_IMG:
                    showText = DBConstant.DISPLAY_FOR_IMAGE;break;

                case DBConstant.MSG_TYPE_SINGLE_VEDIO:
                    showText = DBConstant.DISPLAY_FOR_VIDEO;break;

                case DBConstant.MSG_TYPE_SINGLE_FILE:
                    showText = DBConstant.DISPLAY_FOR_FILE;break;
                default:
                    showText = TextUtils.isEmpty(session.getLatestMsgData()) ? "" : session.getLatestMsgData();
                    break;
            }
            holder.tv_last_msg.setText(showText);
            String dateTimeStr =  CommonFunction.getDisplayTimeFormat(new Date(session.getUpdateTime()));
            holder.tv_last_msg_time.setText(dateTimeStr);
            holder.tv_last_msg_time.setVisibility(View.VISIBLE);
        } else {
            holder.tv_last_msg.setText("没有新消息");
            holder.tv_last_msg_time.setVisibility(View.GONE);
        }
    }

    static abstract class ViewHolder {
        View ll_session_item;
        GlideImageView headerIco;
        TextView tv_session_name;
        TextView tv_last_msg_time;
        TextView tv_unread;
        TextView tv_last_msg;

        private ViewHolder(View convertView) {
            ll_session_item = convertView.findViewById(R.id.ll_session_item);
            tv_session_name = (TextView) convertView.findViewById(R.id.tv_session_name);
            tv_last_msg_time = (TextView) convertView.findViewById(R.id.tv_last_msg_time);
            tv_unread = (TextView) convertView.findViewById(R.id.tv_unread);
            tv_last_msg = (TextView)convertView.findViewById(R.id.tv_last_msg);
            headerIco = (GlideImageView)convertView.findViewById(R.id.iv_avatar);
            convertView.setTag(this);
        }
    }

    static class UserViewHolder extends ViewHolder {

        private UserViewHolder(View convertView) {
            super(convertView);
//            headerIco = (GlideImageView)convertView.findViewById(R.id.iv_avatar);
//            convertView.setTag(this);
        }
    }

    static class GroupViewHolder extends ViewHolder {

        private GroupViewHolder(View convertView) {
            super(convertView);
//            headerIco = (GlideImageView) convertView.findViewById(R.id.iv_avatar);
//            convertView.setTag(this);
        }
    }
    
}
