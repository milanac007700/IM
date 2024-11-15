package com.milanac007.demo.im.ui;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by milanac007 on 2017/1/6.
 */

public interface ISelectUserHandler {
    void handleSelectedUser(ArrayList<? extends Object> selectedUserIds, Boolean isChanged, Bundle extraData);
}