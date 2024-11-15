package com.milanac007.demo.im.event;

/**
 * Created by zqguo on 2016/11/30.
 */
public class CallRecordChangeEvent {
    private int peerId;

    public CallRecordChangeEvent(){

    }

    public CallRecordChangeEvent(int peerId){
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }
}
