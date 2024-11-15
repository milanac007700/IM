package com.milanac007.demo.im.fragment;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.adapter.AddUserAdapter;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.ui.ISelectUserHandler;
import com.milanac007.demo.im.ui.SideBar;
import com.milanac007.demo.im.ui.stickylist.StickyListHeadersListView;

import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;

/**
 * Created by zqguo on 2017/1/5.
 */
public class ContactsBaseFragment extends BaseFragment implements View.OnClickListener{

    protected StickyListHeadersListView mListView;
    protected View contacts_all_list_header;
    protected RelativeLayout mMyFriendLayout;
    protected View mMyGroupChatLayout;
    protected AddUserAdapter mAdapter;
    protected SideBar mSideBar;

    protected RelativeLayout search_layout;
    private LinearLayout search_edit_layout;
    protected ImageView search_img;
    private EditText search_edit;
    private View search_tip;

    protected ImageView my_friend_img;
    protected TextView my_friend_name;
    protected ImageView group_chat_img;
    protected TextView group_chat_name;
    protected TextView mTitle;
    protected TextView mFinish;
    protected TextView mBack;
    protected String mTag;


    protected IMService imService;
    protected IMContactManager contactMgr;
    protected int loginer;
    private int curTabIndex = 0;

    protected boolean isSingle = true;
    protected boolean hideContactsHeader = false;
    private boolean mIsShowSideBar = false;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            contactMgr = imService.getContactManager();
            loginer = imService.getLoginManager().getLoginId();
            setData();
        }

        @Override
        public void onServiceDisconnected() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contacts_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(bundle != null) {
            mTag = bundle.getString("tag");
        }
        initView(view);
        setListener();
    }


    protected void setTitle(TextView mTitle){
        mTitle.setTextSize(18);
        mTitle.setText(getResources().getString(R.string.g_select));
    }

    protected void setFinishText(boolean isSingle){
        if(isSingle){
            mFinish.setText("多选");
        }else {
            if(mAdapter.allSelectedObject().isEmpty()){
                mFinish.setText("单选");
            }else {
                mFinish.setText(String.format("发送(%d)", mAdapter.allSelectedObject().size()));
            }

        }
    }

    protected void initView(View view){

        mBack = (TextView)view.findViewById(R.id.fragment_head1_back);

        mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        setTitle(mTitle);

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        mFinish.setGravity(Gravity.CENTER);
        mFinish.setTextColor(getActivity().getResources().getColor(R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, CommonFunction.dip2px(30));
        params.rightMargin = CommonFunction.dip2px(15);
        mFinish.setLayoutParams(params);
        mFinish.setMinWidth(CommonFunction.dip2px(75));
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.green_btn_style);
        mFinish.setBackgroundDrawable(drawable);

        mSideBar = (SideBar)view.findViewById(R.id.contacts_sideBar);

        mListView = (StickyListHeadersListView)view.findViewById(R.id.contactListView);
        int showDividers = CommonFunction.dip2px(0f);
        mListView.setDividerHeight(showDividers);

        contacts_all_list_header = mActivity.getLayoutInflater().inflate(R.layout.contacts_header_layout, null);

        search_layout = (RelativeLayout)contacts_all_list_header.findViewById(R.id.search_layout);
        search_edit_layout = (LinearLayout) contacts_all_list_header.findViewById(R.id.search_edit_layout);
        search_img = (ImageView)contacts_all_list_header.findViewById(R.id.search_img);
        search_tip = contacts_all_list_header.findViewById(R.id.search_tip);
        search_edit = (EditText)contacts_all_list_header.findViewById(R.id.search_edit);

        mMyFriendLayout = (RelativeLayout)contacts_all_list_header.findViewById(R.id.my_friend_layout);
        mMyGroupChatLayout = contacts_all_list_header.findViewById(R.id.my_groupChat_layout);

        my_friend_img = (ImageView) contacts_all_list_header.findViewById(R.id.my_friend_img);
        my_friend_name = (TextView) contacts_all_list_header.findViewById(R.id.my_friend_name);
        group_chat_img = (ImageView) contacts_all_list_header.findViewById(R.id.group_chat_img);
        group_chat_name = (TextView) contacts_all_list_header.findViewById(R.id.group_chat_name);

        if(!hideContactsHeader){
            mListView.addHeaderView(contacts_all_list_header);
        }

        mAdapter = new AddUserAdapter(this, null);
        mAdapter.setMode(R.id.SINGLE_CHOICE_MODE);
        mListView.setAdapter(mAdapter);

        setFinishText(true);
    }

    public void setShowSideBar(boolean mShowSideBar) {
        mSideBar.setVisibility(mShowSideBar ? View.VISIBLE : View.GONE);
    }

    protected void setListener(){
        View[] views = {mBack, mFinish, mMyFriendLayout, mMyGroupChatLayout, search_layout};
        for (View view : views){
            view.setOnClickListener(this);
        }

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getPositionForPinyin(s.toUpperCase(Locale.getDefault()).charAt(0));
                Logger.getLogger().d("%s", position );
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int index = 0;
                if(hideContactsHeader){
                    index = position;
                }else {
                    index = position-1;
                }

                PeerEntity item = mAdapter.getItem(index);
                if(item == null)
                    return;

                if (mAdapter.isObjectDisableEdit(item)){
                    return;
                }

                if (isSingle) {
                    mAdapter.handleSingleSelectClick(view, index);
                    setFinishText(true);
                    showDialog();
                } else {
                    mAdapter.handleMultiSelectClick(view, index);
                    setFinishText(false);
                }
            }
        });
    }


    protected void setData(){
        List<UserEntity> contactList = contactMgr.getContactSortedList();
        // 没有任何的联系人数据
        if (contactList.size() <= 0) {
            return;
        }

        mAdapter.updateData(contactList);
    }


    protected void handleDoneButton(List<PeerEntity> selectedItem) {

        Fragment fragment = mActivity.getFragment4Tag(mTag);
        if (fragment instanceof ISelectUserHandler) {
            try {
                String methodName = "handleSelectedUser";

                Bundle bundle = new Bundle();
                Object[] args = {selectedItem,true,bundle};
                CommonFunction.invoke(fragment, methodName, args);
            } catch (Exception e) {
                Logger.getLogger().e("%s", e.getMessage());
            }
        }

        onBack();
    }

    protected void onFinishClick(){

        if(isSingle){
            mAdapter.clearSelectedObject();
            mAdapter.setMode(R.id.MULTIPLE_CHOICE_MODE);
            isSingle = false;
            setFinishText(isSingle);
        }else {
            if(mAdapter.allSelectedObject().isEmpty()){
                mAdapter.setMode(R.id.SINGLE_CHOICE_MODE);
                isSingle = true;
                setFinishText(isSingle);
            }else {
                showDialog();
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void showDialog(){

        final List<PeerEntity> allSelectedList = (List<PeerEntity>)mAdapter.allSelectedObject();
        CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mActivity);
        builder.setTitle("发送给");
        if(allSelectedList.size() == 1){
            PeerEntity peerEntity = allSelectedList.get(0);
            builder.setMessage(String.format("确定发送给%s吗？", peerEntity.getMainName()));
        }else {
            PeerEntity peerEntity = allSelectedList.get(0);
            builder.setMessage(String.format("确定发送给%s等%d个好友(群)吗？", peerEntity.getMainName(), allSelectedList.size()));
        }

        builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                handleDoneButton(allSelectedList);
            }
        });

        builder.setNegativeBtn("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        CustomConfirmDialog dialog = builder.create();
        dialog.show();
    }

    protected void onMyFriendClick(){
//        CommonFunction.showToast(R.string.func_developing);
    }

    protected void onMyGroupChatClick(){
//        CommonFunction.showToast(R.string.func_developing);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
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

            case R.id.search_layout:{
                onSearchLayoutClick();
            }break;
            default:
                break;
        }

    }

    protected void onSearchLayoutClick(){
        Drawable drawable = null;
        if(search_edit_layout.getVisibility() == View.VISIBLE){
            search_edit_layout.setVisibility(View.GONE);
            hideKey();
            search_tip.setVisibility(View.VISIBLE);
            drawable = getResources().getDrawable(R.drawable.bg_search);
        }else {
            search_edit_layout.setVisibility(View.VISIBLE);
            search_edit.requestFocus();
            autoKey();
            search_tip.setVisibility(View.GONE);
            drawable = getResources().getDrawable(R.drawable.bg_back);
        }

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        search_img.setImageDrawable(drawable);

    }
}
