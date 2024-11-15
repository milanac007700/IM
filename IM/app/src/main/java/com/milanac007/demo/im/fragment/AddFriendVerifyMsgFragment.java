package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.protobuf.IMBuddy;
import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Preferences;

import java.io.IOException;

import static com.milanac007.demo.im.utils.CommonFunction.showToast;

/**
 * Created by zqguo on 2016/11/22.
 */
public class AddFriendVerifyMsgFragment extends BaseFragment implements TextWatcher, View.OnClickListener{

    private View mBack;
    private EditText mVerifyMsgEditText;
    private TextView mRemainNumTv;
    private TextView mFinish;
    private TextView mTitle;
    private  int mLimitNum;
    private View mView;

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
//            EventBus.getDefault().registerSticky(AddFriendVerifyMsgFragment.this);
        }

        @Override
        public void onServiceDisconnected() {
//            if (EventBus.getDefault().isRegistered(AddFriendVerifyMsgFragment.this)) {
//                EventBus.getDefault().unregister(AddFriendVerifyMsgFragment.this);
//            }
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
//        if (EventBus.getDefault().isRegistered(AddFriendVerifyMsgFragment.this)) {
//            EventBus.getDefault().unregister(AddFriendVerifyMsgFragment.this);
//        }
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_friend_verify_msg_layout, null);
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
        mFinish = (TextView) view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mVerifyMsgEditText = (EditText)view.findViewById(R.id.verify_msg_edit);
        mRemainNumTv = (TextView)view.findViewById(R.id.remain_wordnum_textview);
        setHeadView();

        initEditText(String.format("我是%s", Preferences.getCurrentLoginer().getUserCode()));
        setLimitNum(60);
        setRemainNum(60);
        setRemainNumTvIsShow(true);
    }

    public void setHeadView(){
        mTitle.setText("好友验证");
        mFinish.setText("发送");
        mFinish.setTextSize(15);
        mFinish.setCompoundDrawables(null, null, null, null);
    }

    public void setLimitNum(int number){
        this.mLimitNum = number;
        InputFilter[] filters = {new InputFilter.LengthFilter(mLimitNum)};
        mVerifyMsgEditText.setFilters(filters);
    }

    public void setRemainNumTvIsShow(boolean isShow) {
        if (isShow) {
            mRemainNumTv.setVisibility(View.VISIBLE);
        } else {
            mRemainNumTv.setVisibility(View.GONE);
        }
    }

    public void setRemainNum(int number){
        mRemainNumTv.setText(String.format("您还可以输入%d个字", number));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                hideKey();
                onBack();
            }break;
            case R.id.fragment_head1_finish:{
                clickFinish();
            }break;

        }
    }

    public void setListener(){
        View[] views = {mBack, mFinish};
        for(View view : views){
            view.setOnClickListener(this);
        }

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        mVerifyMsgEditText.addTextChangedListener(this);
    }

    private void clickFinish(){
        hideKey();

        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        int buddyid = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
        handleMsgByStatus(buddyid);
    }

    private void handleMsgByStatus(int buddyid){

        if(buddyid <= 0){
            showToast("账号不能为空");
            return;
        }

        String verifyMsg = mVerifyMsgEditText.getText().toString();
        CommonFunction.showProgressDialog(mActivity, "发送中...");
        imService.getContactManager().ReqAddBuddy(buddyid, verifyMsg, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                CommonFunction.dismissProgressDialog();
                JSONObject rspObject = JSONObject.parseObject((String) response);
                int result = imService.getContactManager().onRepAddBuddy(rspObject);
                if(result == 0){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("发送成功");
                            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
                        }
                    });

                }else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("发送失败");
                        }
                    });
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        int textLength = getEditText().length();
        int remainNum = mLimitNum - textLength;
        setRemainNum(remainNum);
    }

    protected String getEditText() {
        return mVerifyMsgEditText.getText().toString().trim();
    }

    private void initEditText(String text) {
        mVerifyMsgEditText.setText(text);
        if (TextUtils.isEmpty(text)) {
            mVerifyMsgEditText.setSelection(0);
        }else {
            mVerifyMsgEditText.setSelection(text.length());
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_ADD_FRIEND_VERIFY_MSG;
    }

}
