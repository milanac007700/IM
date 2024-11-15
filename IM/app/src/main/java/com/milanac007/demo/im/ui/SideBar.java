package com.milanac007.demo.im.ui;

/**
 * Created by zqguo on 2016/9/14.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.UserEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SideBar extends View {
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private int choose = -1;// 选中
    private Paint paint = new Paint();
    private Context context;
    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SideBar(Context context) {
        super(context);
        this.context = context;
    }


    private List<UserEntity> mAllUsers = new ArrayList<>();
    private ChineseCharComparator mCmp = new ChineseCharComparator();
    protected Character[] mSectionLetters;
    protected int[] mSectionIndices;

    /**
     * 得到所有名字首字母的分类索引：26个英文字母中的那几个的索引
     * @return
     */
    private int[] getSectionIndices() {
        if (mAllUsers.size() == 0) {
            return new int[0];
        }
        ArrayList<Integer> sectionIndices = new ArrayList<>();
        char lastFirstChar = mAllUsers.get(0).getPinyinElement().pinyin.charAt(0);
        sectionIndices.add(0);
        int len = mAllUsers.size();
        for (int i = 1; i < len; i++) {
            char pin = mAllUsers.get(i).getPinyinElement().pinyin.charAt(0);
            if (pin != lastFirstChar) {
                lastFirstChar = pin;
                sectionIndices.add(i);
            }
        }
        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    /**
     * 得到mList中每个不同名字 的拼音的第一个字母的数组
     * @return
     */
    private Character[] getSectionLetters() {
        Character[] letters = new Character[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            letters[i] = mAllUsers.get(mSectionIndices[i]).getPinyinElement().pinyin.charAt(0);
        }
        return letters;
    }

    public void bindData(List<? extends UserEntity> allUsers) {

        if (allUsers != null) {
            mAllUsers.clear();
            mAllUsers.addAll(allUsers);
            Collections.sort(mAllUsers, mCmp);
        }

        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }


//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        // 获取焦点改变背景颜色.
//        int height = getHeight();// 获取对应高度
//        int width = getWidth(); // 获取对应宽度
//        int singleHeight = height / b.length;// 获取每一个字母的高度
//
//        for (int i = 0; i < mSectionLetters.length; i++) {
//            paint.setColor(Color.parseColor("#696969"));
//            // paint.setColor(Color.WHITE);
//            paint.setTypeface(Typeface.DEFAULT);
//            paint.setAntiAlias(true);
//
//            paint.setTextSize(30);
//            // 选中的状态
//            if (i == choose) {
//                paint.setColor(Color.parseColor("#3399ff"));
//                paint.setFakeBoldText(true);
//            }
//            // x坐标等于中间-字符串宽度的一半.
//            float xPos = width / 2 - paint.measureText(mSectionLetters[i].toString()) / 2;
//            float startXpos = (b.length-mSectionLetters.length)*singleHeight/2;
//            float yPos = startXpos + singleHeight * i ;
//
//            canvas.drawText(mSectionLetters[i].toString(), xPos, yPos, paint);
//            paint.reset();// 重置画笔
//        }
//
//    }

    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / b.length;// 获取每一个字母的高度

        for (int i = 0; i < b.length; i++) {
            paint.setColor(Color.parseColor("#696969"));
            // paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setAntiAlias(true);

            if (singleHeight >= 40) {
                paint.setTextSize(30);
            } else if (singleHeight >= 20) {
                paint.setTextSize(20);
            } else {
                paint.setTextSize(height / b.length + 1);
            }
            // 选中的状态
            if (i == choose) {
                paint.setColor(Color.parseColor("#3399ff"));
                paint.setFakeBoldText(true);
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            if (singleHeight < 20) {
                yPos = singleHeight * i + singleHeight + 2;
            }
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();// 重置画笔
        }

    }

    private CharacterIndexDialog indexDialog;
    private void showIndexDialog(String str){
        if(indexDialog == null){
            indexDialog = new CharacterIndexDialog(context);
        }
        indexDialog.setText(str);
        indexDialog.show();
    }

    private void hideIndexDialog(){
        if(indexDialog != null)
            indexDialog.dismiss();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
            case MotionEvent.ACTION_UP:
                choose = -1;
                invalidate();
                hideIndexDialog();
                setBackgroundColor(Color.TRANSPARENT);
                break;

            default:
                setBackgroundColor(context.getResources().getColor(R.color.title_divider));
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {

                        showIndexDialog(b[c]);

                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        choose = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}

class ChineseCharComparator implements Comparator<Object> {
    public int compare(Object o1, Object o2) {
        UserEntity c1 = (UserEntity) o1;
        UserEntity c2 = (UserEntity) o2;

        String str1 = c1.getPinyinElement().pinyin;
        String str2 = c2.getPinyinElement().pinyin;

        return str1.compareToIgnoreCase(str2);
    }
}
