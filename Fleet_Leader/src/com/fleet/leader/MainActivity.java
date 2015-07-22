package com.fleet.leader;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

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
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.fleet.chat.R;
import com.fleet.domain.LocationOfCar;
import com.fleet.utils.HttpUtils;
import com.fleet.utils.Utils;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
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
	// 常参
	// private String idPre = "本車身份：";
	// private String idName = "Leader";
	private String msgPre = "push_message";
	private String apiKey = "TPO9PYH8sULRrUuYHyeCqX7e";
	private String[] sendLevel = { "G", "A" };
	private String[] sendTags = { "group", "all" };

	private String postStr;
	private String selectedLevel;
	private String sendTag;
	private String sendStr;
	private LatLng myLatLng;
	private List<LocationOfCar> locs;

	// Layout控件
	// private TextView text_id;
	private TextView text_all;
	private TextView text_group;
	private Button btn_send;
	private ScrollView scroll_all;
	private ScrollView scroll_group;
	private Spinner spin_level;
	private EditText edit_send;

	// 百度地图
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private Marker marker[];
	private List<Marker> locMarkers;
	private BitmapDescriptor bitmapDescriptor;

	// 定位相关
	private LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	private boolean isFirstLoc = true;// 是否首次定位

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_main);
		edit_send = (EditText) this.findViewById(R.id.edit_send);

		// 选择消息级别
		spin_level = (Spinner) this.findViewById(R.id.spin_level);
		spin_level.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				selectedLevel = (String) spin_level.getSelectedItem();
				if (selectedLevel.equals(sendLevel[0])) {
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
		myLatLng = new LatLng(38.90, 121.53);

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.showScaleControl(true);// 隐藏地图上的比例尺
		mMapView.showZoomControls(false);// 隐藏地图上的缩放控件
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(12).build()));

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(2000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		LatLng tmploc = new LatLng(myLatLng.latitude - Math.random() / 40,
				myLatLng.longitude + Math.random() / 22);
		locs = new ArrayList<LocationOfCar>();
		locMarkers = new ArrayList<Marker>();
		LocationOfCar tmpLocar = new LocationOfCar("Run", "123", tmploc);
		locs.add(tmpLocar);
		tmploc = new LatLng(myLatLng.latitude - Math.random() / 30,
				myLatLng.longitude - Math.random() / 22);
		tmpLocar = new LocationOfCar("Run1", "1232", tmploc);
		locs.add(tmpLocar);
		bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_member);
		setLocations(locs);

		// final LocationOfCar locations[] = new LocationOfCar[4];
		// tmploc = new LatLng(myLatLng.latitude + Math.random() / 30,
		// myLatLng.longitude + Math.random() / 30);
		// locations[0] = new LocationOfCar("car1", "2013.06.06", tmploc);
		// tmploc = new LatLng(myLatLng.latitude - Math.random() / 20,
		// myLatLng.longitude + Math.random() / 40);
		// locations[1] = new LocationOfCar("car2", "2014.06.06", tmploc);
		// tmploc = new LatLng(myLatLng.latitude + Math.random() / 10,
		// myLatLng.longitude + Math.random() / 60);
		// locations[2] = new LocationOfCar("car3", "2013.04.02", tmploc);
		// tmploc = new LatLng(myLatLng.latitude + Math.random() / 40,
		// myLatLng.longitude + Math.random() / 20);
		// locations[3] = new LocationOfCar("car4", "2013.04.02", tmploc);
		//
		// bitmapDescriptor = BitmapDescriptorFactory
		// .fromResource(R.drawable.icon_member);
		// initOverlay(locations);
		//
		// final LocationOfCar locations1[] = new LocationOfCar[2];
		// tmploc = new LatLng(myLatLng.latitude + Math.random() / 15,
		// myLatLng.longitude + Math.random() / 45);
		// locations1[0] = new LocationOfCar("group1", "2013.06.06", tmploc);
		// tmploc = new LatLng(myLatLng.latitude + Math.random() / 25,
		// myLatLng.longitude + Math.random() / 35);
		// locations1[1] = new LocationOfCar("group1", "2013.06.06", tmploc);
		// bitmapDescriptor = BitmapDescriptorFactory
		// .fromResource(R.drawable.icon_group);
		// initOverlay(locations1);

		// 设置车辆身份
		// text_id = (TextView) this.findViewById(R.id.text_id);
		// text_id.setText(idPre + idName);

		// 消息显示区域
		text_all = (TextView) this.findViewById(R.id.text_all);
		text_group = (TextView) this.findViewById(R.id.text_group);
		scroll_all = (ScrollView) this.findViewById(R.id.scroll_all);
		scroll_group = (ScrollView) this.findViewById(R.id.scroll_group);

		// 发送消息
		btn_send = (Button) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendStr = edit_send.getText().toString().trim();
				if (sendStr.equals("")) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"发送消息不能为空！", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					new Thread() {
						public void run() {
							JSONObject jsonObject1 = new JSONObject();// push_message消息体
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							try {
								if (selectedLevel.equals(sendLevel[0])) {// G
									// 添加消息内容
									try {
										jsonObject1.put("message_type", "text");
										jsonObject1.put("src_tag", "leader");
										jsonObject1.put("src_id",
												Utils.MyChannelId);
										jsonObject1.put("attr", "common");
										jsonObject1.put("location", "");
										jsonObject1.put("push_type", "2");
										jsonObject1.put("tag_name", sendTag
												+ "2");
										jsonObject1.put("content", sendStr);
										jsonObject1.put("user_id",
												Utils.MyUserID);
										params.add(new BasicNameValuePair(
												msgPre, jsonObject1.toString()));// 封装消息实体
										mHandler.sendEmptyMessage(3);
										String resFromServer = HttpUtils
												.PostData(params);
										if (!resFromServer.equals("200")) {
											postStr = "Send Failed";
											mHandler.sendEmptyMessage(2);
										}
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
										postStr = e2.toString();
										mHandler.sendEmptyMessage(2);
									} catch (Exception e) {
										// TODO: handle exception
										postStr = e.toString();
										mHandler.sendEmptyMessage(2);
									}

								}
								if (selectedLevel.equals(sendLevel[1])) {// A
									// 添加消息内容
									try {
										jsonObject1.put("message_type", "text");
										jsonObject1.put("src_tag", "leader");
										jsonObject1.put("src_id",
												Utils.MyChannelId);
										jsonObject1.put("attr", "common");
										jsonObject1.put("location", "");
										jsonObject1.put("push_type", "3");
										jsonObject1.put("tag_name", "");
										jsonObject1.put("content", sendStr);
										jsonObject1.put("user_id",
												Utils.MyUserID);
										params.add(new BasicNameValuePair(
												msgPre, jsonObject1.toString()));// 封装消息实体
										mHandler.sendEmptyMessage(5);
										String resFromServer = HttpUtils
												.PostData(params);
										if (!resFromServer.equals("200")) {
											postStr = "Send Failed"
													+ resFromServer;
											mHandler.sendEmptyMessage(4);
										}
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
										postStr = e2.toString();
										mHandler.sendEmptyMessage(4);
									} catch (Exception e) {
										// TODO: handle exception
										postStr = e.toString();
										mHandler.sendEmptyMessage(4);
									}
								}

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						};
					}.start();
				}

			}
		});

		// 绑定百度推送服务
		new Thread() {
			public void run() {
					PushManager.startWork(getApplicationContext(),
							PushConstants.LOGIN_TYPE_API_KEY, apiKey);
					List<String> list = new ArrayList<String>();
					list.add("leader");
					PushManager.setTags(getApplicationContext(), list);
					List<String> delList = new ArrayList<String>();
					delList.add("group");
					delList.add("member");
					PushManager.delTags(getApplicationContext(), delList);
			};
		}.start();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Date now = new Date();
			SimpleDateFormat df = new SimpleDateFormat("[HH:mm:ss] ");// 设置日期格式
			switch (msg.what) {
			case 1:

				break;
			case 2:
				text_group.append(df.format(now) + postStr + "\n");
				break;
			case 3:
				text_group.append(df.format(now) + "Leader:"
						+ edit_send.getText() + "\n");
				edit_send.setText("");
				break;
			case 4:
				text_all.append(df.format(now) + postStr + "\n");
				break;
			case 5:
				text_all.append(df.format(now) + "Leader:"
						+ edit_send.getText() + "\n");
				edit_send.setText("");
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
	protected void onStop() {
		// TODO Auto-generated method stub
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocClient.stop();

		// 关闭方向传感器
		// myOrientationListener.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
		mLocClient.start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
		super.onPause();
	}

	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Date now = new Date();
		SimpleDateFormat df = new SimpleDateFormat("[HH:mm:ss] ");// 设置日期格式
		//系统提醒消息 如绑定信息
		if (Utils.deliverMsg.getSrc_tag().equals("")) {
			if (!Utils.logString.equals("")) {
				text_all.append(df.format(now) + Utils.logString + "\n");
				scroll2Bottom(scroll_all, text_all);
			}
		} else {//群组消息
			if (Utils.deliverMsg.getSrc_tag().contains("group")) {
				text_group.append(df.format(now)
						+ Utils.deliverMsg.getSrc_tag() + ":" + Utils.logString
						+ "\n");
				scroll2Bottom(scroll_group, text_group);
			}
		}
		// text_all.append(Utils.logString + "\n");
	}

	/**
	 * 控制进度条自动滑到底端
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
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;

			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
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

	public void setLocations(List<LocationOfCar> locationList) {
		// marker = new Marker[locationList.size()];
		locMarkers = new ArrayList<Marker>();
		LatLng latLngs;
		for (int i = 0; i < locationList.size(); i++) {
			latLngs = locationList.get(i).getLocation();
			OverlayOptions overlayOptions_marker = new MarkerOptions()
					.position(latLngs).icon(bitmapDescriptor);
			// marker[i] = (Marker)
			// (mBaiduMap.addOverlay(overlayOptions_marker));
			locMarkers.add((Marker) (mBaiduMap
					.addOverlay(overlayOptions_marker)));
		}
	}

	// 地图标记
	public void initOverlay(LocationOfCar locations[]) {
		int count = locations.length;
		LatLng latLngs;
		// LatLngBounds bounds = null;
		// double min_latitude = 0, min_longitude = 0,
		// max_latitude = 0, max_longitude = 0;
		//
		// for(int i = 0; i < count-1; i++){
		// if(locations[i].getLocation().latitude <=
		// locations[i+1].getLocation().latitude){
		// min_latitude = locations[i].getLocation().latitude;
		// max_latitude = locations[i+1].getLocation().latitude;
		// }
		// else {
		// min_latitude = locations[i+1].getLocation().latitude;
		// max_latitude = locations[i].getLocation().latitude;
		// }
		// if(locations[i].getLocation().longitude <=
		// locations[i+1].getLocation().longitude){
		// min_longitude = locations[i].getLocation().longitude;
		// max_longitude = locations[i+1].getLocation().longitude;
		// }
		// else {
		// min_longitude = locations[i+1].getLocation().longitude;
		// max_longitude = locations[i].getLocation().longitude;
		// }
		// }
		marker = new Marker[count];
		for (int i = 0; i < count; i++) {
			latLngs = locations[i].getLocation();
			OverlayOptions overlayOptions_marker = new MarkerOptions()
					.position(latLngs).icon(bitmapDescriptor);
			marker[i] = (Marker) (mBaiduMap.addOverlay(overlayOptions_marker));
		}

		// LatLng southwest = new LatLng(min_latitude, min_longitude);
		// LatLng northeast = new LatLng(max_latitude, max_longitude);
		// LatLng northwest = new LatLng(max_latitude, min_longitude);
		// LatLng southeast = new LatLng(min_latitude, max_longitude);
		//
		// bounds = new
		// LatLngBounds.Builder().include(northeast).include(southwest).include(southeast).include(northwest).build();
		// MapStatusUpdate mapStatusUpdate =
		// MapStatusUpdateFactory.newLatLngBounds(bounds);
		// mBaiduMap.animateMapStatus(mapStatusUpdate,1000);
		// MapStatusUpdate mapStatusUpdate_zoom =
		// MapStatusUpdateFactory.zoomTo(10);
		// mBaiduMap.setMapStatus(mapStatusUpdate_zoom);
	}
}
