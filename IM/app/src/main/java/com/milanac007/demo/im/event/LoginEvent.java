package com.milanac007.demo.im.event;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 */
public class LoginEvent {
    private Event type;
    private Throwable e;

    public LoginEvent(Event type) {
        this.type = type;
    }

    public Throwable getError() {
        return e;
    }

    public void setError(Throwable e) {
        this.e = e;
    }

    public Event getType() {
        return type;
    }

    public enum Event {
        NONE,
        LOGINING,
        // 网络登陆验证成功
        LOGIN_OK,
        LOGIN_INNER_FAILED,
        LOGIN_AUTH_FAILED,
        LOGIN_OUT,

        // 对于离线登陆
        // 如果在此时，网络登陆返回账号密码错误应该怎么处理? todo 强制退出
        // 登陆成功之后触发 LOCAL_LOGIN_MSG_SERVICE
        LOCAL_LOGIN_SUCCESS,
        LOCAL_LOGIN_MSG_SERVICE,


        PC_ONLINE,
        PC_OFFLINE,
        KICK_PC_SUCCESS,
        KICK_PC_FAILED
    }

}
