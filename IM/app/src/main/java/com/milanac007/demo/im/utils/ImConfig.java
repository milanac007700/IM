package com.milanac007.demo.im.utils;

public class ImConfig {
	private final static String server_url_wwwMode = "111.204.244.42";
	private final static String server_url_Lan = "172.16.32.183";
	private final static String server_host_lan = "5060";

	// public static String user_name = "";
	// public static String user_id = "";
	// public static String user_serverid = "";
	// public static String extNo = "";
	public static String reg_name = "";
	public static String activeMQ_url = "";
	public static String loginName = "";
	public static String pwd = "";
	public static String currBuddy = "";
	public static int currentSessionId;
	public static String missedCallBuddy = "";
	public static ImUser user = null;

	// public static boolean isRegisterSuccess = false;
	public static final String getServerUrl() {
		return server_url_Lan;
	}
	public static final String getServerHostPort() {

		return server_host_lan;
	}
}
