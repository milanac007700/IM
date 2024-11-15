package com.milanac007.demo.im.event;

/**
 * Created by zqguo on 2017/5/19.
 */
public enum  MSG_SERVER_DISCONNECTED_REASON {
    NONE, //主动断开连接
    KICKOUT, //其他终端登录
    HEART_BEAT_FAIL, //心跳失败
    HEART_BEAT_TIMEOUT //心跳超时
}
