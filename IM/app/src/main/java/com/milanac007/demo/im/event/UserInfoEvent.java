package com.milanac007.demo.im.event;



import com.milanac007.demo.im.db.entity.UserEntity;

import java.util.List;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 *
 * 用户信息事件
 * 1. 群组的信息
 * 2. 用户的信息
 */
public class UserInfoEvent {
    public Object object;
    public Event event;
    public List<UserEntity> userInfos;

    public UserInfoEvent(Event event){
        this.event = event;
    }
    public UserInfoEvent(Event event, List<UserEntity> u){
        this.event = event;
        userInfos = u;
    }

    public enum  Event{
        USER_INFO_OK,
        USER_INFO_UPDATE,
    }
}
