package com.milanac007.demo.im.db.config;

/**
 * @author : yingmu on 15-1-11.
 * @email : yingmu@mogujie.com.
 */
public interface MessageConstant {

    /**基础消息状态，表示网络层收发成功*/
    public final int   MSG_SENDING = 1;
    public final int   MSG_FAILURE = 2;
    public final int   MSG_FAIL_RESULT_CODE = -1; //表示聊天关系验证失败（双方已经不是好友或者发送者不在群组；
    public final int   MSG_SUCCESS = 3;

    /**附件消息状态，表示下载到本地、上传到服务器的状态*/
    public final int MSG_FILE_UNLOAD =1;
    public final int MSG_FILE_LOADING =2;
    public final int MSG_FILE_LOADED_SUCCESS =3;
    public final int MSG_FILE_LOADED_FAILURE =4;


    /**语音状态，未读与已读*/
    public final int   AUDIO_UNREAD =1;
    public final int   AUDIO_READED = 2;

    /**图片消息的前后常量*/
    public  final String IMAGE_MSG_START = "&$#@~^@[{:";
    public  final String IMAGE_MSG_END = ":}]&$~@#@";

}
