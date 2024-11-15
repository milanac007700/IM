package com.milanac007.demo.im.db.callback;

public interface IMListener<T> {
    void onSuccess(T rsp);

    void onFail(String error);

    void onTimeout();
}
