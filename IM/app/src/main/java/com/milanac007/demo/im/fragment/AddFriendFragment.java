package com.milanac007.demo.im.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.ui.SelfQRCodeDialog;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;

/**
 * Created by zqguo on 2016/10/11.
 */
public class AddFriendFragment extends BaseFragment implements View.OnClickListener{

    private View mBack;
    private TextView mTitle;
    private View mView;
    private View scan_qrcode_layout;
    private View mobile_contacts_layout;

    private EditText mEditSearch;
    private View myUsercodeLayout;
    private TextView usercodeTextView;

    private IMService imService;
    private UserEntity userEntity;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
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
        return inflater.inflate(R.layout.add_friend_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    protected void initView(View view){
        mView = view;
        mBack = view.findViewById(R.id.fragment_head1_back);
        mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);

        view.findViewById(R.id.search_root_layout).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.search_content_cancel).setVisibility(View.GONE);

        mEditSearch = (EditText)view.findViewById(R.id.content_edittext);
        mEditSearch.setFocusable(false);
        mEditSearch.setHint("IM账号/手机号/邮箱");
        InputFilter[] filters = {new InputFilter.LengthFilter(60)};  //最多60个字符
        mEditSearch.setFilters(filters);

        usercodeTextView = (TextView)view.findViewById(R.id.usercodeTextView);
        myUsercodeLayout = view.findViewById(R.id.myUsercodeLayout);
        scan_qrcode_layout = view.findViewById(R.id.scan_qrcode_layout);
        mobile_contacts_layout = view.findViewById(R.id.mobile_contacts_layout);

        setHeadView();
    }

    public void setHeadView(){
        mTitle.setText("添加好友");
//        mFinish.setTextSize(15);
//        Drawable drawable =  getResources().getDrawable(R.drawable.bg_search);
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        mFinish.setCompoundDrawables(drawable, null, null, null);
    }

    SelfQRCodeDialog qrCodeDialog = null;
    private void clickMyQRCodeBtn(){
        if(qrCodeDialog == null) {
            qrCodeDialog = new SelfQRCodeDialog(mActivity, userEntity);
        }
        qrCodeDialog.show();
    }

    private void  clickScanQRCodeBtn(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SCAN_QRCODE, 0, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                hideKey();
                onBack();
            }break;
            case R.id.content_edittext:{
                clickEdittextBtn();
            }break;
            case R.id.myUsercodeLayout:{
                clickMyQRCodeBtn();
            }break;
            case R.id.scan_qrcode_layout:{
                clickScanQRCodeBtn();
            }break;
            case R.id.mobile_contacts_layout:{
                Logger.getLogger().e("click mobile contact");
                //TODO
//                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN,  OnActionListener.Page.SCREEN_MOBILE_CONTACT, null);
            }break;

        }
    }

    public void setListener(){

        View[] views = {mBack, mEditSearch, myUsercodeLayout, scan_qrcode_layout, mobile_contacts_layout};
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

    }


    private void setData(){
        User currentLoginer = Preferences.getCurrentLoginer();
        usercodeTextView.setText("IM账号: "+ currentLoginer.getUserCode());
        userEntity = imService.getContactManager().findContact(currentLoginer.getUuid());
    }

    private void clickEdittextBtn(){
        hideKey();
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SEARCH, "0");
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_ADDFRIEND;
    }
}
