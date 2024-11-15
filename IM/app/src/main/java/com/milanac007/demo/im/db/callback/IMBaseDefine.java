package com.milanac007.demo.im.db.callback;

public interface IMBaseDefine {
    String Login = "Login";
    String ReLogin = "ReLogin";
    String Logout = "Logout";
    String Msg = "Msg";
    String MsgAck = "MsgAck";
    String AllUserList = "AllUserList"; //好友列表
    String BuddyListUserInfo = "BuddyListUserInfo";
    String ReqSearchBuddy = "ReqSearchBuddy";
    String ReqAddBuddy = "ReqAddBuddy";
    String onNotifyAddBuddy = "onNotifyAddBuddy";
    String ConfirmAddBuddy = "ConfirmAddBuddy";
    String onNotifyAddBuddyAccept = "onNotifyAddBuddyAccept";
    String AllGroupList = "AllGroupList";
    String AllNormalGroupList = "AllNormalGroupList";
    String GetGroupDetailInfo = "GetGroupDetailInfo";
    String GroupChangeMember = "GroupChangeMember";
    String GroupChangeMemberNotify = "GroupChangeMemberNotify";
    String ReqCreateGroup = "ReqCreateGroup";
    String onNotifyCreateGroup = "onNotifyCreateGroup";
}
