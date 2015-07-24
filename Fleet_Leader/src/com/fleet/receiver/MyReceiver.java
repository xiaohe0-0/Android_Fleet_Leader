package com.fleet.receiver;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle.Delegate;
import android.text.TextUtils;
import android.util.JsonReader;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.fleet.domain.DeliverMsg;
import com.fleet.leader.MainActivity;
import com.fleet.utils.Utils;

/**
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下： 0 - Success 10001 - Network Problem 30600 - Internal
 * Server Error 30601 - Method Not Allowed 30602 - Request Params Not Valid
 * 30603 - Authentication Failed 30604 - Quota Use Up Payment Required 30605 -
 * Data Required Not Found 30606 - Request Time Expires Timeout 30607 - Channel
 * Token Timeout 30608 - Bind Relation Not Found 30609 - Bind Number Too Many
 * 
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 * 
 */
public class MyReceiver extends FrontiaPushMessageReceiver {

	public MyReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onBind(Context context, int errorCode, String appid,
			String userId, String channelId, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onBind errorCode=" + errorCode + " appid="
				+ appid + " userId=" + userId + " channelId=" + channelId
				+ " requestId=" + requestId;
		Utils.MyChannelId = channelId;
		Utils.MyUserID = userId;
		Utils.deliverMsg = new DeliverMsg();
		String bindResStr = "";
		if (errorCode == 0) {
			bindResStr = "Bind Successed!";
		} else {
			bindResStr = "Bind Failed";
		}
		updateContent(context, bindResStr);
	}

	@Override
	public void onDelTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		// TODO Auto-generated method stub
		Utils.deliverMsg = new DeliverMsg();
		String responseString = "onDelTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;

		// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
		// updateContent(context, responseString);
	}

	@Override
	public void onListTags(Context context, int errorCode, List<String> tags,
			String requestId) {
		// TODO Auto-generated method stub
		Utils.deliverMsg = new DeliverMsg();
		String responseString = "onListTags errorCode=" + errorCode + " tags="
				+ tags;

		// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
		updateContent(context, responseString);
	}

	@Override
	public void onMessage(Context context, String message,
			String customContentString) {
		// TODO Auto-generated method stub
		String messageString = "透传消息 message=\"" + message
				+ "\" customContentString=" + customContentString;
		Utils.deliverMsg = new DeliverMsg();
		JSONObject jsonObject = new JSONObject();
		if (!TextUtils.isEmpty(message)) {
			try {
				jsonObject = new JSONObject(message);
				Utils.deliverMsg.setMessage_type(jsonObject
						.getString("message_type"));
				Utils.deliverMsg.setAttr(jsonObject.getString("attr"));
				Utils.deliverMsg.setContent(jsonObject.getString("content"));
				Utils.deliverMsg.setSrc_tag(jsonObject.getString("src_tag"));
				Utils.deliverMsg.setLocation(jsonObject.getString("location"));
				Utils.deliverMsg.setPush_type(jsonObject.getString("push_type"));

				// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
				updateContent(context, Utils.deliverMsg.getContent());
				//updateContent(context, Utils.deliverMsg.getContent());
				//updateContent(context, messageString);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				updateContent(context, e1.toString());
			}
		}


		// 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
		// if (!TextUtils.isEmpty(customContentString)) {
		// JSONObject customJson = null;
		// try {
		// customJson = new JSONObject(customContentString);
		// String myvalue = null;
		// if (customJson.isNull("mykey")) {
		// myvalue = customJson.getString("mykey");
		// }
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
	}

	@Override
	public void onNotificationClicked(Context context, String title,
			String description, String customContentString) {
		// TODO Auto-generated method stub
		String notifyString = "通知点击 title=\"" + title + "\" description=\""
				+ description + "\" customContent=" + customContentString;
		Utils.deliverMsg = new DeliverMsg();

		// 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
		if (!TextUtils.isEmpty(customContentString)) {
			JSONObject customJson = null;
			try {
				customJson = new JSONObject(customContentString);
				String myvalue = null;
				if (customJson.isNull("mykey")) {
					myvalue = customJson.getString("mykey");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
		updateContent(context, notifyString);
	}

	@Override
	public void onSetTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onSetTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;
		Utils.deliverMsg = new DeliverMsg();

		// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
		// updateContent(context, responseString);
	}

	@Override
	public void onUnbind(Context context, int errorCode, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onUnbind errorCode=" + errorCode
				+ " requestId = " + requestId;
		Utils.deliverMsg = new DeliverMsg();
		// Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
		updateContent(context, responseString);
	}

	private void updateContent(Context context, String content) {
		// TODO Auto-generated method stub
		
		Utils.intentSign = true;
		Intent intent = new Intent(context.getApplicationContext(),
				MainActivity.class);
		try {
			Utils.logString = new String(content.getBytes(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Utils.logString = e.toString();
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.getApplicationContext().startActivity(intent);
	}
	
}
