package com.milanac007.demo.im.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;


public class ThirdActivity extends AppCompatActivity {
    private ListView lv;
    private List<String> list;
    private EditText msg_edit;
    private int mKeyBoardHeight;
    private View rl_title;
    private WindowManager wm;
    private View titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        createTitleBar();

        rl_title = findViewById(R.id.rl_title);

        lv = (ListView) findViewById(R.id.lv);
        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("第" + (i + 1) + "项数据！");
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);


        msg_edit = (EditText)findViewById(R.id.msg_edit);
        msg_edit.addTextChangedListener(textWatcher);

        msg_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                lv.scrollTo(0, mKeyBoardHeight);
                return false;
            }
        });

        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setHideKey();
                return false;
            }
        });
    }

    private void createTitleBar() {

        titleView = LayoutInflater.from(this).inflate(R.layout.custom_titlebar, null);
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(titleView);
                finish();
            }
        });
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams lp  = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.x = 0;
        lp.y = 0;
        lp.format = PixelFormat.TRANSPARENT;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if(titleView.getParent() == null) {
            wm.addView(titleView, lp);
        }
    }


    /**
     * 隐藏软键盘
     */
    public void setHideKey() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = findViewById(android.R.id.content);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /**
     * 底部导航栏的高度
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

//        mKeyBoardHeight = Preferences.getKeyBoardHeight();
        mKeyBoardHeight = 842;
        if(mKeyBoardHeight == 0){
            Rect r = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int statusBarH = r.top; //状态栏的高度
            int screenH = getWindow().getDecorView().getRootView().getHeight();
            int keyBoardH =  screenH - r.bottom - getSoftButtonsBarHeight();

            if(keyBoardH >0){
                mKeyBoardHeight = keyBoardH;
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
        }
    };
}
