package com.fleet.receiver;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.fleet.utils.*;
import com.fleet.activity.GroupActivity;
import com.fleet.activity.MainActivity;
import com.fleet.activity.MapActivity;
import com.fleet.domain.*;

/**
 * Push��Ϣ����receiver�����д����Ҫ�Ļص������� һ����˵�� onBind�Ǳ���ģ���������startWork����ֵ��
 * onMessage��������͸����Ϣ�� onSetTags��onDelTags��onListTags��tag��ز����Ļص���
 * onNotificationClicked��֪ͨ�����ʱ�ص��� onUnbind��stopWork�ӿڵķ���ֵ�ص�
 * 
 * ����ֵ�е�errorCode���������£� 0 - Success 10001 - Network Problem 30600 - Internal
 * Server Error 30601 - Method Not Allowed 30602 - Request Params Not Valid
 * 30603 - Authentication Failed 30604 - Quota Use Up Payment Required 30605 -
 * Data Required Not Found 30606 - Request Time Expires Timeout 30607 - Channel
 * Token Timeout 30608 - Bind Relation Not Found 30609 - Bind Number Too Many
 * 
 * �����������Ϸ��ش���ʱ��������Ͳ����������⣬����ͬһ����ķ���ֵrequestId��errorCode��ϵ����׷�����⡣
 * 
 */
public class MyReceiver extends FrontiaPushMessageReceiver {
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

		// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
		// updateContent(context, responseString);
	}

	@Override
	public void onListTags(Context context, int errorCode, List<String> tags,
			String requestId) {
		// TODO Auto-generated method stub
		Utils.deliverMsg = new DeliverMsg();
		String responseString = "onListTags errorCode=" + errorCode + " tags="
				+ tags;

		// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
		updateContent(context, responseString);
	}

	@Override
	public void onMessage(Context context, String message,
			String customContentString) {
		// TODO Auto-generated method stub
		String messageString = "͸����Ϣ message=\"" + message
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
				Utils.deliverMsg
						.setPush_type(jsonObject.getString("push_type"));
				Utils.deliverMsg.setSrc_id(jsonObject.getString("src_id"));

				// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
				updateContent(context, Utils.deliverMsg.getContent());
				// updateContent(context, Utils.deliverMsg.getContent());
				// updateContent(context, messageString);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				updateContent(context, e1.toString());
			}
		}

		// �Զ������ݻ�ȡ��ʽ��mykey��myvalue��Ӧ͸����Ϣ����ʱ�Զ������������õļ���ֵ
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
		String notifyString = "֪ͨ��� title=\"" + title + "\" description=\""
				+ description + "\" customContent=" + customContentString;
		Utils.deliverMsg = new DeliverMsg();

		// �Զ������ݻ�ȡ��ʽ��mykey��myvalue��Ӧ֪ͨ����ʱ�Զ������������õļ���ֵ
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

		// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
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

		// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
		// updateContent(context, responseString);
	}

	@Override
	public void onUnbind(Context context, int errorCode, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onUnbind errorCode=" + errorCode
				+ " requestId = " + requestId;
		Utils.deliverMsg = new DeliverMsg();
		// Demo���½���չʾ���룬Ӧ��������������Լ��Ĵ����߼�
		updateContent(context, responseString);
	}

	private void updateContent(Context context, String content) {
		// TODO Auto-generated method stub
		Utils.intentSign = true;
		Intent intent = new Intent();

		intent.setClass(context.getApplicationContext(), MainActivity.class);
		try {
			Utils.logString = new String(content.getBytes(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Utils.logString = e.toString();
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (intent != null)
			context.getApplicationContext().startActivity(intent);
	}
}
