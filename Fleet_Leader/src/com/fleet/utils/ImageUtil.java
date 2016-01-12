package com.fleet.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class ImageUtil {
	/**
	 * ��תBitmap
	 * 
	 * @param b
	 * @param rotateDegree
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree) {
		Matrix matrix = new Matrix();
		matrix.postRotate((float) rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
				b.getHeight(), matrix, false);
		return rotaBitmap;
	}

	// ����ͼƬ
	public static Bitmap getUrlImage(String url) {
		Bitmap img = null;
		try {
			URL picurl = new URL(url);
			// �������
			HttpURLConnection conn = (HttpURLConnection) picurl
					.openConnection();
			conn.setConnectTimeout(6000);// ���ó�ʱ
			conn.setDoInput(true);
			conn.setUseCaches(false);// ������
			conn.connect();
			InputStream is = conn.getInputStream();// ���������������
			img = BitmapFactory.decodeStream(is);

			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img;
	}
}
