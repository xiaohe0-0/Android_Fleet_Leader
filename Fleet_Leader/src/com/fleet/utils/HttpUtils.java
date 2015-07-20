package com.fleet.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtils {
	static String path = "http://202.118.75.193:81/push_demo/push.php";// 推送地址

	public HttpUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 向服务器POST数据
	 * @return
	 */
	public static String PostData(List<NameValuePair> params) {
		HttpClient client = new DefaultHttpClient();
		StringBuilder builder = new StringBuilder();
		HttpPost post = new HttpPost(path);
		HttpResponse response;

		try {
			post.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return e1.toString();
		}

		// 接收服务器返回消息
		try {
			response = client.execute(post);
			HttpEntity entity = response.getEntity();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					entity.getContent()));
//			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
//				builder.append(s);
//			}
			builder.append(response.getStatusLine().getStatusCode());
			return builder.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}
	}
}
