package com.milanac007.demo.im.event;


import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;

import java.util.List;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 */
public class GroupEvent {

    private int groupId;
    private GroupEntity groupEntity;
    private GroupMemberEntity memberEntity;
    private Event event;

    /**很多的场景只是关心改变的类型以及change的Ids*/
    private int changeType;
    private List<Integer> changeList;

    private int operateId; //操作者

    public GroupEvent(Event event){
        this.event = event;
    }

    public GroupEvent(Event event, GroupEntity groupEntity){
        this.groupEntity = groupEntity;
        this.event = event;
    }

    public GroupEvent(Event event, int groupId, GroupMemberEntity memberEntity){
        this.groupId = groupId;
        this.memberEntity = memberEntity;
        this.event = event;
    }

    public enum Event{
        NONE,

        GROUP_INFO_OK,
        GROUP_INFO_UPDATED,
        GROUP_INFO_DISBANDED, //退群
        GROUP_INFO_UPDATED_FAIL,
        GROUP_INFO_UPDATED_TIMEOUT,

        CHANGE_GROUP_MEMBER_SUCCESS,
        CHANGE_GROUP_MEMBER_FAIL,
        CHANGE_GROUP_MEMBER_TIMEOUT,

        CREATE_GROUP_OK,
        CREATE_GROUP_FAIL,
        CREATE_GROUP_TIMEOUT,

        SHIELD_GROUP_OK,
        SHIELD_GROUP_TIMEOUT,
        SHIELD_GROUP_FAIL,

        GROUP_SHOW_NICK //是否显示群昵称
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public List<Integer> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<Integer> changeList) {
        this.changeList = changeList;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }
    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }

    public GroupMemberEntity getMemberEntity() {
        return memberEntity;
    }

    public void setMemberEntity(GroupMemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getOperateId() {
        return operateId;
    }

    public void setOperateId(int operateId) {
        this.operateId = operateId;
    }
}
