package com.milanac007.demo.im.adapter;

import android.view.View;

/**
 * Created by zqguo on 2016/9/14.
 */
public class BaseViewHolder {

    public BaseViewHolder(View view){
        if (view == null){
            return;
        }
        view.setTag(this);
    }
}