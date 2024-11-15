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


public class AudioMessage extends MessageEntity implements Serializable {

    private String url = "";
    private String audioPath = "";
    private int audiolength =0 ;
    private int readStatus = MessageConstant.AUDIO_UNREAD;
    private int loadStatus;

    public AudioMessage(){
        super();
    }

    private AudioMessage(MessageEntity entity){
        // 父类主键
        id =  entity.getId();
        msgId  = entity.getMsgId();
        fromId = entity.getFromId();
        toId   = entity.getToId();
        content=entity.getContent();
        msgType=entity.getMsgType();
        sessionKey = entity.getSessionKey();
        displayType=entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    /**接受到网络包，解析成本地的数据*/
    public static AudioMessage parseFromNet(MessageEntity entity) throws JSONException {
        String strContent = entity.getContent();
        AudioMessage audioMessage = new AudioMessage(entity);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        String audioUrl = strContent;

        audioMessage.setUrl(audioUrl.isEmpty() ? null : audioUrl);
        audioMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);


        int index = audioUrl.indexOf("?");
        if(index > 0){
            int playTime  = Integer.valueOf(audioUrl.substring(index+1));
            audioMessage.setAudiolength(playTime);
            audioMessage.setAudioPath(CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(audioUrl.substring(0, index)));
        }

        /**抽离出来 或者用gson*/
        JSONObject extraContent = new JSONObject();
        extraContent.put("url",audioUrl);
        extraContent.put("audioPath",audioMessage.getAudioPath());
        extraContent.put("audiolength",audioMessage.getAudiolength());
        extraContent.put("readStatus",audioMessage.getReadStatus());
        extraContent.put("loadStatus", audioMessage.getLoadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }

    public static AudioMessage parseFromDB(MessageEntity entity) {
        if(entity.getDisplayType() != DBConstant.SHOW_AUDIO_TYPE){
           throw new RuntimeException("#AudioMessage# parseFromDB,not SHOW_AUDIO_TYPE");
        }
        AudioMessage audioMessage = new AudioMessage(entity);
        String originContent = entity.getContent();

        JSONObject extraContent = null;
        try {
            extraContent = new JSONObject(originContent);
            audioMessage.setAudioPath(extraContent.getString("audioPath"));
            audioMessage.setAudiolength(extraContent.getInt("audiolength"));
            audioMessage.setReadStatus(extraContent.getInt("readStatus"));
            audioMessage.setUrl(extraContent.getString("url"));

            int loadStatus = extraContent.getInt("loadStatus");
            if(loadStatus == MessageConstant.MSG_FILE_LOADING){
                loadStatus = MessageConstant.MSG_FILE_UNLOAD;
            }
            audioMessage.setLoadStatus(loadStatus);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return audioMessage;
    }

    public static AudioMessage buildForSend(float audioLen, String audioSavePath, UserEntity fromUser, PeerEntity peerEntity){
        int tLen = (int) (audioLen + 0.5);
        tLen = tLen < 1 ? 1 : tLen;
        if (tLen < audioLen) {
            ++tLen;
        }

        long nowTime = System.currentTimeMillis();
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(fromUser.getPeerId());
        audioMessage.setToId(peerEntity.getPeerId());
        audioMessage.setCreated(nowTime);
        audioMessage.setUpdated(nowTime);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_AUDIO : DBConstant.MSG_TYPE_SINGLE_AUDIO;
        audioMessage.setMsgType(msgType);

        audioMessage.setAudioPath(audioSavePath);
        audioMessage.setAudiolength(tLen);
        audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
        audioMessage.buildSessionKey(true);
        return audioMessage;
    }


    /**
     * Not-null value.
     * DB 存数据的时候需要
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("audioPath",audioPath);
            extraContent.put("audiolength",audiolength);
            extraContent.put("readStatus",readStatus);
            extraContent.put("loadStatus", loadStatus);
            extraContent.put("url", url);
            String audioContent = extraContent.toString();
            return audioContent;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public byte[] getSendContent() {
        try {
            /** 加密*/
//            String sendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(content));
//            return sendContent.getBytes("utf-8");
            return content.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    public byte[] getSendContent() {
//        byte[] result = new byte[4];
//        result = CommonUtil.intToBytes(audiolength);
//        if (TextUtils.isEmpty(audioPath)) {
//            return result;
//        }
//
//        byte[] bytes = FileUtil.getFileContent(audioPath);
//        if (bytes == null) {
//            return bytes;
//        }
//        int contentLength = bytes.length;
//        byte[] byteAduioContent = new byte[4 + contentLength];
//        System.arraycopy(result, 0, byteAduioContent, 0, 4);
//        System.arraycopy(bytes, 0, byteAduioContent, 4, contentLength);
//        return byteAduioContent;
//    }


    /***-------------------------------set/get----------------------------------*/
    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getAudiolength() {
        return audiolength;
    }

    public void setAudiolength(int audiolength) {
        this.audiolength = audiolength;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
