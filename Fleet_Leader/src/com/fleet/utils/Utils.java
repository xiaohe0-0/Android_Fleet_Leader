package com.fleet.utils;

import com.fleet.domain.DeliverMsg;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class Utils {
	
	public static String logString = "";
	public static String logLocation = "";
	public static String MyChannelId = "";
	public static String MyUserID = "";
	public static DeliverMsg deliverMsg;
	public static boolean intentSign = false;
	public static String uploadPicIp = "http://202.118.75.193:81/push_demo/upload.php";

	public Utils() {
		// TODO Auto-generated constructor stub
	}
	
	 // ��ȡApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
        	return e.toString();
        }
        return apiKey;
    }

}
