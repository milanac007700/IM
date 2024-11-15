package com.milanac007.demo.im.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.milanac007.demo.im.activity.BaseActivity;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.event.SessionEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import de.greenrobot.event.EventBus;

public abstract class BaseFragment extends Fragment {

    protected BaseActivity mActivity;
    protected Bundle bundle;

    protected OnActionListener mListener;
    private int mPageNumber;
    private Dialog mProgressDialog = null;
    protected Logger logger = Logger.getLogger();
    public void setOnActionListener(OnActionListener listener) {
        mListener = listener;
    }

    public String TAG(){
        return getClass().getName();
    }

    public BaseFragment(){
        mActivity = (BaseActivity)getActivity();
    }

    public BaseFragment(BaseActivity activity) {
        this.mActivity = activity;
    }

    public void setPageNumber(int number) {
        mPageNumber = number;
    }

    public int getPageNumber() {
        return mPageNumber;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        mActivity = (BaseActivity) getActivity();
        if(!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().registerSticky(this); //注册粘性广播接收者
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.i(getClass().getSimpleName() + " onDestroy().");
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        onIMServiceDisConnected();
    }

    public void onEventMainThread(LoginEvent loginEvent) {

    }

    @Override
    public void onResume() {
        super.onResume();
        IMService imService = mActivity.getImService();
        logger.i(getClass().getSimpleName() +  "#onResume(), imService : " + imService);
        setImService(imService);
    }

    public void onIMServiceConnected(){
        logger.i(getClass().getSimpleName() + " onIMServiceConnected()");
    }

    public void onIMServiceDisConnected() {
        logger.i(getClass().getSimpleName() + " onIMServiceDisConnected()");
        this.imService = null;
    }

    protected IMService imService;
    public void setImService(IMService imService) {
        if(imService != null  && this.imService == null) {
            this.imService = imService;
            onIMServiceConnected();
        }
    }

    public void onBack() {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
    }


    public void checkMyState(boolean online){

    }

    public void popSelf() {
        try {
            if(isDetached() || isRemoving() || getFragmentManager() == null)
                return;

            if(isResumed()) {
                getFragmentManager().popBackStackImmediate();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void popFragment(String fragmentName) {
        popFragment(fragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void popFragment(String fragmentName, int flags) {
        try {
            if(isDetached() || isRemoving() || getFragmentManager() == null)
                return;

            if(isResumed()) {
                getFragmentManager().popBackStackImmediate(fragmentName, flags);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected void showKeyborad(final View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    protected void hideKeyborad(final View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void hideKey() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().findViewById(android.R.id.content);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 如果输入法在窗口上已经显示，则隐藏，反之则显示)
     */
    protected void autoKey(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
