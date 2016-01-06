package com.fleet.function;

import java.io.File;
import java.io.IOException;

import com.fleet.utils.Utils;

import android.media.MediaRecorder;
import android.os.Environment;

public  class SoundMeter {
	static final private double EMA_FILTER = 0.6;

	private MediaRecorder mRecorder = null;
	private double mEMA = 0.0;
	private String voicePath;

	public void start(String name) {
		if (!Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return;
		}
		voicePath = Utils.savePath_voice;
		File file = new File(voicePath);
		//判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
		file.mkdir();
		}
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile(voicePath+name);
			try {
				mRecorder.prepare();
				mRecorder.start();
				
				mEMA = 0.0;
			} catch (IllegalStateException e) {
				System.out.print(e.getMessage());
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}

		}
	}

	public void stop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	public void pause() {
		if (mRecorder != null) {
			mRecorder.stop();
		}
	}

	public void start() {
		if (mRecorder != null) {
			mRecorder.start();
		}
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2700.0);
		else
			return 0;

	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
		return mEMA;
	}
}
