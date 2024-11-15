package com.milanac007.demo.im.db.callback;

import android.os.Handler;

import com.milanac007.demo.im.logger.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ListenerQueue {
    private static ListenerQueue mInstance = new ListenerQueue();
    public static ListenerQueue getInstance() {
        return mInstance;
    }

    private Logger logger = Logger.getLogger();

    private volatile boolean stopFlag = false;
    private volatile boolean hasTask = false;

    //callback队列
    private Map<Integer, Packetlistener> callBackQueue = new ConcurrentHashMap<>();
    private Handler timerHandler = new Handler();

    public void onStart() {
        logger.d("ListenerQueue#onStart run");
        stopFlag = false;
        startTimer();
    }

    public void onDestory() {
        logger.d("ListenerQueue#onDestory ");
        callBackQueue.clear();
        stopTimer();
    }

    private void startTimer() {
        if(!stopFlag && !hasTask) {
            hasTask = true;
            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timerImpl();
                    hasTask = false;
                    startTimer();
                }
            }, 5000);
        }
    }

    private void stopTimer() {
        stopFlag = true;
    }

    private void timerImpl() {
        long currentRealTime = System.currentTimeMillis();

        for(Map.Entry<Integer, Packetlistener> entry: callBackQueue.entrySet()) {

            Packetlistener packetlistener = entry.getValue();
            int seqNo = entry.getKey();
            long timeRange = currentRealTime - packetlistener.getCreateTime();

            try {
                if(timeRange >= packetlistener.getTimeOut()) {
                    Packetlistener listener = pop(seqNo);
                    if(listener != null) {
                        listener.onTimeout();
                    }
                }
            }catch (Exception e) {
                logger.d("ListenerQueue#timerImpl onTimeout is Error,exception is %s", e.getCause());
            }
        }
    }

    public void push(int seqNo, Packetlistener packetlistener) {
        if(seqNo <= 0 || null == packetlistener) {
            logger.d("ListenerQueue#push error, cause by Illegal params");
            return;
        }
        callBackQueue.put(seqNo, packetlistener);
    }

    public Packetlistener pop(int seqNo) {
        return callBackQueue.remove(seqNo);
    }
}
