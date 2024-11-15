package com.milanac007.demo.im.db.websocket;

import com.milanac007.demo.im.logger.Logger;

public class SocketThread extends Thread{

    private String strHost = null;
    private int nPort = 0;
    private static Logger logger = Logger.getLogger();

//    public SocketThread(String strHost, int nPort, SimpleChannelHandler handler) {
//        this.strHost = strHost;
//        this.nPort = nPort;
//        init(handler);
//    }

    @Override
    public void run() {
        doConnect();
    }

    public boolean doConnect() {
        return false;
    }

    public boolean sendRequest(){
        return false;
    }

}
