package com.milanac007.demo.im.ui;

/**
 * Created by zqguo on 2017/4/27.
 */

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 解决长按时同时相应onLongClick和ClickableSpan的onClick事件的问题
 * touch时间不大于500ms才触发 onClick
 */
public class LinkMovementClickMethod extends LinkMovementMethod {
    private long lastClickTime;
    private static final long CLICK_DELAY = 500;

    public static LinkMovementClickMethod getInstance(){
        if(null == sInstance){
            sInstance = new LinkMovementClickMethod();
        }
        return sInstance;
    }

    private static LinkMovementClickMethod sInstance;


    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN){
            int x = (int)event.getX();
            int y = (int)event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    long delay = System.currentTimeMillis() - lastClickTime;
                    if(delay <= CLICK_DELAY){
                        link[0].onClick(widget);
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                    lastClickTime = System.currentTimeMillis();
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}