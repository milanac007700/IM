package com.milanac007.demo.im.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.Utils;
import com.milanac007.scancode.QRCodeUtil;

/**
 * Created by zqguo on 2016/11/24.
 */
public class SelfQRCodeDialog extends Dialog {
    private TextView buddy_name;
    private ImageView mQRCodeImage;
    private int mDialogHeightInDp = 310;
    private TextView rongyihao;
    private CircleImageView iv_photo;
    private final String mQRCodeStr;

    public SelfQRCodeDialog(Context context, UserEntity userEntity) {
        super(context, R.style.Loadingdialog);
        setContentView(R.layout.self_qrcode_dialog_layout);
        mQRCodeStr = userEntity.getSessionKey();
        initView(userEntity);
    }

    private void initView(UserEntity imBuddy) {
        refreshWindow();
        iv_photo = (CircleImageView) findViewById(R.id.iv_photo);
        buddy_name = (TextView) findViewById(R.id.buddy_name);
        rongyihao = (TextView) findViewById(R.id.rongyihao);
        mQRCodeImage = (ImageView)findViewById(R.id.qrcode_image);

        if(imBuddy != null) {
            rongyihao.setText(String.format("IM账号：%s", imBuddy.getUserCode()));
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(iv_photo, imBuddy);
                }
            });
            buddy_name.setText(imBuddy.getMainName());
            Bitmap bitmap = QRCodeUtil.createImage(mQRCodeStr);
            mQRCodeImage.setImageBitmap(bitmap);
        }
    }

    public void refreshWindow(){
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP| Gravity.CENTER_HORIZONTAL;
        params.y = 200;
        params.width = Utils.getScreenWidth()*8/10;
        // dialog 的最大高度为屏幕高度的 6/10；
        int maxHeightInPx = Utils.getScreenHeight()*6/10;
        int dialogHeightInPx = Utils.dp2dx(mDialogHeightInDp);
//        params.height = Math.min(maxHeightInPx, dialogHeightInPx);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

}
