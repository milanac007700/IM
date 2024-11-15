
package com.milanac007.demo.im.db.helper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.专门用来分配序列号
 * 2. 本地消息的唯一msgId键值
 * todo can use AtomicInteger
 */
public class SequenceNumberMaker {

    private volatile AtomicInteger mSquence = new AtomicInteger();

    private volatile long preMsgId = 0;

    private static SequenceNumberMaker maker = new SequenceNumberMaker();

    private SequenceNumberMaker() {
        mSquence.set(0);
    }

    public static SequenceNumberMaker getInstance() {
        return maker;
    }

    public int make() {
        if(mSquence.intValue() >= Integer.MAX_VALUE) {
            mSquence.set(1);
        }
        return mSquence.addAndGet(1);
    }

//    public short make() {
//        synchronized (this) {
//            mSquence++;
//            if (mSquence >= Short.MAX_VALUE)
//                mSquence = 1;
//        }
//        return mSquence;
//    }

    /**依旧比较 Ugly 的解决办法
     * 多线程情况下，生成相同的msgId
     * */
     public int makelocalUniqueMsgId(){
        synchronized(SequenceNumberMaker.this) {
            int timeStamp = (int) (System.currentTimeMillis() % 10000000);
            int localId = timeStamp + 90000000;
            //logger.e("#yingmu2#之前的msgId:%d",preMsgId);
            if (localId == preMsgId) {
                localId++;
                if (localId >= 100000000) {
                    localId = 90000000;
                }
            }
            preMsgId = localId;
            return localId;
        }
    }

    /**比较 Ugly 的解决办法,但是比较实用*/
    public boolean isFailure(int msgId){
        if(msgId>=90000000){
            return true;
        }
        return false;
    }
}
