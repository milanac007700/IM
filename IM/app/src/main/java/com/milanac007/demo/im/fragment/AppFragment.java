package com.milanac007.demo.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanac007.demo.im.R;
import com.milanac007.demo.im.activity.MainActivity;
import com.milanac007.demo.im.event.SessionEvent;
import com.milanac007.demo.im.interfaces.OnActionListener;

public class AppFragment extends BaseFragment implements OnClickListener {

	private static AppFragment newInstance = null;

	public synchronized static AppFragment newInstance() {
		if(newInstance == null) {
			newInstance = new AppFragment();
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
	}

	@Override
	public int getPageNumber() {
		return MainActivity.SCREEN_APP;
	}


	protected void setTitle(TextView mTitle){
		mTitle.setTextSize(18);
		mTitle.setText(getResources().getString(R.string.g_app));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_app, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.fragment_head1_back).setVisibility(View.GONE);

		TextView mTitle = (TextView)view.findViewById(R.id.fragment_head1_title);
		mTitle.setVisibility(View.VISIBLE);
		setTitle(mTitle);
	}

	private void onClickSearch(){
		mListener.OnAction(getPageNumber(), OnActionListener.Action.ACTION_SWITCH_SCREEN, OnActionListener.Page.SCREEN_SEARCH, "1");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		}
	}

}
