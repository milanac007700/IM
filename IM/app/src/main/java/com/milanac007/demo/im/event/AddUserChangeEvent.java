package com.milanac007.demo.im.event;

import com.milanac007.demo.im.db.entity.UserEntity;

/**
 * Created by zqguo on 2016/9/14.
 */
public class AddUserChangeEvent {

    private int mOperateType;//0代表删除，1代表增加
    private UserEntity mPerson;

    public AddUserChangeEvent() {

    }


    public AddUserChangeEvent(UserEntity person, int operateType) {
        mPerson = person;
        mOperateType = operateType;
    }

    public int getOperateType() {
        return mOperateType;
    }

    public UserEntity getPerson() {
        return mPerson;
    }

}