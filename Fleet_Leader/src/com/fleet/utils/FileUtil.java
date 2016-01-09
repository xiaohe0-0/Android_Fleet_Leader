package com.fleet.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;

public class FileUtil {	
	//图片存储相关
	private static  String storagePath = "";
	
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = Utils.savePath_pic;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}
	
	/**保存Bitmap到sdcard
	 * @param b
	 */
	public static String saveBitmap(Bitmap b){

		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + dataTake +".jpg";
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
		return dataTake +".jpg";

	}
	
	
}
