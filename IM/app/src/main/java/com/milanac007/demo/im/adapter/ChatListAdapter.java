
package com.milanac007.demo.im.adapter;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONObject;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.AudioMessage;
import com.milanac007.demo.im.db.entity.msg.ImageMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.entity.msg.VideoMessage;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.ChatFragment;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.manager.FaceManager;
import com.milanac007.demo.im.net.CustomFileDownload;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.ui.CustomRoundProgressBar;
import com.milanac007.demo.im.ui.LinkMovementClickMethod;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.Utils;
//import com.mogujie.tt.DB.DBInterface;


import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ChatListAdapter extends SelectBaseAdapter {
    private static final String TAG = "ChatListAdapter";
    private Context context;
    private BaseFragment mCurrentFragment;
    private LayoutInflater inflater;
    private List<MessageEntity> mMsgList;
    private MediaPlayer mediaPlayer;
    private Animation loadingAnim;
    private AnimationDrawable animationDrawable;
    private List<MessageEntity> showTimeMsgIdList = new ArrayList<>();//需要显示时间ui的msgId集合

    /**震动服务*/
    private Vibrator vib = (Vibrator) App.getInstance().getSystemService(Service.VIBRATOR_SERVICE);

    private IMService imService;
    private UserEntity loginUser;
    private String sessionKey;
    private int sessionType;
    private int sessionId;

    private static final int TAG_KEY_URI = R.id.loader_uri;

    private final Bitmap mPlaceholderBitmap;
    private final Bitmap mRecvVoiceBitmap;
    private final Bitmap mSendVoiceBitmap;

    private int mMode = R.id.NORMAL_MODE;
    public void setMode(int mode){
        mMode = mode;
        notifyDataSetChanged();
    }

    public int getMode(){
        return mMode;
    }

    public  ChatListAdapter(BaseFragment fragment, IMService imService, UserEntity loginUser, String sessionKey, List<MessageEntity> msgList) {
        this.imService = imService;
        this.loginUser = loginUser;
        this.context = fragment.getActivity();
        mCurrentFragment = fragment;
        inflater = LayoutInflater.from(this.context);
        loadingAnim = AnimationUtils.loadAnimation(context, R.anim.roatate_anim);

        this.sessionKey = sessionKey;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        sessionType = Integer.parseInt(sessionInfo[0]);
        sessionId = Integer.parseInt(sessionInfo[1]);

        mPlaceholderBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.msg_pic_fail);
        mRecvVoiceBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.volumn_recv_default);
        mSendVoiceBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.volumn_send_default);

        if(mMsgList == null) {
            mMsgList = new ArrayList<>();
        }
        mMsgList.addAll(msgList);

        sortData();
    }

    public List<MessageEntity> getAllData(){
        return mMsgList;
    }

    @Override
    public int getCount() {
        return mMsgList == null ? 0 : mMsgList.size();
    }

    public void clearData(){
        if(mMsgList != null && !mMsgList.isEmpty())
            mMsgList.clear();
    }


    /**
     * 集合头部添加数据，不刷新
     * @param list
     */
    public void appendData(List<MessageEntity> list) {
        if(list == null || list.isEmpty())
            return;

        mMsgList.addAll(0, list);
        sortData();
    }

    @Override
    public Object getItem(int position) {
        return mMsgList == null ? null : mMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public long getLastCreatedTime(){

        if(mMsgList == null || mMsgList.isEmpty())
            return  0;

        long time = mMsgList.get(0).getCreated();
        return time;
    }

    private void sortData() {
        showTimeMsgIdList.clear();
        Collections.sort(mMsgList, new ChatListAdapter.MessageTimeComparator()); //按消息时间逆序

        int preTime = 0;
        int nextTime = 0;
        for (MessageEntity msg : mMsgList) {
            nextTime = (int)(msg.getCreated()/1000);

            if(nextTime - preTime > 60){ //大于一分钟
                showTimeMsgIdList.add(msg);
            }

            preTime = nextTime;
        }
    }

    public void updateData(){
        sortData();
        notifyDataSetChanged();
    }

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    public void loadHistoryList(final List<MessageEntity> historyList) {

        if (null == historyList || historyList.isEmpty()) {
            return;
        }

        // 如果是历史消息，从头开始加
        mMsgList.addAll(0, historyList);
        updateData();
    }

    public void addItem(MessageEntity msg) {
        if(!mMsgList.contains(msg)) {
            int preTime = mMsgList.size()> 0 ? (int)(mMsgList.get(mMsgList.size()-1).getCreated()/1000) : 0;
            mMsgList.add(msg);
            int nextTime = (int)(msg.getCreated()/1000);
            if(nextTime - preTime > 60){ //大于一分钟
                showTimeMsgIdList.add(msg);
            }
            notifyDataSetChanged();
        }
    }

    //TODO
    public void updateItemState(MessageEntity msg){
        if(!sessionKey.equals(msg.getSessionKey())) //非该会话的消息
            return;

        long dbId = msg.getId();
        int msgId = msg.getMsgId();
        int len = mMsgList.size();
        for (int index = len - 1; index >= 0; index--) {
            MessageEntity entity = mMsgList.get(index);
            if(entity.getId() == null){
                Logger.getLogger().e("%s", "msg updateItemState: entity.getId() == null");
                continue;
            }


            if (dbId == entity.getId() && msgId == entity.getMsgId()) {
                mMsgList.set(index, msg);
                notifyDataSetChanged();
                return;
            }
        }

    }

    public void removeItems(List<MessageEntity> msgList) {
        for(MessageEntity msg: msgList) {
            mMsgList.remove(msg);
            showTimeMsgIdList.remove(msg);
        }
        notifyDataSetChanged();
    }

    public void removeItem(MessageEntity msg) {
        if (mMsgList.contains(msg)) {
            mMsgList.remove(msg);
            showTimeMsgIdList.remove(msg);

            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final MessageEntity msg = (MessageEntity) getItem(position);
        int msgType = msg.getMsgType();

        switch (msgType){
            case DBConstant.MSG_TYPE_SINGLE_TEXT:
            case DBConstant.MSG_TYPE_GROUP_TEXT:
                return getTextMsgView((TextMessage)msg, convertView);
            case DBConstant.MSG_TYPE_SINGLE_IMG:
            case  DBConstant.MSG_TYPE_GROUP_IMG:
                return getImageMsgView((ImageMessage)msg, convertView);
            case DBConstant.MSG_TYPE_SINGLE_AUDIO:
            case  DBConstant.MSG_TYPE_GROUP_AUDIO:
                return getVoiceMsgView(position, (AudioMessage)msg, convertView);
            case DBConstant.MSG_TYPE_SINGLE_VEDIO:
            case  DBConstant.MSG_TYPE_GROUP_VEDIO:
                return getVideoMsgView((VideoMessage)msg, convertView);
            case DBConstant.MSG_TYPE_SINGLE_SYSTEM_TEXT:
            case DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT:
            case DBConstant.MSG_TYPE_NEED_ADD_BUDDY_VERIFY_SYSTEM_TEXT: //TODO
                return getSystemMsgView((TextMessage)msg, convertView);
        }

        return convertView;
    }

    private void setSystemMsgData(final TextMessage msg, final SystemMsgViewHolder holder) {
        setMsgDateTime(msg, holder.send_msg_date);
        holder.system_msg_content.setText(msg.getContent());
        if(msg.getMsgType() == DBConstant.MSG_TYPE_NEED_ADD_BUDDY_VERIFY_SYSTEM_TEXT){
            setOnClickListener(msg, holder.system_msg_content);
        }
    }

    private void setOnClickListener(final TextMessage msg, TextView msgView){

        msgView.setMovementMethod(LinkMovementClickMethod.getInstance()); //设置可点击
        final String msgText = msg.getContent();
        final String sendVerifyMsgText = "发送好友验证";
        msgView.append(Html.fromHtml(sendVerifyMsgText, null, new Html.TagHandler() {
            @Override
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

                String str = output.toString();
                Pattern pattern = Pattern.compile(sendVerifyMsgText, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(str);

                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    output.setSpan(new AddBuddyVerifyClickSpan(msg.getFromId()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }));
    }

    class AddBuddyVerifyClickSpan extends ClickableSpan{
        private int buddyid;

        AddBuddyVerifyClickSpan(int buddyid){
            this.buddyid = buddyid;
        }

        @Override
        public void onClick(View widget) {
            if(mCurrentFragment instanceof ChatFragment) {
                ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                chatFragment.sendAddBuddyVerifyMsg(buddyid);
            }
        }
    }

    private void setVideoView(final VideoMessage msg, final VideoMsgViewHolder holder, final ViewGroup layout,
                              final Button playBtn, final ImageView thumbnailImageView, final ImageView unreadStateView) {


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == R.id.NORMAL_MODE) {
                    asyncloadVideoView(msg, thumbnailImageView, playBtn, layout, holder);
                }else {
                    onClickItemCallback(msg, holder.seleced_state_imageview);
                }
            }
        });

        playBtn.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if(mMode == R.id.NORMAL_MODE){
                    vib.vibrate(15);//只震动15ms，一次 单位：ms
                    if(mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                        chatFragment.showLongPressDialog(msg);
                    }
                    return true;
                }
                return false;
            }
        });

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(mMode == R.id.MULTIPLE_CHOICE_MODE) {
                    //方式1：消费事件
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.i("onTouch", "ACTION_DOWN from: VideoView");
                        return true;
                    }else if(event.getAction() == MotionEvent.ACTION_UP) {
                        onClickItemCallback(msg, holder.seleced_state_imageview);
                    }
                    return false;

                }else if(mMode == R.id.NORMAL_MODE){
                    View view = layout.getChildAt(0);
                    if(view instanceof VideoView){
                        VideoView videoView = (VideoView)view;
                        if(videoView.isPlaying()){
                            videoView.stopPlayback();

                            thumbnailImageView.setVisibility(View.VISIBLE);
                            playBtn.setVisibility(View.VISIBLE);
                            layout.removeView(videoView);
                        }
                    }
                }

                return false;
            }
        });


        layout.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {

                if(mMode == R.id.NORMAL_MODE){
                    vib.vibrate(15);//只震动15ms，一次 单位：ms
                    if(mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                        chatFragment.showLongPressDialog(msg);
                    }
                    return true;
                }
                return false;
            }
        });


        String finalUri, uri;
        boolean isSend = false;
        if(msg.isSend(loginUser.getPeerId())) {
            finalUri = msg.getThumbnailPath();
            uri = finalUri;
            isSend = true;
        }else {
            finalUri = msg.getThumbnailUrl();
            uri = finalUri;
            int index = uri.indexOf("?");
            if(index > 0) {
                uri = uri.substring(0, index);
            }
        }

        Bitmap bitmap = CacheManager.getInstance().getBitmapFromMemCache(uri);
        if(bitmap != null) {
            setVideoUI(bitmap, thumbnailImageView, playBtn, msg, holder);
//            setVideoUI(bitmap, thumbnailImageView);
            return;
        }

        if(canCancelTask(finalUri, thumbnailImageView)) {
            boolean isSendFlag = isSend;
            TheWorkTask workTask = new TheWorkTask(finalUri, thumbnailImageView, new Callable() {
                @Override
                public Object call() throws Exception {
                    msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
                    Bitmap bmp = loadBitmap(finalUri, isSendFlag, false);
                    if(bmp != null) {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
                        // DBInterface.instance().insertOrUpdateMessage(msg);
                        MessageEntity.insertOrUpdateSingleData(msg);

                        if(finalUri == getTheWorkTask(thumbnailImageView).uri){
                            new HandlerPost(0, true){
                                @Override
                                public void doAction() {
                                    setVideoUI(bmp, thumbnailImageView, playBtn, msg, holder);
                                }
                            };
                        }
                    }else {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
//                        DBInterface.instance().insertOrUpdateMessage(message);
                        MessageEntity.insertOrUpdateSingleData(msg);
                    }
                    return null;
                }
            });

            AsyncDrawable drawable = new AsyncDrawable(context.getResources(), mPlaceholderBitmap, workTask);
            thumbnailImageView.setImageDrawable(drawable);
            App.THREAD_POOL_EXECUTOR.submit(workTask);
        }

    }

    /**
     * 获取视频随机的缩略图
     *
     * @param filePath
     * @return
     */
    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            Logger.getLogger().e("%s", e.getMessage());
        } catch (RuntimeException e) {
            Logger.getLogger().e("%s", e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                Logger.getLogger().e("%s", e.getMessage());
            }
        }

        return bitmap;
    }

    private void setVideoUI(Bitmap previewBitmap, final ImageView thumbnailImageView, final Button playBtn, final VideoMessage msg, final VideoMsgViewHolder holder) {
        if (previewBitmap == null)
            return;

        //根据图片尺寸初始化控件大小
        final ViewGroup.LayoutParams params = thumbnailImageView.getLayoutParams();
        List<Integer> size = getReqSizeFromBitmap(previewBitmap.getWidth(), previewBitmap.getHeight());
        if(size == null)
            return;

        params.width = size.get(0);
        params.height = size.get(1);

        thumbnailImageView.setLayoutParams(params);
        thumbnailImageView.setImageBitmap(previewBitmap);
        playBtn.setVisibility(View.VISIBLE);

        if(holder.roundProgressBar != null) {
            holder.roundProgressBar.setVisibility(View.GONE);
        }

//        if (msg.isSend(loginUser.getPeerId())) {
//            holder.videoView_my_preview.setImageBitmap(previewBitmap);
//
//            holder.button_play_my.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mMode == R.id.NORMAL_MODE) {
//                        asyncDownVideoFile(msg, holder);
//                    }else {
//                        onClickItemCallback(msg, holder.seleced_state_imageview);
//                    }
//                }
//            });
//
//            holder.video_capture_my_layout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mMode == R.id.NORMAL_MODE) {
//                        if(mCurrentFragment instanceof ChatFragment){
//                            View view = holder.video_capture_my_layout.getChildAt(0);
//                            if(view instanceof VideoView){
//                                VideoView videoView = (VideoView)view;
//                                if(videoView.isPlaying()){
//                                    videoView.stopPlayback();
//                                }
//
//                                videoView.setVisibility(View.GONE);
//                                holder.videoView_my_preview.setVisibility(View.VISIBLE);
//                                holder.button_play_my.setVisibility(View.VISIBLE);
//                            }
//
//                            if (!TextUtils.isEmpty(msg.getVideoPath()) && new File(msg.getVideoPath()).exists()){
//                                ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
//                                chatFragment.playVideo(msg.getVideoPath());
//                            }
//
//                        }
//                    }else {
//                        onClickItemCallback(msg, holder.seleced_state_imageview);
//                    }
//                }
//            });
//
//            holder.video_capture_my_layout.setOnLongClickListener(new OnLongClickListener(){
//                @Override
//                public boolean onLongClick(View v) {
//                    if(mMode == R.id.NORMAL_MODE) {
//                        vib.vibrate(15);//只震动15ms，一次 单位：ms
//                        if(mCurrentFragment instanceof ChatFragment) {
//                            ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
//                            chatFragment.showLongPressDialog(msg);
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            });
//
//
//        } else {
//            holder.videoView_buddy_preview.setImageBitmap(previewBitmap);
//            holder.button_play_buddy.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mMode == R.id.NORMAL_MODE) {
//                        asyncDownVideoFile(msg, holder);
//                    }else {
//                        onClickItemCallback(msg, holder.seleced_state_imageview);
//                    }
//                }
//            });
//
//            holder.video_capture_buddy_layout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mMode == R.id.NORMAL_MODE) {
//                        if (mCurrentFragment instanceof ChatFragment) {
//
//                            View view = holder.video_capture_buddy_layout.getChildAt(0);
//                            if (view instanceof VideoView) {
//                                VideoView videoView = (VideoView) view;
//                                if (videoView.isPlaying()) {
//                                    videoView.stopPlayback();
//                                }
//                                videoView.setVisibility(View.GONE);
//                                holder.videoView_buddy_preview.setVisibility(View.VISIBLE);
//                                holder.button_play_buddy.setVisibility(View.VISIBLE);
//                            }
//
//                            if (!TextUtils.isEmpty(msg.getVideoPath()) && new File(msg.getVideoPath()).exists()) {
//                                ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
//                                chatFragment.playVideo(msg.getVideoPath());
//                            }
//                        }
//                    }else {
//                        onClickItemCallback(msg, holder.seleced_state_imageview);
//                    }
//                }
//            });
//
//            holder.video_capture_buddy_layout.setOnLongClickListener(new OnLongClickListener(){
//                @Override
//                public boolean onLongClick(View v) {
//
//                    if(mMode == R.id.NORMAL_MODE) {
//                        vib.vibrate(15);//只震动15ms，一次 单位：ms
//                        if(mCurrentFragment instanceof ChatFragment) {
//                            ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
//                            chatFragment.showLongPressDialog(msg);
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            });
//
//        }
    }

    /**
     * 下载视频缩略图
     *
     * @param msg
     * @param
     */
//    private void asyncDownVideoThumbnail(final VideoMessage msg, final VideoMsgViewHolder holder) {
//
//        final int index = msg.getVideoUrl().indexOf("?");
//        int reqWidth = 480;
//        int reqHeight = 480;
//        if(index != -1){
//            String subStr = msg.getVideoUrl().substring(index+1);
//            String[] size = subStr.split("x");
//            reqWidth = Integer.parseInt(size[0]);
//            reqHeight = Integer.parseInt(size[1]);
//        }
//
//        String filePath = msg.getThumbnailPath();
//        if(!TextUtils.isEmpty(filePath)){
//            Bitmap previewBitmap = CacheManager.getInstance().getBitmapFormCache(filePath);
//            if(previewBitmap != null){
//                setVideoUI(previewBitmap, msg, holder);
//                return;
//            }else if(new File(filePath).exists()){
//                previewBitmap = CacheManager.getInstance().addCacheData(filePath, reqWidth, reqHeight);
//                setVideoUI(previewBitmap, msg, holder);
//                return;
//            }
//        }
//
//
//        final String path = CommonFunction.getDirUserTemp() + CommonFunction.getImageFileNameByUrl(msg.getThumbnailUrl());
//        msg.setThumbnailPath(path);
//
//
//        final int req_width = reqWidth;
//        final int req_height = reqHeight;
//
//        final File file = new File(path);
//        if (!file.exists()) {
//            msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
//            //TODO downlaod and play
//            String url = msg.getThumbnailUrl();
//            new CustomFileDownload(url, file, new CustomFileDownload.DownloadListener() {
//                @Override
//                public void onDownloadEnd(JSONObject input) {
//                    boolean result = input.getBoolean("success");
//                    if (result) {
//                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
//                        Bitmap previewBitmap = CacheManager.getInstance().addCacheData(path, req_width, req_height);
//                        setVideoUI(previewBitmap, msg, holder);
////                        DBInterface.instance().insertOrUpdateMessage(msg);
//                        MessageEntity.insertOrUpdateSingleData(msg);
//                    } else {
//                        Toast.makeText(context, "下载失败: " + input.getString("error"), Toast.LENGTH_SHORT).show();
//                        if(file.exists()){
//                            file.delete();
//                        }
//                    }
//                }
//
//                @Override
//                public void onProgress(int value) {
//
//                }
//
//            }).execute();
//        } else {
//            //TODO play
//            msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
//            Bitmap previewBitmap = CacheManager.getInstance().addCacheData(path, reqWidth, reqHeight);
//            setVideoUI(previewBitmap, msg, holder);
////            DBInterface.instance().insertOrUpdateMessage(msg);
//            MessageEntity.insertOrUpdateSingleData(msg);
//
//        }
//    }

    private void playVideo(final VideoMessage msg, final ImageView thumbnailImageView, final Button playBtn, final ViewGroup layout) {
        final VideoView videoView = new VideoView(context);

        ViewGroup.LayoutParams lp = thumbnailImageView.getLayoutParams();
        final int reqWidth = lp.width;
        final int reqHeight = lp.height;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(reqWidth, reqHeight); //根据屏幕宽度设置预览控件的尺寸，为了解决预览拉伸问题
        videoView.setLayoutParams(layoutParams);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                thumbnailImageView.setVisibility(View.VISIBLE);
                playBtn.setVisibility(View.VISIBLE);
                layout.removeView(videoView);
            }
        });

        layout.addView(videoView, 0);
        videoView.setVisibility(View.VISIBLE);
        thumbnailImageView.setVisibility(View.GONE);
        playBtn.setVisibility(View.GONE);

        videoView.setVideoPath(msg.getVideoPath());
//         videoView.setMediaController(new MediaController(context)); //设置了一个播放控制器。
        videoView.start(); //程序运行时自动开始播放视频。
        videoView.requestFocus(); //播放窗口为当前窗口
    }

    private void asyncloadVideoView(final VideoMessage msg, final ImageView thumbnailImageView, final Button playBtn, final ViewGroup layout, final VideoMsgViewHolder holder) {
        String filePath = msg.getVideoPath();
        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            playVideo(msg, thumbnailImageView, playBtn, layout);
        } else {
            int index = msg.getVideoUrl().indexOf("?");
            String videoUrl = msg.getVideoUrl();
            if(index > 0){
                videoUrl = msg.getVideoUrl().substring(0, index);
            }

//            DBInterface.instance().insertOrUpdateMessage(msg);
            MessageEntity.insertOrUpdateSingleData(msg);

            final File file = new File(filePath);
            new CustomFileDownload(videoUrl, file, new CustomFileDownload.DownloadListener() {
                @Override
                public void onDownloadEnd(JSONObject input) {
                    CommonFunction.hideToast();
                    holder.roundProgressBar.setVisibility(View.GONE);
                    boolean result = input.getBoolean("success");
                    if (result) {
                        playVideo(msg, thumbnailImageView, playBtn, layout);
                    } else {
                        CommonFunction.showToast("下载失败: " +input.getString("error"));
                        if(file.exists()){
                            file.delete();
                        }
                    }
                }

                @Override
                public void onProgress(int value) {
                    holder.roundProgressBar.setVisibility(View.VISIBLE);
                    holder.roundProgressBar.setValue(value/100.0f);
                }

            }).execute();
        }
    }


    private void setVideoMsgData(final VideoMessage msg, final VideoMsgViewHolder holder) {

        initBaseViewHolderData(msg, holder);

        if (msg.isSend(loginUser.getPeerId())) {
            setVideoView(msg, holder, holder.video_capture_my_layout, holder.button_play_my, holder.videoView_my_preview,null);
        } else {
            setVideoView(msg, holder, holder.video_capture_buddy_layout, holder.button_play_buddy, holder.videoView_buddy_preview, holder.seleced_state_imageview);
        }
    }

    public int getMetadata(String path) {
        int durationValue = 0; //秒数
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Logger.getLogger().d("%s", path);
        try {
            mmr.setDataSource(path);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            durationValue = (int) (Long.valueOf(duration) / 1000);


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            try {
                mmr.release();
            } catch (RuntimeException e) {
                Logger.getLogger().e("%s", e.getMessage());
            }
            return durationValue;
        }

    }

    public boolean loadVoiceFromHttp(String url, String filePath) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI thread.");
        }

        try {
             FileOutputStream outputStream = new FileOutputStream(filePath);
            return downloadUrlToStream(url, outputStream);
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void refreshVoiceView(final AudioMessage msg, final TextView textView, final View layout, final ImageView unreadStateView) {
        //解决getMetadata() 不同品牌的手机算出来的时长不一致的bug
        int durationValue = msg.getAudiolength();
        if(durationValue > 0) {
            textView.setText(String.format("%d\"", durationValue));
            textView.setVisibility(View.VISIBLE);
        }else {
            textView.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams params = layout.getLayoutParams();
        int screenWidth = CommonFunction.getWidthPx();
        int onePiece = screenWidth / 5;
        int maxLength = screenWidth * 3 / 5;
        int valueLength = maxLength * durationValue / 60; //最大时长60秒
        valueLength += onePiece;
        if (valueLength > maxLength) {
            valueLength = maxLength;
        }
        params.width = valueLength;
        layout.setLayoutParams(params);

        if (!msg.isSend(loginUser.getPeerId()) && msg.getReadStatus() == MessageConstant.AUDIO_UNREAD) {
            if (unreadStateView != null)
                unreadStateView.setVisibility(View.VISIBLE);
        } else {
            if (unreadStateView != null)
                unreadStateView.setVisibility(View.GONE);
        }
    }

    /**
     * @param msg
     * @param unreadStateView 自己发的消息， 传null
     */
    private void setVoiceView(final AudioMessage msg, final VoiceMsgViewHolder viewHolder, final View layout,
                              final TextView textView, final ImageView imageView, final ImageView unreadStateView) {

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMode == R.id.NORMAL_MODE){
                    clickVoiceView(msg, viewHolder);
                }else {
                    onClickItemCallback(msg, viewHolder.seleced_state_imageview);
                }
            }
        });

        layout.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {

                if(mMode == R.id.NORMAL_MODE){
                    vib.vibrate(15);//只震动15ms，一次 单位：ms
                    if(mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                        chatFragment.showLongPressDialog(msg);
                    }
                    return true;
                }
                return false;
            }
        });


        String finalUri;
        Bitmap placeholderBitmap = null;
        if(msg.isSend(loginUser.getPeerId())) {
            finalUri = msg.getAudioPath();
            placeholderBitmap = mSendVoiceBitmap;
        }else {
            finalUri = msg.getUrl();
            placeholderBitmap = mRecvVoiceBitmap;
        }

        if(new File(msg.getAudioPath()).exists()) {
            refreshVoiceView(msg, textView, layout, unreadStateView);
            return;
        }

        if(canCancelTask(finalUri, imageView)) {
            TheWorkTask workTask = new TheWorkTask(finalUri, imageView, new Callable() {
                @Override
                public Object call() throws Exception {
                    msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);

                    boolean result = loadVoiceFromHttp(finalUri, msg.getAudioPath());
                    if(result) {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
                        // DBInterface.instance().insertOrUpdateMessage(msg);
                        MessageEntity.insertOrUpdateSingleData(msg);

                        if(Objects.equals(finalUri, getTheWorkTask(imageView).uri)){
                            new HandlerPost(0, true){
                                @Override
                                public void doAction() {
                                    refreshVoiceView(msg, textView, layout, unreadStateView);
                                }
                            };
                        }
                    }else {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
//                        DBInterface.instance().insertOrUpdateMessage(message);
                        MessageEntity.insertOrUpdateSingleData(msg);
                    }
                    return null;
                }
            });

            AsyncDrawable drawable = new AsyncDrawable(context.getResources(), placeholderBitmap, workTask);
            imageView.setImageDrawable(drawable);
            App.THREAD_POOL_EXECUTOR.submit(workTask);
        }

    }


    public void releaseVoiceResource() {

        if (animationDrawable != null) {
            animationDrawable.stop();
            animationDrawable = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void stopPlayVoiceMsg() {
        mIsVoicePlaying = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            Logger.getLogger().e("2. mediaPlayer.isPlaying(): %s" , mediaPlayer.isPlaying()?"true":"false");
            mediaPlayer.release();
            mediaPlayer = null;
            stopVoiceAnimation();
        }
    }

    private VoiceMsgViewHolder currentVoiceMsgViewHolder = null;
    private boolean mIsVoicePlaying = false;
    private AudioMessage currentVoiceMsg = null;

    private void playVoice(final AudioMessage msg, final VoiceMsgViewHolder holder) {
        System.out.println("playVoice()");
        String voicePath = msg.getAudioPath();
        if (mIsVoicePlaying && currentVoiceMsgViewHolder == holder) {
            stopPlayVoiceMsg();
            return;
        }else {
            stopPlayVoiceMsg();
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayVoiceMsg();
                }
            });
        }

        mIsVoicePlaying = true;
        currentVoiceMsgViewHolder = holder; //标记当前播放的imageView和item
        currentVoiceMsg = msg;

        startVoiceAnimation();

        try {
            mediaPlayer.setDataSource(voicePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Logger.getLogger().e("%s", e.getMessage());
            stopPlayVoiceMsg();
        }
    }

    //TODO 有复用bug
    private void startVoiceAnimation() {
        AudioMessage msg = (AudioMessage)getItem(currentVoiceMsgViewHolder.getPosition());
        if(!msg.getId().equals(currentVoiceMsg.getId())){
            boolean isMyself = msg.isSend(loginUser.getPeerId());
            if (isMyself) {
                currentVoiceMsgViewHolder.voice_msg_my_img.setImageResource(R.mipmap.volumn_send_default);
            } else {
                currentVoiceMsgViewHolder.voice_msg_buddy_img.setImageResource(R.mipmap.volumn_recv_default);
            }
            return;
        }

        boolean isMyself = currentVoiceMsg.isSend(loginUser.getPeerId());
        ImageView voiceMsgImgView = null;
        if (isMyself) {
            voiceMsgImgView = currentVoiceMsgViewHolder.voice_msg_my_img;
            voiceMsgImgView.setImageResource(R.drawable.msg_voice_anim_send_play);
        } else {
            voiceMsgImgView = currentVoiceMsgViewHolder.voice_msg_buddy_img;
            voiceMsgImgView.setImageResource(R.drawable.msg_voice_anim_recv_play);
        }
        animationDrawable = (AnimationDrawable) voiceMsgImgView.getDrawable();
        if (animationDrawable != null) {
            animationDrawable.start();
        }

    }

    private void stopVoiceAnimation() {

        if (animationDrawable == null || currentVoiceMsgViewHolder == null) {
            return;
        }else {
            animationDrawable.stop();
        }
        boolean isMyself = currentVoiceMsg.isSend(loginUser.getPeerId());
        if (isMyself) {
            currentVoiceMsgViewHolder.voice_msg_my_img.setImageResource(R.mipmap.volumn_send_default);
        } else {
            currentVoiceMsgViewHolder.voice_msg_buddy_img.setImageResource(R.mipmap.volumn_recv_default);
        }

    }

    private void clickVoiceView(final AudioMessage msg, final VoiceMsgViewHolder holder) {

        if (!msg.isSend(loginUser.getPeerId()) && msg.getReadStatus() == MessageConstant.AUDIO_UNREAD) {

            msg.setReadStatus( MessageConstant.AUDIO_READED);
//             DBInterface.instance().insertOrUpdateMessage(msg);
            MessageEntity.insertOrUpdateSingleData(msg);
            if (holder.voice_msg_read_state != null)
                holder.voice_msg_read_state.setVisibility(View.GONE);
        }

        String filePath = msg.getAudioPath();

        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath); //直接加载本地文件
            if (file.exists()) {
                //TODO Play
                playVoice(msg, holder);
            }
        }
    }

//    private void asyncDownLoadAudioFile(final AudioMessage msg, final VoiceMsgViewHolder holder) {
//
//        String filePath = msg.getAudioPath();
//        if (!TextUtils.isEmpty(filePath) && new File(filePath).length()>0) {
////            msg.setAudioPath(filePath);
////            msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
////            DBInterface.instance().insertOrUpdateMessage(msg);
//        } else {
//
//            String fileUrl = msg.getUrl();
//            int index = fileUrl.indexOf("?");
//            if(index > 0){
//                fileUrl = fileUrl.substring(0, index);
//            }
//            filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(fileUrl);
//            msg.setAudioPath(filePath);
//
//            final File file = new File(filePath);
//            if (!file.exists()) {
//                msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
//                String url = msg.getUrl();
//                new CustomFileDownload(url, file, new CustomFileDownload.DownloadListener() {
//                    @Override
//                    public void onDownloadEnd(JSONObject input) {
//                        boolean result = input.getBoolean("success");
//                        if (result) {
//                            msg.setAudioPath(input.getString("localPath"));
//                            msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
////                            DBInterface.instance().insertOrUpdateMessage(msg);
//                            MessageEntity.insertOrUpdateSingleData(msg);
//
//                            if (msg.isSend(loginUser.getPeerId())) {
//                                setVoiceView(msg, holder, holder.voice_msg_my_layout, holder.voice_msg_my_text, holder.voice_msg_my_img, null);
//                            } else {
//                                setVoiceView(msg, holder, holder.voice_msg_buddy_layout, holder.voice_msg_buddy_text, holder.voice_msg_buddy_img, holder.voice_msg_read_state);
//                            }
//                        } else {
//                            Toast.makeText(context, "下载失败: " +input.getString("error"), Toast.LENGTH_SHORT).show();
//                            if(file.exists()){
//                                file.delete();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onProgress(int value) {
//
//                    }
//
//                }).execute();
//            } else {
//                msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
////                DBInterface.instance().insertOrUpdateMessage(msg);
//                MessageEntity.insertOrUpdateSingleData(msg);
//            }
//        }
//    }

    private void setVoiceMsgData(final AudioMessage msg, final VoiceMsgViewHolder holder) {

        initBaseViewHolderData(msg, holder);

        if (msg.isSend(loginUser.getPeerId())) {
//            asyncDownLoadAudioFile(msg, holder);
            setVoiceView(msg, holder, holder.voice_msg_my_layout, holder.voice_msg_my_text, holder.voice_msg_my_img,null);
        } else {
//            asyncDownLoadAudioFile(msg, holder);
            setVoiceView(msg, holder, holder.voice_msg_buddy_layout, holder.voice_msg_buddy_text, holder.voice_msg_buddy_img, holder.voice_msg_read_state);
        }
    }


    private void setImageMsgData(final ImageMessage msg, final ImageMsgViewHolder holder) {

        initBaseViewHolderData(msg, holder);

        if (msg.isSend(loginUser.getPeerId())) {
            setImageView(msg, holder.img_chat_my_msg, holder.seleced_state_imageview);
        } else {
            setImageView(msg, holder.img_chat_buddy_msg, holder.seleced_state_imageview);
        }

    }

    private List<Integer> getReqSizeFromBitmap(int width, int height) {
        if(width == 0 || height == 0)
            return null;

        int screenWidth = CommonFunction.getWidthPx();
        float radio = (float)height/width;
        if(width < height){
            width = screenWidth/3;
        }else {
            if(width > screenWidth/2){
                width = screenWidth/2;
            }else if(width < screenWidth/3){
                width = screenWidth/3;
            }
        }

        List<Integer> size = new ArrayList<>();
        size.add(width);
        size.add((int)Math.floor(width*radio));

        return size;
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private void GlideView(final ImageMessage message, final Bitmap bitmap, final ImageView imageView, final String filePath, final ImageView seleced_state_imageview) {
//        if(bitmap == null || imageView == null)
//            return;
//
//        final ViewGroup.LayoutParams params = imageView.getLayoutParams();
//        List<Integer> size = getReqSizeFromBitmap(bitmap.getWidth(), bitmap.getHeight());
//        if(size == null)
//            return;
//
//        params.width = size.get(0);
//        params.height = size.get(1);
//
//        imageView.setLayoutParams(params);
//        imageView.setImageBitmap(bitmap);
//
////        new AsyncTask<Void, Void, byte[]>() {
////
////            @Override
////            protected byte[] doInBackground(Void... params) {
////                ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//这步比较耗时，故放在后台中
////                return baos.toByteArray();
////            }
////
////            @Override
////            protected void onPostExecute(byte[] byteArray) {
////                super.onPostExecute(byteArray);
////
////                Glide.with(mCurrentFragment)
////                        .load(byteArray)
////                        .dontAnimate()
////                        .placeholder(R.drawable.msg_pic_fail)
////                        .error(R.drawable.msg_pic_fail)
////                        .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
////                        .skipMemoryCache(true) //不缓存内存
////                        .into(imageView);
////
////            }
////        }.execute();
//
//
//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                if(mMode == R.id.MULTIPLE_CHOICE_MODE) {
//                    //方式1：消费事件
//                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                        Log.i("onTouch", "ACTION_DOWN from: chat_text_view");
//                        return true;
//                    }else if(event.getAction() == MotionEvent.ACTION_UP) {
//                        onClickItemCallback(message, seleced_state_imageview);
//                    }
//                    return false;
//
//                    //注：这里不能直接返回false：不消费事件，即OnTouchListener->onTouch返回false,执行onTouchEvent。
//                    //因设置了OnLongClickListener, 所以clickable， onTouchEvent返回true， 在MotionEvent.ACTION_UP中执行OnClickListener，
//                    // 没有OnClickListener的话导致没有执行 选中/未选中UI的切换 的逻辑。
////                    return false;
//                }else if(mMode == R.id.NORMAL_MODE){
//                    if (mCurrentFragment instanceof ChatFragment) {
//                        ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
//                        //TODO
//                        ArrayList<String> imagePaths = getAllImagePaths();
//                        int index = imagePaths.indexOf(filePath);
//                        chatFragment.preViewImage(imagePaths, index);
//                    }
//                }
//
//                return false;
//            }
//        });
//
//
////        imageView.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                if(mMode == R.id.NORMAL_MODE) {
////                    if (mCurrentFragment instanceof ChatFragment) {
////                        ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
////                        //TODO
////                        ArrayList<String> imagePaths = getAllImagePaths();
////                        int index = imagePaths.indexOf(filePath);
////                        chatFragment.preViewImage(imagePaths, index);
////                    }
////                }else {
////                    onClickItemCallback(message, seleced_state_imageview);
////                }
////            }
////        });
//
//        imageView.setOnLongClickListener(new OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View v) {
//
//                if(mMode == R.id.NORMAL_MODE) {
//                    vib.vibrate(15);//只震动15ms，一次 单位：ms
//                    if(mCurrentFragment instanceof ChatFragment) {
//                        ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
//                        chatFragment.showLongPressDialog(message);
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//
//    }

    /**
     * 获取所有图片的本地路径
     * @return
     */
    private ArrayList<String> getAllImagePaths(){
        if(mMsgList == null)
            return null;

        ArrayList<String> paths = new ArrayList<>();
        for(MessageEntity msg : mMsgList){
            if(msg instanceof ImageMessage){
                ImageMessage imageMessage = (ImageMessage)msg;
                paths.add(imageMessage.getPath());
            }
        }

        return paths;
    }

    private void GlideView(Context context, Bitmap bitmap, ImageView imageView){
        //根据图片尺寸初始化控件大小
        final ViewGroup.LayoutParams params = imageView.getLayoutParams();
        List<Integer> size = getReqSizeFromBitmap(bitmap.getWidth(), bitmap.getHeight());
        if(size == null)
            return;

        params.width = size.get(0);
        params.height = size.get(1);

        imageView.setLayoutParams(params);
        imageView.setImageBitmap(bitmap);


//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] bytes = baos.toByteArray();
//
//        // ImageView.ScaleType.CENTER_CROP:
//        //Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the image will be equal to
//        // or larger than the corresponding dimension of the view (minus padding).
////        new GlideBuilder(mContext).
//
//        Glide.with(context)
//                .load(bytes)
//                .placeholder(com.example.milanac007.pickerandpreviewphoto.R.mipmap.msg_pic_fail)
//                .error(com.example.milanac007.pickerandpreviewphoto.R.mipmap.msg_pic_fail)
//                .crossFade()
//                .centerCrop()
//                .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
//                .skipMemoryCache(true) //不缓存内存
//                .into(imageView);
    }

    private static class AsyncDrawable extends BitmapDrawable {
        WeakReference<TheWorkTask> ref;
        public AsyncDrawable(Resources res, Bitmap bitmap, TheWorkTask task) {
//            super();
            super(res, bitmap);
            ref = new WeakReference<>(task);
        }

        public TheWorkTask getTheWorkTask() {
            return ref.get();
        }
    }

    private static class TheWorkTask<T> extends FutureTask {
        private final String uri;
        private final WeakReference<ImageView> ref;
        public TheWorkTask(String uri, ImageView imageView, Callable<T> callable) {
            super(callable);
            this.uri = uri;
            ref = new WeakReference<>(imageView);
        }

    }

    private TheWorkTask getTheWorkTask(ImageView imageView) {
        if(imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getTheWorkTask();
            }
        }
        return null;
    }

    private boolean canCancelTask(String uri, ImageView imageView) {
        final TheWorkTask theWorkTask = getTheWorkTask(imageView);

        if(theWorkTask != null){
            final String url = theWorkTask.uri;
            // If bitmapPath is not yet set or it differs from the new data
            if(TextUtils.isEmpty(url) || !url.equals(uri)){
                // Attempts to cancel previous task(通过调用thread的interrupt()方法，置中断位。如果当前thread处于Waitting(调用wait、sleep方法)会清除中断状态位，并抛出InterruptedException异常;
                // 如果当前thread处于Blocking(例如调用synchronized、lock.lock()), 那么依然处于Blocking；
                // 如果正在运行Running， 则依然在运行。只能主动调用Thread.currentThread().isInterrupted()来检测中断位)
                theWorkTask.cancel(true);
            }else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }


    private Bitmap loadBitmap(String uri, boolean isSend, boolean sendOriginalPic) {

        Bitmap bmp;
        if(isSend) {
            if(!sendOriginalPic) {
                bmp = CacheManager.getInstance().getBitmapFromMemCache(uri);
                if (bmp != null) {
                    return bmp;
                }

                bmp = CacheManager.getInstance().getBitmapFromDiskCache(uri);
                if (bmp != null) {
                    return bmp;
                }

                final String filePath = uri;
                if (new File(filePath).exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    List<Integer> size = getReqSizeFromBitmap(options.outWidth, options.outHeight);
                    if (size != null) {
                        int reqWidth = size.get(0);
                        int reqHeight = size.get(1);
                        bmp = CacheManager.getInstance().addCacheData(filePath, reqWidth, reqHeight, false);
                        if (bmp != null) {
                            return bmp;
                        }
                    }
                }
            }else { //TODO 发送原图为发送行为，和UI显示关系不大， 考虑移除该逻辑
                final String filePath = uri;
                if (new File(filePath).exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    bmp = CacheManager.getInstance().addCacheData(filePath, options.outWidth, options.outHeight, false);
                    if (bmp != null) {
                        return bmp;
                    }
                }
            }

        }else { //recv
            //从url中解析原始图片的宽高, 最终取移除宽高的url做key
            int index = uri.indexOf("?");
            String subStr = uri.substring(index+1);
            String[] size = subStr.split("[\\*, x]{1}");
            final int width = Integer.parseInt(size[0]);
            final int height = Integer.parseInt(size[1]);
            uri = uri.substring(0, index);

            bmp = CacheManager.getInstance().getBitmapFromMemCache(uri);
            if (bmp != null) {
                return bmp;
            }

            bmp = CacheManager.getInstance().getBitmapFromDiskCache(uri);
            if (bmp != null) {
                return bmp;
            }

            //检查temp目录
            String filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(uri);
            if (new File(filePath).exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, options);
                List<Integer> bmpSize = getReqSizeFromBitmap(options.outWidth, options.outHeight);
                if (size != null) {
                    int reqWidth = bmpSize.get(0);
                    int reqHeight = bmpSize.get(1);
                    bmp = CacheManager.getInstance().addCacheData(uri, filePath, reqWidth, reqHeight, false);
                    if (bmp != null) {
                        return bmp;
                    }
                }
            }

            //远程下载
            List<Integer> bmpSize = getReqSizeFromBitmap(width, height);
            int reqWidth = bmpSize.get(0);
            int reqHeight = bmpSize.get(1);
            bmp = loadBitmapFromHttp(uri, filePath, reqWidth, reqHeight);
            if (bmp != null) {
                return bmp;
            }
        }
        return null;
    }

    public Bitmap loadBitmapFromHttp(String url, String filePath, int reqWidth, int reqHeight) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI thread.");
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            if(downloadUrlToStream(url, outputStream)){
                CacheManager.getInstance().addCacheData(url, filePath, reqWidth, reqHeight, false);
            }else {
                new File(filePath).delete();//下载失败时可能为空文件、也可能不完整，删除文件
            }
            return CacheManager.getInstance().getBitmapFormCache(url);
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private HttpURLConnection getHttpURLConnection(String requestUrl) throws IOException, GeneralSecurityException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(requestUrl);
        if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
//            setSSLSocketFactory((HttpsURLConnection)urlConnection);
            setSSLSocketFactory2((HttpsURLConnection)urlConnection);
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        return urlConnection;
    }

    //客户端不校验服务器证书
    private void setSSLSocketFactory2(HttpsURLConnection httpsURLConn) throws IOException, GeneralSecurityException {
        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{tm}, new SecureRandom());
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        httpsURLConn.setSSLSocketFactory(ssf);
    }


    //客户端校验服务器证书
    private void setSSLSocketFactory(HttpsURLConnection httpsURLConn) throws IOException, GeneralSecurityException {
        SSLSocketFactory ssf = null;
        List<byte[]> inBytesList = new ArrayList<>();
//        String[] cerFiles = {"client.pem", "client_sm.pem"};
        String[] cerFiles = {"client.pem"};
//        String[] cerFiles = {"client_sm.pem"};

        int len = 0;
        for(String cerFile : cerFiles) {
            InputStream in = App.getContext().getAssets().open(cerFile);
            byte[] inBytes = toByteArray(in);
            inBytesList.add(inBytes);
            len += inBytes.length;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
        for(byte[] inbytes : inBytesList) {
            bos.write(inbytes);
        }

        ByteArrayInputStream ins = new ByteArrayInputStream(bos.toByteArray());
        X509TrustManager x509TrustManager = trustManagerForCertificates(ins);
        ssf = getSSLSocketFactory(x509TrustManager);
        httpsURLConn.setSSLSocketFactory(ssf);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))){
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    private static javax.net.ssl.SSLSocketFactory getSSLSocketFactory(X509TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        return context.getSocketFactory();
    }

    private static X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        //通过证书工厂得到自签名证书对象组合
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if(certificates.isEmpty()){
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        //为证书设置一个keyStore
        char[] password = "password".toCharArray(); //Any password will work
        KeyStore keyStore = newEmptyKeyStore(password);

        int index = 0;
        //将证书放入keyStore中
        for(Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        //use it to build an X509 trust manager
        //使用包含自签名证书信息的Keystore构建一个X509TrustManager
//        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        keyManagerFactory.init(keyStore, password);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if(trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" +
                    Arrays.toString(trustManagers));
        }

        return (X509TrustManager)trustManagers[0];

    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; //By convention, 'null' creates an empty keystore
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {

            // 解析url，并创建一个HttpURLConnection 或 HttpsURLConnection
            urlConnection = getHttpURLConnection(urlString);

            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return true;
        }catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "downloadBitmap failed." + e);
        }finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            try {
                out.close();
            } catch (IOException e) { }

            try {
                in.close();
            } catch (IOException e) { }
        }
        return false;
    }


    /**
     * 当满足filePath不为空、缓存有效或filePath对应的文件存在时，取缓存使用
     * 否则，试图从fileUrl下手，尝试本地查找和远程下载
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setImageView(ImageMessage msg, ImageView imageView, final ImageView seleced_state_imageview) {

        imageView.setVisibility(View.VISIBLE);

        /**
         * 这里不能设置imageView.setOnTouchListener来实现imageView的逻辑方法，因为imageView设置了OnLongClickListener, 所以clickable，
         * onTouchEvent返回true(自身消耗事件)， 并在MotionEvent.ACTION_UP中执行OnClickListener，
         * 因为OnTouchListener->onTouch返回false,才执行onTouchEvent。
         * NORMAL_MODE下，imageView 有长按和短按逻辑。如果短按逻辑放在setOnTouchListener的onTouch中执行，需MotionEvent.ACTION_DOWN返回true,
         * 则无法再走onTouchEvent中的长按逻辑。
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMode == R.id.NORMAL_MODE) {
                    if (mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
                        //TODO
                        ArrayList<String> imagePaths = getAllImagePaths();
                        int index = imagePaths.indexOf(msg.getPath());
                        chatFragment.preViewImage(imagePaths, index);
                    }
                }else {
                    onClickItemCallback(msg, seleced_state_imageview);
                }
            }
        });

        imageView.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if(mMode == R.id.NORMAL_MODE) {
                    vib.vibrate(15);//只震动15ms，一次 单位：ms
                    if(mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                        chatFragment.showLongPressDialog(msg);
                    }
                    return true;
                }
                return false;
            }
        });


        String finalUri, uri;
        boolean isSend = false;
        if(msg.isSend(loginUser.getPeerId())) {
            finalUri = msg.getPath();
            uri = finalUri;
            isSend = true;
        }else {
            finalUri = msg.getUrl();
            uri = finalUri;
            int index = uri.indexOf("?");
            if(index > 0) {
                uri = uri.substring(0, index);
            }
        }

        Bitmap bitmap = CacheManager.getInstance().getBitmapFromMemCache(uri);
        if(bitmap != null) {
            GlideView(context, bitmap, imageView);
            return;
        }

        final boolean isSendFlag = isSend;
        if(canCancelTask(finalUri, imageView)) {
            TheWorkTask workTask = new TheWorkTask(finalUri, imageView, new Callable() {
                @Override
                public Object call() throws Exception {
                    msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
                    Bitmap bmp = loadBitmap(finalUri, isSendFlag, false);
                    if(bmp != null) {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
            //            DBInterface.instance().insertOrUpdateMessage(msg);
                        MessageEntity.insertOrUpdateSingleData(msg);

                        if(finalUri == getTheWorkTask(imageView).uri){
                            new HandlerPost(0, true){
                                @Override
                                public void doAction() {
                                    GlideView(context, bmp, imageView);
                                }
                            };
                        }
                    }else {
                        msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
//                        DBInterface.instance().insertOrUpdateMessage(message);
                        MessageEntity.insertOrUpdateSingleData(msg);
                    }
                    return null;
                }
            });

            AsyncDrawable drawable = new AsyncDrawable(context.getResources(), mPlaceholderBitmap, workTask);
            imageView.setImageDrawable(drawable);
            App.THREAD_POOL_EXECUTOR.submit(workTask);
        }


//        String filePath = msg.getPath();
//        if(!TextUtils.isEmpty(filePath)) {
//            Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(filePath);
//            if(bitmap != null) {
//                GlideView(msg, bitmap, imageView, filePath, seleced_state_imageview);
//                return;
//            }
//
//            if(new File(filePath).exists()) {
//                //todo 异步处理
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(filePath, options);
//                List<Integer> size = getReqSizeFromBitmap(options.outWidth, options.outHeight);
//                if(size != null) {
//                    int reqWidth = size.get(0);
//                    int reqHeight = size.get(1);
//                    bitmap = CacheManager.getInstance().addCacheData(filePath, reqWidth, reqHeight);
//                    GlideView(msg, bitmap, imageView, filePath, seleced_state_imageview);
//                    return;
//                }
//            }
//        }
//
//        String fileUrl = msg.getUrl();
//        if(TextUtils.isEmpty(fileUrl))
//            return;
//
//        int index = fileUrl.indexOf("?");
//        if(index > 0){
//            fileUrl = fileUrl.substring(0, index);
//        }
//        filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(fileUrl);
//        msg.setPath(filePath);
//
//        if (!new File(filePath).exists()) {
//            msg.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
//            asyncLoadImage(msg, imageView, seleced_state_imageview);
//        } else {
//            msg.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
////            DBInterface.instance().insertOrUpdateMessage(msg);
//            MessageEntity.insertOrUpdateSingleData(msg);
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(filePath, options);
//            List<Integer> size = getReqSizeFromBitmap(options.outWidth, options.outHeight);
//            if(size != null) {
//                int reqWidth = size.get(0);
//                int reqHeight = size.get(1);
//                Bitmap bitmap = CacheManager.getInstance().addCacheData(filePath, reqWidth, reqHeight);
//                GlideView(msg, bitmap, imageView, filePath, seleced_state_imageview);
//            }
//
//        }
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
        drawable.draw(canvas);
        return bitmap;
    }

//    protected void asyncLoadImage(final ImageMessage message, final ImageView iv, final ImageView seleced_state_imageview) {
//        String imageUrl = message.getUrl();
//        LazyHeaders.Builder builder = new LazyHeaders.Builder();
//        builder.addHeader("clientId", Utils.getDeviceId());
//        builder.addHeader("refreshTokenGrantType", "refresh_token");
//        builder.addHeader("refreshToken", Preferences.getRefreshToken());
////        builder.addHeader("tokenValue", Preferences.getAccessToken());
//        LazyHeaders headers = builder.build();
//        GlideUrl glideUrl = new GlideUrl(imageUrl, headers);
//
//        int index = imageUrl.indexOf("?");
//        String subStr = imageUrl.substring(index+1);
//        String[] size = subStr.split("[\\*, x]{1}");
//        final int reqWidth = (int)Double.valueOf(size[0]).doubleValue();
//        final int reqHeight = (int)Double.valueOf(size[1]).doubleValue();
//
//
//        //根据需下载的图片初始化控件大小
//        ViewGroup.LayoutParams params = iv.getLayoutParams();
//        List<Integer> reqSize = getReqSizeFromBitmap(reqWidth, reqHeight);
//        params.width = reqSize.get(0);
//        params.height = reqSize.get(1);
//
//        iv.setLayoutParams(params);
//
//        Glide.with(mCurrentFragment.getActivity())
//                .load(glideUrl)
//                .override(reqWidth, reqHeight)
//                .listener(new RequestListener<GlideUrl, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, GlideUrl model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        if(e != null) {
//                            e.printStackTrace();
//                            CommonFunction.showToast(TextUtils.isEmpty(e.getMessage()) ? "异常" : e.getMessage());
//                        }
////                        "下载失败: " +input.getString("error")
//                        File file = new File(message.getPath());
//                        if(file.exists()){
//                            file.delete();
//                        }
//
//                        message.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
////                        DBInterface.instance().insertOrUpdateMessage(message);
//                        MessageEntity.insertOrUpdateSingleData(message);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, GlideUrl model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        final Bitmap bitmap = drawableToBitamp(resource);
//                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        GlideView(message, bitmap, iv, message.getPath(), seleced_state_imageview);
//
//                        new AsyncTask<Void, Void, Void>() {
//
//                            @Override
//                            protected Void doInBackground(Void... params) {
//                                try {
//                                    message.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
////                                    DBInterface.instance().insertOrUpdateMessage(message);
//                                    MessageEntity.insertOrUpdateSingleData(message);
//
//                                    File file = new File(message.getPath());
//                                    FileOutputStream os = new FileOutputStream(file);
//                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//                                    os.flush();
//                                    os.close();
//
//                                    CacheManager.getInstance().addCacheData(file.getPath(), reqWidth, reqHeight);
//
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                return null;
//                            }
//
//                            @Override
//                            protected void onPostExecute(Void aVoid) {
//                                super.onPostExecute(aVoid);
//                                GlideView(message, bitmap, iv, message.getPath(), seleced_state_imageview);
//                            }
//                        }.execute();
//
//                        return false;
//                    }
//                })
//                .error(R.mipmap.msg_pic_fail)
//                .placeholder(R.mipmap.msg_pic_fail)
//                .diskCacheStrategy(DiskCacheStrategy.NONE) //不缓存到SD卡
//                .skipMemoryCache(true) //不缓存内存
//                .into(iv);
//
//    }

    private void setMsgDateTime(final MessageEntity message, final TextView dateTimeView) {
        if(showTimeMsgIdList.contains(message)){
            dateTimeView.setVisibility(View.VISIBLE);
            String dateStr = CommonFunction.getDisplayTimeFormat(new Date(message.getCreated()));
            dateTimeView.setText(dateStr);
        }else {
            dateTimeView.setVisibility(View.GONE);
        }
    }

    private void setMsgState(ImageView msg_state, int msgState) {
        switch (msgState) {
            case MessageConstant.MSG_SUCCESS:
                msg_state.clearAnimation();
                msg_state.setVisibility(View.INVISIBLE);
                break;
            case MessageConstant.MSG_FAILURE:
            case MessageConstant.MSG_FAIL_RESULT_CODE:
                msg_state.setVisibility(View.VISIBLE);
                msg_state.clearAnimation();
                msg_state.setImageResource(R.mipmap.msg_failed_img);
                break;
            case MessageConstant.MSG_SENDING:
                msg_state.setVisibility(View.VISIBLE);
                msg_state.setImageResource(R.mipmap.msg_loading);
                msg_state.startAnimation(loadingAnim);
                break;
        }

    }

    private void showReSendDialog(final MessageEntity msg, final MsgBaseViewHolder holder){
        final CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(context);
        builder.setPositiveBtn(R.string.g_resend, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                if(mCurrentFragment instanceof ChatFragment) {
                    ChatFragment chatFragment = (ChatFragment)mCurrentFragment;

                    if(!Utils.isNetworkAvailable()){
                        CommonFunction.showToast("请检查当前网络");
                    }else {
                        msg.setStatus(MessageConstant.MSG_SENDING);
                        setMsgState(holder.msg_state, msg.getStatus());
                        chatFragment.sendMsgToServer(msg, false);
                    }
                }
            }
        });
        builder.setNegativeBtn(R.string.g_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setTitle("重发该消息？");
        CustomConfirmDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 用于替换emoji表情、电话icon
     */
    private void replaceContent(final TextMessage msg, final TextView contentView) {

        final String content = msg.getContent();

        contentView.setTag(content); //原始内容设为contentView的tag，方便后期替换后检查tag是否已经发生了变化，变化则忽略掉

        //替换emoji表情图片
        Pattern pattern = Pattern.compile("\\[[\\u4e00-\\u9fa5, OK, NO]{1,3}\\]");
        Matcher mather =  pattern.matcher(content);

        //替换电话图标
        Pattern pattern2 = Pattern.compile("<img src=\"extra/");
        Matcher matcher2 = pattern2.matcher(content);

        int sessionType = msg.getSessionType();

        Callable<CharSequence> callable = new Callable<CharSequence>() {
            @Override
            public CharSequence call() throws Exception {

                CharSequence resultContent; //替换之后的文本

                if (mather.find()){ //替换emoji表情图片
                    String replaceContent = content;
                    replaceContent = replaceContent.replaceAll(" ", "&nbsp;");
                    replaceContent = replaceContent.replaceAll("\r\n", "<br>");
                    replaceContent = replaceContent.replaceAll("\r","<br>");
                    replaceContent = replaceContent.replaceAll("\n","<br>");

                    final String replaceStr = FaceManager.getInstance().replaceFaceChacter(replaceContent);
                    resultContent = Html.fromHtml(replaceStr, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            AssetManager assetManager = context.getAssets();
                            BitmapDrawable drawable = null;
                            try {
                                InputStream in = assetManager.open(source);
                                Bitmap bmp = BitmapFactory.decodeStream(in);
                                drawable = new BitmapDrawable(bmp);
//                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());//图像很小 19px,原因待查
                                drawable.setBounds(0, 0, CommonFunction.dip2px(19), CommonFunction.dip2px(19));
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                            return drawable;
                        }
                    }, null);

                }else if(matcher2.find() && sessionType == DBConstant.SESSION_TYPE_SINGLE){ //替换电话图标
                    resultContent = Html.fromHtml(content, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            AssetManager assetManager = context.getAssets();
                            BitmapDrawable drawable = null;
                            try {
                                InputStream in = assetManager.open(source);
                                Bitmap bmp = BitmapFactory.decodeStream(in);
                                drawable = new BitmapDrawable(bmp);
                                drawable.setBounds(0, 0, CommonFunction.dip2px(26), CommonFunction.dip2px(13));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            return drawable;
                        }
                    }, null);

                }else {
                    resultContent = content;
                }

                return resultContent;
            }
        };

        Future<CharSequence> task = App.THREAD_POOL_EXECUTOR.submit(callable);
        try {
            CharSequence resultContent = task.get();

            String tag = (String)contentView.getTag();
            if(!tag.equals(content)) {
                Log.w(TAG, "set text-content, but content has changed, ignored.");
            }else {
                new HandlerPost(0, true){
                    @Override
                    public void doAction() {
                        contentView.setText(resultContent);
                        if(matcher2.find()) {
                            contentView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mMode == R.id.NORMAL_MODE) {
                                        if (mCurrentFragment instanceof ChatFragment) {
                                            ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
                                            chatFragment.showCallDialog();
                                        }
                                    } else {
                                        //TODO
                //                        if(message != null && seleced_state_imageview != null){
                //                            onClickItemCallback(message, seleced_state_imageview);
                //                        }
                                    }
                                }
                            });
                        }
                    }
                };
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

//    private void replaceCallMsg2(final TextMessage message, final TextView textView, final ImageView seleced_state_imageview) {
//        Pattern pattern = Pattern.compile("<img src=\"extra/");
//        Matcher matcher = pattern.matcher(message.getContent());
//        if(matcher.find()){
//            new AsyncTask<Void, Void, Spanned>(){
//                @Override
//                protected Spanned doInBackground(Void... params) {
//
//                    String replaceContent = message.getContent();
//                    //替换电话图标
//                    Spanned htmlStr = Html.fromHtml(replaceContent, new Html.ImageGetter() {
//                        @Override
//                        public Drawable getDrawable(String source) {
//                            AssetManager assetManager = context.getAssets();
//                            BitmapDrawable drawable = null;
//                            try {
//                                InputStream in = assetManager.open(source);
//                                Bitmap bmp = BitmapFactory.decodeStream(in);
//                                drawable = new BitmapDrawable(bmp);
//                                drawable.setBounds(0, 0, CommonFunction.dip2px(26), CommonFunction.dip2px(13));
//                            }catch (IOException e){
//                                e.printStackTrace();
//                            }
//
//                            return drawable;
//                        }
//                    }, null);
//                    return htmlStr;
//                }
//
//                @Override
//                protected void onPostExecute(Spanned s) {
//                    super.onPostExecute(s);
//                    textView.setText(s);
//                    textView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if(mMode == R.id.NORMAL_MODE) {
//                                if(mCurrentFragment instanceof ChatFragment) {
//                                    ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
//                                    chatFragment.showCallDialog();
//                                }
//                            }else {
//                                if(message != null && seleced_state_imageview != null){
//                                    onClickItemCallback(message, seleced_state_imageview);
//                                }
//                            }
//                        }
//                    });
//                }
//            }.execute();
//
//        }else {
//            textView.setText(message.getContent());
//        }
//    }
//
//    private void replaceCallMsg(final TextMessage message, final TextView textView, final ImageView seleced_state_imageview) {
//
//        final String text = message.getContent();
//        final String callStr = "通话时长";
//        if(!text.startsWith(callStr))
//            return;
//
//        if(mMode == R.id.NORMAL_MODE) {
//            textView.setMovementMethod(LinkMovementClickMethod.getInstance()); //必须：设置超链接为可点击状态
//            // 设置TextView的内容
//            textView.setText(Html.fromHtml(text,null, new Html.TagHandler() {
//                @Override
//                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
//                    String str = output.toString();
//                    Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
//                    Matcher matcher = pattern.matcher(str);
//
//                    while (matcher.find()) {
//                        int start = matcher.start();
//                        int end = matcher.end();
////                        output.setSpan(new CallClickSpan(message, seleced_state_imageview), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        output.setSpan(new CallClickSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    }
//                }
//            }));
//        }else {
//            // 设置TextView的内容
//            textView.setText(Html.fromHtml(text,null, null));
//        }
//    }

    class CallClickSpan extends ClickableSpan {
        private MessageEntity messageEntity;
        private ImageView seleced_state_imageview;
        CallClickSpan(MessageEntity messageEntity, ImageView seleced_state_imageview){
            super();
            this.messageEntity = messageEntity;
            this.seleced_state_imageview = seleced_state_imageview;
        }

        CallClickSpan(){
            super();
        }

        @Override
        public void onClick(View widget) {
            if(mMode == R.id.NORMAL_MODE) {
                if(mCurrentFragment instanceof ChatFragment) {
                    ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                    chatFragment.showCallDialog();
                }
            }else {
                if(messageEntity != null && seleced_state_imageview != null){
                    //bug: 会连续触发两次,所以这里不依赖CallClickSpan的onClick 改变多选状态
                    onClickItemCallback(messageEntity, seleced_state_imageview);
                }
            }
        }
    }

    private void setTextMsgData(final TextMessage msg, final TextMsgViewHolder holder) {

        initBaseViewHolderData(msg, holder);

        if (msg.isSend(loginUser.getPeerId())) {
            setTextView(msg, holder.text_chat_my_msg, holder.seleced_state_imageview);
        } else {
            setTextView(msg, holder.text_chat_buddy_msg, holder.seleced_state_imageview);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTextView(final TextMessage msg, final TextView chat_text_view, final ImageView seleced_state_imageview){
        final String content = msg.getContent();
        if(content.length() < 10){
            chat_text_view.setGravity(Gravity.CENTER);
        } else{
            chat_text_view.setGravity(Gravity.NO_GRAVITY);
        }

        replaceContent(msg, chat_text_view);

        chat_text_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(mMode == R.id.MULTIPLE_CHOICE_MODE) {
                    //方式1：消费事件
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.i("onTouch", "ACTION_DOWN from: chat_text_view");
                        return true;
                    }else if(event.getAction() == MotionEvent.ACTION_UP) {
                        onClickItemCallback(msg, seleced_state_imageview);
                    }
                    return false;

                    //注：这里不能直接返回false：不消费事件，即OnTouchListener->onTouch返回false,执行onTouchEvent。
                    //因设置了OnLongClickListener, 所以clickable， onTouchEvent返回true， 在MotionEvent.ACTION_UP中执行OnClickListener，
                    // 没有OnClickListener的话导致没有执行 选中/未选中UI的切换 的逻辑。
//                    return false;
                }

                return false;
            }
        });

        chat_text_view.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if(mMode == R.id.NORMAL_MODE) {
                    vib.vibrate(15);//只震动15ms，一次 单位：ms
                    if (mCurrentFragment instanceof ChatFragment) {
                        ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
                        chatFragment.showLongPressDialog(msg);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private View getTextMsgView(TextMessage msg, View convertView) {

        TextMsgViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.im_msg_text_layout, null);
            holder = new TextMsgViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof TextMsgViewHolder){
                holder = (TextMsgViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.im_msg_text_layout, null);
                holder = new TextMsgViewHolder(convertView);
            }
        }

        if (msg.isSend(loginUser.getPeerId())) {
            holder.im_plain_receiver_view_stub.setVisibility(View.GONE);
            holder.im_plain_sender_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_me = convertView.findViewById(R.id.icon_chat_me);
            holder.text_chat_my_msg = convertView.findViewById(R.id.text_chat_my_msg);
            holder.msg_state = convertView.findViewById(R.id.msg_state);
        } else {
            holder.im_plain_sender_view_stub.setVisibility(View.GONE);
            holder.im_plain_receiver_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_buddy = convertView.findViewById(R.id.icon_chat_buddy);
            holder.text_chat_buddy_msg = convertView.findViewById(R.id.text_chat_buddy_msg);
            holder.receiver_nickname = convertView.findViewById(R.id.receiver_nickname);
        }

        setTextMsgData(msg, holder);
        return convertView;
    }

    private View getImageMsgView(ImageMessage msg, View convertView) {
        ImageMsgViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.im_msg_image_layout, null);
            holder = new ImageMsgViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof ImageMsgViewHolder){
                holder = (ImageMsgViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.im_msg_image_layout, null);
                holder = new ImageMsgViewHolder(convertView);
            }
        }

        if (msg.isSend(loginUser.getPeerId())) {
            holder.im_image_receiver_view_stub.setVisibility(View.GONE);
            holder.im_image_sender_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_me = convertView.findViewById(R.id.icon_chat_me);
            holder.img_chat_my_msg = convertView.findViewById(R.id.img_chat_my_msg);
            holder.msg_state = convertView.findViewById(R.id.msg_state);
        } else {
            holder.im_image_sender_view_stub.setVisibility(View.GONE);
            holder.im_image_receiver_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_buddy = convertView.findViewById(R.id.icon_chat_buddy);
            holder.img_chat_buddy_msg = convertView.findViewById(R.id.img_chat_buddy_msg);
            holder.receiver_nickname = convertView.findViewById(R.id.receiver_nickname);
        }

        setImageMsgData(msg, holder);
        return convertView;
    }

    private View getVoiceMsgView(int position, AudioMessage msg, View convertView){
        VoiceMsgViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.im_msg_voice_layout, null);
            holder = new VoiceMsgViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof VoiceMsgViewHolder){
                holder = (VoiceMsgViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.im_msg_voice_layout, null);
                holder = new VoiceMsgViewHolder(convertView);
            }
        }

        holder.setPosition(position);

        if (msg.isSend(loginUser.getPeerId())) {
            holder.im_voice_receiver_view_stub.setVisibility(View.GONE);
            holder.im_voice_sender_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_me = convertView.findViewById(R.id.icon_chat_me);
            holder.msg_state = convertView.findViewById(R.id.msg_state);

            holder.voice_msg_my_layout = convertView.findViewById(R.id.voice_msg_my_layout);
            holder.voice_msg_my_text = convertView.findViewById(R.id.voice_msg_my_text);
            holder.voice_msg_my_img = convertView.findViewById(R.id.voice_msg_my_img);
        } else {
            holder.im_voice_sender_view_stub.setVisibility(View.GONE);
            holder.im_voice_receiver_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_buddy = convertView.findViewById(R.id.icon_chat_buddy);
            holder.receiver_nickname = convertView.findViewById(R.id.receiver_nickname);

            holder.voice_msg_buddy_layout = convertView.findViewById(R.id.voice_msg_buddy_layout);
            holder.voice_msg_buddy_text = convertView.findViewById(R.id.voice_msg_buddy_text);
            holder.voice_msg_buddy_img = convertView.findViewById(R.id.voice_msg_buddy_img);
            holder.voice_msg_read_state = convertView.findViewById(R.id.voice_msg_read_state);
        }

        setVoiceMsgData(msg, holder);
        return convertView;
    }

    private View getVideoMsgView(VideoMessage msg, View convertView) {
        VideoMsgViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.im_msg_video_layout, null);
            holder = new VideoMsgViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof VideoMsgViewHolder){
                holder = (VideoMsgViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.im_msg_video_layout, null);
                holder = new VideoMsgViewHolder(convertView);
            }
        }

        if (msg.isSend(loginUser.getPeerId())) {
            holder.im_video_receiver_view_stub.setVisibility(View.GONE);
            holder.im_video_sender_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_me = convertView.findViewById(R.id.icon_chat_me);
            holder.msg_state = convertView.findViewById(R.id.msg_state);

            holder.video_capture_my_layout = (FrameLayout) convertView.findViewById(R.id.video_capture_my_layout);
            holder.videoView_my_preview = (ImageView)convertView.findViewById(R.id.videoView_my_preview);
            holder.button_play_my = (Button) convertView.findViewById(R.id.button_play_my);
        } else {
            holder.im_video_sender_view_stub.setVisibility(View.GONE);
            holder.im_video_receiver_view_stub.setVisibility(View.VISIBLE);

            holder.icon_chat_buddy = convertView.findViewById(R.id.icon_chat_buddy);
            holder.receiver_nickname = convertView.findViewById(R.id.receiver_nickname);

            holder.video_capture_buddy_layout = (FrameLayout) convertView.findViewById(R.id.video_capture_buddy_layout);
            holder.videoView_buddy_preview = (ImageView)convertView.findViewById(R.id.videoView_buddy_preview);
            holder.button_play_buddy = (Button) convertView.findViewById(R.id.button_play_buddy);
            holder.roundProgressBar = (CustomRoundProgressBar)convertView.findViewById(R.id.roundProgressBar);
        }

        setVideoMsgData(msg, holder);
        return convertView;
    }

    private View getSystemMsgView(TextMessage msg, View convertView) {
        SystemMsgViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.im_system_layout, null);
            holder = new SystemMsgViewHolder(convertView);
        } else {
            Object object = convertView.getTag();
            if(object instanceof SystemMsgViewHolder){
                holder = (SystemMsgViewHolder)object;
            }else {
                convertView = inflater.inflate(R.layout.im_system_layout, null);
                holder = new SystemMsgViewHolder(convertView);
            }
        }

        setSystemMsgData(msg, holder);
        return convertView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initBaseViewHolderData(final MessageEntity msg, final MsgBaseViewHolder holder){

        if(mMode == R.id.NORMAL_MODE){
            holder.seleced_state_imageview.setVisibility(View.GONE);
        }else if(mMode == R.id.MULTIPLE_CHOICE_MODE){
            holder.seleced_state_imageview.setVisibility(View.VISIBLE);
            Drawable drawable =  context.getResources().getDrawable(R.mipmap.checkbox_off);
            if(allSelectedObject().contains(msg)){
                drawable = context.getResources().getDrawable(R.mipmap.checkbox_on);
            }
            holder.seleced_state_imageview.setImageDrawable(drawable);
        }

        setMsgDateTime(msg, holder.send_msg_date);

        int sessionType = msg.getSessionType();
        if (!msg.isSend(loginUser.getPeerId())) { //收到的消息才可能显示昵称
            if(sessionType == DBConstant.SESSION_TYPE_SINGLE){
                holder.receiver_nickname.setVisibility(View.GONE);
            }else {
                boolean isShow = imService.getConfigSp().isShowNick(sessionId);
                if(isShow){
                    GroupMemberEntity member = imService.getGroupManager().findGroupMember(sessionId, msg.getFromId());
                    UserEntity userEntity = imService.getContactManager().findContact(msg.getFromId());

                    boolean hasNickName = userEntity != null && !TextUtils.isEmpty(userEntity.getNickName());
                    boolean hasGroupNickName = member != null && !TextUtils.isEmpty(member.getNickName());

                    String nickName = hasNickName ? userEntity.getNickName(): hasGroupNickName ? member.getNickName() : userEntity.getMainName();

                    holder.receiver_nickname.setText(nickName);
                    holder.receiver_nickname.setVisibility(View.VISIBLE);
                }else {
                    holder.receiver_nickname.setVisibility(View.GONE);
                }
            }
        }

        final UserEntity userEntity = imService.getContactManager().findContact(msg.getFromId());
        if (msg.isSend(loginUser.getPeerId())) {

            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.icon_chat_me, userEntity);
                }
            });

            setMsgState(holder.msg_state, msg.getStatus());


            holder.icon_chat_me.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mMode == R.id.NORMAL_MODE) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN) {
                            return true; // MotionEvent.ACTION_DOWN 必须 return true来消费事件，否则后续的 MotionEvent.ACTION_UP事件不会发送过来
                        }else if(event.getAction() == MotionEvent.ACTION_UP) {
                            if(mCurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment)mCurrentFragment;
                                chatFragment.JumpToPersonalInfo(userEntity.getPeerId());
                            }
                        }
                        return false; //对于MotionEvent.ACTION_UP和MotionEvent.ACTION_MOVE而言，return false 和 return true没什么区别
                    }

                    return false; //多选模式下不消费事件，交由父布局统一处理
                }
            });

            holder.msg_state.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mMode == R.id.NORMAL_MODE) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN) {
                            return true; //消费事件
                        }else if(event.getAction() == MotionEvent.ACTION_UP) {
                            if (msg.getStatus() == MessageConstant.MSG_FAILURE || msg.getStatus() == MessageConstant.MSG_FAIL_RESULT_CODE) {//失败的时候才能发送
                                showReSendDialog(msg, holder);
                            }
                        }
                        return false;
                    }
                    return false; //多选模式下不消费事件，交由父布局统一处理
                }
            });

        } else {

            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(holder.icon_chat_buddy, userEntity);
                }
            });

            holder.icon_chat_buddy.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mMode == R.id.NORMAL_MODE) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            return true; //消费事件
                        }else if(event.getAction() == MotionEvent.ACTION_UP) {
                            if (mCurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
                                chatFragment.JumpToPersonalInfo(userEntity.getPeerId());
                            }
                        }
                        return false;
                    }
                    return false; //多选模式下不消费事件，交由父布局统一处理
                }
            });
        }

        holder.content_body.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mMode == R.id.MULTIPLE_CHOICE_MODE) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        return true;  //多选模式下消费事件
                    }else if(event.getAction() == MotionEvent.ACTION_UP){
                        Log.i("onTouch", "from: content_body");
                        onClickItemCallback(msg, holder.seleced_state_imageview);
                    }
                    return false;
                }
                return false; //正常模式下不做处理
            }
        });
    }

    private void onClickItemCallback(final MessageEntity msg, final ImageView state_imageview){

        Logger.getLogger().i("onItemClick ");
        if(mMode == R.id.MULTIPLE_CHOICE_MODE){
            if(!allSelectedObject().contains(msg)){
                addSelectedObject(msg);
            }else {
                removeSelectedObject(msg);
            }

            state_imageview.setVisibility(View.VISIBLE);
            Drawable drawable = context.getResources().getDrawable(R.mipmap.checkbox_off);
            if(allSelectedObject().contains(msg)){
                drawable = context.getResources().getDrawable(R.mipmap.checkbox_on);
            }
            state_imageview.setImageDrawable(drawable);

            if (mCurrentFragment instanceof ChatFragment) {
                ChatFragment chatFragment = (ChatFragment) mCurrentFragment;
                chatFragment.setMoreOperationBtnEnableState();
            }
        }
    }

    static class TextMsgViewHolder extends MsgBaseViewHolder {

        ViewStub im_plain_sender_view_stub;
        TextView text_chat_my_msg;

        ViewStub im_plain_receiver_view_stub;
        TextView text_chat_buddy_msg;

        TextMsgViewHolder(View convertView) {
            super(convertView);
            content_body = convertView.findViewById(R.id.im_plain_layout);
            im_plain_sender_view_stub = convertView.findViewById(R.id.im_plain_sender_view_stub);
            im_plain_receiver_view_stub = convertView.findViewById(R.id.im_plain_receiver_view_stub);
        }
    }

    static class ImageMsgViewHolder extends MsgBaseViewHolder {

        ViewStub im_image_sender_view_stub;
        ImageView img_chat_my_msg;

        ViewStub im_image_receiver_view_stub;
        ImageView img_chat_buddy_msg;

        ImageMsgViewHolder(View convertView){
            super(convertView);
            content_body = convertView.findViewById(R.id.im_image_layout);
            im_image_sender_view_stub = convertView.findViewById(R.id.im_image_sender_view_stub);
            im_image_receiver_view_stub = convertView.findViewById(R.id.im_image_receiver_view_stub);
        }
    }

    static class VoiceMsgViewHolder extends MsgBaseViewHolder {

        ViewStub im_voice_sender_view_stub;
        ViewGroup voice_msg_my_layout;
        TextView voice_msg_my_text;
        ImageView voice_msg_my_img;

        ViewStub im_voice_receiver_view_stub;
        ViewGroup voice_msg_buddy_layout;
        TextView voice_msg_buddy_text;
        ImageView voice_msg_buddy_img;

        ImageView voice_msg_read_state;

        VoiceMsgViewHolder(View convertView){
            super(convertView);
            content_body = convertView.findViewById(R.id.im_voice_layout);
            im_voice_sender_view_stub = convertView.findViewById(R.id.im_voice_sender_view_stub);
            im_voice_receiver_view_stub = convertView.findViewById(R.id.im_voice_receiver_view_stub);
        }
    }

    static class VideoMsgViewHolder extends MsgBaseViewHolder {
        ViewStub im_video_sender_view_stub;
        FrameLayout video_capture_my_layout;
        ImageView videoView_my_preview;
        Button button_play_my;

        ViewStub im_video_receiver_view_stub;
        FrameLayout video_capture_buddy_layout;
        ImageView videoView_buddy_preview;
        Button button_play_buddy;
        CustomRoundProgressBar roundProgressBar;

//        VideoView videoView_buddy;
//        VideoView videoView_my;

        VideoMsgViewHolder(View convertView){
            super(convertView);
            content_body = convertView.findViewById(R.id.im_video_layout);
            im_video_sender_view_stub = convertView.findViewById(R.id.im_video_sender_view_stub);
            im_video_receiver_view_stub = convertView.findViewById(R.id.im_video_receiver_view_stub);
        }
    }

    static class SystemMsgViewHolder extends MsgDateViewHolder {
        TextView system_msg_content;

        SystemMsgViewHolder(View convertView){
            super(convertView);
            system_msg_content = (TextView)convertView.findViewById(R.id.system_msg_content);
            convertView.setTag(this);
        }
    }

    static class MsgBaseViewHolder extends MsgDateViewHolder {
        ImageView seleced_state_imageview;
        View content_body;
        CircleImageView icon_chat_me;
        ImageView msg_state;

        CircleImageView icon_chat_buddy;
        TextView receiver_nickname;//群昵称,只对群有用

        MsgBaseViewHolder(View convertView){
            super(convertView);
            seleced_state_imageview = (ImageView)convertView.findViewById(R.id.seleced_state_imageview);
        }
    }

    static class MsgDateViewHolder extends BaseClickableViewHolder {
        TextView send_msg_date;
        MsgDateViewHolder(View convertView){
            super(convertView, false);
            send_msg_date = (TextView) convertView.findViewById(R.id.send_msg_date);
        }
    }

    public static class MessageTimeComparator implements Comparator<MessageEntity> {
        @Override
        public int compare(MessageEntity lhs, MessageEntity rhs) {
            if (lhs.getCreated() == rhs.getCreated()) {
                return (int)(lhs.getId() - rhs.getId());
            }
            return (int)(lhs.getCreated() - rhs.getCreated()); //倒序， 等价于 return (int)(lhs.getCreated() > rhs.getCreated());
        }
    }

}

