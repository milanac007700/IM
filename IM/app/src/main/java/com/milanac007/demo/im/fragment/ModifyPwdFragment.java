package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.interfaces.OnActionListener;
import com.milanac007.demo.im.utils.CommonFunction;

import static com.milanac007.demo.im.utils.CommonFunction.showToast;

public class ModifyPwdFragment extends BaseFragment implements OnClickListener{
	private EditText mPwd1;
	private EditText mPwd2;
	private EditText mOldPwd;
	private Button btnOk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_resetpwd, null);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.fragment_head1_back:{
				hideKey();
				onBack();
			}break;

		}
	}


	protected void setTitle(TextView mTitle){
		mTitle.setText(getResources().getString(R.string.g_modify_password));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView mBack = (TextView)view.findViewById(R.id.fragment_head1_back);
		mBack.setOnClickListener(this);

		TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
		mTitle.setVisibility(View.VISIBLE);
		setTitle(mTitle);

		mPwd1 = (EditText) view.findViewById(R.id.pwd1);
		mPwd2 = (EditText) view.findViewById(R.id.pwd2);
		mOldPwd = (EditText) view.findViewById(R.id.oldpwd);
		btnOk = (Button) view.findViewById(R.id.btnOK);

		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true; /* 防止点击穿透，底层的fragment响应上层点击触摸事件 */
			}
		});

		mPwd1.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				if(TextUtils.isEmpty(str)){
					btnOk.setEnabled(false);
				}else {
					btnOk.setEnabled(true);
				}
			}
		});

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideKey();
				setpwd();
			}

		});

	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		mPwd1.setText("");
		mPwd2.setText("");
		mOldPwd.setText("");
	}

	@Override
	public int getPageNumber() {
		return OnActionListener.Page.SCREEN_RESETPWD;
	}


	private void setpwd() {
		String oldpwd = mOldPwd.getText().toString().trim();
		String pwd1 = mPwd1.getText().toString().trim();
		String pwd2 = mPwd2.getText().toString().trim();

		if (TextUtils.isEmpty(oldpwd)) {
			showToast("请输入原密码");
			return;
		}else if (TextUtils.isEmpty(pwd1)) {
			showToast("请输入新密码");
			return;
		}else if (TextUtils.isEmpty(pwd2)) {
			showToast("请输入确认密码");
			return;
		} else if(pwd1.length() < 8 || pwd1.length() > 16){
			showToast("密码为8-16位字符");
			return;
		} else if (!CommonFunction.isPasswordLegal(pwd1)) {
			showToast("密码为8-16位字符, 需要包含数字和字母");
			return;
		} else if (!pwd1.equals(pwd2)) {
			showToast("两次输入密码不一致");
			return;
		}

		//TODO
//		CommonFunction.showProgressDialog(getActivity(), "请稍候");
//		ModifyPwdRequest request = new ModifyPwdRequest(oldpwd, pwd1);
//		request.request(new RequestHandler() {
//
//			@Override
//			public void onSuccess(Object object) {
//				getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						CommonFunction.dismissProgressDialog();
//					}
//				});
//
//				JSONObject resultOjbect = (JSONObject)object;
//				boolean result = resultOjbect.getBoolean("result");
//				if(!result){
//					showToast(String.format("修改密码失败\n%s", resultOjbect.getString("error")));
//				}else {
//					showToast("修改密码成功");
//					Preferences.setPassword("");
//					mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_RETURN, 0, null);
//				}
//			}
//
//
//			@Override
//			public void onServerError(final int errorCode, Header[] headers, byte[] responseBody, final Throwable error) {
//				CommonFunction.dismissProgressDialog();
//
//				String message = error.getMessage();
//				if(!TextUtils.isEmpty(message)){
//					showToast("errorCode"+ errorCode +": " + message);
//				}else {
//					showToast("errorCode"+ errorCode +": " + "网络连接异常");
//				}
//			}
//
//		});
	}

}
