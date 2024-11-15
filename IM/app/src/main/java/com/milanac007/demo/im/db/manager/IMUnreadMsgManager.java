package com.milanac007.demo.im.db.manager;


import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.UnreadEntity;
import com.milanac007.demo.im.event.UnreadEvent;
import com.milanac007.demo.im.logger.Logger;

import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * 未读消息相关的处理，归属于messageEvent中
 * 可以理解为MessageManager的又一次拆分
 * 为session提供未读支持。
 * DB 中不保存
 */
public class IMUnreadMsgManager extends IMManager {
    private int totalUnreadCount = 0;
    private boolean unreadListReady = false;

    private Logger logger = Logger.getLogger();

    // 单例
    private static IMUnreadMsgManager inst = new IMUnreadMsgManager();
    public static IMUnreadMsgManager instance() {
        return inst;
    }

    /**key=> sessionKey*/
    private ConcurrentHashMap<String,UnreadEntity> unreadMsgMap = new ConcurrentHashMap<>();

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        unreadListReady = false;
        unreadMsgMap.clear();
    }

    // 未读消息控制器，本地是不存状态的
    public void onNormalLoginOk(){
        unreadMsgMap.clear();
        reqUnreadMsgContactList();
    }

    /**
     * 请求未读消息列表
     */
    private void reqUnreadMsgContactList() {
        // TODO
    }

    /**
     * 会话是否已经被设定为屏蔽
     * @param unreadEntity
     */
    private void addIsForbidden(UnreadEntity unreadEntity){
        if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_GROUP){
            GroupEntity groupEntity= IMGroupManager.instance().findGroup(unreadEntity.getPeerId());
            if(groupEntity !=null && groupEntity.getStatus() == DBConstant.GROUP_STATUS_SHIELD){
                unreadEntity.setForbidden(true);
            }
        }
    }

    /**设定未读回话为屏蔽回话 仅限于群组 todo*/
    public void setForbidden(String sessionKey,boolean isFor){
        UnreadEntity unreadEntity =  unreadMsgMap.get(sessionKey);
        if(unreadEntity !=null){
            unreadEntity.setForbidden(isFor);
        }
    }

    public void add(MessageEntity msg) {
        //更新session list中的msg信息
        //更新未读消息计数
        if(msg == null){
            logger.d("unread#unreadMgr#add msg is null!");
            return;
        }
        // isFirst场景:出现一条未读消息，出现小红点，需要触发 [免打扰的情况下]
        boolean isFirst = false;
        logger.d("unread#unreadMgr#add unread msg:%s", msg);
        UnreadEntity unreadEntity;
        int loginId = IMLoginManager.instance().getLoginId();
        String sessionKey = msg.getSessionKey();
        boolean isSend = msg.isSend(loginId);
        if(isSend){
            IMNotificationManager.instance().cancelSessionNotifications(sessionKey);
            return;
        }

        if(unreadMsgMap.containsKey(sessionKey)){
            unreadEntity = unreadMsgMap.get(sessionKey);
            // 判断最后一条msgId是否相同
            if(unreadEntity.getLaststMsgId() == msg.getMsgId()){
                return;
            }
            unreadEntity.setUnReadCnt(unreadEntity.getUnReadCnt()+1);
        }else {
            isFirst = true;
            unreadEntity = new UnreadEntity();
            unreadEntity.setUnReadCnt(1);
            unreadEntity.setPeerId(msg.getPeerId(isSend));
            unreadEntity.setSessionType(msg.getSessionType());
            unreadEntity.buildSessionKey();
        }

        unreadEntity.setLatestMsgData(msg.getMessageDisplay());
        unreadEntity.setLaststMsgId(msg.getMsgId());
        unreadEntity.setLatestMsgFromUserId(msg.getFromId());
        addIsForbidden(unreadEntity);

        /**放入manager 状态中*/
        unreadMsgMap.put(unreadEntity.getSessionKey(),unreadEntity);

        /**没有被屏蔽才会发送广播*/
        if(!unreadEntity.isForbidden() || isFirst) {
            UnreadEvent unreadEvent = new UnreadEvent();
            unreadEvent.event = UnreadEvent.Event.UNREAD_MSG_RECEIVED;
            unreadEvent.entity = unreadEntity;
            triggerEvent(unreadEvent);
        }
    }

    public void ackReadMsg(UnreadEntity entity){
        // TODO 用户多端同步
    }

    public void ackReadMsg(MessageEntity entity){
        // TODO 用户多端同步
    }

    /**
     * 备注: 先获取最后一条消息
     * 1. 清除回话内的未读计数
     * 2. 发送最后一条msgId的已读确认
     * @param sessionKey
     */
    public void readUnreadSession(String sessionKey){
        logger.d("unread#readUnreadSession# sessionKey:%s", sessionKey);
        if(unreadMsgMap.containsKey(sessionKey)){
            UnreadEntity entity = unreadMsgMap.remove(sessionKey);
            ackReadMsg(entity);
            triggerEvent(new UnreadEvent(UnreadEvent.Event.SESSION_READED_UNREAD_MSG));
        }
    }

    /**
     * 继承该方法实现自身的事件驱动
     * @param event
     */
    public synchronized void triggerEvent(UnreadEvent event) {
        switch (event.event){
            case UNREAD_MSG_LIST_OK:
                unreadListReady = true;
                break;
        }

        EventBus.getDefault().post(event);
    }

    /**----------------实体set/get-------------------------------*/
    public ConcurrentHashMap<String, UnreadEntity> getUnreadMsgMap() {
        return unreadMsgMap;
    }

    public int getTotalUnreadCount() {
        int count = 0;
        for(UnreadEntity entity:unreadMsgMap.values()){
            if(!entity.isForbidden()){
                count  = count +  entity.getUnReadCnt();
            }
        }
        return count;
    }

    public boolean isUnreadListReady() {
        return unreadListReady;
    }
}
