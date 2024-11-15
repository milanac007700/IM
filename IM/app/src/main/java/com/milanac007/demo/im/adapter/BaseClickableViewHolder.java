package com.milanac007.demo.im.adapter;

import android.view.View;


/**
 * Created by zqguo on 2017/1/17.
 */
public abstract class BaseClickableViewHolder extends BaseViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private int mPosition;

    public BaseClickableViewHolder(View view) {
        super(view);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public BaseClickableViewHolder(View view, boolean isListenerBySelf) {
        super(view);
        if (isListenerBySelf) {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}