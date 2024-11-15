package com.milanac007.demo.im.activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.milanac007.pickerandpreviewphoto.HomePageActivity;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMLoginManager;
import com.milanac007.demo.im.event.LoginEvent;
import com.milanac007.demo.im.permission.PermissionUtils;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.exception.TdrException;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.net.NetRequestByOkHttpClient;
import com.milanac007.demo.im.rxjava.AndroidScheduler;
import com.milanac007.demo.im.utils.Utils;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private EditText et_phone;
    private EditText et_password;
    private View btn_login;
    private RotateAnimation rotateAnimation;
    private View loading_process;
    private View phone_del_view;
    private View password_del_view;
    private View login_layout;

    @Override
    public void onIMServiceConnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_layout = findViewById(R.id.login_layout);
        login_layout.setVisibility(View.VISIBLE);
        TextView title_text_view = findViewById(R.id.title_text_view);
        TextView func_btn = findViewById(R.id.func_btn);
        loading_process = findViewById(R.id.loading_process);
        et_phone = findViewById(R.id.et_phone);
        et_password = findViewById(R.id.et_password);
        phone_del_view = findViewById(R.id.phone_del_view);
        password_del_view = findViewById(R.id.password_del_view);
        btn_login = findViewById(R.id.btn_login);
        View tv_config = findViewById(R.id.tv_config);
        title_text_view.setText(R.string.login);
        func_btn.setVisibility(View.VISIBLE);
        setAnimation();

        View[] views = {btn_login, func_btn, phone_del_view, password_del_view, tv_config};
        for(View view : views) {
            view.setOnClickListener(this);
        }

        et_phone.addTextChangedListener(textWatcher);
        et_password.addTextChangedListener(textPasswordWatcher);


        //这里再清除一次，保证异常(eg:程序被杀死)时 也清除敏感信息
//        Preferences.clear();
//        Log.i(TAG, "clearData() finish: " + this);

        String permission = PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE;
        if(PermissionUtils.lacksPermission(mContext, permission)) {
            PermissionUtils.requestPermission(this, PermissionUtils.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE, permission, mCallback);
        }else {
            initView();
        }
    }


    PermissionUtils.PermissionGrantCallback mCallback = new PermissionUtils.PermissionGrantCallback() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE:{
                    initView();
                }break;
            }
        }

        @Override
        public void onPermissionDenied(int requestCode, String err) {
            Log.i(TAG, "PermissionGrantCallback onPermissionDenied: " + err);
            switch (requestCode) {
                case PermissionUtils.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE:{
                    showDialog("提示", err, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }, null);
                }break;
            }
        }

        @Override
        public void onError(String error) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults, mCallback);
    }

    /**
     * 底部虚拟按键栏的高度
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    private void setkeyBoardHeight(){

        if(Preferences.getKeyBoardHeight() == 0){ //键盘类型/高度： text/842; textPassword/883
            Rect r = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int statusBarH = r.top; //状态栏的高度
            int screenH = getWindow().getDecorView().getRootView().getHeight();
            int keyBoardH =  screenH - r.bottom - getSoftButtonsBarHeight();

            if(keyBoardH >0){
                Preferences.setKeyBoardHeight(keyBoardH);
            }
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            setkeyBoardHeight();
            updateViewsState();
        }
    };

    private TextWatcher textPasswordWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateViewsState();
        }
    };

    private void updateViewsState(){
        String phoneStr = et_phone.getText().toString();
        String passwordStr = et_password.getText().toString();

        phone_del_view.setVisibility(TextUtils.isEmpty(phoneStr) ? View.GONE : View.VISIBLE);
        password_del_view.setVisibility(TextUtils.isEmpty(passwordStr) ? View.GONE : View.VISIBLE);

        if(TextUtils.isEmpty(phoneStr) || TextUtils.isEmpty(passwordStr)){
            btn_login.setEnabled(false);
        }else {
            btn_login.setEnabled(true);
        }
    }

    private void setAnimation(){
        rotateAnimation = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                User loginer = Preferences.getCurrentLoginer();
                if(loginer == null) {
                    loginer = User.getUserLastLogin();
                }

                if(loginer != null && !TextUtils.isEmpty(loginer.getUserCode())) {
                    et_phone.setText(loginer.getUserCode());
                    et_phone.setSelection(et_phone.length());
                    et_password.requestFocus();
                }else {
                    et_phone.setText("");
                    et_phone.requestFocus();
                }
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoKey();
            }
        }, 500);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phone_del_view:{
                et_phone.setText("");
            }break;
            case R.id.password_del_view:{
                et_password.setText("");
            }break;
            case R.id.btn_login:{
                setHideKey();
                processLogin();
//                startActivity(new Intent(this, ThirdActivity.class));
            }break;
            case R.id.tv_config:{
                startActivity(new Intent(this, ConfigActivity.class));
            }break;
            default:break;
        }
    }

    private boolean checkDataValid(){
        final String phoneStr = et_phone.getText().toString().trim();
        final String pwdStr = et_password.getText().toString().trim();

        if(!Utils.isValidIMAccount(phoneStr)){
            et_phone.setSelection(et_phone.length());
            et_phone.requestFocus();
            showToast("请输入账号: 8-16位字母和数字组合");
            return false;
        }

        if(!Utils.isValidPassword(pwdStr)) {
            et_password.setSelection(et_password.length());
            et_password.requestFocus();
            showToast("密码应为8-16位字母和数字组合");
            return false;
        }

        return true;
    }

    private void processLogin() {
        setHideKey();

        if (TextUtils.isEmpty(NetConstants.HostName)) {
            showToast("请先在配置服务器地址");
            return;
        }

        if(!checkDataValid()){
            return;
        }

        String account = et_phone.getText().toString().trim();
        final String passwordStr = et_password.getText().toString().trim();
        final String sha256Pwd = Utils.stringToDigest("SHA-256", passwordStr);
        IMLoginManager.instance().login(account, sha256Pwd);
    }

//    public void onEventMainThread(LoginEvent event) {
    public void onEvent(LoginEvent event) {
        LoginEvent.Event type = event.getType();
        switch (type) {
            case LOGINING: {
                loading_process.setVisibility(View.VISIBLE);
                loading_process.startAnimation(rotateAnimation);
                Utils.showProgressDialog(mContext, "登录中，请稍候...");
            }break;
            case LOGIN_OK: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading_process.setVisibility(View.GONE);
                        loading_process.clearAnimation();
                        Utils.dismissProgressDialog();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String error = event.getError().getMessage();
                        showToast(error);
                        loading_process.setVisibility(View.GONE);
                        loading_process.clearAnimation();
                        Utils.dismissProgressDialog();

                        TdrException tdrException = (TdrException)event.getError();
                        if(tdrException == null) return;

                        int errorCode = tdrException.getErrorCode();
                        if(errorCode == 2203) {
                            et_password.setSelection(et_password.length());
                            et_password.requestFocus();
                        } else if(errorCode == 2201){
                            et_phone.setSelection(et_phone.length());
                            et_phone.requestFocus();
                        }
                    }
                });
            }break;

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy(): " + this);
        mHandler.removeCallbacksAndMessages(null);
    }

}
