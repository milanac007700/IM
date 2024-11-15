package com.milanac007.demo.im.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.logger.Logger;

import java.util.List;

public class BringToFrontReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.getLogger().i("BringToFrontReceiver onReceive.");
        // 1.获取ActivityManager
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 2.获得当前运行的task
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(20);
        for(ActivityManager.RunningTaskInfo info: taskList) {
            // 3.找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
            if(info.topActivity.getPackageName().equals(context.getPackageName())) {
                try {
                    Intent resultIntent = new Intent(context, Class.forName(info.topActivity.getClassName()));
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(resultIntent);
                    return;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        // 4.若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(resultIntent);
    }
}
