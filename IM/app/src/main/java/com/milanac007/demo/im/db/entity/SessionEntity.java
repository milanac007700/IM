package com.milanac007.demo.im.db.entity;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;


/*******************会话，存取DB**********************/
@DatabaseTable( tableName = "session")
public class SessionEntity {
    @DatabaseField(generatedId = true, unique = true) //指定字段为自增主键, 唯一
    private Long id;

    @DatabaseField
    private String sessionKey;

    @DatabaseField
    private int peerId;

    @DatabaseField
    private int peerType;

    @DatabaseField
    private int latestMsgType;

    @DatabaseField
    private int latestMsgId;

    @DatabaseField
    private String latestMsgData;

    @DatabaseField
    private int talkId; //单聊：谁说的; 群聊：哪个群成员说的话

    @DatabaseField
    private long created;

    @DatabaseField
    private long updated;

    private static Context context;


    public SessionEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getSessionKey() {
        return sessionKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerType() {
        return peerType;
    }

    public void setPeerType(int peerType) {
        this.peerType = peerType;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgId() {
        return latestMsgId;
    }

    public void setLatestMsgId(int latestMsgId) {
        this.latestMsgId = latestMsgId;
    }

    /** Not-null value. */
    public String getLatestMsgData() {
        return latestMsgData;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public int getTalkId() {
        return talkId;
    }

    public void setTalkId(int talkId) {
        this.talkId = talkId;
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

    // KEEP METHODS - put your custom methods here
    public String buildSessionKey(){
        if(peerType <=0 || peerId <=0){
            throw new IllegalArgumentException(
                    "SessionEntity buildSessionKey error,cause by some params <=0");
        }
        sessionKey = EntityChangeEngine.getSessionKey(peerId, peerType);
        return sessionKey;
    }
    // KEEP METHODS END


    ////////////////////////////////////////////////////////////////////////////
    //DB操作

    public static void checkValid() {
        if(context == null) {
            context = App.getContext();
        }
    }

    public static void insertOrUpdateSingleData(SessionEntity field){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<SessionEntity, String> dao = helper.getDao(SessionEntity.class);
            int peerId = field.getPeerId();
            QueryBuilder<SessionEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("peerId", peerId);
            List<SessionEntity> dataField = queryBuilder.query();
            if(dataField != null && !dataField.isEmpty()) {
                dao.update(field);
            }else {
                dao.create(field);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrUpdateSingleData(JSONObject data) {
        Gson gson = new Gson();
        SessionEntity field = gson.fromJson(data.toJSONString(), SessionEntity.class);
        insertOrUpdateSingleData(field);
    }

    /**
     * 事务提交
     * @param fields
     * @return
     */
    public static boolean insertOrUpdateMultiData(final List<SessionEntity> fields){

        if(fields == null || fields.isEmpty())
            return true;

        checkValid();
        boolean result = false;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            final Dao<SessionEntity, String> dao = helper.getDao(SessionEntity.class);
            ConnectionSource connectionSource = dao.getConnectionSource();
            TransactionManager manager = new TransactionManager(connectionSource);
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for(SessionEntity field : fields){
                        int peerId = field.getPeerId();
                        QueryBuilder<SessionEntity, String> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("peerId", peerId);
                        List<SessionEntity> dataField = queryBuilder.query();

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

    public static boolean insertOrUpdateMultiData(JSONArray datas) {
//        List<UserEntity> list = new ArrayList<>();
//        Gson gson = new Gson();
//        List<UserEntity> fields = gson.fromJson(datas.toJSONString(), list.getClass());
//        return insertOrUpdateMultiData(fields);

        for(int i = 0; i<datas.size(); i++) {
            JSONObject data =  datas.getJSONObject(i);
            insertOrUpdateSingleData(data);
        }
        return true;
    }



    public static List<SessionEntity> getSessionList() {
        checkValid();
        List<SessionEntity> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<SessionEntity, String> dao = helper.getDao(SessionEntity.class);
            QueryBuilder<SessionEntity, String> queryBuilder = dao.queryBuilder();
            dataField = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataField;
    }

    public static void deleteSession(String sessionKey) {
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<SessionEntity, String> dao = helper.getDao(SessionEntity.class);
            DeleteBuilder<SessionEntity, String> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("sessionKey", sessionKey);
            int result = deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
