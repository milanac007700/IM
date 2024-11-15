package com.milanac007.demo.im.db.config;

public class ImAction {
	public static final String INTENT_BRING_APP_TO_FRONT = "com.milanac007.demo.im.receiver.BringToFront"; //应用调到前台
	public static final String INTENT_SYNC_CONTACTS = "com.milanac007.demo.im.sip.sync_contacts";
	public static final String INTENT_DOWNLOAD_FINISHED = "com.milanac007.demo.im.download_finished";//下载文件完成
	public static final String INTENT_DOWNLOAD_FAILED = "com.milanac007.demo.im.download_failed";//下载文件失败

	
	public static final String INTENT_CALL_ACTION = "call_action";
	public static final String INTENT_CHECK_STATE = "CHECK_STATE";
	public static final String INTENT_RECEIVE_DATA_FROM_SERVER = "com.milanac007.demo.im.data.receive";
	public static final String INTENT_SEND_DATA_TO_SERVICE = "dataToService";
	public static final String INTENT_ALARM = "alarm";
	public static final String INTENT_NOTIFICATION = "com.milanac007.demo.im.notification";
	public static final String INTENT_SIP_STATE_CHANGED = "com.milanac007.demo.im.sip.server.statu.changed";
	//public static final String INTENT_ADD_ALL_CONTACTS = "com.milanac007.demo.im.sip.action.addAllContacts";
	public static final String INTENT_ALL_BUDDY_INFO = "com.milanac007.demo.im.sip.action.addContactsInfo";
	public static final String INTENT_SIP_RESOURCE_INITED = "com.milanac007.demo.im.sip.resource.inited";//sip相关资源初始化完成
	}
