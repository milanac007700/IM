package com.milanac007.demo.im.db.config;

public class DataConstants {
	public static final String INTENT_ACTION_DATA_RECEIVE = "data.receive";

	public static final int DATA_STATUS_SEND = 0;
	public static final int DATA_STATUS_RECEIVE = 1;

	public static final int DATA_TYPE_SETUSERINFO = 1;
	public static final int DATA_TYPE_SETSERVERINFO = 2;
	public static final int DATA_TYPE_LOGIN = 3;
	public static final int DATA_TYPE_INCOMINGCALL = 4;
	public static final int DATA_TYPE_CALL_STATECHANGED = 5;
	public static final int DATA_TYPE_BUDDY_STATECHANGED = 6;
	public static final int DATA_TYPE_NEW_MSG_RECEIVE = 7;
	public static final int DATA_TYPE_ADDBUDDY = 8;
	public static final int DATA_TYPE_SENDMSG = 9;
	public static final int DATA_TYPE_DIAL = 10;
	public static final int DATA_TYPE_ANSWER = 11;
	public static final int DATA_TYPE_REJECT = 12;
	public static final int DATA_TYPE_HUNDUP = 13;
	public static final int DATA_TYPE_SET_PRESENCE = 14;
	public static final int DATA_TYPE_LOGOUT = 15;
	public static final int DATA_CHECK_STATE = 16;
	public static final int DATA_TYPE_ON_VIDIO_STATE_CHANGED = 17;
	public static final int DATA_TYPE_INCOMING_VIDIO_CALL = 18;
	public static final int DATA_TYPE_RE_DIAL = 19;
	public static final int DATA_TYPE_ANSWERVIDEO = 20;
	public static final int DATA_TYPE_DIAL_VIDEO = 21;
	public static final int DATA_TYPE_SET_VIDEO_CALL_LEVEL = 22;

	public static final String MSG_TYPE_FLAG_TEXT = "text/plain";
	public static final String MSG_TYPE_FLAG_IMAGE = "text/image";
	public static final String MSG_TYPE_FLAG_VOICE = "text/voice";
	public static final String MSG_TYPE_FLAG_LITTLE_VIDEO = "text/littlevideo";
	public static final String MSG_TYPE_FLAG_VIDEO = "text/video";
	public static final String MSG_TYPE_FLAG_ADDBUDDY = "text/sys-Addbuddy";//添加好友申请
	public static final String MSG_TYPE_FLAG_GUARANTEEBUDDY = "text/sys-Guaranteebuddy"; //通过好友添加请求
	public static final String MSG_TYPE_FLAG_SYS_ACCEPT_ADDBUDDY = "text/sys-accept-addbuddy"; //系统消息
	public static final String MSG_TYPE_FLAG_DELBUDDY = "text/sys-Delbuddy";//解除好友

	public static final String CALL_HISTORY_SESSION_ID = "CALL_HISTORY_SESSION_ID"; //标识通话记录和邮箱的session_id, 这里暂写死
	public static final String EMAIL_INBOX_SESSION_ID = "EMAIL_INBOX_SESSION_ID";

	public static final String MSG_TYPE_SHOWTEXT_IMAGE = "[图片]";
	public static final String MSG_TYPE_SHOWTEXT_VOICE = "[语音]";
	public static final String MSG_TYPE_SHOWTEXT_VIDEO = "[视频]";
	public static final String MSG_TYPE_SHOWTEXT_SYS_ACCEPT_ADDBUDDY_FOR_ME = "你已经添加%s, 现在可以开始聊天了。";
	public static final String MSG_TYPE_SHOWTEXT_SYS_ACCEPT_ADDBUDDY = "我同意了你的朋友验证请求，现在我们可以开始聊天了";
	public static final String MSG_TYPE_SHOWTEXT_SYS_ACCEPT_ACCEPTBUDDY_FOR_ME = "%s同意了你的朋友验证请求，现在我们可以开始聊天了";


	public static final int NOTIFICATION_ACTION_MISSEDCALL = 1;

	public static final String ACTION = "action";
	public static final String ALL_BODDY_INFO = "allBuddyInfo";
	public static final String ADDRESS = "addr";
	public static final String BUDDY_ID = "buddyId";
	public static final String SESSION_ID = "sessionId";
	public static final String CALL_STATE = "callState";
	public static final String CALLER_URI = "callerUri";
	public static final String CONTENT_TYPE = "contentType";
	public static final String MSG_BODY = "msgBody";
	public static final String DATA = "data";
	public static final String IS_VIDIO = "isVideo";
	public static final String NAME = "name";
	public static final String PASSWORD = "pass";
	public static final String PORT = "port";
	public static final String PRESENCE = "presence";
	public static final String REG_STATE = "regState";
	public static final String REG_CODE = "regCode";
	public static final String REASON = "reason";
	public static final String SCREEN = "screen";
	public static final String STATE = "state";
	public static final String VIDEO_LEVEL = "vedio_level";
}
