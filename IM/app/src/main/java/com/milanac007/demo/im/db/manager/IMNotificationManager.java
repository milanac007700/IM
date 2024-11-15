package com.milanac007.demo.im.db.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.ImAction;
import com.milanac007.demo.im.db.config.SysConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.UnreadEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.sp.ConfigurationSp;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.UnreadEvent;
import com.milanac007.demo.im.logger.Logger;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.service.DownloadService;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.HandlerPost;

import java.io.File;

import de.greenrobot.event.EventBus;

//


/**
 * 伪推送; app退出之后就不会收到推送的信息
 * 通知栏新消息通知
 * a.每个session 只显示一条
 * b.每个msg 信息都显示
 * 配置依赖与 configure
 */
public class IMNotificationManager extends IMManager{
    public static final String APP_ID = "IM_ID";
    public static final String APP_NAME = "IM";
    private static final int App_Notification_id = 10001;

    private Logger logger = Logger.getLogger();
	private static IMNotificationManager inst = new IMNotificationManager();
	public static IMNotificationManager instance() {
			return inst;
	}
    private ConfigurationSp configurationSp;
    private Messenger mServiceMessenger;
    private ServiceConnection conn;

	private IMNotificationManager() {
	}

    @Override
    public void doOnStart() {
	    cancelAllNotifications();
        startDownloadFileService();

        showNotification();
    }

    private void startDownloadFileService(){
        Intent intent = new Intent(ctx, DownloadService.class);
        ctx.startService(intent);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceMessenger = null;
            }
        };
        ctx.bindService(intent, conn, Context.BIND_AUTO_CREATE);

        IntentFilter downloadFilefilter = new IntentFilter(ImAction.INTENT_DOWNLOAD_FINISHED);
        downloadFilefilter.addAction(ImAction.INTENT_DOWNLOAD_FAILED);
        ctx.registerReceiver(mDownloadReceiver, downloadFilefilter);
	}

    public void startDownloadFile(int fileType, String url, String filePath, int reqWidth, int reqHeight){

        Message message = Message.obtain();
        message.what = DownloadService.DOWNLOAD_ING;
        Bundle bundle = new Bundle();
        bundle.putString(DownloadService.DOWNLOAD_URL, url);
        bundle.putInt(DownloadService.DOWNLOAD_TYPE, fileType);
        bundle.putString(DownloadService.DOWNLOAD_FILE_PATH, filePath);
        bundle.putInt(DownloadService.DOWNLOAD_WIDTH, reqWidth);
        bundle.putInt(DownloadService.DOWNLOAD_HEIGHT, reqHeight);
        message.setData(bundle);
        try {
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String url = intent.getStringExtra(DownloadService.DOWNLOAD_URL);
            if(action.equals(ImAction.INTENT_DOWNLOAD_FINISHED)){

            }else if(action.equals(ImAction.INTENT_DOWNLOAD_FAILED)){
                CommonFunction.showToast("下载失败: " + url);
            }
        }
    };

	public void onLoginSuccess() {
	    int loginId = IMLoginManager.instance().getLoginId();
	    configurationSp = ConfigurationSp.instance(ctx, loginId);
	    if(!EventBus.getDefault().isRegistered(inst)) {
	        EventBus.getDefault().register(inst);
        }
	}

    public void reset() {
        if(EventBus.getDefault().isRegistered(inst)) {
            EventBus.getDefault().unregister(inst);
        }
        cancelAllNotifications();
    }

    public void onEventMainThread(UnreadEvent event) {
	    switch (event.event) {
            case UNREAD_MSG_RECEIVED:{
                UnreadEntity unreadEntity = event.entity;
                handleMsgRecv(unreadEntity);
            }break;
        }
    }

    public void onEventMainThread(GroupEvent event) {
        GroupEntity groupEntity = event.getGroupEntity();
        if(groupEntity != null && event.getEvent() == GroupEvent.Event.SHIELD_GROUP_OK) {
            cancelSessionNotifications(groupEntity.getSessionKey());
        }
    }

    private void handleMsgRecv(UnreadEntity entity) {
        logger.d("notification#recv unhandled message");
        int peerId = entity.getPeerId();
        int sessionType =  entity.getSessionType();
        logger.d("notification#msg no one handled, peerId:%d, sessionType:%d", peerId, sessionType);

        //判断是否设定了免打扰
        if(entity.isForbidden()){
            logger.d("notification#GROUP_STATUS_SHIELD");
            return;
        }

        //PC端是否登陆 取消 【暂时先关闭】
//        if(IMLoginManager.instance().isPcOnline()){
//            logger.d("notification#isPcOnline");
//            return;
//        }

        // 全局开关
        boolean globallyIsOpen = configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.NOTIFICATION);
        if (!globallyIsOpen) {
            logger.d("notification#shouldGloballyShowNotification is false, return");
            return;
        }

        // 单独的设置
        boolean singleOpen = configurationSp.getCfg(entity.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
        if (!singleOpen) {
            logger.d("notification#shouldShowNotificationBySession is false, return");
            return;
        }

        //if the message is a multi login message which send from another terminal,not need notificate to status bar
        // 判断是否是自己的消息
        if(IMLoginManager.instance().getLoginId() != peerId){
            showNotification(entity);
        }
    }

    public void cancelAllNotifications() {
        logger.d("notification#cancelAllNotifications");
	    if(ctx == null) {
	        return;
        }

        NotificationManager nm = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
	    if(nm != null) {
            nm.cancelAll();
        }
    }

    /**
     * 在通知栏中删除特定会话的通知
     * @param sessionKey
     */
    public void cancelSessionNotifications(String sessionKey) {
        logger.d("notification#cancelSessionNotifications");
        NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == notifyMgr) {
            return;
        }
        int notificationId = getSessionNotificationId(sessionKey);
        notifyMgr.cancel(notificationId);
    }

    public int getSessionNotificationId(String sessionKey) {
        logger.d("notification#getSessionNotificationId sessionTag:%s", sessionKey);
        int hashNotificationId = (int)hashBKDR(sessionKey);
        logger.d("notification#hashedNotificationId:%d", hashNotificationId);
        return hashNotificationId;
    }

    // come from
    // http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
    private long hashBKDR(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }
        return hash;
    }

    private void showNotification(final UnreadEntity unreadEntity) {
        int peerId = unreadEntity.getPeerId();
        String avatarUrl = "";
        String sender = "";
        String content = unreadEntity.getLatestMsgData();
        String unit = ctx.getString(R.string.msg_cnt_unit);
        int totalUnread = unreadEntity.getUnReadCnt();
        String title = "";
        String conent;

        if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE){
            UserEntity contact = IMContactManager.instance().findContact(peerId);
            if(contact !=null){
                sender = contact.getMainName();
                avatarUrl = contact.getAvatar();
            }else{
                sender = "User_"+peerId;
                avatarUrl = "";
            }
            title = sender;

            if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SHOW_MSG_DETAIL)) {
                conent = String.format("[%d%s] %s", totalUnread, unit, content);
            }else {
                conent = String.format("发来了%d%s消息", totalUnread, unit);
            }

        }else{
            GroupEntity group = IMGroupManager.instance().findGroup(peerId);
            int senderId = unreadEntity.getLatestMsgFromUserId();
            UserEntity talkUser = IMContactManager.instance().findContact(senderId);
            if(group !=null) {
                avatarUrl = group.getAvatar();
            }else{
                avatarUrl = "";
            }

            if(talkUser != null){
                sender = !TextUtils.isEmpty(talkUser.getNickName()) ? talkUser.getNickName() : !TextUtils.isEmpty(talkUser.getMainName()) ? talkUser.getMainName() : talkUser.getUserCode();
            }else{
                sender = "User_"+senderId;
            }

            title = group.getMainName();

            if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SHOW_MSG_DETAIL)) {
                conent = String.format("[%d%s]%s: %s", totalUnread, unit, sender, content);
            }else {
                conent = String.format("%s发来了%d%s消息", sender, totalUnread, unit);
            }
        }

        final String ticker = conent;
        final int notificationId = getSessionNotificationId(unreadEntity.getSessionKey());

        // 设置内容和点击事件
        final Intent intent = new Intent(ImAction.INTENT_BRING_APP_TO_FRONT);
        intent.putExtra(DBConstant.KEY_SESSION_KEY, unreadEntity.getSessionKey());

        logger.d("notification#notification avatarUrl:%s", avatarUrl);
        final String finalTitle = title;
        if(TextUtils.isEmpty(avatarUrl)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = 80;
            options.outHeight = 80;
            Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.male, options);
            if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_GROUP) {
                 bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.group_default, options);
            }
            showInNotificationBar(finalTitle, ticker, bitmap, notificationId, intent);
        }else {
            final String avatarLocalPath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(avatarUrl);
            Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(avatarLocalPath);
            if(bitmap != null) {
                showInNotificationBar(finalTitle, ticker, bitmap, notificationId, intent);
            }else {
                startDownloadFile(DownloadService.DOWNLOAD_TYPE_IMAGE, avatarUrl, avatarLocalPath, 80, 80);
            }
        }
    }

    private void showInNotificationBar(String title,String ticker, Bitmap iconBitmap,int notificationId,Intent intent) {
        logger.d("notification#showInNotificationBar title:%s ticker:%s",title,ticker);

        NotificationManager NM = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if(NM == null) {
            return;
        }

        Notification.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(APP_ID, APP_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            builder = new Notification.Builder(ctx, channel.getId());
        }else {
            builder = new Notification.Builder(ctx);
        }

        builder.setContentTitle(title)
                .setContentText(ticker)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        // this is the content near the right bottom side
        // builder.setContentInfo("content info");
        if(configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.VIBRATION)) {
            // delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
            long[] vibrate = {0, 200, 250, 200};
            builder.setVibrate(vibrate);
        }else {
            logger.d("notification#setting is not using vibration");
        }

        //sound
        if(configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SOUND)) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }else {
            logger.d("notification#setting is not using sound");
        }

        if(iconBitmap != null) {
            builder.setLargeIcon(iconBitmap);
        }

        // if 主Activity is in the background, the system would bring it to the front
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NM.notify(notificationId, notification);

    }

    /**
     * Background execution not allowed 广播无法接收问题的解决方法
     * 出现此报错的原因是Android O中对隐式广播做了限制，
     * 从代码中看，如下四个条件同时满足，则该广播不能被正常收到。
     * 1.r.intent.getComponent() == null
     * 2.r.intent.getPackage() == null
     * 3.r.intent.getFlags() & Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND) == 0
     * 4.!isSignaturePerm(r.requiredPermissions)
     *
     * 解决方法：intent添加packageName
     * https://blog.csdn.net/wangwei890702/article/details/99644607
     */
    /**
     * 常驻通知栏，显示在线状态
     * @param online
     */
    public void showNotification(boolean online) {
        NotificationManager NM = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(ImAction.INTENT_BRING_APP_TO_FRONT);
        intent.setPackage(ctx.getPackageName()); //解决 Background execution not allowed 广播无法接收问题
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(APP_ID, APP_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NM.createNotificationChannel(channel);
            builder = new Notification.Builder(ctx, channel.getId());
        }else {
            builder = new Notification.Builder(ctx);
        }

        String tip = "";
        if (online) {
            tip = "[在线]";
        } else {
            tip = "[离线]";
        }

        builder.setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher_round).setWhen(System.currentTimeMillis())
                .setContentTitle("IM").setContentText(tip)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_NO_CLEAR; // 点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用

        NM.notify(App_Notification_id, notification);
    }


    public void showNotification() {

        NotificationManager NM = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(ImAction.INTENT_BRING_APP_TO_FRONT);
        intent.setPackage(ctx.getPackageName()); //解决 Background execution not allowed 广播无法接收问题
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);//PendingIntent.FLAG_UPDATE_CURRENT

        Notification.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(APP_ID, APP_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NM.createNotificationChannel(channel);
            builder = new Notification.Builder(ctx, channel.getId());
        }else {
            builder = new Notification.Builder(ctx);
        }

        builder.setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round).setWhen(System.currentTimeMillis())
                .setContentTitle("IM")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        final Notification.Builder newBuilder = builder;
        new HandlerPost(8000){

            @Override
            public void doAction() {

                newBuilder.setContentText("hello world");
                Notification notification = newBuilder.build();
                NM.notify(2, notification);
            }
        };

        new HandlerPost(12000){

            @Override
            public void doAction() {

                newBuilder.setContentText("hello 小米");
                Notification notification = newBuilder.build();
                NM.notify(1, notification);

            }
        };

    }

}
