package com.milanac007.demo.im.db.websocket;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.db.manager.IMLoginManager;
import com.milanac007.demo.im.utils.ImUser;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private static final String TAG = "WebSocketClient";
    private String wsUrl;
    private ConnectStatus status;
    private WebSocketCallBack mSocketSocketCallBack;

    public void setSocketIOCallBack(WebSocketCallBack callBack) {
        mSocketSocketCallBack = callBack;
    }

    public void removeSocketIOCallBack() {
        mSocketSocketCallBack = null;
    }

    public WebSocketClient(String wsUrl) {
        super(URI.create(wsUrl));
        this.wsUrl = wsUrl;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public synchronized void connect() {
        status = ConnectStatus.Connecting;
        super.connect();
    }

    public synchronized void reConnect() {
        if (status == ConnectStatus.Connecting) {
            return;
        }
        status = ConnectStatus.Connecting;
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.reconnect();
    }

    @Override
    public void close() {
        super.close();
        this.status = ConnectStatus.Closed;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "onOpen");
        this.status = ConnectStatus.Open;
        if(mSocketSocketCallBack != null) {
            mSocketSocketCallBack.onOpen();
        }
    }


    @Override
    public void onMessage(String message) {
        Log.i(TAG, "onRecv : " + message);
        if (mSocketSocketCallBack != null) { //TODO need new Thread ?
            mSocketSocketCallBack.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose");
        this.status = ConnectStatus.Closed;
        if (mSocketSocketCallBack != null) {
            mSocketSocketCallBack.onClose();
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.i(TAG, "onError: " + ex.toString());
        ex.printStackTrace();
        this.status = ConnectStatus.Canceled;
        if (mSocketSocketCallBack != null) {
            mSocketSocketCallBack.onConnectError(ex);
        }
    }

    public void send(int loginId, int tn, String type, String text) {
        Log.i(TAG, String.format("send type : %s, text: %s", type, text));
        JSONObject data = new JSONObject();
        data.put("reqUserId", loginId);
        data.put("tn", tn);
        data.put("type", type);
        data.put("data", text);
        super.send(data.toJSONString());
        Log.i(TAG, "onSend OK.");
    }

    public void send(String type, String text) {
        Log.i(TAG, String.format("send type : %s, text: %s", type, text));
        JSONObject data = new JSONObject();
        data.put("type", type);
        data.put("data", text);
        super.send(data.toJSONString());
        Log.i(TAG, "onSend OK.");
    }

    public void send(String tn, byte[] bytes) {
        Log.i(TAG, "onSend : " + new String(bytes));
        super.send(bytes);
        Log.i(TAG, "onSend OK : " + tn);
    }

    public void send(String tn, ByteBuffer byteBuffer) {
        Log.i(TAG, "onSend : " + new String(byteBuffer.array()));
        super.send(byteBuffer);
        Log.i(TAG, "onSend OK : " + tn);
    }

    public enum ConnectStatus {
        Connecting, // the initial state of each web socket.
        Open, // the web socket has been accepted by the remote peer
        Closing, // one of the peers on the web socket has initiated a graceful shutdown
        Closed, //  the web socket has transmitted all of its messages and has received all messages from the peer
        Canceled // the web socket connection failed
    }

    @Override
    public String toString() {
        return String.format("WebSocketClient[%s]",getLocalSocketAddress());
    }

}
