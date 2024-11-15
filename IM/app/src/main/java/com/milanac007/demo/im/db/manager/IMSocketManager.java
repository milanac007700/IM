package com.milanac007.demo.im.db.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.db.callback.ListenerQueue;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.helper.SequenceNumberMaker;
import com.milanac007.demo.im.db.websocket.WebSocketCallBack;
import com.milanac007.demo.im.db.websocket.WebSocketClient;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.utils.Preferences;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import androidx.annotation.NonNull;

public class IMSocketManager extends IMManager {
    public final static long TIMEOUT_MILLISECONDS = 15 * 1000;

    private Logger logger = Logger.getLogger();
    // 单例
    private static IMSocketManager inst = new IMSocketManager();
    private UserEntity currentUserEntity;
    private Packetlistener packetlistener;

    public static IMSocketManager instance() {
        return inst;
    }


    private static String URL;
    private boolean mIsConnect;
    private boolean mIsReleased;
    private WebSocketClient instance;
    private Context mContext;
    //callback 队列
    private ListenerQueue listenerQueue = ListenerQueue.getInstance();

    public void pushListener(int seqNo,Packetlistener packetlistener){
        if(seqNo <= 0 || null == packetlistener){
            logger.d("ListenerQueue#push error, cause by Illegal params");
            return;
        }
        listenerQueue.push(seqNo,packetlistener);
    }


    public Packetlistener popListener(int seqNo){
        return listenerQueue.pop(seqNo);
    }

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {

    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1://心跳
                    try {
                        boolean closed = instance.isClosed();
                        if (closed) {
                            reConnect(instance);
                        } else {
                            onHeartBeat();
                        }
                        mHandler.sendEmptyMessageDelayed(1,5000);
                    } catch (Exception e) {
                        reConnect(instance);
                    }

                    break;
            }
            return false;
        }
    });

    public void loginMsgServer(Context context, Packetlistener listener) {
        mIsReleased = false;
        mContext = context.getApplicationContext();

        String userCode = Preferences.getLoginName();
        currentUserEntity = UserEntity.getByUserCode(userCode);
        packetlistener = listener;

        if (instance == null || instance.isClosed()) {
            URL = String.format("ws://%s:8081", NetConstants.HostName);
            instance = new WebSocketClient(URL);
            instance.setSocketIOCallBack(webSocketCallBack);
            instance.connect();
        }
//TODO
//        mHandler.removeMessages(1);
//        mHandler.sendEmptyMessageDelayed(1,5000);
    }


    public void release(Context context) {
        mIsReleased = true;
        instance.close();
        mHandler.removeMessages(1);
    }

    public void onHeartBeat(){
        mIsConnect = true;
        //TODO
    }

    public void sendMsg(String type, String msg) throws Exception {
        if(instance.isClosed()) {
            throw new Exception("the WebSocketClient is not connected to server.");
        }

        instance.send(type, msg);
    }

    public void sendMsg(String type, String msg, Packetlistener listener) {
        if(instance.isClosed()) {
            String error = "the WebSocketClient is not connected to server.";
            if(listener != null) {
                listener.onFail(error);
            }
            return;
        }

        int tn = SequenceNumberMaker.getInstance().make();
        pushListener(tn, listener);
        int loginId = IMLoginManager.instance().getLoginId();
        instance.send(loginId, tn, type, msg);
    }


    private WebSocketCallBack webSocketCallBack = new WebSocketCallBack(){
        @Override
        public void onConnectError(Throwable t) {
            logger.i("onConnectError "+t.toString());
        }

        @Override
        public void onOpen() {
            logger.i("onOpen");
            listenerQueue.onStart();
            Gson gson = new Gson();
            String userJson = gson.toJson(currentUserEntity, UserEntity.class);
            int tn = SequenceNumberMaker.getInstance().make();
            pushListener(tn, packetlistener);
            int loginId = IMLoginManager.instance().getLoginId();
            instance.send(loginId, tn, IMBaseDefine.Login, userJson);
        }

        @Override
        public void onMessage(String message) {
            JSONObject messageData = JSONObject.parseObject(message);
            int tn = messageData.getIntValue("tn");
            String type = messageData.getString("type");
            String data = messageData.getString("data");

            logger.i("onMessage type : %s, data: %s", type, data);

            Packetlistener packetlistener = popListener(tn);
            if(packetlistener != null) {
                packetlistener.onSuccess(data);
                return;
            }

            Gson gson = new Gson();
            switch (type) {
                case IMBaseDefine.Login:{

                }break;
                case IMBaseDefine.Msg:{
                    MessageEntity msg = gson.fromJson(data, MessageEntity.class);
                    IMMessageManager.instance().onRecvMessage(msg, true);
                }break;
                case IMBaseDefine.GroupChangeMemberNotify:{
                    IMGroupManager.instance().onNotifyGroupChangeMember(data);
                }break;
                case IMBaseDefine.onNotifyAddBuddy:{
                    IMContactManager.instance().onNotifyAddBuddy(data);
                }break;
                case IMBaseDefine.onNotifyAddBuddyAccept:{
                    IMContactManager.instance().onNotifyAddBuddyAccept(data);
                }break;
                case IMBaseDefine.onNotifyCreateGroup:{
                    IMGroupManager.instance().onNotifyCreateGroup(data);
                }break;
            }

        }

        @Override
        public void onClose() {
            logger.i("onClose ");
            listenerQueue.onDestory();
        }
    };

    private void reConnect(final WebSocketClient instance) {
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);//服务异常重新绑定延迟5s，以减少更新应用带来的异常
                    if(!mIsReleased) {
                        if (instance.isClosed()) {
                            instance.reConnect();
                        }
                    }
                } catch (Exception e) {

                }
                finally {

                }
            }
        }.start();
    }

    public void testNIO() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String remoteAddress = "192.168.8.57";
                int remotePort = 10001;
                try {
                    SocketChannel clientChannel = SocketChannel.open();
                    clientChannel.configureBlocking(false); //非阻塞，以使用Selector
                    clientChannel.connect(new InetSocketAddress(remoteAddress, remotePort));
                    while (!clientChannel.finishConnect()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    logger.i("clientChannel.finishConnect(): " + clientChannel.finishConnect() + ", clientChannel.isConnected(): " + clientChannel.isConnected());

                    Selector selector = null;
                    try {
                        selector = Selector.open();
                        clientChannel.register(selector, SelectionKey.OP_READ, new Integer(1));
                        boolean mIsRunning = true;

                        localBuffer.put("hello, I am a client".getBytes());
                        localBuffer.flip();
//                        clientChannel.write(localBuffer, 0, localBuffer.limit());
                        clientChannel.write(localBuffer);
                        localBuffer.clear();
                        while (mIsRunning) {
                            int size = selector.selectNow();
                            if(size == 0) {
                                continue;
                            }

                            Set<SelectionKey> selectionKeys = selector.selectedKeys();
                            Iterator<SelectionKey> it = selectionKeys.iterator();
                            while (it.hasNext()) {
                                SelectionKey key = it.next();
                                it.remove();
                                if(key.isReadable()) {
                                    int value =  (Integer) key.attachment();
                                    if(value == 1) {
                                        zeroCopy(clientChannel, localBuffer);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            selector.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
//                    localChannel.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            localBuffer.clear();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    ByteBuffer localBuffer = ByteBuffer.allocate(100);
    protected void zeroCopy(SocketChannel in, ByteBuffer buffer) throws IOException {
        int count;

        while ((count = in.read(buffer)) > 0) {
            buffer.flip();

            byte[] arr = buffer.array();
            logger.i("datas to recv: " + new String(arr,0, buffer.limit()));
            buffer.clear(); //不同于Stream，缓冲区的内容一直都在，所以读完后需要清空，才能进行向里面写
        }

        if(count == -1) { //远端正常关闭了连接时，read反-1； 非正常断开连接时，会抛出IOException
            throw new IOException("远端正常关闭了连接");
        }

    }
}
