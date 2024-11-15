package com.milanac007.demo.im.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.net.NetConstants;

public class Preferences {

    private static SharedPreferences mPreferences = App.getContext()
            .getSharedPreferences(App.getContext().getPackageName() + "_IM", Context.MODE_PRIVATE);


    public static String getLoginName() {
        return mPreferences.getString("login_name", "");
    }

    public static void setLoginName(String loginName) {
        mPreferences.edit().putString("login_name", loginName).apply();
    }


    public static String getSHA265Pwd() {
        return mPreferences.getString("sha256_pwd", "");
    }

    public static void setSHA265Pwd(String sha265Pwd) {
        mPreferences.edit().putString("sha256_pwd", sha265Pwd).apply();
    }

    public static String getRefreshToken() {
        return mPreferences.getString("refresh_token", "");
    }

    public static void setRefreshToken(String refreshToken) {
        mPreferences.edit().putString("refresh_token", refreshToken).apply();
    }

    public static int getKeyBoardHeight() {
        return mPreferences.getInt("keyBoardHeight", 0);
    }

    public static void setKeyBoardHeight(int keyBoardHeight) {
        mPreferences.edit().putInt("keyBoardHeight", keyBoardHeight).commit();
    }

    public static String getHostName() {
        return mPreferences.getString("hostName", "");
    }

    public static void setHostName(String hostName) {
        mPreferences.edit().putString("hostName", hostName).commit();
        NetConstants.init();
    }

    public static void updateCurrentLoginer(User user) {
        Gson gson = new Gson();
        String userToStr = gson.toJson(user, User.class);
        mPreferences.edit().putString("loginer", userToStr).apply();
    }

    public static User getCurrentLoginer() {
        String str = mPreferences.getString("loginer", "");
        Gson gson = new Gson();
        return TextUtils.isEmpty(str) ? null: gson.fromJson(str, User.class);
    }

	//同步清除sp
    public static void clear() {
        mPreferences.edit().clear().commit();
    }
}
