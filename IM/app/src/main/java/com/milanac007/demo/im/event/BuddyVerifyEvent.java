package com.milanac007.demo.im.event;

/**
 * Created by zqguo on 2017/3/23.
 */
public class  BuddyVerifyEvent {
    public static final int ADD_BUDDY_REQUEST = 0; //添加好友申请
    public static final int ADD_BUDDY_ACCEPT = 1; //通过好友添加请求
    public static final int DELBUDDY_REQUEST = 2; //解除好友

    public int type;
    public int buddyid;

    public BuddyVerifyEvent(int type, int buddyid){
        this.type = type;
        this.buddyid = buddyid;
    }

}
