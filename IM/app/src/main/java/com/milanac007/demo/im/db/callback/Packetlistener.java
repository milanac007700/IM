package com.milanac007.demo.im.db.callback;

public abstract class Packetlistener implements IMListener {
    private long createTime;
    private long timeOut;
    public Packetlistener(long timeOut){
        this.timeOut = timeOut;
        long now = System.currentTimeMillis();
        createTime = now;
    }

    public Packetlistener(){
        this.timeOut = 8*1000;
        long now = System.currentTimeMillis();
        createTime = now;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public abstract void onSuccess(Object response);

    @Override
    public abstract void onTimeout();

    @Override
    public abstract void onFail(String error);
}