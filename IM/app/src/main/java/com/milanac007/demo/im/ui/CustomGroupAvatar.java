package com.milanac007.demo.im.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqguo on 2017/5/22.
 */
public class CustomGroupAvatar extends AppCompatImageView {
    private Paint mPaint;
    private Paint.FontMetricsInt mFontMetricsInt;
    private String[] mMemberAvatars;
    private List<UserEntity> mMembers;
    private Bitmap groupAvatarBmp;
    private int mPadding = CommonFunction.dip2px(2);

    public CustomGroupAvatar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGroupAvatar(Context context) {
        this(context, null);
    }

    public String[] getMemberAvatars() {
        return mMemberAvatars;
    }

    public void setMemberAvatars(String[] avatarList) {
        mMemberAvatars = avatarList;
        Canvas cv = new Canvas(groupAvatarBmp);
        drawGroupAvatar(cv);
//        invalidate();
    }

    public void setMemberAvatars(List<UserEntity> users) {

        mMembers = new ArrayList<>();

        if(users.size() <= 9){
            mMembers.addAll(users);
        }else {
            for(int i=0, size=9; i<size; i++){
                mMembers.add(users.get(i));
            }
        }

        mMemberAvatars = new String[mMembers.size()];
        int i = 0;
        for(UserEntity user : mMembers){
            if(TextUtils.isEmpty(user.getAvatar()))
                mMemberAvatars[i++] = user.getMainName();
            else
                mMemberAvatars[i++] = user.getAvatar();
        }

        Canvas cv = new Canvas(groupAvatarBmp);
        drawGroupAvatar(cv);
//        invalidate();
    }


    @TargetApi(16)
    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.line_e3));
        groupAvatarBmp = Bitmap.createBitmap(CommonFunction.dip2px(45), CommonFunction.dip2px(45), Bitmap.Config.RGB_565);
        setImageBitmap(groupAvatarBmp);
    }

    private Bitmap getSrcBitmap(String avatar){
        Bitmap srcBmp = null;
        if(avatar.contains("/")){
            String filePath = CommonFunction.getDirUserTemp() + File.separator + CommonFunction.getImageFileNameByUrl(avatar);
            srcBmp = CacheManager.getInstance().getBitmapFormCache(filePath);

            if(srcBmp != null){
                return srcBmp;
            }else if(new File(filePath).exists()){
                srcBmp = CacheManager.getInstance().addCacheData(filePath, 100, 100);
                return srcBmp;
            }
        }

//        srcBmp = BitmapFactory.decodeResource(getResources(), R.drawable.male);
        srcBmp = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        Canvas cv = new Canvas(srcBmp);
        mPaint.setColor(getResources().getColor(R.color.head_icon_bg_male));
        cv.drawRect(0,0, srcBmp.getWidth(), srcBmp.getHeight(), mPaint);//绘制背景

        String text = avatar.substring(avatar.length()-1);
        mPaint.setColor(getResources().getColor(R.color.white));
        mPaint.setStrokeWidth(3);
        mPaint.setTextSize(60);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Paint.Style.STROKE);
        mFontMetricsInt = mPaint.getFontMetricsInt();
        int y = (srcBmp.getHeight() - mFontMetricsInt.ascent - mFontMetricsInt.descent) / 2;
        cv.drawText(text, srcBmp.getWidth()/2, y, mPaint);
        return srcBmp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGroupAvatar(canvas);
    }

    private void drawGroupAvatar(Canvas canvas){
        int width = groupAvatarBmp.getWidth(), height = groupAvatarBmp.getHeight();

        canvas.drawRect(0,0, width, height, mPaint);//绘制背景
        Bitmap thumbBmp = null;

        switch (mMemberAvatars.length){
            case 3:
            case 4:{
                int w = (width - mPadding*3)/2;

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);

                if(mMemberAvatars.length == 4){
                    canvas.drawBitmap(thumbBmp, mPadding, mPadding, null);
                }else {
                    canvas.drawBitmap(thumbBmp, mPadding*2+w/2, mPadding, null);
                }

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[1]), w, w);
                canvas.drawBitmap(thumbBmp, mPadding, w+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[2]), w, w);
                canvas.drawBitmap(thumbBmp, mPadding*2 + w, w+mPadding*2, null);

                if(mMemberAvatars.length == 4){
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[3]), w, w);
                    canvas.drawBitmap(thumbBmp, mPadding*2+w, mPadding, null);
                }

            }break;

            case 5:
            case 6:{
                int w = (width - mPadding*4)/3;

                if(mMemberAvatars.length == 5){
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);
                    canvas.drawBitmap(thumbBmp, mPadding*2+w/2, mPadding+w/2, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[1]), w, w);
                    canvas.drawBitmap(thumbBmp, 3*w/2+mPadding*3,  mPadding+w/2, null);
                }else {
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);
                    canvas.drawBitmap(thumbBmp, mPadding, mPadding+w/2, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[1]), w, w);
                    canvas.drawBitmap(thumbBmp, w+mPadding*2, mPadding+w/2, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[5]), w, w);
                    canvas.drawBitmap(thumbBmp, 2*w+mPadding*3, mPadding+w/2, null);
                }

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[2]), w, w);
                canvas.drawBitmap(thumbBmp, mPadding, 3*w/2+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[3]), w, w);
                canvas.drawBitmap(thumbBmp, w+mPadding*2, 3*w/2+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[4]), w, w);
                canvas.drawBitmap(thumbBmp, 2*w+mPadding*3, 3*w/2+mPadding*2, null);

            }break;

            case 7:
            case 8:
            case 9:{
                int w = (width - mPadding*4)/3;

                if(mMemberAvatars.length == 7){
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);
                    canvas.drawBitmap(thumbBmp, mPadding*2+w, mPadding, null);
                }else if(mMemberAvatars.length == 8){
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);
                    canvas.drawBitmap(thumbBmp, w/2+mPadding*2, mPadding, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[7]), w, w);
                    canvas.drawBitmap(thumbBmp, 3*w/2+mPadding*3, mPadding, null);

                }else {
                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[0]), w, w);
                    canvas.drawBitmap(thumbBmp, mPadding, mPadding, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[7]), w, w);
                    canvas.drawBitmap(thumbBmp, w+mPadding*2, mPadding, null);

                    thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[8]), w, w);
                    canvas.drawBitmap(thumbBmp, 2*w+mPadding*3, mPadding, null);
                }

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[1]), w, w);
                canvas.drawBitmap(thumbBmp, mPadding, w+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[2]), w, w);
                canvas.drawBitmap(thumbBmp, w+mPadding*2, w+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[3]), w, w);
                canvas.drawBitmap(thumbBmp, 2*w+mPadding*3, w+mPadding*2, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[4]), w, w);
                canvas.drawBitmap(thumbBmp, mPadding, 2*w+mPadding*3, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[5]), w, w);
                canvas.drawBitmap(thumbBmp, w+mPadding*2, 2*w+mPadding*3, null);

                thumbBmp = ThumbnailUtils.extractThumbnail(getSrcBitmap(mMemberAvatars[6]), w, w);
                canvas.drawBitmap(thumbBmp, 2*w+mPadding*3, 2*w+mPadding*3, null);

            }break;

            default:
                break;
        }

        //TODO
//        canvas.save(Canvas.ALL_SAVE_FLAG); //保存全部图层
        canvas.save();
        canvas.restore();
    }

    public Bitmap getBitmap(){
        return groupAvatarBmp;
    }

}
