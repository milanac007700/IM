package com.milanac007.demo.im.db.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.db.manager.IMSocketManager.TIMEOUT_MILLISECONDS;

public class IMGroupManager extends IMManager {
    private Logger logger = Logger.getLogger();

    // 单例
    private static IMGroupManager inst = new IMGroupManager();
    public static IMGroupManager instance() {
        return inst;
    }

    // 依赖的服务管理
    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager imLoginManager=IMLoginManager.instance();
    private IMContactManager contactManager = IMContactManager.instance();

    // todo Pinyin的处理
    //正式群,临时群都会有的，存在竞争 如果不同时请求的话
    private Map<Integer, GroupEntity> groupMap = new ConcurrentHashMap<>();
    // 群组状态
    private boolean isGroupReady = false;

    @Override
    public void doOnStart() {
        groupMap.clear();
    }

    public void onNormalLoginOk(){
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 1. 加载本地信息
     * 2. 请求正规群信息 ， 与本地进行对比
     * 3. version groupId 请求
     * */
    public void onLocalLoginOk(){
        logger.i("group#loadFromDb");

        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().registerSticky(inst);
        }

        // 加载本地group
        List<GroupEntity> localGroupInfoList = GroupEntity.loadAllGroupNormal();
        if (localGroupInfoList != null) {
            for (GroupEntity groupInfo : localGroupInfoList) {
                List<GroupMemberEntity> members = GroupMemberEntity.loadAllGroupMembersByGroupId(groupInfo.getPeerId());
                groupInfo.setGroupMemberList(members);

                groupMap.put(groupInfo.getPeerId(), groupInfo);
            }
        }

        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_OK));
    }

    public void onLocalNetOk() {
        reqGetNormalGroupList();
    }

    @Override
    public void reset() {
        isGroupReady =false;
        groupMap.clear();
        EventBus.getDefault().unregister(inst);
    }

    public void onEvent(GroupEvent event){
        switch (event.getEvent()){
            case GROUP_INFO_OK:{

            }break;
            case GROUP_INFO_UPDATED:{

            }break;
        }
    }

    /**
     * 实现自身的事件驱动
     * @param event
     */
    public  synchronized void triggerEvent(GroupEvent event) {
        switch (event.getEvent()){
            case GROUP_INFO_OK:
                isGroupReady = true;
                break;
            case GROUP_INFO_UPDATED:
                isGroupReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }


    /**
     * 联系人页面正式群的请求
     * todo 正式群与临时群逻辑上的分开的，但是底层应该是想通的
     */
    private void reqGetNormalGroupList() {
        logger.i("group#reqGetNormalGroupList");
        imSocketManager.sendMsg(IMBaseDefine.AllNormalGroupList, "", new Packetlistener(TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    JSONArray groupListJSONArray = rspObject.getJSONArray("groupList");
                    GroupEntity.insertOrUpdateMultiData(groupListJSONArray);

                    List<GroupEntity> localGroupInfoList = GroupEntity.loadAllGroupNormal();
                    if (localGroupInfoList != null) {
                        for (GroupEntity groupInfo : localGroupInfoList) {
                            List<GroupMemberEntity> members = GroupMemberEntity.loadAllGroupMembersByGroupId(groupInfo.getPeerId());
                            groupInfo.setGroupMemberList(members);
                            groupMap.put(groupInfo.getPeerId(), groupInfo);
                        }
                    }

                    triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
                }
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED_TIMEOUT));
            }

            @Override
            public void onFail(String error) {
                triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED_FAIL));
            }
        });
    }

    /**
     * 创建群
     */
    public void reqCreateGroup(String groupName, String groupAvatarUrl, List<Integer> memberList, Packetlistener packetlistener){
        logger.i("group#reqCreateGroup, groupName = %s", groupName);
        int loginId = imLoginManager.getLoginId();

        JSONObject reqCreateGroup = new JSONObject();
        reqCreateGroup.put("UserId", loginId);
        reqCreateGroup.put("GroupName", groupName);
        reqCreateGroup.put("GroupType",  DBConstant.GROUP_TYPE_NORMAL);
        reqCreateGroup.put("AllMemberIdList", memberList);
        reqCreateGroup.put("GroupAvatar", groupAvatarUrl);

        imSocketManager.sendMsg(IMBaseDefine.ReqCreateGroup, reqCreateGroup.toJSONString(), packetlistener);
    }

    public void onReqCreateGroup(JSONObject groupCreateRsp) {
        logger.d("group#onReqCreateGroup");
        int resultCode = groupCreateRsp.getIntValue("resultCode");
        int operatorId = groupCreateRsp.getIntValue("UserId");

        if(0 != resultCode){
            logger.e("group#createGroup failed");
            triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
            return;
        }

        JSONObject groupInfo = groupCreateRsp.getJSONObject("GroupInfo");
        Gson gson = new Gson();
        GroupEntity groupEntity = gson.fromJson(groupInfo.toJSONString(), GroupEntity.class);
        // 更新DB 更新map
        groupMap.put(groupEntity.getPeerId(),groupEntity);

        IMSessionManager.instance().updateSession(groupEntity, groupEntity.getGroupMemberList(), operatorId, "ADD");
        GroupMemberEntity.insertOrUpdateMultiData(groupEntity.getGroupMemberList());
        GroupEntity.insertOrUpdateSingleData(groupEntity);
        triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_OK, groupEntity)); // 接收到之后修改UI
    }


    public void onNotifyCreateGroup(String notifyStr){
        logger.d("group#onNotifyCreateGroup");
        JSONObject notify = JSONObject.parseObject(notifyStr);
        final int groupId = notify.getIntValue("GroupId");

        reqGroupDetailInfo(groupId, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    JSONObject groupInfoListRsp = rspObject.getJSONObject("GroupInfoListRsp");
                    IMGroupManager.instance().onRepGroupDetailInfo(groupInfoListRsp);
                    GroupEntity groupEntity = groupMap.get(groupId);
                    IMSessionManager.instance().updateSession(groupEntity, groupEntity.getGroupMemberList() , groupEntity.getCreatorId(),"ADD");
                    GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CREATE_GROUP_OK);// 接收到之后修改UI
                    groupEvent.setOperateId(groupEntity.getCreatorId());
                    groupEvent.setGroupId(groupEntity.getPeerId());
                    groupEvent.setGroupEntity(groupEntity);
                    triggerEvent(groupEvent);
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
     * 新增群成员
     * ADD_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqAddGroupMember(int groupId, List<Integer> addMemberlist){
        reqChangeGroupMember(groupId,DBConstant.GROUP_MODIFY_TYPE_ADD, addMemberlist);
    }

    /**
     * 删除群成员
     * REMOVE_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqRemoveGroupMember(int groupId,List<Integer> removeMemberlist){
        reqChangeGroupMember(groupId, DBConstant.GROUP_MODIFY_TYPE_DEL, removeMemberlist);
    }

    /**
     * 主动退群
     * @param groupId
     * @param leaveId
     */
    public void reqLeaveGroupMember(int groupId, Integer leaveId){
        List<Integer> leaveMemberList = new ArrayList<>();
        leaveMemberList.add(leaveId);
        reqChangeGroupMember(groupId, DBConstant.GROUP_MODIFY_TYPE_LEAVE, leaveMemberList);
    }

    private void reqChangeGroupMember(final int groupId, int groupModifyType, List<Integer> changeMemberlist) {
        logger.i("group#reqChangeGroupMember, changeGroupMemberType = %d", groupModifyType);

        final int loginId = imLoginManager.getLoginId();
        JSONObject groupChangeMemberReq = new JSONObject();
        groupChangeMemberReq.put("UserId", loginId);
        groupChangeMemberReq.put("ChangeType", groupModifyType);
        groupChangeMemberReq.put("ChangeMemberList", changeMemberlist);
        groupChangeMemberReq.put("GroupId", groupId);

        imSocketManager.sendMsg(IMBaseDefine.GroupChangeMember, groupChangeMemberReq.toJSONString(), new Packetlistener(TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    JSONObject groupChangeMemberRsp = rspObject.getJSONObject("groupChangeMemberRsp");
                    onReqChangeGroupMember(groupChangeMemberRsp);
                }
            }

            @Override
            public void onTimeout() {
                GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT);
                groupEvent.setGroupId(groupId);
                groupEvent.setOperateId(loginId);
                triggerEvent(groupEvent);
            }

            @Override
            public void onFail(String error) {
                GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL);
                groupEvent.setGroupId(groupId);
                groupEvent.setOperateId(loginId);
                triggerEvent(groupEvent);
            }
        });
    }

    public void onReqChangeGroupMember(JSONObject groupChangeMemberRsp) {
        int resultCode = groupChangeMemberRsp.getIntValue("resultCode");
        int operatorId = groupChangeMemberRsp.getIntValue("UserId");
        int groupId = groupChangeMemberRsp.getIntValue("GroupId");
        if (0 != resultCode) {
            final int loginId = imLoginManager.getLoginId();
            GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL);
            groupEvent.setGroupId(groupId);
            groupEvent.setOperateId(loginId);
            triggerEvent(groupEvent);
            return;
        }

        GroupEntity groupEntityRet = groupMap.get(groupId);
        List<Integer> changeUserIdList = groupChangeMemberRsp.getObject("ChgUserIdList", List.class);
        int type = groupChangeMemberRsp.getIntValue("ChangeType");
        List<GroupMemberEntity> changeMemberEntityList = new ArrayList<>();
        if (type == DBConstant.GROUP_MODIFY_TYPE_ADD) {

            for (Integer peerId : changeUserIdList) {
                GroupMemberEntity memberEntity = new GroupMemberEntity();
                memberEntity.setGroupId(groupId);
                memberEntity.setPeerId(peerId);
                memberEntity.setgKey(groupId + "_" + peerId);
                memberEntity.setStatus(0);
                memberEntity.setUpdated(System.currentTimeMillis());
                changeMemberEntityList.add(memberEntity);

                UserEntity contact = IMContactManager.instance().findContact(peerId);
                if (contact != null) {
                    memberEntity.setNickName(contact.getMainName());
                    memberEntity.setAvatar(contact.getAvatar());
                }else {
                    memberEntity.setNickName("");
                }
            }
            groupEntityRet.addGroupMemberList(changeMemberEntityList);
            IMSessionManager.instance().updateSession(groupEntityRet, changeMemberEntityList, operatorId, "ADD");

        } else if(type == DBConstant.GROUP_MODIFY_TYPE_DEL){
            changeMemberEntityList.addAll(groupEntityRet.delGroupMemberList(groupEntityRet.getPeerId(), changeUserIdList));
            IMSessionManager.instance().updateSession(groupEntityRet, changeMemberEntityList, operatorId, "DEL");

        }else if(type == DBConstant.GROUP_MODIFY_TYPE_LEAVE){
            IMSessionManager.instance().reqRemoveSession(groupEntityRet.getSessionKey());
            groupMap.remove(groupId);
            GroupEntity.delGroup(groupId);
            GroupMemberEntity.deleteGroupMemsByGroupId(groupId);

            GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.GROUP_INFO_DISBANDED);
            groupEvent.setGroupId(groupId);
            groupEvent.setOperateId(operatorId);
            triggerEvent(groupEvent);
            return;
        }

        List<Integer> curUserIdList = groupChangeMemberRsp.getObject("CurUserIdList", List.class);
        groupEntityRet.setlistGroupMemberIds(curUserIdList);

        groupMap.put(groupId, groupEntityRet);
        GroupEntity.insertOrUpdateSingleData(groupEntityRet);

        GroupMemberEntity.insertOrUpdateMultiData(groupEntityRet.getGroupMemberList());

        GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
        groupEvent.setGroupId(groupId);
        groupEvent.setOperateId(operatorId);
        groupEvent.setChangeType(type);
        groupEvent.setChangeList(changeUserIdList);
        groupEvent.setGroupEntity(groupEntityRet);
        triggerEvent(groupEvent);
    }

    /**
     * 收到群成员发生变更消息
     * 服务端主动发出
     * DB
     */
    public void onNotifyGroupChangeMember(String notifyStr) {
        JSONObject notify = JSONObject.parseObject(notifyStr);

        final int operatorId = notify.getIntValue("OperatorId");
        final int groupId = notify.getIntValue("GroupId");
        final int changeType = notify.getIntValue("ChangeType");

        final List<Integer> changeList = notify.getObject("ChgUserIdList", List.class);
        final List<Integer> curMemberList = notify.getObject("CurUserIdList", List.class);

        final List<GroupMemberEntity> changeMemberEntityList = new ArrayList<>();
        if(changeType == DBConstant.GROUP_MODIFY_TYPE_ADD){
            if(groupMap.containsKey(groupId)){
                final GroupEntity entity = groupMap.get(groupId);
                entity.setlistGroupMemberIds(curMemberList);

                ArrayList<Integer> needFetchUserIds = new ArrayList<>();
                for(Integer peerId : changeList){
                    GroupMemberEntity memberEntity = new GroupMemberEntity();
                    memberEntity.setGroupId(groupId);
                    memberEntity.setPeerId(peerId);
                    memberEntity.setgKey(groupId+"_"+peerId);
                    memberEntity.setStatus(0);
                    memberEntity.setUpdated(System.currentTimeMillis());
                    changeMemberEntityList.add(memberEntity);

                    UserEntity contact = IMContactManager.instance().findContact(peerId);
                    if(contact != null && !contact.isFake()){
                        memberEntity.setNickName(contact.getMainName());
                        memberEntity.setAvatar(contact.getAvatar());
                    }else {
                        memberEntity.setNickName("");

                        if (contact == null) {
                            contact = new UserEntity();
                            contact.setFake(true);
                            contact.setPeerId(peerId);
                            contact.setFriend(false);
                            contact.setMainName("");
                            contact.setPinyinName("");
                            IMContactManager.instance().putContact(contact);
                            UserEntity.insertOrUpdateSingleData(contact);
                        }
                        needFetchUserIds.add(peerId);
                    }
                }

                if(needFetchUserIds.size() > 0){
                    //TODO 联网获取 人的详情
                    contactManager.reqGetDetaillUsers(needFetchUserIds, new Packetlistener(TIMEOUT_MILLISECONDS) {
                        @Override
                        public void onSuccess(Object response) {
                            JSONObject rspObject = JSONObject.parseObject((String) response);
                            int resultCode = rspObject.getIntValue("resultCode");
                            if(resultCode != 0) {

                            }else {
                                int loginId = rspObject.getIntValue("UserId");
                                JSONArray userInfoListJSONArray = rspObject.getJSONArray("UserInfoList");
                                ArrayList<UserEntity>  dbNeed = new ArrayList<>();
                                for (int i=0; i<userInfoListJSONArray.size(); i++) {
                                    JSONObject userInfoJsonObject = userInfoListJSONArray.getJSONObject(i);
                                    Gson gson = new Gson();
                                    UserEntity userEntity = gson.fromJson(userInfoJsonObject.toJSONString(), UserEntity.class);
                                    PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                                    userEntity.setPinyinName(userEntity.getPinyinElement().pinyin);

                                    UserEntity contact = IMContactManager.instance().findContact(userEntity.getPeerId());
                                    if(contact != null){
                                        if(contact.isFake()){
                                            userEntity.setFriend(contact.isFriend());
                                        }else {
                                            userEntity.setFriend(true);
                                        }

                                        IMContactManager.instance().putContact(userEntity);
                                        dbNeed.add(userEntity);

                                        for(GroupMemberEntity member : changeMemberEntityList){
                                            if(member.getPeerId() == userEntity.getPeerId()){
                                                member.setNickName(!TextUtils.isEmpty(userEntity.getNickName()) ? userEntity.getNickName() : userEntity.getMainName());
                                                member.setAvatar(userEntity.getAvatar());
                                                break;
                                            }
                                        }

                                        entity.addGroupMemberList(changeMemberEntityList);
                                        groupMap.put(groupId,entity);
                                        GroupMemberEntity.insertOrUpdateMultiData(entity.getGroupMemberList());
                                        GroupEntity.insertOrUpdateSingleData(entity);
                                    }
                                }

                                // 负责userMap
                                UserEntity.insertOrUpdateMultiData(dbNeed);
                                IMSessionManager.instance().updateSession(entity, changeMemberEntityList, operatorId, "ADD");
                            }

                        }

                        @Override
                        public void onTimeout() {

                        }

                        @Override
                        public void onFail(String error) {

                        }
                    });
                }else {
                    entity.addGroupMemberList(changeMemberEntityList);
                    groupMap.put(groupId,entity);
                    GroupMemberEntity.insertOrUpdateMultiData(entity.getGroupMemberList());
                    GroupEntity.insertOrUpdateSingleData(entity);

                    IMSessionManager.instance().updateSession(entity, changeMemberEntityList, operatorId, "ADD");
                    GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
                    groupEvent.setGroupId(groupId);
                    groupEvent.setOperateId(operatorId);
                    groupEvent.setChangeType(changeType);
                    groupEvent.setChangeList(changeList);
                    groupEvent.setGroupEntity(entity);
                    triggerEvent(groupEvent);
                }

            }else{
                //TODO 自己被添加到了一个新群里 需:
                // 1.先获取群详情
                //2. 创建session会话
                //3. 通知UI
                reqGroupDetailInfo(groupId, new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        JSONObject rspObject = JSONObject.parseObject((String) response);
                        int resultCode = rspObject.getIntValue("resultCode");
                        if(resultCode != 0) {

                        }else {
                            int loginId = rspObject.getIntValue("UserId");
                            JSONObject groupInfoListRsp = rspObject.getJSONObject("GroupInfoListRsp");
                            IMGroupManager.instance().onRepGroupDetailInfo(groupInfoListRsp);
                            GroupEntity groupEntityRet = groupMap.get(groupId);
                            List<GroupMemberEntity> changeMemList = new ArrayList<>();
                            for(Integer changedId : changeList){
                                for(GroupMemberEntity member : groupEntityRet.getGroupMemberList()){
                                    if(changedId == member.getPeerId()){
                                        changeMemList.add(member);
                                        break;
                                    }
                                }
                            }

                            IMSessionManager.instance().updateSession(groupEntityRet, changeMemList, operatorId, "ADD");

                            GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS, groupEntityRet);
                            groupEvent.setGroupId(groupId);
                            groupEvent.setOperateId(operatorId);
                            groupEvent.setChangeType(changeType);
                            groupEvent.setChangeList(changeList);
                            triggerEvent(groupEvent);
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

        }else if(changeType == DBConstant.GROUP_MODIFY_TYPE_DEL){//del

            if(groupMap.containsKey(groupId)){
                GroupEntity entity = groupMap.get(groupId);
                entity.setlistGroupMemberIds(curMemberList);
                changeMemberEntityList.addAll(entity.delGroupMemberList(groupId, changeList));

                groupMap.put(groupId,entity);
                GroupEntity.insertOrUpdateSingleData(entity);

                GroupMemberEntity.insertOrUpdateMultiData(entity.getGroupMemberList());

                IMSessionManager.instance().updateSession(entity, changeMemberEntityList, operatorId, "DEL");

                GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
                groupEvent.setGroupId(groupId);
                groupEvent.setOperateId(operatorId);
                groupEvent.setChangeType(changeType);
                groupEvent.setChangeList(changeList);
                groupEvent.setGroupEntity(entity);
                triggerEvent(groupEvent);
            }
        } else if(changeType == DBConstant.GROUP_MODIFY_TYPE_LEAVE){

            GroupEntity entity = groupMap.get(groupId);
            final int loginId = imLoginManager.getLoginId();

            if(operatorId == loginId){
                IMSessionManager.instance().reqRemoveSession(entity.getSessionKey());
                groupMap.remove(groupId);
                GroupEntity.delGroup(groupId);
                GroupMemberEntity.deleteGroupMemsByGroupId(groupId);

                GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.GROUP_INFO_DISBANDED);
                groupEvent.setGroupId(groupId);
                groupEvent.setOperateId(operatorId);
                groupEvent.setChangeType(changeType);
                triggerEvent(groupEvent);
                return;

            }else {
                entity.setlistGroupMemberIds(curMemberList);
                changeMemberEntityList.addAll(entity.delGroupMemberList(groupId, changeList));
                groupMap.put(groupId,entity);
                GroupMemberEntity.insertOrUpdateMultiData(entity.getGroupMemberList());
                GroupEntity.insertOrUpdateSingleData(entity);

                IMSessionManager.instance().updateSession(entity, changeMemberEntityList, operatorId, "EXIT");

                GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
                groupEvent.setGroupId(groupId);
                groupEvent.setOperateId(operatorId);
                groupEvent.setChangeList(changeList);
                groupEvent.setChangeType(changeType);
                groupEvent.setGroupEntity(entity);
                triggerEvent(groupEvent);
            }

        }
    }

    public void reqGroupDetailInfo(int groupId, final Packetlistener callback){
        int loginId = IMLoginManager.instance().getLoginId();
        JSONObject param = new JSONObject();
        param.put("UserId", loginId);
        param.put("GroupId", groupId);
        param.put("Version", 0);

        if(callback != null){
            imSocketManager.sendMsg(IMBaseDefine.GetGroupDetailInfo, param.toJSONString(), callback);
        }
    }


    public void onRepGroupDetailInfo(JSONObject groupInfoListRsp) {
        int resultCode = groupInfoListRsp.getIntValue("resultCode");
        int userId = groupInfoListRsp.getIntValue("UserId");
        int groupId = groupInfoListRsp.getIntValue("GroupId");
        if(resultCode != 0){
            logger.i("group#onRepGroupDetailInfo failed");
            return;
        }

        logger.i("group#onRepGroupDetailInfo");
        int groupSize = groupInfoListRsp.getIntValue("GroupSize");
        int loginId = imLoginManager.getLoginId();
        logger.i("group#onRepGroupDetailInfo cnt:%d",groupSize);
        if(groupSize <=0 || userId!=loginId){
            logger.i("group#onRepGroupDetailInfo size empty or userid[%d]≠ loginId[%d]",userId,loginId);
            return;
        }
        ArrayList<GroupEntity>  needDb = new ArrayList<>();
        JSONArray groupInfoList = groupInfoListRsp.getJSONArray("GroupInfoList");
        for (int i=0; i<groupInfoList.size(); i++) {
            // 群组的详细信息
            // 保存在DB中
            // GroupManager 中的变量
            JSONObject groupInfo = groupInfoList.getJSONObject(i);
            Gson gson = new Gson();
            GroupEntity groupEntity = gson.fromJson(groupInfo.toJSONString(), GroupEntity.class);
            needDb.add(groupEntity);
            GroupMemberEntity.insertOrUpdateMultiData(groupEntity.getGroupMemberList());
            groupMap.put(groupEntity.getPeerId(), groupEntity);
        }
        GroupEntity.insertOrUpdateMultiData(needDb);
        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
    }

    /**
     * 对于认证群，管理员确认后 给其他成员的通知
     * 对于非认证群， 有人加入后通知其他成员
     */
//    public void onNotifyGroupJoinConfirmNotify(IMGroup.IMGroupJoinConfirmNotify notify){
//        final int confirmUserId = notify.getConfirmUserId(); //谁加入
//        int groupId = notify.getGroupId();
//        final GroupEntity entity = groupMap.get(groupId);
//
//        IMBaseDefine.ConfirmType confirmType = notify.getConfirmType();
//        if(confirmType == IMBaseDefine.ConfirmType.CONFIRM_TYPE_AGREE || confirmType == IMBaseDefine.ConfirmType.CONFIRM_TYPE_NO_AUTH){
//            ArrayList<Integer> needFetchUserIds = new ArrayList<>();
//            final List<GroupMemberEntity> changeMemberEntityList = new ArrayList<>();
//            final GroupMemberEntity memberEntity = new GroupMemberEntity();
//            memberEntity.setGroupId(groupId);
//            memberEntity.setPeerId(confirmUserId);
//            memberEntity.setgKey(groupId+"_"+confirmUserId);
//            memberEntity.setStatus(0);
//            memberEntity.setUpdated(System.currentTimeMillis());
//            changeMemberEntityList.add(memberEntity);
//
//            UserEntity contact = IMContactManager.instance().findContact(confirmUserId);
//            if(contact != null && !contact.isFake()){
//                memberEntity.setNickName(contact.getMainName());
//                memberEntity.setAvatar(contact.getAvatar());
//                IMSessionManager.instance().updateSession(entity, changeMemberEntityList, confirmUserId, "JOIN");
//            }else {
//                memberEntity.setNickName("");
//
//                if (contact == null) {
//
//                    contact = new UserEntity();
//                    contact.setFake(true);
//                    contact.setPeerId(confirmUserId);
//                    contact.setFriend(false);
//                    contact.setMainName("");
//                    PinYin.getPinYin(contact.getMainName(), contact.getPinyinElement());
//                    IMContactManager.instance().putContact(contact);
//                    dbInterface.insertOrUpdateUser(contact);
//
//                    needFetchUserIds.add(confirmUserId);
//                    contactManager.reqGetDetaillUsers(needFetchUserIds, new Packetlistener() {
//                        @Override
//                        public void onSuccess(Object response) {
//                            CommonFunction.dismissProgressDialog();
//                            try{
//                                IMBuddy.IMUsersInfoRsp usersInfoRsp = IMBuddy.IMUsersInfoRsp.parseFrom((CodedInputStream) response);
//                                int resultCode = usersInfoRsp.getResultCode();
//                                if(resultCode == 0){
//                                    List<IMBaseDefine.UserInfo> userInfoList = usersInfoRsp.getUserInfoListList();
//
//                                    ArrayList<UserEntity>  dbNeed = new ArrayList<>();
//                                    for(IMBaseDefine.UserInfo userInfo : userInfoList) {
//                                        UserEntity userEntity = null;
//                                        UserEntity contact = IMContactManager.instance().findContact(userInfo.getUserId());
//                                        if(contact != null){
//                                            if(contact.isFake()){
//                                                userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo, contact.isFriend());
//                                            }else {
//                                                userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo, true);
//                                            }
//
//                                            IMContactManager.instance().putContact(userEntity);
//                                            dbNeed.add(userEntity);
//                                            memberEntity.setNickName(userEntity.getMainName());
//                                            dbInterface.batchInsertOrUpdateGroupMembers(entity.getGroupMemberList());
//                                        }
//
//                                    }
//
//
//
//                                    // 负责userMap
//                                    DBInterface.instance().batchInsertOrUpdateUser(dbNeed);
//
//                                    IMSessionManager.instance().updateSession(entity, changeMemberEntityList, confirmUserId, "JOIN");
//                                }
//                            }catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFaild() {
//
//                        }
//
//                        @Override
//                        public void onTimeout() {
//
//                        }
//                    });
//                }
//
//            }
//
//            entity.addGroupMemberList(changeMemberEntityList);
//            List<Integer> memberIds = new ArrayList<>();
//            for(GroupMemberEntity member : entity.getGroupMemberList()){
//                memberIds.add(member.getPeerId());
//            }
//            entity.setlistGroupMemberIds(memberIds);
//            groupMap.put(groupId,entity);
//            dbInterface.batchInsertOrUpdateGroupMembers(entity.getGroupMemberList());
//            dbInterface.insertOrUpdateGroup(entity);
//
//        }
//    }


    /**
     * 群组属性设置
     * @param groupId
     * @param type 修改类别
     * @param value
     */
//    public void reqModifyGroupInfo(final int groupId, final int type, String value){ }

    /**
     * 收到群组信息变更消息
     * 服务端主动发出
     * DB
     */
//    public void receiveGroupInfoChangeNotify(IMGroup.IMGroupInfoChangeNotify notify){}


    /**
     * 申请加入群
     */
//    public void reqApplyJoinGroup(final int groupId, final String joinInfo, final Packetlistener callback){}


    /**
     * 群成员属性设置
     * @param groupId
     * @param groupMemId
     * @param type 修改类别
     * @param value
     */
//    public void reqModifyGroupMemberInfo(final int groupId, final int groupMemId, final int type, String value){}

    /**
     * 收到群组成员信息变更消息
     * 服务端主动发出
     * DB
     */
//    public void onNotifyModifyGroupMemberInfo(IMGroup.IMGroupChangeMemberInfoNotify notify){}


    public List<GroupEntity> getNormalGroupList() {
        List<GroupEntity> normalGroupList = new ArrayList<>();
        for (Map.Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
            GroupEntity group = entry.getValue();
            if (group == null || group.isFake()) {
                continue;
            }
            if (group.getGroupType() == DBConstant.GROUP_TYPE_NORMAL || group.getGroupType() == DBConstant.GROUP_TYPE_AUTH_NORMAL) {
                normalGroupList.add(group);
            }
        }
        return normalGroupList;
    }

    // 该方法只有正式群
    // todo eric efficiency
    public  List<GroupEntity> getNormalGroupSortedList() {
        List<GroupEntity> groupList = getNormalGroupList();
        Collections.sort(groupList, new Comparator<GroupEntity>(){
            @Override
            public int compare(GroupEntity entity1, GroupEntity entity2) {
                if(entity1.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                }
                if(entity2.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
            }
        });

        return groupList;
    }

    public List<GroupEntity> getAllGroupList() {
        List<GroupEntity> groupList = new ArrayList<>();
        for (Map.Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
            GroupEntity group = entry.getValue();
            if (group == null || group.isFake()) {
                continue;
            }
            groupList.add(group);
        }

        Collections.sort(groupList, new Comparator<GroupEntity>(){
            @Override
            public int compare(GroupEntity entity1, GroupEntity entity2) {
                if(entity1.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                }
                if(entity2.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
            }
        });

        return groupList;
    }

    public GroupEntity findGroup(int groupId) {
        logger.d("group#findGroup groupId:%s", groupId);
        if(groupMap.containsKey(groupId)){
            return groupMap.get(groupId);
        }
        return null;
    }

    public List<GroupMemberEntity> loadAllGroupMembersByGroupId(int groupId){
        List<GroupMemberEntity> members = GroupMemberEntity.loadAllGroupMembersByGroupId(groupId);
        int index = -1;

        List<Integer> needFetchUserList = new ArrayList<>();
        for(int i=0, size = members.size(); i<size; i++){
            GroupMemberEntity member = members.get(i);
            UserEntity contact = IMContactManager.instance().findContact(member.getPeerId());
            if (contact == null) {
                contact = new UserEntity();
                contact.setFake(true);
                contact.setPeerId(member.getPeerId());
                contact.setMainName(member.getNickName());
                contact.setAvatar(member.getAvatar());
                contact.setFriend(false);
                PinYin.getPinYin(contact.getMainName(), contact.getPinyinElement());
                contact.setPinyinName(contact.getPinyinElement().pinyin);
                IMContactManager.instance().putContact(contact);

                UserEntity.insertOrUpdateSingleData(contact);

                needFetchUserList.add(member.getPeerId());
            }else if(contact.isFake()){
                needFetchUserList.add(member.getPeerId());
            }

            if(member.getPeerId() == findGroup(groupId).getCreatorId()) {//群主
                index = i;
            }
        }

        if(index >= 0){ //群主放于队首
            members.add(0, members.remove(index));
        }


        IMContactManager.instance().reqGetDetaillUsers(needFetchUserList);
        return members;
    }

    public GroupMemberEntity findGroupMember(int groupId, int peerId){

        GroupEntity group = findGroup(groupId);
        if(group !=null && group.getGroupMemberList() != null){

            for(GroupMemberEntity member : group.getGroupMemberList()){
                if(peerId == member.getPeerId())
                    return member;
            }
        }

        return GroupMemberEntity.findGroupMem(groupId, peerId);
    }


    public void updateGroupMember(UserEntity userEntity) {
        List<GroupMemberEntity> memberEntityList = GroupMemberEntity.findGroupMemByMemId(userEntity.getPeerId());

        for(GroupMemberEntity member : memberEntityList){
            String nickName =  member.getNickName();
            if(TextUtils.isEmpty(nickName)){
                member.setNickName(userEntity.getMainName());
                GroupMemberEntity.insertOrUpdateSingleData(member);

                GroupEntity group = groupMap.get(member.getGroupId());
                for(GroupMemberEntity m : group.getGroupMemberList()){
                    if(m.getGroupId() == member.getGroupId() && m.getPeerId() == member.getPeerId()){
                        m.setNickName(member.getNickName());
                        break;
                    }
                }
            }
        }
    }

    public List<GroupEntity>  getSearchAllGroupList(String key){
        List<GroupEntity> searchList = new ArrayList<>();
        for(Map.Entry<Integer,GroupEntity> entry:groupMap.entrySet()){
            GroupEntity groupEntity = entry.getValue();
//            if (IMUIHelper.handleGroupSearch(key, groupEntity)) {
//                searchList.add(groupEntity);
//            }
        }
        return searchList;
    }


    public List<UserEntity> getGroupMembers(int groupId) {
        logger.d("group#getGroupMembers groupId:%s", groupId);

        GroupEntity group = findGroup(groupId);
        if (group == null) {
            logger.e("group#no such group id:%s", groupId);
            return null;
        }
        List<Integer> userList = group.getlistGroupMemberIds();
        ArrayList<UserEntity> memberList = new ArrayList<UserEntity>();
        for (Integer id : userList) {
            UserEntity contact = IMContactManager.instance().findContact(id);
            if (contact == null) {
                logger.e("group#no such contact id:%s", id);
                continue;
            }
            memberList.add(contact);
        }
        return memberList;
    }

    /**------set/get 的定义*/
    public Map<Integer, GroupEntity> getGroupMap() {
        return groupMap;
    }

    public boolean isGroupReady() {
        return isGroupReady;
    }

}
