package com.milanac007.demo.im.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.scancode.QRCodeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zqguo on 2016/10/14.
 */
public class SimpleZXingFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = "SimpleZXingFragment";
    private TextView buddy_name;
    private ImageView mQRCodeImage;
    private TextView rongyihao;
    private CircleImageView iv_photo;
    private GlideImageView group_avatar;
    private TextView qrcode_label;
    private Bitmap bitmap;
    private TextView mTitle;
    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            setData();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simple_zxing_fragment, null);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView mBack = (TextView)view.findViewById(R.id.fragment_head1_back);
        mBack.setOnClickListener(this);

        mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText("二维码名片");

        TextView mFinish = (TextView)view.findViewById(R.id. fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.drawable.more_operation);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);
        mFinish.setOnClickListener(this);

        iv_photo = (CircleImageView) view.findViewById(R.id.iv_photo);
        group_avatar = (GlideImageView)view.findViewById(R.id.group_avatar);

        buddy_name = (TextView) view.findViewById(R.id.buddy_name);
        rongyihao = (TextView) view.findViewById(R.id.rongyihao);
        mQRCodeImage = (ImageView)view.findViewById(R.id.qrcode_image);

        qrcode_label = (TextView)view.findViewById(R.id.qrcode_label);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });
    }

    private void setData(){
        String sessionKey = bundle.getString("sessionKey");
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);

        if(peerType == DBConstant.SESSION_TYPE_SINGLE){
            UserEntity imBuddy =  imService.getContactManager().findContact(peerId);
            if(imBuddy != null){
                rongyihao.setText(String.format("IM账号：%s", imBuddy.getUserCode()));
                App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                    @Override
                    public void run() {
                        CommonFunction.setHeadIconImageView(iv_photo, imBuddy);
                    }
                });
                buddy_name.setText(imBuddy.getMainName());
                bitmap = QRCodeUtil.createImage(imBuddy.getSessionKey());
                mQRCodeImage.setImageBitmap(bitmap);
                mQRCodeImage.setVisibility(View.VISIBLE);
            }else {
                mQRCodeImage.setVisibility(View.GONE);
            }
        }else { //群
            GroupEntity group =  imService.getGroupManager().findGroup(peerId);
            if(group != null){
                rongyihao.setVisibility(View.GONE);
                iv_photo.setVisibility(View.GONE);
                group_avatar.setVisibility(View.VISIBLE);
                App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                    @Override
                    public void run() {
                        CommonFunction.setHeadIconImageView(group_avatar, group);
                    }
                });
                buddy_name.setText(group.getMainName());
                bitmap = QRCodeUtil.createImage(group.getSessionKey());
                mQRCodeImage.setImageBitmap(bitmap);
                mQRCodeImage.setVisibility(View.VISIBLE);
            }else {
                mQRCodeImage.setVisibility(View.GONE);
            }
            mTitle.setText("群二维码名片");
            qrcode_label.setText("扫一扫，加入该群聊");
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_SIMPLE_ZXING;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back:{
                onBack();
            }break;
            case R.id.fragment_head1_finish:{
                showMoreOperationDialog();
            }break;
        }
    }

    private String[] getRightMenus() {
        String[] menuStr = {"分享二维码", "保存到手机", "扫一扫"};
        return menuStr;
    }

    OperateListDialog operateListDialog;
    private ArrayList<OperateListDialog.OperateItem> operateItems  = new ArrayList<>();

    private void showMoreOperationDialog() {

        final String[] menuStr = getRightMenus();
        if (menuStr == null || menuStr.length <= 0) {
            return;
        }

        if(operateListDialog == null) {
            operateListDialog = new OperateListDialog(mActivity);
            operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
        }
        operateItems.clear();


        int size = menuStr.length;
        for (int i = 0; i< size; i++) {
            final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
            item.setmItemNameStr(menuStr[i]);
            item.setmOperateKey(String.valueOf(i));

            item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
                @Override
                public void clickItem(int position) {
                    switch (Integer.valueOf(item.getmOperateKey())) {
                        case 0: {
                            CommonFunction.showToast(R.string.func_developing);
                            showShare();
                        }
                        break;
                        case 1: {
                            saveToLocal();
                        }
                        break;
                        case 2: {
                            clickScanQRCodeBtn();
                        }
                        break;
                        default:
                            break;
                    }

                    if (operateListDialog != null) {
                        operateListDialog.dismiss();
                    }
                }
            });
            operateItems.add(item);
        }

        operateListDialog.showTitle(false);
        operateListDialog.setGravityType(1);
        operateListDialog.updateOperateItems(operateItems);
        operateListDialog.show();
    }

    private void showShare() {
//        ShareSDK.initSDK(mActivity);
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//
//        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
//        oks.setTitle("标题");
//        // titleUrl是标题的网络链接，QQ和QQ空间等使用
//        oks.setTitleUrl("http://sharesdk.cn");
//        // text是分享文本，所有平台都需要这个字段
//        oks.setText("我是分享文本");
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
//        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl("http://sharesdk.cn");
//        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//        oks.setComment("我是测试评论文本");
//        // site是分享此内容的网站名称，仅在QQ空间使用
//        oks.setSite(getString(R.string.app_name));
//        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//        oks.setSiteUrl("http://sharesdk.cn");
//
//        // 启动分享GUI
//        oks.show(mActivity);
    }

    private void saveToLocal(){
        File f = new File(CommonFunction.getDirUserTemp() + File.separator + CommonFunction.date2Str(new Date(), "yyyyMMddHHmmss") + ".png");
        if(f.exists()){
            f.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();




            CommonFunction.showToast("已保存到" + CommonFunction.getDirUserTemp() + "目录");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void  clickScanQRCodeBtn(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SCAN_QRCODE, 0, null);
    }

}

