package com.milanac007.demo.im.interfaces;

import android.os.Bundle;

import com.milanac007.demo.im.fragment.ContactsBaseFragment;
import com.milanac007.demo.im.fragment.TransmitMsgFragment;

public interface OnActionListener {
    interface Page {
        int SCREEN_SESSIONLIST = 0;
        int SCREEN_CONTACTLIST = 1;
        int SCREEN_APP = 2;
        int SCREEN_SELFINFO = 3;

        int SCREEN_CHAT = 4;
        int SCREEN_PERSONALINFO = 5;
        int SCREEN_MODIFY_BUDDY_EXTRA = 6; //设置好友备注信息
        int SCREEN_MODIFY_PERSONALINFO = 7;
        int SCREEN_SINGLE_CHAT_SETTING = 8;  //私聊设置页面

        int SCREEN_ADDFRIEND = 9;
        int SCREEN_MY_FRIEND = 10;
        int SCREEN_ADD_FRIEND_VERIFY_MSG = 11;

        int SCREEN_SIMPLE_ZXING = 12;
        int SCREEN_SELECT_BUDDY = 13;   //选人
        int SCREEN_TRANSMIT = 14; //转发
        int SCREEN_SEARCH = 15;

        int SCREEN_RESETPWD = 16;
        int SCREEN_MY_DETAIL = 17;
        int SCREEN_SETUP = 18;
        int SCREEN_ACCOUNT_SECURITY = 19;
        int SCREEN_ABOUT_OA = 20;
        int SCREEN_NEW_MSG_NOTIFY_SETTING = 21;

        int SCREEN_GROUP_CHAT_MAIN = 22; //群聊主页面
        int SCREEN_GROUP_CHAT_SETTING = 23; //群聊设置页面
        int SCREEN_CREATE_GROUP_CHAT = 24; //发起群聊
        int SCREEN_SELECT_GROUP_CHAT = 25; //选择群聊
        int SCREEN_GROUP_NAME_SETTING = 26; //群聊名称/自己的昵称 设置页面
        int SCREEN_GROUP_NOTICE_EDIT = 27; //群公告编辑页面
        int SCREEN_CREATE_MODIFY_GROUP_MEM = 28; //编辑群成员
        int SCREEN_GROUP_ADMIN = 29; //群管理页面
        int SCREEN_GROUP_ADMIN_MODIFY = 30; //群管理员移交
        int SCREEN_GROUP_INTRODUCTION = 31; //群简介页面

    }

    interface Action {
        int ACTION_RETURN = 0;
        int ACTION_SEND_TEXT_MSG = 1;
        int ACTION_SWITCH_SCREEN = 2;
        int ACTION_EXIT = 3;
        int ACTION_CALL = 4;
        int ACTION_HISTORY_DETAIL = 5;
        int ACTION_SHOW_BAR = 6;
        int ACTION_CALL_VIDEO = 7;
        int ACTION_CHECK_SIPSERVER_STATE = 8; //测试时使用，正式时屏蔽掉
        int ACTION_SEND_VOICE_MSG = 9;
        int ACTION_HISTORY = 10;
        int ACTION_RETURN_IM = 11; //从IM窗口返回时，应pop回退栈，并 切换到消息tab
        int ACTION_SHOW_MENU = 12; //显示菜单
        int ACTION_GUARANTEEBUDDY = 13; //同意添加好友
        int ACTION_SEARCH_BUDDY = 14; //查找好友
        int ACTION_MODIFY_PERSONALINFO = 15; //更新个人信息相关
        int ACTION_SHOW_CALL_DIALOG = 16; //显示sip电话dialog
        int ACTION_SELECT_BUDDY = 17; //选人页面
        int ACTION_VIDEO_PLAY = 19; //播放小视频
        int ACTION_DELBUDDY = 20; //删除好友关系
        int ACTION_APP_CHECK_UPDATE = 21; //版本检测与更新
        int ACTION_SHOW_QRCODE = 22; //显示二维码 我/群
        int ACTION_GROUP_NAME_SETTING = 23; //群聊名称/自己的昵称 设置页面
        int ACTION_SCAN_QRCODE = 24; //扫一扫
        int ACTION_CREATE_GROUP_CHAT = 28; //发起群聊
        int ACTION_SELECT_GROUP_CHAT = 29; //选择群聊
        int ACTION_TRANSMIT = 30; //转发
    }

     void OnAction(int page, int action, int code, String arg);
     Bundle getArguments(int page, int arguments);
}
