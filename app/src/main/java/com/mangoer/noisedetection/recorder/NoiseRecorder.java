package com.mangoer.noisedetection.recorder;


import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;


/**
 * @ClassName NoiseRecorder
 * @Description TODO()
 * @author Mangoer
 * @Date 2017/12/25 17:29
 */
public class NoiseRecorder {

    private final String TAG = "NoiseRecorder";

    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长10分钟

    private MediaRecorder mMediaRecorder;

    private String filePath;
    private long startTime;
    private long endTime;

    private int BASE = 1;
    private int SPACE = 500;// 间隔取样时间

    private Handler handler = new Handler();

    public NoiseRecorder(Handler handler){
        this.filePath = Environment.getExternalStorageDirectory()+ File.separator+System.currentTimeMillis()+".amr";
        this.handler=handler;
    }

    public void startRecord() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            startTime = System.currentTimeMillis();
            updateMicStatus();
        } catch (IllegalStateException e) {
            Log.e(TAG,"IllegalStateException="+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,"IOException="+e.getMessage());
        }
    }

    /**
     * 停止录音
     *
     */
    public long stopRecord() {
        if (mMediaRecorder == null)
            return 0L;
        endTime = System.currentTimeMillis();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        return endTime - startTime;
    }


    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };


    private void updateMicStatus() {

        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude() /BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = (20 * Math.log10(ratio))*0.7;
            Message message =Message.obtain();
            message.what=0X00;
            message.obj=db;
            handler.sendMessage(message);
            handler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }
}