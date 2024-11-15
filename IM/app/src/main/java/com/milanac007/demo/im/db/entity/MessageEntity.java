package com.milanac007.demo.im.db.entity;

/**
 * 这个类不同与其他自动生成代码
 * 需要依赖conten与display 依赖不同的状态
 * */
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.entity.msg.AudioMessage;
import com.milanac007.demo.im.db.entity.msg.ImageMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.entity.msg.VideoMessage;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.db.helper.SequenceNumberMaker;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import kotlin.jvm.Transient;


@DatabaseTable( tableName = "message")
public class MessageEntity implements java.io.Serializable {

    @DatabaseField(generatedId = true)
    protected Long id;

    @DatabaseField
    protected int msgId;

    @DatabaseField
    protected int fromId;

    @DatabaseField
    protected int toId;

    @DatabaseField
    protected String sessionKey;

    @DatabaseField
    protected String content;

    @DatabaseField
    protected int msgType;

    @DatabaseField
    protected int displayType;

    @DatabaseField
    protected int status;

    @DatabaseField
    protected long created;

    @DatabaseField
    protected long updated;

    @DatabaseField
    protected boolean isGIfEmo;

    protected static Context context;


    public MessageEntity() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    public MessageEntity(Long id, int msgId, int fromId, int toId, String sessionKey, String content, int msgType, int displayType, int status, long created, long updated) {
        this.id = id;
        this.msgId = msgId;
        this.fromId = fromId;
        this.toId = toId;
        this.sessionKey = sessionKey;
        this.content = content;
        this.msgType = msgType;
        this.displayType = displayType;
        this.status = status;
        this.created = created;
        this.updated = updated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    /** Not-null value. */
    public String getSessionKey() {
        return sessionKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    /** Not-null value. */
    public String getContent() {
        return content;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setContent(String content) {
        this.content = content;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getDisplayType() {
        return displayType;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        MessageEntity.context = context;
    }

    public int getSessionType() {
        switch (msgType) {
            case DBConstant.MSG_TYPE_SINGLE_SYSTEM_TEXT:
            case  DBConstant.MSG_TYPE_SINGLE_TEXT:
            case  DBConstant.MSG_TYPE_SINGLE_AUDIO:
            case  DBConstant.MSG_TYPE_SINGLE_VEDIO:
            case  DBConstant.MSG_TYPE_SINGLE_IMG:
            case  DBConstant.MSG_TYPE_SINGLE_FILE:
            case DBConstant.MSG_TYPE_CANCEL_SGL_MSG:
                return DBConstant.SESSION_TYPE_SINGLE;

            case DBConstant.MSG_TYPE_GROUP_SYSTEM_TEXT:
            case DBConstant.MSG_TYPE_GROUP_TEXT:
            case DBConstant.MSG_TYPE_GROUP_AUDIO:
            case DBConstant.MSG_TYPE_GROUP_VEDIO:
            case DBConstant.MSG_TYPE_GROUP_IMG:
            case DBConstant.MSG_TYPE_GROUP_FILE:
            case DBConstant.MSG_TYPE_CANCEL_GRP_MSG:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                //todo 有问题
                return DBConstant.SESSION_TYPE_SINGLE;
        }
    }


    public String getMessageDisplay() {
        switch (displayType){
            case DBConstant.SHOW_AUDIO_TYPE:
                return DBConstant.DISPLAY_FOR_AUDIO;
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                return content;
            case DBConstant.SHOW_IMAGE_TYPE:
                return DBConstant.DISPLAY_FOR_IMAGE;
            case DBConstant.SHOW_VIDEO_TYPE:
                return DBConstant.DISPLAY_FOR_VIDEO;
            case DBConstant.SHOW_FILE_TYPE:
                return DBConstant.DISPLAY_FOR_FILE;

            case DBConstant.SHOW_AUDIO_CALL_TYPE:
                return DBConstant.DISPLAY_FOR_AUDIO_CALL;

            case DBConstant.SHOW_VIDEO_CALL_TYPE:
                return DBConstant.DISPLAY_FOR_VIDEO_CALL;

            case DBConstant.SHOW_MIX_TEXT:
                return DBConstant.DISPLAY_FOR_MIX;

            default:
                return DBConstant.DISPLAY_FOR_ERROR;
        }
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "id=" + id +
                ", msgId=" + msgId +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", content='" + content + '\'' +
                ", msgType=" + msgType +
                ", displayType=" + displayType +
                ", status=" + status +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageEntity)) return false;

        MessageEntity that = (MessageEntity) o;

        if (created != that.created) return false;
        if (displayType != that.displayType) return false;
        if (fromId != that.fromId) return false;
        if (msgId != that.msgId) return false;
        if (msgType != that.msgType) return false;
        if (status != that.status) return false;
        if (toId != that.toId) return false;
        if (updated != that.updated) return false;
        if (!content.equals(that.content)) return false;
        if (!id.equals(that.id)) return false;
        if (!sessionKey.equals(that.sessionKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + msgId;
        result = 31 * result + fromId;
        result = 31 * result + toId;
        result = 31 * result + sessionKey.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + msgType;
        result = 31 * result + displayType;
        result = 31 * result + status;
        result = 31 * result + (int)created;
        result = 31 * result + (int)updated;
        return result;
    }


    /**
     * 获取会话的sessionId
     * @param isSend
     * @return
     */
    public int getPeerId(boolean isSend){
        if(isSend){  /**自己发出去的*/
            return toId;
        }else{  /**接收到的*/
            switch (getSessionType()){
                case DBConstant.SESSION_TYPE_SINGLE:
                    return fromId;
                case DBConstant.SESSION_TYPE_GROUP:
                    return toId;
                default:
                    return toId;
            }
        }
    }

    public byte[] getSendContent(){
        return null;
    }

    public boolean isGIfEmo() {
        return isGIfEmo;
    }

    public void setGIfEmo(boolean isGIfEmo) {
        this.isGIfEmo = isGIfEmo;
    }

    public boolean isSend(int loginId){
        return (loginId == fromId);
    }

    public String buildSessionKey(boolean isSend){
        int sessionType = getSessionType();
        int peerId = getPeerId(isSend);
        sessionKey = EntityChangeEngine.getSessionKey(peerId,sessionType);
        return sessionKey;
    }

    ////////////////////////////////////////////////////////////////////////////
    //DB操作

    public static void checkValid() {
        if(context == null) {
            context = App.getContext();
        }
    }

    // where (msgId >= startMsgId and msgId<=lastMsgId) or
    // (msgId=0 and status = 0)
    // order by created desc
    // limit count;
    // 按照时间排序
    public static List<MessageEntity> getHistoryMsg(String sessionKey, long lastCreateTime, int count, boolean isFirst){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            QueryBuilder<MessageEntity, String> queryBuilder = dao.queryBuilder();

            Where<MessageEntity, String> where = queryBuilder.where();
            where.eq("sessionKey", sessionKey);
            if(isFirst) {
                where.and().le("created", lastCreateTime); // <=
            }else {
                where.and().lt("created", lastCreateTime); // <
            }

            queryBuilder.setWhere(where);
            queryBuilder.orderBy("created", false).orderBy("id", false).limit(count);
            List<MessageEntity> listMsg = queryBuilder.query();

            for(MessageEntity msg : listMsg){
                if(msg.getStatus() == MessageConstant.MSG_SENDING) //重置状态
                    msg.setStatus(MessageConstant.MSG_FAILURE);
            }
            return formatMessage(listMsg);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static MessageEntity getLastMsgBySessionKey(String sessionKey){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            QueryBuilder<MessageEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.setWhere(queryBuilder.where().eq("sessionKey", sessionKey));
            queryBuilder.orderBy("created", false).orderBy("id", false).limit(1);
            List<MessageEntity> listMsg = queryBuilder.query();

            if(listMsg == null || listMsg.isEmpty())
                return null;

            return formatMessage(listMsg).get(0);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }


    public static void insertOrUpdateSingleData(MessageEntity field){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            if(field.getId() == null) {
                dao.create(field);
                return;
            }

            long id = field.getId();
            QueryBuilder<MessageEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("id", id);
            List<MessageEntity> dataField = queryBuilder.query();
            if(dataField != null && !dataField.isEmpty()) {
                dao.update(field);
            }else {
                dao.create(field);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 事务提交
     * @param fields
     * @return
     */
    public static boolean insertOrUpdateMultiData(final List<MessageEntity> fields){

        if(fields == null || fields.isEmpty())
            return true;

        checkValid();
        boolean result = false;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            final Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            ConnectionSource connectionSource = dao.getConnectionSource();
            TransactionManager manager = new TransactionManager(connectionSource);
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for(MessageEntity field : fields){
                        int msgId = field.getMsgId();
                        QueryBuilder<MessageEntity, String> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("msgId", msgId);
                        List<MessageEntity> dataField = queryBuilder.query();

                        if(dataField != null && !dataField.isEmpty()) {
                            dao.update(field);
                        }else {
                            dao.create(field);
                        }
                    }
                    return true;
                }
            };

            result = manager.callInTransaction(callable);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static MessageEntity getOneMessage(int msgId, String sessionKey){

        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            QueryBuilder<MessageEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.setWhere(queryBuilder.where().eq("sessionKey", sessionKey).and().eq("msgId", msgId));
            List<MessageEntity> listMsg = queryBuilder.query();

            if(listMsg == null || listMsg.isEmpty())
                return null;

            return formatMessage(listMsg.get(0));

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void deleteMessageBySessionId(String sessionKey){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            DeleteBuilder<MessageEntity, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("sessionKey", sessionKey));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMessage(int msgId, String sessionKey){
        if(msgId <= 0){
            return;
        }

        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<MessageEntity, String> dao = helper.getDao(MessageEntity.class);
            DeleteBuilder<MessageEntity, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("sessionKey", sessionKey).and().eq("msgId", msgId));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static MessageEntity formatMessage(MessageEntity msg){
        if(msg == null)
            return null;

        MessageEntity messageEntity = null;
        int displayType = msg.getDisplayType();
        switch (displayType){
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
            case DBConstant.SHOW_AUDIO_CALL_TYPE:
            case DBConstant.SHOW_VIDEO_CALL_TYPE:
                messageEntity = TextMessage.parseFromDB(msg);
                break;
            case DBConstant.SHOW_IMAGE_TYPE:
                messageEntity = ImageMessage.parseFromDB(msg);
                break;
            case DBConstant.SHOW_AUDIO_TYPE:
                messageEntity = AudioMessage.parseFromDB(msg);
                break;
            case DBConstant.SHOW_VIDEO_TYPE:
                messageEntity = VideoMessage.parseFromDB(msg);
                break;
        }
        return messageEntity;
    }


    public static List<MessageEntity> formatMessage(List<MessageEntity> msgList){
        if(msgList.size() <= 0){
            return Collections.emptyList();
        }
        ArrayList<MessageEntity> newList = new ArrayList<>();
        for(MessageEntity info: msgList){
            newList.add(formatMessage(info));
        }
        return newList;
    }

}
