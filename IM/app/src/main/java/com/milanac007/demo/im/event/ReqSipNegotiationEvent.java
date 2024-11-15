package com.milanac007.demo.im.event;

/**
 * Created by zqguo on 2017/4/20.
 */
public class ReqSipNegotiationEvent {

    private int userId; //谁请求
    private int callSubcriberId;//主叫
    private int calledSubcriberId;//被叫
    private int qualityType; //视频质量


    public ReqSipNegotiationEvent() {

    }

    public ReqSipNegotiationEvent(int userId, int callSubcriberId, int calledSubcriberId, int qualityType) {
        this.userId = userId;
        this.callSubcriberId = callSubcriberId;
        this.calledSubcriberId = calledSubcriberId;
        this.qualityType = qualityType;
    }

    public int getUserId() {
        return userId;
    }

    public int getCallSubcriberId() {
        return callSubcriberId;
    }

    public int getCalledSubcriberId() {
        return calledSubcriberId;
    }

    public int getQualityType() {
        return qualityType;
    }
}
