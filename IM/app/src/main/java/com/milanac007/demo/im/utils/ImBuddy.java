package com.milanac007.demo.im.utils;
import com.milanac007.demo.im.enums.EGender;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.PinYin;

import java.util.ConcurrentModificationException;
import java.util.List;

/******************* 联系人对象 **********************/
public class ImBuddy {
	private String mName;
	private String mId;
	private int mPresence;
	private String mNo;
	private String mServerId;

	private String mSearchStr = "";

	public ImBuddy(){

	}

	public ImBuddy(String id) {
		mName = id;
		mId = id;
		mNo = id;
		mServerId = "";
//		mPresence = ImPresence.ON_LINE;
		MakeSearchString();
	}

	public String getServerId() {  //TODO 新版已经没意义了
		return mServerId;
	}

	public void setNo(String no) {
		mNo = no;
	}

	public String getNo() {
		return mNo;
	}



	public void setName(String name) {
		mName = name;
		MakeSearchString();
		
		setmNamePinyin(CommonFunction.getStringPingYin(name));
	}

	public String getName() {
		return mName;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}

	public void setPresence(int presence) {
		mPresence = presence;
	}

	public int getPresence() {
		return mPresence;
	}

	public String getSearchString() {
		return mSearchStr;
	}

	public void MakeSearchString() {
		mSearchStr = "";
		mSearchStr += (mNo + "\n");
		mSearchStr += (mName.toLowerCase() + "\n");

		try {
			//TODO
			List<String> list = PinYin.getPinYin(mName);
//			List<String> list = null;
			if(list == null || list.isEmpty())
				return;

			if(list.size() == 1){
				mSearchStr += (list.get(0) + "\n");
			}else {
				for (String item : list) {
					mSearchStr += (item + "\n");
				}
			}

		}catch (ConcurrentModificationException e){
			e.printStackTrace();
		}

	}

	public boolean isGeneralContact() {
//		if (mId!=null&&ImContactList.getInstance().getGroupCount() > 0) {
//			ImContactGroup imGeneralContactGroup = ImContactList.getInstance()
//					.getGroup(0);
//			if (imGeneralContactGroup != null
//					&& imGeneralContactGroup.getBuddyCount() > 0) {
//				for(ImBuddy buddy :imGeneralContactGroup.getAllImBuddies()){
//					if(mId.equals(buddy.getId())){
//						return true;
//					}
//				}
//			}
//		}
		return false;
	}

	//added by zqguo

	private String mNamePinyin;
	public String getmNamePinyin() {
		if (CommonFunction.isStringEmpty(mNamePinyin)) {
			mNamePinyin = " ";
		}
		return mNamePinyin;
	}

	public void setmNamePinyin(String mNamePinyin) {
		this.mNamePinyin = mNamePinyin;
	}

	private String mUserIconUrl;
	public void setUserIconUrl(String userIconUrl){
		mUserIconUrl = userIconUrl;
	}
	public String getUserIconUrl() {
		return mUserIconUrl;
	}

	private String mHeadIcoLocalPath;
	public void setHeadIcoLocalPath(String localpath){
		mHeadIcoLocalPath = localpath;
	}
	public String getHeadIcoLocalPath() {
		return mHeadIcoLocalPath;
	}

	private EGender mGender;
	public EGender getmGender() {
		return mGender == null ? EGender.MALE : mGender;
	}

	public void setmGender(EGender gender) {
		this.mGender = gender;
	}

	private String mEmailAddress;
	public void setEmailAddress(String emailAddress){
		mEmailAddress = emailAddress;
	}
	public String getEmailAddress() {
		return mEmailAddress;
	}

	private String mTelephone;

	public String getTelephone() {
		return mTelephone;
	}

	public void setTelephone(String mTelephone) {
		this.mTelephone = mTelephone;
	}

	private boolean isFriend = false;

	public boolean isFriend() {
		return isFriend;
	}

	public void setFriend(boolean friend) {
		isFriend = friend;
	}
}
