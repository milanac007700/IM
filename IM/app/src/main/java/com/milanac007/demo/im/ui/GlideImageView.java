package com.milanac007.demo.im.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.Utils;
//import com.mogujie.tt.DB.DBInterface;
//import com.mogujie.tt.DB.dao.GroupDao;
//import com.mogujie.tt.DB.dao.UserDao;
//import com.milanac007.demo.im.db.manager.IMLoginManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;


/**
 * Created by zqguo on 2017/5/23.
 */
public class GlideImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Context mContext;

    public GlideImageView(Context context) {
        this(context, null, 0);
    }

    public GlideImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlideImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setImageBitmap(String url, PeerEntity peerEntity){
        Bitmap bitmap = null;
        if(peerEntity instanceof UserEntity){
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.male);//默认图标，以后可能要更改
        }else if(peerEntity instanceof GroupEntity){
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.group_default);//默认图标，以后可能要更改
        }
        asyncLoadImage(url, bitmap, peerEntity);
    }


    private void GlideView(Bitmap bitmap, ImageView imageView) {
        if(bitmap != null) {
            Logger.getLogger().d("GlideView Size, width: %d, height:%d", bitmap.getWidth(), bitmap.getHeight());
            new HandlerPost(0, true){
                @Override
                public void doAction() {
                    imageView.setScaleType(ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(bitmap);
                }
            };


//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] bytes = baos.toByteArray();
//
//            Glide.with(mContext)
//                    .load(bytes)
//                    .dontAnimate()
//                    .placeholder(R.drawable.msg_pic_fail)
//                    .error(R.drawable.msg_pic_fail)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
//                    .skipMemoryCache(true) //不缓存内存
//                    .into(imageView);

        }
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);//将drawable绘制在画布canvas上
        return bitmap;
    }

    private void asyncLoadImage(String imageUrl, final Bitmap defaultBitmap, final PeerEntity peerEntity) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException(String.format("can not visit the method from UI thread: ", GlideImageView.class.getName() + "#" + "asyncLoadImage"));
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            if (defaultBitmap != null){
                new HandlerPost(0, true){
                    @Override
                    public void doAction() {
                        setImageBitmap(defaultBitmap);
                    }
                };
            }
            return;
        }

        String icoPath = peerEntity.getAvatarLocalPath();
        if(TextUtils.isEmpty(icoPath)){
            icoPath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(peerEntity.getAvatar());
            peerEntity.setAvatarLocalPath(icoPath);
        }

        //http://124.42.73.14:8000/group1/M00/00/0B/rBAKmljJBSuEI676AAAAANDULqw892.bmp
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        builder.addHeader("clientId", Utils.getDeviceId());
        builder.addHeader("refreshTokenGrantType", "refresh_token");
        builder.addHeader("refreshToken", Preferences.getRefreshToken());
        LazyHeaders headers = builder.build();
        GlideUrl glideUrl = new GlideUrl(imageUrl, headers);

        final int reqWidth = 100;
        final int reqHeight = 100;
        final String headicoPath = icoPath;
        Glide.with(mContext)
                .load(glideUrl)
                .override(reqWidth, reqHeight)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        e.printStackTrace();
                        Utils.showToast(TextUtils.isEmpty(e.getMessage()) ? "异常" : e.getMessage());
                        File file = new File(headicoPath);
                        if(file.exists()){
                            file.delete();
                        }

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        final Bitmap bitmap = drawableToBitamp(resource);
                        GlideView(bitmap, GlideImageView.this);

                        try {
                            File file = new File(headicoPath);
                            FileOutputStream os = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                            os.flush();
                            os.close();

                            CacheManager.getInstance().addCacheData(file.getPath(), reqWidth, reqHeight, false);

                            //TODO update DB
                            if(peerEntity instanceof UserEntity) {
                                UserEntity.insertOrUpdateSingleData((UserEntity)peerEntity);
                            }else {
                                GroupEntity.insertOrUpdateSingleData((GroupEntity) peerEntity);
                            }

//                                    if(peerEntity instanceof UserEntity) {
//                                        DBInterface.instance().updateUserSingleColumn(peerEntity.getPeerId(), UserDao.Properties.AvatarLocalPath, headicoPath);
//
//                                        if(peerEntity.getPeerId() == IMLoginManager.instance().getLoginId()){
//                                            IMLoginManager.instance().setLoginInfo((UserEntity)peerEntity);
//                                        }
//
//                                        if(ImConfig.user.id == peerEntity.getPeerId()) {
//                                            User.updateSingleColumn("mHeadIcoLocalPath", headicoPath);
//                                            User.setImConfigUser(User.getUserLastLogin());
//                                        }
//
//                                    }else if(peerEntity instanceof GroupEntity){
//                                        DBInterface.instance().updateGroupSingleColumn(peerEntity.getPeerId(), GroupDao.Properties.AvatarLocalPath, headicoPath);
//                                    }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .error(new BitmapDrawable(defaultBitmap))
                .placeholder(new BitmapDrawable(defaultBitmap))
                .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
                .skipMemoryCache(true) //不缓存内存
                .into(this);

    }

}
