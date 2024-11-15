package com.milanac007.demo.im.ui;

import android.app.Activity;
import android.graphics.drawable.PaintDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.HandlerPost;


/**
 * Created by zqguo on 2016/10/17.
 */
public class VoiceRecordPop {
    public static final String TAG = VoiceRecordPop.class.getName();

    private PopupWindow mPopWindow;
    private LayoutInflater mInflater;
    private Activity mActivity;
    private TextView voice_pop_text;
    private VolumeView voice_pop_viewer;
    private LinearLayout voice_pop_layout;
    private ImageView voice_pop_img;
    private View voice_outside_pop_layout;
    private final View parentView;
    boolean isTipPop = false;

    public VoiceRecordPop(Activity context){
        mActivity = context;
        mInflater = LayoutInflater.from(mActivity);
        parentView = mActivity.findViewById(android.R.id.content);
        init();
        setListener();
    }

    private void init(){
        View view = mInflater.inflate(R.layout.voice_pop, null);
        voice_pop_text = (TextView) view.findViewById(R.id.voice_pop_text);
        voice_pop_viewer = (VolumeView) view.findViewById(R.id.voice_pop_view);
        voice_pop_layout = (LinearLayout) view.findViewById(R.id.voice_pop_layout);
        voice_pop_img = (ImageView) view.findViewById(R.id.voice_pop_img);
        voice_outside_pop_layout =  view.findViewById(R.id.voice_outside_pop_layout);
        mPopWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        mPopWindow.setBackgroundDrawable(new PaintDrawable(-000000));
    }

    private void setListener(){
        voice_outside_pop_layout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mPopWindow.update();
    }

    //倒计时 显示
    public void setRemainTime(int time){
        voice_pop_text.setBackgroundResource(R.color.transparent);
        voice_pop_text.setText(String.format("还可以说%d秒", time));
    }

    //分贝转换 0-120
    public void setVolumeValue(int value){
        int relativeValue = 1;
        if(value <= 60){
            relativeValue = 1;
        }else if(value >60 && value <= 65){
            relativeValue = 2;
        }else if(value >65 && value <= 70){
            relativeValue = 3;
        }else if(value >70 && value <= 75){
            relativeValue = 4;
        }else if(value >75 && value <= 80){
            relativeValue = 5;
        }else {
            relativeValue = 6;
        }

        voice_pop_viewer.setVulumeValue(relativeValue);
    }

    public void setRecordAndCancelView(boolean isCancel){
        isTipPop = false;
        if(!isCancel){
            voice_pop_layout.setVisibility(View.VISIBLE);
            voice_pop_img.setVisibility(View.GONE);
            voice_pop_text.setBackgroundResource(R.color.transparent);
            voice_pop_text.setText(R.string.m_finger_slide_up);
        }else {
            voice_pop_layout.setVisibility(View.GONE);
            voice_pop_img.setVisibility(View.VISIBLE);
            voice_pop_img.setImageResource(R.mipmap.voice_img_back);
            voice_pop_text.setBackgroundResource(R.drawable.voice_pop_text_bg);
            voice_pop_text.setText(R.string.m_loosen_cancel);
        }
    }

    public void setShortRecordView(){

        if(mPopWindow != null){
            mPopWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        }

        isTipPop = true;
        voice_pop_layout.setVisibility(View.GONE);
        voice_pop_img.setVisibility(View.VISIBLE);
        voice_pop_img.setImageResource(R.mipmap.voice_img_stop);
        voice_pop_text.setBackgroundResource(R.color.transparent);
        voice_pop_text.setText(R.string.m_voice_too_short);
        new HandlerPost(2000){
            @Override
            public void doAction() {
                if(isTipPop)
                    dismiss();
            }
        };
    }

    public void showPop(){
        setRecordAndCancelView(false); //init View
        if(mPopWindow != null){
            mPopWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        }
    }

    public void dismiss(){
        if(mPopWindow != null){
            mPopWindow.dismiss();
        }
    }
}
