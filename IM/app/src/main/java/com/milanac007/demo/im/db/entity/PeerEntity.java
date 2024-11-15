package com.milanac007.demo.im.db.entity;

import android.content.Context;

import com.j256.ormlite.field.DatabaseField;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.utils.pinyin.PinYin;

/**
 *  聊天对象抽象类  may be user/group / groupmember
 *
 * useGetSet:指定ormlite访问变量使用set,get方法, 默认使用的是反射机制直接访问变量
 *
 * create(T data): Create a new entry in the database from an object. Should return 1 indicating 1 row was inserted.
 *
 * createIfNotExists(T data): This is a convenience method to creating a data item but only if the ID does not already exist in the table.
 * This extracts the id from the data parameter, does a query for on it, returning the data if it exists.
 * If it does not exist then create is called with the data parameter.
 *
 * createOrUpdate(T data):  This is a convenience method for creating an item in the database if it does not exist.
 * The id is extracted from the data argument and a query-by-id is made on the database. If a row in the database
 * with the same id exists then all of the columns in the database will be updated from the fields in the data
 * parameter. If the id is null (or 0 or some other default value) or doesn’t exist in the database then the object
 * will be created in the database. This also means that your data item must have an id field defined.
 *
 * update(T data): Save the fields from an object to the database. If you have made changes to an object,
 * this is how you persist those changes to the database. You cannot use this method to update the id field –
 * see updateId(). This should return 1 since 1 row was updated.
 *
 * 2.11.2 Issuing Raw Update Statements
 * You can also issue raw update statements against the database if the DAO functionality does not give you enough flexibility.
 * Update SQL statements must contain the reserved words INSERT, DELETE, or UPDATE. For example:
 *
 * fooDao.updateRaw("INSERT INTO accountlog (account_id, total) "
 *    + "VALUES ((SELECT account_id,sum(amount) FROM accounts))
 *
 */
public abstract class PeerEntity {
    @DatabaseField(generatedId = true) //指定字段为自增主键, 唯一
    protected long id;

    @DatabaseField
    protected int peerId;

    @DatabaseField
    protected String mainName; //自己的name / 群名称 / 群成员名称

    @DatabaseField
    protected String pinyinName;

    @DatabaseField
    protected long created; //创建时间

    @DatabaseField
    protected long updated; //最后更新时间

    @DatabaseField
    protected String avatar; //头像url

    @DatabaseField
    protected String avatarLocalPath; //头像本地路径

    protected static Context context;

    protected PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();//mainName pinyin
    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }
    public void setPinyinElement(PinYin.PinYinElement pinyinElement) {
        this.pinyinElement = pinyinElement;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatarLocalPath() {
        return avatarLocalPath;
    }

    public void setAvatarLocalPath(String avatarLocalPath) {
        this.avatarLocalPath = avatarLocalPath;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getPinyinName() {
        return pinyinName;
    }

    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public abstract int getType();

    // peer就能生成sessionKey
    public String getSessionKey(){
        return EntityChangeEngine.getSessionKey(peerId,getType());
    }

    public static void checkValid() {
        if(context == null) {
            context = App.getContext();
        }
    }
}
