package com.milanac007.demo.im.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.google.gson.Gson;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.adapter.MainFragmentPagerAdapter;
import com.milanac007.demo.im.databinding.ImContaninerBinding;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.config.ImAction;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.BuddyVerifyMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.db.manager.IMMessageManager;
import com.milanac007.demo.im.db.manager.IMUnreadMsgManager;
import com.milanac007.demo.im.event.AddUserChangeEvent;
import com.milanac007.demo.im.event.BuddyVerifyEvent;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.event.MSG_SERVER_DISCONNECTED_REASON;
import com.milanac007.demo.im.event.MessageEvent;
import com.milanac007.demo.im.event.ReqSipNegotiationEvent;
import com.milanac007.demo.im.event.SocketEvent;
import com.milanac007.demo.im.event.UnreadEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.fragment.AboutOAFragment;
import com.milanac007.demo.im.fragment.AccountSecurityFragment;
import com.milanac007.demo.im.fragment.AddFriendFragment;
import com.milanac007.demo.im.fragment.AddFriendVerifyMsgFragment;
import com.milanac007.demo.im.fragment.AppFragment;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.ChatFragment;
import com.milanac007.demo.im.fragment.ContactsBaseFragment;
import com.milanac007.demo.im.fragment.ContactsFragmentNew;
import com.milanac007.demo.im.fragment.CreateGroupChatFragment;
import com.milanac007.demo.im.fragment.GroupAdminFragment;
import com.milanac007.demo.im.fragment.GroupAdminModifyFragment;
import com.milanac007.demo.im.fragment.GroupChatMainFragment;
import com.milanac007.demo.im.fragment.GroupChatSettingFragment;
import com.milanac007.demo.im.fragment.GroupIntroductFragment;
import com.milanac007.demo.im.fragment.GroupNameModifyFragment;
import com.milanac007.demo.im.fragment.GroupNoticeEditFragment;
import com.milanac007.demo.im.fragment.MineFragment;
import com.milanac007.demo.im.fragment.ModifyBuddyExtraFragment;
import com.milanac007.demo.im.fragment.ModifyGroupMemFragment;
import com.milanac007.demo.im.fragment.ModifyPersonalInfoFragment;
import com.milanac007.demo.im.fragment.ModifyPwdFragment;
import com.milanac007.demo.im.fragment.MyDetailFragment;
import com.milanac007.demo.im.fragment.MyFriendFragment;
import com.milanac007.demo.im.fragment.NewMsgNofitySettingFragment;
import com.milanac007.demo.im.fragment.PersonalInfoFragment;
import com.milanac007.demo.im.fragment.SearchFragment;
import com.milanac007.demo.im.fragment.SelectGroupChatFragment;
import com.milanac007.demo.im.fragment.SessionListFragment;
import com.milanac007.demo.im.fragment.SetupFragment;
import com.milanac007.demo.im.fragment.SimpleZXingFragment;
import com.milanac007.demo.im.fragment.SingleChatSettingFragment;
import com.milanac007.demo.im.fragment.TransmitMsgFragment;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.permission.PermissionUtils;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.demo.im.ui.StatusBarUtil;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.IMUIHelper;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;
import com.milanac007.demo.im.utils.pinyin.PinYin;
import com.milanac007.scancode.QRCodeScanActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_CREATE_GROUP_CHAT;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_DELBUDDY;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_EXIT;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_GROUP_NAME_SETTING;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_GUARANTEEBUDDY;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_MODIFY_PERSONALINFO;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_RETURN;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_RETURN_IM;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SCAN_QRCODE;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SEARCH_BUDDY;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SELECT_BUDDY;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SELECT_GROUP_CHAT;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SEND_TEXT_MSG;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SEND_VOICE_MSG;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SHOW_CALL_DIALOG;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SHOW_QRCODE;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_SWITCH_SCREEN;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_TRANSMIT;
import static com.milanac007.demo.im.interfaces.OnActionListener.Action.ACTION_VIDEO_PLAY;

public class MainActivity extends BaseActivity implements OnActionListener, View.OnClickListener, PermissionUtils.PermissionGrantCallback {
    private Context mContext;
    private ImContaninerBinding mBinding;
    private ArrayList<BaseFragment> mFragments = new ArrayList<>();
    private BaseFragment currentFragment;
    private Class[] fragmentNames;

    public static final int SCREEN_SESSIONLIST = 0;
    public static final int SCREEN_CONTACTLIST = 1;
    public static final int SCREEN_APP = 2;
    public static final int SCREEN_MINE = 3;

    private boolean mSyncDataFinished = false; //标识远端数据是否取完
    private int mGuaranteebuddyId = 0;
    private int mDelbuddyId = 0;
    private int mSessionid = 0;
    private Map<Integer, Integer> mNewAddFriendRequestMap = new HashMap<>();  //当前新的好友添加请求个数
    private Logger logger = Logger.getLogger();
    private int isLogin = -1; //标记是否成功登陆过服务器
    private int screen = 0;
    private String mBuddyid;
    public static final int ARGUMENT_BUDDYID = 0;
    public static final int ARGUMENT_CALLSTATUS = 1;
    private static MainActivity instance = null;
    public static MainActivity getInstance(){
        return instance;
    }


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main2);
//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);
//    }


    @Override
    protected void customStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, getColor(R.color.color_f9f9f9_bg), 0);
            StatusBarUtil.setLightMode(this);
            updateNeedOffsetViewLayout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        instance = this;
        mBinding = ImContaninerBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        View[] tabViews = {mBinding.tabMessage, mBinding.tabContact, mBinding.tabApp, mBinding.tabMine,
            mBinding.layoutNeterr,
        };
        for(View tabView : tabViews){
            tabView.setOnClickListener(this);
        }

        initFragmentList();
        
        mFragments.add(SessionListFragment.newInstance());
        mFragments.add(ContactsFragmentNew.newInstance());
        mFragments.add(AppFragment.newInstance());
        mFragments.add(MineFragment.newInstance());

        MainFragmentPagerAdapter fragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mBinding.fragmentContainer.setAdapter(fragmentPagerAdapter);
        mBinding.fragmentContainer.setOffscreenPageLimit(1);

        mBinding.fragmentContainer.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
        setTabUI(R.id.tab_message);


        // 设置fragment调用activity的接口
        for (BaseFragment frag : mFragments) {
            frag.setOnActionListener(this);
        }

//        SystemConfigSp.instance().init(getApplicationContext());
//        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
//            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, URLConstants.ACCESS_MSG_ADDRESS);
//        }

        if(PermissionUtils.lacksPermission(mContext, PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE)){
            PermissionUtils.requestPermission(this, PermissionUtils.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE, PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE, this);
        }else {
            // 恢复用户信息
            if (Preferences.getCurrentLoginer() == null) {
                User.setImConfigUser(User.getUserLastLogin());
            }
        }
    }

    private void initFragmentList() {
        fragmentNames = new Class[] {
                SessionListFragment.class,
                ContactsFragmentNew.class,
                AppFragment.class,
                MineFragment.class,

                ChatFragment.class,
                PersonalInfoFragment.class,
                ModifyBuddyExtraFragment.class,
                ModifyPersonalInfoFragment.class,
                SingleChatSettingFragment.class,

                AddFriendFragment.class,
                MyFriendFragment.class,
                AddFriendVerifyMsgFragment.class,

                SimpleZXingFragment.class,
                ContactsBaseFragment.class,
                TransmitMsgFragment.class,
                SearchFragment.class,
                ModifyPwdFragment.class,
                MyDetailFragment.class,
                SetupFragment.class,
                AccountSecurityFragment.class,
                AboutOAFragment.class,
                NewMsgNofitySettingFragment.class,

                GroupChatMainFragment.class,
                GroupChatSettingFragment.class,
                CreateGroupChatFragment.class,
                SelectGroupChatFragment.class,
                GroupNameModifyFragment.class,
                GroupNoticeEditFragment.class,
                ModifyGroupMemFragment.class,
                GroupAdminFragment.class,
                GroupAdminModifyFragment.class,
                GroupIntroductFragment.class,

//                BuddyListFragment.class,
//                VideoPlayFragment.class,
        };
    }

    @Override
    public void onBackPressed() {
        if(mFragments.size()> 0){
            if(!mFragments.contains(currentFragment))
                currentFragment.onBack();
            else {
                Intent i= new Intent(Intent.ACTION_MAIN);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
            }
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CacheManager.getInstance().flushDiskCache();
    }

    public boolean isSyncDataFinished(){
        return mSyncDataFinished;
    }

    private void initData(){
        mSyncDataFinished = true;
        sendBroadcast(new Intent(ImAction.INTENT_SYNC_CONTACTS));

        updateUnReadMsgCountUI();
        if(mGuaranteebuddyId != 0 || mDelbuddyId  != 0){
            EventBus.getDefault().post(new AddUserChangeEvent());

            if(mGuaranteebuddyId != 0){
                if(imService.getLoginManager().getLoginId() == mGuaranteebuddyId){
                    if(mSessionid != 0){
                        createBuddyVerifyMsgToSession(0, mSessionid);    //系统消息
                        UserEntity userEntity = imService.getContactManager().findContact(mSessionid);
                        if(userEntity != null){
                            sendIM(userEntity.getSessionKey());
                            showToast("添加好友成功");
                        }
                    }
                }else{
                    if(mSessionid != 0){
                        createBuddyVerifyMsgToSession(1, mSessionid);    //系统消息
                    }
                }
            }else {
                if(imService.getLoginManager().getLoginId() == mDelbuddyId){
                    if(mSessionid != 0){
                        //TODO db 删session中的所有消息
                        String sessionKey = DBConstant.SESSION_TYPE_SINGLE + "_" + mSessionid;
                        imService.getSessionManager().reqRemoveSession(sessionKey);
                        OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_RETURN_IM, 0, null);
                        showToast("删除好友成功");
                    }
                }else {
                    //TODO
                    UserEntity userEntity = imService.getContactManager().findContact(mSessionid);
                    if(userEntity != null){
                        imService.getSessionManager().deleteAddBuddyAcceptVerifyMsgsBySessionId(mSessionid);
                    }
                }
            }

            mGuaranteebuddyId = 0;
            mDelbuddyId = 0;
            mSessionid = 0;
        }
    }

    private void sendIM(String extNo) {
        OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, extNo);
    }

    /**
     * 添加系统消息
     * @param buddyid
     */
    private  void createBuddyVerifyMsgToSession(int type, int buddyid) {

        // 将消息添加到本地会话列表
        // 创建会话sesssion，如果已有则是直接获得
        if (type == 0) {
            BuddyVerifyMessage buddyVerifyMessage = BuddyVerifyMessage.getAddBuddyVerifyMsgBySessionId(buddyid);
            if (buddyVerifyMessage != null) {
                TextMessage textMessage = new TextMessage();
                textMessage.setMsgType(DBConstant.MSG_TYPE_SINGLE_TEXT);
                long nowTime = System.currentTimeMillis();
                textMessage.setFromId(buddyVerifyMessage.getFromId());
                textMessage.setToId(buddyVerifyMessage.getToId());
                textMessage.setUpdated(nowTime);
                textMessage.setCreated(nowTime);
                textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
                textMessage.setContent(buddyVerifyMessage.getContent());

                int loginId = imService.getLoginManager().getLoginId();
                boolean isSend = textMessage.isSend(loginId);
                textMessage.buildSessionKey(isSend);

                textMessage.setStatus(MessageConstant.MSG_SUCCESS);
                MessageEntity.insertOrUpdateSingleData(textMessage);
            }

            UserEntity buddy = imService.getContactManager().findContact(buddyid);
            if (buddy != null) {
                TextMessage textMessage = new TextMessage();
                textMessage.setMsgType(DBConstant.MSG_TYPE_SINGLE_SYSTEM_TEXT);
                long nowTime = System.currentTimeMillis();
                //TODO
                textMessage.setFromId(buddyid);
                int loginId = imService.getLoginManager().getLoginId();
                textMessage.setToId(loginId);
                textMessage.setUpdated(nowTime);
                textMessage.setCreated(nowTime);
                textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
                // 内容的设定
                String content = String.format(DataConstants.MSG_TYPE_SHOWTEXT_SYS_ACCEPT_ADDBUDDY_FOR_ME, buddy.getMainName());
                textMessage.setContent(content);

                boolean isSend = textMessage.isSend(loginId);
                textMessage.buildSessionKey(isSend);

                textMessage.setStatus(MessageConstant.MSG_SUCCESS);
                IMMessageManager.instance().onRecvMessage(textMessage, false);
            }
        }else if (type == 1) {
            BuddyVerifyMessage buddyVerifyMessage = BuddyVerifyMessage.getVerifyMsgBySessionId(buddyid);
            if(buddyVerifyMessage != null){
                TextMessage textMessage = new TextMessage();
                textMessage.setMsgType(DBConstant.MSG_TYPE_SINGLE_TEXT);
                long nowTime = System.currentTimeMillis();
                textMessage.setFromId(buddyVerifyMessage.getFromId());
                textMessage.setToId(buddyVerifyMessage.getToId());
                textMessage.setUpdated(nowTime);
                textMessage.setCreated(nowTime);
                textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
                textMessage.setContent(buddyVerifyMessage.getContent());

                int loginId = imService.getLoginManager().getLoginId();
                boolean isSend = textMessage.isSend(loginId);
                textMessage.buildSessionKey(isSend);

                textMessage.setStatus(MessageConstant.MSG_SUCCESS);
                MessageEntity.insertOrUpdateSingleData(textMessage);
            }

            UserEntity buddy = imService.getContactManager().findContact(buddyid);
            if (buddy != null) {
                TextMessage textMessage = new TextMessage();
                textMessage.setMsgType(DBConstant.MSG_TYPE_SINGLE_SYSTEM_TEXT);
                long nowTime = System.currentTimeMillis();
                //TODO
                textMessage.setFromId(buddyid);
                int loginId = imService.getLoginManager().getLoginId();
                textMessage.setToId(loginId);
                textMessage.setUpdated(nowTime);
                textMessage.setCreated(nowTime);
                textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
                // 内容的设定
                String content = String.format(DataConstants.MSG_TYPE_SHOWTEXT_SYS_ACCEPT_ACCEPTBUDDY_FOR_ME, buddy.getMainName());
                textMessage.setContent(content);

                boolean isSend = textMessage.isSend(loginId);
                textMessage.buildSessionKey(isSend);

                textMessage.setStatus(MessageConstant.MSG_SUCCESS);
                IMMessageManager.instance().onRecvMessage(textMessage, false);
            }
        }

    }

	@Override
	public void onResume() {
		super.onResume();
		// 避免意外。。。
		if (currentFragment != null) {
			currentFragment.setOnActionListener(this);
		}

		updateUnReadMsgCountUI();
	}

//	// 切换/显示fragment,isBack表示是返回操作，isRemove表示不将当前fragment保存在返回的队列里（目前没用。。。）
//	public void switchFragment(BaseFragment fragment, boolean isBack, boolean isRemoveCurrent) {
//
//		if (fragment == null) {
////			fragment = fragments[0];
//			fragment = mFragments.get(0);
//		}
//
//		if (fragment.equals(currentFragment)) {
//			return;
//		}
//
//		fragment.setOnActionListener(this);
//
//		FragmentManager fm = getSupportFragmentManager();
//		FragmentTransaction transaction = fm.beginTransaction();
//		if (currentFragment != null) {
//			transaction.hide(currentFragment);
//			currentFragment.onBack();
//		}
//		if (!fragment.isAdded()) {
//			transaction.add(R.id.fragment_container, fragment);
//		} else {
//			transaction.hide(currentFragment);
//			transaction.show(fragment);
//			fragment.onResume();
//		}
//		transaction.commitAllowingStateLoss();
//
//		if (!isBack) {
//			if (isRemoveCurrent) {
//				fragment.setPreFragment(currentFragment.getPreFragment());
//			} else {
//				fragment.setPreFragment(currentFragment);
//			}
//		}
//		currentFragment = fragment;
//
//		screen = fragment.getPageNumber();
//		title.setText(currentFragment.getTitle());
//		if (currentFragment.canBack()) {
//			layout_back.setVisibility(View.VISIBLE);
//			// 返回按钮
//			layout_back.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					InputMethodManager imm = (InputMethodManager) getApplicationContext()
//							.getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.hideSoftInputFromWindow(getWindow().getDecorView()
//							.getWindowToken(), 0);
//					switchFragment(currentFragment.getPreFragment(), true,
//							false);
//				}
//
//			});
//			layout_portrait.setVisibility(View.INVISIBLE);
//		} else {
//			layout_back.setVisibility(View.GONE);
//			layout_portrait.setVisibility(View.VISIBLE);
//		}
//	}

    private void handleLogin() {
        if(ImConfig.user == null || TextUtils.isEmpty(Preferences.getRefreshToken())){
            loginProcess();
            return;
        }

        if (!Utils.isNetworkAvailable()) {
            showToast("网络似乎有问题，请打开网络");
//            imService.getLoginManager().login(ImConfig.user.userCode, Preferences.getSHA265Pwd());
            isLogin = 0;
        }else {
            imService.getLoginManager().login(ImConfig.user.userCode, Preferences.getSHA265Pwd());
            loginProcess();
        }

    }

    private void loginProcess() {
// TODO
//        final String refreshToken = Preferences.getRefreshToken();
//        if(Utils.isNetworkAvailable()&& ImConfig.user != null && !TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken)){
//            new GetVerifyTokenRequest(accessToken, refreshToken).request(new BaseClient.RequestHandler() {
//                @Override
//                public void onSuccess(Object object) {
//                    JSONObject input = (JSONObject)object;
//                    if(input != null){
//                        int result = input.getIntValue("result");
//                        if(result == 0){
//                            // TODO 登录成功
//                            logger.e("%s", "GetVerifyTokenRequest success !");
//                            String refresh_token = input.getString("refresh_token");
//                            if(!TextUtils.isEmpty(refresh_token)){
//                                Preferences.setRefreshToken(refreshToken);
//                            }
//                            String access_token = input.getString("access_token");
//                            if(!TextUtils.isEmpty(access_token)){
//                                Preferences.setAccessToken(access_token);
//                            }
//
//                            isLogin = 1;
//                            imService.getSocketMgr().reqMsgServerAddrs();
//
//                            long now = System.currentTimeMillis();
//                            long timeDiff = (now - CommonFunction.getLastCheckTime())/1000;
//                            if(timeDiff > ONE_DAY_SECOND) {
//                                CommonFunction.checkUpdate(instance, true);
//                            }
//
//                        }else {
//                            showToast("登录过期，请重新登录");
//                            Intent i = new Intent(ImContainer.this, LoginActivity.class);
//                            startActivity(i);
//                            finish();
//                        }
//                    }
//                }
//
//                @Override
//                public void onServerError(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//
//                }
//            });
//        } else {
//            if(ImConfig.user == null || TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(refreshToken)) {
////				Intent i = new Intent(ImContainer.this, LoginActivity.class);
////				startActivity(i);
////				finish();
//
//                ///TODO
//                layout_nologin.setVisibility(View.VISIBLE);
//                imService.onLocalOk();
//            }
//        }
    }

    @Override
    protected void onNetStateChanged(Context context, Intent intent) {
        logger.e("onNetStateChanged  Time %d", System.currentTimeMillis());

        boolean networkAvailable = Utils.isNetworkAvailable();
        logger.e("onNetStateChanged  ImUtils.isNetworkAvailable(): %s", networkAvailable?"true":"false");
        checkMyState(networkAvailable);
        if (!networkAvailable) {
//			checkMyState(false);
        }else {
            if(isLogin == 0) {
                isLogin = -1;
                loginProcess();
            }
        }

    }

    private void checkMyState(boolean online) {
        // ImUtils.isWIFIConnected() SDK未能正确反应通讯服务器连接情况 cbq 03-30 15:36
        // 包括BackgroundService.showNotification

        if(mBinding.layoutNeterr != null){
            mBinding.layoutNeterr.setVisibility(online ? View.GONE : View.VISIBLE);
        }

        if(currentFragment !=null)
            currentFragment.checkMyState(online);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().cancelAllMissedCallNotification();
        instance = null;
//        if(CustomDownloadManager.getInstance() != null){
//            CustomDownloadManager.getInstance().unregisterReceiver();
//        }

        if(imService != null) {
            imService.handleLoginout();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.i("MainActivity onNewIntent()");
    }

    protected void popBackStack(String tag){
        getSupportFragmentManager().popBackStack(tag, 0);
        currentFragment =  (BaseFragment) getFragment4Tag(tag);
        return;
    }

    @Override
    protected void addNewFragment(Class<? extends BaseFragment> aClass, String tag, Bundle bundle, FragmentTransaction transaction, int id, Fragment parent) {
        BaseFragment fragment;
        try {
            fragment = aClass.newInstance();
            fragment.setOnActionListener(this);
            currentFragment = fragment;
            screen = fragment.getPageNumber();

            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            transaction.add(id, fragment, tag);
            if (parent != null) {
                fragment.setTargetFragment(parent, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void OnAction(int page, int action, int code, String arg) {
        switch (action) {
            case ACTION_SWITCH_SCREEN:
                if(!TextUtils.isEmpty(arg)){
                    mBuddyid = arg;
                }

                if(code >=0 && code <=3){ //首页的4个 特殊处理
                    FragmentManager fm = getSupportFragmentManager();
                    int count = fm.getBackStackEntryCount();
                    if(count > 0){
                        FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
                        String className = entry.getName();
                        logger.e("%s", "popSelf(" + className +")");
                        currentFragment = (BaseFragment) getFragment4Tag(className);
                        currentFragment.popFragment(className);
                    }
                    if(mBinding.fragmentContainer.getCurrentItem() == code){
                        updateTabUi(mBinding.fragmentContainer.getCurrentItem());
                    }else {
                        mBinding.fragmentContainer.setCurrentItem(code);
                    }
                }else {
                    if(code == OnActionListener.Page.SCREEN_SEARCH){
                        Bundle bundle = new Bundle();
                        bundle.putInt("searchType", Integer.valueOf(arg));
                        showBaseFragment(fragmentNames[code], fragmentNames[code].getName(), true, bundle, R.anim.fade_in, R.anim.fade_out);
                    }else {
                        showBaseFragment(fragmentNames[code], fragmentNames[code].getName(), true, null);
                    }
                }
                break;
            case ACTION_RETURN://将当前的fragment pop出栈，并将回退栈的栈顶设为当前fragment,如果回退栈已为空，则设置tab 0为当前fragment
                //			switchFragment(currentFragment.getPreFragment(), true, false);
                currentFragment.popSelf();

                FragmentManager fm = getSupportFragmentManager();
                int count = fm.getBackStackEntryCount();
                if(count > 0){
                    FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(count-1);
                    String className = entry.getName();
                    logger.e("%s", "popSelf: "+ className);
                    currentFragment = (BaseFragment) getFragment4Tag(className);
                }else {
                    updateTabUi(mBinding.fragmentContainer.getCurrentItem()); //首页的4个fragment未放入回退栈
                }
                break;
            case ACTION_RETURN_IM:{ //清空回退栈所有fragment，并切到tab 0(消息页面)上
                fm = getSupportFragmentManager();
                count = fm.getBackStackEntryCount();
                if(count > 0){
                    if(count == 1){
                        currentFragment.popSelf();
                    }else {
                        FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
                        String className = entry.getName();
                        logger.e("%s", "popSelf(" + className +")");
                        currentFragment = (BaseFragment) getFragment4Tag(className);
                        currentFragment.popFragment(className);
                    }
                }
                if(mBinding.fragmentContainer.getCurrentItem() == 0){
                    updateTabUi(mBinding.fragmentContainer.getCurrentItem());
                }else {
                    mBinding.fragmentContainer.setCurrentItem(0);
                }

                logger.i("after pop, currentFragment: %s", currentFragment.getClass().getName());

            }break;
            case ACTION_SEND_TEXT_MSG:
                mBuddyid = arg;
                //			switchFragment(fragments[SCREEN_CHAT], false, false);
                //			switchFragment(mFragments.get(SCREEN_CHAT), false, false);
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("sessionKey", arg);
                    showBaseFragment(ChatFragment.class, ChatFragment.class.getName(), true, bundle);
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case ACTION_SEND_VOICE_MSG:{
                Bundle bundle = new Bundle();
                bundle.putInt("msgType", ACTION_SEND_VOICE_MSG);
                showBaseFragment(ChatFragment.class, ChatFragment.class.getName(), true, bundle);
            }break;
            case ACTION_EXIT:
                exit();
                break;
            case ACTION_GUARANTEEBUDDY:{
                if(!TextUtils.isEmpty(arg)) {
                    mGuaranteebuddyId = imService.getLoginManager().getLoginId();
                    mSessionid = Integer.valueOf(arg);
                    handleAcceptBuddy();
                }
            }break;
            case ACTION_DELBUDDY:{
                if(!TextUtils.isEmpty(arg)) {
                    mDelbuddyId = imService.getLoginManager().getLoginId();
                    mSessionid = Integer.valueOf(arg);
                    handleDelBuddy();
                }
            }break;
            case ACTION_SEARCH_BUDDY:{
                searchBuddyInfo(arg);
            }break;
            case ACTION_MODIFY_PERSONALINFO:{
                Bundle bundle = new Bundle();
                bundle.putString("type", arg);
                showBaseFragment(ModifyPersonalInfoFragment.class, ModifyPersonalInfoFragment.class.getName(), true, bundle);
            }break;

            case ACTION_SHOW_CALL_DIALOG:{
                showCallDialog(arg);
            }break;

            case ACTION_TRANSMIT:{
                Bundle bundle = new Bundle();
                bundle.putString("tag", arg);
                showBaseFragment(TransmitMsgFragment.class, TransmitMsgFragment.class.getName(), true, bundle);
            }break;

            case ACTION_SELECT_BUDDY:{
                Bundle bundle = new Bundle();
                bundle.putString("tag", arg);
                //TODO
//                showBaseFragment(SelectBuddyFragment.class, SelectBuddyFragment.class.getName(), true, bundle);
            }break;

            case ACTION_SELECT_GROUP_CHAT:{
                Bundle bundle = null;
                if(!TextUtils.isEmpty(arg)){
                    bundle = new Bundle();
                    bundle.putString("arg", arg);
                }
                showBaseFragment(SelectGroupChatFragment.class, SelectGroupChatFragment.class.getName(), true, bundle);
            }break;

            case ACTION_VIDEO_PLAY:{
                Bundle bundle = new Bundle();
                bundle.putString("path", arg);
//                showBaseFragment(VideoPlayFragment.class, VideoPlayFragment.class.getName(), true, bundle, R.anim.fade_in, R.anim.fade_out);
            }break;

            case ACTION_SHOW_QRCODE:{
                Bundle bundle = new Bundle();
                bundle.putString("sessionKey", arg);
                showBaseFragment(SimpleZXingFragment.class, SimpleZXingFragment.class.getName(), true, bundle);
            }break;

            case ACTION_GROUP_NAME_SETTING:{
                Bundle bundle = new Bundle();
                bundle.putString("sessionKey", arg);
                showBaseFragment(GroupNameModifyFragment.class, GroupNameModifyFragment.class.getName(), true, bundle);
            }break;

            case ACTION_SCAN_QRCODE:{
                Intent intent = new Intent();
                intent.setClass(this, QRCodeScanActivity.class);
                startActivityForResult(intent, QRCodeScanActivity.QRCODE_MASK);
            }break;

            case ACTION_CREATE_GROUP_CHAT:{
                Bundle bundle = null;
                if(!TextUtils.isEmpty(arg)){
                    bundle = new Bundle();
                    bundle.putString("param", arg);
                }
                showBaseFragment(CreateGroupChatFragment.class, CreateGroupChatFragment.class.getName(), true, bundle);
            }break;
        }
    }

    private void handleAcceptBuddy(){
        //同步好友列表
        //切换到im页面
        //添加好友成功
        logger.e("%s", "GetContactsRequest success.");
        imService.getContactManager().reqGetAllUsers();
    }

    private void handleDelBuddy(){
        logger.e("%s", "GetContactsRequest success.");
        imService.getContactManager().reqGetAllUsers();
    }

    private void handleScanQRCode(String sessionKey){
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        if(sessionInfo == null || sessionInfo.length == 1){
            showToast("二维码识别无效");
            return;
        }
        int peerType = Integer.parseInt(sessionInfo[0]);
        final int peerId = Integer.parseInt(sessionInfo[1]);

        CommonFunction.showProgressDialog(this, "搜索中...");

        List<Integer> needFetchUserList = new ArrayList<>(1);
        if(peerType == DBConstant.SESSION_TYPE_SINGLE){
            //本地搜
            UserEntity imBuddy = imService.getContactManager().findContact(peerId);
            if(imBuddy != null && !imBuddy.isFake()){
                CommonFunction.dismissProgressDialog();
                OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, imBuddy.getPeerId() + "");
                return;
            } else if (imBuddy == null) {
                imBuddy = new UserEntity();
                imBuddy.setFake(true);
                imBuddy.setPeerId(peerId);
                imBuddy.setMainName("");
                imBuddy.setFriend(false);
                PinYin.getPinYin(imBuddy.getMainName(), imBuddy.getPinyinElement());
                imBuddy.setPinyinName(imBuddy.getPinyinElement().pinyin);

                IMContactManager.instance().putContact(imBuddy);
                UserEntity.insertOrUpdateSingleData(imBuddy);
                needFetchUserList.add(peerId);
            } else if(imBuddy.isFake()){
                needFetchUserList.add(peerId);
            }

            imService.getContactManager().reqGetDetaillUsers(needFetchUserList, new Packetlistener() {
                @Override
                public void onSuccess(Object response) {
                    CommonFunction.dismissProgressDialog();

                    JSONObject rspObject = JSONObject.parseObject((String) response);
                    int resultCode = rspObject.getIntValue("resultCode");
                    if(resultCode != 0) {

                    }else {
                        int loginId = rspObject.getIntValue("UserId");
                        JSONArray userInfoListJSONArray = rspObject.getJSONArray("UserInfoList");
                        ArrayList<UserEntity>  dbNeed = new ArrayList<>();
                        for (int i=0; i<userInfoListJSONArray.size(); i++) {
                            JSONObject userInfoJsonObject = userInfoListJSONArray.getJSONObject(i);
                            Gson gson = new Gson();
                            UserEntity userEntity = gson.fromJson(userInfoJsonObject.toJSONString(), UserEntity.class);
                            PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                            userEntity.setPinyinName(userEntity.getPinyinElement().pinyin);

                            UserEntity contact = IMContactManager.instance().findContact(userEntity.getPeerId());
                            if(contact != null){
                                if(contact.isFake()){
                                    userEntity.setFriend(contact.isFriend());
                                }else {
                                    userEntity.setFriend(true);
                                }

                                IMContactManager.instance().putContact(userEntity);
                                dbNeed.add(userEntity);
                            }
                        }

                        // 负责userMap
                        UserEntity.insertOrUpdateMultiData(dbNeed);
                        OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, peerId + "");
                    }
                }

                @Override
                public void onTimeout() {
                    CommonFunction.showToast("联网超时，请检查网络");
                    CommonFunction.dismissProgressDialog();
                }

                @Override
                public void onFail(String error) {
                    CommonFunction.showToast(error);
                    CommonFunction.dismissProgressDialog();
                }
            });

        } else {
            GroupEntity groupEntity = imService.getGroupManager().findGroup(peerId);
            if(groupEntity != null){
                CommonFunction.dismissProgressDialog();
                if(groupEntity.isFake() || !groupEntity.getlistGroupMemberIds().contains(imService.getLoginManager().getLoginId())){//TODO 如果自己已被踢出群，则需要重新加入
                    OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_INTRODUCTION, peerId+"");
                }else {
                    sendIM(sessionKey);
                }
                return;
            }

            imService.getGroupManager().reqGroupDetailInfo(peerId, new Packetlistener() {
                @Override
                public void onSuccess(Object response) {
                    CommonFunction.dismissProgressDialog();
                    JSONObject rspObject = JSONObject.parseObject((String) response);
                    int resultCode = rspObject.getIntValue("resultCode");
                    if(resultCode != 0) {

                    }else {
                        JSONObject groupInfoListRsp = rspObject.getJSONObject("GroupInfoListRsp");
                        JSONArray groupInfos = groupInfoListRsp.getJSONArray("GroupInfoList");
                        if (groupInfos != null && !groupInfos.isEmpty()) {
                            JSONObject group = groupInfos.getJSONObject(0);
                            Gson gson = new Gson();
                            GroupEntity groupEntity = gson.fromJson(group.toJSONString(), GroupEntity.class);
                            groupEntity.setFake(true);//不存入db
                            imService.getGroupManager().getGroupMap().put(peerId, groupEntity);
                            OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_INTRODUCTION, peerId+"");
                        }
                    }
                }

                @Override
                public void onTimeout() {
                    CommonFunction.showToast("联网超时，请检查网络");
                    CommonFunction.dismissProgressDialog();
                }

                @Override
                public void onFail(String error) {
                    CommonFunction.showToast(error);
                    CommonFunction.dismissProgressDialog();
                }
            });
        }
    }

    private void searchBuddyInfo(String searchStr){
        if(CommonFunction.isStringEmpty(searchStr)){
            CommonFunction.showToast("内容不能为空");
            return;
        }

        boolean isIMAccount = CommonFunction.isIMAccount(searchStr);
        boolean isMobileNum = CommonFunction.isMobile(searchStr);

        if(!isIMAccount && !isMobileNum){
            CommonFunction.showToast("搜索内容无效");
            return;
        }

        CommonFunction.showProgressDialog(this, "搜索中...");

        if(isIMAccount){
            UserEntity imBuddy = imService.getContactManager().findContactByUserCode(searchStr);
            if(imBuddy != null){
                CommonFunction.dismissProgressDialog();
                OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, imBuddy.getPeerId() + "");
                return;
            }
        }else  if(isMobileNum){
            UserEntity imBuddy = imService.getContactManager().findContactByPhone(searchStr);
            if(imBuddy != null){
                CommonFunction.dismissProgressDialog();
                OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, imBuddy.getPeerId() + "");
                return;
            }
        }

        imService.getContactManager().ReqSearchBuddy(searchStr, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                CommonFunction.dismissProgressDialog();
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int searchId = imService.getContactManager().onRepSearchBuddy(rspObject);
                if(searchId > 0){
                    OnAction(currentFragment.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, searchId + "");
                }
            }

            @Override
            public void onTimeout() {
                CommonFunction.dismissProgressDialog();
            }

            @Override
            public void onFail(String error) {
                CommonFunction.dismissProgressDialog();
            }
        });

    }


    @Override
    public Bundle getArguments(int page, int arguments) {
        Bundle bundle = new Bundle();

        switch (arguments) {
            case ARGUMENT_BUDDYID:
                bundle.putString(DataConstants.BUDDY_ID, mBuddyid);
                break;
        }
        return bundle;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        switch (requestCode){
            case PermissionUtils.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // 恢复用户信息
                if (Preferences.getCurrentLoginer() == null) {
                    User.setImConfigUser(User.getUserLastLogin());
                }
            }break;
//            case 3:{
//                if(currentFragment instanceof MobileContactFragment){
//                    MobileContactFragment mobileContactFragment = (MobileContactFragment)currentFragment;
//                    mobileContactFragment.setData();
//                }
//
//            }break;
            case 4:{
                if(currentFragment instanceof SessionListFragment){

                }
            }break;
//            case 5:{
//                CommonFunction.downloadUpdate();
//            }break;
        }
    }

    @Override
    public void onPermissionDenied(int requestCode, String err) {
        int code1 = requestCode;
        String err1 = err;
    }

    @Override
    public void onError(String error) {

    }

    private boolean is_initiator;
    private int currentExtNo = -1;
    private void call(int extNo, boolean is_initiator) {
        showToast("功能开发中");
    }

    private void callVideo(int extNo, boolean is_initiator){
        showToast("功能开发中");
    }

    // TODO
    public void exit() {

//        NotificationManager notifymanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notifymanager.cancelAll();
//        Preferences.setUser(null);
//        Preferences.setPassword("");
//
//        SessionListFragment.newInstance().destoryInstance();
//        ContactsFragmentNew.newInstance().destoryInstance();
//        AppFragment.newInstance().destoryInstance();
//        MineFragment.newInstance().destoryInstance();
//
//        CommonFunction.reset();
//        CacheManager.getInstance().closeDiskCache();
//        Preferences.setAccessToken("");
//        Preferences.setRefreshToken("");
//        FeedbackManager.getInstance().cleanInstanceData();
//
//        if(layout_nologin.getVisibility() ==View.VISIBLE){
////			User.setImConfigUser(null);
//        }else {
//            ExitRequest request = new ExitRequest();
//            request.request(new RequestHandler() {
//
//                @Override
//                public void onSuccess(Object object) {
//                    BaseClient.destoryBaseClient();
////					User.setImConfigUser(null);
//                }
//
//                @Override
//                public void onServerError(int arg0, Header[] arg1, byte[] arg2,
//                                          Throwable arg3) {
//                }
//
//            });
//
//            IMLoginManager.instance().setKickout(false);
//            IMLoginManager.instance().logOut();
//
//            Preferences.setBuddyListLastUpdateTime(0);
//            Preferences.setIsLoginServer(false);
//            if (ImUtils.isNetworkAvailable() && PreferencesService.getIsLoginSipServer()) {
//                LocalLogicSend.logout();
//            }
//        }
//
//        Intent i = new Intent(App.getInstance(), LoginActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_message:
            case R.id.tab_contact:
            case R.id.tab_app:
            case R.id.tab_mine:
                setCurrentFragment(v.getId());
                break;

            case R.id.layout_neterr:{
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }break;
            default:
                break;
        }
    }

    private void setCurrentFragment(int viewid){
        ViewPager fragment_container = mBinding.fragmentContainer;
        switch (viewid) {
            case R.id.tab_message:
                fragment_container.setCurrentItem(0);
                break;
            case R.id.tab_contact:
                fragment_container.setCurrentItem(1);
                break;
            case R.id.tab_app:
                fragment_container.setCurrentItem(2);
                break;
            case R.id.tab_mine:
                fragment_container.setCurrentItem(3);
                break;
            default:
                break;
        }
    }

    private void setTabUI(int id){

        Drawable drawable = getResources().getDrawable(R.mipmap.tab_message_default);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mBinding.tabMessage.setCompoundDrawables(null, drawable, null, null);
        mBinding.tabMessage.setTextColor(Color.parseColor("#c0c0c0"));

        drawable = getResources().getDrawable(R.mipmap.tab_app_default);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mBinding.tabApp.setCompoundDrawables(null, drawable, null, null);
        mBinding.tabApp.setTextColor(Color.parseColor("#c0c0c0"));

        drawable = getResources().getDrawable(R.mipmap.tab_contacts_default);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mBinding.tabContact.setCompoundDrawables(null, drawable, null, null);
        mBinding.tabContact.setTextColor(Color.parseColor("#c0c0c0"));

        drawable = getResources().getDrawable(R.mipmap.tab_my_default);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mBinding.tabMine.setCompoundDrawables(null, drawable, null, null);
        mBinding.tabMine.setTextColor(Color.parseColor("#c0c0c0"));

        switch (id) {
            case R.id.tab_message:
                drawable = getResources().getDrawable(R.mipmap.tab_message_selected);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mBinding.tabMessage.setCompoundDrawables(null, drawable, null, null);
                mBinding.tabMessage.setTextColor(Color.parseColor("#a4bde8"));
                currentFragment = mFragments.get(0);
                refreshMsgListUI();
                break;
            case R.id.tab_contact:
                drawable = getResources().getDrawable(R.mipmap.tab_contacts_selected);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mBinding.tabContact.setCompoundDrawables(null, drawable, null, null);
                mBinding.tabContact.setTextColor(Color.parseColor("#a4bde8"));
                currentFragment = mFragments.get(1);
                break;
            case R.id.tab_app:
                drawable = getResources().getDrawable(R.mipmap.tab_app_selected);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mBinding.tabApp.setCompoundDrawables(null, drawable, null, null);
                mBinding.tabApp.setTextColor(Color.parseColor("#a4bde8"));
                currentFragment = mFragments.get(2);
                break;
            case R.id.tab_mine:
                drawable = getResources().getDrawable(R.mipmap.tab_my_selected);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mBinding.tabMine.setCompoundDrawables(null, drawable, null, null);
                mBinding.tabMine.setTextColor(Color.parseColor("#a4bde8"));
                currentFragment = mFragments.get(3);
                break;
            default:
                break;
        }
        screen = currentFragment.getPageNumber();
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageSelected(int index) {
            updateTabUi(index);
        }

    }

    private void updateTabUi(int index){
        switch (index) {
            case 0:
                setTabUI(R.id.tab_message);
                break;
            case 1:
                setTabUI(R.id.tab_contact);
                break;
            case 2:
                setTabUI(R.id.tab_app);
                break;
            case 3:
                setTabUI(R.id.tab_mine);
                break;
        }
    }


    public void onReceiveMsg(Bundle bundle){

    }

//    @Override
//    protected boolean onReceiveServerMsg(Bundle bundle) {
//
//        LocalTransferData data = bundle.getParcelable(DataConstants.DATA);
//
//        switch (data.mType) {
//            case DataConstants.DATA_TYPE_LOGIN:
//                if (Preferences.getIsLoginServer() && ImConfig.user != null) {
////					CommonFunction.showToast("sip login success");
//                }
//                break;
//
//            case DataConstants.DATA_CHECK_STATE:
//                String state = data.mContent.get("state");
//                Dialog alertDialog = new AlertDialog.Builder(this).setTitle("State").setMessage(state).setIcon(R.drawable.ic_launcher).create();
//                alertDialog.show();
//                break;
//            default:break;
//
//        }
//
//        return super.onReceiveServerMsg(bundle);
//    }


    public int getNewAddFriendRequestSum(){
        int sum = 0;
        for(Integer i : mNewAddFriendRequestMap.values()){
            sum += i;
        }
        return sum;
    }

    public void setNewFriendTip(int buddyid, int visibility){
        if(visibility == View.VISIBLE){
            mNewAddFriendRequestMap.put(buddyid, 1);
        }else {
            mNewAddFriendRequestMap.clear();
        }
        mBinding.contactNewTip.setVisibility(visibility);
    }

    public  void updateUnReadMsgCountUI(){
        if(imService == null)
            return;

        IMUnreadMsgManager unreadMsgManager =imService.getUnReadMsgManager();
        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        logger.d("unread#total cnt %d", totalUnreadMsgCnt);

        if(totalUnreadMsgCnt <=0){
            mBinding.msgNewTip.setText("0");
            mBinding.msgNewTip.setVisibility(View.INVISIBLE);
        }else {
            mBinding.msgNewTip.setText("" + totalUnreadMsgCnt);
            mBinding.msgNewTip.setVisibility(View.VISIBLE);
        }
    }

    private void refreshMsgListUI(){
        SessionListFragment.newInstance().reloadData();
        updateUnReadMsgCountUI();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case QRCodeScanActivity.QRCODE_MASK:{
                if(resultCode == 200) { //success
                    final String result = data.getStringExtra("code");
                    if(!TextUtils.isEmpty(result)){
                        logger.e("QRCodeScanActivity result: %s", result);
                        if (result.startsWith("http://") || result.startsWith("https://")) {
                            Uri uri = Uri.parse(result);
                            Intent returnIt = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(returnIt);
                        }else {
                            new HandlerPost(200){ //保证QRCodeScanActivity已经销毁
                                @Override
                                public void doAction() {
                                    handleScanQRCode(result);
                                }
                            };
                        }
                    }

                }
            }break;
            default:
                break;
        }
    }


    private String[] getRightMenus() {
        String[] menuStr = {"语音电话", "视频电话"};
        return menuStr;
    }

    OperateListDialog operateListDialog;
    private ArrayList<OperateListDialog.OperateItem> operateItems  = new ArrayList<>();

    private void showCallDialog(final String buddyIdStr) {
        final int buddyId = Integer.valueOf(buddyIdStr);

        final String[] menuStr = getRightMenus();
        if (menuStr == null || menuStr.length <= 0) {
            return;
        }

        if(operateListDialog == null) {
            operateListDialog = new OperateListDialog(this);
            operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
        }
        operateItems.clear();


        int size = menuStr.length;
        for (int i = 0; i< size; i++) {
            final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
            item.setmItemNameStr(menuStr[i]);
            item.setmOperateKey(String.valueOf(i));

            item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
                @Override
                public void clickItem(int position) {
                    switch (Integer.valueOf(item.getmOperateKey())) {
                        case 0: {
                            call(buddyId, true);
                        }
                        break;
                        case 1: {
                            callVideo(buddyId, true);
                        }
                        break;
                        default:
                            break;
                    }

                    if (operateListDialog != null) {
                        operateListDialog.dismiss();
                    }
                }
            });
            operateItems.add(item);
        }

//		operateListDialog.setTitle("请选择");
        operateListDialog.showTitle(false);
        operateListDialog.setGravityType(1);
        operateListDialog.updateOperateItems(operateItems);
        operateListDialog.show();
    }

    private Handler uiHandler = new Handler();
    private boolean autoLogin = true;
    private boolean loginSuccess = false;

    @Override
    public void onIMServiceConnected() {
        logger.d("login#onIMServiceConnected");

        //viewPager初始加载的两个页面，在onResume()时，imService可能还为空，这里再保证一下。
        SessionListFragment.newInstance().setImService(imService);
        ContactsFragmentNew.newInstance().setImService(imService);
        handleLogin(); // TODO
    }

    private void jumpToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * ----------------------------event 事件驱动----------------------------
     */

    public void onEventMainThread(BuddyVerifyEvent event){
        switch (event.type){
            case BuddyVerifyEvent.ADD_BUDDY_REQUEST:{
                setNewFriendTip(event.buddyid, View.VISIBLE);
                ContactsFragmentNew.newInstance().refreshNewAddFriendRequestUI();
            }break;
            case BuddyVerifyEvent.ADD_BUDDY_ACCEPT:{
                mGuaranteebuddyId = event.buddyid;
                mSessionid = event.buddyid;
                handleAcceptBuddy();
            }break;
            case BuddyVerifyEvent.DELBUDDY_REQUEST:{
                mDelbuddyId = event.buddyid;
                mSessionid = event.buddyid;
                handleDelBuddy();
            }break;
        }
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event.event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                initData();
                break;
        }
    }
//
    public void onEventMainThread(LoginEvent event) {
        switch (event.getType()) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
            case LOCAL_LOGIN_MSG_SERVICE: //中途断开连接服务器又连接上
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
        }
    }

    private void relogin(){
//        new GetAuthCodeRequest(imService.getLoginManager().getLoginUserCode(), imService.getLoginManager().getLoginPwd(), true).request(new RequestHandler() {
//            @Override
//            public void onSuccess(Object object) {
//                JSONObject outJsonObject = (JSONObject)object;
//                int result = outJsonObject.getIntValue("result");
//                if(result == 0){
//                    String authCode = outJsonObject.getString("authCode");
//                    new GetCreateTokenRequest(authCode).request(new RequestHandler() {
//                        @Override
//                        public void onSuccess(Object object) {
//                            JSONObject output = (JSONObject)object;
//                            int result = output.getInteger("result");
//                            //TODO
//                            if(result == 0){
//                                String refresh_token = output.getString("refresh_token");
//                                String access_token = output.getString("access_token");
//
//                                Preferences.setAccessToken(access_token);
//                                Preferences.setRefreshToken(refresh_token);
//
//                                isLogin = 1;
//                                IMLoginManager.instance().relogin();
//
//                            }else {
//                                final String error = output.getString("error");
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        CommonFunction.showToast(error);
//                                        jumpToLoginPage();
//                                    }
//                                });
//
//                                Preferences.setIsLoginServer(false);
//                                Preferences.setUser(null);
//                            }
//                        }
//
//                        @Override
//                        public void onServerError(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//                            CommonFunction.showToast("网络连接异常，请稍后再试");
//                            Preferences.setIsLoginServer(false);
//                            Preferences.setUser(null);
//                            jumpToLoginPage();
//                        }
//                    });
//
//                }else {
//                    String errorStr = outJsonObject.getString("error");
//                    if(result == 1){
//                        if(outJsonObject.getString("errorCode").equals("60000")){
//                            errorStr = "用户名或密码错误";
//                        }else {
//                            errorStr = String.format("登录失败：%s", errorStr);
//                        }
//                    }
//
//                    final String error = errorStr;
//                    CommonFunction.showToast(error);
//                    Preferences.setIsLoginServer(false);
//                    Preferences.setUser(null);
//                    jumpToLoginPage();
//                }
//            }
//
//            @Override
//            public void onServerError(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//                CommonFunction.showToast("网络连接异常，请稍后再试");
//                Preferences.setIsLoginServer(false);
//                Preferences.setUser(null);
//                jumpToLoginPage();
//            }
//        });
    }

    private void handleServerDisconnected() {

//        if(imService != null){
//            if(imService.getLoginManager().isKickout()){
//                CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(instance);
//                builder.setTitle("下线提醒");
//                builder.setMessage(R.string.disconnect_kickout);
//                builder.setPositiveBtn("重新登录", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        if(Utils.isNetworkAvailable()){
//                            relogin();
//                        }else{
//                            CommonFunction.showToast( getString(R.string.no_network_toast)+"\n请确认网络连接后重新登录");
//                            jumpToLoginPage();
//                            return;
//                        }
//                    }
//                });
//
//                builder.setNegativeBtn("退出", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        App.getInstance().exitApp();
//                    }
//                });
//
//                CustomConfirmDialog d = builder.create();
//                d.setCancelable(false);
//                if(instance != null){
//                    d.show();
//                }
//
//            }else{
//
//            }
//        }

    }

    public void onEventMainThread(SocketEvent event) {
        switch (event.event) {
            case MSG_SERVER_DISCONNECTED:
                checkMyState(false);
                if(event.disconnected_reason == MSG_SERVER_DISCONNECTED_REASON.KICKOUT){
                    handleServerDisconnected();
                }break;

            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                checkMyState(false);
                if (!loginSuccess)
                    onSocketFailure(event);
                break;
        }
    }

    public void onEventMainThread(UnreadEvent event){
        switch (event.event){
            case UNREAD_MSG_RECEIVED:
            case UNREAD_MSG_LIST_OK:
                refreshMsgListUI();
                break;
        }
    }

    public void onEventMainThread(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        switch (type) {
            case HISTORY_MSG_OBTAIN: {
                refreshMsgListUI();
            }
            break;
        }
    }

    private void onLoginSuccess() {
        logger.e("%s", "login#onLoginSuccess");
        loginSuccess = true;
        checkMyState(true);
    }

    private void onLoginFailure(LoginEvent event) {
        logger.e("login#onLoginError -> errorCode:%s", event.getType().name());
        jumpToLoginPage();
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        logger.e("login#errorTip:%s", errorTip);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("login#onLoginError -> errorCode:%s,", event.event.name());
        jumpToLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.e("login#errorTip:%s", errorTip);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }


}