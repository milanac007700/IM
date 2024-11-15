package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.SwitchView;
import com.milanac007.demo.im.utils.CommonFunction;

import de.greenrobot.event.EventBus;

/**
 * 群管理
 * Created by zqguo on 2017/4/27.
 */
public class GroupAdminFragment extends BaseFragment implements View.OnClickListener{

    private View change_group_admin_textview;
    private SwitchView confirm_switchview;

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_ADMIN;
    }

    private View view1;
    private TextView mBack;
    private int groupId;
    private boolean isAdmin;
    private GroupEntity groupEntity;
    private IMService imService;
    private int loginerId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            loginerId = imService.getLoginManager().getLoginId();
            setData();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_admin_layout, null);
    }

    protected void setTitle(TextView mTitle) {
        mTitle.setText(R.string.g_group_admin);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view1 = view;
        mBack = (TextView) view.findViewById(R.id.fragment_head1_back);
        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);

        change_group_admin_textview = view.findViewById(R.id.change_group_admin_textview);
        confirm_switchview = (SwitchView)view.findViewById(R.id.confirm_switchview);
        setTitle(mTitle);
        setListener();
    }

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

    private void setListener() {
        View[] views = {mBack, change_group_admin_textview};
        for (View view : views) {
            view.setOnClickListener(this);
        }

        view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

        confirm_switchview.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                CommonFunction.showToast(R.string.func_developing);
                confirm_switchview.toggleSwitch(!confirm_switchview.isOpened());
            }

            @Override
            public void toggleToOff(SwitchView view) {
                CommonFunction.showToast(R.string.func_developing);
                confirm_switchview.toggleSwitch(!confirm_switchview.isOpened());
            }
        });
    }

    private void setData(){
        Bundle bundle = this.mListener.getArguments(this.getPageNumber(), MainActivity.ARGUMENT_BUDDYID);
        groupId = Integer.parseInt(bundle.getString(DataConstants.BUDDY_ID));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_head1_back: {
                onBack();
            }
            break;
            case R.id.change_group_admin_textview: {
                mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN,  OnActionListener.Page.SCREEN_GROUP_ADMIN_MODIFY, groupId+"");
            }
            break;
        }
    }

    public void onEventMainThread(GroupEvent event){

        if(event.getGroupId() == groupId && event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_AUTH_VALUE && event.getOperateId() == loginerId) {
            switch (event.getEvent()){
                case GROUP_INFO_UPDATED:{
                    CommonFunction.dismissProgressDialog();
                    setData();
                }break;

                case GROUP_INFO_UPDATED_FAIL:
                case GROUP_INFO_UPDATED_TIMEOUT: {
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群认证属性修改失败");
                }break;
            }
        }
    }

}
