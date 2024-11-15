package com.milanac007.demo.im.ui;


import com.milanac007.demo.im.utils.ImBuddy;
import com.milanac007.demo.im.utils.ImBuddyList;

import java.util.Date;

/**
 * Created by zqguo on 2016/10/28.
 */
public class FriendAdapterListItem {
        boolean mIsSameComp = false; //是否是同一公司
        boolean mIsFriend = false; //是否是好友
        String mVerifyMsg; //验证消息
        int mStatus; // 0(wait for accept), 1(对方已同意)，  2(wait for me accept), 3(我已接受)
        int buddyid; //所属会话id
        Date createDate;
        Date updateDate;


    public Date getCreateDate() {
            return createDate;
        }

        public void setCreateDate(Date createDate) {
            this.createDate = createDate;
        }

        public Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }

        public FriendAdapterListItem(){

        }
        public boolean ismIsSameComp() {
            return mIsSameComp;
        }

        public void setIsSameComp(boolean mIsSameComp) {
            this.mIsSameComp = mIsSameComp;
        }

        public boolean isFriend() {
            return mIsFriend;
        }

        public void setIsFriend(boolean mIsFriend) {
            this.mIsFriend = mIsFriend;
        }

        public String getVerifyMsg() {
            return mVerifyMsg;
        }

        public void setVerifyMsg(String mVerifyMsg) {
            this.mVerifyMsg = mVerifyMsg;
        }

        public int getStatus() {
            return mStatus;
        }

        public void setStatus(int mStatus) {
            this.mStatus = mStatus;
        }

        public ImBuddy getBuddy() {
            return ImBuddyList.getInstance().getBuddy(buddyid);
        }

        public int getBuddyId() {
            return buddyid;
        }

        public void setBuddyid(int buddyid) {
            this.buddyid = buddyid;
        }
}