package com.milanac007.demo.im.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.request.target.Target;
import com.milanac007.demo.im.db.config.ImAction;
import com.milanac007.demo.im.logger.Logger;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.net.DownloadCallable;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.Nullable;

/**
 * Created by zqguo on 2017/3/7.
 */
public class DownloadService extends Service {

    public static final String TAG = "DownloadService";
    public static final String ACTION = "DownloadImageAcion";
    public static final String DOWNLOAD_URL = "DOWNLOAD_URL";
    public static final String DOWNLOAD_TYPE = "DOWNLOAD_TYPE"; //下载类型
    public static final String DOWNLOAD_FILE_PATH = "DOWNLOAD_FILE_PATH"; //下载后的文件路径
    public static final String DOWNLOAD_WIDTH = "DOWNLOAD_WIDTH";//需要下载的尺寸宽度
    public static final String DOWNLOAD_HEIGHT = "DOWNLOAD_HEIGHT";//需要下载的尺寸高度

    public static final int DOWNLOAD_ING = 1;
    public static final int DOWNLOAD_SUCCESS = 2;
    public static final int DOWNLOAD_FAIL = 3;

    //下载类型
    public static final int DOWNLOAD_TYPE_IMAGE = 0;
    public static final int DOWNLOAD_TYPE_AUDIO = 1;
    public static final int DOWNLOAD_TYPE_VIDEO = 2;


    private int reqWidth = Target.SIZE_ORIGINAL;
    private int reqHeight = Target.SIZE_ORIGINAL;

    private Messenger messenger;
    private ExecutorService mThreadPool;

    @Override
    public void onCreate() {
        super.onCreate();
        messenger = new Messenger(mHandler);
        mThreadPool = Executors.newCachedThreadPool();
        Logger.getLogger().d("%s", "DownloadService onCreate.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!mThreadPool.isShutdown()) {
            mThreadPool.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case DOWNLOAD_ING:{
                    Bundle bundle = msg.getData();
                    String url = bundle.getString(DOWNLOAD_URL);
                    int type = bundle.getInt(DOWNLOAD_TYPE);
                    String path = bundle.getString(DOWNLOAD_FILE_PATH);
                    if(type == DOWNLOAD_TYPE_IMAGE){
                        int reqWidth = bundle.getInt(DOWNLOAD_WIDTH, Target.SIZE_ORIGINAL);
                        int reqHeight = bundle.getInt(DOWNLOAD_HEIGHT, Target.SIZE_ORIGINAL);
                        AddDownloadTask(reqWidth, reqHeight, new File(path), url, mHandler);
                    }else {
                        AddDownloadTask(type, new File(path), url, mHandler);
                    }

                }break;

                case DOWNLOAD_SUCCESS:{
                    Intent intent = new Intent(ImAction.INTENT_DOWNLOAD_FINISHED);
                    intent.putExtra(DOWNLOAD_URL, (String)msg.obj);
                    sendBroadcast(intent);
                }break;

                case DOWNLOAD_FAIL:{
                    Intent intent = new Intent(ImAction.INTENT_DOWNLOAD_FAILED);
                    intent.putExtra(DOWNLOAD_URL, (String)msg.obj);
                    sendBroadcast(intent);
                }break;
            }

            return true;
        }
    });



    /**
     * 下载图片
     * @param req_width
     * @param req_height
     */
    public void AddDownloadTask(int req_width, int req_height, final File file, final String reqUrl, final Handler callBack){
        reqWidth = req_width;
        reqHeight = req_height;

        int downloadType = DownloadService.DOWNLOAD_TYPE_IMAGE;
        Future<JSONObject> input = mThreadPool.submit(new DownloadCallable(downloadType, reqUrl, file));

        processResult(downloadType, reqUrl, file, input);

    }

    private void processResult(int downloadType, String reqUrl, final File file, Future<JSONObject> input) {
        boolean result = false;
        JSONObject output = null;
        try {
            output = input.get();
            result = output.getIntValue("resultCode") == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message message = Message.obtain(mHandler);
        message.obj = reqUrl;

        if (result) {
            if(downloadType == DownloadService.DOWNLOAD_TYPE_IMAGE){
                CacheManager.getInstance().addCacheData(file.getPath(), reqWidth, reqHeight);
            }

            if (file.exists()) {
                message.what = DOWNLOAD_SUCCESS;
            } else {
                message.what = DOWNLOAD_FAIL;
                if(file.exists()){
                    file.delete();
                }
            }
        } else {
            CommonFunction.showToast("下载失败: " + output.getString("error"));
            message.what = DOWNLOAD_FAIL;
            if(file.exists()){
                file.delete();
            }
        }

        message.sendToTarget();

    }

    /**
     * @param downloadType 下载类型 参见DownloadImageService定义
     * @param file 下载保存后的文件
     */
    public void AddDownloadTask(final int downloadType, final File file, final String reqUrl, final Handler callback){
        Future<JSONObject> input = mThreadPool.submit(new DownloadCallable(downloadType, reqUrl, file));
        processResult(downloadType, reqUrl, file, input);
    }

}
