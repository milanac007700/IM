package com.milanac007.demo.im.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.adapter.GroupItemListAdapter;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.demo.im.utils.CommonFunction;

import java.util.ArrayList;
import de.greenrobot.event.EventBus;

/**
 * Created by zqguo on 2017/4/6.
 */
public class GroupChatMainFragment extends BaseFragment implements View.OnClickListener {
    private TextView mSearchBtn;
    private TextView mTitle;
    private TextView group_item_sum_label;
    private TextView no_data_textview;
    private ListView listview;
    private GroupItemListAdapter adapter;
    private LinearLayout searchLayout;
    private EditText searchEditText;
    private int loginerId;

    @Override
    public void onIMServiceConnected() {
        super.onIMServiceConnected();
        loginerId = imService.getLoginManager().getLoginId();
        adapter = new GroupItemListAdapter(GroupChatMainFragment.this, imService);
        listview.setAdapter(adapter);
        loadData();
    }
    
    private void loadData(){
        int size = adapter.updateNormalGroupData();
        if(size == 0){
            listview.setVisibility(View.GONE);
            no_data_textview.setVisibility(View.VISIBLE);
        }else {
            listview.setVisibility(View.VISIBLE);
            no_data_textview.setVisibility(View.GONE);
        }
        group_item_sum_label.setText(String.format("%d个群聊", size));
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

        mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(getResources().getString(R.string.g_group_chat));

        mSearchBtn = (TextView) view.findViewById(R.id.fragment_head1_finish2);
        mSearchBtn.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.mipmap.bg_search);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSearchBtn.setCompoundDrawables(drawable, null, null, null);

        TextView mFinish = (TextView) view.findViewById(R.id.fragment_head1_finish);
        mFinish.setVisibility(View.VISIBLE);
        drawable = getResources().getDrawable(R.mipmap.bg_add_sign);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mFinish.setCompoundDrawables(drawable, null, null, null);

        ///
        RelativeLayout fragment_head1 = (RelativeLayout)view.findViewById(R.id.fragment_head1);

        searchLayout = new LinearLayout(getActivity());
        searchLayout.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonFunction.dip2px(36));
        lp1.addRule(RelativeLayout.CENTER_VERTICAL);
        lp1.addRule(RelativeLayout.RIGHT_OF, R.id.fragment_head1_back);
        lp1.addRule(RelativeLayout.LEFT_OF, R.id.right_view_layout);
        searchLayout.setLayoutParams(lp1);
        searchLayout.setBackground(getActivity().getResources().getDrawable(R.mipmap.search_bg));
        fragment_head1.addView(searchLayout, lp1);

        searchEditText = new EditText(getActivity());
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setSingleLine(true);
        searchEditText.setPadding(CommonFunction.dip2px(15), 0, 0, 0);
        searchEditText.setHint(R.string.g_search);
        searchEditText.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setTextColor(getResources().getColor(R.color.font_grey_s));
        searchEditText.setTextSize(15);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        searchEditText.setLayoutParams(lp);
        searchLayout.addView(searchEditText);

        ImageView del_content_img = new ImageView(getActivity());
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = CommonFunction.dip2px(3);
        del_content_img.setImageDrawable(getResources().getDrawable(R.mipmap.del_icon));
        del_content_img.setLayoutParams(lp);
        searchLayout.addView(del_content_img);
        searchLayout.setVisibility(View.GONE);

        no_data_textview = (TextView)view.findViewById(R.id.no_data_textview);
        listview = (ListView)view.findViewById(R.id.group_item_listview);
        int showdivider = CommonFunction.dip2px(0.5f);
        listview.setDividerHeight(showdivider);

        TextView group_chat_label = new TextView(getActivity());
        group_chat_label.setTextColor(getResources().getColor(R.color.font_grey_s));
        group_chat_label.setText("群聊");
        int padding = CommonFunction.dip2px(5);
        group_chat_label.setPadding(padding, padding, padding, padding);
        group_item_sum_label = new TextView(getActivity());
        group_item_sum_label.setTextSize(16f);
        group_item_sum_label.setTextColor(getResources().getColor(R.color.font_grey_s));
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, CommonFunction.dip2px(60));
        group_item_sum_label.setLayoutParams(params);
        group_item_sum_label.setGravity(Gravity.CENTER);
        listview.addHeaderView(group_chat_label);
        listview.addFooterView(group_item_sum_label);

        View[] views = {mBack, mSearchBtn, mFinish};
        for(View view1: views){
            view1.setOnClickListener(this);
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupEntity groupEntity = adapter.getItem(position-1);
                if(groupEntity == null)
                    return;
                sendIM(groupEntity);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                GroupEntity groupEntity = adapter.getItem(position-1);
                if(groupEntity == null)
                    return true;
                currentSelectedGroupId = groupEntity.getPeerId();
                showLongPressDialog(currentSelectedGroupId);
                return true;
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
            }
        });

    }

    public void sendIM(GroupEntity group) {
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SEND_TEXT_MSG, 0, group.getSessionKey());
    }

    private int currentSelectedGroupId = 0;
    private OperateListDialog operateListDialog;
    private ArrayList<OperateListDialog.OperateItem> operateItems = new ArrayList<>();
    private void showLongPressDialog(final int groupId) {
        final String[] menuStr = {"从通讯录中移除"};
        if (menuStr == null || menuStr.length <= 0) {
            return;
        }

        if(operateListDialog == null) {
            operateListDialog = new OperateListDialog(getActivity());
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
                            CommonFunction.showProgressDialog(getActivity(), "提交中...");
//                            imService.getGroupManager().reqModifyGroupInfo(groupId, IMBaseDefine.PreferenceType.PREFERENCE_TYPE_GROUP_NORMAL_VALUE, String.valueOf(0));
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

        operateListDialog.setGravityType(0); //居中显示
//        operateListDialog.setTitle("请选择");
        operateListDialog.showTitle(false);
        operateListDialog.updateOperateItems(operateItems);
        operateListDialog.show();
    }


    private void onFinishClick(){
        mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_CREATE_GROUP_CHAT, 0, null);
    }

    protected void onSearchClick(){
        searchLayout.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();
        autoKey();
        mTitle.setVisibility(View.GONE);
        mSearchBtn.setVisibility(View.GONE);
    }

    @Override
    public void onBack() {
        if(searchLayout.getVisibility() == View.VISIBLE){
            searchLayout.setVisibility(View.GONE);
            hideKey();
            mSearchBtn.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.VISIBLE);
        }else {
            super.onBack();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_head1_back:
                onBack();
                break;
            case R.id.fragment_head1_finish2:
                onSearchClick();
                break;
            case R.id.fragment_head1_finish:
                onFinishClick();
                break;
        }
    }

    @Override
    public int getPageNumber() {
        return OnActionListener.Page.SCREEN_GROUP_CHAT_MAIN;
    }

    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()){
            case GROUP_INFO_UPDATED:{
                if(event.getGroupId() == currentSelectedGroupId && event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NORMAL_VALUE && event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    loadData();
                }
            }break;

            case GROUP_INFO_UPDATED_FAIL:
            case GROUP_INFO_UPDATED_TIMEOUT: {
                if(event.getGroupId() == currentSelectedGroupId && event.getChangeType() == DBConstant.PREFERENCE_TYPE_GROUP_NORMAL_VALUE && event.getOperateId() == loginerId) {
                    CommonFunction.dismissProgressDialog();
                    CommonFunction.showToast("群属性修改失败");
                }

            }break;
        }
    }

}
