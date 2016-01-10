package com.fleet.utils;

import com.fleet.domain.*;

public class Utils {
	public static String externalPath = android.os.Environment.getExternalStorageDirectory()+"";
	public static String savePath_voice = externalPath+"/Carticipate/Voice/";
	public static String savePath_pic = externalPath+"/Carticipate/Photo/";
	public static String upload_pic_ip = "http://202.118.75.193:81/push_demo/upload_pic.php";
	
	public static String logString = "";
	public static String logLocation = "";
	public static String MyChannelId = "";
	public static String MyUserID = "";
	public static String MyTag = "leader";
	public static String SendTag = "";
	public static String SendLevel = "";
	public static DeliverMsg deliverMsg;
	public static boolean intentSign = false;
	public static final String SendTitle = "Leader";
}
