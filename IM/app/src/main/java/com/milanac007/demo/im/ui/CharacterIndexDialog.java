package com.milanac007.demo.im.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.milanac007.demo.im.R;

/**
 * Created by zqguo on 2017/1/6.
 */
public class CharacterIndexDialog extends Dialog {

    private TextView textView;

    public CharacterIndexDialog(Context context) {
        super(context, R.style.Loadingdialog);
        textView = new TextView(context);
        textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_green_bg)); //context.getResources().getColor(R.color.lightgreen)
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setTextSize(24);
        textView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(params);
        setContentView(textView);
        refreshWindow();
    }

    public void refreshWindow(){
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = 120;
        params.height = 120;
        window.setAttributes(params);
    }

    public void setText(String str){
        if(textView != null){
            textView.setText(str);
        }
    }

}
