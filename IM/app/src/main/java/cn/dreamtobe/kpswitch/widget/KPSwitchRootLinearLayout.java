package cn.dreamtobe.kpswitch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import cn.dreamtobe.kpswitch.handler.KPSwitchRootLayoutHandler;

public class KPSwitchRootLinearLayout extends LinearLayout {

    private KPSwitchRootLayoutHandler conflictHandler;

    private void init() {
        conflictHandler = new KPSwitchRootLayoutHandler(this);
    }

    public KPSwitchRootLinearLayout(Context context) {
        super(context);
        init();
    }

    public KPSwitchRootLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KPSwitchRootLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("### RootLayout: onMeasure()");
        conflictHandler.handleBeforeMeasure(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
