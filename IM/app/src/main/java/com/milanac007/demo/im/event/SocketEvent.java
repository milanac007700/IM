package com.milanac007.demo.im.event;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 */
public class SocketEvent {
    public Event event;
    public MSG_SERVER_DISCONNECTED_REASON disconnected_reason;

    public enum Event{
        /**登陆之前的动作*/
        NONE,
        REQING_MSG_SERVER_ADDRS,
        REQ_MSG_SERVER_ADDRS_FAILED,
        REQ_MSG_SERVER_ADDRS_SUCCESS,

        /**请求登陆的过程*/
        CONNECTING_MSG_SERVER,
        CONNECT_MSG_SERVER_SUCCESS,
        CONNECT_MSG_SERVER_FAILED,
        MSG_SERVER_DISCONNECTED,    //channel disconnect 会触发，再应用开启内，要重连【可能是服务端、客户端断掉】
    };


    public SocketEvent(Event event){
        this.event = event;
    }

    public SocketEvent(Event event, MSG_SERVER_DISCONNECTED_REASON disconnected_reason){
        this.event = event;
        this.disconnected_reason = disconnected_reason;
    }
}
