package com.milanac007.demo.im.db.entity.msg;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.utils.CommonFunction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by zqguo on 2017/3/21.
 */
public class VideoMessage extends MessageEntity implements Serializable {

    /**图片的网络地址*/
    private String videoUrl = "";
    private String thumbnailUrl = "";

    /**本地保存的path*/
    private String videoPath = "";
    private String thumbnailPath = "";

    private int loadStatus;

    public VideoMessage(){
        super();
    }

    /**消息拆分的时候需要*/
    private VideoMessage(MessageEntity entity){
        /**父类的id*/
        id =  entity.getId();
        msgId  = entity.getMsgId();
        fromId = entity.getFromId();
        toId   = entity.getToId();
        sessionKey = entity.getSessionKey();
        content=entity.getContent();
        msgType=entity.getMsgType();
        displayType=entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    /**接受到网络包，解析成本地的数据*/
    public static VideoMessage parseFromNet(MessageEntity entity) throws JSONException {

        String strContent = entity.getContent();
        VideoMessage videoMessage = new VideoMessage(entity);
        videoMessage.setDisplayType(DBConstant.SHOW_VIDEO_TYPE);

        String[] splits = strContent.split(";");
        if(splits != null && splits.length >0){
            if(splits[0].contains("mp4")){
                videoMessage.setVideoUrl(splits[0]);
                videoMessage.setThumbnailUrl(splits[1]);
            }else if(splits[1].contains("mp4")){
                videoMessage.setVideoUrl(splits[1]);
                videoMessage.setThumbnailUrl(splits[0]);
            }

            String videoUrl = videoMessage.getVideoUrl();
            int index = videoUrl.indexOf("?");
            if(index > 0){
                videoUrl = videoUrl.substring(0, index);
            }
            videoMessage.setVideoPath(CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(videoUrl));


            String thumbnailUrl = videoMessage.getThumbnailUrl();
            int index2 = thumbnailUrl.indexOf("?");
            if(index2 > 0){
                thumbnailUrl = thumbnailUrl.substring(0, index2);
            }
            videoMessage.setThumbnailPath(CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(thumbnailUrl));
        }


        videoMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
        videoMessage.setStatus(MessageConstant.MSG_SUCCESS);

        /**抽离出来 或者用gson*/
        JSONObject extraContent = new JSONObject();
        extraContent.put("videoUrl",videoMessage.getVideoUrl());
        extraContent.put("videoPath",videoMessage.getVideoPath());
        extraContent.put("thumbnailUrl",videoMessage.getThumbnailUrl());
        extraContent.put("thumbnailPath",videoMessage.getThumbnailPath());
        extraContent.put("loadStatus", videoMessage.getLoadStatus());

        videoMessage.setContent(extraContent.toString());

        return videoMessage;

    }


    public static VideoMessage parseFromDB(MessageEntity entity)  {
        if(entity.getDisplayType() != DBConstant.SHOW_VIDEO_TYPE){
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
        }
        VideoMessage imageMessage = new VideoMessage(entity);
        String originContent = entity.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent);

            imageMessage.setVideoPath(extraContent.getString("videoPath"));
            imageMessage.setVideoUrl(extraContent.getString("videoUrl"));
            imageMessage.setThumbnailPath(extraContent.getString("thumbnailPath"));
            imageMessage.setThumbnailUrl(extraContent.getString("thumbnailUrl"));

            int loadStatus = extraContent.getInt("loadStatus");

            //todo temp solution
            if(loadStatus == MessageConstant.MSG_FILE_LOADING){
                loadStatus = MessageConstant.MSG_FILE_UNLOAD;
            }
            imageMessage.setLoadStatus(loadStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageMessage;
    }

    // 消息页面，发送小视频消息
    public static VideoMessage buildForSend(String videoPath, UserEntity fromUser, PeerEntity peerEntity){
        VideoMessage videoMessage = new VideoMessage();
        long nowTime = System.currentTimeMillis();
        videoMessage.setFromId(fromUser.getPeerId());
        videoMessage.setToId(peerEntity.getPeerId());
        videoMessage.setUpdated(nowTime);
        videoMessage.setCreated(nowTime);
        videoMessage.setDisplayType(DBConstant.SHOW_VIDEO_TYPE);
        videoMessage.setVideoPath(videoPath);

        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_VEDIO : DBConstant.MSG_TYPE_SINGLE_VEDIO;
        videoMessage.setMsgType(msgType);

        videoMessage.setStatus(MessageConstant.MSG_SENDING);
        videoMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
        videoMessage.buildSessionKey(true);
        return videoMessage;
    }

    /**
     * Not-null value.
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("videoPath",videoPath);
            extraContent.put("videoUrl",videoUrl);
            extraContent.put("thumbnailPath",thumbnailPath);
            extraContent.put("thumbnailUrl",thumbnailUrl);
            extraContent.put("loadStatus",loadStatus);
            String imageContent = extraContent.toString();
            return imageContent;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getSendContent() {
        try {
            String sendContent = videoUrl+";" + thumbnailUrl;
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**-----------------------set/get------------------------*/

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }

}

