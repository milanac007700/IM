package com.milanac007.demo.im.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.ContactsFragmentNew;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.stickylist.StickyListHeadersAdapter;
import com.milanac007.demo.im.utils.CommonFunction;
//import com.milanac007.demo.im.service.IMService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zqguo on 2016/9/14.
 */
public class AddUserAdapter extends SelectBaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
    private static final String TAG = "AddUserAdapter";
    protected List<PeerEntity> mPeerList;

    protected Character[] mSectionLetters;
    protected int[] mSectionIndices;
    public Activity mActivity;
    protected LayoutInflater inflater;
    protected boolean mShowCharIndex = true; //是否显示索引,默认显示

    private int mMode = R.id.NORMAL_MODE;
    protected ChineseCharComparator mCmp = new ChineseCharComparator();
    protected BaseFragment currentFragment;
    protected IMService imService;
    protected Logger logger = Logger.getLogger();

    public void setMode(int mode){
        mMode = mode;
    }

    public int getMode(){
        return mMode;
    }

    public void setShowCharIndex(boolean mShowCharIndex) {
        this.mShowCharIndex = mShowCharIndex;
    }

    public AddUserAdapter(BaseFragment baseFragment, IMService imService) {
        currentFragment = baseFragment;
        mActivity = baseFragment.getActivity();
        inflater = LayoutInflater.from(mActivity);
        mPeerList = new ArrayList<>();
        this.imService = imService;
    }

    @Override
    public int getCount() {
        return mPeerList.size();
    }

    @Override
    public PeerEntity getItem(int position) {
        return mPeerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        return renderPeerView(position, convertView, viewGroup);
    }

    public View renderPeerView(int position, View convertView, ViewGroup viewGroup){
        ContactViewHolder holder = null;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.contacts_list_item, viewGroup, false);
            holder = new ContactViewHolder(convertView);
        }else {
            holder = (ContactViewHolder)convertView.getTag();
        }

        setParam(position, holder);
        return convertView;
    }

    protected void setParam(int position, ContactViewHolder holder) {
        final PeerEntity peerEntity = getItem(position);
        if(peerEntity instanceof UserEntity){
            final UserEntity person = (UserEntity) peerEntity;
            String nameStr = !TextUtils.isEmpty(person.getNickName()) ? person.getNickName() : !TextUtils.isEmpty(person.getMainName()) ? person.getMainName() : person.getUserCode();
            holder.contact_name.setText(nameStr);

            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.contact_img, person);
                }
            });

            holder.contact_email_address.setText(person.getEmail());

            setCallIconListener(holder.call_icon, person.getPeerId());
            initBoxView(position, holder.contact_box, holder.call_icon);
        }else if(peerEntity instanceof GroupEntity){
            final GroupEntity group = (GroupEntity) peerEntity;
            String nameStr = !TextUtils.isEmpty(group.getDisplayName()) ? group.getDisplayName() :group.getMainName();
            String groupUserCnt = String.format("(%s人)", group.getUserCnt());
            holder.contact_name.setText(nameStr + groupUserCnt);

            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.contact_img, group);
                }
            });

            holder.contact_email_address.setVisibility(View.GONE);
            holder.call_icon.setVisibility(View.GONE);
            initBoxView(position, holder.contact_box, holder.call_icon);
        }
    }

    private void setCallIconListener(View callIcon, final int peerId){
        callIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCallDialog(peerId);
            }
        });
    }

    private void showCallDialog(final int peerId) {
        if(currentFragment != null && currentFragment instanceof ContactsFragmentNew){
            ContactsFragmentNew contactsFragmentNew = (ContactsFragmentNew)currentFragment;
            contactsFragmentNew.showCallDialog(peerId);
        }
    }

    public void initBoxView(final int position, final ImageView select_box, ImageView call_icon) {
        PeerEntity item = getItem(position);
        if(mMode == R.id.NORMAL_MODE){
            if(item instanceof UserEntity){
                call_icon.setVisibility(View.VISIBLE);
            }else {
                call_icon.setVisibility(View.GONE);
            }
            select_box.setVisibility(View.GONE);
        }else if(mMode == R.id.SINGLE_CHOICE_MODE){
            call_icon.setVisibility(View.GONE);
            select_box.setVisibility(View.GONE);
        }else {
            call_icon.setVisibility(View.GONE);
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

    public int getPositionForPinyin(char c) {
        for (int i = 0; i < getCount(); i++) {
            char sort = mPeerList.get(i).getPinyinElement().pinyin.charAt(0);
            if (sort == c) {
                return i;
            }
        }
        return -1;
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

        ContactViewHolder holder = (ContactViewHolder) convertView.getTag();

        if (isObjectSelected(item)) {
            holder.contact_box.setImageResource(R.mipmap.checkbox_off);
            removeSelectedObject(item);
        } else {
            holder.contact_box.setImageResource(R.mipmap.checkbox_on);
            addSelectedObject(item);
        }

        notifyDataSetChanged();
    }

    /**
     * 得到所有名字首字母的分类索引：26个英文字母中的那几个的索引
     * @return
     */
    private int[] getSectionIndices() {
        if (mPeerList.size() == 0) {
            return new int[0];
        }
        ArrayList<Integer> sectionIndices = new ArrayList<>();
        char lastFirstChar = mPeerList.get(0).getPinyinElement().pinyin.charAt(0);
        sectionIndices.add(0);
        int len = mPeerList.size();
        for (int i = 1; i < len; i++) {
            char pin = mPeerList.get(i).getPinyinElement().pinyin.charAt(0);
            if (pin != lastFirstChar) {
                sectionIndices.add(i);
                lastFirstChar = pin;
            }
        }

        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    /**
     * 得到mList中每个不同名字 的拼音的第一个字母的数组
     * @return
     */
    private Character[] getSectionLetters() {
        Character[] letters = new Character[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
        	letters[i] = mPeerList.get(mSectionIndices[i]).getPinyinElement().pinyin.charAt(0);
        }
        return letters;
    }


    public void bindData(List<? extends PeerEntity> allUsers, List<? extends PeerEntity> selectedUser) {

        if (allUsers != null) {
            mPeerList.clear();
            mPeerList.addAll(allUsers);
    		Collections.sort(mPeerList, mCmp);
        }
        if (selectedUser != null){
            clearSelectedObject();
            addSelectedObject(selectedUser);
        }

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        notifyDataSetChanged();
    }

    public void bindData(List<? extends PeerEntity> allUsers, List<? extends PeerEntity> selectedUser, List<? extends PeerEntity> disableEditUsers){

        bindData(allUsers,selectedUser);

        if (disableEditUsers != null){
            clearDisableEditObject();
            addDisableEditObject(disableEditUsers);
        }
    }

    public void updateData(List<? extends PeerEntity> newData) {

        if (newData != null) {
            mPeerList.clear();
            mPeerList.addAll(newData);
            Collections.sort(mPeerList, mCmp);
        }

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        notifyDataSetChanged();
    }

    public void appendData(List<? extends PeerEntity> newData) {

        if (newData != null) {
            mPeerList.addAll(newData);
            Collections.sort(mPeerList, mCmp);
        }

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        notifyDataSetChanged();
    }

    public void clearAll(){
        mPeerList.clear();
    }

    public void removeOneData(int uuid){

        boolean isFind = false;
        for(PeerEntity item : mPeerList){
            if(item.getPeerId() == uuid){
                isFind = true;
                mPeerList.remove(item);
                break;
            }
        }

        if(isFind) {
            notifyDataSetChanged();
        }
    }

    public void removeSelectedItem(int uuid){

        List<PeerEntity> selectedItems = (List<PeerEntity>)allSelectedObject();
        for (int i = 0, len = selectedItems.size(); i < len; i++) {
            PeerEntity item = selectedItems.get(i);
            if(item.getPeerId() == uuid){
                removeSelectedObject(i);
                break;
            }
        }
    }


    @Override
	// 这个是处理StickyListView的HeaderView的显示的
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeadViewHolder headViewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.contacts_header_index, parent, false);
			headViewHolder = new HeadViewHolder(convertView);
			headViewHolder.contacts_letter = (TextView) convertView.findViewById(R.id.contacts_letter);
		} else {
			headViewHolder = (HeadViewHolder) convertView.getTag();
		}
		if(mShowCharIndex) {
            headViewHolder.contacts_letter.setVisibility(View.VISIBLE);
			headViewHolder.contacts_letter.setText(mPeerList.get(position).getPinyinElement().pinyin.toUpperCase().charAt(0) + "");
		}else {
			headViewHolder.contacts_letter.setVisibility(View.GONE);
		}
		return convertView;
	}

    @Override
    public long getHeaderId(int position) {
        return mPeerList.get(position).getPinyinElement().pinyin.charAt(0);
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    @Override
	/* 通过该项的位置，获得所在分类组的索引号 */
    public int getPositionForSection(int section) {
        if (mSectionIndices.length == 0) {
            return 0;
        }

        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    @Override
	/* 通过该项的位置，获得所在分类组的索引号 */
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    protected static class ContactViewHolder extends BaseViewHolder{
        public TextView contact_name;
        public View contact_img_line;
        public ImageView contact_box;
        public ImageView call_icon;
        public CircleImageView contact_img;
        public TextView contact_email_address;


        public ContactViewHolder(View convertView) {
            super(convertView);

            this.contact_img = (CircleImageView) convertView.findViewById(R.id.contact_img);
            this.contact_name = (TextView) convertView.findViewById(R.id.contact_name);
            this.contact_box = (ImageView) convertView.findViewById(R.id.contact_box);
            this.call_icon = (ImageView)convertView.findViewById(R.id.call_icon);
            this.contact_img_line = convertView.findViewById(R.id.contact_img_line);
            this.contact_email_address = (TextView) convertView.findViewById(R.id.contact_email_address);
        }
    }
    
    private static class HeadViewHolder extends BaseViewHolder{
		TextView contacts_letter;

		 HeadViewHolder(View view){
			super(view);
		}
	}

    private static class ChineseCharComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            PeerEntity c1 = (PeerEntity) o1;
            PeerEntity c2 = (PeerEntity) o2;

            String str1 = c1.getPinyinElement().pinyin;
            String str2 = c2.getPinyinElement().pinyin;

            return str1.compareToIgnoreCase(str2);
        }
    }
}


