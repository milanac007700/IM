package com.mengle.lib.wiget;

import com.cheshang8.library.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ScrollView;;



	
public class BaseScrollView extends PullToRefreshScrollView{
	public static interface OnLoadListener{
		public boolean onLoad(int offset,int limit);
		public void onLoadSuccess();
		public void onScrollToBottom();
	}
	
	private int offset = 0;
	
	private OnLoadListener onLoadListener;
	
	private View mFooterLoading;
	
	protected boolean hasMode = false;
	
	private int limit = 20;
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOnLoadListener(OnLoadListener onLoadListener) {
		this.onLoadListener = onLoadListener;
	}
	
	public BaseScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BaseScrollView(
			Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode,
			com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle style) {
		super(context, mode, style);
		init();
	}

	public BaseScrollView(Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode) {
		super(context, mode);
		init();
	}
	
	

	public BaseScrollView(Context context) {
		super(context);
		init();
	}
	
	
	
	

	private void init(){
		View footer = inflate(getContext(), R.layout.footer_loading, null);
		mFooterLoading = footer.findViewById(R.id.layout_checkmore);
		mFooterLoading.setVisibility(View.GONE);
//		getRefreshableView().addFooterView(footer);
		
		setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				
				load(true);
				
			}
		});
		
//		setOnScrollListener(new OnScrollListener() {
//            
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                switch (scrollState) {
//                    // 当不滚动时
//                    case OnScrollListener.SCROLL_STATE_IDLE:
//                        // 判断滚动到底部
//                        if (onLoadListener != null) 
//                        {
//                            onLoadListener.onScrollToBottom();
//                        }
//                        break;
//                }
//            }
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
//                    int totalItemCount) {
//                // TODO Auto-generated method stub
//                
//            }
//
//        });
	}
	
	public void load(boolean refresh){
		
		hasMode = false;
		if(refresh){
			setRefreshing(true);
			offset = 0;
		}
		if(onLoadListener != null){
			
			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					
					return onLoadListener.onLoad(offset,limit);
				}
				
				@Override
				protected void onPostExecute(Boolean result) {
					onLoadListener.onLoadSuccess();
					hasMode = result;
					if(!result){
						mFooterLoading.setVisibility(View.GONE);
					}else{
						mFooterLoading.setVisibility(View.VISIBLE);
					}
					BaseScrollView.this.onRefreshComplete();
					offset += limit;
				}
				
			}.execute();
			
		}
	}
}
