package com.milanac007.demo.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.List;

/**
 * Created by zqguo on 2017/4/11.
 */
public class GroupItemListAdapter extends SelectBaseAdapter {

    private Context context;
    private List<GroupEntity> mList;
    private LayoutInflater inflater;
    private BaseFragment mCurrentFragment;
    private IMService imService;
    private int mMode = R.id.NORMAL_MODE;

    public void setMode(int mode) {
        this.mMode = mode;
    }

    private boolean showUserCnt; //是否显示群成员数量

    public GroupItemListAdapter(BaseFragment fragment, IMService imService) {
        mCurrentFragment = fragment;
        this.context = fragment.getActivity();
        inflater = LayoutInflater.from(this.context);
        this.imService = imService;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public GroupEntity getItem(int position) {
        return mList.get(position);
    }

    public List<GroupEntity> getAllItems() {
        return mList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int updateNormalGroupData() {
        mList = imService.getGroupManager().getNormalGroupSortedList();
        if(mList == null || mList.isEmpty())
            return 0;

        showUserCnt = false;
        notifyDataSetChanged();
        return mList.size();
    }

    public void updateData() {
        mList = imService.getGroupManager().getAllGroupList();
        showUserCnt = true;
        notifyDataSetChanged();
    }


    public void bindData(List<GroupEntity> allGroups, List<PeerEntity> selectedGroup){
        if (allGroups != null) {
            mList = allGroups;
        }
        if (selectedGroup != null){
            clearSelectedObject();
            addSelectedObject(selectedGroup);
        }

        notifyDataSetChanged();
    }

    public void bindData(List<GroupEntity> allGroups, List<PeerEntity> selectedGroup, List<PeerEntity> disableEditGroups){

        bindData(allGroups,selectedGroup);

        if (disableEditGroups != null){
            clearDisableEditObject();
            addDisableEditObject(disableEditGroups);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.normal_group_item_layout, null);
            holder.group_item_name = (TextView) convertView.findViewById(R.id.group_item_name);
            holder.group_item_icon = (GlideImageView) convertView.findViewById(R.id.group_item_icon);
            holder.group_item_member_count = (TextView)convertView.findViewById(R.id.group_item_member_count);
            holder.select_state_box = (ImageView)convertView.findViewById(R.id.select_state_box);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GroupEntity groupEntity = getItem(position);
        if (groupEntity == null)
            return convertView;

        App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                CommonFunction.setHeadIconImageView(holder.group_item_icon, groupEntity);
            }
        });
        holder.group_item_name.setText(groupEntity.getMainName());
        if(showUserCnt){
            holder.group_item_member_count.setVisibility(View.VISIBLE);
            holder.group_item_member_count.setText("("+groupEntity.getUserCnt()+")");
        }else {
            holder.group_item_member_count.setVisibility(View.GONE);
        }
        initBoxView(position, holder.select_state_box);
        return convertView;
    }

    public void initBoxView(final int position, final ImageView select_box) {

        PeerEntity item = getItem(position);

        if(mMode == R.id.NORMAL_MODE){
            select_box.setVisibility(View.GONE);
        }else if(mMode == R.id.SINGLE_CHOICE_MODE){
            select_box.setVisibility(View.GONE);
        }else {
            select_box.setVisibility(View.VISIBLE);
            if (isObjectDisableEdit(item)){
                select_box.setImageResource(R.mipmap.add_user_forbiden);
            }else {
                if (isObjectSelected(item)) {
                    select_box.setImageResource(R.mipmap.checkbox_on);
                } else {
                    select_box.setImageResource(R.mipmap.checkbox_off);
                }
            }
        }
    }

    public void handleSingleSelectClick(final View convertView, final int position){
        if (convertView == null){
            return;
        }

        PeerEntity item = getItem(position);
        if (isObjectDisableEdit(item)){
            return;
        }

        clearSelectedObject();
        addSelectedObject(item);

    }

    public void handleMultiSelectClick(final View convertView, final int position){
        if (convertView == null){
            return;
        }

        PeerEntity item = getItem(position);
        if (isObjectDisableEdit(item)){
            return;
        }

        GroupItemListAdapter.ViewHolder holder = (GroupItemListAdapter.ViewHolder) convertView.getTag();

        if (isObjectSelected(item)) {
            holder.select_state_box.setImageResource(R.mipmap.checkbox_off);
            removeSelectedObject(item);
        } else {
            holder.select_state_box.setImageResource(R.mipmap.checkbox_on);
            addSelectedObject(item);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView group_item_name;
        GlideImageView group_item_icon;
        TextView group_item_member_count;
        ImageView select_state_box;
    }
}
