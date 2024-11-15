package com.milanac007.demo.im.db.manager;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.activity.LoginActivity;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.exception.TdrException;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.net.NetRequestByOkHttpClient;
import com.milanac007.demo.im.rxjava.AndroidScheduler;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import de.greenrobot.event.EventBus;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IMLoginManager extends IMManager {

    private Logger logger = Logger.getLogger();

    // 单例
    private static IMLoginManager inst = new IMLoginManager();
    public static IMLoginManager instance() {
        return inst;
    }

    public IMLoginManager() {

    }

    private int loginId;
    private UserEntity loginInfo;


    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        loginId = -1;
        loginInfo = null;
    }

    public synchronized void triggerEvent(LoginEvent event) {
        EventBus.getDefault().post(event);
    }

    public void login(String account, String sha256Pwd) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                JSONObject param = new JSONObject();

                param.put("userCode", account);
                param.put("userPassword", sha256Pwd);
//                String rsp = new NetRequestByOkHttpClient().postRequest(NetConstants.LoginURL, param.toJSONString());

                //TODO test
                int peerId = 0;
                switch (account) {
                    case "test0001": peerId = 10001;break;
                    case "test0002": peerId = 10002;break;
                    case "test0003": peerId = 10003;break;
                    case "test0004": peerId = 10004;break;
                    case "test0005": peerId = 10005;break;
                    case "test0006": peerId = 10006;break;
                }

                String rsp;
                JSONObject rspObject1 = new JSONObject();
                if (peerId == 0) {
                    rspObject1.put("resultCode", -1);
                    rspObject1.put("errorMsg", "账号不存在");
                    rsp = rspObject1.toJSONString();
                } else {
                    rspObject1.put("resultCode", 0);
                    JSONObject respdata1 = new JSONObject();
                    respdata1.put("peerId", peerId);
                    respdata1.put("mainName", account);
                    respdata1.put("nickName", account);
                    respdata1.put("userCode", account);
                    respdata1.put("gender", 0);
                    respdata1.put("created", System.currentTimeMillis());
                    respdata1.put("isFriend", true);
                    rspObject1.put("respdata", respdata1);
                    rsp = rspObject1.toJSONString();
                }

                Log.i(TAG(), "登录： "+ rsp);
                JSONObject rspObject =  JSONObject.parseObject(rsp);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {
                    final String errorMsg = rspObject.getString("errorMsg");
                    TdrException tdrException = new TdrException(resultCode, errorMsg);
                    emitter.onError(tdrException);
                }else {
                    JSONObject respdata = rspObject.getJSONObject("respdata");
                    int uuid = respdata.getIntValue("peerId");
                    String userCode = respdata.getString("userCode");
                    String userName = respdata.getString("mainName");
                    int gender = respdata.getIntValue("gender");

                    //更新DB
                    User currentUser = new User();
                    currentUser.setUuid(uuid);
                    currentUser.setUserCode(userCode);
                    currentUser.setName(userName);
                    currentUser.setGender(gender);
                    currentUser.setSha256Pwd(sha256Pwd);
                    currentUser.setLastloginTime(System.currentTimeMillis());
                    User.insertOrUpdateSingleData(currentUser);
                    UserEntity.insertOrUpdateSingleData(respdata);

                    //更新sp
                    Preferences.setLoginName(account);
                    Preferences.setSHA265Pwd(sha256Pwd);
                    Preferences.updateCurrentLoginer(currentUser);

                    emitter.onNext(true);
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        LoginEvent event = new LoginEvent(LoginEvent.Event.LOGINING);
                        triggerEvent(event);
                    }

                    @Override
                    public void onNext(@NonNull Boolean result) {
//                        LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_OK);
//                        triggerEvent(event);
                        if(result) {
                            reqLoginMsgServer();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_AUTH_FAILED);
                        event.setError(e);
                        triggerEvent(event);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public void reqLoginMsgServer() {
        IMSocketManager.instance().loginMsgServer(App.getContext(), new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int resultCode = rspObject.getIntValue("resultCode");
                if(resultCode != 0) {
                    LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_AUTH_FAILED);
                    event.setError(new TdrException(resultCode, rspObject.getString("errorMsg")));
                    triggerEvent(event);
                    IMSocketManager.instance().release(App.getContext());
                }else {
                    //更新变量
                    String userInfo = rspObject.getString("userInfo");
                    Gson gson = new Gson();
                    UserEntity user = gson.fromJson(userInfo, UserEntity.class);
                    int peerId = user.getPeerId();
                    setLoginId(peerId);
                    setLoginInfo(UserEntity.getByPeerId(peerId));
                    UserEntity.insertOrUpdateSingleData(user);

                    LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_OK);
                    triggerEvent(event);
                }
            }

            @Override
            public void onFail(String error) {
                LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_INNER_FAILED);
                event.setError(new TdrException(-1, error));
                triggerEvent(event);
                IMSocketManager.instance().release(App.getContext());
            }

            @Override
            public void onTimeout() {
                LoginEvent event = new LoginEvent(LoginEvent.Event.LOGIN_INNER_FAILED);
                event.setError(new TdrException(-1, "访问超时"));
                triggerEvent(event);
                IMSocketManager.instance().release(App.getContext());
            }
        });
    }

    /**------------------状态的 set  get------------------------------*/
    public int getLoginId() {
        return loginId;
    }

    public void setLoginId(int loginId) {
        logger.d("login#setLoginId -> loginId:%d", loginId);
        this.loginId = loginId;

    }

    public UserEntity getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(UserEntity loginInfo) {
        this.loginInfo = loginInfo;
    }

}
