package com.milanac007.demo.im.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/******************* 联系人列表对象，这个列表不包括分组信息，用于逻辑而不用于UI **********************/
public class ImBuddyList {
	private static ImBuddyList mInstance = null;

	private HashMap<String, ImBuddy> mBuddyList;

	private ArrayList<ImBuddy> mSortList;

	private static ImExtBinarySearch search = new ImExtBinarySearch();

	class buddyCompare implements ImCompareInterface {

		ArrayList<ImBuddy> list;

		public buddyCompare(ArrayList<ImBuddy> list) {
			this.list = list;
		}

		@Override
		public int compare(Object o1, Object o2) {
			int r = ((String) o1).compareTo((String) o2);
			return r;
		}

		@Override
		public int getDataCount() {
			return list.size();
		}

		@Override
		public Object getData(int index) {
			return list.get(index).getName();
		}

	}

	private buddyCompare buddycomp;

	public static boolean isInited() {
		return mInstance != null;
	}

	public static ImBuddyList getInstance() {
		if (mInstance == null) {
			mInstance = new ImBuddyList();
		}

		return mInstance;
	}

	private ImBuddyList() {
		mBuddyList = new HashMap<String, ImBuddy>();
		mSortList = new ArrayList<ImBuddy>();

		buddycomp = new buddyCompare(mSortList);
	}


	public int getCount() {
		return mBuddyList.size();
	}

	public String getBuddyName(String id) {
		ImBuddy buddy = mBuddyList.get(id);
		if (buddy != null) {
			return buddy.getName();
		} else {
			return "";
		}
	}

	public String getBuddyName(int index) {
		if (index < 0 || index >= mBuddyList.size()) {
			return "";
		}

		ImBuddy buddy = mSortList.get(index);
		return buddy.getName();
	}

	public ImBuddy getBuddy(String id) {
		return mBuddyList.get(id);
	}

	public ImBuddy getBuddyByInput(String input) {
		if(CommonFunction.isIMAccount(input)){
			return mBuddyList.get(input);
		}

		if(CommonFunction.isMobile(input)){
			for(ImBuddy buddy:mSortList){
				if(buddy.getTelephone().equals(input)){
					return buddy;
				}
			}
		}

		return null;
	}

	public ImBuddy getBuddy(int index) {
		if (index < 0 || index >= mBuddyList.size()) {
			return null;
		}

		return mSortList.get(index);
		// return (ImBuddy) mBuddyList.values().toArray()[index];
	}

	/**
	 * @param id
	 *            buddy's id, the buddy's name is same with id.
	 * @deprecated Use {@link #addBuddy(ImBuddy)} instead.
	 */
	@Deprecated
	public void addBuddy(String id) {
		if (!mBuddyList.containsKey(id)) {
			ImBuddy buddy = new ImBuddy(id);
			addBuddy(buddy);
		}
	}

	/**
	 * add buddy to buddylist
	 * 
	 * @param buddy
	 * object of buddy.
	 */
	public void addBuddy(ImBuddy buddy) {
		if (!mBuddyList.containsKey(buddy.getId())) {
			mBuddyList.put(buddy.getId(), buddy);

			int index = mSortList.indexOf(buddy);
			if(index >= 0){
				mSortList.remove(index);
			}
			mSortList.add(buddy);

			// 排序
//			insertBuddy(buddy);
		}
	}

	public List<ImBuddy> getBuddyList(){
		return mSortList;
	}

	public List<ImBuddy> getBuddyListExcuteMe(){ //排除自己
		List<ImBuddy> list = new ArrayList<>();
		for(ImBuddy imBuddy : mSortList){
			if(!imBuddy.getId().equals(Preferences.getCurrentLoginer().getUserCode()) && imBuddy.isFriend())
				list.add(imBuddy);
		}
		return list;
	}

	public void clear() {
		mSortList.clear();
		mBuddyList.clear();
	}

	private void insertBuddy(ImBuddy buddy) {
		// 一个简单的插入排序，数量小的时候其实没什么意义。。。
		int index = search.search(buddy.getName(), buddycomp);
		if (-1 == index) {
			mSortList.add(buddy);
		} else if (-2 == index) {
			// index = -2表示此时数组里只有一个值，下面的处理将其变为一个正序数组
			if (buddycomp.compare(buddy.getName(), mSortList.get(0).getName()) > 0) {
				mSortList.add(buddy);
			} else {
				mSortList.add(0, buddy);
			}

		} else {
			mSortList.add(index, buddy);
		}
	}

	public void release() {
		clear();
		mInstance = null;
	}
}
