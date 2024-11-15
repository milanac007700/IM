package com.milanac007.demo.im.db.entity.msg;

import android.text.TextUtils;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//import com.mogujie.tt.ui.adapter.album.ImageItem;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class ImageMessage extends MessageEntity implements Serializable {

    /**本地保存的path*/
    private String path = "";
    /**图片的网络地址*/
    private String url = "";
    private int loadStatus;

    //存储图片消息
    private static java.util.HashMap<Long, ImageMessage> imageMessageMap = new java.util.HashMap<Long, ImageMessage>();
    private static ArrayList<ImageMessage> imageList=null;
    /**
     * 添加一条图片消息
     * @param msg
     */
    public static synchronized void addToImageMessageList(ImageMessage msg){
        try {
            if(msg!=null && msg.getId()!=null)
            {
                imageMessageMap.put(msg.getId(),msg);
            }
        }catch (Exception e){
        }
    }

    /**
     * 获取图片列表
     * @return
     */
    public static ArrayList<ImageMessage> getImageMessageList(){
        imageList = new ArrayList<>();
        java.util.Iterator it = imageMessageMap.keySet().iterator();
        while (it.hasNext()) {
            imageList.add(imageMessageMap.get(it.next()));
        }
        Collections.sort(imageList, new Comparator<ImageMessage>(){
            public int compare(ImageMessage image1, ImageMessage image2) {
                Long a =  image1.getUpdated();
                Long b = image2.getUpdated();
                if(a.equals(b))
                {
                    return image2.getId().compareTo(image1.getId());
                }
                // 升序
                //return a.compareTo(b);
                // 降序
                return b.compareTo(a);
            }
        });
        return imageList;
    }

    /**
     * 清除图片列表
     */
    public static synchronized void clearImageMessageList(){
        imageMessageMap.clear();
        imageList.clear();
    }



    public ImageMessage(){
        super();
    }

    /**消息拆分的时候需要*/
    private ImageMessage(MessageEntity entity){
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
    public static ImageMessage parseFromNet(MessageEntity entity) throws JSONException {
        ImageMessage imageMessage = new ImageMessage(entity);
        imageMessage.setDisplayType(DBConstant.SHOW_IMAGE_TYPE);

        String imageUrl = entity.getContent();

        imageMessage.setUrl(TextUtils.isEmpty(imageUrl) ? "" : imageUrl);
        imageMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
        imageMessage.setStatus(MessageConstant.MSG_SUCCESS);

        int index = imageUrl.indexOf("?");
        if(index > 0){
            imageUrl = imageUrl.substring(0, index);
        }
        String filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(imageUrl);
        imageMessage.setPath(filePath);


        /**抽离出来 或者用gson, 因MessageDao没这些字段，故保存在content里 */
        JSONObject extraContent = new JSONObject();
        extraContent.put("path",imageMessage.getPath());
        extraContent.put("url",imageMessage.getUrl());
        extraContent.put("loadStatus", imageMessage.getLoadStatus());
        imageMessage.setContent(extraContent.toString());
        return imageMessage;

    }


    public static ImageMessage parseFromDB(MessageEntity entity)  {
        if(entity.getDisplayType() != DBConstant.SHOW_IMAGE_TYPE){
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
        }
        ImageMessage imageMessage = new ImageMessage(entity);
        String originContent = entity.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent);
            imageMessage.setPath(extraContent.getString("path"));
            imageMessage.setUrl(extraContent.getString("url"));
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


    public static ImageMessage buildForSend(String takePhotoSavePath, UserEntity fromUser, PeerEntity peerEntity){
        ImageMessage imageMessage = new ImageMessage();
        long nowTime = System.currentTimeMillis();
        imageMessage.setFromId(fromUser.getPeerId());
        imageMessage.setToId(peerEntity.getPeerId());
        imageMessage.setUpdated(nowTime);
        imageMessage.setCreated(nowTime);
        imageMessage.setDisplayType(DBConstant.SHOW_IMAGE_TYPE);
        imageMessage.setPath(takePhotoSavePath);
        int peerType = peerEntity.getType();

        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_IMG : DBConstant.MSG_TYPE_SINGLE_IMG;
        imageMessage.setMsgType(msgType);

        imageMessage.setStatus(MessageConstant.MSG_SENDING);
        imageMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
        imageMessage.buildSessionKey(true);
        return imageMessage;
    }


    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("path",path);
            extraContent.put("url",url);
            extraContent.put("loadStatus",loadStatus);
            return extraContent.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getSendContent() {
        // 加密, 发送的时候非常关键
//        String sendContent = MessageConstant.IMAGE_MSG_START
//                + url + MessageConstant.IMAGE_MSG_END;
//
//       String encrySendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(sendContent));
//
//        try {
//            return encrySendContent.getBytes("utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        try {
            return url.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**-----------------------set/get------------------------*/
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}
