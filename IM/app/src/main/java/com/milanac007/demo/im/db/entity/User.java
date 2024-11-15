package com.milanac007.demo.im.db.entity;

/**
 * Created by milanac007 on 2016/11/23.
 */

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.utils.Preferences;

import java.sql.SQLException;
import java.util.List;

/*******************登录用户，存取DB**********************/
@DatabaseTable(tableName = "user")
public class User {

    @DatabaseField(id=true)
    private int uuid;

    @DatabaseField
    private String userCode; //账号
    
    @DatabaseField
    private String telephone;

    @DatabaseField
    private String name; //名字

    @DatabaseField
    private int gender; //性别 0：男  1：女 -1: unknown

    @DatabaseField
    private String emailAddress;
    
    @DatabaseField
    private String userIconUrl;

    @DatabaseField
    private String headIcoLocalPath;
    
    @DatabaseField
    private long lastloginTime; //最后登录时间

    @DatabaseField
    private String sha256Pwd;

    private static Context context;
    
    public User(){

    }

    public User(int uuid){
        this();
        this.uuid = uuid;
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUserIconUrl() {
        return userIconUrl;
    }

    public void setUserIconUrl(String userIconUrl) {
        this.userIconUrl = userIconUrl;
    }

    public String getHeadIcoLocalPath() {
        return headIcoLocalPath;
    }

    public void setHeadIcoLocalPath(String headIcoLocalPath) {
        this.headIcoLocalPath = headIcoLocalPath;
    }

    public long getLastloginTime() {
        return lastloginTime;
    }

    public void setLastloginTime(long lastloginTime) {
        this.lastloginTime = lastloginTime;
    }

    public String getSha256Pwd() {
        return sha256Pwd;
    }

    public void setSha256Pwd(String sha256Pwd) {
        this.sha256Pwd = sha256Pwd;
    }

    public static void checkValid() {
        if(context == null) {
            context = App.getContext();
        }
    }

    public static void insertOrUpdateSingleData(User field){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<User, String> dao = helper.getDao(User.class);
            int uuid = field.getUuid();
            QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("uuid", uuid);
            List<User> dataField = queryBuilder.query();
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
     * 更新某一个字段
     * @param columnName
     * @param value
     */
    public static void updateSingleColumn(String columnName, String value){
        checkValid();
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<User, String> dao = helper.getDao(User.class);
            UpdateBuilder<User, String> updateBuilder = dao.updateBuilder();
            updateBuilder.where().eq("uuid", Preferences.getCurrentLoginer().uuid);
            updateBuilder.updateColumnValue(columnName, value);
            updateBuilder.update();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最后登录的用户
     * @return
     */
    public static User getUserLastLogin(){
        checkValid();
        List<User> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<User, String> dao = helper.getDao(User.class);
            QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
//            queryBuilder.orderBy("mlastloginTime", false).limit((long)1);
            queryBuilder.orderBy("lastloginTime", false);
            dataField = queryBuilder.query();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(dataField.size() > 0){
            return dataField.get(0);
        }else {
            return null;
        }

    }

    /** @DatabaseField
     * 获取制定id的用户
     * @return
     */
    public static User getUserByUuid(int uuid){
        checkValid();
        List<User> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<User, String> dao = helper.getDao(User.class);
            QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("uuid", uuid);
            dataField = queryBuilder.query();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(dataField.size() > 0){
            return dataField.get(0);
        }else {
            return null;
        }
    }

    /** @DatabaseField
     * 获取用户 模糊查找
     * @return
     */
    public static User getUserByInput(String input){
        checkValid();
        List<User> dataField = null;
        final DataBaseHelper helper = DataBaseHelper.getHelper(context);
        try {
            Dao<User, String> dao = helper.getDao(User.class);
            QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("userCode", input).or().eq("telephone", input).or().eq("emailAddress", input);
            dataField = queryBuilder.query();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(dataField.size() > 0){
            return dataField.get(0);
        }else {
            return null;
        }
    }

    public static void setImConfigUser(User user){
        if(user != null){
            Preferences.updateCurrentLoginer(user);
        }
    }
    
}
