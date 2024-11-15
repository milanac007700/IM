package com.milanac007.demo.im.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.event.ReqSipNegotiationEvent;
import com.milanac007.demo.im.event.SocketEvent;
import com.milanac007.demo.im.fragment.BaseFragment;
import com.milanac007.demo.im.fragment.ChatFragment;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.ToastCompat;
import com.milanac007.demo.im.ui.StatusBarUtil;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.greenrobot.event.EventBus;

import static android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    protected BaseActivity mContext;
    protected Handler mHandler;
    private Logger logger = Logger.getLogger();

    protected void setHandler() {
        mHandler = new Handler();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setHandler();

        if(!EventBus.getDefault().isRegistered(mContext)) {
            EventBus.getDefault().register(mContext);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mReceiver, filter);

        imServiceConnector.connect(mContext);
    }


    protected IMService imService;
    public IMService getImService() {
        return imService;
    }

    public abstract void onIMServiceConnected();

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            mContext.onIMServiceConnected();
        }
    };

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                // 网络状态改变
                onNetStateChanged(context, intent);
            }
        }

    };

    protected void onNetStateChanged(Context context, Intent intent) {

    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        customStatusBar();
    }

    public void setContentView(View rootView) {
        super.setContentView(rootView);
        customStatusBar();
    }

    protected void customStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, getColor(R.color.bg_gray), 0);
            StatusBarUtil.setLightMode(this);
            updateNeedOffsetViewLayout();
        }
    }

    private int needOffsetViewID = -1;
    protected int getNeedOffsetViewLayout() {
        return -1;
    }

    protected void updateNeedOffsetViewLayout(){
        needOffsetViewID = getNeedOffsetViewLayout();
        if(needOffsetViewID != -1) {
            View needOffsetView = findViewById(needOffsetViewID);
            if (needOffsetView != null) {
                Object haveSetOffset = needOffsetView.getTag(StatusBarUtil.TAG_KEY_HAVE_SET_OFFSET);
                if (haveSetOffset != null && (Boolean) haveSetOffset) {
                    return;
                }else {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) needOffsetView.getLayoutParams();
                    layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + StatusBarUtil.getStatusBarHeight(mContext),
                            layoutParams.rightMargin, layoutParams.bottomMargin);
                    needOffsetView.setTag(StatusBarUtil.TAG_KEY_HAVE_SET_OFFSET, true);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setHideKey();
        hideToast();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }

        imServiceConnector.disconnect(mContext);
    }

    public void onEvent(SocketEvent event) {

    }

    //added by zqguo 2016.9.14
    protected boolean hasResume = true;
    @Override
    protected void onResume() {
        super.onResume();
        hasResume = true;
    }


    @Override
    protected void onPause() {
        hasResume = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        hasResume = false;
        super.onStop();
    }

    public Fragment getFragment4Tag(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * 根据tag移除某个Fragment的方法
     */
    public void removeFragment(String tag) {
        Fragment fragment = getFragment4Tag(tag);
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment).commitAllowingStateLoss();
        }
    }


    /**
     * 加入动画
     *
     * @param aClass
     * @param tag
     * @param addToBackStack
     * @param bundle
     * @param animIn
     * @param animOut
     */
    public void showBaseFragment(Class<? extends BaseFragment> aClass, String tag,
                                 boolean addToBackStack, Bundle bundle, int animIn, int animOut) {
        removeAndAddFragment(aClass, tag, addToBackStack, bundle, android.R.id.content, null, animIn, animOut);
        setHideKey();
    }

    /**
     * 添加Fragment
     *
     * @param aClass
     * @param tag
     *            ：fragment的标识
     * @param addToBackStack
     *            ： 是否入后退栈
     */
    public void showBaseFragment(Class<? extends BaseFragment> aClass, String tag, boolean addToBackStack, Bundle bundle) {

        removeAndAddFragment(aClass, tag, addToBackStack, bundle, android.R.id.content, null,
                R.anim.fragment_left_enter, R.anim.fragment_right_exit);
        setHideKey();
    }

    protected void popBackStack(String tag){
        getSupportFragmentManager().popBackStack(tag, 0);
        return;
    }

    /**
     *
     * activity添加fragment的方法 使用自定义动画
     */
    public void removeAndAddFragment(final Class<? extends BaseFragment> aClass, final String tag, final boolean addToBackStack, final Bundle bundle,
                                     final int id, final Fragment parent, final int animIn, final int animOut) {

        //如果已在回退栈 则直接拿过来用
        if(isInTaskStack(tag)){
            if(tag.equals(ChatFragment.class.getName())){
                String currentSessionKey =  bundle.getString("sessionKey");
                if(currentSessionKey.equals(ChatFragment.newInstance().getCurrentSessionKey())){
                    popBackStack(tag);
                }else {
                    getSupportFragmentManager().popBackStackImmediate(tag, POP_BACK_STACK_INCLUSIVE); //0
                    new HandlerPost(300){
                        @Override
                        public void doAction() {
                            Jump2Fragment(aClass, tag, addToBackStack, bundle, id, parent, animIn, animOut);
                        }
                    };
                    return;
                }
            }else {
                popBackStack(tag);
            }
        }
        Jump2Fragment(aClass, tag, addToBackStack, bundle, id, parent, animIn, animOut);
    }


    private void Jump2Fragment(Class<? extends BaseFragment> aClass, final String tag, final boolean addToBackStack, final Bundle bundle,
                               final int id, final Fragment parent, final int animIn, final int animOut){

        if (!hasResume) {
            logger.d("%s", "hasResume " + tag);
            return;
        }

        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        transaction.setCustomAnimations(animIn, animOut, animIn, animOut);
        if (fragment != null) {
            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.replace(id, fragment, tag);
            }
        } else {
            addNewFragment(aClass, tag, bundle, transaction, id, parent);
        }

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        if (!hasResume) {
            logger.d("%s", "hasResume return 2 " + tag);
            return;
        }
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * activity添加fragment的方法 使用标准转场动画
     */
    public void removeAndAddFragment(Class<? extends BaseFragment> aClass, String tag, boolean addToBackStack, Bundle bundle, int id,
                                     Fragment parent, int transit) {
        if (!hasResume) {
            logger.d("%s", "hasResume " + tag);
            return;
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        transaction.setTransition(transit);
        if (fragment != null) {
            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.replace(id, fragment, tag);
            }
        } else {
            addNewFragment(aClass, tag, bundle, transaction, id, parent);
        }

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        if (!hasResume) {
            logger.d("%s", "hasResume return 2 " + tag);
            return;
        }
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void addNewFragment(Class<? extends BaseFragment> aClass, String tag, Bundle bundle,
                                  FragmentTransaction transaction, int id, Fragment parent) {
        BaseFragment fragment;
        try {
            fragment = aClass.newInstance();
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

    /**得到当前的fragment
     * @return
     */
    public BaseFragment getCurrentFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            int size = fragments.size();
            for (int i = size; i > 0; i--) {
                Fragment fragment = fragments.get(i - 1);
                if (fragment instanceof BaseFragment) {
                    BaseFragment curFragment = (BaseFragment) fragments.get(i - 1);
                    if (curFragment != null) {
                        return curFragment;
                    }
                }
            }
        }

        return null;
    }


    public boolean isInTaskStack(String tag){
        boolean isInTaskStack = false;
        int count = getSupportFragmentManager().getBackStackEntryCount();
        logger.d("%s", "count: "+count);

        for (int i = 0;i < count; i++){

            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(i);
            String name = entry.getName();
            int id = entry.getId();
            logger.d("name=%s,id=%s", "id: ",name, id);

            if (tag.equalsIgnoreCase(name)) {
                isInTaskStack = true;
                break;
            }

        }

        return isInTaskStack;
    }


    private static Toast mToast = null;

    public static Toast getToast(String str){
        //暂时解决快速调用该方法，有些版本的手机(华为 Android 10)会不显示toast的bug
        if(mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        //解决小米手机 toast带应用名称的问题
//        mToast = Toast.makeText(App.getContext(), str, Toast.LENGTH_SHORT);
        mToast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
        mToast.setText(str);
        return mToast;
    }

    public static final void showToast(final String msg) {
        Log.i(TAG, String.format("showToast(final String msg): Looper.getMainLooper().getThread().getId() = %d, Thread.currentThread().getId() = %d",
                Looper.getMainLooper().getThread().getId(), Thread.currentThread().getId()));

        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            new HandlerPost(0, true){
                @Override
                public void doAction() {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){//Android8.0以前的版本有bug：没有catch BadTokenException
                        ToastCompat.getToast(msg).show();
                        return;
                    }
                    getToast(msg).show();
                }
            };
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                ToastCompat.getToast(msg).show();
                return;
            }
            getToast(msg).show();
        }
    }

    public static void hideToast(){
        if(mToast != null){
            mToast.cancel();
            mToast = null;
        }
    }

    protected boolean isFrontground(){
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        if(runningTasks != null && !runningTasks.isEmpty()) {
            ComponentName componentName = runningTasks.get(0).topActivity;
            String className = this.getComponentName().getClassName();
            if(className.equals(componentName.getClassName()))
                return true;
        }

        return false;
    }


    private static ProgressDialog progressDialog = null;
    public static void showProgressDialog(Context context, String msg) {
        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    if(progressDialog == null){
                        progressDialog = new ProgressDialog(context);
                    }
                    Log.i("Utils", "progressDialog: " + progressDialog);
                    progressDialog.setMessage(msg);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    if(progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = null;
                }
            }
        };

    }

    public static void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }

        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    progressDialog.dismiss();
                    progressDialog = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    protected  void showAlertDialog(final Activity context, String title, String message, String okStr, String cancelStr,
                                    DialogInterface.OnClickListener okCallback, DialogInterface.OnClickListener cancelCallback){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);

        if(okCallback != null) {
            if(TextUtils.isEmpty(okStr)){
                okStr = getString(R.string.g_confirm);
            }
            builder.setPositiveButton(okStr, okCallback);
        }
        if(cancelCallback != null) {
            if(TextUtils.isEmpty(cancelStr)){
                cancelStr = getString(R.string.g_cancel);
            }
            builder.setNegativeButton(cancelStr, cancelCallback);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setCancelable(false).create().show();
            }
        });

    }

    protected  void showAlertDialog(final Activity context, String title, String message,
                                    DialogInterface.OnClickListener okCallback, DialogInterface.OnClickListener cancelCallback){


        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);

        if(okCallback != null) {
            builder.setPositiveButton(R.string.g_confirm, okCallback);
        }
        if(cancelCallback != null) {
            builder.setNegativeButton(R.string.g_cancel, cancelCallback);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setCancelable(false).create().show();
            }
        });
    }

    protected void showDialog(String title, String content) {
        final CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mContext);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveBtn(R.string.g_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        new HandlerPost(0, true) {
            @Override
            public void doAction() {
                CustomConfirmDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };

    }

    public void showDialog(String title, String content, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        final CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mContext);
        builder.setTitle(title).setMessage(content);

        if (okListener != null) {
            builder.setPositiveBtn(R.string.g_confirm, okListener);
        }
        if (cancelListener != null) {
            builder.setNegativeBtn(R.string.g_cancel, cancelListener);
        }

        new HandlerPost(0, true) {
            @Override
            public void doAction() {
                CustomConfirmDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };
    }


    protected void showDialog(String title, String content, String okStr, String cancelStr, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        final CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(mContext);
        builder.setTitle(title).setMessage(content);

        if (okListener != null) {
            String okContent = TextUtils.isEmpty(okStr) ? getString(R.string.g_ok) : okStr;
            builder.setPositiveBtn(okContent, okListener);
        }
        if (cancelListener != null) {
            String okContent = TextUtils.isEmpty(cancelStr) ? getString(R.string.g_cancel) : cancelStr;
            builder.setNegativeBtn(okContent, cancelListener);
        }

        new HandlerPost(0, true) {
            @Override
            public void doAction() {
                CustomConfirmDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };
    }

    /**
     * 隐藏软键盘
     */
    public void setHideKey() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = findViewById(android.R.id.content);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void setShowKey() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = findViewById(android.R.id.content);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 如果输入法在窗口上已经显示，则隐藏，反之则显示)
     */
    public void autoKey(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
