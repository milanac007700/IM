package com.milanac007.demo.im.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** 声音格数画指示器
 * Created by zqguo on 2016/10/17.
 */
public class VolumeView extends View {
    public static  final String TAG = "VolumeView";
    private Paint mPaint;
    private int mVulumeValue = 0;
    private final int MAX_VOLUME_NUM = 6;
    private int mLineHeight = 0;

    public VolumeView(Context context) {
        super(context);
        init();
    }

    public VolumeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //抗锯齿
        mPaint.setColor(Color.WHITE);
        mVulumeValue = 0;
    }

    public void setVulumeValue(int vulumeValue){
        mVulumeValue = vulumeValue;
        mLineHeight = getHeight()/MAX_VOLUME_NUM;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int minWidth = 30; //空余量
        int dx = (getWidth()-minWidth)/6;
        for(int i=0; i<mVulumeValue; i++){
            int top = getHeight()-mLineHeight*(i+1);
            int bottom = top + mLineHeight*3/5; //剩余的2/5为间隙
            canvas.drawRect(0f, top, dx*(i+1), bottom, mPaint);
        }
    }
}
