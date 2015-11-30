package com.fleet.leader;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
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
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.fleet.chat.R;
import com.fleet.domain.LocationOfCar;
import com.fleet.utils.HttpUtils;
import com.fleet.utils.Utils;

import android.support.v7.app.ActionBarActivity;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.Window;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	// 常参
	private final String msgPre = "push_message";// 解析接收到的消息时的前缀
	private final String apiKey = "TPO9PYH8sULRrUuYHyeCqX7e";// 百度PUSH的API KEY
	private final String[] sendLevel = { "G", "A" };// 下拉框选项
	private final String[] sendTags = { "group", "all" };// 向group/all发送时的TAG
	private final int freshTime = 1000;// 地图刷新时间
	private final int locScanPan = 2000;// 定位刷新时间
	private final int groupRecv = 2;// mHandlerMsg
	private final int groupSend = 3;// mHandlerMsg
	private final int allRecv = 4;// mHandlerMsg
	private final int allSend = 5;// mHandlerMsg
	private final int zoomLevel = 20;//Baidu map zoom level

	// 变量
	private String postStr;
	private String selectedLevel;
	private String sendTag;
	private String sendStr;
	private LatLng myLatLng;
	private List<LocationOfCar> locs = null;
	private Handler locHandler;
	private Vibrator vibrator;

	// Layout控件
	private TextView text_all;
	private TextView text_group;
	private Button btn_send;
	private Button btn_picture;
	private ScrollView scroll_all;
	private ScrollView scroll_group;
	private Spinner spin_level;
	private EditText edit_send;

	// 百度地图
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private Marker marker[] = null;
	private BitmapDescriptor bitmapDescriptor;

	// 定位相关
	private LocationClient mLocClient = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	private boolean isFirstLoc = true;// 是否首次定位

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		edit_send = (EditText) this.findViewById(R.id.edit_send);//发送Button
		locs = new ArrayList<LocationOfCar>();//位置数组
		bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_member);//图标
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);//震动

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

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.showScaleControl(true);// 隐藏地图上的比例尺
		mMapView.showZoomControls(false);// 隐藏地图上的缩放控件
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(zoomLevel).build()));

		mBaiduMap.setMyLocationEnabled(true);// 开启定位图层
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(locScanPan);
		mLocClient.setLocOption(option);
		mLocClient.start();

		locHandler = new Handler();
		locHandler.postDelayed(runnable, freshTime);

		// 消息显示区域
		text_all = (TextView) this.findViewById(R.id.text_all);
		text_group = (TextView) this.findViewById(R.id.text_group);
		scroll_all = (ScrollView) this.findViewById(R.id.scroll_all);
		scroll_group = (ScrollView) this.findViewById(R.id.scroll_group);
		
		//照相
		btn_picture = (Button)this.findViewById(R.id.btn_picture);
		btn_picture.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent_picture = new Intent();
				intent_picture.setClass(MainActivity.this, Activity_Picture.class);
				startActivity(intent_picture);
			}
		});

		// 发送消息
		btn_send = (Button) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendStr = edit_send.getText().toString().trim();
				if (sendStr.equals("")) {// 判断消息不能为空
					Toast toast = Toast.makeText(getApplicationContext(),
							"发送消息不能为空！", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					new Thread() {
						public void run() {
							JSONObject jsonObject1 = new JSONObject();// push_message消息内容
							List<NameValuePair> params = new ArrayList<NameValuePair>();// push_message消息实体
							try {
								if (selectedLevel.equals(sendLevel[0])) {// G：向组长车发送消息
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
										mHandler.sendEmptyMessage(groupSend);
										String resFromServer = HttpUtils
												.PostData(params);
										if (!resFromServer.equals("200")) {
											postStr = "Send Failed";
											mHandler.sendEmptyMessage(groupRecv);
										}
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
										postStr = e2.toString();
										mHandler.sendEmptyMessage(groupRecv);
									} catch (Exception e) {
										// TODO: handle exception
										postStr = e.toString();
										mHandler.sendEmptyMessage(groupRecv);
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
										mHandler.sendEmptyMessage(allSend);
										String resFromServer = HttpUtils
												.PostData(params);
										if (!resFromServer.equals("200")) {
											postStr = "Send Failed"
													+ resFromServer;
											mHandler.sendEmptyMessage(allRecv);
										}
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
										postStr = e2.toString();
										mHandler.sendEmptyMessage(allRecv);
									} catch (Exception e) {
										// TODO: handle exception
										postStr = e.toString();
										mHandler.sendEmptyMessage(allRecv);
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

	/**
	 * 定位其他车辆
	 */
	private Runnable runnable = new Runnable() {
		public void run() {
			new Thread() {
				public void run() {
					if (null == this) { // 走到了onDestory,则不再进行后续消息处理
						return;
					}
					if (MainActivity.this.isFinishing()) { // Activity正在停止，则不再后续处理
						return;
					}
					setLocations(locs);
				};
			}.start();
			locHandler.postDelayed(this, freshTime);
		}

	};

	/**
	 * 界面中消息的显示
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (null == this) { // 走到了onDestory,则不再进行后续消息处理
				return;
			}
			if (MainActivity.this.isFinishing()) { // Activity正在停止，则不再后续处理
				return;
			}
			Date now = new Date();
			SimpleDateFormat df = new SimpleDateFormat("[HH:mm:ss] ");// 设置日期格式
			switch (msg.what) {
			case 1:

				break;
			case groupRecv:// group显示接收消息
				text_group.append(df.format(now) + postStr + "\n");
				scroll2Bottom(scroll_group, text_group);
				break;
			case groupSend:// group显示发送消息
				text_group.append(df.format(now) + "Leader:"
						+ edit_send.getText() + "\n");
				edit_send.setText("");
				scroll2Bottom(scroll_group, text_group);
				break;
			case allRecv:// 广播显示接收消息
				text_all.append(df.format(now) + postStr + "\n");
				scroll2Bottom(scroll_all, text_all);
				break;
			case allSend:// 广播显示发送消息
				text_all.append(df.format(now) + "Leader:"
						+ edit_send.getText() + "\n");
				edit_send.setText("");
				scroll2Bottom(scroll_all, text_all);
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
		vibrator.cancel();

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		if (null != mHandler) {
			mHandler.removeMessages(1);
			mHandler.removeMessages(groupRecv);
			mHandler.removeMessages(groupSend);
			mHandler.removeMessages(allRecv);
			mHandler.removeMessages(allSend);
			mHandler = null;
		}
		if (null != locHandler) {
			locHandler = null;
		}
		PushManager.stopWork(getApplicationContext());
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mBaiduMap.setMyLocationEnabled(true);
		mMapView.onResume();

		// 重新定位
		isFirstLoc = true;
		if (mLocClient == null || !mLocClient.isStarted()) {
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
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
		super.onPause();
	}

	/**
	 * 显示消息
	 */
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		// 仅在来新消息时显示消息
		if (Utils.intentSign) {
			Date now = new Date();
			SimpleDateFormat df = new SimpleDateFormat("[HH:mm:ss] ");// 设置日期格式
			if (Utils.deliverMsg.getMessage_type().equals("location")) {// 定位信息
				String recvLocStr = Utils.deliverMsg.getLocation();
				String[] recvLoc = recvLocStr.split(",");
				// 测试数据
				myLatLng = new LatLng(Double.parseDouble(recvLoc[0]),
						Double.parseDouble(recvLoc[1]));
				
				boolean existSign = false;
				for (int i = 0; i < locs.size(); i++) {
					if (locs.get(i).getName()
							.equals(Utils.deliverMsg.getSrc_id())) {
						locs.get(i).setLocation(myLatLng);
						locs.get(i).setTime(df.format(now));
						existSign = true;
						break;
					}
				}
				if (!existSign) {
					LocationOfCar tmpLocar = new LocationOfCar(
							Utils.deliverMsg.getSrc_id(), df.format(now), myLatLng);
					locs.add(tmpLocar);
				}

			} else if (Utils.deliverMsg.getSrc_tag().equals("")) {
				if (!Utils.logString.equals("")) {// 系统提醒消息 如绑定信息
					text_all.append(df.format(now) + Utils.logString + "\n");
					scroll2Bottom(scroll_all, text_all);
				}
			} else if (Utils.deliverMsg.getSrc_tag().contains("group")) {// 群组消息
				text_group.append(df.format(now)
						+ Utils.deliverMsg.getSrc_tag() + ":" + Utils.logString
						+ "\n");
				scroll2Bottom(scroll_group, text_group);
				
				//震动手机
				long pattern = 300;
				vibrator.vibrate(pattern);
			}
		}
		Utils.intentSign = false;// 确定消息只显示一次
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

			// 将地图的中心设为定位点
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

	/**
	 * 显示Marker组的位置
	 * 
	 * @param locationList
	 */
	public void setLocations(List<LocationOfCar> locationList) {
		if (locationList != null && locationList.size() != 0) {
			// 清除之前的标记
			if (marker != null) {
				for (int i = 0; i < marker.length; i++) {
					marker[i].remove();
				}
			}
			marker = new Marker[locationList.size()];
			LatLng latLngs;
			for (int i = 0; i < locationList.size(); i++) {
				latLngs = locationList.get(i).getLocation();
				OverlayOptions overlayOptions_marker = new MarkerOptions()
						.position(latLngs).icon(bitmapDescriptor);
				marker[i] = (Marker) (mBaiduMap
						.addOverlay(overlayOptions_marker));
			}
		}
	}
}
