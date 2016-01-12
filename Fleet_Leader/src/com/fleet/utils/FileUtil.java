package com.fleet.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class FileUtil {
	// 图片存储相关
	private static String storagePath = "";
	private static String storagePath_voice = "";

	private static String initPath() {
		if (storagePath.equals("")) {
			storagePath = Utils.savePath_pic;
			File f = new File(storagePath);
			if (!f.exists()) {
				f.mkdir();
			}
		}
		return storagePath;
	}

	private static String initPath_voice() {
		if (storagePath_voice.equals("")) {
			storagePath_voice = Utils.savePath_voice;
			File f = new File(storagePath_voice);
			if (!f.exists()) {
				f.mkdir();
			}
		}
		return storagePath_voice;
	}

	/**
	 * 保存Bitmap到sdcard
	 * 
	 * @param b
	 */
	public static String saveBitmap(Bitmap b) {

		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + dataTake + ".jpg";
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataTake + ".jpg";

	}

	// 加载语音输入流
	public static InputStream getUrlVoice(String url) {
		InputStream is = null;
		try {
			URL voiceurl = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) voiceurl
					.openConnection();
			conn.setConnectTimeout(6000);// 设置超时
			conn.setDoInput(true);
			conn.setUseCaches(false);// 不缓存
			conn.connect();
			is = conn.getInputStream();// 获得图片的数据流

			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	public static String saveVoice(InputStream is) {
		String path = initPath_voice();
		long dataTake = System.currentTimeMillis();
		String voiceName = path + "/" + dataTake + ".amr";
		OutputStream outputStream = null;
		File file = new File(voiceName);
		try {
			
			file.createNewFile();
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[4 * 1024];
			while (is.read(buffer) != -1) {
				outputStream.write(buffer);
			}
			outputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dataTake + ".amr";
	}

}
