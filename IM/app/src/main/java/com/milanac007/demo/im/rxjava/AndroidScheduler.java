package com.milanac007.demo.im.rxjava;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AndroidScheduler implements Executor {

    private static AndroidScheduler instance;

    private Scheduler mMainScheduler;
    private Handler mHandler;

    private AndroidScheduler() {
        mHandler = new Handler(Looper.myLooper());
        mMainScheduler = Schedulers.from(this);
    }

    public static synchronized Scheduler mainThread() {
        if(instance == null) {
            instance = new AndroidScheduler();
        }
        return instance.mMainScheduler;
    }


    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }
}
