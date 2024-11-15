package com.milanac007.demo.im.ui;

import android.app.Service;
import android.content.Context;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;

import androidx.appcompat.widget.AppCompatTextView;


public class VoiceSendButton extends AppCompatTextView {
	public static final String TAG = VoiceSendButton.class.getName();
	private boolean mIsCancel = false;
	private int mYpositon = -100;
	private RecordListener listener;
	protected String textOn;
	private String textOff;
	private String textCancel;

	public VoiceSendButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VoiceSendButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VoiceSendButton(Context context) {
		super(context);
		init();
	}

	protected void init() {
		textOn = App.getInstance().getString(R.string.m_press_speak);
		textOff = App.getInstance().getString(R.string.m_loosen_end);
		textCancel = App.getInstance().getString(R.string.m_send_cancel);
	}

	public RecordListener getListener() {
		return listener;
	}

	public void setListener(RecordListener listener) {
		this.listener = listener;
	}

	protected void setBackgroundByAction(int action){
		if(action == MotionEvent.ACTION_DOWN){
			setBackgroundResource(R.mipmap.search_click_bg);
		}else if(action == MotionEvent.ACTION_UP){
			setBackgroundResource(R.mipmap.search_bg);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (MotionEvent.ACTION_MASK & event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.e(TAG, "onTouchEvent ACTION_DOWN: getY = "+ event.getY());
			setText(textOff);
			setBackgroundByAction(MotionEvent.ACTION_DOWN);

			/**震动服务*/
			Vibrator vib = (Vibrator) App.getInstance().getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(15);//只震动一秒，一次 单位：ms

			if (listener != null) {
				listener.onStartRecord();
			}
			break;

		case MotionEvent.ACTION_UP:
			Log.e(TAG, "onTouchEvent ACTION_UP: getY = "+ event.getY());
			setText(textOn);
			setBackgroundByAction(MotionEvent.ACTION_UP);
			if (listener != null) {
				if (mIsCancel) {
					listener.onCancelRecord();
				} else {
					listener.onFinishRecord();

				}
			}
			break;

		case MotionEvent.ACTION_MOVE:

			Log.e(TAG, "onTouchEvent ACTION_MOVE: getY = "+ event.getY());
			if (event.getY() < mYpositon) {
				mIsCancel = true;
				if (listener != null) {
					listener.onMoveLayout(mIsCancel);
					setText(textCancel);
				}
			} else {
				mIsCancel = false;
				if (listener != null) {
					listener.onMoveLayout(mIsCancel);
					setText(textOff);
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:{
			Log.e(TAG, "cencel!!!");
			timeoutMotionActionUp();
		}break;
		default:
			break;
		}
		return true;
	}

	//发送ACTION_UP事件
	public void timeoutMotionActionUp(){
		int[] location = new int[2];
		this.getLocationInWindow(location);

		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis() + 100;
		float x = location[0] + 5;
		float y = location[1] + 5;

		int metaSate = 0;
		MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaSate);
		dispatchTouchEvent(motionEvent);
	}

	public interface RecordListener {

		void onStartRecord();

		void onFinishRecord();

		void onCancelRecord();

		void onMoveLayout(boolean isInLayout);
	}
}
