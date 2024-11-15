package com.milanac007.demo.im.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zqguo on 2017/1/18.
 */
public class CustomRoundProgressBar extends View {

    private Paint mPaint;
    private float mValue = 0;
    public CustomRoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width/2, height/2, width/2, mPaint);

        //画扇形：
        // 设置个新的长方形，0为左上点的x坐标，0为左上点的y坐标；200为右下点的 x坐标，200为右下点的y坐标。
//        RectF oval2 = new RectF(0, 0, width, height);
//        // 画弧，第一个参数是RectF：该类第二个参数是角度的开始，第三个参数是多少度，第四个参数是真的时候画扇形，是假的时候画弧线
//        canvas.drawArc(oval2, 0, 130, true, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        RectF oval = new RectF(2, 2, width-4, height-4);
        canvas.drawArc(oval, 270, 360*mValue, true, mPaint);
//        try {
//            canvas.restore();
//        }catch (IllegalStateException e){
//            e.printStackTrace();
//        }
    }

    public void setValue(float value){
        mValue = value;
        invalidate();
    }

}
