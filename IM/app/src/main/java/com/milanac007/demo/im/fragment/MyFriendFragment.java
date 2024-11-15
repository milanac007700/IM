package com.milanac007.demo.im.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.DB.DBInterface;
//import com.mogujie.tt.protobuf.IMBuddy;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.milanac007.demo.im.adapter.FriendAddItemAdapter;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.msg.BuddyVerifyMessage;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.event.AddUserChangeEvent;
import com.milanac007.demo.im.ui.FriendAdapterListItem;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.utils.CommonFunction.showToast;

/**
 * 我的好友页面
 */
public class MyFriendFragment extends BaseFragment implements View.OnClickListener{
    private FriendAddItemAdapter mAdapter;
    private PullToRefreshListView mListView;
    private String mSearchText = "";
    private View ll_no_data;
    private TextView mBack;
    private View mView;
    private EditText mEditSearch;

    private List<FriendAdapterListItem> newFriendItemList;
    private TextView mFinish;

    private IMService imService;
    private IMContactManager contactMgr;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onIMServiceConnected() {
            logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            if (EventBus.getDefault().isRegistered(MyFriendFragment.this)) {
                EventBus.getDefault().unregister(MyFriendFragment.this);
            }
            EventBus.getDefault().registerSticky(MyFriendFragment.this);
            contactMgr = imService.getContactManager();
            mAdapter = new FriendAddItemAdapter(MyFriendFragment.this, contactMgr);
            mListView.setAdapter(mAdapter);
            setData();
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(MyFriendFragment.this)) {
                EventBus.getDefault().unregister(MyFriendFragment.this);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }


    public void onEventMainThread(AddUserChangeEvent event){
        setData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_friend_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_MY_FRIEND;
    }

    private void initView(View view) {
        mView = view;
        mBack = (TextView)view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setText("新的朋友");
        mTitle.setVisibility(View.VISIBLE);

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.drawable.bg_add_sign);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);


        view.findViewById(R.id.search_content_cancel).setVisibility(View.GONE);
        mEditSearch = (EditText)view.findViewById(R.id.content_edittext);
        mEditSearch.setFocusable(false);
        mEditSearch.setHint("IM账号/手机号/邮箱");
        InputFilter[] filters = {new InputFilter.LengthFilter(60)};  //最多60个字符
        mEditSearch.setFilters(filters);

        ll_no_data = view.findViewById(R.id.ll_no_data);
        mListView = (PullToRefreshListView)view.findViewById(R.id.item_list_listview);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);
//        int dividerHeight = CommonFunction.dip2px(0.5f);
        mListView.getRefreshableView().setDividerHeight(0);
    }

    private void setData() {
        fetchFriendData();
    }


    private void clickEdittextBtn(){
        hideKey();
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SEARCH, "0");
    }


    private void setListener() {
        View[] views = {mBack, mFinish, mEditSearch};
        for(View view : views){
            view.setOnClickListener(this);
        }

        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendAdapterListItem friendItem = (FriendAdapterListItem) mListView.getRefreshableView().getAdapter().getItem(position);
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, friendItem.getBuddyId()+"");
            }
        });

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;

            case R.id.content_edittext:{
                clickEdittextBtn();
            }break;

            case R.id.fragment_head1_finish:{
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ADDFRIEND, null);
            }break;
        }
    }


    private void fetchFriendData(){
        //查询DB
        newFriendItemList = new ArrayList<>();
        List<BuddyVerifyMessage> verifyMsgs = BuddyVerifyMessage.getVerifyMsgList();

        for(BuddyVerifyMessage msg : verifyMsgs){
            int msgType = msg.getMsgType();
            if(msgType == DBConstant.MSG_TYPE_DELBUDDY_REQUEST)
                continue;

            FriendAdapterListItem item = new FriendAdapterListItem();
            item.setBuddyid(msg.getSessionId());
            item.setCreateDate(new Date(msg.getCreated()));
            item.setVerifyMsg(msg.getContent());

            int mStatus = 0; // 0(wait for accept), 1(对方已同意)，  2(wait for me accept), 3(我已接受)
            if(msg.getFromId() != msg.getSessionId()){ //自己发的
                if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST){
                    mStatus = 0;
                }else if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT){
                    mStatus = 3;
                }
            }else {
                if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_REQUEST){
                    mStatus = 2;
                }else if(msgType == DBConstant.MSG_TYPE_ADD_BUDDY_ACCEPT){
                    mStatus = 1;
                }
            }
            item.setStatus(mStatus);
            addFriendItemList(item);
        }

        mListView.onRefreshComplete();

        if (newFriendItemList != null) {
            if (newFriendItemList.size() > 0) {
                showListView();
                mAdapter.updateData(newFriendItemList);
            } else {
                showNoDataLayout();
            }
        }
    }

    private void addFriendItemList(FriendAdapterListItem item){

        boolean isNew = true;
        for(FriendAdapterListItem i : newFriendItemList){
            if(i.getBuddyId() == item.getBuddyId()){
                isNew = false;
                if(i.getStatus() < item.getStatus()){
                    newFriendItemList.set(newFriendItemList.indexOf(i), item);
                }
                return;
            }
        }

        if(isNew){
            newFriendItemList.add(item);
        }
    }

    /**显示没有数据的视图
     * @param
     */
    protected void showNoDataLayout(){
        ll_no_data.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    /**
     * 有网 有数据
     */
    protected void showListView(){
        mListView.setVisibility(View.VISIBLE);
        ll_no_data.setVisibility(View.GONE);
    }

    public void handleAcceptFriendRequest(final int buddyid){
        if(buddyid <= 0){
            showToast("账号不能为空");
            return;
        }

        String acceptMsg = DBConstant.SHOWTEXT_SYS_ACCEPT_ADDBUDDY;
        CommonFunction.showProgressDialog(mActivity, "确认中...");
        imService.getContactManager().ReqAddBuddyAccept(buddyid, acceptMsg, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                CommonFunction.dismissProgressDialog();
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int result = imService.getContactManager().onRepAddBuddyAccept(rspObject);
                if(result == 0){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_GUARANTEEBUDDY, 0, buddyid+"");
                        }
                    });

                }else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonFunction.showToast("添加好友失败");
                        }
                    });
                }
            }

            @Override
            public void onTimeout() {
                showToast("添加好友失败：处理超时");
                CommonFunction.dismissProgressDialog();
            }

            @Override
            public void onFail(String error) {
                CommonFunction.dismissProgressDialog();
            }
        });
    }

}
