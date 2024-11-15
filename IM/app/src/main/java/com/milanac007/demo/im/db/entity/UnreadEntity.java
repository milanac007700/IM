package com.milanac007.demo.im.db.entity;


import com.milanac007.demo.im.db.helper.EntityChangeEngine;

/**
 * 未读session实体，并未保存在DB中
 */
public class UnreadEntity {
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int unReadCnt;
    private int laststMsgId;
    private String latestMsgData;
    private int latestMsgType;
    private int latestMsgFromUserId;
    private String extra; //额外信息， 根据需要调用

    private boolean isForbidden = false;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getUnReadCnt() {
        return unReadCnt;
    }

    public void setUnReadCnt(int unReadCnt) {
        this.unReadCnt = unReadCnt;
    }

    public int getLaststMsgId() {
        return laststMsgId;
    }

    public void setLaststMsgId(int laststMsgId) {
        this.laststMsgId = laststMsgId;
    }

    public String getLatestMsgData() {
        return latestMsgData;
    }

    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public boolean isForbidden() {
        return isForbidden;
    }

    public void setForbidden(boolean isForbidden) {
        this.isForbidden = isForbidden;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgFromUserId() {
        return latestMsgFromUserId;
    }

    public void setLatestMsgFromUserId(int latestMsgFromUserId) {
        this.latestMsgFromUserId = latestMsgFromUserId;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "UnreadEntity{" +
                "sessionKey='" + sessionKey + '\'' +
                ", peerId=" + peerId +
                ", sessionType=" + sessionType +
                ", unReadCnt=" + unReadCnt +
                ", laststMsgId=" + laststMsgId +
                ", latestMsgData='" + latestMsgData + '\'' +
                ", latestMsgType=" + latestMsgType +
                ", latestMsgFromUserId=" + latestMsgFromUserId +
                ", isForbidden=" + isForbidden +
                ", extra=" + extra +
                '}';
    }

    public String buildSessionKey(){
        if(sessionType <=0 || peerId <=0){
            throw new IllegalArgumentException(
                    "SessionEntity buildSessionKey error,cause by some params <=0");
        }
        sessionKey = EntityChangeEngine.getSessionKey(peerId,sessionType);
        return sessionKey;
    }
}
