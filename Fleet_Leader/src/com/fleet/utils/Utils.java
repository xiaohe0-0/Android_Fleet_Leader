package com.fleet.utils;

import android.R.anim;

public class Utils {
	public static String externalPath = android.os.Environment.getExternalStorageDirectory()+"";
	public static String savePath_voice = externalPath+"/Carticipate/Voice/";
	public static String savePath_pic = externalPath+"/Carticipate/Photo/";
	public static String upload_pic_ip = "http://202.118.75.193:81/push_demo/upload_pic.php";
}
