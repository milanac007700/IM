package com.milanac007.demo.im.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.ui.PinnedSectionListView;
import com.milanac007.demo.im.utils.IMUIHelper;
import com.milanac007.demo.im.utils.pinyin.PinYin;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.fragment.SearchFragment;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.GlideImageView;
//import com.mogujie.tt.imservice.entity.CallMessage;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

import static com.milanac007.demo.im.adapter.SearchAdapter.SearchItem.ITEM;
import static com.milanac007.demo.im.adapter.SearchAdapter.SearchItem.SECTION;
import static com.milanac007.demo.im.adapter.SearchAdapter.SearchType.GROUP;
import static com.milanac007.demo.im.adapter.SearchAdapter.SearchType.USER;


/**
 * 列表的顺序是： 用户-->群组
 */
public class SearchAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter,AdapterView.OnItemClickListener{

    private Logger logger = Logger.getLogger();
    private final LayoutInflater inflater;
    private Context ctx;
    private final Fragment currentFragment;
    private IMService imService;
    private List<SearchItem> mSearchItemList = new ArrayList<>();
    private String searchKey;

    public enum SearchType{
        USER,
        GROUP,
        //        DEPT,
        ILLEGAL
    }

    public static class SearchItem {

        public static final int SECTION = 1;
        public static final int ITEM = 0;

        public int type;
        public SearchType searchType;
        public  Object item;

        public SearchItem(int type, SearchType searchType) {
            this.type = type;
            this.searchType = searchType;
        }

        public SearchItem(int type, SearchType searchType, Object item) {
            this.type = type;
            this.searchType = searchType;
            this.item = item;
        }
    }


    @Override
    public int getItemViewType(int position) {
        return (getItem(position)).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == SearchItem.SECTION;
    }

    public boolean isSetionItem(int position) {
        return getItemViewType(position) == SearchItem.SECTION;
    }


    public SearchAdapter(Fragment fragment, IMService pimService){
        this.ctx = fragment.getActivity();
        currentFragment = fragment;
        inflater = LayoutInflater.from(this.ctx);
        this.imService = pimService;
    }

    public void clear(){
        if(mSearchItemList != null)
            mSearchItemList.clear();
        notifyDataSetChanged();
    }

    public void putUserList(List<UserEntity> pUserList){
        if(pUserList == null || pUserList.size() <=0){
            return;
        }

        SearchItem sectionItem = new SearchItem(SearchItem.SECTION, SearchType.USER);
        mSearchItemList.add(sectionItem);
        for(UserEntity userEntity : pUserList){
            mSearchItemList.add(new SearchItem(SearchItem.ITEM, SearchType.USER, userEntity));
        }
    }

    public void putGroupList(List<GroupEntity> pGroupList){

        if(pGroupList == null || pGroupList.size() <=0){
            return;
        }
        SearchItem sectionItem = new SearchItem(SearchItem.SECTION, SearchType.GROUP);
        mSearchItemList.add(sectionItem);
        for(GroupEntity groupEntity : pGroupList){
            mSearchItemList.add(new SearchItem(SearchItem.ITEM, SearchType.GROUP, groupEntity));
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(isSetionItem(position))
            return;

        SearchItem searchItem = getItem(position);

        if(currentFragment instanceof SearchFragment) {
            SearchFragment searchFragment = (SearchFragment) currentFragment;

            if (searchItem.item instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) searchItem.item;
                searchFragment.sendIM(userEntity.getSessionKey());

            } else if (searchItem.item instanceof GroupEntity) {
                GroupEntity groupEntity = (GroupEntity) searchItem.item;
                searchFragment.sendIM(groupEntity.getSessionKey());
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mSearchItemList == null ? 0 : mSearchItemList.size();
    }

    @Override
    public SearchItem getItem(int position) {
        return mSearchItemList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        PeerEntityViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_item_layout, null);
            holder = new PeerEntityViewHolder();
            holder.search_item_layout = convertView.findViewById(R.id.search_item_layout);
            holder.setionView = (TextView)convertView.findViewById(R.id.setionView);
            holder.userAvatar = (CircleImageView)convertView.findViewById(R.id.userAvatar);
            holder.groupAvatar = (GlideImageView) convertView.findViewById(R.id.groupAvatar);
            holder.peerEntityNameTextView = (TextView) convertView.findViewById(R.id.peerEntityNameTextView);
            holder.remarkTextView = (TextView) convertView.findViewById(R.id.remarkTextView);
            convertView.setTag(holder);
        } else {
            Object object = convertView.getTag();
            if(object instanceof PeerEntityViewHolder){
                holder = (PeerEntityViewHolder)object;
            }else {
                holder = new PeerEntityViewHolder();
                convertView = inflater.inflate(R.layout.search_item_layout, null);
                holder.search_item_layout = convertView.findViewById(R.id.search_item_layout);
                holder.setionView = (TextView)convertView.findViewById(R.id.setionView);
                holder.userAvatar = (CircleImageView)convertView.findViewById(R.id.userAvatar);
                holder.peerEntityNameTextView = (TextView) convertView.findViewById(R.id.peerEntityNameTextView);
                holder.remarkTextView = (TextView) convertView.findViewById(R.id.remarkTextView);
                convertView.setTag(holder);
            }
        }

        if(imService == null)
            return convertView;

        SearchItem searchItem =  getItem(position);

        if(isSetionItem(position)){
            holder.setionView.setVisibility(View.VISIBLE);
            holder.search_item_layout.setVisibility(View.GONE);
            String searchStr = searchItem.searchType == SearchType.USER ? "联系人" : "群聊";
            holder.setionView.setText(searchStr);
            return convertView;
        }

        holder.setionView.setVisibility(View.GONE);
        holder.search_item_layout.setVisibility(View.VISIBLE);

        if (searchItem.item instanceof UserEntity) {
            UserEntity peerEntity = (UserEntity) searchItem.item;
            holder.userAvatar.setVisibility(View.VISIBLE);
            holder.groupAvatar.setVisibility(View.GONE);
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.userAvatar, peerEntity);
                }
            });
            String nameStr = !TextUtils.isEmpty(peerEntity.getNickName()) ? peerEntity.getNickName() : !TextUtils.isEmpty(peerEntity.getMainName()) ? peerEntity.getMainName() : peerEntity.getUserCode();
            holder.peerEntityNameTextView.setText(nameStr);

            int type = FindSearchKey(peerEntity, searchKey);
            if(type == 0){ //nickName
                holder.remarkTextView.setVisibility(View.GONE);
                IMUIHelper.setTextHilighted(holder.peerEntityNameTextView, nameStr, peerEntity.getSearchElement());

            }else if(type == 1){ //mainName
                if(!TextUtils.isEmpty(peerEntity.getNickName())){
                    holder.remarkTextView.setVisibility(View.VISIBLE);
                    String remarkStr = "昵称: ";
                    IMUIHelper.setTextHilighted(holder.remarkTextView, peerEntity.getMainName(), remarkStr, peerEntity.getSearchElement());
                }else {
                    holder.remarkTextView.setVisibility(View.GONE);
                    IMUIHelper.setTextHilighted(holder.peerEntityNameTextView, nameStr, peerEntity.getSearchElement());
                }

            }else if(type == 2){ //userCode
                if(!TextUtils.isEmpty(peerEntity.getNickName()) || !TextUtils.isEmpty(peerEntity.getMainName())){
                    holder.remarkTextView.setVisibility(View.VISIBLE);
                    String remarkStr = "IM账号: ";
                    IMUIHelper.setTextHilighted(holder.remarkTextView, peerEntity.getUserCode(), remarkStr, peerEntity.getSearchElement());
                }else {
                    holder.remarkTextView.setVisibility(View.GONE);
                    IMUIHelper.setTextHilighted(holder.peerEntityNameTextView, nameStr, peerEntity.getSearchElement());
                }
            }
        }  else if (searchItem.item instanceof GroupEntity) {
            GroupEntity peerEntity = (GroupEntity) searchItem.item;

            holder.groupAvatar.setVisibility(View.VISIBLE);
            holder.userAvatar.setVisibility(View.GONE);
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.groupAvatar, peerEntity);
                }
            });
            String groupNameStr = String.format("%s(%d)", peerEntity.getMainName(), peerEntity.getUserCnt());
            holder.peerEntityNameTextView.setText(groupNameStr);


            int type = FindSearchKey(peerEntity, searchKey);
            if(type == 3){ //groupMainName
                holder.remarkTextView.setVisibility(View.GONE);
                IMUIHelper.setTextHilighted(holder.peerEntityNameTextView, groupNameStr, peerEntity.getSearchElement());
            }else {

                for(Integer memberId : peerEntity.getlistGroupMemberIds()){
                    UserEntity userEntity = imService.getContactManager().findContact(memberId);
                    if(userEntity == null)
                        continue;

                    int result = FindSearchKey(userEntity, searchKey.toUpperCase());
                    String remarkStr = "包含: ";
                    if(result == 0){
                        holder.remarkTextView.setVisibility(View.VISIBLE);
                        IMUIHelper.setTextHilighted(holder.remarkTextView, userEntity.getNickName(), remarkStr, userEntity.getSearchElement());
                    }else if(result == 1){
                        holder.remarkTextView.setVisibility(View.VISIBLE);
                        IMUIHelper.setTextHilighted(holder.remarkTextView, userEntity.getMainName(), remarkStr, userEntity.getSearchElement());
                    }else if(result == 2){
                        holder.remarkTextView.setVisibility(View.VISIBLE);
                        IMUIHelper.setTextHilighted(holder.remarkTextView, userEntity.getUserCode(), remarkStr, userEntity.getSearchElement());
                    }

                }

            }
        }

        return convertView;
    }


    private boolean isInTokenPinyinList(PinYin.PinYinElement pinYinElement, String searchKey){
        String upperCaseKey = searchKey.toUpperCase();
        int tokenCnt =pinYinElement.tokenPinyinList.size();
        for (int i = 0; i < tokenCnt; ++i) {
            String tokenPinyin = pinYinElement.tokenPinyinList.get(i);
            if (tokenPinyin.startsWith(upperCaseKey)) {
                return true;
            }
        }
        return false;
    }
    /**
     *
     * @param peerEntity
     * @param searchKey
     * @return 0: nickName匹配; 1:MainName  2:userCode  3:groupMainName,  -1无匹配
     */
    private int FindSearchKey(PeerEntity peerEntity, String searchKey){
        String upperCaseKey = searchKey.toUpperCase();

        if(peerEntity instanceof UserEntity){
            UserEntity userEntity = (UserEntity)peerEntity;
            if(!TextUtils.isEmpty(userEntity.getNickName()) &&
                    (userEntity.getNickNamePinyinElement().tokenFirstChars.contains(upperCaseKey) ||
                            isInTokenPinyinList(userEntity.getNickNamePinyinElement(), upperCaseKey) ||
                            userEntity.getNickName().toUpperCase().contains(upperCaseKey))){
                return 0;
            }

            if(!TextUtils.isEmpty(userEntity.getMainName()) &&
                    (userEntity.getPinyinElement().tokenFirstChars.contains(upperCaseKey) ||
                            isInTokenPinyinList(userEntity.getPinyinElement(), upperCaseKey) ||
                            userEntity.getMainName().toUpperCase().contains(upperCaseKey))){
                return 1;
            }

            if(!TextUtils.isEmpty(userEntity.getUserCode()) && userEntity.getUserCode().toUpperCase().contains(upperCaseKey)){
                return 2;
            }
        }else if(peerEntity instanceof GroupEntity){
            GroupEntity groupEntity = (GroupEntity)peerEntity;
            if(!TextUtils.isEmpty(groupEntity.getMainName()) &&
                    (groupEntity.getPinyinElement().tokenFirstChars.contains(upperCaseKey) ||
                            isInTokenPinyinList(groupEntity.getPinyinElement(), upperCaseKey) ||
                            groupEntity.getMainName().toUpperCase().contains(upperCaseKey))){
                return 3;
            }

        }

        return -1;
    }


    private static class PeerEntityViewHolder {
        View search_item_layout;
        TextView setionView;
        CircleImageView userAvatar;
        GlideImageView groupAvatar;
        TextView peerEntityNameTextView;
        TextView remarkTextView;
    }

    /**---------------------------set/get--------------------------*/
    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}
