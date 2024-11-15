package com.milanac007.demo.im.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.milanac007.pickerandpreviewphoto.PhotoPreviewActivity;
import com.example.milanac007.pickerandpreviewphoto.PickerAlbumActivity;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.event.SelfInfoChangeEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.net.CustomFileUpload;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2016/10/25.
 */
public class MyDetailFragment extends BaseFragment implements View.OnClickListener{

    private CircleImageView mMyIco;
    private TextView mNickName;
    private TextView mRongyiNumber;
    private TextView sexView;
    private IMService imService;
    private  int loginId;
    private IMContactManager imContactManager;
    private UserEntity loginerUserEntity;

    private IMServiceConnector conn = new IMServiceConnector() {

        @Override
        public void onIMServiceConnected() {
            if(EventBus.getDefault().isRegistered(MyDetailFragment.this)){
                EventBus.getDefault().unregister(MyDetailFragment.this);
            }
            EventBus.getDefault().registerSticky(MyDetailFragment.this);
            imService = getIMService();
            loginId = imService.getLoginManager().getLoginId();
            imContactManager =  imService.getContactManager();
            setData();
        }

        @Override
        public void onServiceDisconnected() {
            if(EventBus.getDefault().isRegistered(MyDetailFragment.this)){
                EventBus.getDefault().unregister(MyDetailFragment.this);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conn.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        conn.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_detail_fragment, null);
        return view;
    }

    protected void setTitle(TextView mTitle){
        mTitle.setText("我的资料");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView mBack = (TextView)view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        View mMyIcoLayout = view.findViewById(R.id.my_detail_ico_layout);
        View mNicknameLayout = view.findViewById(R.id.my_detail_nickname_layout);
        View mQRCodeLayout = view.findViewById(R.id. my_detail_QRCode_layout);

        mNickName = (TextView)view.findViewById(R.id.my_detail_nickname);
        mRongyiNumber = (TextView)view.findViewById(R.id.my_detail_rongyi_number);

        mMyIco = (CircleImageView)view.findViewById(R.id.my_detail_ico);
        sexView = (TextView)view.findViewById(R.id.sexTextView);

        setTitle(mTitle);

        View[] views = {mBack, mNicknameLayout, mQRCodeLayout, mMyIcoLayout};
        for (View view1: views){
            view1.setOnClickListener(this);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

    }

    private void setData(){
        loginerUserEntity = imContactManager.findContact(loginId);
        if(loginerUserEntity != null){
            mNickName.setText(TextUtils.isEmpty(loginerUserEntity.getMainName()) ? "暂无" : loginerUserEntity.getMainName());
            mRongyiNumber.setText(TextUtils.isEmpty(loginerUserEntity.getUserCode()) ? "暂无" : loginerUserEntity.getUserCode());
            sexView.setText(loginerUserEntity.getGender() == 1 ? "男" : loginerUserEntity.getGender() == 2 ? "女": "未知");
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(mMyIco, loginerUserEntity);
                }
            });
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_MY_DETAIL;
    }


    private void sendIM(String extNo) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_MODIFY_PERSONALINFO, 0, extNo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_head1_back: {
                onBack();
            }break;
            case R.id.my_detail_nickname_layout: {
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_MODIFY_PERSONALINFO, 0, "username");
            }break;
            case R.id.my_detail_QRCode_layout: {
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SHOW_QRCODE, 0, loginerUserEntity.getSessionKey());
            }break;
            case R.id.my_detail_ico_layout: {
                onClickCropHeader();
            }break;
            default:
                break;
        }
    }

    private String mHeadIcoLocalPath;

    private void onClickCropHeader(){
        Intent intent = new Intent(mActivity, PickerAlbumActivity.class);
        intent.putExtra(PhotoPreviewActivity.MODE, PickerAlbumActivity.PICKER_HEADICO_FROM_ALBUM_CODE);
        startActivityForResult(intent, PickerAlbumActivity.PICKER_ALBUM_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PickerAlbumActivity.PICKER_ALBUM_CODE){
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> filePaths = data.getStringArrayListExtra(PickerAlbumActivity.SELECTED_KEY);
                mHeadIcoLocalPath = filePaths.get(0);
                sendProcess();
            }
        }
    }

    private void sendProcess(){
        CommonFunction.showProgressDialog(getActivity(), "请稍候");

        final File cropfile = new File(mHeadIcoLocalPath);
        String suffix = CommonFunction.getSuffix(mHeadIcoLocalPath);
        long fileSize= cropfile.length();

        final String url = NetConstants.URL_UPLOADFILE;
        int fileType = 3; //文件类型fileType（音频1，视频2，图片3，可执行文件等其他文件类型为4）
        int useType = 5; //文件用途useType（私聊文件1、群文件2、公告文件3、朋友圈文件4，私有文件5等）(只有5是长期保留)
        List<File> files = new ArrayList<File>();
        files.add(cropfile);

        CustomFileUpload fileUpload = new CustomFileUpload(url, files, fileType, useType, new CustomFileUpload.UploadListener(){
            @Override
            public void onUploadEnd(JSONObject result) {
                CommonFunction.dismissProgressDialog();
                boolean success = result.getBoolean("success");
                if(success){
                    JSONArray pathList = result.getJSONArray("uploadUrl");
                    if(pathList != null && !pathList.isEmpty()){
                        cropfile.delete();
                        String fileUrl = pathList.getJSONObject(0).getString("path").replace(NetConstants.FILE_SERVER_URL, "");
                        //TODO
//                        imContactManager.ReqModifyAvatar(fileUrl, new Packetlistener() {
//                            @Override
//                            public void onSuccess(Object response) {
//                                CommonFunction.dismissProgressDialog();
//                                try{
//                                    IMBuddy.IMChangeAvatarRsp imChangeAvatarRsp = IMBuddy.IMChangeAvatarRsp.parseFrom((CodedInputStream) response);
//                                    onRepModifyAvatar(imChangeAvatarRsp);
//                                }catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onFaild() {
//                                CommonFunction.dismissProgressDialog();
//                                CommonFunction.showToast("修改头像失败");
//                            }
//
//                            @Override
//                            public void onTimeout() {
//                                CommonFunction.dismissProgressDialog();
//                                CommonFunction.showToast("修改头像失败：超时");
//                            }
//                        });

                    }

                }else {
                    String status = result.getString("status");
                    if(status.equals("401")){ //"message": "Authorize error,code is 4000002,4000002"
                        CommonFunction.showToast(String.format("登录过期，请重新登录"));
                    }else {
                        String errorMsg = result.getString("error");
                        CommonFunction.showToast(String.format("发送图片失败：%s", errorMsg));
                    }
                }
            }

            @Override
            public void onProgress(int value) {
                //TODO
                Logger.getLogger().d("上传进度: %d%s", value, "%");
            }
        });

        fileUpload.execute();

    }

    //TODO
    //修改当前用户头像rsp
//    public void onRepModifyAvatar(IMBuddy.IMChangeAvatarRsp imChangeAvatarRsp){
//        int resultCode = imChangeAvatarRsp.getResultCode(); //结果码，0:successed 1:failed
//        if(resultCode == 0 && imChangeAvatarRsp.getUserId() == loginerUserEntity.getPeerId()){
//            String avatarUrl = imChangeAvatarRsp.getAvatarUrl();
//            loginerUserEntity.setAvatar(avatarUrl);
//            loginerUserEntity.setAvatarLocalPath(mHeadIcoLocalPath);
//
//            // UPDATE DB
//            DBInterface.instance().updateUserSingleColumn(loginerUserEntity.getPeerId(), UserDao.Properties.Avatar, avatarUrl);
//            DBInterface.instance().updateUserSingleColumn(loginerUserEntity.getPeerId(), UserDao.Properties.AvatarLocalPath, mHeadIcoLocalPath);
//
//            User.updateSingleColumn("mUserIconUrl", avatarUrl);
//            User.updateSingleColumn("mHeadIcoLocalPath", mHeadIcoLocalPath);
//            User lastUser = User.getUserLastLogin();
//            User.setImConfigUser(lastUser);
//            Preferences.setUser(lastUser);
//
//            IMLoginManager.instance().setLoginInfo(loginerUserEntity);
//
//            EventBus.getDefault().post(new SelfInfoChangeEvent());
//
//        }else {
//            CommonFunction.showToast("修改头像失败");
//        }
//    }

    public void onEventMainThread(SelfInfoChangeEvent event){
        setData();
    }

}
