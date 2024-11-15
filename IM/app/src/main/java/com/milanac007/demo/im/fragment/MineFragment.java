package com.milanac007.demo.im.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.LoginActivity;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.manager.IMContactManager;
import com.milanac007.demo.im.event.SelfInfoChangeEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.service.IMService;
import com.milanac007.demo.im.ui.CircleImageView;

import com.milanac007.demo.im.ui.SelfQRCodeDialog;

import com.milanac007.demo.im.utils.CommonFunction;
//import com.milanac007.demo.im.db.manager.IMContactManager;
//import com.milanac007.demo.im.service.IMService;
//import com.milanac007.demo.im.db.helper.IMServiceConnector;
//import com.milanac007.demo.im.event.SelfInfoChangeEvent;

import de.greenrobot.event.EventBus;

public class MineFragment extends BaseFragment implements OnClickListener {


	private static MineFragment newInstance = null;
	private TextView selfName;
	private TextView selfId;
	private CircleImageView selfIco;

	private  int loginId;
	private IMContactManager imContactManager;
	private UserEntity loginerUserEntity;

	public static MineFragment newInstance() {
		if(newInstance == null) {
			newInstance = new MineFragment();
		}
		return newInstance;
	}

	public static boolean instanceExist() {
		return newInstance != null;
	}

	public void destoryInstance(){
		if(newInstance != null) {
			newInstance = null;
		}
	}

	@Override
	public void onIMServiceConnected() {
		super.onIMServiceConnected();
		loginId = imService.getLoginManager().getLoginId();
		imContactManager =  imService.getContactManager();
		setData();
	}

	@Override
	public int getPageNumber() {
		return MainActivity.SCREEN_MINE;
	}

	protected void setTitle(TextView mTitle){
		mTitle.setTextSize(18);
		mTitle.setText(getResources().getString(R.string.g_my));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().registerSticky(this); //注册粘性广播接收者
			EventBus.getDefault().register(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_mine, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.fragment_head1_back).setVisibility(View.GONE);
		TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
		mTitle.setVisibility(View.VISIBLE);
		setTitle(mTitle);


		selfIco = (CircleImageView) view.findViewById(R.id.iv_photo);
		selfName = (TextView)view.findViewById(R.id.buddy_name);
		selfId = (TextView)view.findViewById(R.id.buddy_id);
		View my_detail_layout = view.findViewById(R.id.my_detail_layout);
		View settingLayout =  view.findViewById(R.id.settingLayout);


		View[] views = {my_detail_layout, settingLayout};
		for(View view1 : views){
			view1.setOnClickListener(this);
		}

		if(imService != null){
			setData();
		}
	}

	private void setData(){
		loginerUserEntity = imContactManager.findContact(loginId);
		if(loginerUserEntity != null){
			App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
				@Override
				public void run() {
					CommonFunction.setHeadIconImageView(selfIco, loginerUserEntity);
				}
			});

			selfName.setText(loginerUserEntity.getMainName());
			selfId.setVisibility(View.VISIBLE);
			selfId.setText(String.format("%s%s", "IM账号：", loginerUserEntity.getUserCode()));
		}else {
			selfName.setText("立即登录");
			selfId.setVisibility(View.GONE);
		}
	}

	SelfQRCodeDialog qrCodeDialog = null;
	private void clickMyQRCodeBtn(){
		if(qrCodeDialog == null) {
			qrCodeDialog = new SelfQRCodeDialog(getActivity(), loginerUserEntity);
		}
		qrCodeDialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.settingLayout:
				openSetup();
				break;
			case R.id.my_detail_layout:{
				if(loginerUserEntity != null) {
					this.mListener.OnAction(this.getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_MY_DETAIL, null);
				}else {
					Intent i = new Intent(getActivity(), LoginActivity.class);
					startActivity(i);
				}
			}break;
		}
	}

	private void openSetup() {
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SETUP, null);
	}

	public void onEventMainThread(SelfInfoChangeEvent event){
		Logger.getLogger().i("onEventMainThread: SelfInfoChangeEvent");
		setData();
	}

}
