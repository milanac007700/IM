package com.milanac007.demo.im.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.db.manager.IMGroupManager;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.adapter.AddUserAdapter;
import com.milanac007.demo.im.adapter.SearchAdapter;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by milanac007 on 2016/11/26.
 */
public class SearchFragment extends BaseFragment implements View.OnClickListener{

    private View mBack;
    private View mView;
    private EditText mSearchEdit;
    private View mDelImg;
    private View mSearchImg;
    private int searchType; //0: 搜人    1：本地搜(人/群聊等)   2:通话记录
    private SearchAdapter mSearchAdapter;
    private IMService imService;
    private ListView search_listview;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            mSearchAdapter = new SearchAdapter(SearchFragment.this, imService);
            search_listview.setAdapter(mSearchAdapter);
            search_listview.setOnItemClickListener(mSearchAdapter);
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
        return inflater.inflate(R.layout.search_content_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(bundle != null && bundle.containsKey("searchType")){
            searchType = bundle.getInt("searchType", 0);
        }

        initView(view);
        setListener();
    }

    protected void initView(View view){
        mView = view;
        View search_root_layout  = view.findViewById(R.id.search_root_layout);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)search_root_layout.getLayoutParams();
        lp.leftMargin = 0;
        search_root_layout.setLayoutParams(lp);

        mBack = view.findViewById(R.id.search_content_cancel);
        mSearchImg = view.findViewById(R.id.search_content_img);
        mSearchEdit = (EditText)view.findViewById(R.id.content_edittext);
        if(searchType == 0){
            mSearchEdit.setHint("搜索联系人");
        }else {
            mSearchEdit.setHint("搜索");
        }

        InputFilter[] filters = {new InputFilter.LengthFilter(60)};  //最多60个字符
        mSearchEdit.setFilters(filters);
        mSearchEdit.requestFocus();
        autoKey();
        mDelImg = view.findViewById(R.id.del_content_img);

        search_listview = (ListView)view.findViewById(R.id.search_listview);

    }


    public void setListener(){

        View[] views = {mBack, mDelImg, mSearchImg};
        for(View view : views){
            view.setOnClickListener(this);
        }

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKey();
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String editStr = editable.toString();
                if(CommonFunction.isStringEmpty(editStr)){
                    mDelImg.setVisibility(View.GONE);
                }else {
                    mDelImg.setVisibility(View.VISIBLE);
                    if(searchType == 1){
                        clickSearchBtn();
                    }
                }
            }
        });

        mSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_SEND){ //搜索键 发送键
                    clickSearchBtn();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.search_content_cancel:{
                hideKey();
                onBack();
            }break;
            case R.id.search_content_img:{
                clickSearchBtn();
            }break;
            case R.id.del_content_img:{
                Editable searchEditable = mSearchEdit.getText();
                int endIndex = mSearchEdit.getSelectionStart();
                searchEditable.delete(0, endIndex);
                clickSearchBtn();
            }break;

        }
    }

    private void clickSearchBtn(){
        hideKey();
        String searchStr = mSearchEdit.getText().toString().trim();
        if(searchType == 0) {
            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEARCH_BUDDY, 0, searchStr);
        } else if(searchType == 1){
            mSearchAdapter.setSearchKey(searchStr);
            searchInfoFromLocal(searchStr);
        }else if(searchType == 2){
            //TODO
            CommonFunction.showToast(R.string.func_developing);
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_SEARCH;
    }


    public void sendIM(final String extNo) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, extNo);
    }

    /**
     * 本地模糊搜索
     * @param searchStr
     */
    private void searchInfoFromLocal(String searchStr){
        CommonFunction.showProgressDialog(mActivity, "搜索中...");
        searchEntityLists(searchStr);
    }

    // 文字高亮search 模块
    private void searchEntityLists(String key) {
        mSearchAdapter.clear();

        List<UserEntity> contactList = IMContactManager.instance().getSearchContactList(key);
        mSearchAdapter.putUserList(contactList);

        List<GroupEntity> groupList = IMGroupManager.instance().getSearchAllGroupList(key);
        mSearchAdapter.putGroupList(groupList);

        mSearchAdapter.notifyDataSetChanged();
        CommonFunction.dismissProgressDialog();
    }

}
