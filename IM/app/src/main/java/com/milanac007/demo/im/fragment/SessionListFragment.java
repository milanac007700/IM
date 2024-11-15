/******************************
 * 聊天列表
 ***********************************/

package com.milanac007.demo.im.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.BaseActivity;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.adapter.SessionListAdapter;
import com.milanac007.demo.im.db.config.ImAction;
import com.milanac007.demo.im.db.entity.RecentInfo;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.event.AddUserChangeEvent;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.SessionEvent;
import com.milanac007.demo.im.event.UnreadEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.permission.PermissionUtils;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.demo.im.ui.OperateListPop;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;


import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SessionListFragment extends LazerLoadFragment implements OnClickListener {
    private static SessionListFragment newInstance = null;
    private ListView mListView;
    private SessionListAdapter mAdapter = null;
    private View new_tip;
    private TextView mFinish;

    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private boolean isSyncData = false;
    private OperateListPop mOperateListPop;
    private TextView mSearchBtn;
    private View ll_no_data;

    public static SessionListFragment newInstance() {
        if (newInstance == null) {
            newInstance = new SessionListFragment();
        }
        return newInstance;
    }

    public static boolean instanceExist() {
        return newInstance != null;
    }

    public void destoryInstance() {
        if (newInstance != null) {
            newInstance = null;
        }
    }

    @Override
    public void onIMServiceConnected() {
        super.onIMServiceConnected();
        if(getActivity() == null) {
            Log.e(TAG(), "onIMServiceConnected(), getActivity() == null, return.");
            return;
        }

        mAdapter = new SessionListAdapter(getActivity(), imService);
        mListView.setAdapter(mAdapter);
        isPrepared = true;

        lazyLoad();
    }

    @Override
    public void setImService(IMService imService) {
        if(imService != null) {
            this.imService = imService;
            onIMServiceConnected();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(ImAction.INTENT_SYNC_CONTACTS);
        getActivity().registerReceiver(syncReceiver, filter);
        if(PermissionUtils.lacksPermission(getActivity(), PermissionUtils.PERMISSION_READ_CONTACTS)){
            PermissionUtils.requestPermission(getActivity(), 4, PermissionUtils.PERMISSION_READ_CONTACTS, MainActivity.getInstance());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(syncReceiver);
        //关闭对话框
        CommonFunction.dismissProgressDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sessionlist_activity, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.fragment_head1_back).setVisibility(View.GONE);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        setTitle(mTitle);

        mSearchBtn = (TextView) view.findViewById(R.id.fragment_head1_finish2);
        mSearchBtn.setVisibility(View.GONE); //TODO 体验不好，待优化
        Drawable drawable = getResources().getDrawable(R.mipmap.bg_search);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSearchBtn.setCompoundDrawables(drawable, null, null, null);

        mFinish = (TextView) view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        drawable = getResources().getDrawable(R.mipmap.bg_add_sign);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);

        mListView = (ListView) view.findViewById(R.id.listsession);
        int showDividers = CommonFunction.dip2px(0.5f);
        mListView.setDividerHeight(showDividers);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO 取消对应的通知栏消息， 这里暂时都取消了
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                RecentInfo session = (RecentInfo) mAdapter.getItem(position);
                int sessionType = session.getSessionType();
                // 跳转至聊天窗口
                startChat(session.getSessionKey());
            }

        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                RecentInfo session = (RecentInfo) mAdapter.getItem(position);
                int sessionType = session.getSessionType();
                showDialog(session);
                return true;
            }
        });

        if(mAdapter != null) {
            mListView.setAdapter(mAdapter);
        }

        ll_no_data = view.findViewById(R.id.ll_no_data);
        TextView no_data_view = view.findViewById(R.id.no_data_view);
        no_data_view.setText("暂无聊天记录");

        View[] clickViews = {mSearchBtn, mFinish};
        for(View view1 : clickViews){
            view1.setOnClickListener(this);
        }
    }


    OperateListDialog operateListDialog;
    private ArrayList<OperateListDialog.OperateItem> operateItems = new ArrayList<>();
    public void showDialog(final RecentInfo recentInfo) {

        List<String> menuStr = new ArrayList<>();
        menuStr.add("删除该聊天");

        if (operateListDialog == null) {
            operateListDialog = new OperateListDialog(getActivity());
            operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
        }
        operateItems.clear();

        int size = menuStr.size();
        for (int i = 0; i< size; i++) {
            final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
            item.setmItemNameStr(menuStr.get(i));
            item.setmOperateKey(String.valueOf(i));

            item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
                @Override
                public void clickItem(int position) {
                    String itemStr = item.getmItemNameStr();
                    if("删除该聊天".equals(itemStr)){
                        CommonFunction.showProgressDialog(getActivity(), "请稍候...");
                        imService.getSessionManager().reqRemoveSession(recentInfo);
                        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN_IM, 0, null);//直接回到消息列表
                        CommonFunction.dismissProgressDialog();
                    }

                    if (operateListDialog != null) {
                        operateListDialog.dismiss();
                    }
                }
            });
            operateItems.add(item);
        }

        operateListDialog.showTitle(false);
        operateListDialog.setGravityType(0); //居中显示
        operateListDialog.updateOperateItems(operateItems);
        operateListDialog.show();
    }


    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getLogger().e("%s", "INTENT_SYNC_CONTACTS");
            if(intent.getAction().equals(ImAction.INTENT_SYNC_CONTACTS)){
                loadData();
            }
        }
    };

    private void loadData(){
        //关闭对话框
        CommonFunction.dismissProgressDialog();
        reloadData();
        mHasLoadedOnce = true;
    }

    public void reloadData() {
        onRecentContactDataReady();
    }

    @Override
    protected void lazyLoad() {
        if(Preferences.getCurrentLoginer() == null)
            return;

        if (!isPrepared || !isVisible) {
            return;
        }

        reloadData();

//        if (!isPrepared || !isVisible || mHasLoadedOnce) {
//            reloadData();
//            return;
//        }
//
//        //显示加载进度对话框
//        //关闭对话框
////        CommonFunction.showProgressDialog(mActivity, "加载中");
//
//        //TODO 可能不需要
//        if(MainActivity.getInstance().isSyncDataFinished()){
//            loadData();
//        }
    }


    public void onEventMainThread(SessionEvent sessionEvent){
        switch (sessionEvent){
            case RECENT_SESSION_LIST_UPDATE:
            case RECENT_SESSION_LIST_SUCCESS:
            case SET_SESSION_TOP:
                onRecentContactDataReady();
                break;
        }
    }

    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()){
            case GROUP_INFO_OK:
            case CHANGE_GROUP_MEMBER_SUCCESS:
                onRecentContactDataReady();
                break;

            case GROUP_INFO_UPDATED:
                onRecentContactDataReady();
                break;
        }
    }

    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case UNREAD_MSG_RECEIVED: {
                onRecentContactDataReady();
            }break;
        }
    }


    // 依赖联系人会话、未读消息、用户的信息三者的状态
    private void onRecentContactDataReady(){
        if(mAdapter == null)
            return;

        boolean isUserData = imService.getContactManager().isUserDataReady();
        boolean isGroupData =  imService.getGroupManager().isGroupReady();
        boolean isSessionData = imService.getSessionManager().isSessionListReady();
        if ( !(isUserData&&isSessionData&&isGroupData)) {
            return;
        }

        ((MainActivity) getActivity()).updateUnReadMsgCountUI();
        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();
        mAdapter.updateList(recentSessionList);

        if (recentSessionList == null || recentSessionList.isEmpty()) {
            ll_no_data.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            ll_no_data.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }

    }


    public void onEventMainThread(AddUserChangeEvent event) {
        reloadData();
    }

    private void onFinishClick() {

        if (mOperateListPop == null) {
            mOperateListPop = new OperateListPop(CommonFunction.getWidthPx() * 2 / 5, getActivity(), getResources().getDrawable(R.mipmap.bg_menu));
            ArrayList<OperateListPop.PopItem> popItems = new ArrayList<>();
            final OperateListPop.PopItem popItem = mOperateListPop.new PopItem(R.mipmap.menu_item_add_friend, "添加朋友", new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOperateListPop.dismiss();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ADDFRIEND, null);
                }
            });
            popItems.add(popItem);

            final OperateListPop.PopItem popItem2 = mOperateListPop.new PopItem(R.mipmap.menu_item_group_chat, "群组对话", new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_CREATE_GROUP_CHAT, 0, null);
                    mOperateListPop.dismiss();
                }
            });
            popItems.add(popItem2);

            final OperateListPop.PopItem popItem3 = mOperateListPop.new PopItem(R.mipmap.menu_item_scan, "扫一扫", new OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickScanQRCodeBtn();
                    mOperateListPop.dismiss();

                }
            });
            popItems.add(popItem3);

            mOperateListPop.setData(popItems);
        }
        mOperateListPop.show(mFinish, 0, -CommonFunction.dip2px(6f));
    }

    private void  clickScanQRCodeBtn(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SCAN_QRCODE, 0, null);
    }

    private void onClickSearch() {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SEARCH, "1");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_head1_finish2: {
                onClickSearch();
            }
            break;
            case R.id.fragment_head1_finish: {
                onFinishClick();
            }
            break;
            default:
                break;
        }
    }

    protected void setTitle(TextView mTitle) {
        mTitle.setTextSize(18);
        mTitle.setText(getResources().getString(R.string.g_msg));
    }


    // 跳转至聊天窗口
    private void startChat(String buddyid) {
        this.mListener.OnAction(this.getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, buddyid);
    }

    @Override
    public int getPageNumber() {
        return MainActivity.SCREEN_SESSIONLIST;
    }

}
