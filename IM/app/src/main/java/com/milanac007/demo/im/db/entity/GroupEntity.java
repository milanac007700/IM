package com.milanac007.demo.im.db.entity;

import android.text.TextUtils;

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
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@DatabaseTable (tableName = "group")
public class GroupEntity extends PeerEntity {
    @DatabaseField
    private int groupType;   // NORMAL = 1; TEMP = 2

    @DatabaseField
    private int creatorId; //群创建者/管理员

    @DatabaseField
    private int userCnt;

    @DatabaseField
    private String userList;

    @DatabaseField
    private int version; //版本号，创建群时为1

    @DatabaseField
    private int status; // 1: shield屏蔽  0: not shield

    @DatabaseField
    private String notice; //群公告

    @DatabaseField
    private String displayName; //UI上显示的群名字

    @DatabaseField
    private boolean isFake; //是否是假冒数据


    private List<GroupMemberEntity> groupMemberList;

    // KEEP FIELDS - put your custom fields here
    private SearchElement searchElement = new SearchElement();

    public SearchElement getSearchElement() {
        return searchElement;
    }

    public void setSearchElement(SearchElement searchElement) {
        this.searchElement = searchElement;
    }


    // KEEP FIELDS END


    public GroupEntity() {

    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    /** Not-null value. */
    public String getMainName() {
        return mainName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    /** Not-null value. */
    public String getAvatar() {
        return avatar;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getUserCnt() {
        return userCnt;
    }

    public void setUserCnt(int userCnt) {
        this.userCnt = userCnt;
    }

    /** Not-null value. */
    public String getUserList() {
        return userList;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUserList(String userList) {
        this.userList = userList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }

    @Override
    public int getType() {
        return DBConstant.SESSION_TYPE_GROUP;
    }


    @Override
    public String toString() {
        return "GroupEntity{" +
                ", peerId=" + peerId +
                ", mainName='" + mainName + '\'' +
                ", pinyinName='" + pinyinName + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", avatar='" + avatar + '\'' +
                ", avatarLocalPath=" + avatarLocalPath + '\'' +

                ", groupType='" + groupType +
                ", creatorId='" + creatorId +
                ", userCnt='" + userCnt +
                ", userList='" + userList + '\'' +
                ", groupMemberList.size()='" + groupMemberList.size()  + '\'' +
                ", version='" + version +
                ", status='" + status +
                ", notice='" + notice + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isFake='" + isFake +
                '}';
    }

    ////////////////////////////////////////////////////////////////////////////
    //DB操作

    public static void insertOrUpdateSingleData(GroupEntity field){
        checkValid();

        if (TextUtils.isEmpty(field.getPinyinName())) {
            PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            field.setPinyinName(field.getPinyinElement().pinyin);
        }

        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupEntity, String> dao = helper.getDao(GroupEntity.class);
            int peerId = field.getPeerId();
            QueryBuilder<GroupEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("peerId", peerId);
            List<GroupEntity> dataField = queryBuilder.query();
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
        GroupEntity groupEntity = gson.fromJson(data.toJSONString(), GroupEntity.class);
        insertOrUpdateSingleData(groupEntity);
        GroupMemberEntity.insertOrUpdateMultiData(groupEntity.getGroupMemberList());
    }

    /**
     * 事务提交
     * @param fields
     * @return
     */
    public static boolean insertOrUpdateMultiData(final List<GroupEntity> fields){

        if(fields == null || fields.isEmpty())
            return true;

        checkValid();
        boolean result = false;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            final Dao<GroupEntity, String> dao = helper.getDao(GroupEntity.class);
            ConnectionSource connectionSource = dao.getConnectionSource();
            TransactionManager manager = new TransactionManager(connectionSource);
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for(GroupEntity field : fields){
                        int peerId = field.getPeerId();
                        QueryBuilder<GroupEntity, String> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("peerId", peerId);
                        List<GroupEntity> dataField = queryBuilder.query();

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

    public static List<GroupEntity> loadAllGroup(){
        checkValid();
        List<GroupEntity> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupEntity, String> dao = helper.getDao(GroupEntity.class);
            QueryBuilder<GroupEntity, String> queryBuilder = dao.queryBuilder();
            dataField = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for(GroupEntity field: dataField) {
            if(TextUtils.isEmpty(field.getPinyinElement().pinyin)) {
                PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            }

            field.setPinyinName(field.getPinyinElement().pinyin);
        }

        return dataField;
    }

    /**
     * 通讯录群
     * @return
     */
    public static List<GroupEntity> loadAllGroupNormal(){
        checkValid();
        List<GroupEntity> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupEntity, String> dao = helper.getDao(GroupEntity.class);
            QueryBuilder<GroupEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in("groupType", 1, 17);
            dataField = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(dataField.size() == 0) {
            return null;
        }

        for(GroupEntity field: dataField) {
            if(TextUtils.isEmpty(field.getPinyinElement().pinyin)) {
                PinYin.getPinYin(field.getMainName(), field.getPinyinElement());
            }

            field.setPinyinName(field.getPinyinElement().pinyin);
        }
        return dataField;
    }


    public static void delGroup(int groupId) {
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<GroupEntity, String> dao = helper.getDao(GroupEntity.class);
            DeleteBuilder<GroupEntity, String> deleteBuilder = dao.deleteBuilder();

            deleteBuilder.setWhere(deleteBuilder.where().eq("peerId", groupId));
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * 获取群组成员的list
     * -- userList 前后去空格，按照逗号区分， 不检测空的成员(非法)
     */
    public List<Integer> getlistGroupMemberIds(){
        if(TextUtils.isEmpty(userList)){
            return Collections.emptyList();
        }
        String[] arrayUserIds =  userList.trim().split(",");
        if(arrayUserIds.length <=0){
            return Collections.emptyList();
        }
        /**zhe'g*/
        List<Integer> result = new ArrayList<>();
        for(int index=0;index < arrayUserIds.length;index++){
            int userId =  Integer.parseInt(arrayUserIds[index]);
            result.add(userId);
        }
        return result;
    }

    //todo 入参变为 set【自动去重】
    // 每次都要转换 性能不是太好，todo
    public void setlistGroupMemberIds(List<Integer> memberList){
        String userList = TextUtils.join(",", memberList);
        setUserList(userList);
    }


    public List<GroupMemberEntity> getGroupMemberList() {
        return groupMemberList;
    }

    public void setGroupMemberList(List<GroupMemberEntity> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

    public void addGroupMemberList(List<GroupMemberEntity> groupMemberList) {
        if(groupMemberList == null || groupMemberList.isEmpty())
            return;

        if(this.groupMemberList == null)
            this.groupMemberList = new ArrayList<>();

        //去重
        List<GroupMemberEntity> delList = new ArrayList<>();
        for(GroupMemberEntity member : groupMemberList){
            for(int i=0, size =this.groupMemberList.size(); i<size; i++){
                GroupMemberEntity m = this.groupMemberList.get(i);
                if(m.getGroupId() == member.getGroupId() && m.getPeerId() == member.getPeerId()){
                    delList.add(m);
                    break;
                }
            }
        }

        if(!delList.isEmpty()) {
            this.groupMemberList.removeAll(delList);
        }

        this.groupMemberList.addAll(this.groupMemberList.size(), groupMemberList);
    }

    /**
     * 标记删除 不能做物理删除，否则群消息bug
     * @param groupMemberList
     * @return
     */
    public List<GroupMemberEntity> delGroupMemberList(int groupId, List<Integer> groupMemberList) {
        if(groupMemberList == null || groupMemberList.isEmpty())
            return null;

        List<GroupMemberEntity> delMemberEntityList = new ArrayList<>();

        for(int peerId : groupMemberList) {
            for (GroupMemberEntity member : this.groupMemberList) {
                if (groupId == member.getGroupId() && peerId == member.getPeerId()) {
                    member.setStatus(-1);
                    delMemberEntityList.add(member);
                    break;
                }
            }
        }

        return delMemberEntityList;
    }
}
