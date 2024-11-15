package com.milanac007.demo.im.db.entity;


import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/** 群成员
 * Created by milanac007 on 2017/4/10.
 */
@DatabaseTable(tableName = "group_member")
public class GroupMemberEntity extends PeerEntity {

    @DatabaseField
    String nickName; //在群里的昵称

    @DatabaseField
    int groupId; //所在group

    @DatabaseField
    int status; //0:在群  -1:不在群(被踢或离开)

    @DatabaseField
    String gKey; // groupId + "_" + groupMemberId (即 groupId + "_" + peerId)

    @Override
    public int getType() {
        return DBConstant.SESSION_TYPE_GROUP;
    }

    public GroupMemberEntity() {}

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }

    public String getgKey() {
        return gKey;
    }

    public void setgKey(String gKey) {
        this.gKey = gKey;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    @Override
    public String toString() {
        return "GroupMemberEntity{" +
                ", peerId=" + peerId +
                ", mainName='" + mainName + '\'' +
                ", pinyinName='" + pinyinName + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", avatar='" + avatar + '\'' +
                ", avatarLocalPath=" + avatarLocalPath + '\'' +

                ", nickName='" + nickName + '\'' +
                ", groupId='" + groupId +
                ", status='" + status +
                ", gKey='" + gKey + '\'' +
                '}';
    }

    ////////////////////////////////////////////////////////////////////////////
    //DB操作

    public static void insertOrUpdateSingleData(GroupMemberEntity field){
        checkValid();

        if (TextUtils.isEmpty(field.getPinyinName())) {
            PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            field.setPinyinName(field.getPinyinElement().pinyin);
        }

        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupMemberEntity, Long> dao = helper.getDao(GroupMemberEntity.class);
            int peerId = field.getPeerId();
            int groupId = field.getGroupId();
            QueryBuilder<GroupMemberEntity, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("groupId", groupId).and().eq("peerId", peerId);
            List<GroupMemberEntity> dataField = queryBuilder.query();
            if(dataField != null && !dataField.isEmpty()) {
                int update = dao.update(field);
                Logger.getLogger().i("update= %d", update);

                UpdateBuilder<GroupMemberEntity, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.where().eq("groupId", groupId).and().eq("peerId", peerId);
                updateBuilder.updateColumnValue("gKey", field.getgKey());
                int update2 = updateBuilder.update();
                Logger.getLogger().i("update2= %d", update2);
            }else {
                dao.create(field);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrUpdateSingleData(JSONObject data) {
        Gson gson = new Gson();
        GroupMemberEntity field = gson.fromJson(data.toJSONString(), GroupMemberEntity.class);
        insertOrUpdateSingleData(field);
    }

    /**
     * 事务提交
     * @param fields
     * @return
     */
    public static boolean insertOrUpdateMultiData(final List<GroupMemberEntity> fields){

        if(fields == null || fields.isEmpty())
            return true;

        checkValid();
        boolean result = false;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            final Dao<GroupMemberEntity, Long> dao = helper.getDao(GroupMemberEntity.class);
            ConnectionSource connectionSource = dao.getConnectionSource();
            TransactionManager manager = new TransactionManager(connectionSource);
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for(GroupMemberEntity field : fields){
                        int peerId = field.getPeerId();
                        int groupId = field.getGroupId();
                        QueryBuilder<GroupMemberEntity, Long> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("groupId", groupId).and().eq("peerId", peerId);
                        List<GroupMemberEntity> dataField = queryBuilder.query();

                        if(dataField != null && !dataField.isEmpty()) {
                            long id = dataField.get(0).getId();
                            field.setId(id);
                            int update = dao.update(field);
                            Logger.getLogger().i("update result = %s", update > 0 ? "success": "fail");

                            //更新字段的有效方法：
//                            String tableName = "group_member";
//                            String[] columnNames = {"nickName", "gKey"};
//                            String[] whereNames = {"groupId", "peerId"};
//                            String[] columnValues = {field.getNickName(), field.getgKey(), String.valueOf(groupId), String.valueOf(peerId)};
//                            int update1 = dao.updateRaw("update " + tableName + " set " + columnNames[0] + "=?, " + columnNames[1] + "=? where " + whereNames[0] + "=? and " + whereNames[1] + "=? ", columnValues);
//                            Logger.getLogger().i("update1= %d", update1);
//
//                            String sql = "update group_member set " + "nickName" + "= '" + field.getNickName() + "'," + "gKey" + "= '" + field.getgKey() +"'  where " + "groupId" + "= '" + groupId + "' and " + "peerId" + "= '" + peerId + "'";
//                            System.out.println("dao.update3 sql=" + sql);
//                            int update2 = dao.updateRaw(sql);
//                            Logger.getLogger().i("update2= %d", update2);
//
//                            UpdateBuilder<GroupMemberEntity, Long> updateBuilder = dao.updateBuilder();
//                            updateBuilder.where().eq("groupId", groupId).and().eq("peerId", peerId);
//                            updateBuilder.updateColumnValue("gKey", field.getgKey());
//                            updateBuilder.updateColumnValue("nickName", field.getNickName());
//                            int update3 = updateBuilder.update();
//                            Logger.getLogger().i("update3= %d", update3);
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

    public static List<GroupMemberEntity> loadAllGroupMembersByGroupId(int groupId){
        checkValid();
        List<GroupMemberEntity> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupMemberEntity, String> dao = helper.getDao(GroupMemberEntity.class);
            QueryBuilder<GroupMemberEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("groupId", groupId).and().eq("status", 0);
            dataField = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(GroupMemberEntity field: dataField) {
            if(TextUtils.isEmpty(field.getPinyinElement().pinyin)) {
                PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            }

            field.setPinyinName(field.getPinyinElement().pinyin);
        }
        return dataField;
    }

    public static GroupMemberEntity findGroupMem(int groupId, int memberId){
        checkValid();
        GroupMemberEntity field = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupMemberEntity, String> dao = helper.getDao(GroupMemberEntity.class);
            QueryBuilder<GroupMemberEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("groupId", groupId).and().eq("peerId", memberId).and().eq("status", 0);;
            field = queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(field != null) {
            if(TextUtils.isEmpty(field.getPinyinElement().pinyin)) {
                PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            }
            field.setPinyinName(field.getPinyinElement().pinyin);
        }

        return field;
    }

    public static List<GroupMemberEntity> findGroupMemByMemId(int memberId){
        checkValid();
        List<GroupMemberEntity> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupMemberEntity, String> dao = helper.getDao(GroupMemberEntity.class);
            QueryBuilder<GroupMemberEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("peerId", memberId);
            dataField = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(dataField == null) {
            return null;
        }

        for(GroupMemberEntity field: dataField) {
            if(TextUtils.isEmpty(field.getPinyinElement().pinyin)) {
                PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            }
            field.setPinyinName(field.getPinyinElement().pinyin);
        }
        return dataField;
    }

//    public void deleteGroupMem(int groupId, int memberId){ }

//    public void deleteGroupMem(int groupId, List<Integer> memberIds){ }

    public static void deleteGroupMemsByGroupId(int groupId){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupMemberEntity, String> dao = helper.getDao(GroupMemberEntity.class);
            DeleteBuilder<GroupMemberEntity, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("groupId", groupId));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
