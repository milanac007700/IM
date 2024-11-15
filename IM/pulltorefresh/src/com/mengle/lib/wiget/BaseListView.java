package com.mengle.lib.wiget;

import com.cheshang8.library.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;


import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;




	
public class BaseListView extends PullToRefreshListView{
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
	
	public BaseListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BaseListView(
			Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode,
			com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle style) {
		super(context, mode, style);
		init();
	}

	public BaseListView(Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode) {
		super(context, mode);
		init();
	}
	
	

	public BaseListView(Context context) {
		super(context);
		init();
	}
	
	
	
	

	private void init(){
		View footer = inflate(getContext(), R.layout.footer_loading, null);
		mFooterLoading = footer.findViewById(R.id.layout_checkmore);
		mFooterLoading.setVisibility(View.GONE);
		getRefreshableView().addFooterView(footer);
		setOnRefreshListener(new OnRefreshListener<ListView>() {

			
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				load(true);
				
			}
		});
		
		setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			
			public void onLastItemVisible() {
				if(hasMode){
					load(false);
				}
				
			}
		});
		
		setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) 
                        {
                            if(onLoadListener != null){
                            	onLoadListener.onScrollToBottom();
                            }
                        }
                      break;
                }
            }  
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                
            }

        });
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
					BaseListView.this.onRefreshComplete();
					offset += limit;
				}
				
			}.execute();
			
		}
	}
	/*protected BitmapDrawable writeOnDrawable(String text){
		int width = getWidth(),height = getHeight();
        Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint(); 
        paint.setColor(Color.parseColor("#a8a8a8")); 
        paint.setTextSize(DisplayUtils.spToPx(getContext(), 18)); 
        paint.setTextAlign(Align.CENTER); 
        paint.setAntiAlias(true);

        FontMetrics fontMetrics = paint.getFontMetrics(); 
        // 计算文字高度 
        float fontHeight = fontMetrics.bottom - fontMetrics.top; 
        // 计算文字baseline 
        float textBaseY = height - (height - fontHeight) / 2 - fontMetrics.bottom; 
        canvas.drawText(text, width / 2, textBaseY, paint);

        return new BitmapDrawable(bm);
    }*/
	
	
	
}
