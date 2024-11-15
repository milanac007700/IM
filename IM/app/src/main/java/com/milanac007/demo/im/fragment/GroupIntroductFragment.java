package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.google.protobuf.CodedInputStream;
//import com.mogujie.tt.protobuf.IMGroup;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.callback.Packetlistener;
import com.milanac007.demo.im.db.manager.IMSessionManager;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.db.config.DataConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 群简介
 * Created by zqguo on 2017/4/28.
 */
public class GroupIntroductFragment extends BaseFragment{

    private IMService imService;
    private GlideImageView groupHeadIcon;
    private TextView groupName;
    private TextView groupMemberCount;
    private int groupId;
    private int loginId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            loginId = imService.getLoginManager().getLoginId();
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

    private void setData(){
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        groupId = Integer.valueOf(bundle.getString(DataConstants.BUDDY_ID));
        GroupEntity groupEntity = imService.getGroupManager().findGroup(groupId);
        if(groupEntity != null){
            App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    CommonFunction.setHeadIconImageView(groupHeadIcon, groupEntity);
                }
            });
            groupName.setText(groupEntity.getMainName());
            groupMemberCount.setText(String.format("（共%d人）", groupEntity.getUserCnt()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_introduction_layout, null);
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_INTRODUCTION;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View mBack = view.findViewById(R.id.fragment_head1_back);

        groupHeadIcon = (GlideImageView)view.findViewById(R.id.groupHeadIcon);
        groupName = (TextView)view.findViewById(R.id.groupName);
        groupMemberCount = (TextView)view.findViewById(R.id.groupMemberCount);
        TextView apply_btn = (TextView)view.findViewById(R.id.apply_btn);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleApplyBtnClick();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });
    }

    private void handleApplyBtnClick(){
        CommonFunction.showProgressDialog(getActivity(), "提交中...");
        //TODO
//        imService.getGroupManager().reqApplyJoinGroup(groupId, "", new Packetlistener() {
//            @Override
//            public void onSuccess(Object response) {
//                try {
//                    IMGroup.IMGroupJoinRsp groupJoinRsp = IMGroup.IMGroupJoinRsp.parseFrom((CodedInputStream)response);
//                    int resultCode = groupJoinRsp.getResultCode();
//                    if (0 != resultCode) {
//                        CommonFunction.showToast("处理失败，请重试");
//                        return;
//                    }
//                    final int operatorId = groupJoinRsp.getUserId();
//                    if(loginId != operatorId || groupId != groupJoinRsp.getGroupId()){
//                        CommonFunction.showToast("处理失败，请重试");
//                        return;
//                    }
//
//                    imService.getGroupManager().reqGroupDetailInfo(groupId, new Packetlistener() {
//                        @Override
//                        public void onSuccess(Object response) {
//
//                            CommonFunction.dismissProgressDialog();
//                            try {
//                                IMGroup.IMGroupInfoListRsp groupInfoListRsp = IMGroup.IMGroupInfoListRsp.parseFrom((CodedInputStream)response);
//                                imService.getGroupManager().onRepGroupDetailInfo(groupInfoListRsp);
//                                GroupEntity groupEntityRet = imService.getGroupManager().findGroup(groupId);
//                                List<GroupMemberEntity> changeMemList = new ArrayList<>();
//                                for(GroupMemberEntity member : groupEntityRet.getGroupMemberList()){
//                                    if(operatorId == member.getPeerId()){
//                                        changeMemList.add(member);
//                                        break;
//                                    }
//                                }
//
//                                IMSessionManager.instance().updateSession(groupEntityRet, changeMemList , operatorId,"JOIN");
//                                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, groupEntityRet.getSessionKey());
//
//                            }catch (IOException e){
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFaild() {
//                            CommonFunction.showToast("处理失败，请重试");
//                        }
//
//                        @Override
//                        public void onTimeout() {
//                            CommonFunction.showToast("处理失败，请重试");
//                        }
//                    });
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    CommonFunction.showToast("处理失败，请重试");
//                }
//            }
//
//            @Override
//            public void onFaild() {
//                CommonFunction.dismissProgressDialog();
//                CommonFunction.showToast("处理失败，请重试");
//            }
//
//            @Override
//            public void onTimeout() {
//                CommonFunction.dismissProgressDialog();
//                CommonFunction.showToast("处理超时，请重试");
//            }
//        });

        onBack();
    }
}
