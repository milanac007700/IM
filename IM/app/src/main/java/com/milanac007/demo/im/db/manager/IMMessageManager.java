package com.milanac007.demo.im.db.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.milanac007.demo.im.db.callback.IMBaseDefine;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.ImAction;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.config.SysConstant;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.AudioMessage;
import com.milanac007.demo.im.db.entity.msg.ImageMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.entity.msg.VideoMessage;
import com.milanac007.demo.im.db.helper.ParseUtils;
import com.milanac007.demo.im.db.helper.SequenceNumberMaker;
import com.milanac007.demo.im.event.MessageEvent;
import com.milanac007.demo.im.event.PriorityEvent;
import com.milanac007.demo.im.event.RefreshHistoryMsgEvent;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.DownloadService;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_AUDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_IMG;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_GROUP_VEDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_AUDIO;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_IMG;
import static com.milanac007.demo.im.db.config.DBConstant.MSG_TYPE_SINGLE_VEDIO;

/**
 * 消息的处理
 */
public class IMMessageManager extends IMManager{
    private Logger logger = Logger.getLogger();

    // 消息发送超时时间设定
    private final static long TIMEOUT_MILLISECONDS = 15 * 1000;

    // 单例
    private static IMMessageManager inst = new IMMessageManager();
    public static IMMessageManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMSessionManager sessionManager = IMSessionManager.instance();

//    private DBInterface dbInterface = DBInterface.instance();

    /**
     * 接受到消息，并且向服务端发送确认
     * @param msg
     */
    public void ackReceiveMsg(MessageEntity msg) {
        logger.d("chat#ackReceiveMsg -> msg:%s", msg);

        if(msg.getSessionType() == DBConstant.SESSION_TYPE_GROUP) //群消息不回确认
            return;

        JSONObject data = new JSONObject();
        data.put("resultCode", 0);
        data.put("msgId", msg.getMsgId());
        data.put("fromId", msg.getFromId());
        data.put("toId", msg.getToId());
        data.put("sessionType", msg.getSessionType());

        try {
            imSocketManager.sendMsg(IMBaseDefine.MsgAck, data.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void doOnStart() {
        startDownloadFileService();
    }

    private Messenger mServiceMessenger;
    private ServiceConnection conn;
    private void startDownloadFileService(){
        Intent intent = new Intent(ctx, DownloadService.class);
        ctx.startService(intent);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceMessenger = null;
            }
        };
        ctx.bindService(intent, conn, Context.BIND_AUTO_CREATE);

        IntentFilter downloadFilefilter = new IntentFilter(ImAction.INTENT_DOWNLOAD_FINISHED);
        downloadFilefilter.addAction(ImAction.INTENT_DOWNLOAD_FAILED);
        ctx.registerReceiver(mDownloadReceiver, downloadFilefilter);
    }

    public void startDownloadFile(int fileType, String url, String filePath, int reqWidth, int reqHeight){

        Message message = Message.obtain();
        message.what = DownloadService.DOWNLOAD_ING;
        Bundle bundle = new Bundle();
        bundle.putString(DownloadService.DOWNLOAD_URL, url);
        bundle.putInt(DownloadService.DOWNLOAD_TYPE, fileType);
        bundle.putString(DownloadService.DOWNLOAD_FILE_PATH, filePath);
        bundle.putInt(DownloadService.DOWNLOAD_WIDTH, reqWidth);
        bundle.putInt(DownloadService.DOWNLOAD_HEIGHT, reqHeight);
        message.setData(bundle);
        try {
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String url = intent.getStringExtra(DownloadService.DOWNLOAD_URL);
            if(action.equals(ImAction.INTENT_DOWNLOAD_FINISHED)){

            }else if(action.equals(ImAction.INTENT_DOWNLOAD_FAILED)){
                //TODO
                CommonFunction.showToast("下载失败: " + url);
            }
        }
    };

    public void onLoginSuccess(){
        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().register(inst);
        }
    }

    @Override
    public void reset() {
        EventBus.getDefault().unregister(inst);

        if(mDownloadReceiver != null){
            ctx.unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
        if(conn != null) {
            ctx.unbindService(conn);
            conn = null;
        }
    }

    /**
     * 自身的事件驱动
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**图片的处理放在这里，因为在发送图片的过程中，很可能messageActivity已经关闭掉*/
    public void onEvent(MessageEvent event){
        MessageEvent.Event  type = event.getEvent();
        switch (type){
            case FILE_UPLOAD_FAILD:{
                logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage)event.getMessageEntity();
                imageMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
                imageMessage.setStatus(MessageConstant.MSG_FAILURE);
//                dbInterface.insertOrUpdateMessage(imageMessage);
                MessageEntity.insertOrUpdateSingleData(imageMessage);

                /**通知Activity层 失败*/
                event.setEvent(MessageEvent.Event.HANDLER_FILE_UPLOAD_FAILD);
                event.setMessageEntity(imageMessage);
                triggerEvent(event);
            }break;

            case FILE_UPLOAD_SUCCESS:{
                onImageLoadSuccess(event);
            }break;
        }
    }

    /**
     * 事件的处理会在一个后台线程中执行，对应的函数名是onEventBackgroundThread，
     * 虽然名字是BackgroundThread，事件处理是在后台线程，
     * 但事件处理时间还是不应该太长
     * 因为如果发送事件的线程是后台线程，会直接执行事件，
     * 如果当前线程是UI线程，事件会被加到一个队列中，由一个线程依次处理这些事件，
     * 如果某个事件处理时间太长，会阻塞后面的事件的派发或处理
     * */
    public void onEventBackgroundThread(RefreshHistoryMsgEvent historyMsgEvent){
//        doRefreshLocalMsg(historyMsgEvent);
    }

    /**----------------------底层的接口-------------------------------------*/
    /**
     * 发送消息，最终的状态情况
     * MessageManager下面的拆分
     * 应该是自己发的信息，所以msgId为0
     * 这个地方用DB id作为主键
     */
    public void sendMessage(MessageEntity messageEntity) {
        logger.d("chat#sendMessage, msg:%s", messageEntity);
        // 发送情况下 msg_id 都是0
        // 服务端是从1开始计数的
        if (!SequenceNumberMaker.getInstance().isFailure(messageEntity.getMsgId())) {
            throw new RuntimeException("#sendMessage# msgId is wrong,cause by 0!");
        }

        Gson gson = new Gson();
        String data = gson.toJson(messageEntity, MessageEntity.class);
        imSocketManager.sendMsg(IMBaseDefine.Msg, data, new Packetlistener(TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                JSONObject msgDataAck = JSONObject.parseObject((String) response);
                int resultCode = msgDataAck.getIntValue("resultCode");
                if(resultCode != 0) {

                }else {
                    int msgId = msgDataAck.getIntValue("msgId");
                    if(msgId <= 0){
                        throw  new RuntimeException("Msg ack error,cause by msgId <=0");
                    }

                    messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
                    messageEntity.setMsgId(msgId);

                    //对于语音、图片和小视频消息，发送的content是url，之后替换成JsonObject存入DB
                    messageEntity.setContent(messageEntity.getContent());

                    /**主键ID已经存在，直接替换*/
                    MessageEntity.insertOrUpdateSingleData(messageEntity);
                    /**更新sessionEntity lastMsgId问题*/
                    sessionManager.updateSession(messageEntity);
                    triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_OK, messageEntity));
                }
            }

            @Override
            public void onTimeout() {
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);

                //对于语音、图片和小视频消息，发送的content是url，之后替换成JsonObject存入DB
                messageEntity.setContent(messageEntity.getContent());
                MessageEntity.insertOrUpdateSingleData(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_TIME_OUT,messageEntity));
            }

            @Override
            public void onFail(String error) {
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                MessageEntity.insertOrUpdateSingleData(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE,messageEntity));
            }
        });
    }

    /**
     * 收到服务端原始信息
     * 1. 解析消息的类型
     * 2. 根据不同的类型,转化成不同的消息
     * 3. 先保存在DB[insertOrreplace]中，session的更新，Unread的更新
     * 4上层通知
     */
    public void onRecvMessage(MessageEntity imMsgData, boolean needAck) {
        logger.i("chat#onRecvMessage");
        if (imMsgData == null) {
            logger.w("chat#decodeMessageInfo failed,cause by is null");
            return;
        }

        MessageEntity recvMessage = ParseUtils.getMessageEntity(imMsgData);

        int msgSenderId = recvMessage.getFromId();
        List<Integer> needFetchUserList = new ArrayList<>();
        UserEntity contact = IMContactManager.instance().findContact(msgSenderId);

        // TODO
//        if(recvMessage.getSessionType() == DBConstant.SESSION_TYPE_SINGLE){
//            if(contact == null) //私聊：不是好友不能收 TODO 放在服务器处理
//                return;
//
//            int loginId = IMLoginManager.instance().getLoginId();//过滤自己发给自己的消息, 多终端同步时还需进一步处理
//            if(msgSenderId == loginId)
//                return;
//        }

        if (contact == null) {
            contact = new UserEntity();
            contact.setFake(true);
            contact.setPeerId(msgSenderId);
            contact.setMainName("");
            contact.setFriend(false);
            PinYin.getPinYin(contact.getMainName(), contact.getPinyinElement());
            contact.setPinyinName(contact.getPinyinElement().pinyin);

            IMContactManager.instance().putContact(contact);
            UserEntity.insertOrUpdateSingleData(contact);
            needFetchUserList.add(msgSenderId);
        }else if(contact.isFake()){
            needFetchUserList.add(msgSenderId);
        }
        //联网获取 人的详情 TODO
//        IMContactManager.instance().reqGetDetaillUsers(needFetchUserList, null);

        int loginId = IMLoginManager.instance().getLoginId();
        boolean isSend = recvMessage.isSend(loginId);
        recvMessage.buildSessionKey(isSend);
        recvMessage.setStatus(MessageConstant.MSG_SUCCESS);

        //TODO 发现有msgId 重复的bug
        MessageEntity.insertOrUpdateSingleData(recvMessage);
        sessionManager.updateSession(recvMessage);

        if (needAck) {
            //rsp应答
            ackReceiveMsg(recvMessage);
        }

        /**
         *  发送已读确认由上层的activity处理 特殊处理
         *  1. 未读计数、 通知、session页面
         *  2. 当前会话
         * */
        PriorityEvent notifyEvent = new PriorityEvent();
        notifyEvent.event = PriorityEvent.Event.MSG_RECEIVED_MESSAGE;
        notifyEvent.object = recvMessage;
        triggerEvent(notifyEvent);

    }



    /**-------------------其实可以继续分层切分---------消息发送相关-------------------------------*/
    /**
     * 1. 先保存DB
     * 2. push到adapter中
     * 3. 等待ack,更新页面
     * */
    public void sendText(TextMessage textMessage) {
        logger.e("chat#text#textMessage : %s, to: ", textMessage.getContent(), textMessage.getToId());
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        try {
//            long pkId =  DBInterface.instance().insertOrUpdateMessage(textMessage);
            MessageEntity.insertOrUpdateSingleData(textMessage);
        }catch (Exception e){
            e.printStackTrace();
        }

        sessionManager.updateSession(textMessage);
        sendMessage(textMessage);
    }

    public void sendVoice(AudioMessage audioMessage) {
        logger.i("chat#audio#sendVoice");
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
//        long pkId =  DBInterface.instance().insertOrUpdateMessage(audioMessage);
        MessageEntity.insertOrUpdateSingleData(audioMessage);
        sessionManager.updateSession(audioMessage);
        sendMessage(audioMessage);
    }


    public void sendVideo(VideoMessage videoMessage) {
        logger.i("chat#video#sendVideo");
        videoMessage.setStatus(MessageConstant.MSG_SENDING);
//        long pkId =  DBInterface.instance().insertOrUpdateMessage(videoMessage);
        MessageEntity.insertOrUpdateSingleData(videoMessage);
        sessionManager.updateSession(videoMessage);
        sendMessage(videoMessage);
    }

    public void sendSingleImage(ImageMessage imageMessage){
        logger.i("chat#video#sendImage");
        imageMessage.setStatus(MessageConstant.MSG_SENDING);
//        long pkId =  DBInterface.instance().insertOrUpdateMessage(videoMessage);
        MessageEntity.insertOrUpdateSingleData(imageMessage);
        sessionManager.updateSession(imageMessage);
        sendMessage(imageMessage);
    }

    /**
     * 重新发送 message数据包
     * 1.检测DB状态
     * 2.删除DB状态 [不用删除]
     * 3.调用对应的发送
     * 判断消息的类型、判断是否是重发的状态
     * */
    public void resendMessage(MessageEntity msgInfo) {
        if (msgInfo == null) {
            logger.d("chat#resendMessage msgInfo is null or already send success!");
            return;
        }
        /**check 历史原因处理*/
        if(!SequenceNumberMaker.getInstance().isFailure(msgInfo.getMsgId())){
            // 之前的状态处理有问题
            msgInfo.setStatus(MessageConstant.MSG_SUCCESS);
//            dbInterface.insertOrUpdateMessage(msgInfo);
            MessageEntity.insertOrUpdateSingleData(msgInfo);
            triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_OK,msgInfo));
            return;
        }

        logger.d("chat#resendMessage msgInfo %s",msgInfo);
        /**重新设定message 的时间,已经从DB中删除*/
        long nowTime = System.currentTimeMillis();
        msgInfo.setUpdated(nowTime);
        msgInfo.setCreated(nowTime);

        /**判断信息的类型*/
        int msgType = msgInfo.getDisplayType();
        switch (msgType){
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                sendText((TextMessage)msgInfo);
                break;
            case DBConstant.SHOW_IMAGE_TYPE:
                sendSingleImage((ImageMessage) msgInfo);
                break;
            case DBConstant.SHOW_AUDIO_TYPE:
                sendVoice((AudioMessage)msgInfo); break;
            default:
                throw new IllegalArgumentException("#resendMessage#enum type is wrong!!,cause by displayType"+msgType);
        }
    }

    public void asyncDownloadFile(MessageEntity recvMessage){
        int msgType = recvMessage.getMsgType();
        switch (msgType){
            case MSG_TYPE_SINGLE_IMG:
            case MSG_TYPE_GROUP_IMG:{
                JSONObject content = JSONObject.parseObject(recvMessage.getContent());
                String imageUrl = content.getString("url");
                String imagePath = content.getString("path");
                int index = imageUrl.indexOf("?");
                int reqWidth = Target.SIZE_ORIGINAL;
                int reqHeight = Target.SIZE_ORIGINAL;
                if(index > 0){
                    String sizeStr = imageUrl.substring(index+1);
                    String[] size = sizeStr.split("x");
                    reqWidth = Integer.valueOf(size[0]);
                    reqHeight = Integer.valueOf(size[1]);
                }
                startDownloadFile(DownloadService.DOWNLOAD_TYPE_IMAGE, imageUrl, imagePath, reqWidth, reqHeight);
            }break;

            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:{
                JSONObject content = JSONObject.parseObject(recvMessage.getContent());
                String audioUrl = content.getString("url");
                String audioPath = content.getString("audioPath");
                startDownloadFile(DownloadService.DOWNLOAD_TYPE_AUDIO, audioUrl, audioPath, -1, -1);
            }break;
            case MSG_TYPE_SINGLE_VEDIO:
            case MSG_TYPE_GROUP_VEDIO:{
                JSONObject content = JSONObject.parseObject(recvMessage.getContent());
                String videoUrl = content.getString("videoUrl");
                String thumbnailUrl = content.getString("thumbnailUrl");
                String thumbnailPath = content.getString("thumbnailPath");

                //只下载缩略图
                final int index = videoUrl.indexOf("?");
                int reqWidth = 480;
                int reqHeight = 480;
                if(index != -1){
                    String subStr = videoUrl.substring(index+1);
                    String[] size = subStr.split("x");
                    reqWidth = (int)Double.valueOf(size[0]).doubleValue();
                    reqHeight = (int)Double.valueOf(size[1]).doubleValue();
                }

                startDownloadFile(DownloadService.DOWNLOAD_TYPE_IMAGE, thumbnailUrl, thumbnailPath, reqWidth, reqHeight);
            }break;
            default:
                break;

        }

    }

        /**
         * 拉取历史消息
         * @param sessionKey
         * @param lastCreateTime 最后一条消息的时间，
         * @param isFirst 是否是首次拉取
         * @return
         */
    public List<MessageEntity> loadHistoryMsg(String sessionKey, long lastCreateTime, boolean isFirst){

        int count = SysConstant.MSG_CNT_PER_PAGE;
        if(count <0 || lastCreateTime <0|| TextUtils.isEmpty(sessionKey)){
            return Collections.emptyList();
        }

//        return dbInterface.getHistoryMsg(sessionKey,lastCreateTime, count, isFirst);
        return MessageEntity.getHistoryMsg(sessionKey, lastCreateTime, count, isFirst);
    }

    /**
     * network 请求历史消息
     */
    public  void reqHistoryMsgNet(int peerId,int peerType, int lastMsgId, int cnt){}


    /**下载图片的整体迁移出来*/
    private void onImageLoadSuccess(MessageEvent imageEvent){

        ImageMessage imageMessage = (ImageMessage)imageEvent.getMessageEntity();
        logger.d("pic#onImageUploadFinish");
        String imageUrl = imageMessage.getUrl();
        logger.d("pic#imageUrl:%s", imageUrl);
        String realImageURL = "";
        try {
            realImageURL = URLDecoder.decode(imageUrl, "utf-8");
            logger.d("pic#realImageUrl:%s", realImageURL);
        } catch (UnsupportedEncodingException e) {
            logger.e(e.toString());
        }

        imageMessage.setUrl(realImageURL);
        imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
        imageMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
//        dbInterface.insertOrUpdateMessage(imageMessage);
        MessageEntity.insertOrUpdateSingleData(imageMessage);

        /**通知Activity层 成功 ， 事件通知*/
        imageEvent.setEvent(MessageEvent.Event.HANDLER_FILE_UPLOAD_SUCCESS);
        imageEvent.setMessageEntity(imageMessage);
        triggerEvent(imageEvent);

        imageMessage.setContent(MessageConstant.IMAGE_MSG_START
                + realImageURL + MessageConstant.IMAGE_MSG_END);
        sendMessage(imageMessage);
    }


    public void deleteMessageByMsgId(int msgId, String sessionKey){
//        MessageEntity delMsg = dbInterface.getOneMessage(msgId, sessionKey);
        MessageEntity delMsg = MessageEntity.getOneMessage(msgId, sessionKey);
        long delUpdateTime = delMsg.getUpdated();
//        dbInterface.deleteMessage(msgId, sessionKey);
        MessageEntity.deleteMessage(msgId, sessionKey);
        SessionEntity sessionEntity = sessionManager.findSession(sessionKey);
        if(msgId == sessionEntity.getLatestMsgId()){
//            MessageEntity msg = dbInterface.getLastMsgBySessionKey(sessionKey);
            MessageEntity msg = MessageEntity.getLastMsgBySessionKey(sessionKey);
            if(msg !=null){
                sessionEntity.setUpdated(msg.getUpdated());
                sessionEntity.setLatestMsgData(msg.getMessageDisplay());
                sessionEntity.setLatestMsgId(msg.getMsgId());
                sessionEntity.setLatestMsgType(msg.getMsgType());
                sessionEntity.setTalkId(msg.getFromId());
            }else { //没有消息了
                sessionEntity.setLatestMsgData("");
                sessionEntity.setUpdated(delUpdateTime);
                sessionEntity.setLatestMsgType(-1);
            }
        }
    }

}
