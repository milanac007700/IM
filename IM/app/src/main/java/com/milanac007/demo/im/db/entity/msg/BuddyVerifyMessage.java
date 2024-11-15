package com.milanac007.demo.im.db.entity.msg;

import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.utils.ImConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 *  添加好友、同意添加，删除好友请求相关
 * Created by zqguo on 2017/3/23.
 */
@DatabaseTable( tableName = "buddyVerifyMessage")
public class BuddyVerifyMessage {
    @DatabaseField(id = true)
    protected Long id;

    @DatabaseField
    protected int fromId;

    @DatabaseField
    protected int toId;

    @DatabaseField
    private int SessionId; //标示该条消息属于的哪个会话

    @DatabaseField
    protected String content;

    @DatabaseField
    protected int msgType;

    @DatabaseField
    protected long created;

    public BuddyVerifyMessage(){

    }
    public BuddyVerifyMessage(Long id, int SessionId, int fromId, int toId, String content, int msgType, long created){
        this.id = id;
        this.SessionId = SessionId;
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.msgType = msgType;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getSessionId() {
        return SessionId;
    }

    public void setSessionId(int sessionId) {
        SessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "BuddyVerifyMessage{" +
                "id=" + id +
                ", SessionId=" + SessionId +
                ", fromId=" + fromId +
                ", toId='" + toId + '\'' +
                ", content='" + content + '\'' +
                ", msgType='" + msgType + '\'' +
                ", created='" + created + '\'' +
                '}';
    }

    /* ----------------------------------------好友验证------------------------------------------------ */
    public static long insertOrUpdateBuddyVerifyMsg(BuddyVerifyMessage field){
        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            if(field.getId() == null) {
                dao.create(field);
                return -1;
            }

            long id = field.getId();
            QueryBuilder<BuddyVerifyMessage, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("id", id);
            List<BuddyVerifyMessage> dataField = queryBuilder.query();
            if(dataField != null && !dataField.isEmpty()) {
                dao.update(field);
            }else {
                dao.create(field);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static BuddyVerifyMessage getVerifyMsgBySessionId(Integer sessionId){
        BuddyVerifyMessage dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            QueryBuilder<BuddyVerifyMessage, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("SessionId", sessionId);
            dataField = queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataField;
    }

    public static BuddyVerifyMessage getAddBuddyVerifyMsgBySessionId(Integer sessionId){
        BuddyVerifyMessage dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            QueryBuilder<BuddyVerifyMessage, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("SessionId", sessionId).and().eq("msgType", DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST);
            dataField = queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataField;
    }

    public static List<BuddyVerifyMessage> getVerifyMsgList(){
        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            QueryBuilder<BuddyVerifyMessage, String> queryBuilder = dao.queryBuilder();
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void deletebuddyVerifyMsgsBySessionId(Integer sessionId){
        if(sessionId <= 0)
            return;

        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            DeleteBuilder<BuddyVerifyMessage, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("SessionId", sessionId));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAddBuddyAcceptVerifyMsgsBySessionId(Integer sessionId){
        if(sessionId <= 0)
            return;

        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            DeleteBuilder<BuddyVerifyMessage, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("SessionId", sessionId).and().eq("msgType", DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void clearVerifyMsgs(){
        final DataBaseHelper helper = DataBaseHelper.getHelper(App.getContext());
        try {
            Dao<BuddyVerifyMessage, String> dao = helper.getDao(BuddyVerifyMessage.class);
            DeleteBuilder<BuddyVerifyMessage, String> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
