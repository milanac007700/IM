
package com.milanac007.demo.im.db.manager;


import android.text.TextUtils;

import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.RecentInfo;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.entity.UnreadEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.db.sp.ConfigurationSp;
import com.milanac007.demo.im.event.SessionEvent;
import com.milanac007.demo.im.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * app显示首页
 * 最近联系人列表
 */
public class IMSessionManager extends IMManager{

    private Logger logger = Logger.getLogger();
    private static IMSessionManager inst = new IMSessionManager();
    private List<RecentInfo> recentSessionList = new ArrayList<>();

    public static IMSessionManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager imLoginManager = IMLoginManager.instance();
    //    private DBInterface dbInterface = DBInterface.instance();
    private IMGroupManager groupManager = IMGroupManager.instance();
    private IMContactManager contactManager = IMContactManager.instance();

    // key = sessionKey -->  sessionType_peerId
    private  Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();
    //SessionManager 状态字段
    private boolean sessionListReady = false;

    @Override
    public void doOnStart() {

    }

    public void reset() {
        sessionListReady = false;
        sessionMap.clear();
    }

    /**
     * 实现自身的事件驱动
     * @param event
     */
    public void triggerEvent(SessionEvent event) {
        switch (event){
            case RECENT_SESSION_LIST_SUCCESS:
                sessionListReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }


    public void onNormalLoginOk() {
        logger.d("recent#onLogin Successful");
        onLocalLoginOk();
//        onLocalNetOk();
    }

    /**
     * DB加载
     */
    public void onLocalLoginOk(){
        logger.i("session#loadFromDb");

        List<SessionEntity>  sessionInfoList = SessionEntity.getSessionList();

        for(SessionEntity sessionInfo:sessionInfoList){
            sessionMap.put(sessionInfo.getSessionKey(), sessionInfo);
        }

        triggerEvent(SessionEvent.RECENT_SESSION_LIST_SUCCESS);
    }

    /**
     * 网络加载
     */
    public void onLocalNetOk(){
//        int latestUpdateTime = dbInterface.getSessionLastTime();
//        logger.d("session#更新时间:%d",latestUpdateTime);
//        reqGetRecentContacts(latestUpdateTime);
    }

    /**
     * 请求最近会话
     */
    private void reqGetRecentContacts(int latestUpdateTime) {}


    public void clearMsgBySession(SessionEntity sessionEntity){
        int loginId = imLoginManager.getLoginId();
        String sessionKey = sessionEntity.getSessionKey();
        /**直接本地先删除,清楚未读消息*/
        if(sessionMap.containsKey(sessionKey)){
            sessionEntity.setLatestMsgType(-1);
            sessionEntity.setLatestMsgData("");

            sessionMap.put(sessionKey, sessionEntity);
//            dbInterface.insertOrUpdateSession(sessionEntity);
            SessionEntity.insertOrUpdateSingleData(sessionEntity);

//            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);

//            dbInterface.deleteMessageBySessionId(sessionKey);
            MessageEntity.deleteMessageBySessionId(sessionKey);

            ConfigurationSp.instance(ctx,loginId).setSessionTop(sessionKey,false);
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    /**
     * 请求删除会话
     */
    public  void reqRemoveSession(RecentInfo recentInfo) {
        logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();
        String sessionKey = recentInfo.getSessionKey();
        /**直接本地先删除,清楚未读消息*/
        if (sessionMap.containsKey(sessionKey)) {
            sessionMap.remove(sessionKey);

//            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);

//            dbInterface.deleteSession(sessionKey);
            SessionEntity.deleteSession(sessionKey);

//            dbInterface.deleteMessageBySessionId(sessionKey);
            MessageEntity.deleteMessageBySessionId(sessionKey);

            ConfigurationSp.instance(ctx, loginId).setSessionTop(sessionKey, false);
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    /**
     * 删除会话 包括删除对应的好友验证消息
     * @param sessionKey
     */
    public  void reqRemoveSession(String sessionKey) {
        logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();
        /**直接本地先删除,清楚未读消息*/
        if(sessionMap.containsKey(sessionKey)){
            sessionMap.remove(sessionKey);
//            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);

//            dbInterface.deleteSession(sessionKey);
            SessionEntity.deleteSession(sessionKey);

//            dbInterface.deleteMessageBySessionId(sessionKey);
            MessageEntity.deleteMessageBySessionId(sessionKey);

            String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
            int peerType = Integer.parseInt(sessionInfo[0]);
            int peerId = Integer.parseInt(sessionInfo[1]);
            if(peerType == DBConstant.SESSION_TYPE_SINGLE){
//                dbInterface.deletebuddyVerifyMsgsBySessionId(peerId);
            }

            ConfigurationSp.instance(ctx,loginId).setSessionTop(sessionKey,false);
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    public void deleteAddBuddyAcceptVerifyMsgsBySessionId(Integer sessionId){
//        dbInterface.deleteAddBuddyAcceptVerifyMsgsBySessionId(sessionId);
    }

    /**
     *
     * @param operatorId
     * @param operationType ADD/DEL/EXIT
     * @return
     */
    private String getGroupTipsText(GroupEntity entity, int operatorId, List<GroupMemberEntity> changeMemList, String operationType) {
        List<GroupMemberEntity> mChangeMemList = null;
        if(changeMemList != null){
            mChangeMemList = new ArrayList<>();
            mChangeMemList.addAll(changeMemList);
        }


        int loginId = imLoginManager.getLoginId();
        StringBuilder retBuilder = new StringBuilder();

        String operatorName ="";
        if(loginId == operatorId){
            operatorName = "您";
        }else {
            UserEntity user = contactManager.findContact(operatorId);
            if(user != null){
                String nameStr = !TextUtils.isEmpty(user.getNickName()) ? user.getNickName() : !TextUtils.isEmpty(user.getMainName()) ? user.getMainName() : user.getUserCode();
                operatorName = String.format("\"%s\"", nameStr);
            }else {
                operatorName = String.format("\"\"");
            }
        }

        if("ADD".equals(operationType)){// "'您' 邀请 '您'" 不合适
            Iterator<GroupMemberEntity> it = mChangeMemList.iterator();
            while (it.hasNext()){
                GroupMemberEntity member = it.next();
                if(member.getPeerId() == operatorId)
                    it.remove();
            }
        }

        StringBuilder usersBuilder = new StringBuilder();
        if(mChangeMemList != null && !mChangeMemList.isEmpty()) {
            for(int i=0, size = mChangeMemList.size(); i<size; i++){
                GroupMemberEntity member = mChangeMemList.get(i);
                if(member.getPeerId() == loginId){
                    usersBuilder.append("您");
                }else {
                    usersBuilder.append(member.getNickName());
                }

                if (i < size - 1) {
                    usersBuilder.append("、");
                }
            }
        }

        if ("ADD".equals(operationType)) {
            retBuilder.append(operatorName);
            retBuilder.append("邀请\"");
            retBuilder.append(usersBuilder);
            retBuilder.append("\"");
            retBuilder.append("加入了群聊");
        }else if("JOIN".equals(operationType)){
            retBuilder.append(operatorName);
            retBuilder.append("通过二维码扫描加入群聊");
        } else if ("DEL".equals(operationType)) {
            retBuilder.append("\"");
            retBuilder.append(usersBuilder);
            retBuilder.append("\"");
            retBuilder.append("被");
            retBuilder.append(operatorName);
            retBuilder.append("移出了群聊");
        } else if ("EXIT".equals(operationType)) {
            retBuilder.append(operatorName);
            retBuilder.append("退出了群组");
        }else {
            try {
                int type = Integer.valueOf(operationType);
                switch (type){
//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NOTICE_VALUE:{
                    case 2: {
                        retBuilder.append(operatorName);
                        retBuilder.append("修改群公告:\n\"");
                        retBuilder.append(entity.getNotice());
                        retBuilder.append("\"");
                    }break;

//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NAME_VALUE:{
                    case 3: {
                        retBuilder.append(operatorName);
                        retBuilder.append("修改群名为\"");
                        retBuilder.append(entity.getMainName());
                        retBuilder.append("\"");
                    }break;

//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_ADMIN_VALUE:{
                    case 4: {
                        if(loginId == entity.getCreatorId()){
                            retBuilder.append("您");
                        }else {
//                            GroupMemberEntity admin = dbInterface.findGroupMem(entity.getPeerId(), entity.getCreatorId());
                            GroupMemberEntity admin = GroupMemberEntity.findGroupMem(entity.getPeerId(), entity.getCreatorId());
                            retBuilder.append(String.format("\"%s\"", admin.getNickName()));
                        }
                        retBuilder.append("已成为新群主");
                    }break;

//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_AUTH_VALUE:{
                    case 5: {
                    }break;

//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NORMAL_VALUE:{
                    case 6: {
                    }break;

//                    case IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_AVATAR_VALUE:{ //群头像
                    case 7: {
                        if(loginId == entity.getCreatorId()){
                            retBuilder.append("您");
                        }else {
//                            GroupMemberEntity admin = dbInterface.findGroupMem(entity.getPeerId(), entity.getCreatorId());
                            GroupMemberEntity admin = GroupMemberEntity.findGroupMem(entity.getPeerId(), entity.getCreatorId());
                            retBuilder.append(String.format("\"%s\"", admin.getNickName()));
                        }
                        retBuilder.append("修改了群头像");
                    }break;

                }
            }catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return retBuilder.toString();
    }

    public void updateSession(GroupEntity entity, List<GroupMemberEntity>changeMemList, int opeartorId, String operationType){
        logger.d("recent#updateSession GroupEntity:%s", entity);

        String groupTip = getGroupTipsText(entity, opeartorId, changeMemList, operationType);
        if(TextUtils.isEmpty(groupTip))
            return;

        int loginId = imLoginManager.getLoginId();

        TextMessage groupSysMsg = new TextMessage();
        groupSysMsg.setMsgType(DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT);
        long nowTime = System.currentTimeMillis();
        groupSysMsg.setFromId(opeartorId);
        groupSysMsg.setToId(entity.getPeerId());
        groupSysMsg.setUpdated(nowTime);
        groupSysMsg.setCreated(nowTime);
        groupSysMsg.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        groupSysMsg.setContent(groupTip);

        boolean isSend = groupSysMsg.isSend(loginId);
        groupSysMsg.buildSessionKey(isSend);
        groupSysMsg.setStatus(MessageConstant.MSG_SUCCESS);

        SessionEntity sessionEntity = sessionMap.get(entity.getSessionKey());
        if (sessionEntity == null) {
            sessionEntity = new SessionEntity();
            sessionEntity.setLatestMsgType(DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT);
            sessionEntity.setCreated(entity.getCreated());
            sessionEntity.setPeerType(DBConstant.SESSION_TYPE_GROUP);
            sessionEntity.setPeerId(entity.getPeerId());
            sessionEntity.buildSessionKey();
        }
        sessionEntity.setUpdated(groupSysMsg.getUpdated());
        sessionEntity.setLatestMsgData(groupSysMsg.getContent());
        sessionEntity.setTalkId(entity.getCreatorId());

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);

        SessionEntity.insertOrUpdateMultiData(needDb);
        IMMessageManager.instance().onRecvMessage(groupSysMsg, false);

    }


    /**
     * 1.自己发送消息
     * 2.收到消息
     * @param msg
     */
    public void updateSession(MessageEntity msg) {
        logger.d("recent#updateSession msg:%s", msg);
        if (msg == null) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }
        int loginId = imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);
        // 因为多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();

            // 判断群组的信息是否存在
            if(sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP){
                GroupEntity groupEntity = groupManager.findGroup(peerId);
                //TODO 查验有没有这种情况
                if(groupEntity == null){
//                   groupManager.reqGroupDetailInfo(peerId);
                }

            }
        }else{
            logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());
            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        /**DB 先更新*/
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        SessionEntity.insertOrUpdateMultiData(needDb);

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
    }

    /**
     * 获取某一sessionId的未读消息列表，刷新UI
     * @param msgList
     */
    public void updateSession(List<MessageEntity> msgList) {

        if (msgList == null || msgList.isEmpty()) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }
        int loginId =imLoginManager.getLoginId();
        SessionEntity sessionEntity = null;

        for(MessageEntity msg : msgList){
            boolean isSend = msg.isSend(loginId);
            // 因为多端同步的问题
            int peerId = msg.getPeerId(isSend);

            sessionEntity = sessionMap.get(msg.getSessionKey());
            if (sessionEntity == null) {
                logger.d("session#updateSession#not found msgSessionEntity");
                sessionEntity = EntityChangeEngine.getSessionEntity(msg);
                sessionEntity.setPeerId(peerId);
                sessionEntity.buildSessionKey();
                sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);

                // 判断群组的信息是否存在
                if(sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP){
                    GroupEntity groupEntity = groupManager.findGroup(peerId);
                    if(groupEntity == null){
//                        groupManager.reqGroupDetailInfo(peerId, null);
                    }
                }
            }else{
                logger.d("session#updateSession#msgSessionEntity already in Map");

                //必要时 更新session最后更新时间
                if(sessionEntity.getUpdated() < msg.getUpdated()) {
                    sessionEntity.setUpdated(msg.getUpdated());
                    sessionEntity.setLatestMsgData(msg.getMessageDisplay());
                    sessionEntity.setTalkId(msg.getFromId());
                    sessionEntity.setLatestMsgId(msg.getMsgId());
                    sessionEntity.setLatestMsgType(msg.getMsgType());
                }
            }
        }

        /**DB 先更新*/
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
//        dbInterface.batchInsertOrUpdateSession(needDb);
        SessionEntity.insertOrUpdateMultiData(needDb);
        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
    }


    public List<SessionEntity> getRecentSessionList() {
        List<SessionEntity> recentInfoList = new ArrayList<>(sessionMap.values());
        return recentInfoList;
    }

    private static void sort(List<RecentInfo> data) {
        Collections.sort(data, new Comparator<RecentInfo>() {
            public int compare(RecentInfo o1, RecentInfo o2) {
                Long a =  o1.getUpdateTime();
                Long b = o2.getUpdateTime();

                boolean isTopA = o1.isTop();
                boolean isTopB = o2.isTop();

                if(isTopA == isTopB){
                    // 升序
                    //return a.compareTo(b);
                    // 降序
                    return  b.compareTo(a);
                }else{
                    if(isTopA){
                        return -1;
                    }else{
                        return 1;
                    }
                }

            }
        });
    }

    // 获取最近联系人列表，RecentInfo 是sessionEntity unreadEntity user/group 等等实体的封装
    public List<RecentInfo> getRecentListInfo() {
        /**整理topList*/
        recentSessionList.clear();
        int loginId = IMLoginManager.instance().getLoginId();

        List<SessionEntity> sessionList = getRecentSessionList();
        Map<Integer, UserEntity> userMap = contactManager.getUserMap();
        Map<String, UnreadEntity> unreadMsgMap = IMUnreadMsgManager.instance().getUnreadMsgMap();
        Map<Integer, GroupEntity> groupEntityMap = IMGroupManager.instance().getGroupMap();
        HashSet<String> topList = ConfigurationSp.instance(ctx,loginId).getSessionTopList();

        for(SessionEntity recentSession:sessionList){
            int sessionType = recentSession.getPeerType();
            int peerId = recentSession.getPeerId();
            String sessionKey = recentSession.getSessionKey();

            UnreadEntity unreadEntity = unreadMsgMap.get(sessionKey);
            if(sessionType == DBConstant.SESSION_TYPE_GROUP){
                GroupEntity groupEntity = groupEntityMap.get(peerId);
                RecentInfo recentInfo = new RecentInfo(recentSession,groupEntity,unreadEntity);
                if(topList !=null && topList.contains(sessionKey)){
                    recentInfo.setTop(true);
                }


                //谁说的这条信息，只有群组需要，例如 【XXX:您好】
                int lastFromId = recentSession.getTalkId();
                UserEntity talkUser = userMap.get(lastFromId);
                GroupMemberEntity member = IMGroupManager.instance().findGroupMember(peerId, lastFromId);

                // 用户已经不存在了
                if(talkUser !=null){
                    String  oriContent =  recentInfo.getLatestMsgData();
                    String  finalContent = "";
                    if(recentInfo.getLatestMsgType() == DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT){
                        finalContent = oriContent;
                    }else {
                        if(!TextUtils.isEmpty(oriContent)) {
                            String nameStr = !TextUtils.isEmpty(talkUser.getNickName()) ? talkUser.getNickName() : //对好友的备注
                                    !TextUtils.isEmpty(member.getNickName()) ? member.getNickName() :     //群成员昵称
                                            !TextUtils.isEmpty(talkUser.getMainName()) ? talkUser.getMainName() : talkUser.getUserCode(); //用户昵称/IM账号
                            finalContent = nameStr + ": " + oriContent;
                        }else {
                            finalContent = "";
                        }
                    }

                    recentInfo.setLatestMsgData(finalContent);
                }
                recentSessionList.add(recentInfo);
            }else if(sessionType == DBConstant.SESSION_TYPE_SINGLE){
                UserEntity userEntity = userMap.get(peerId);
                RecentInfo recentInfo = new RecentInfo(recentSession,userEntity,unreadEntity);
                if(topList !=null && topList.contains(sessionKey)){
                    recentInfo.setTop(true);
                }
                recentSessionList.add(recentInfo);
            }
        }
        sort(recentSessionList);
        return recentSessionList;
    }

    public SessionEntity findSession(String sessionKey){
        if(sessionMap.size()<=0 || TextUtils.isEmpty(sessionKey)){return null;}
        if(sessionMap.containsKey(sessionKey)){
            return sessionMap.get(sessionKey);
        }
        return null;
    }

    public PeerEntity findPeerEntity(String sessionKey){
        if(TextUtils.isEmpty(sessionKey)){
            return null;
        }
        // 拆分
        PeerEntity peerEntity = null;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        switch (peerType){
            case DBConstant.SESSION_TYPE_SINGLE: {
                peerEntity  = contactManager.findContact(peerId);
            }break;
            case DBConstant.SESSION_TYPE_GROUP:{
                peerEntity = IMGroupManager.instance().findGroup(peerId);
            }break;
            default:
                throw new IllegalArgumentException("findPeerEntity#peerType is illegal,cause by " +peerType);
        }
        return peerEntity;
    }

    /**------------------------实体的get set-----------------------------*/
    public boolean isSessionListReady() {
        return sessionListReady;
    }

    public void setSessionListReady(boolean sessionListReady) {
        this.sessionListReady = sessionListReady;
    }

}
