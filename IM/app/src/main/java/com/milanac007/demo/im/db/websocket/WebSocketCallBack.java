package com.milanac007.demo.im.db.websocket;

public interface WebSocketCallBack {
    void onConnectError(Throwable t);

    void onOpen();

    void onMessage(String text);

    void onClose();
}
