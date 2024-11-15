package com.milanac007.demo.im.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created by zqguo on 2017/3/7.
 */

public class DownLoadImageRunnable {
    public static final String TAG = "DownLoadImageRunnable";

    private GlideUrl glideUrl;
    private String imageUrl;
    private Context context;
    private String filePath;
    private File currentFile;
    private ImageDownLoadCallBack callBack;
    private int reqWidth = Target.SIZE_ORIGINAL;
    private int reqHeight = Target.SIZE_ORIGINAL;
    private SimpleTarget<Bitmap> target;

    public interface ImageDownLoadCallBack {
        void onDownLoadSuccess(String imageUrl);

        void onDownLoadFailed(String imageUrl);
    }


    public DownLoadImageRunnable(Context context, String imageUrl, int reqWidth, int reqHeight, ImageDownLoadCallBack callBack) {
        this.callBack = callBack;
        this.context = context;
        this.imageUrl = imageUrl;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;

        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        builder.addHeader("clientId", Utils.getDeviceId());
        builder.addHeader("refreshTokenGrantType", "refresh_token");
        builder.addHeader("refreshToken", Preferences.getRefreshToken());
//        builder.addHeader("tokenValue", Preferences.getAccessToken());
        LazyHeaders headers = builder.build();
        glideUrl = new GlideUrl(imageUrl, headers);
        filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(imageUrl);
        currentFile = new File(filePath);

        //TODO bug: 尺寸指定无效，下载的依然是原图, 原因待查
        target = new SimpleTarget<Bitmap>(reqWidth, reqHeight) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (resource != null) {
                    Logger.getLogger().d("%s", "target.size: " + resource.getWidth() + ", " + resource.getHeight() + ", url:" + DownLoadImageRunnable.this.imageUrl);
                    // 在这里执行图片保存方法
                    saveImageToCache(resource);
                    if (currentFile.exists()) {
                        DownLoadImageRunnable.this.callBack.onDownLoadSuccess(DownLoadImageRunnable.this.imageUrl);
                    } else {
                        DownLoadImageRunnable.this.callBack.onDownLoadFailed(DownLoadImageRunnable.this.imageUrl);
                    }
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                //显示错误信息
//                Logger.getLogger().w("%s", e, "onException: ");
                Logger.getLogger().d("onException: target.getRequest().isRunning(): %s" , target.getRequest().isRunning()?"true":"false");

                if (currentFile.exists()) {
                    currentFile.delete();
                    filePath = null;
                }
                DownLoadImageRunnable.this.callBack.onDownLoadFailed(DownLoadImageRunnable.this.imageUrl);
            }
        };
    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if(msg.what == 0){

                //TODO
//                try {
//                    Glide.with(context)
//                            .load(glideUrl)
//                            .asBitmap()  //TODO 已废弃
//                            .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
//                            .skipMemoryCache(true) //不缓存内存
//                            .into(target); //java.lang.IllegalArgumentException: You must call this method on the main thread
//
//                }catch (IllegalArgumentException e){
//                    e.printStackTrace();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
            }
            return true;
        }
    });

    public void run() {
        Logger.getLogger().w("%s", "reqSize: " + reqWidth + ", " + reqHeight);
        mHandler.sendEmptyMessage(0);

//        Bitmap bitmap = null;
//        try {
//            bitmap = Glide.with(context)
//                    .load(glideUrl)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
//                    .skipMemoryCache(true) //不缓存内存
//                    .into(reqWidth, reqHeight)
//                    .get();
//
//            if (bitmap != null){
//                Log.d(TAG, "bitmap.size: " + bitmap.getWidth() + ", " + bitmap.getHeight() + ", url:" + imageUrl);
//                // 在这里执行图片保存方法
//                saveImageToCache(bitmap);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (bitmap != null && currentFile.exists()) {
//                callBack.onDownLoadSuccess(imageUrl);
//            } else {
//                callBack.onDownLoadFailed(imageUrl);
//            }
//        }
    }

    public void saveImageToCache(Bitmap bitmap) {

        if(currentFile.exists()){
            currentFile.delete();
        }
        try {
            currentFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                CacheManager.getInstance().addCacheData(currentFile.getPath(), bitmap.getWidth(), bitmap.getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
