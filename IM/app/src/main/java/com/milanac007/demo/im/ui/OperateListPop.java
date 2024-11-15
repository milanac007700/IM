package com.milanac007.demo.im.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;

/**
 * Created by zqguo on 2016/10/9.
 */
public class OperateListPop {

    private PopupWindow mPopupWindow;
    private LayoutInflater mInflater;
    private Context mContext;
    private LinearLayout menu_item_layout;
    private View mView;
    private Drawable mBgDrawable;

    public OperateListPop(int popWidthPx, Context context, Drawable bgDrawable){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mBgDrawable = bgDrawable;
        initView(popWidthPx);
    }

    public OperateListPop(int popWidthPx, Context context){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        initView(popWidthPx);
    }

    public OperateListPop(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        initView(CommonFunction.getWidthPx()/2);
    }

    private void initView(int popWidthPx) {
        mView = mInflater.inflate(R.layout.operate_pop_layout, null);
        menu_item_layout = (LinearLayout) mView.findViewById(R.id.menu_item_layout);

        mPopupWindow = new PopupWindow(mView,popWidthPx , WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOutsideTouchable(true);

        if(mBgDrawable != null){
            mPopupWindow.setBackgroundDrawable(mBgDrawable);
        }else {
            mPopupWindow.setBackgroundDrawable(new PaintDrawable(R.color.camera_bg)); // color4f000
        }

    }

    public void show(View parentView, int xOff, int yOff){
        if (parentView == null || mPopupWindow == null || mPopupWindow.isShowing()) {
            return;
        }
        mPopupWindow.showAsDropDown(parentView,xOff,yOff);
    }

    public void setData(ArrayList<PopItem> popItems) {
        int size = popItems.size();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonFunction.dip2px(45));
        layoutParams.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams lineParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonFunction.dip2px(1f));

        for(int i=0; i<size; i++){
            PopItem popItem = popItems.get(i);
            TextView popItemView = new TextView(mContext);
            Drawable drawable = mContext.getResources().getDrawable(popItem.resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            popItemView.setCompoundDrawables(drawable, null, null, null);
            popItemView.setText(popItem.name);
//            popItemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            popItemView.setTextSize(18);//默认sp为单位
            popItemView.setTextColor(mContext.getResources().getColor(R.color.white));
            popItemView.setCompoundDrawablePadding(CommonFunction.dip2px(10f));
            popItemView.setGravity(Gravity.CENTER_VERTICAL);

            popItemView.setOnClickListener(popItem.listener);
            popItemView.setLayoutParams(layoutParams);
            menu_item_layout.addView(popItemView);

            if(i < size-1){
                View line = new View(mContext);
                line.setBackgroundColor(mContext.getResources().getColor(R.color.black));
                line.setLayoutParams(lineParam);
                menu_item_layout.addView(line);
            }
        }
        menu_item_layout.setPadding(CommonFunction.dip2px(15f), CommonFunction.dip2px(10f), CommonFunction.dip2px(15f), CommonFunction.dip2px(0f));
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setPopBackground(int resId) {
        mView.setBackgroundResource(resId);
        mPopupWindow.setBackgroundDrawable(new PaintDrawable(Color.TRANSPARENT));
        menu_item_layout.setPadding(CommonFunction.dip2px(5f), CommonFunction.dip2px(15f), CommonFunction.dip2px(5f), CommonFunction.dip2px(0f));
    }

    public void setPopBackground(Drawable bg) {
        mPopupWindow.setBackgroundDrawable(bg);
        menu_item_layout.setPadding(CommonFunction.dip2px(5f), CommonFunction.dip2px(15f), CommonFunction.dip2px(5f), CommonFunction.dip2px(0f));
    }


    public class PopItem{
        int resId;
        String name;
        View.OnClickListener listener;

        public PopItem(int resId, String name){
            this.resId = resId;
            this.name = name;
        }

        public PopItem(int resId, String name, View.OnClickListener listener){
            this.resId = resId;
            this.name = name;
            this.listener = listener;
        }
    }

}
