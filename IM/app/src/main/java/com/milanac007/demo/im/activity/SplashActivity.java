package com.milanac007.demo.im.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.milanac007.demo.im.R;
import com.milanac007.scancode.StatusBarUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 解决部分机型 每次点击桌面图标都会重新启动应用的Bug，如小米5,红米K20 pro、K30Pro Android10.0，
         * 解决方法：当启动launch页的时候去判断这个lanuch页面是不是任务栈中的第一个activity,如果是，则启动，如果不是则不运行。
         * 注：三星not4 Android 4.4上，此方法一直返false,故从设置页退出后，返不到LoginActivity页，直接退出了。
         * 还需再通过判断intent是不是启动launcher，可避免此问题
         */
        if(!isTaskRoot()) {
            Intent intent = getIntent();
            if(intent != null) {
                String action = intent.getAction();
                if(intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

//        setFullscreen(true);
        customStatusBar();
        setContentView(R.layout.activity_splash);
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if(message.what == 0) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
            return true;
        }
    });

    protected void customStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, Color.TRANSPARENT, 0);
//            StatusBarUtil.setLightMode(this);
            StatusBarUtil.setFullScreenLightMode2(this);
        }
    }

    private void setFullscreen(boolean enable) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if(enable) {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }else {
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(lp);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}