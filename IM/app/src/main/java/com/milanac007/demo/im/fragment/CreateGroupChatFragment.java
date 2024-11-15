package com.milanac007.demo.im.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

//import com.mogujie.tt.DB.DBInterface;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.google.gson.Gson;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.manager.IMGroupManager;
import com.milanac007.demo.im.db.manager.IMSessionManager;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.utils.CommonFunction;
//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.protobuf.IMGroup;
//import com.mogujie.tt.protobuf.helper.ProtoBuf2JavaBean;
import com.milanac007.demo.im.ui.CustomGroupAvatar;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.net.CustomFileUpload;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.utils.Preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.milanac007.demo.im.db.manager.IMSocketManager.TIMEOUT_MILLISECONDS;


/**
 * Created by zqguo on 2017/4/6.
 */
public class CreateGroupChatFragment extends ContactsBaseFragment {

    private JSONArray selectedIds;
    private boolean fromSingleChatSetting = false; //来源于单聊设置页面
    private File avatarFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(bundle != null && bundle.containsKey("param")){ //用于控制是否显示contacts_all_list_header

            JSONObject param = JSONObject.parseObject(bundle.getString("param"));

            if(param == null)
                return;

            if(param.containsKey("type"))
                hideContactsHeader = param.getBoolean("type");

            if(param.containsKey("fromSingleChatSetting")){
                fromSingleChatSetting = param.getBoolean("fromSingleChatSetting");
            }

            if(param.containsKey("selected")){
                selectedIds = param.getJSONArray("selected");
            }
        }


    }


    @Override
    protected void initView(View view) {
        super.initView(view);
        mTitle.setText(getResources().getString(R.string.g_create_group_chat));

        search_layout.setVisibility(View.VISIBLE);
        mMyFriendLayout.setVisibility(View.GONE);

        group_chat_img.setVisibility(View.GONE);
        group_chat_name.setText("选择一个群");

        isSingle = false;
        mAdapter.setMode(R.id.MULTIPLE_CHOICE_MODE);
    }


    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_CREATE_GROUP_CHAT;
    }

    protected void onFinishClick(){

        if(!mAdapter.allSelectedObject().isEmpty()){
            showDialog();
        }
        setFinishText(false);
        mAdapter.notifyDataSetChanged();
    }

    protected void setFinishText(boolean isSingle){
        if(mAdapter.allSelectedObject().isEmpty()){
            mFinish.setText("确定");
            mFinish.setEnabled(false);
        }else {
            mFinish.setText(String.format("确定(%d)", mAdapter.allSelectedObject().size()));
            mFinish.setEnabled(true);
        }
    }

    protected void setData(){
        List<UserEntity> contactList = contactMgr.getContactSortedListExecuteMe();
        // 没有任何的联系人数据
        if (contactList.size() <= 0) {
            return;
        }

        List<UserEntity> selectedList = null;
        if(selectedIds !=null && !selectedIds.isEmpty()){
            selectedList = new ArrayList<>();
            for(UserEntity user : contactList){
                if(selectedIds.contains(user.getPeerId()))
                    selectedList.add(user);
            }
        }

        mAdapter.bindData(contactList, selectedList, null);
    }

    protected void showDialog(){

        final List<UserEntity> allSelectedList = (List<UserEntity>)mAdapter.allSelectedObject();

        if(allSelectedList.size() == 1){ //单聊

            if(fromSingleChatSetting){
                CommonFunction.showToast("至少选择两个人");
                return;
            }

            UserEntity imBuddy = allSelectedList.get(0);
            mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, imBuddy.getSessionKey());
        }else {
            CommonFunction.showProgressDialog(getActivity(), "正在发起群聊...");
            createGroup(allSelectedList);
        }
    }



    private void createGroup(List<UserEntity> allSelectedList){

        CommonFunction.showProgressDialog(getActivity(), "请稍候");

        final List<UserEntity> selectedList = new ArrayList<>();
        selectedList.add(contactMgr.findContact(Preferences.getCurrentLoginer().getUuid()));//插入群主
        selectedList.addAll(allSelectedList);


        if(avatarFile != null && avatarFile.exists() && avatarFile.length() > 0){

        }else {
            CustomGroupAvatar avatar = new CustomGroupAvatar(mActivity);
            avatar.setMemberAvatars(selectedList);
            Bitmap avatarBmp = avatar.getBitmap();

            avatarFile = new File(CommonFunction.getDirUserTemp() + File.separator + UUID.randomUUID()+".jpg");
            try {
                OutputStream out = new FileOutputStream(avatarFile);
                avatarBmp.compress(Bitmap.CompressFormat.JPEG, 10, out);//压缩成10%
                CacheManager.getInstance().addBitmapCache(avatarFile.getPath(), avatarBmp, true);
                out.flush();
                out.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }


        final String url = NetConstants.URL_UPLOADFILE;
        int fileType = 3; //文件类型fileType（音频1，视频2，图片3，可执行文件等其他文件类型为4）
        int useType = 5; //文件用途useType（私聊文件1、群文件2、公告文件3、朋友圈文件4，私有文件5等）(只有5是长期保留)
        List<File> files = new ArrayList<File>();
        files.add(avatarFile);

        CustomFileUpload fileUpload = new CustomFileUpload(url, files, fileType, useType, new CustomFileUpload.UploadListener(){
            @Override
            public void onUploadEnd(JSONObject result) {

                boolean success = result.getBoolean("success");
                if(success){
                    JSONArray pathList = result.getJSONArray("uploadUrl");
                    if(pathList != null && !pathList.isEmpty()) {
//                        String groupAvatarUrl = pathList.getJSONObject(0).getString("path").replace(NetConstants.FILE_SERVER_URL, "");
                        String groupAvatarUrl = pathList.getJSONObject(0).getString("path");
                        List<Integer> memberList = new ArrayList<>();
                        String groupName = "";
                        for(int i=0, size = selectedList.size(); i<size; i++){
                            UserEntity userEntity = selectedList.get(i);
                            memberList.add(userEntity.getPeerId());
                            if(i <= 1){
                                groupName += userEntity.getMainName() + "、";
                            }else if(i == 2){
                                groupName += userEntity.getMainName();
                            }
                        }

                        if(selectedList.size() > 3){
                            groupName += "...";
                        }

                        imService.getGroupManager().reqCreateGroup(groupName, groupAvatarUrl, memberList, new Packetlistener(TIMEOUT_MILLISECONDS) {
                            @Override
                            public void onSuccess(Object response) {
                                CommonFunction.dismissProgressDialog();
                                JSONObject rspObject = JSONObject.parseObject((String) response);
                                int resultCode = rspObject.getIntValue("resultCode");
                                if(resultCode != 0) {

                                }else {
                                    JSONObject groupCreateRsp = rspObject.getJSONObject("groupCreateRsp");
                                    onReqCreateGroup(groupCreateRsp);
                                }
                            }

                            @Override
                            public void onTimeout() {
                                CommonFunction.dismissProgressDialog();
                                CommonFunction.showToast(R.string.create_temp_group_failed);
                            }

                            @Override
                            public void onFail(String error) {
                                CommonFunction.dismissProgressDialog();
                                CommonFunction.showToast(R.string.create_temp_group_failed);
                            }
                        });
                    }

                }else {
                    CommonFunction.dismissProgressDialog();
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
                Logger.getLogger().d("上传进度: %d%", value);
            }
        });

        fileUpload.execute();

    }

    public void onReqCreateGroup(JSONObject groupCreateRsp){
        int resultCode = groupCreateRsp.getIntValue("resultCode");
        int operatorId = groupCreateRsp.getIntValue("UserId");
        if(0 != resultCode){
            Logger.getLogger().d("group#createGroup failed");
            CommonFunction.dismissProgressDialog();
            CommonFunction.showToast(R.string.create_temp_group_failed);
            return;
        }

        JSONObject groupInfo = groupCreateRsp.getJSONObject("GroupInfo");
        Gson gson = new Gson();
        GroupEntity groupEntity = gson.fromJson(groupInfo.toJSONString(), GroupEntity.class);
        if(avatarFile != null && avatarFile.length() > 0){
            groupEntity.setAvatarLocalPath(avatarFile.getPath());
        }
        // 更新DB 更新map
        IMGroupManager.instance().getGroupMap().put(groupEntity.getPeerId(),groupEntity);
        IMSessionManager.instance().updateSession(groupEntity, groupEntity.getGroupMemberList(), groupEntity.getCreatorId(), "ADD");
        GroupMemberEntity.insertOrUpdateMultiData(groupEntity.getGroupMemberList());
        GroupEntity.insertOrUpdateSingleData(groupEntity);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(fromSingleChatSetting){  //此时聊天页面已在回退栈中，不能直接跳转，否则不能刷新数据
//                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN_IM, 0, null);
//                }else {
//                    mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, groupEntity.getSessionKey());
//                }
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, groupEntity.getSessionKey());
            }
        });
    }


    protected void onMyGroupChatClick(){
        this.mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SELECT_GROUP_CHAT, 0, CreateGroupChatFragment.class.getName());
    }

}
