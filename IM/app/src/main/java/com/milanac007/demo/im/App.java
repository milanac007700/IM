package com.milanac007.demo.im;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.example.milanac007.pickerandpreviewphoto.MyApplication;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.reflect.Method;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.multidex.MultiDex;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private Logger logger = Logger.getLogger();

    private static Context mContext;
    private static App instance;
    private MyApplication moduleApp; //pickerandpreviewphoto的Application
    public static Context getContext() {
        return mContext;
    }

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    public static final ExecutorService THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    static {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        instance = this;
        CrashHandler.getInstance().init(this);
        startIMService();
        if(moduleApp != null) {
            moduleApp.onCreate(); //用于执行module的一些自定义初始化操作
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                new HttpTest().getByHttpsURLConnection();
//                new HttpTest().getByOkHttpClient();
//            }
//        }).start();

    }

    /**
     * Application本身是没有getApplicationContext()和getBaseContext()这两个方法的，这两个方法其实在Application
     * 的父类ContextWrapper中，其中context是在attachBaseContext(Context base)中赋值的，所以我们重写attachBaseContext的时候，
     * 一定要记得调一遍super.attachBaseContext(base)传入当前context。
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        moduleApp = getModuleApplicationInstance(this);
        try {
            //通过反射调用moduleApplication的attach方法
            Method method = Application.class.getDeclaredMethod("attach", Context.class);
            if(method != null) {
                method.setAccessible(true);
                method.invoke(moduleApp, getBaseContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MyApplication getModuleApplicationInstance(Context context) {
        try {
            if(moduleApp == null) {
                ClassLoader classLoader = context.getClassLoader();
                if(classLoader != null) {
                    Class<?> aClass = classLoader.loadClass(MyApplication.class.getName());
                    if(aClass != null) {
                        moduleApp = (MyApplication)aClass.newInstance();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moduleApp;
    }

    public static App getInstance() {
        return instance;
    }


    private void startIMService() {
        logger.i("%s", "start IMService");
        Intent intent = new Intent();
        intent.setClass(this, IMService.class);
        try {
            startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<Integer> mMissedCallNotifacationIdList = new ArrayList<>();

    public void addToMissedCallNotifacationIdList(int id) {
        mMissedCallNotifacationIdList.add(id);
    }

    public void cancelAllMissedCallNotification() {
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i : mMissedCallNotifacationIdList) {
            notificationManager.cancel(i);
        }
        mMissedCallNotifacationIdList.clear();
    }
}
