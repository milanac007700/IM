package com.milanac007.demo.im.db.helper;

import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.msg.AudioMessage;
import com.milanac007.demo.im.db.entity.msg.ImageMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.entity.msg.VideoMessage;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_AUDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_IMG;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_TEXT;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_VEDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_AUDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_IMG;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_SYSTEM_TEXT;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_TEXT;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_VEDIO;

public class ParseUtils {
    public static MessageEntity getMessageEntity(MessageEntity msgInfo){
        MessageEntity messageEntity = null;
        int msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
            case MSG_TYPE_SINGLE_SYSTEM_TEXT:
            case MSG_TYPE_GROUP_SYSTEM_TEXT: // TODO
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_SINGLE_IMG:
            case MSG_TYPE_GROUP_IMG:
                try {
                    messageEntity = analyzeImage(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case MSG_TYPE_SINGLE_VEDIO:
            case MSG_TYPE_GROUP_VEDIO:
                try {
                    messageEntity = analyzeVideo(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }


        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }



    public static TextMessage analyzeText(MessageEntity msgInfo){
        return TextMessage.parseFromNet(msgInfo);
    }

    public static ImageMessage analyzeImage(MessageEntity msgInfo) throws JSONException{
        return ImageMessage.parseFromNet(msgInfo);
    }

    public static AudioMessage analyzeAudio(MessageEntity msgInfo) throws JSONException {
        return AudioMessage.parseFromNet(msgInfo);
    }

    public static VideoMessage analyzeVideo(MessageEntity msgInfo) throws JSONException {
        return VideoMessage.parseFromNet(msgInfo);
    }

}
