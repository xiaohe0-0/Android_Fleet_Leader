package com.fleet.activity;

import java.util.ArrayList;
import java.util.List;

import com.fleet.domain.*;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
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
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.fleet.chat.R;
import com.fleet.chat.R.id;
import com.fleet.chat.R.layout;
import com.fleet.chat.R.menu;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity extends ActionBarActivity {
	// 常参
	private final int freshTime = 1000;// 地图刷新时间
	private final int locScanPan = 2000;// 定位刷新时间
	private final int zoomLevel = 20;// Baidu map zoom level

	// 变量
	private String postStr;
	private String selectedLevel;
	private String sendStr;
	private LatLng myLatLng;
	private List<LocationOfCar> locs = null;
	private Handler locHandler;
	private Vibrator vibrator;

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

		setContentView(R.layout.activity_map);

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);

		locs = new ArrayList<LocationOfCar>();// 位置数组
		bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_member);// 图标

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
		
		//其他位置定位刷新
		locHandler = new Handler();
		locHandler.post(runnable);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		if (null != locHandler) {
			locHandler = null;
		}
	}

	@Override
	protected void onResume() {
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
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocClient.stop();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
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
					if (MapActivity.this.isFinishing()) { // Activity正在停止，则不再后续处理
						return;
					}
					setLocations(locs);
				};
			}.start();
			locHandler.postDelayed(this, freshTime);
		}

	};

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
					// .accuracy(location.getRadius())
					.accuracy(0)
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
