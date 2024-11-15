package com.milanac007.demo.im.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.db.helper.IMServiceConnector;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.adapter.GroupItemListAdapter;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.ui.ISelectUserHandler;

import java.util.List;

import androidx.fragment.app.Fragment;
import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2017/4/11.
 */
public class SelectGroupChatFragment extends BaseFragment implements View.OnClickListener {

    private TextView no_data_textview;
    private ListView listview;
    private TextView mFinish;

    private GroupItemListAdapter adapter;
    private IMService imService;
    private int mode = R.id.NORMAL_MODE;
    private boolean isSingle = true;
    private Fragment fromFragment;
    private IMServiceConnector imServerConnecter = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServerConnecter.getIMService();
            if(EventBus.getDefault().isRegistered(SelectGroupChatFragment.this)){
                EventBus.getDefault().unregister(SelectGroupChatFragment.this);
            }
            EventBus.getDefault().register(SelectGroupChatFragment.this);
            adapter = new GroupItemListAdapter(SelectGroupChatFragment.this, imService);
            adapter.setMode(mode);
            listview.setAdapter(adapter);
            setFinishText(isSingle);
            loadData();
        }

        @Override
        public void onServiceDisconnected() {
            if(EventBus.getDefault().isRegistered(SelectGroupChatFragment.this)){
                EventBus.getDefault().unregister(SelectGroupChatFragment.this);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServerConnecter.connect(getActivity());
        String mTag = bundle.getString("arg");
        fromFragment = mActivity.getFragment4Tag(mTag);
        if(fromFragment instanceof TransmitMsgFragment){
            TransmitMsgFragment transmitMsgFragment = (TransmitMsgFragment) fromFragment;
            isSingle = transmitMsgFragment.isSingle;
            mode = isSingle ? R.id.SINGLE_CHOICE_MODE :R.id.MULTIPLE_CHOICE_MODE;
        }
    }

    private void loadData(){
        if(fromFragment instanceof TransmitMsgFragment){
            TransmitMsgFragment transmitMsgFragment = (TransmitMsgFragment)fromFragment;
            List<GroupEntity> groupList = imService.getGroupManager().getAllGroupList();
            if (groupList.size() <= 0) {
                return;
            }

            adapter.bindData(groupList, null, transmitMsgFragment.allSelectedEntity());
            setFinishText(isSingle);
        }else {
            adapter.updateData();
        }

        int size = adapter.getCount();
        if(size == 0){
            listview.setVisibility(View.GONE);
            no_data_textview.setVisibility(View.VISIBLE);
        }else {
            listview.setVisibility(View.VISIBLE);
            no_data_textview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServerConnecter.disconnect(getActivity());
    }

    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()) {
            case GROUP_INFO_UPDATED:
                loadData();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_chat_main_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view){
        TextView mBack = (TextView) view.findViewById(R.id.fragment_head1_back);

        TextView mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(getResources().getString(R.string.g_select_group_chat));

        mFinish = (TextView)view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        mFinish.setGravity(Gravity.CENTER);
        mFinish.setTextColor(getActivity().getResources().getColor(R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, CommonFunction.dip2px(30));
        params.rightMargin = CommonFunction.dip2px(15);
        mFinish.setLayoutParams(params);
        mFinish.setMinWidth(CommonFunction.dip2px(75));
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.green_btn_style);
        mFinish.setBackgroundDrawable(drawable);

        no_data_textview = (TextView)view.findViewById(R.id.no_data_textview);
        listview = (ListView)view.findViewById(R.id.group_item_listview);
        int showdivider = CommonFunction.dip2px(0.5f);
        listview.setDividerHeight(showdivider);

        View[] views = {mBack, mFinish};
        for(View view1: views){
            view1.setOnClickListener(this);
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupEntity groupEntity = adapter.getItem(position);
                if(groupEntity == null)
                    return;
                if(mode == R.id.NORMAL_MODE){
                    sendIM(groupEntity);
                }else {
                    if (adapter.isObjectDisableEdit(groupEntity)){
                        return;
                    }

                    if (isSingle) {
                        adapter.handleSingleSelectClick(view, position);
                        setFinishText(true);
                        showDialog();
                    } else {
                        adapter.handleMultiSelectClick(view, position);
                        setFinishText(false);
                    }
                }

            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

    }

    protected void onFinishClick(){
        if(!adapter.allSelectedObject().isEmpty()){
            showDialog();
        }
        setFinishText(false);
        adapter.notifyDataSetChanged();
    }

    protected void setFinishText(boolean isSingle){
        if(isSingle){
            mFinish.setVisibility(View.GONE);
        }else {
            mFinish.setVisibility(View.VISIBLE);
            String str = getString(R.string.g_confirm);
            if(adapter.allSelectedObject().isEmpty()){
                mFinish.setText(str);
                mFinish.setEnabled(false);
            }else {
                mFinish.setText(String.format("%s(%d)", str,  adapter.allSelectedObject().size()));
                mFinish.setEnabled(true);
            }
        }
    }

    protected void showDialog(){
        final List<PeerEntity> allSelectedList = (List<PeerEntity>)adapter.allSelectedObject();
        handleDoneButton(allSelectedList);
    }


    protected void handleDoneButton(List<PeerEntity> selectedItem) {

        String mTag = bundle.getString("arg");
        Fragment fragment = mActivity.getFragment4Tag(mTag);
        if (fragment instanceof ISelectUserHandler) {
            try {
                String methodName = "handleSelectedUser";

                Bundle bundle = new Bundle();
                bundle.putString("peerType", "GroupEntity");
                Object[] args = {selectedItem,true,bundle};
                CommonFunction.invoke(fragment, methodName, args);
            } catch (Exception e) {
                Logger.getLogger().e("%s", e.getMessage());
            }
        }

        onBack();
    }

    public void sendIM(GroupEntity group) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, group.getSessionKey());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_head1_back:
                onBack();break;
            case R.id.fragment_head1_finish:{
                onFinishClick();
            }break;
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_SELECT_GROUP_CHAT;
    }
}
