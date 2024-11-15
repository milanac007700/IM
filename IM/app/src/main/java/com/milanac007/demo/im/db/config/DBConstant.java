package com.milanac007.demo.im.db.config;

public interface DBConstant {

    /**性别
     * 1. 男性 2.女性
     * */
    public final int SEX_MAILE = 1;
    public final int SEX_FEMALE = 2;

    /**msgType*/
    int MSG_TYPE_SINGLE_SYSTEM_TEXT = -1; //私聊系统消息
    int MSG_TYPE_GROUP_SYSTEM_TEXT = 0; //群聊系统消息
    int MSG_TYPE_NEED_ADD_BUDDY_VERIFY_SYSTEM_TEXT = -2; //发送失败，需要好友验证系统消息
    int MSG_TYPE_SIP_CALL_MSG_TEXT = -3; //网络电话记录消息

    int  MSG_TYPE_SINGLE_TEXT = 1;
    int  MSG_TYPE_SINGLE_AUDIO = 2;
    int  MSG_TYPE_SINGLE_VEDIO = 3;
    int  MSG_TYPE_SINGLE_IMG = 4;
    int  MSG_TYPE_SINGLE_FILE = 5;

    int  MSG_TYPE_GROUP_TEXT = 17;
    int  MSG_TYPE_GROUP_AUDIO = 18;
    int  MSG_TYPE_GROUP_VEDIO = 19;
    int  MSG_TYPE_GROUP_IMG = 20;
    int  MSG_TYPE_GROUP_FILE = 21;

    int  MSG_TYPE_CANCEL_SGL_MSG = 7; //单聊消息撤回
    int  MSG_TYPE_CANCEL_GRP_MSG = 23;

    int MSG_TYPE_ADD_BUDDY_REQUEST = 100;//添加好友申请
    int MSG_TYPE_ADD_BUDDY_ACCEPT = 101; //通过好友添加请求
    int MSG_TYPE_DELBUDDY_REQUEST = 102;//解除好友

    String SHOWTEXT_SYS_ACCEPT_ADDBUDDY_FOR_ME = "你已经添加%s, 现在可以开始聊天了。";
    String SHOWTEXT_SYS_ACCEPT_ADDBUDDY = "我同意了你的朋友验证请求，现在我们可以开始聊天了";

    /**msgDisplayType
     * 保存在DB中，与服务端一致，图文混排也是一条
     * 1. 最基础的文本信息
     * 2. 纯图片信息
     * 3. 语音
     * 4. 图文混排
     * */
    int SHOW_ORIGIN_TEXT_TYPE = 1;
    int  SHOW_IMAGE_TYPE = 2;
    int  SHOW_AUDIO_TYPE = 3;
    int  SHOW_MIX_TEXT = 4;
    int  SHOW_GIF_TYPE = 5;
    int SHOW_VIDEO_TYPE = 6; //自己定义的，后期修改
    int SHOW_FILE_TYPE = 7;
    int  SHOW_AUDIO_CALL_TYPE = 8;
    int  SHOW_VIDEO_CALL_TYPE = 9;

    String DISPLAY_FOR_IMAGE = "[图片]";
    String DISPLAY_FOR_AUDIO = "[语音]";
    String DISPLAY_FOR_VIDEO = "[视频]";
    String DISPLAY_FOR_FILE = "[文件]";
    String DISPLAY_FOR_ERROR = "[未知消息]";
    String DISPLAY_FOR_MIX = "[图文消息]";
    String DISPLAY_FOR_AUDIO_CALL = "[语音聊天]";
    String DISPLAY_FOR_VIDEO_CALL = "[视频聊天]";

    /**sessionType*/
    int  SESSION_TYPE_SINGLE = 1;
    int  SESSION_TYPE_GROUP = 2;
    int  SESSION_TYPE_CALL_RECORD = 3; //标识通话记录和邮箱
    int  SESSION_TYPE_EMAIL_INBOX = 4;
    int SESSION_TYPE_ERROR= 5;

    /**user status
     * 1. 试用期 2. 正式 3. 离职 4.实习
     * */
    public final int  USER_STATUS_PROBATION = 1;
    public final int  USER_STATUS_OFFICIAL = 2;
    public final int  USER_STATUS_LEAVE = 3;
    public final int  USER_STATUS_INTERNSHIP =4;

   /**group type*/
   int  GROUP_TYPE_NORMAL = 1;
   int  GROUP_TYPE_TEMP = 2;
   int  GROUP_TYPE_AUTH_NORMAL = 17;
   int GROUP_TYPE_AUTH_TMP = 18;


    /* 群认证类型（1：认证，0：非认证）*/
    int GROUP_AUTH_TYPE_YES = 1;
    int GROUP_AUTH_TYPE_NO = 0;


    /**group status
     * 1: shield屏蔽  0: not shield
     * */

    public final int  GROUP_STATUS_ONLINE = 0;
    public final int  GROUP_STATUS_SHIELD = 1;

    /**group change Type*/
    public final int  GROUP_MODIFY_TYPE_ADD= 1;
    public final int  GROUP_MODIFY_TYPE_DEL =2;
    int GROUP_MODIFY_TYPE_LEAVE = 3; //主动退群

    /**depart status Type*/
    public final int  DEPT_STATUS_OK= 0;
    public final int  DEPT_STATUS_DELETE =1;

    int PREFERENCE_TYPE_GROUP_NICK_VALUE = -1; //group member的属性，防止和GROUP_MODIFY_TYPE_ADD的值冲突，这里设置为-1
    int PREFERENCE_TYPE_GROUP_NOTICE_VALUE = 2;
    int PREFERENCE_TYPE_GROUP_NAME_VALUE = 3;
    int PREFERENCE_TYPE_GROUP_ADMIN_VALUE = 4;
    int PREFERENCE_TYPE_GROUP_AUTH_VALUE = 5;
    int PREFERENCE_TYPE_GROUP_NORMAL_VALUE = 6;
    int PREFERENCE_TYPE_GROUP_AVATAR_VALUE = 7;

    String KEY_SESSION_KEY = "chat_session_key";
}
