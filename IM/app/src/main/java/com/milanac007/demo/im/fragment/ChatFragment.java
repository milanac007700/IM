/*******************************聊天窗口***********************************/

package com.milanac007.demo.im.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.example.milanac007.pickerandpreviewphoto.PhotoPreviewActivity;
import com.example.milanac007.pickerandpreviewphoto.PickerAlbumActivity;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.adapter.ChatListAdapter;
import com.milanac007.demo.im.adapter.FaceViewPagerAdapter;
import com.milanac007.demo.im.db.config.DBConstant;
import com.milanac007.demo.im.db.config.DataConstants;
import com.milanac007.demo.im.db.config.HandlerConstant;
import com.milanac007.demo.im.db.config.MessageConstant;
import com.milanac007.demo.im.db.config.SysConstant;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.AudioMessage;
import com.milanac007.demo.im.db.entity.msg.ImageMessage;
import com.milanac007.demo.im.db.entity.msg.TextMessage;
import com.milanac007.demo.im.db.entity.msg.VideoMessage;
import com.milanac007.demo.im.db.helper.EntityChangeEngine;
import com.milanac007.demo.im.event.AddUserChangeEvent;
import com.milanac007.demo.im.event.GroupEvent;
import com.milanac007.demo.im.event.MessageEvent;
import com.milanac007.demo.im.event.PriorityEvent;
import com.milanac007.demo.im.event.UserInfoEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.manager.FaceManager;
import com.milanac007.demo.im.net.CustomFileUpload;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.permission.PermissionUtils;
import com.milanac007.demo.im.ui.CustomAudioCapture;
import com.milanac007.demo.im.ui.CustomConfirmDialog;
import com.milanac007.demo.im.ui.ISelectUserHandler;
import com.milanac007.demo.im.ui.OperateListDialog;
import com.milanac007.demo.im.ui.VoiceSendButton;
import com.milanac007.demo.im.utils.CommonFunction;
import com.milanac007.demo.im.utils.HandlerPost;
import com.milanac007.demo.im.utils.ImConfig;
import com.milanac007.demo.im.utils.Utils;
import com.milanac007.demo.videocropdemo.activity.CustomVideoCaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchPanelFrameLayout;
import de.greenrobot.event.EventBus;

import static com.milanac007.demo.im.utils.Utils.showToast;

public class ChatFragment extends BaseFragment implements View.OnClickListener, ISelectUserHandler {

	//TODO 内存泄漏?
	private static ChatFragment newInstance = null;
	public static ChatFragment newInstance() {
		return newInstance;
	}

	private View chat_root;
	private TextView mTitle;
	private TextView mBack;
	private TextView mFinish;
	private View msg_send_layout;
	private ImageView msg_picture_add;
	private ImageView msg_face_add;
	private ImageView msg_volume_add;
	private VoiceSendButton msg_voice_send_button;

	private View tabbar;
	private View icon_transmit;
	private View icon_collection;
	private View icon_del;
	private View icon_email;

	private View text_mode_ayout;
	private KPSwitchPanelFrameLayout panelLayout;
	private LinearLayout msg_face_layout;
	private LinearLayout msg_pic_layout;

	private boolean isShowKeyboard = false;

	private View cameraView;
	private View photoAlbum;
	private View callView;
	private View videoCapture;

	private ViewPager msg_face_viewpager;
	private LinearLayout msg_face_point;

	private EditText mMsgEdit;
	private Button mMsgSendBtn;

	private CustomAudioCapture audioCapture;
	private boolean mIsRecording = false; //最大计时触发ACTION_UP事件后，当手指抬起时ACTION_UP依然有，故判断

//	private PullToRefreshListView mChatListView;
	private SwipeRefreshLayout mRefreshLayout;
	private ListView mChatListView;

	private ChatListAdapter mAdapter;

	private inputMsgMode mInputMsgMode = inputMsgMode.NORMAL_MODE;

	private List<MessageEntity> mCurrentSelectedMsgs;
	private String currentSessionKey;
	private UserEntity loginUser;
	private PeerEntity peerEntity;
	private boolean isGroupChat;

	private int scrollPos = 0;
	private int scrollTop = 0;

	private WindowManager wm;
	private View titleView;

	private enum inputMsgMode {
		NORMAL_MODE(0),
		ADD_FACE_MODE(1),
		ADD_PIC_MODE(2),
		ADD_VOLUME_MODE(3),
		DEFAULT_MODE(4);

		private int modeValue;
		inputMsgMode(int modeValue){
			this.modeValue = modeValue;
		}
		int getValue(){
			return modeValue;
		}
	};

	public String getCurrentSessionKey() {
		return currentSessionKey;
	}

	@Override
	public void onIMServiceConnected() {
		super.onIMServiceConnected();
		if (getActivity() == null) {
			logger.e("ChatFragment#getActivity() is null!!");
			return;
		}

		setTitle(mTitle);
		if(isGroupChat){
			if(((GroupEntity)peerEntity).getlistGroupMemberIds().contains(loginUser.getPeerId())){
				mFinish.setVisibility(View.VISIBLE);
			}else {
				mFinish.setVisibility(View.GONE);
			}
		}

		List<MessageEntity> msgList = reqHistoryMsg();
		mAdapter = new ChatListAdapter(ChatFragment.this, imService, loginUser, currentSessionKey, msgList);
		mChatListView.setAdapter(mAdapter);

		new HandlerPost(100){
			@Override
			public void doAction() {
				mChatListView.setSelection(mAdapter.getCount()-1);  //快速滑到尾
			}
		};

	}

	protected void setTitle(TextView mTitle) {
		currentSessionKey = bundle.getString("sessionKey");
		loginUser = imService.getLoginManager().getLoginInfo();
		peerEntity = imService.getSessionManager().findPeerEntity(currentSessionKey);

		if(peerEntity instanceof UserEntity){
			UserEntity userEntity = (UserEntity) peerEntity;
			String nameStr = !TextUtils.isEmpty(userEntity.getNickName()) ? userEntity.getNickName() : !TextUtils.isEmpty(userEntity.getMainName()) ? userEntity.getMainName() : userEntity.getUserCode();
			mTitle.setText(nameStr);
			isGroupChat = false;
		}else {
			mTitle.setText(peerEntity.getMainName());
			isGroupChat = true;
		}

		imService.getUnReadMsgManager().readUnreadSession(currentSessionKey);
		imService.getNotificationManager().cancelSessionNotifications(currentSessionKey);
	}

	/**
	 * 1.初始化请求历史消息
	 * 2.本地消息不全，也会触发
	 */
	private int historyTimes = 0;
	private List<MessageEntity> reqHistoryMsg() {
		historyTimes++;
		SessionEntity sessionEntity = imService.getSessionManager().findSession(currentSessionKey);
		long lastCreateTime = 0;
		if (sessionEntity != null) {
			lastCreateTime = sessionEntity.getUpdated();
		}
		return imService.getMessageManager().loadHistoryMsg(currentSessionKey, lastCreateTime, true);
	}

	private void createTitleBar() {

		titleView = LayoutInflater.from(mActivity).inflate(R.layout.fragment_head1, null);
		mBack = (TextView) titleView.findViewById(R.id.fragment_head1_back);
		mFinish = (TextView) titleView.findViewById(R.id.fragment_head1_finish);
		mTitle = (TextView) titleView.findViewById(R.id.fragment_head1_title);
		mTitle.setVisibility(View.VISIBLE);

		mFinish.setVisibility(View.VISIBLE);
		Drawable drawable = getResources().getDrawable(R.mipmap.single_chat_setting);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		mFinish.setCompoundDrawables(drawable, null, null, null);

		mBack.setOnClickListener(this);
		mFinish.setOnClickListener(this);

		wm = (WindowManager)mActivity.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp  = new WindowManager.LayoutParams();
		lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
		lp.gravity = Gravity.LEFT | Gravity.TOP;
		lp.x = 0;
		lp.y = 0;
		lp.format = PixelFormat.TRANSPARENT;
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

		if(titleView.getParent() == null) {
			wm.addView(titleView, lp);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		newInstance = this;
//		createTitleBar();

		if(EventBus.getDefault().isRegistered(this)){
			EventBus.getDefault().unregister(this);
		}
		EventBus.getDefault().register(this, SysConstant.MESSAGE_EVENTBUS_PRIORITY);
	}

	@Override
	public void onBack() {
		if(mAdapter.getMode() == R.id.MULTIPLE_CHOICE_MODE){
			mAdapter.setMode(R.id.NORMAL_MODE);
			mAdapter.clearSelectedObject();
			setMoreOperationBtnEnableState();
			setOnTouchListeners();
			setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
			msg_send_layout.setVisibility(View.VISIBLE);
			tabbar.setVisibility(View.GONE);
		}else {
			if(panelLayout.getVisibility() == View.VISIBLE || isShowKeyboard){
				KPSwitchConflictUtil.hidePanelAndKeyboard(panelLayout, mMsgEdit);
			}else {
//			wm.removeView(titleView);
				mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN_IM, 0, null);
			}
		}

	}

	public void clearData(){
		mAdapter.clearData();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();

		Logger.getLogger().i("%s", "onResume");
		if(mAdapter == null)
			return;

		mHandler.sendEmptyMessage(HandlerConstant.REFRESH_DATA);

		// 保存当前会话的好友id
		ImConfig.currentSessionId = peerEntity.getPeerId();
	}

	@Override
	public void onPause() {
		super.onPause();
		mAdapter.releaseVoiceResource();

		// 清空保存
		ImConfig.currentSessionId = -1;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(audioCapture != null) {
			audioCapture.destroy();
			audioCapture = null;
		}
		mAdapter.releaseVoiceResource();
		EventBus.getDefault().unregister(ChatFragment.this);
		newInstance = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chat_activity, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		chat_root = view;

		mBack = (TextView) view.findViewById(R.id.fragment_head1_back);
		mFinish = (TextView) view.findViewById(R.id.fragment_head1_finish);
		mTitle = (TextView) view.findViewById(R.id.fragment_head1_title);
		mTitle.setVisibility(View.VISIBLE);

		mFinish.setVisibility(View.VISIBLE);
		Drawable drawable = getResources().getDrawable(R.mipmap.single_chat_setting);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		mFinish.setCompoundDrawables(drawable, null, null, null);

		msg_send_layout = view.findViewById(R.id.msg_send_layout);
		msg_picture_add = (ImageView) view.findViewById(R.id.msg_picture_add);
		msg_face_add = (ImageView)view.findViewById(R.id.msg_face_add);
		msg_volume_add = (ImageView)view.findViewById(R.id.msg_volume_add);
		msg_voice_send_button = (VoiceSendButton)view.findViewById(R.id.msg_voice_send_button);
		text_mode_ayout = view.findViewById(R.id.text_mode_ayout);
		mMsgEdit = (EditText) view.findViewById(R.id.msg_edit);
		mMsgSendBtn = (Button) view.findViewById(R.id.msg_send_button);

		msg_face_layout = view.findViewById(R.id.msg_face_layout);
		msg_pic_layout = view.findViewById(R.id.msg_pic_layout);
		panelLayout = view.findViewById(R.id.panelLayout);

		KeyboardUtil.attach(mActivity, panelLayout, new KeyboardUtil.OnKeyboardShowingListener() {
			@Override
			public void onKeyboardShowing(boolean isShowing) {
				System.out.println(isShowing ? "onKeyboardShowing" : "onKeyboardHiding");
				isShowKeyboard = isShowing;
			}
		});

//		KPSwitchConflictUtil.attach(msg_pic_layout, msg_picture_add, mMsgEdit);

		mRefreshLayout = view.findViewById(R.id.refresh_layout);
		mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);//设置刷新进度框大小
		mRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE); //设置刷新进度框的背景色
		mRefreshLayout.setColorSchemeResources(R.color.label_light_green); //设置刷新进度框的动画颜色，最多设置4个
		mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadmoreData();
			}
		});

		mChatListView = view.findViewById(R.id.chat_listview);
		mChatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

//		Drawable loadingDrawable = getResources().getDrawable(R.mipmap.login_loading_img);

		msg_face_viewpager = (ViewPager)view.findViewById(R.id.msg_face_viewpager);
		msg_face_point = (LinearLayout)view.findViewById(R.id.msg_face_point);

		cameraView = view.findViewById(R.id.msg_camera);
		photoAlbum = view.findViewById(R.id.msg_photo);
		videoCapture = view.findViewById(R.id.msg_video);
		callView = view.findViewById(R.id.msg_call);

		tabbar = view.findViewById(R.id.tabbar);
		icon_transmit = view.findViewById(R.id.icon_transmit);
		icon_collection = view.findViewById(R.id.icon_collection);
		icon_del = view.findViewById(R.id.icon_del);
		icon_email = view.findViewById(R.id.icon_email);

		setListener();
		audioCapture = new CustomAudioCapture(getActivity(), new CustomAudioCapture.CaculateListener() {
			@Override
			public void onFinish() {
				msg_voice_send_button.timeoutMotionActionUp();
			}
		});
		initFaceViewPager();
		setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);

	}

	private void delOneCharacter() {
		int startIndex = mMsgEdit.getSelectionStart();//获取光标当前位置
		Editable editable = mMsgEdit.getText();
		String subString = editable.toString().substring(0, startIndex);
		if(subString.endsWith("]")){
			Pattern pattern = Pattern.compile("(\\[[\\u4e00-\\u9fa5, OK, NO]{1,3}\\])");
			Matcher mather =  pattern.matcher(subString);
			String matherstr = "";
			while (mather.find()){
				matherstr = mather.group(); //筛选出最后的一组
			}
			if(!TextUtils.isEmpty(matherstr)){
				int length  = FaceManager.getInstance().findLength(matherstr);
				Logger.getLogger().d("matherstr：%s, length: %d", matherstr, length);
				if(length > -1){
					editable.delete(startIndex - length, startIndex);
					return;
				}
			}
		}

		// 删除输入框一个字符 delete(int st, int end):st开始位置， end结束位置
		if(editable.length() >0 ) {
			editable.delete(startIndex - 1, startIndex);
		}
	}

	private void initFaceViewPager() {
		msg_face_viewpager.setAdapter(new FaceViewPagerAdapter(getActivity(), new FaceViewPagerAdapter.GridViewFaceListener() {
			@Override
			public void onGridViewFaceListener(String str) {
				if(!TextUtils.isEmpty(str)) {
					if(str.equals("del")) {
						delOneCharacter();
					}else {
						int startIndex = mMsgEdit.getSelectionStart();//获取光标当前位置
						Editable editable = mMsgEdit.getText();
						editable.insert(startIndex, str); //添加表情到输入框
					}
				}
			}
		}));

		msg_face_point.removeAllViews();
		final int face_page_count = FaceManager.FACE_NUM / FaceManager.COUNT_PER_PAGE + 1;
		final ImageView[] points = new ImageView[face_page_count];
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(CommonFunction.dip2px(10), 0, CommonFunction.dip2px(10), 0);

		for(int i=0; i<face_page_count; i++){
			ImageView imageView = new ImageView(getContext());
			imageView.setBackgroundResource(i==0 ? R.drawable.face_page_select: R.drawable.face_page_unselect);
			imageView.setLayoutParams(params);
			points[i] = imageView;
			msg_face_point.addView(imageView);
		}

		msg_face_viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) { }

			@Override
			public void onPageScrollStateChanged(int i) { }

			@Override
			public void onPageSelected(int position) {
				for(int i=0; i<face_page_count; i++){
					points[i].setBackgroundResource(i==position ? R.drawable.face_page_select: R.drawable.face_page_unselect);
				}
			}
		});
	}

 	@SuppressLint("ClickableViewAccessibility")
	private void setOnTouchListeners(){
		mMsgEdit.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					setMsgLayoutUI(inputMsgMode.NORMAL_MODE);
				}
				return false;
			}
		});

		if(mAdapter != null && mAdapter.getMode() == R.id.MULTIPLE_CHOICE_MODE){
			mChatListView.setOnTouchListener(null);
		}else{
			mChatListView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
						setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
					}
					return false;
				}
			});
		}
	}

	private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			switch (message.what){
				case HandlerConstant.MSG_RECEIVED_MESSAGE: {
					MessageEntity entity = (MessageEntity) message.obj;
					onMsgRecv(entity);
				}break;
				case HandlerConstant.CALL_MSG_RECEIVED: {

				}break;
				case HandlerConstant.SLIP_BOTTOM_REFRESH_DATA: {
					mRefreshLayout.setRefreshing(false); //停止刷新动作
					mAdapter.notifyDataSetChanged();
//					new HandlerPost(100) {
//						@Override
//						public void doAction() {
////							mChatListView.setSelection(mAdapter.getCount() - 1);  //快速滑到尾
//							mChatListView.smoothScrollToPosition(mChatListView.getCount() - 1);//平移动到尾部
//						}
//					};
				} break;
				case HandlerConstant.REFRESH_DATA: {
					mRefreshLayout.setRefreshing(false); //停止刷新动作
					mAdapter.notifyDataSetChanged();
				} break;
			}
			return true;
		}
	});

	private void loadmoreData(){
		historyTimes++;
		App.THREAD_POOL_EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				List<MessageEntity> msgList = imService.getMessageManager().loadHistoryMsg(currentSessionKey, mAdapter.getLastCreatedTime(), false);
				if(msgList.size() == 0){
					new HandlerPost(200, true){
						@Override
						public void doAction() {
							mRefreshLayout.setRefreshing(false); //停止刷新动作
							CommonFunction.showToast(R.string.g_no_more);
						}
					};
					return;
				}


				mAdapter.appendData(msgList);
				mChatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
				new HandlerPost(0, true){
					@Override
					public void doAction() {
						mRefreshLayout.setRefreshing(false); //停止刷新动作
						mAdapter.notifyDataSetChanged();
//						mChatListView.smoothScrollToPosition(msgList.size());//平移
//						mChatListView.setSelection(msgList.size());
						mChatListView.setSelectionFromTop(msgList.size(), 100); //与setSelection什么区别？
					}
				};

				new HandlerPost(200, true){
					@Override
					public void doAction() {
						mChatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
					}
				};
			}
		});
	}

	public void startAudioCapture() {
		mIsRecording = true;
		audioCapture.startCapture();
	}

	private void setListener() {
		View[] views = {mBack, mFinish, mMsgSendBtn, msg_picture_add, msg_face_add, msg_volume_add ,photoAlbum,
				callView, videoCapture, cameraView, icon_transmit, icon_collection, icon_del, icon_email};

		for(View view : views){
			view.setOnClickListener(this);
		}

//		chat_root.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				return true;
//			}
//		});

		setOnTouchListeners();

		mMsgEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				String str = editable.toString().trim();
				if(TextUtils.isEmpty(str)){
					msg_picture_add.setVisibility(View.VISIBLE);
					mMsgSendBtn.setVisibility(View.GONE);
				}else {
					msg_picture_add.setVisibility(View.GONE);
					mMsgSendBtn.setVisibility(View.VISIBLE);
				}
			}
		});

		mMsgEdit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP){
					delOneCharacter();
					return true;

				}else if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				}

				return false;
			}
		});

		msg_voice_send_button.setListener(new VoiceSendButton.RecordListener() {

			@Override
			public void onStartRecord() {
				if(PermissionUtils.lacksPermission(getActivity(), PermissionUtils.PERMISSION_RECORD_AUDIO)){
					PermissionUtils.requestPermission(getActivity(), 1, PermissionUtils.PERMISSION_RECORD_AUDIO, MainActivity.getInstance());
				}else {
					startAudioCapture();
				}
			}

			@Override
			public void onFinishRecord() {
				if (!mIsRecording) {
					return;
				}
				mIsRecording = false;

				long captureResult = audioCapture.stopCapture();
				if(captureResult < 2)
					return;

				List<String> voicePaths = new ArrayList<>();
				voicePaths.add(audioCapture.getCurrentFileName());
				sendProcess(DataConstants.MSG_TYPE_FLAG_VOICE, String.valueOf(captureResult), voicePaths);//暂时挂载音频长度到msg中，方便处理
			}

			@Override
			public void onCancelRecord() {
				if (!mIsRecording) {
					return;
				}
				mIsRecording = false;
				audioCapture.cancelCapture();
			}

			@Override
			public void onMoveLayout(boolean isCancel) {
				audioCapture.onMoveLayout(isCancel);
			}
		});
	}

	private class AddMsgToContainer extends AsyncTask<Void, Void, Boolean> {
		int peerId;
		String msg;
		String path;
		String contentType;
		String sessionKey;
		int msgType;
		MessageEntity m = null;
		boolean isTransmit = false;

		public AddMsgToContainer(int peerId, String msg, String path, String contentType) {
			super();
			this.peerId = peerId;
			this.msg = msg;
			this.path = path;
			this.contentType = contentType;
		}

		public AddMsgToContainer(MessageEntity msg, String sessionKey) {
			super();
			this.msgType = msg.getMsgType();
			this.sessionKey = sessionKey;
			isTransmit = true;

			switch (this.msgType){
				case DBConstant.MSG_TYPE_SINGLE_TEXT:
				case DBConstant.MSG_TYPE_GROUP_TEXT:{
					m = new TextMessage();
					m.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
				}break;
				case DBConstant.MSG_TYPE_SINGLE_IMG:
				case DBConstant.MSG_TYPE_GROUP_IMG:{
					m = new ImageMessage();
					m.setDisplayType(DBConstant.SHOW_IMAGE_TYPE);
					if(msg instanceof ImageMessage){
						ImageMessage imageMessage = (ImageMessage)msg;
						ImageMessage imgMsg = ((ImageMessage)m);
						imgMsg.setPath(imageMessage.getPath());
						imgMsg.setLoadStatus(imageMessage.getLoadStatus());
						imgMsg.setUrl(imageMessage.getUrl());
					}
				}break;
				case DBConstant.MSG_TYPE_SINGLE_AUDIO:
				case DBConstant.MSG_TYPE_GROUP_AUDIO:{
					m = new AudioMessage();
					m.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
					if(msg instanceof AudioMessage){
						AudioMessage audioMessage = (AudioMessage)msg;
						AudioMessage audioMsg = ((AudioMessage)m);
						audioMsg.setAudiolength(audioMessage.getAudiolength());
						audioMsg.setAudioPath(audioMessage.getAudioPath());
						audioMsg.setLoadStatus(audioMessage.getLoadStatus());
						audioMsg.setUrl(audioMessage.getUrl());
					}
				}break;
				case DBConstant.MSG_TYPE_SINGLE_VEDIO:
				case DBConstant.MSG_TYPE_GROUP_VEDIO:{
					m = new VideoMessage();
					m.setDisplayType(DBConstant.SHOW_VIDEO_TYPE);
					if(msg instanceof VideoMessage){
						VideoMessage videoMessage = (VideoMessage)msg;
						VideoMessage videoMsg = ((VideoMessage)m);

						videoMsg.setVideoPath(videoMessage.getVideoPath());
						videoMsg.setThumbnailPath(videoMessage.getThumbnailPath());
						videoMsg.setVideoUrl(videoMessage.getVideoUrl());
						videoMsg.setThumbnailUrl(videoMessage.getThumbnailUrl());
						videoMsg.setLoadStatus(videoMessage.getLoadStatus());
					}
				}break;
			}

			m.setId(null);
			m.setContent(msg.getContent());
			m.setFromId(loginUser.getPeerId());
			long nowTime = System.currentTimeMillis();
			m.setUpdated(nowTime);
			m.setCreated(nowTime);
			m.setStatus(MessageConstant.MSG_SENDING);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			if(isTransmit){
				String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
				int peerType = Integer.parseInt(sessionInfo[0]);
				int peerId = Integer.parseInt(sessionInfo[1]);

				int msgType = this.msgType;
				if(peerType == DBConstant.SESSION_TYPE_SINGLE){
					switch (this.msgType){
						case DBConstant.MSG_TYPE_GROUP_TEXT:
							msgType = DBConstant.MSG_TYPE_SINGLE_TEXT; break;
						case DBConstant.MSG_TYPE_GROUP_IMG:
							msgType = DBConstant.MSG_TYPE_SINGLE_IMG; break;
						case DBConstant.MSG_TYPE_GROUP_AUDIO:
							msgType = DBConstant.MSG_TYPE_SINGLE_AUDIO; break;
						case DBConstant.MSG_TYPE_GROUP_VEDIO:
							msgType = DBConstant.MSG_TYPE_SINGLE_VEDIO; break;
					}
				}else {
					switch (this.msgType){
						case DBConstant.MSG_TYPE_SINGLE_TEXT:
							msgType = DBConstant.MSG_TYPE_GROUP_TEXT; break;
						case DBConstant.MSG_TYPE_SINGLE_IMG:
							msgType = DBConstant.MSG_TYPE_GROUP_IMG; break;
						case DBConstant.MSG_TYPE_SINGLE_AUDIO:
							msgType = DBConstant.MSG_TYPE_GROUP_AUDIO; break;
						case DBConstant.MSG_TYPE_SINGLE_VEDIO:
							msgType = DBConstant.MSG_TYPE_GROUP_VEDIO; break;
					}
				}

				m.setMsgType(msgType);
				m.setToId(peerId);
				m.buildSessionKey(true);

			}else{
				try {
					// 将消息添加到本地会话列表
					if(contentType.equals(DataConstants.MSG_TYPE_FLAG_TEXT)){
						m = TextMessage.buildForSend(msg, loginUser, peerEntity);
					}else if(contentType.equals(DataConstants.MSG_TYPE_FLAG_IMAGE)){
						m = ImageMessage.buildForSend(path,loginUser, peerEntity);
					}else if(contentType.equals(DataConstants.MSG_TYPE_FLAG_VOICE)){
						m = AudioMessage.buildForSend(Float.valueOf(msg), path, loginUser, peerEntity);
					}else if(contentType.equals(DataConstants.MSG_TYPE_FLAG_LITTLE_VIDEO)){
						m = VideoMessage.buildForSend(path, loginUser, peerEntity);
					}

					int msgType = m.getMsgType();

					//视频特殊处理
					if (msgType == DBConstant.MSG_TYPE_SINGLE_VEDIO || msgType == DBConstant.MSG_TYPE_GROUP_VEDIO) {
						Bitmap firstFrameBitmap = CommonFunction.getVideoThumbnail(path); //添加缩略图
						VideoMessage videoMessage = (VideoMessage)m;
						String thumbnailPath = videoMessage.getVideoPath().replace("mp4", "jpg");
						File firstFrameFile = new File(thumbnailPath);
						FileOutputStream out = new FileOutputStream(firstFrameFile);
						firstFrameBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
						out.flush();
						out.close();
						((VideoMessage) m).setThumbnailPath(firstFrameFile.getPath());
						CacheManager.getInstance().addBitmapCache(thumbnailPath, firstFrameBitmap, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

//				/**
//				 * CrashHandler :: java.lang.IllegalStateException:
//				 The content of the adapter has changed but ListView did not receive a notification.
//				 Make sure the content of your adapter is not modified from a background thread,
//				 but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes.
//				 [in ListView(2131689739, class android.widget.ListView) with Adapter(class com.milanac007.demo.im.adapter.ChatListAdapter)]
//				 */
//				mActivity.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						session.appendMsgAndFirstSession(m);
//					}
//				});

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				sendMsgToServer(m, isTransmit);
			}
		}
	}


	/**
	 * 放入容器中，再发送
	 * @param messageType
	 * @param filePaths
     */
	public void sendProcess(String messageType, String content, List<String> filePaths){

		if(filePaths == null){
			AddMsgToContainer task = new AddMsgToContainer(peerEntity.getPeerId(), content, null, messageType);
			task.execute();
		}else {
			for(String filePath : filePaths){
				AddMsgToContainer task = new AddMsgToContainer(peerEntity.getPeerId(), content, filePath, messageType);
				task.execute();
			}
		}
	}

	//消息转发处理
	public void transmitProcess(MessageEntity msg, List<String> sessionKeys) {
		if(msg == null || sessionKeys == null || sessionKeys.isEmpty())
			return;

		for(String sessionKey : sessionKeys){
			AddMsgToContainer task = new AddMsgToContainer(msg, sessionKey);
			task.execute();
		}
	}

	private void generateAddBuddyVerifyMsg(){
		UserEntity userEntity = (UserEntity) peerEntity;
		String verifyStr = String.format("%s开启了朋友验证，你还不是他 (她) 朋友。请先发送朋友验证请求，对方验证通过后，才能聊天。", userEntity.getMainName());

		TextMessage textMessage = new TextMessage();
		textMessage.setMsgType(DBConstant.MSG_TYPE_NEED_ADD_BUDDY_VERIFY_SYSTEM_TEXT);
		long nowTime = System.currentTimeMillis();
		//TODO
		textMessage.setFromId(userEntity.getPeerId());
		int loginId = imService.getLoginManager().getLoginId();
		textMessage.setToId(loginId);
		textMessage.setUpdated(nowTime);
		textMessage.setCreated(nowTime);
		textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
		// 内容的设定
		textMessage.setContent(verifyStr);

		boolean isSend = textMessage.isSend(loginId);
		textMessage.buildSessionKey(isSend);

		textMessage.setStatus(MessageConstant.MSG_SUCCESS);

		pushList(textMessage);

//		long pkId = DBInterface.instance().insertOrUpdateMessage(textMessage);
		MessageEntity.insertOrUpdateSingleData(textMessage);
		imService.getSessionManager().updateSession(textMessage);
	}

	public void sendMsgToServer(final MessageEntity m, boolean isTransmit){

		if(isTransmit){

			if(this.currentSessionKey.equals(m.getSessionKey())){ //给自己转发，添加到当前UI
				pushList(m);
			}

			if(!Utils.isNetworkAvailable()){
				m.setStatus(MessageConstant.MSG_FAILURE);
//				long pkId =  DBInterface.instance().insertOrUpdateMessage(m);
				imService.getSessionManager().updateSession(m);
				CommonFunction.showToast("请检查当前网络");
			}else {
				m.setStatus(MessageConstant.MSG_SENDING);
				sendMsg(m);
			}
			return;
		}

		pushList(m);

		if(!Utils.isNetworkAvailable()){

			switch (m.getMsgType()){
				case DBConstant.MSG_TYPE_SINGLE_IMG:
				case DBConstant.MSG_TYPE_GROUP_IMG: {
					ImageMessage imageMessage = (ImageMessage) m;
					if(TextUtils.isEmpty(imageMessage.getUrl())) {
						imageMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
						m.setContent(imageMessage.getContent());
					}
				} break;
				case DBConstant.MSG_TYPE_SINGLE_AUDIO:
				case DBConstant.MSG_TYPE_GROUP_AUDIO: {
					AudioMessage audioMessage = (AudioMessage) m;
					if(TextUtils.isEmpty(audioMessage.getUrl())) {
						audioMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
						m.setContent(audioMessage.getContent());
					}

//					extraContent.put("audioPath",audioPath);
//					extraContent.put("audiolength",audiolength);
//					extraContent.put("readStatus",readStatus);
//					extraContent.put("loadStatus", loadStatus);
//					extraContent.put("url", url);

				} break;
				case DBConstant.MSG_TYPE_SINGLE_VEDIO:
				case DBConstant.MSG_TYPE_GROUP_VEDIO: {
					VideoMessage videoMessage = (VideoMessage) m;
					if(TextUtils.isEmpty(videoMessage.getVideoUrl()) || TextUtils.isEmpty(videoMessage.getThumbnailUrl())) {
						videoMessage.setLoadStatus(MessageConstant.MSG_FILE_UNLOAD);
						m.setContent(videoMessage.getContent());
					}

//					extraContent.put("videoPath",videoPath);
//					extraContent.put("videoUrl",videoUrl);
//					extraContent.put("thumbnailPath",thumbnailPath);
//					extraContent.put("thumbnailUrl",thumbnailUrl);
//					extraContent.put("loadStatus",loadStatus);

				} break;
			}

			m.setStatus(MessageConstant.MSG_FAILURE);
//			long pkId =  DBInterface.instance().insertOrUpdateMessage(m);
			MessageEntity.insertOrUpdateSingleData(m);
			imService.getSessionManager().updateSession(m);
			CommonFunction.showToast("请检查当前网络");
			return;
		}else {
//			//先做权限检查 目前服务器已做处理
//			if(peerEntity instanceof UserEntity){
//				UserEntity userEntity = (UserEntity) peerEntity;
//				if(userEntity.getAction() == 2){
//					m.setStatus(MessageConstant.MSG_FAILURE);
//					long pkId =  DBInterface.instance().insertOrUpdateMessage(m);
//					imService.getSessionManager().updateSession(m);
//
//					//这里再检查一次，防止对方解除好友时 ，自己没收到服务器报文通知
//					imService.getSessionManager().deleteAddBuddyAcceptVerifyMsgsBySessionId(m.getPeerId(true));
//					generateAddBuddyVerifyMsg();
//					return;
//				}
//			}else if(peerEntity instanceof GroupEntity){ //判断是否还在当前群组
//				GroupEntity groupEntity = (GroupEntity) peerEntity;
//				if(!groupEntity.getlistGroupMemberIds().contains(loginUser.getPeerId())){
//					m.setStatus(MessageConstant.MSG_FAILURE);
//					long pkId =  DBInterface.instance().insertOrUpdateMessage(m);
//					imService.getSessionManager().updateSession(m);
//					return;
//				}
//			}
		}


		if(m.getMsgType() == DBConstant.MSG_TYPE_GROUP_TEXT || m.getMsgType() == DBConstant.MSG_TYPE_SINGLE_TEXT){
			sendMsg(m);
			return;
		}else if(m.getStatus() == MessageConstant.MSG_FAILURE) {
			boolean needUploadFile = true;
			switch (m.getMsgType()){
				case DBConstant.MSG_TYPE_SINGLE_IMG:
				case DBConstant.MSG_TYPE_GROUP_IMG: {
					ImageMessage imageMessage = (ImageMessage) m;
					if(imageMessage.getLoadStatus() == MessageConstant.MSG_FILE_LOADED_SUCCESS) {
						m.setContent(imageMessage.getUrl());
						needUploadFile = false;
					}
				} break;
				case DBConstant.MSG_TYPE_SINGLE_AUDIO:
				case DBConstant.MSG_TYPE_GROUP_AUDIO: {
					AudioMessage audioMessage = (AudioMessage) m;
					if(audioMessage.getLoadStatus() == MessageConstant.MSG_FILE_LOADED_SUCCESS) {
						m.setContent(audioMessage.getUrl());
						needUploadFile = false;
					}
				} break;
				case DBConstant.MSG_TYPE_SINGLE_VEDIO:
				case DBConstant.MSG_TYPE_GROUP_VEDIO: {
					VideoMessage videoMessage = (VideoMessage) m;
					if(videoMessage.getLoadStatus() == MessageConstant.MSG_FILE_LOADED_SUCCESS) {
						m.setContent(videoMessage.getVideoUrl() + ";" + videoMessage.getThumbnailUrl());
						needUploadFile = false;
					}
				} break;
			}

			if(!needUploadFile) {//不需要上传附件，直接发送消息
				m.setStatus(MessageConstant.MSG_SENDING);
				sendMsg(m);
				return;
			}
		}

		String filePath = null;
		ArrayList<File> files = new ArrayList<>();
		final String url = NetConstants.URL_UPLOADFILE;

		int fileType = 4; //文件类型fileType（音频1，视频2，图片3，可执行文件等其他文件类型为4）
		int useType = m.getSessionType() == DBConstant.SESSION_TYPE_SINGLE ? 1 : 2;//文件用途useType（私聊文件1、群文件2、公告文件3、朋友圈文件4，私有文件5等）
		switch (m.getMsgType()){
			case DBConstant.MSG_TYPE_SINGLE_IMG:
			case DBConstant.MSG_TYPE_GROUP_IMG:
				fileType = 3;
				ImageMessage imageMessage = (ImageMessage)m;
				filePath = imageMessage.getPath();

				// 图片的话 从缓存里取对应的bitmap，转成file后替换原文件,最终发送的为小文件
				Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(filePath);
				if (bitmap != null) {
					File tempfile = new File(CommonFunction.getDirUserTemp() + File.separator + UUID.randomUUID() + ".jpg");
					try {
						FileOutputStream out = null;
						out = new FileOutputStream(tempfile);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
						files.add(tempfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case DBConstant.MSG_TYPE_SINGLE_AUDIO:
			case DBConstant.MSG_TYPE_GROUP_AUDIO:
				fileType = 1;
				AudioMessage audioMessage = (AudioMessage)m;
				filePath = audioMessage.getAudioPath();
				files.add(new File(filePath));
				break;
			case DBConstant.MSG_TYPE_SINGLE_VEDIO:
			case DBConstant.MSG_TYPE_GROUP_VEDIO:
				fileType = 2;
				VideoMessage videoMessage = (VideoMessage)m;
				filePath = videoMessage.getVideoPath();
				files.add(new File(filePath));
				File thumbnailfile = new File(videoMessage.getThumbnailPath());
				files.add(thumbnailfile);
		}

		final int file_type = fileType;
		CustomFileUpload fileUpload = new CustomFileUpload(url, files, file_type, useType, new CustomFileUpload.UploadListener(){
			@Override
			public void onUploadEnd(JSONObject result) {
				boolean success = result.getBoolean("success");
				if(success){
					JSONArray pathList = result.getJSONArray("uploadUrl");
					if(pathList != null && !pathList.isEmpty()){
						//音频1，视频2，图片3

						if(file_type == 1 || file_type == 3){
							String url = pathList.getJSONObject(0).getString("path");
							if(file_type == 1){
								AudioMessage audioMessage = (AudioMessage)m;
								audioMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
								url += String.format("?%s", audioMessage.getAudiolength()); //音频时长
								audioMessage.setUrl(url);
							}if(file_type == 3){
								ImageMessage imageMessage = (ImageMessage)m;
								imageMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);
								Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(imageMessage.getPath());
								int imgWidth = bitmap.getWidth();
								int imgHeight = bitmap.getHeight();
								url += String.format("?%d%s%d", imgWidth, "x",imgHeight);
								imageMessage.setUrl(url);
							}
							m.setContent(url);
						}else if(file_type == 2) {
							String path1 = pathList.getJSONObject(0).getString("path");
							String path2 = pathList.getJSONObject(1).getString("path");

							VideoMessage videoMessage = (VideoMessage)m;
							videoMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_SUCCESS);

							videoMessage.setVideoUrl(path2);
							videoMessage.setThumbnailUrl(path1);
							if(path1.contains("mp4") && (path2.contains("jpg") || path2.contains("jpeg"))){
								videoMessage.setVideoUrl(path1);
								videoMessage.setThumbnailUrl(path2);
							}

							Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(videoMessage.getThumbnailPath());
							int imgWidth = bitmap.getWidth();
							int imgHeight = bitmap.getHeight();
							videoMessage.setVideoUrl(String.format("%s?%d%s%d", videoMessage.getVideoUrl(), imgWidth,"x", imgHeight));
							videoMessage.setThumbnailUrl(String.format("%s?%d%s%d", videoMessage.getThumbnailUrl(), imgWidth,"x", imgHeight));

							m.setContent(videoMessage.getVideoUrl() + ";" + videoMessage.getThumbnailUrl());
						}
					}

					sendMsg(m);

				} else {
					m.setStatus(MessageConstant.MSG_FAILURE);
					String status = result.getString("status");
					if(status.equals("401")){ //"message": "Authorize error,code is 4000002,4000002"
						CommonFunction.showToast(String.format("登录过期，请重新登录"));
					}else {
						String errorMsg = result.getString("error");
						if(m.getMsgType() == DBConstant.MSG_TYPE_SINGLE_AUDIO || m.getMsgType() == DBConstant.MSG_TYPE_GROUP_AUDIO){
							AudioMessage audioMessage = (AudioMessage)m;
							audioMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
							CommonFunction.showToast(String.format("发送语音失败：%s", errorMsg));
						}else if(m.getMsgType() == DBConstant.MSG_TYPE_SINGLE_IMG || m.getMsgType() == DBConstant.MSG_TYPE_GROUP_IMG){
							CommonFunction.showToast(String.format("发送图片失败：%s", errorMsg));
							ImageMessage imageMessage = (ImageMessage)m;
							imageMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
						}else if(m.getMsgType() == DBConstant.MSG_TYPE_SINGLE_VEDIO || m.getMsgType() == DBConstant.MSG_TYPE_GROUP_VEDIO){
							CommonFunction.showToast(String.format("发送小视频失败：%s", errorMsg));
							VideoMessage videoMessage = (VideoMessage)m;
							videoMessage.setLoadStatus(MessageConstant.MSG_FILE_LOADED_FAILURE);
						}
					}
				}
			}

			@Override
			public void onProgress(int value) {
				Logger.getLogger().d("上传进度: %d%s", value, "%");
			}
		});

		fileUpload.execute();
	}


    /**
     * 锁定内容高度，防止跳闪
     */
    private void lockContentHeight(){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mChatListView.getLayoutParams();
        params.height = mChatListView.getHeight();
        params.weight = 0.0F;
        mChatListView.setLayoutParams(params);
    }

    private void unlockContentHeightDelayed() {
		System.out.println("unlockContentHeightDelayed start, mChatListView.height: " + mChatListView.getLayoutParams().height);

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mChatListView.getLayoutParams();
		params.weight = 1.0F;
		params.height = 0;
		mChatListView.setLayoutParams(params);
    }


	private void setMsgLayoutUI(inputMsgMode mode){
		switch (mode){
			default:
			case DEFAULT_MODE:
			case NORMAL_MODE:{
				msg_face_add.setImageResource(R.mipmap.msg_face_add);
				msg_volume_add.setImageResource(R.mipmap.msg_volume);
				text_mode_ayout.setVisibility(View.VISIBLE);
				msg_voice_send_button.setVisibility(View.GONE);
				msg_pic_layout.setVisibility(View.GONE);
				msg_face_layout.setVisibility(View.GONE);

				if(mode == inputMsgMode.NORMAL_MODE){
					KPSwitchConflictUtil.showKeyboard(panelLayout, mMsgEdit);
//					new HandlerPost(100){
//						@Override
//						public void doAction() {
//							mChatListView.smoothScrollToPosition(mAdapter.getCount());//平移动到尾部
//						}
//					};
				}else {
					KPSwitchConflictUtil.hidePanelAndKeyboard(panelLayout, mMsgEdit);
				}
			}break;
			case ADD_FACE_MODE:{
				if(mInputMsgMode == inputMsgMode.ADD_FACE_MODE){
					setMsgLayoutUI(inputMsgMode.NORMAL_MODE);
					return;
				}
				msg_face_add.setImageResource(R.mipmap.msg_key);
				msg_volume_add.setImageResource(R.mipmap.msg_volume);
				text_mode_ayout.setVisibility(View.VISIBLE);
				msg_voice_send_button.setVisibility(View.GONE);
				msg_pic_layout.setVisibility(View.GONE);
				msg_face_layout.setVisibility(View.VISIBLE);
				KPSwitchConflictUtil.showPanel(panelLayout, mMsgEdit);
//				new HandlerPost(100){
//					@Override
//					public void doAction() {
//						mChatListView.smoothScrollToPosition(mAdapter.getCount());//平移动到尾部
//					}
//				};
				break;
			}
			case ADD_PIC_MODE:{
				if(mInputMsgMode == inputMsgMode.ADD_PIC_MODE){
					setMsgLayoutUI(inputMsgMode.NORMAL_MODE);
					return;
				}

				msg_face_add.setImageResource(R.mipmap.msg_face_add);
				msg_volume_add.setImageResource(R.mipmap.msg_volume);
				text_mode_ayout.setVisibility(View.VISIBLE);
				msg_voice_send_button.setVisibility(View.GONE);
				msg_pic_layout.setVisibility(View.VISIBLE);
				msg_face_layout.setVisibility(View.GONE);
				KPSwitchConflictUtil.showPanel(panelLayout, mMsgEdit);
//				new HandlerPost(100){
//					@Override
//					public void doAction() {
//						mChatListView.smoothScrollToPosition(mAdapter.getCount());//平移动到尾部
//					}
//				};
				break;
			}
			case ADD_VOLUME_MODE:{
				if(mInputMsgMode == inputMsgMode.ADD_VOLUME_MODE){
					setMsgLayoutUI(inputMsgMode.NORMAL_MODE);
					return;
				}

				msg_face_add.setImageResource(R.mipmap.msg_face_add);
				msg_volume_add.setImageResource(R.mipmap.msg_key);
				text_mode_ayout.setVisibility(View.GONE);
				msg_voice_send_button.setVisibility(View.VISIBLE);
				msg_pic_layout.setVisibility(View.GONE);
				msg_face_layout.setVisibility(View.GONE);
				KPSwitchConflictUtil.hidePanelAndKeyboard(panelLayout, mMsgEdit);
				break;
			}
		}
		mInputMsgMode = mode;
	}

	// 发送消息
	@SuppressLint("NewApi")
	private void sendMsg() {
		// 获取消息内容
		String msg = mMsgEdit.getText().toString();
		// 为空直接返回
		if (msg.isEmpty()) {
			return;
		}

		sendProcess(DataConstants.MSG_TYPE_FLAG_TEXT,msg, null);

		// 清空输入框
		mMsgEdit.setText("");
	}

	public void sendMsg(MessageEntity m){

		if(m.getStatus() != MessageConstant.MSG_FAILURE){
			switch (m.getMsgType()){
				case DBConstant.MSG_TYPE_SINGLE_TEXT:
				case DBConstant.MSG_TYPE_GROUP_TEXT:
					imService.getMessageManager().sendText((TextMessage)m);break;
				case DBConstant.MSG_TYPE_SINGLE_IMG:
				case DBConstant.MSG_TYPE_GROUP_IMG:
					imService.getMessageManager().sendSingleImage((ImageMessage) m);break;
				case DBConstant.MSG_TYPE_SINGLE_AUDIO:
				case DBConstant.MSG_TYPE_GROUP_AUDIO:
					imService.getMessageManager().sendVoice((AudioMessage) m);break;
				case DBConstant.MSG_TYPE_SINGLE_VEDIO:
				case DBConstant.MSG_TYPE_GROUP_VEDIO:
					imService.getMessageManager().sendVideo((VideoMessage) m);break;
				default:break;
			}
		}

	}

	public void pushList(MessageEntity msg) {
		mAdapter.addItem(msg);
	}

	public void pushList(List<MessageEntity> entityList) {
		mAdapter.loadHistoryList(entityList);
	}

	@Override
	public int getPageNumber() {
		return OnActionListener.Page.SCREEN_CHAT;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.fragment_head1_back: {
				onBack();
			} break;
			case R.id.msg_send_button: {
				msg_picture_add.setVisibility(View.VISIBLE);
				mMsgSendBtn.setVisibility(View.GONE);
				sendMsg();
			}break;
			case R.id.msg_picture_add:{
				setMsgLayoutUI(inputMsgMode.ADD_PIC_MODE);
			}break;
			case R.id.msg_face_add:{
				setMsgLayoutUI(inputMsgMode.ADD_FACE_MODE);
			}break;
			case R.id.msg_volume_add:{
				setMsgLayoutUI(inputMsgMode.ADD_VOLUME_MODE);
			}break;
			case R.id.msg_photo:{
				setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				onClickPhotoAlbum();
			}break;
			case R.id.msg_call:{
				setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				showCallDialog();
			}break;
			case R.id.msg_video:{
				setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				onClickLittleVideo();
			}break;
			case R.id.msg_camera:{
				setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				onClickPhotoAlbum();
			}break;
			case R.id.fragment_head1_finish:{
				setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
				if(!isGroupChat){
					mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SINGLE_CHAT_SETTING, peerEntity.getPeerId()+"");
				}else {
					mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_GROUP_CHAT_SETTING, peerEntity.getPeerId() + "");
				}
			}break;
			case R.id.icon_transmit:{
				transmitMsgs(); //TODO
			}break;
			case R.id.icon_del:{
				delSelectedMsgs(); //TODO
			}break;
			case R.id.icon_collection:{
				CommonFunction.showToast(R.string.func_developing);
			}break;
			case R.id.icon_email:{
				CommonFunction.showToast(R.string.func_developing);
			}break;
			default:
				break;
		}
	}

	public void showCallDialog() {
		CommonFunction.showToast(R.string.func_developing);
	}

	public void sendAddBuddyVerifyMsg(int buddyid){
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_ADD_FRIEND_VERIFY_MSG, buddyid+"");
	}

	private void onClickPhotoAlbum(){
		Intent intent = new Intent(mActivity, PickerAlbumActivity.class);
		startActivityForResult(intent, PickerAlbumActivity.PICKER_ALBUM_CODE);
	}

	private void onClickLittleVideo(){
		Intent intent = new Intent();
		intent.setClass(mActivity, CustomVideoCaptureActivity.class);
		startActivityForResult(intent, CustomVideoCaptureActivity.VIDEO_CAPTURE_CODE);
	}

	public void preViewImage(ArrayList<String> imagePaths, int currentIndex){
		Intent intent = new Intent(mActivity, PhotoPreviewActivity.class);
		intent.putExtra(PhotoPreviewActivity.SELECTED_KEY, imagePaths);
		intent.putExtra(PhotoPreviewActivity.CURRENT_INDEX, currentIndex);
		intent.putExtra(PhotoPreviewActivity.MODE, PhotoPreviewActivity.NormalPreview);
		startActivity(intent);
	}

	//TODO
	public void playVideo(String videoPath){
//		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_VIDEO_PLAY, OnActionListener.Page.SCREEN_VIDEO_PLAY, videoPath);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == PickerAlbumActivity.PICKER_ALBUM_CODE){
			if(resultCode == Activity.RESULT_OK){
				final ArrayList<String> filePaths = data.getStringArrayListExtra(PickerAlbumActivity.SELECTED_KEY);
				sendProcess(DataConstants.MSG_TYPE_FLAG_IMAGE, "", filePaths);
			}
		}else if (requestCode == CustomVideoCaptureActivity.VIDEO_CAPTURE_CODE){
			if (resultCode == Activity.RESULT_OK) {
				// Video captured and saved to fileUri specified in the Intent

				/**
				 * Uri fileUri = data.getData();
				 * 此处是content:// 开头的地址，不好处理，可以通过resolver获取流
				 * 	ContentResolver resolver =  getContentResolver();
				 *  InputStream inputStream = resolver.openInputStream(contentUri);
				 */
				String filePath = data.getStringExtra("videoPath");
				Logger.getLogger().d("##@@ videofilePath: %s", filePath);
				if(!TextUtils.isEmpty(filePath)){
					List<String> filePaths = new ArrayList<>();
					filePaths.add(filePath);
					sendProcess(DataConstants.MSG_TYPE_FLAG_LITTLE_VIDEO, "", filePaths);
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
				showToast("小视频录制失败");
			}
		}
	}

	//TODO
	public void transmitMsg(MessageEntity msg){
		mAdapter.clearSelectedObject();
		mAdapter.addSelectedObject(msg);
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_TRANSMIT, 0, ChatFragment.class.getName());
	}

	public void transmitMsgs(){
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_TRANSMIT, 0, ChatFragment.class.getName());
	}

	@Override
	public void handleSelectedUser(ArrayList<? extends Object> selectedUsers, Boolean isChanged, Bundle extraData) {
		if(selectedUsers == null || selectedUsers.isEmpty())
			return;

		ArrayList<PeerEntity> mSelectedPeerList = (ArrayList<PeerEntity>)selectedUsers;

		List<String> sessionKeys = new ArrayList<>();
		for (PeerEntity peerEntity : mSelectedPeerList){
			sessionKeys.add(peerEntity.getSessionKey());
		}

		List<MessageEntity> newSelectedMsgs = new ArrayList<>();
		List<MessageEntity> selectedMsgs = (List<MessageEntity>)mAdapter.allSelectedObject();
		for(MessageEntity msg : selectedMsgs){
			if(msg.getStatus() != MessageConstant.MSG_SUCCESS){ //排除发送失败的消息
				continue;
			}
			newSelectedMsgs.add(msg);
		}

		if(!Utils.isNetworkAvailable()){
			CommonFunction.showToast("请检查当前网络");
		}else {
			if(newSelectedMsgs.size() > 0) {
				for(MessageEntity msg : newSelectedMsgs){
					transmitProcess(msg, sessionKeys);
				}
				CommonFunction.showToast("已发送");
			}
		}

		//退出多选模式
		onBack();
	}

	public void onEventMainThread(AddUserChangeEvent event){
		//TODO
	}

	public void onEventMainThread(UserInfoEvent event){
		switch (event.event){
			case USER_INFO_UPDATE:
				List<UserEntity> userInfos = event.userInfos;
				for (UserEntity user : userInfos){
					if(peerEntity.getPeerId() == user.getPeerId()){
						peerEntity = imService.getSessionManager().findPeerEntity(currentSessionKey);
						mAdapter.notifyDataSetChanged(); //TODO 页面会闪动 体验不好，最好最局部更新
						break;
					}
					if(peerEntity instanceof GroupEntity){
						GroupEntity groupEntity = (GroupEntity) peerEntity;
						List<Integer> memberIds = groupEntity.getlistGroupMemberIds();
						if(memberIds != null && memberIds.contains(user.getPeerId())){
							mAdapter.notifyDataSetChanged(); //TODO 页面会闪动 体验不好，最好最局部更新
							break;
						}
					}
				}
				break;
		}
	}

	/**
	 * 背景: 1.EventBus的cancelEventDelivery的只能在postThread中运行，而且没有办法绕过这一点
	 * 2. onEvent(A a)  onEventMainThread(A a) 这个两个是没有办法共存的
	 * 解决: 抽离出那些需要优先级的event，在onEvent通过handler调用主线程，
	 * 然后cancelEventDelivery
	 * <p/>
	 * todo  need find good solution
	 */
	public void onEvent(PriorityEvent event) {
		switch (event.event) {
			case MSG_RECEIVED_MESSAGE: {
				MessageEntity entity = (MessageEntity) event.object;
				/**正式当前的会话*/
				if (currentSessionKey.equals(entity.getSessionKey())) {
					Message message = Message.obtain();
					message.what = HandlerConstant.MSG_RECEIVED_MESSAGE;
					message.obj = entity;
					mHandler.sendMessage(message);
					EventBus.getDefault().cancelEventDelivery(event);
				}
			}break;
		}
	}

	//TODO
	public void onEventMainThread(MessageEvent event) {
		MessageEvent.Event type = event.getEvent();
		MessageEntity entity = event.getMessageEntity();
		switch (type) {
			case ACK_SEND_MESSAGE_OK: {
				onMsgAck(event.getMessageEntity());
			}
			break;

			case ACK_SEND_MESSAGE_FAILURE:
				// 失败情况下新添提醒
				showToast(R.string.message_send_failed);
			case ACK_SEND_MESSAGE_TIME_OUT: {
				onMsgUnAckTimeoutOrFailure(event.getMessageEntity());
			}
			break;

			case HANDLER_FILE_UPLOAD_FAILD: {
				ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
				mAdapter.updateItemState(imageMessage);
				showToast(R.string.message_send_failed);
			}
			break;

			case HANDLER_FILE_UPLOAD_SUCCESS: {
				ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
				mAdapter.updateItemState(imageMessage);
			}
			break;

			case HISTORY_MSG_OBTAIN: {
				if (historyTimes == 1) {
					mAdapter.clearData();
					pushList(reqHistoryMsg());
				}
			}
			break;

		}
	}

	private void resetData(){
		new HandlerPost(0){
			@Override
			public void doAction() {
				//scrollPos记录当前可见的List顶端的一行位置
				scrollPos = mChatListView.getFirstVisiblePosition();

				//判断下ListView的数据是否为空
				if(mAdapter.getCount() > 0){
					View v = mChatListView.getChildAt(0);
					scrollTop = (v == null) ? 0 : v.getTop();
				}

				mAdapter.clearData();
				historyTimes = 0;
				//此方式可防止刷新时的闪跳问题
				mAdapter = new ChatListAdapter(ChatFragment.this, imService, loginUser, currentSessionKey, reqHistoryMsg());
				mChatListView.setAdapter(mAdapter);
				mAdapter.updateData();
				mChatListView.setSelectionFromTop(scrollPos, 0);
			}
		};
	}


	public void JumpToPersonalInfo(int buddyid){
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_PERSONALINFO, buddyid+"");
	}

	public void onEventMainThread(GroupEvent event){

		if(event.getGroupId() != peerEntity.getPeerId())
			return;

		switch (event.getEvent()){
			case GROUP_SHOW_NICK:
				resetData();

			case CHANGE_GROUP_MEMBER_SUCCESS:{
				if(event.getChangeType() == DBConstant.GROUP_MODIFY_TYPE_DEL) {
					if(event.getChangeList().contains(loginUser.getPeerId())){
						mFinish.setVisibility(View.GONE);
					}
				}else if(event.getChangeType() == DBConstant.GROUP_MODIFY_TYPE_ADD) {
					if(event.getChangeList().contains(loginUser.getPeerId())){
						mFinish.setVisibility(View.VISIBLE);
					}
				}
			}break;
		}

	}

	private void onMsgUnAckTimeoutOrFailure(MessageEntity messageEntity) {
		Logger.getLogger().d("chat#onMsgUnAckTimeoutOrFailure, msgId:%s", messageEntity.getMsgId());
		// msgId 应该还是为0
		mAdapter.updateItemState(messageEntity);
		imService.getSessionManager().updateSession(messageEntity);

		if(messageEntity.getStatus() == MessageConstant.MSG_FAIL_RESULT_CODE){
			//先做权限检查 目前服务器已做处理
			if(peerEntity instanceof UserEntity){

				//这里再检查一次，防止对方解除好友时 ，自己没收到服务器报文通知
				//TODO
				imService.getSessionManager().deleteAddBuddyAcceptVerifyMsgsBySessionId(messageEntity.getPeerId(true));
				generateAddBuddyVerifyMsg();

//				UserEntity userEntity = (UserEntity) peerEntity;
//				if(userEntity.getAction() == 2){
//					imService.getSessionManager().updateSession(messageEntity);
//
//					//这里再检查一次，防止对方解除好友时 ，自己没收到服务器报文通知
//					imService.getSessionManager().deleteAddBuddyAcceptVerifyMsgsBySessionId(messageEntity.getPeerId(true));
//					generateAddBuddyVerifyMsg();
//				}
			}else if(peerEntity instanceof GroupEntity){ //判断是否还在当前群组
				//TODO
//				GroupEntity groupEntity = (GroupEntity) peerEntity;
//				if(!groupEntity.getlistGroupMemberIds().contains(loginUser.getPeerId())){
//					imService.getSessionManager().updateSession(messageEntity);
//				}
			}
		}
	}

	private void onMsgAck(MessageEntity messageEntity) {
		Logger.getLogger().d("%s", "message_activity#onMsgAck");
		int msgId = messageEntity.getMsgId();
		Logger.getLogger().d("chat#onMsgAck, msgId:%d", msgId);

		mAdapter.updateItemState(messageEntity);
	}

	// 肯定是在当前的session内
	private void onMsgRecv(MessageEntity entity) {
		Logger.getLogger().d("%s", "message_activity#onMsgRecv");

		imService.getUnReadMsgManager().ackReadMsg(entity);
		pushList(entity);
	}

	private void onClickDelChatBtn(final MessageEntity msg){
		imService.getMessageManager().deleteMessageByMsgId(msg.getMsgId(), msg.getSessionKey());
		List<MessageEntity> delMsgList = new ArrayList<>();
		delMsgList.add(msg);
		mAdapter.removeItems(delMsgList);
	}

	private void delSelectedMsgs(){
		final List<MessageEntity> msgDelList = (List<MessageEntity>)mAdapter.allSelectedObject();
		if(msgDelList == null)
			return;

		CustomConfirmDialog.Builder builder = new CustomConfirmDialog.Builder(getActivity());
		builder.setTitle("删除");
		builder.setMessage("确认删除?");
		builder.setPositiveBtn("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				mAdapter.removeItems(msgDelList);
				//TODO
				for(MessageEntity msg : msgDelList) {
					imService.getMessageManager().deleteMessageByMsgId(msg.getMsgId(), msg.getSessionKey());
				}
				CommonFunction.showToast("已删除");
				//退出多选模式
				onBack();
			}
		});
		builder.setNegativeBtn("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	OperateListDialog operateListDialog;
	private ArrayList<OperateListDialog.OperateItem> operateItems = new ArrayList<>();
	public void showLongPressDialog(final MessageEntity msg) {

		List<String> menuStr = new ArrayList<>();
		menuStr.add("发送给朋友");
		menuStr.add("删除");
		menuStr.add("更多");
		if(msg instanceof TextMessage){
			Pattern pattern = Pattern.compile("<img src=\"extra/");
			Matcher matcher = pattern.matcher(msg.getContent());
			if(matcher.find()){ //通话消息
				menuStr.clear();
				menuStr.add("删除");
			}else {
				menuStr.add(0, "复制");
			}
		} else if(msg instanceof AudioMessage){
			menuStr.remove(0);
		}else if(msg instanceof VideoMessage){

		}
		if (menuStr == null || menuStr.size() <= 0) {
			return;
		}

		if (operateListDialog == null) {
			operateListDialog = new OperateListDialog(getActivity());
			operateListDialog.setIconType(OperateListDialog.EIconType.RIGHT);
		}
		operateItems.clear();

		int size = menuStr.size();
		for (int i = 0; i< size; i++) {
			final OperateListDialog.OperateItem item = operateListDialog.new OperateItem();
			item.setmItemNameStr(menuStr.get(i));
			item.setmOperateKey(String.valueOf(i));

			item.setItemClickLister(new OperateListDialog.OperateItemClickListener() {
				@Override
				public void clickItem(int position) {
					String itemStr = item.getmItemNameStr();
					if("复制".equals(itemStr)){
						ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("copy", msg.getContent());
						clipboard.setPrimaryClip(clip);
						CommonFunction.showToast("已复制到剪贴板");
					}else if("发送给朋友".equals(itemStr)){
						transmitMsg(msg);
					}else if("删除".equals(itemStr)){
						onClickDelChatBtn(msg);
					}else if("更多".equals(itemStr)){
						mAdapter.setMode(R.id.MULTIPLE_CHOICE_MODE);
						setOnTouchListeners();
						setMsgLayoutUI(inputMsgMode.DEFAULT_MODE);
						msg_send_layout.setVisibility(View.GONE);
						tabbar.setVisibility(View.VISIBLE);

						mAdapter.clearSelectedObject();
						mAdapter.addSelectedObject(msg);
						setMoreOperationBtnEnableState();
					}

					if (operateListDialog != null) {
						operateListDialog.dismiss();
					}
				}
			});
			operateItems.add(item);
		}

		operateListDialog.showTitle(false);
		operateListDialog.setGravityType(0); //居中显示
		operateListDialog.updateOperateItems(operateItems);
		operateListDialog.show();
	}

	public void setMoreOperationBtnEnableState(){
		View[] views = {icon_transmit, icon_collection, icon_del, icon_email};
		for (View view : views){
			view.setEnabled(mAdapter.allSelectedObject() != null && mAdapter.allSelectedObject().size() > 0);
		}
	}

}
