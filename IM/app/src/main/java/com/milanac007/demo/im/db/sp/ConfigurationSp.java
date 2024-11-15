package com.milanac007.demo.im.db.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.SessionEvent;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 * <p/>
 * 特定用户的配置文件
 * User_userId.ini
 * <p/>
 * todo
 * 1.lastUpdate 2.lastVersion 3.listMsg 需要保存嘛
 * <p/>
 * <p/>
 * 群置顶的功能也会放在这里
 * 现在的有两种key  sessionKey 以及 DBConstant的Global
 * 多端的状态最好不放在这里。备注: 例如屏蔽的状态
 *
 * 增加显示群成员的群集合
 */
public class ConfigurationSp {
    private Context ctx;
    private int loginId;
    private String fileName;
    private SharedPreferences sharedPreferences;

    private static ConfigurationSp configurationSp = null;

    public static ConfigurationSp instance(Context ctx, int loginId) {
        if (configurationSp == null || configurationSp.loginId != loginId) {
            synchronized (ConfigurationSp.class) {
                configurationSp = new ConfigurationSp(ctx, loginId);
                return configurationSp;
            }
        }
        return configurationSp;
    }

    private ConfigurationSp(Context ctx, int loginId) {
        this.ctx = ctx;
        this.loginId = loginId;
        this.fileName = "User_" + loginId + ".ini";
        this.sharedPreferences = ctx.getSharedPreferences(fileName, ctx.MODE_PRIVATE);
    }



    public HashSet<String> getShowNickList() {
        Set<String> nickList = sharedPreferences.getStringSet(CfgDimension.SHOW_GROUP_CHAT_NICK.name(), null);
        if (null == nickList) {
            return null;
        }
        return (HashSet<String>) nickList;
    }

    public boolean isShowNick(int groupId) {
        if(groupId == 0)
            return false;
        String sessionKey = String.valueOf(groupId);

        HashSet<String> list =  getShowNickList();
        if (list != null && list.size() > 0 && list.contains(sessionKey)) {
            return true;
        }
        return false;
    }

    public void setShowGroupNick(int groupId, boolean isShowNick){
        if(groupId == 0)
            return;
        String sessionKey = String.valueOf(groupId);

        Set<String> nickGroupList = sharedPreferences.getStringSet(CfgDimension.SHOW_GROUP_CHAT_NICK.name(), null);
        Set<String> newList = new HashSet<>();
        if(nickGroupList != null && nickGroupList.size() >0){
            newList.addAll(nickGroupList);
        }

        if(isShowNick){
            newList.add(sessionKey);
        }else {
            if (newList.contains(sessionKey)) {
                newList.remove(sessionKey);
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CfgDimension.SHOW_GROUP_CHAT_NICK.name(), newList);
        editor.apply();
        GroupEvent event = new GroupEvent(GroupEvent.Event.GROUP_SHOW_NICK);
        event.setGroupId(groupId);
        EventBus.getDefault().post(event);
    }


    // 获取全部置顶的session
    public HashSet<String> getSessionTopList() {
        Set<String> topList = sharedPreferences.getStringSet(CfgDimension.SESSIONTOP.name(), null);
        if (null == topList) {
            return null;
        }
        return (HashSet<String>) topList;
    }


    public boolean isTopSession(String sessionKey) {
        HashSet<String> list =  getSessionTopList();
        if (list != null && list.size() > 0 && list.contains(sessionKey)) {
            return true;
        }
        return false;
    }

    /**
     * shareF 在设定set的时候有个蛋疼的点
     * 参考:http://stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
     *
     * @param sessionKey
     * @param isTop
     */
    public void setSessionTop(String sessionKey, boolean isTop) {
        if (TextUtils.isEmpty(sessionKey)) {
            return;
        }
        Set<String> topList = sharedPreferences.getStringSet(CfgDimension.SESSIONTOP.name(), null);
        Set<String> newList = new HashSet<>();
        if (topList != null && topList.size() > 0) {
            newList.addAll(topList);
        }

        if (isTop) {
            newList.add(sessionKey);
        } else {
            if (newList.contains(sessionKey)) {
                newList.remove(sessionKey);
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CfgDimension.SESSIONTOP.name(), newList);
        //提交当前数据
        editor.apply();
        EventBus.getDefault().post(SessionEvent.SET_SESSION_TOP);
    }


    public boolean getCfg(String key, CfgDimension dimension) {
//        boolean defaultValue = dimension == CfgDimension.NOTIFICATION ? false : true;
        boolean defaultValue = true;
        boolean isOpen = sharedPreferences.getBoolean(dimension.name() + key, defaultValue);
        return isOpen;
    }


    public void setCfg(String key, CfgDimension dimension, boolean onoff) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(dimension.name() + key, onoff);
        //提交当前数据
        editor.commit();
    }

    // 更新时间基准点， 暂时没有使用！
    public int getTimeLine(TimeLine timeLine) {
        int updateTime = sharedPreferences.getInt(timeLine.name(), 0);
        return updateTime;
    }

    public void setTimeLine(TimeLine timeLine, int newTimePoint) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(timeLine.name(), newTimePoint);
        //提交当前数据
        editor.commit();
    }

    /**
     * 1. 勿扰
     * 2. 声音
     * 3. 自动
     * 4. 通知的方式 one session/ one message
     */
    public enum CfgDimension {
        NOTIFICATION,
        SOUND,
        VIBRATION,
        SHOW_MSG_DETAIL,

        //置顶session 设定
        SESSIONTOP,

        //显示群昵称
        SHOW_GROUP_CHAT_NICK,

        //接收通话记录提醒
        RECV_CALL_RECORD_NOTIFY,
    }

    public enum TimeLine {
        // session回话增量更新的时间点
        SESSION_UPDATE_TIME
    }

}
