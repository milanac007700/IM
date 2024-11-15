package com.milanac007.demo.im.ui;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import com.milanac007.demo.im.activity.BaseActivity;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.CommonFunction;

import java.io.File;
import java.io.IOException;

/**
 * Created by zqguo on 2016/10/17.
 */
public class CustomAudioCapture {
    public static final String TAG = CustomAudioCapture.class.getName();
    private MediaRecorder mMediaRecorder = null;
    private String currentFileName = null;
    private  VoiceRecordPop voice_record_pop = null;
    private Handler mHandler = null;
    private Handler mCaculateTimeHandler = null;
    private static final int MAX_CAPTURE_TIME = 1000 * 60; //60秒
    private  long mStartCaptureTime;
    private  long mEndCaptureTime;
    private  int captureTime = -1;
    private Activity mActivity = null;
    private boolean hasPermission = true;
    private Logger logger = Logger.getLogger();

    public CustomAudioCapture(Activity mActivity, CaculateListener caculateListener){
        this.mActivity = mActivity;
        mCaculateListener = caculateListener;
    }

    public void startCapture(){
        try {

        if(mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS); //有的设备不支持播放此格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setMaxDuration(MAX_CAPTURE_TIME);
        }

        if(voice_record_pop == null){
            voice_record_pop = new VoiceRecordPop(mActivity);
        }

        currentFileName = CommonFunction.getDirUserTemp() + File.separator + System.currentTimeMillis() + ".aac";
        mMediaRecorder.setOutputFile(currentFileName);

            voice_record_pop.showPop();
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            setVolumeValue();
            caculateCaptureTime();
            mStartCaptureTime = System.currentTimeMillis();
        } catch (RuntimeException e){
            logger.e("%s", e.getMessage());
            CommonFunction.showToast("打开录音设备失败，请确认开启录音权限后重试");
            hasPermission = false;
        } catch (IOException e){
            logger.e("%s", e.getMessage());
        }
    }

    public String getCurrentFileName(){
        return currentFileName;
    }

    public void destroy(){

        releaseMediaRecorder();

        if(voice_record_pop != null){
            voice_record_pop.dismiss();
            voice_record_pop = null;
        }

        if(mHandler != null){
            mHandler.removeCallbacks(volumeRunnable);
            mHandler = null;
        }

        volumeRunnable = null;

        if(mCaculateTimeHandler != null){
            mCaculateTimeHandler.removeCallbacks(caculateRemainTimeRunnable);
            mCaculateTimeHandler = null;
        }

        caculateRemainTimeRunnable = null;
        mActivity = null;
    }

    public long stopCapture(){
        releaseMediaRecorder();

        if(hasPermission){
            mEndCaptureTime  = System.currentTimeMillis();
            long captureTime = (mEndCaptureTime - mStartCaptureTime)/1000;
            logger.e("captureTime: %s", captureTime);
            if(captureTime < 2){
                voice_record_pop.setShortRecordView();
            }else {
                if(voice_record_pop != null){
                    voice_record_pop.dismiss();
                }
            }

            if(mHandler != null){
                mHandler.removeCallbacks(volumeRunnable);
                voice_record_pop.setVolumeValue(0);
            }

            if(mCaculateTimeHandler != null){
                mCaculateTimeHandler.removeCallbacks(caculateRemainTimeRunnable);
            }

            this.captureTime = -1;

            return captureTime;
        }else {
            hasPermission = true;
            if(voice_record_pop != null){
                voice_record_pop.dismiss();
            }
            return -1;
        }


    }

    private void releaseMediaRecorder(){
        if(mMediaRecorder != null){
            //报错为：RuntimeException:stop failed
            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop(); //Note that a RuntimeException is intentionally thrown to the application, if no valid audio/video data has been received when stop() is called. This happens if stop() is called immediately after start().
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }catch (RuntimeException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }

            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public  void cancelCapture(){
        releaseMediaRecorder();

        File file = new File(currentFileName);
        if(file.exists()){
            file.delete();
        }
        currentFileName = null;

        if(voice_record_pop != null){
            voice_record_pop.dismiss();
        }

        if(mHandler != null){
            mHandler.removeCallbacks(volumeRunnable);
            voice_record_pop.setVolumeValue(0);
        }


        if(mCaculateTimeHandler != null){
            mCaculateTimeHandler.removeCallbacks(caculateRemainTimeRunnable);
        }

        captureTime = -1;
        hasPermission = true;
    }

    public  void onMoveLayout(boolean isInLayout){
        if(voice_record_pop != null){
            voice_record_pop.setRecordAndCancelView(isInLayout);
        }
    }

    final int mBase = 1;
    final int mSampletTmeInterval = 200; //200ms
    private Runnable volumeRunnable = new Runnable() {
        @Override
        public void run() {

            if(mMediaRecorder != null && voice_record_pop != null){

                int maxAmplitude = mMediaRecorder.getMaxAmplitude();
                final int volumeValue = (int)(20 * Math.log10(maxAmplitude/mBase));
                logger.e("maxAmplitude: %d, volumeValue: %d",maxAmplitude, volumeValue);

                if(volumeValue > 0){
                    voice_record_pop.setVolumeValue(volumeValue);
                }
                mHandler.postDelayed(this, mSampletTmeInterval);
            }
        }
    };
    /**
     * 计算采样的分贝值 0-120
     */
    public  void setVolumeValue(){

        if(mHandler == null){
            mHandler = new Handler(Looper.getMainLooper());
        }

        mHandler.postDelayed(volumeRunnable, mSampletTmeInterval);
    }

    private Runnable caculateRemainTimeRunnable = new Runnable() {
        @Override
        public void run() {
            captureTime++;
            logger.e("RemainTime: %d", (60 - captureTime));

            if(voice_record_pop != null){
                if(60 - captureTime <= 10){ //10秒倒计时
                    voice_record_pop.setRemainTime(60 - captureTime);
                }
            }

            if(captureTime > 60){
                if(mCaculateListener != null){
                    mCaculateListener.onFinish();
                    mCaculateTimeHandler.removeCallbacks(this);
                }
            }else {
                mCaculateTimeHandler.postDelayed(this, 1000);
            }
        }
    };

    private  void caculateCaptureTime(){
        if(mCaculateTimeHandler == null){
            mCaculateTimeHandler = new Handler(Looper.getMainLooper());
        }
        captureTime = -1;
        mCaculateTimeHandler.postDelayed(caculateRemainTimeRunnable, 1000);

    }

    public interface CaculateListener {
         void onFinish();
    }

    private  CaculateListener mCaculateListener = null;
    public  void setCaculateListener(CaculateListener listener){
        mCaculateListener = listener;
    }

}
