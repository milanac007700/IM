package com.milanac007.demo.im.db.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.DataBaseHelper;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.BuddyVerifyMessage;
import com.milanac007.demo.im.event.BuddyVerifyEvent;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.db.manager.IMSocketManager.TIMEOUT_MILLISECONDS;

/**
 * 负责用户信息的请求
 * 为回话页面以及联系人页面提供服务
 *
 * 联系人信息管理
 * 普通用户的version  有总版本
 * 群组没有总version的概念， 每个群有version
 * 具体请参见 服务端具体的pd协议
 */
public class IMContactManager extends IMManager {
    private Logger logger = Logger.getLogger();
    private DataBaseHelper mDbHelper;

    // 单例
    private static IMContactManager inst = new IMContactManager();
    public static IMContactManager instance() {
            return inst;
    }
    private IMSocketManager imSocketManager = IMSocketManager.instance();

    public IMContactManager() {
        mDbHelper = DataBaseHelper.getHelper(App.getContext());
    }

    // 自身状态字段
    private boolean  userDataReady = false;
    private Map<Integer, UserEntity> userMap = new ConcurrentHashMap<>();

    @Override
    public void doOnStart() {
    }

    public void onNormalLoginOk(){
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 本地DB加载
     */
    public void onLocalLoginOk() {
        List<UserEntity> userlist = UserEntity.getAllContacts();
        for(UserEntity userInfo:userlist){
            userMap.put(userInfo.getPeerId(),userInfo);
            if(userInfo.getPeerId() == IMLoginManager.instance().getLoginId()){
                IMLoginManager.instance().setLoginInfo(userInfo);
            }
        }

        triggerEvent(new UserInfoEvent(UserInfoEvent.Event.USER_INFO_OK));
    }

    public void reqGetAllUsers() {
        imSocketManager.sendMsg(IMBaseDefine.AllUserList, "", new Packetlistener(TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    JSONArray userListJSONArray = rspObject.getJSONArray("userList");
                    UserEntity.insertOrUpdateMultiData(userListJSONArray);
                    List<UserEntity> userlist = UserEntity.getAllContacts();
                    ArrayList<UserEntity> needDb = new ArrayList<>();
                    for(UserEntity userInfo:userlist){
                        if( userInfo.getAction() == 0 ||  userInfo.getAction() == 2) { //好友、被对方删除
                            userMap.put(userInfo.getPeerId(), userInfo);
                            needDb.add(userInfo);
                        }else if( userInfo.getAction() == 1){ //主动删除
                            //后续可能还需要该对象在UI上显示，故这里不做物理删除
                            userInfo.setFriend(false);
                            userMap.put(userInfo.getPeerId(), userInfo);
                            needDb.add(userInfo);
                        }

                        if(userInfo.getPeerId() == IMLoginManager.instance().getLoginId()){
                            IMLoginManager.instance().setLoginInfo(userInfo);
                            updateCurrentLoginUser(userInfo);
                        }
                    }

                    triggerEvent(new UserInfoEvent(UserInfoEvent.Event.USER_INFO_UPDATE, needDb));
                }
            }

            @Override
            public void onTimeout() {

            }

            @Override
            public void onFail(String error) {

            }
        });
    }

    /**
     * 网络加载
     */
    public void onLocalNetOk() {
        reqGetAllUsers();
    }

    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
    }


    public void triggerEvent(UserInfoEvent event) {
        //先更新自身的状态
        switch (event.event){
            case USER_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    public void putContact(UserEntity buddy){
        if(buddy == null)
            return ;

        userMap.put(buddy.getPeerId(), buddy);
    }

    public UserEntity findContact(int buddyId){
        if(buddyId > 0 && userMap.containsKey(buddyId)){
            return userMap.get(buddyId);
        }
        return null;
    }

    public UserEntity findContactByUserCode(String userCode){
        Collection<UserEntity> userEntities = userMap.values();
        for(UserEntity userEntity : userEntities){
            if(userEntity.getUserCode().equals(userCode))
                return userEntity;
        }
        return null;
    }

    public UserEntity findContactByPhone(String phone){
        Collection<UserEntity> userEntities = userMap.values();
        for(UserEntity userEntity : userEntities){
            if(userEntity.getPhone().equals(phone))
                return userEntity;
        }
        return null;
    }

    /**
     * 请求用户详细信息
     * @param userIds
     */
    public void reqGetDetaillUsers(List<Integer> userIds, final Packetlistener callback){
        logger.i("contact#contact#reqGetDetaillUsers");
        if(null == userIds || userIds.size() <= 0){
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        JSONObject param = new JSONObject();
        param.put("UserId", loginId);
        param.put("UserIdList", userIds);

        if(callback != null){
            imSocketManager.sendMsg(IMBaseDefine.BuddyListUserInfo, param.toJSONString(), callback);
        }
    }

    public void reqGetDetaillUsers(List<Integer> userIds) {
        logger.i("contact#contact#reqGetDetaillUsers");
        if(null == userIds || userIds.size() <=0){
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        JSONObject param = new JSONObject();
        param.put("UserId", loginId);
        param.put("UserIdList", userIds);
        imSocketManager.sendMsg(IMBaseDefine.BuddyListUserInfo, param.toJSONString(), new Packetlistener(TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    int loginId = rspObject.getIntValue("UserId");
                    boolean needEvent = false;

                    JSONArray userInfoListJSONArray = rspObject.getJSONArray("UserInfoList");
                    ArrayList<UserEntity>  dbNeed = new ArrayList<>();
                    for (int i=0; i<userInfoListJSONArray.size(); i++) {
                        JSONObject userInfoJsonObject = userInfoListJSONArray.getJSONObject(i);
                        Gson gson = new Gson();
                        UserEntity userEntity = gson.fromJson(userInfoJsonObject.toJSONString(), UserEntity.class);
                        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                        userEntity.setPinyinName(userEntity.getPinyinElement().pinyin);

                        int userId = userEntity.getPeerId();
                        if (userMap.containsKey(userId) && userMap.get(userId).equals(userEntity)) {
                            //没有必要通知更新
                        } else {
                            needEvent = true;
                            userMap.put(userEntity.getPeerId(), userEntity);
                            dbNeed.add(userEntity);
                            if (userEntity.getPeerId() == loginId) {
                                IMLoginManager.instance().setLoginInfo(userEntity);
                            }
                        }
                    }

                    // 负责userMap
                    UserEntity.insertOrUpdateMultiData(dbNeed);// TODO

                    // 判断有没有必要进行推送
                    if(needEvent){
                        triggerEvent(new UserInfoEvent(UserInfoEvent.Event.USER_INFO_UPDATE, dbNeed));
                    }
                }
            }

            @Override
            public void onTimeout() {
                //TODO
            }

            @Override
            public void onFail(String error) {
                //TODO
            }
        });
    }


    private void updateCurrentLoginUser(UserEntity entity){
        User newUser = new User();
        newUser.setUuid(entity.getPeerId());
        newUser.setUserCode(entity.getUserCode());
        newUser.setName(entity.getMainName());
        newUser.setTelephone(entity.getPhone());
        newUser.setEmailAddress(entity.getEmail());
        newUser.setLastloginTime(System.currentTimeMillis());
        newUser.setHeadIcoLocalPath(entity.getAvatarLocalPath());
        User.insertOrUpdateSingleData(newUser);

        User lastUser = User.getUserLastLogin();
        User.setImConfigUser(lastUser);
    }


    /**
     * 修改好友备注名称,自己的昵称，自己的手机号
     * @param changeUserId 更改谁
     */
//    public void ReqChangeBuddyInfo(int changeUserId, IMBaseDefine.PreferenceType type, String value, Packetlistener callback){
//
//    }

    //修改当前用户头像req
//    public void ReqModifyAvatar(String avatarUrl, Packetlistener callback){
//
//    }

    //查找buddy req
    public void ReqSearchBuddy(String searchStr, final Packetlistener callback) {
        int loginId = IMLoginManager.instance().getLoginId();
        JSONObject param = new JSONObject();
        param.put("UserId", loginId);
        param.put("SearchInfo", searchStr);
        imSocketManager.sendMsg(IMBaseDefine.ReqSearchBuddy, param.toJSONString(), callback);
    }

    public int onRepSearchBuddy(final JSONObject rspObject) {
        int resultCode = rspObject.getIntValue("resultCode");
        if(resultCode != 0) {
            return-1;
        }else {
            int loginId = rspObject.getIntValue("UserId");
            ArrayList<UserEntity>  dbNeed = new ArrayList<>();

            JSONObject userInfoJsonObject = rspObject.getJSONObject("UserInfo");
            Gson gson = new Gson();
            UserEntity userEntity = gson.fromJson(userInfoJsonObject.toJSONString(), UserEntity.class);
            PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
            userEntity.setPinyinName(userEntity.getPinyinElement().pinyin);

            int userId = userEntity.getPeerId();

            //TODO 有的人不是好友，还需要后续处理
            if (userMap.containsKey(userId)) {
                //没有必要通知更新
                userEntity.setFriend(true);
                if (userEntity.getPeerId() == loginId) {
                    IMLoginManager.instance().setLoginInfo(userEntity);
                    updateCurrentLoginUser(userEntity);
                }
                if(!Objects.equals(userMap.get(userId), userEntity)){
                    dbNeed.add(userEntity);
                }
            } else {
                userEntity.setFriend(false);
                userMap.put(userEntity.getPeerId(), userEntity);
                dbNeed.add(userEntity);
            }

            UserEntity.insertOrUpdateMultiData(dbNeed);
            return userId;
        }
    }

    /**
     * 添加buddy req
     * @param buddyid 添加谁
     * @param verifyStr 验证消息
     * @param callback
     */
    public void ReqAddBuddy(int buddyid, String verifyStr, final Packetlistener callback){
        UserEntity loginer = IMLoginManager.instance().getLoginInfo();
        JSONObject req = new JSONObject();
        req.put("UserId", loginer.getPeerId());
        req.put("UserName", loginer.getMainName());
        req.put("AddUserId", buddyid);
        req.put("AddReqInfo", verifyStr);
        IMSocketManager.instance().sendMsg(IMBaseDefine.ReqAddBuddy, req.toJSONString(), callback);
    }

    /**
     * 添加buddy rsp
     * @param imAddBuddyRsp
     * @return 0:success 1: failed
     */
    public int onRepAddBuddy(JSONObject imAddBuddyRsp) {
        int resultCode = imAddBuddyRsp.getIntValue("resultCode");
        if(resultCode != 0) {
            return-1;
        }else {
            BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();
            verifyMessage.setSessionId(imAddBuddyRsp.getIntValue("AddUserId"));
            verifyMessage.setFromId(imAddBuddyRsp.getIntValue("UserId"));
            verifyMessage.setToId(imAddBuddyRsp.getIntValue("AddUserId"));
            verifyMessage.setContent(imAddBuddyRsp.getString("AddReqInfo"));
            verifyMessage.setMsgType(DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST);
            verifyMessage.setCreated(System.currentTimeMillis());
            // 负责userMap
            BuddyVerifyMessage.insertOrUpdateBuddyVerifyMsg(verifyMessage);
            return 0;
        }
    }

    /**
     * message IMAddBuddyNotify{
     //service id:		0x0002
     //command id:		0x0218
     uint32 user_id;//谁添加
     uint32 add_user_id;//添加谁
     string add_req_info;//添加好友的请求说明
     }
     * @param notifyStr
     * @return
     */
    public void onNotifyAddBuddy(String notifyStr){
        JSONObject imAddBuddyNotify = JSONObject.parseObject(notifyStr);

        BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();
        int loginId = IMLoginManager.instance().getLoginId();
        if(loginId == imAddBuddyNotify.getIntValue("UserId")){ //其他终端处理后的notify
            verifyMessage.setSessionId(imAddBuddyNotify.getIntValue("AddUserId"));
        }else {
            verifyMessage.setSessionId(imAddBuddyNotify.getIntValue("UserId"));
        }

        long timeNow = System.currentTimeMillis();

        verifyMessage.setFromId(imAddBuddyNotify.getIntValue("UserId"));
        verifyMessage.setToId(imAddBuddyNotify.getIntValue("AddUserId"));
        verifyMessage.setContent(imAddBuddyNotify.getString("AddReqInfo"));
        verifyMessage.setMsgType(DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST);
        verifyMessage.setCreated(timeNow);
        logger.i("onNotifyAddBuddy: %s", verifyMessage.toString());
        // 负责userMap
        BuddyVerifyMessage.insertOrUpdateBuddyVerifyMsg(verifyMessage);

        //TODO 放入usermap和db 后续使用，真正成为好友后 从server获取真实数据替换
        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(0);
        userEntity.setAvatar(imAddBuddyNotify.getString("UserAvatal"));
        userEntity.setCreated(timeNow);
        userEntity.setGender(0);
        String userName = imAddBuddyNotify.getString("UserName");
        userEntity.setMainName(userName);
        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        userEntity.setPinyinName(userEntity.getPinyinElement().pinyin);

        if(CommonFunction.isIMAccount(userName)) {
            userEntity.setUserCode(userName);
        }
        userEntity.setUpdated(timeNow);
        userEntity.setPeerId(imAddBuddyNotify.getIntValue("UserId"));
        userEntity.setFriend(false);
        userEntity.setFake(true);
        userMap.put(userEntity.getPeerId(), userEntity);
        UserEntity.insertOrUpdateSingleData(userEntity);

//        List<Integer> needFetchUserIds = new ArrayList<>();
//        needFetchUserIds.add(imAddBuddyNotify.getUserId());
//        reqGetDetaillUsers(needFetchUserIds, new Packetlistener() {
//            @Override
//            public void onSuccess(Object response) {
//
//            }
//
//            @Override
//            public void onFaild() {
//
//            }
//
//            @Override
//            public void onTimeout() {
//
//            }
//        });

        //通知
        EventBus.getDefault().post(new BuddyVerifyEvent(BuddyVerifyEvent.ADD_BUDDY_REQUEST, verifyMessage.getSessionId()));
    }


    /**
     * 同意添加buddy req
     * @param buddyid 确认谁
     * IM.BaseDefine.ConfirmType confirm_type;//确认类型
     * @param verifyStr  好友添加请求的回复内容
     * @param callback
     */
    public void ReqAddBuddyAccept(int buddyid, String verifyStr, final Packetlistener callback){
        int loginId = IMLoginManager.instance().getLoginId();
        JSONObject req = new JSONObject();
        req.put("UserId", loginId ); //请求者
        req.put("ConfirmUserId", buddyid); //同意确认者
        req.put("ConfirmRsqInfo", verifyStr);
        IMSocketManager.instance().sendMsg(IMBaseDefine.ConfirmAddBuddy, req.toJSONString(), callback);
    }

    /**
     * 添加buddy rsp
     * @param imConformAddBuddyRsq
     * @return 0:success 1: failed
     */
    public int onRepAddBuddyAccept(JSONObject imConformAddBuddyRsq){
        int resultCode = imConformAddBuddyRsq.getIntValue("resultCode");
        if(resultCode != 0) {
            return-1;
        }else {
            BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();
            verifyMessage.setSessionId(imConformAddBuddyRsq.getIntValue("ConfirmUserId"));
            verifyMessage.setFromId(imConformAddBuddyRsq.getIntValue("UserId"));
            verifyMessage.setToId(imConformAddBuddyRsq.getIntValue("ConfirmUserId"));
            verifyMessage.setContent(imConformAddBuddyRsq.getString("ConfirmRsqInfo"));
            verifyMessage.setMsgType(DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT);
            verifyMessage.setCreated(System.currentTimeMillis());
            // 负责userMap
            BuddyVerifyMessage.insertOrUpdateBuddyVerifyMsg(verifyMessage);
            return 0;
        }
    }

    /**
     * message IMConformAddBuddyNotify{
     //service id:		0x0002
     //command id:		0x021b
     uint32 user_id;//谁确认
     uint32 confirm_user_id;//确认谁
     IM.BaseDefine.ConfirmType confirm_type;//确认类型
     string confirm_rsp_info;//好友添加请求的回复内容
     }
     * @param notifyStr
     */
    public void onNotifyAddBuddyAccept(String notifyStr){
        JSONObject imConformAddBuddyNotify = JSONObject.parseObject(notifyStr);

        BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();

        int loginId = IMLoginManager.instance().getLoginId();
        if(loginId == imConformAddBuddyNotify.getIntValue("UserId")){ //其他终端处理后的notify
            verifyMessage.setSessionId(imConformAddBuddyNotify.getIntValue("ConfirmUserId"));
        }else {
            verifyMessage.setSessionId(imConformAddBuddyNotify.getIntValue("UserId"));
        }
        verifyMessage.setFromId(imConformAddBuddyNotify.getIntValue("UserId"));
        verifyMessage.setToId(imConformAddBuddyNotify.getIntValue("ConfirmUserId"));
        verifyMessage.setContent(imConformAddBuddyNotify.getString("ConfirmRsqInfo"));
        verifyMessage.setMsgType(DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT);
        verifyMessage.setCreated(System.currentTimeMillis());
        logger.i("onNotifyAddBuddyAccept: %s", verifyMessage.toString());
        // 负责userMap
        verifyMessage.insertOrUpdateBuddyVerifyMsg(verifyMessage);

        //通知
        EventBus.getDefault().post(new BuddyVerifyEvent(BuddyVerifyEvent.ADD_BUDDY_ACCEPT, verifyMessage.getSessionId()));
    }


    /**
     * 删除buddy req
     * @param buddyid 删除谁
     * @param delReqStr 删除好友的请求说明，可选
     * @param callback
     */
//    public void ReqDelBuddy(int buddyid, String delReqStr, final Packetlistener callback){
//        int loginId = IMLoginManager.instance().getLoginId();
//        IMBuddy.IMDelBuddyReq imAddBuddyReq = IMBuddy.IMDelBuddyReq.newBuilder()
//                .setUserId(loginId)
//                .setDelUserId(buddyid)
//                .setDelReqInfo(delReqStr)
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
//        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_BUDDY_DEL_REQUEST_VALUE;
//        IMSocketManager.instance().sendRequest(imAddBuddyReq, sid, cid, callback);
//    }
//
//
//    /**
//     * 删除buddy rsp
//     * @param imDelBuddyRsp
//     * @return 0:success 1: failed
//     */
//    public int onRepDelBuddy(IMBuddy.IMDelBuddyRsp imDelBuddyRsp){
//        int resultCode = imDelBuddyRsp.getResultCode(); //结果码，0:successed 1:failed
//        if(resultCode == 0){
//            BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();
//            verifyMessage.setSessionId(imDelBuddyRsp.getDelUserId());
//            verifyMessage.setFromId(imDelBuddyRsp.getUserId());
//            verifyMessage.setToId(imDelBuddyRsp.getDelUserId());
//            verifyMessage.setContent(imDelBuddyRsp.getDelReqInfo());
//            verifyMessage.setMsgType(DBConstant.MSG_TYPE_DELBUDDY_REQUEST);
//            verifyMessage.setCreated(System.currentTimeMillis());
//            // 负责userMap
//            dbInterface.insertOrUpdateBuddyVerifyMsg(verifyMessage);
//            return 0;
//        }else {
//            //TODO
//            return -1;
//        }
//    }
//
//    /**
//     * IMDelBuddyNotify{
//     //service id:		0x0002
//     //command id:		0x021e
//     uint32 user_id;//谁删除
//     uint32 del_user_id;//删除谁
//     string del_req_info;//删除好友的请求说明
//     }
//     * @param imDelBuddyNotify
//     * @return
//     */
//    public void onNotifyDelBuddy(IMBuddy.IMDelBuddyNotify imDelBuddyNotify){
//        if(findContact(imDelBuddyNotify.getUserId()) == null) //己方删除对方后，不需要再接收对方删除自己的通知
//            return;
//
//        BuddyVerifyMessage verifyMessage = new BuddyVerifyMessage();
//        int loginId = IMLoginManager.instance().getLoginId();
//        if(loginId == imDelBuddyNotify.getUserId()){ //其他终端处理后的notify
//            verifyMessage.setSessionId(imDelBuddyNotify.getDelUserId());
//        }else {
//            verifyMessage.setSessionId(imDelBuddyNotify.getUserId());
//        }
//
//        verifyMessage.setFromId(imDelBuddyNotify.getUserId());
//        verifyMessage.setToId(imDelBuddyNotify.getDelUserId());
//        verifyMessage.setContent(imDelBuddyNotify.getDelReqInfo());
//        verifyMessage.setMsgType(DBConstant.MSG_TYPE_DELBUDDY_REQUEST);
//        verifyMessage.setCreated(System.currentTimeMillis());
//        logger.i("onNotifyDelBuddy: %s", verifyMessage.toString());
//        // update db
//        dbInterface.insertOrUpdateBuddyVerifyMsg(verifyMessage);
//
//        //通知
//        EventBus.getDefault().post(new BuddyVerifyEvent(BuddyVerifyEvent.DELBUDDY_REQUEST, verifyMessage.getSessionId()));
//    }



    /**
     *
     * @return 好友列表
     */
    public  List<UserEntity> getContactSortedList() {
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Iterator<UserEntity> iterator = contactList.iterator();
        while (iterator.hasNext()){
            if(!iterator.next().isFriend()){
                iterator.remove();
            }
        }

        Collections.sort(contactList, new Comparator<UserEntity>() {
            public int compare(UserEntity o1, UserEntity o2) {
                if (o2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (o1.getPinyinElement().pinyin.startsWith("#")) {
                    return 1;
                } else {
                    return o1.getPinyinName().compareToIgnoreCase(o2.getPinyinName()); //字母升序排列
                }
            }
        });

        return contactList;
    }

    /**
     *
     * @return 好友列表, 排除自己
     */
    public  List<UserEntity> getContactSortedListExecuteMe() {

        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Iterator<UserEntity> iterator = contactList.iterator();
        while (iterator.hasNext()){
            UserEntity userEntity = iterator.next();
            if(!userEntity.isFriend() || userEntity.getPeerId() == IMLoginManager.instance().getLoginId()){
                iterator.remove();
            }
        }

        Collections.sort(contactList, new Comparator<UserEntity>(){
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if(entity1.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity1.getMainName(),entity1.getPinyinElement());
                    }
                    if(entity2.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }

    // 确实要将对比的抽离出来 Collections
    public  List<UserEntity> getSearchContactList(String key){
        List<UserEntity> searchList = new ArrayList<>();
        for(Map.Entry<Integer,UserEntity> entry:userMap.entrySet()){
            UserEntity user = entry.getValue();
//            if (IMUIHelper.handleContactSearch(key, user)) {
//                searchList.add(user);
//            }
        }
        return searchList;
    }

//    public void BatchCheckUserIfExistByPhone(List<String> phoneList, final Packetlistener callback){}


    /**-----------------------实体 get set 定义-----------------------------------*/

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }


}
