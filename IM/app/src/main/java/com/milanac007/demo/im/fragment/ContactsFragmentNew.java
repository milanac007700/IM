package com.milanac007.demo.im.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;


import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.adapter.AddUserAdapter;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.event.AddUserChangeEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.OperateListPop;
import com.milanac007.demo.im.ui.SideBar;
import com.milanac007.demo.im.ui.stickylist.StickyListHeadersListView;
import com.milanac007.demo.im.utils.CommonFunction;
//import com.milanac007.demo.im.db.manager.IMContactManager;
//import com.milanac007.demo.im.service.IMService;
//import com.milanac007.demo.im.db.helper.IMServiceConnector;



import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2016/9/14.
 */
public class ContactsFragmentNew extends LazerLoadFragment implements View.OnClickListener{

    private StickyListHeadersListView mListView;
    private View mMyFriendLayout;
    private View mMyGroupChatLayout;
    private AddUserAdapter mAdapter;
    protected SideBar mSideBar;
    /**单选还是多选*/
    protected boolean isSingle = true;
    private boolean mIsShowSideBar = false;

    private static ContactsFragmentNew newInstance = null;
    private TextView mSearchBtn;
    private TextView mFinish;
    private TextView mNewAddBuddyTip;
    private IMContactManager contactMgr;

    public static ContactsFragmentNew newInstance() {
        if(newInstance == null) {
            newInstance = new ContactsFragmentNew();
        }
        return newInstance;
    }

    public static boolean instanceExist() {
        return newInstance != null;
    }

    public void destoryInstance(){
        if(newInstance != null) {
            newInstance = null;
        }
    }

    @Override
    public void onIMServiceConnected() {
        super. onIMServiceConnected();
        if(getActivity() == null) {
            Log.e(TAG(), "onIMServiceConnected(), getActivity() == null, return.");
            return;
        }

        contactMgr = imService.getContactManager();
        mAdapter = new AddUserAdapter(ContactsFragmentNew.this, imService);
        mListView.setAdapter(mAdapter);
        isPrepared = true;
        lazyLoad();
    }

    public void onIMServiceDisConnected() {
        super.onIMServiceDisConnected();
        mHasLoadedOnce = false;
    }

    @Override
    public void setImService(IMService imService) {
        if(imService != null) {
            this.imService = imService;
            onIMServiceConnected();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contacts_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    @Override
    public int getPageNumber() {
        return MainActivity.SCREEN_CONTACTLIST;
    }

    protected void setTitle(TextView mTitle){
        mTitle.setTextSize(18);
        mTitle.setText(getResources().getString(R.string.g_contacts_label));
    }

    private void initView(View view){

        view.findViewById(R.id.fragment_head1_back).setVisibility(View.GONE);

        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        setTitle(mTitle);

        mSearchBtn = (TextView) view.findViewById(R.id.fragment_head1_finish2);
        mSearchBtn.setVisibility(View.GONE); //TODO 体验不好，待优化
        Drawable drawable = getResources().getDrawable(R.mipmap.bg_search);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSearchBtn.setCompoundDrawables(drawable, null, null, null);

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        drawable = getResources().getDrawable(R.mipmap.bg_add_sign);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);

        mSideBar = (SideBar)view.findViewById(R.id.contacts_sideBar);

        mListView = (StickyListHeadersListView)view.findViewById(R.id.contactListView);
        int showDividers = CommonFunction.dip2px(0f);
        mListView.setDividerHeight(showDividers);
 
        
        View contacts_all_list_header = getActivity().getLayoutInflater().inflate(R.layout.contacts_header_layout, null);
        mMyFriendLayout = contacts_all_list_header.findViewById(R.id.my_friend_layout);
        mNewAddBuddyTip = (TextView)mMyFriendLayout.findViewById(R.id.unread_view);
        refreshNewAddFriendRequestUI();
        mMyGroupChatLayout = contacts_all_list_header.findViewById(R.id.my_groupChat_layout);
        mListView.addHeaderView(contacts_all_list_header);
        if(mAdapter != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    private void setListener(){
        View[] views = {mSearchBtn, mFinish, mMyFriendLayout, mMyGroupChatLayout};
        for (View view : views){
            view.setOnClickListener(this);
        }

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getPositionForPinyin(s.toUpperCase(Locale.getDefault()).charAt(0));

                logger.d("%s", position);
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                PeerEntity imBuddy = mAdapter.getItem(position-1);
                if(imBuddy == null)
                    return;

                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, imBuddy.getPeerId()+"");

            }
        });

    }

    /** 标志位，标志已经初始化完成 */
    private boolean isPrepared;
    /** 是否已被加载过一次，第二次就不再去请求数据了 */
    private boolean mHasLoadedOnce;


    @Override
    protected void lazyLoad() {
        
        refreshNewAddFriendRequestUI();


//        if (!isPrepared || !isVisible || mHasLoadedOnce) {
//            return;
//        }

        if (!isPrepared || !isVisible) {
            return;
        }

        //显示加载进度对话框
        CommonFunction.showProgressDialog(getActivity(), "加载中");
        List<UserEntity> contactList = contactMgr.getContactSortedList();
        if (contactList.size() <= 0) {  // 没有任何的联系人数据
            CommonFunction.dismissProgressDialog();
            return;
        }

        mAdapter.bindData(contactList, null);
//        mHasLoadedOnce = true;

        //关闭对话框
        CommonFunction.dismissProgressDialog();

    }

    public void refreshNewAddFriendRequestUI(){
        int newAddFriendRequestSum = MainActivity.getInstance().getNewAddFriendRequestSum();
        if(newAddFriendRequestSum <=0){
            mNewAddBuddyTip.setText("0");
            mNewAddBuddyTip.setVisibility(View.INVISIBLE);
        }else {
            mNewAddBuddyTip.setText("" + newAddFriendRequestSum);
            mNewAddBuddyTip.setVisibility(View.VISIBLE);
        }
    }


    private void onClickSearch(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SEARCH, "1");
    }

    private OperateListPop mOperateListPop;
    private void onFinishClick(){

        if(mOperateListPop == null){
            mOperateListPop = new OperateListPop(CommonFunction.getWidthPx()*2/5, getActivity(), getResources().getDrawable(R.mipmap.bg_menu));
            ArrayList<OperateListPop.PopItem> popItems = new ArrayList<>();
            final OperateListPop.PopItem popItem = mOperateListPop.new PopItem(R.mipmap.menu_item_add_friend, "添加朋友", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOperateListPop.dismiss();
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ADDFRIEND, null);
                }
            });
            popItems.add(popItem);

            final OperateListPop.PopItem popItem2 = mOperateListPop.new PopItem(R.mipmap.menu_item_group_chat, "群组对话", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_CREATE_GROUP_CHAT, 0, null);
                    mOperateListPop.dismiss();
                }
            });
            popItems.add(popItem2);

            final OperateListPop.PopItem popItem3 = mOperateListPop.new PopItem(R.mipmap.menu_item_scan, "扫一扫", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickScanQRCodeBtn();
                    mOperateListPop.dismiss();
                }
            });
            popItems.add(popItem3);

            mOperateListPop.setData(popItems);
        }
        mOperateListPop.show(mFinish, 0,  -CommonFunction.dip2px(6f));
    }

    private void  clickScanQRCodeBtn(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SCAN_QRCODE, 0, null);
    }

    private void onMyFriendClick(){
        MainActivity.getInstance().setNewFriendTip(0, View.INVISIBLE);
        refreshNewAddFriendRequestUI();

        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_MY_FRIEND, null);
    }

    private void onMyGroupChatClick(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_CHAT_MAIN, null);
    }

    
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_finish2:{
                onClickSearch();
            }break;

            case R.id.fragment_head1_finish:{
                onFinishClick();
            }break;

            case R.id.my_friend_layout:{
                onMyFriendClick();
            }break;

            case R.id.my_groupChat_layout:{
                onMyGroupChatClick();
            }break;
            
            default:
                break;

        }

    }


    public void onEventMainThread(AddUserChangeEvent event){
        Logger.getLogger().i("%s", "onEventMainThread: AddUserChangeEvent");
        lazyLoad();
        if(event.getPerson() != null) {
            Logger.getLogger().i("%s", event.getOperateType() + ", " + event.getPerson().getPeerId());
        }
    }

    public void showCallDialog(final int peerId) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SHOW_CALL_DIALOG, 0, peerId+"");
    }

}
