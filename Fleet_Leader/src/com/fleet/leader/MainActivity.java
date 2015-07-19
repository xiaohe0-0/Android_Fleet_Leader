package com.fleet.leader;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushListener.PushMessageListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.fleet.chat.R;
import com.fleet.utils.HttpUtils;
import com.fleet.utils.Utils;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	// ����
	private String idPre = "��܇��ݣ�";
	private String idName = "Leader";
	private String msgPre = "push_message";
	private String apiKey = "TPO9PYH8sULRrUuYHyeCqX7e";
	private String[] sendLevel = { "G", "A" };
	private String[] sendTags = {"group","all"};

	private String postStr;
	private String selectedLevel;
	private String sendTag;
	private String sendStr;

	// Layout�ؼ�
	private TextView text_id;
	private TextView text_all;
	private TextView text_group;
	private Button btn_bind;
	private Button btn_send;
	private ScrollView scroll_all;
	private ScrollView scroll_group;
	private Spinner spin_level;
	private EditText edit_send;

	// �ٶȵ�ͼ
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;

	// ��λ���
	private LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	private boolean isFirstLoc = true;// �Ƿ��״ζ�λ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_main);
		edit_send = (EditText) this.findViewById(R.id.edit_send);

		// ѡ����Ϣ����
		spin_level = (Spinner) this.findViewById(R.id.spin_level);
		spin_level.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				selectedLevel = (String) spin_level.getSelectedItem();
				if(selectedLevel.equals(sendLevel[0])){
					sendTag = sendTags[0];
				}
				if (selectedLevel.equals(sendLevel[1])) {
					sendTag = sendTags[1];
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				selectedLevel = sendLevel[0];
				sendTag = sendTags[0];
			}
		});

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(18).build()));
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(100);
		mLocClient.setLocOption(option);
		mLocClient.start();

		// ���ó������
		text_id = (TextView) this.findViewById(R.id.text_id);
		text_id.setText(idPre + idName);

		// ��Ϣ��ʾ����
		text_all = (TextView) this.findViewById(R.id.text_all);
		text_group = (TextView) this.findViewById(R.id.text_group);
		scroll_all = (ScrollView) this.findViewById(R.id.scroll_all);
		scroll_group = (ScrollView) this.findViewById(R.id.scroll_group);

		// ������Ϣ
		btn_send = (Button) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendStr = edit_send.getText().toString().trim();
				if (sendStr.equals("")) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"������Ϣ����Ϊ�գ�", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					new Thread() {
						public void run() {
							JSONObject jsonObject1 = new JSONObject();// push_message��Ϣ��
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							try {
								if (selectedLevel.equals(sendLevel[0])) {// G
									// �����Ϣ����
									try {
										jsonObject1.put("message_type", "text");
										jsonObject1.put("src_tag", "leader");
										jsonObject1.put("src_id", "");
										jsonObject1.put("attr", "common");
										jsonObject1.put("location", "test");
										jsonObject1.put("push_type", "2");
										jsonObject1.put("tag_name", sendTag+"1");
										jsonObject1.put("content",
												sendStr);
										jsonObject1.put("user_id",
												Utils.MyUserID);
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
									}
									params.add(new BasicNameValuePair(
											msgPre, jsonObject1
													.toString()));// ��װ��Ϣʵ��
								}
								if (selectedLevel.equals(sendLevel[1])) {// A

								}
								mHandler.sendEmptyMessage(3);
								postStr = HttpUtils.PostData(params);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 mHandler.sendEmptyMessage(2);
						};
					}.start();
				}

			}
		});

		// �󶨰ٶ����ͷ���
		btn_bind = (Button) this.findViewById(R.id.btn_bind);
		btn_bind.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				PushManager.startWork(getApplicationContext(),
						PushConstants.LOGIN_TYPE_API_KEY, apiKey);
				List<String> list = new ArrayList<String>();
				list.add("leader");
				PushManager.setTags(getApplicationContext(), list);
				List<String> delList = new ArrayList<String>();
				delList.add("group");
				delList.add("member");
				PushManager.delTags(getApplicationContext(), delList);
			}
		});
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:

				break;
			case 2:
				text_group.setText(postStr);
				break;
			case 3:
				edit_send.setText("");
				break;
			default:
				break;
			}
		};
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
	}

	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (Utils.logString != "") {
			text_all.append(Utils.logString + "\n");
			scroll2Bottom(scroll_all, text_all);
		}
	}

	/**
	 * ���ƽ������Զ������׶�
	 */
	public static void scroll2Bottom(final ScrollView scroll, final View inner) {
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (scroll == null || inner == null)
					return;
				int offset = inner.getMeasuredHeight()
						- scroll.getMeasuredHeight();
				if (offset < 0) {
					offset = 0;
				}
				scroll.scrollTo(0, offset);
			}
		});
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
}
