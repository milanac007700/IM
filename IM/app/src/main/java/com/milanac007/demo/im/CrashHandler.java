package com.milanac007.demo.im;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.milanac007.demo.im.utils.CommonFunction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;

    private static String PATH;
    private static final String FILE_NAME= "crash";
    private static final String FILE_NAME_SUFFIX = ".txt";

    private static CrashHandler sInstance = null;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    public static CrashHandler getInstance() {
        if(sInstance == null) {
            synchronized (CrashHandler.class) {
                if(sInstance == null) {
                    sInstance = new CrashHandler();
                }
             }
        }
        return sInstance;
    }

    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            dumpExceptionToSDCard(e);
            uploadExceptionToServer();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        e.printStackTrace();
        //如果提供了默认的异常处理器，则交给系统去结束程序，否则将由自己结束自己
        if(mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(t, e);
        }else {
            Process.killProcess(Process.myPid());
        }
    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if(DEBUG) {
                Log.w(TAG, "sdcard unmounted, skip dump exception");
            }
            return;
        }
        PATH = CommonFunction.getRootDir() + "/CrashHandler/log/";
        File dir = new File(PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
        }catch (Exception e) {
            Log.w(TAG, "dump crash info failed.");
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //Android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print('_');
        pw.println(Build.VERSION.SDK_INT);

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //CPU架构
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
    }

    private void uploadExceptionToServer() {
        //TODO
    }


}
