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
	// ����
	private final int freshTime = 1000;// ��ͼˢ��ʱ��
	private final int locScanPan = 2000;// ��λˢ��ʱ��
	private final int zoomLevel = 20;// Baidu map zoom level

	// ����
	private String postStr;
	private String selectedLevel;
	private String sendStr;
	private LatLng myLatLng;
	private List<LocationOfCar> locs = null;
	private Handler locHandler;
	private Vibrator vibrator;

	// �ٶȵ�ͼ
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private Marker marker[] = null;
	private BitmapDescriptor bitmapDescriptor;

	// ��λ���
	private LocationClient mLocClient = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	private boolean isFirstLoc = true;// �Ƿ��״ζ�λ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_map);

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);

		locs = new ArrayList<LocationOfCar>();// λ������
		bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_member);// ͼ��

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.showScaleControl(true);// ���ص�ͼ�ϵı�����
		mMapView.showZoomControls(false);// ���ص�ͼ�ϵ����ſؼ�
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(zoomLevel).build()));

		mBaiduMap.setMyLocationEnabled(true);// ������λͼ��
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(locScanPan);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		//����λ�ö�λˢ��
		locHandler = new Handler();
		locHandler.post(runnable);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();
		if (null != locHandler) {
			locHandler = null;
		}
	}

	@Override
	protected void onResume() {
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mBaiduMap.setMyLocationEnabled(true);
		mMapView.onResume();

		// ���¶�λ
		isFirstLoc = true;
		if (mLocClient == null || !mLocClient.isStarted()) {
			// ������λͼ��
			mBaiduMap.setMyLocationEnabled(true);
			// ��λ��ʼ��
			mLocClient = new LocationClient(this);
			mLocClient.registerLocationListener(myListener);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);// ��gps
			option.setCoorType("bd09ll"); // ������������
			option.setScanSpan(2000);
			mLocClient.setLocOption(option);
			mLocClient.start();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		// �ر�ͼ�㶨λ
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
	 * ��λ��������
	 */
	private Runnable runnable = new Runnable() {
		public void run() {
			new Thread() {
				public void run() {
					if (null == this) { // �ߵ���onDestory,���ٽ��к�����Ϣ����
						return;
					}
					if (MapActivity.this.isFinishing()) { // Activity����ֹͣ�����ٺ�������
						return;
					}
					setLocations(locs);
				};
			}.start();
			locHandler.postDelayed(this, freshTime);
		}

	};

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
					// .accuracy(location.getRadius())
					.accuracy(0)
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);

			// ����ͼ��������Ϊ��λ��
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
	 * ��ʾMarker���λ��
	 * 
	 * @param locationList
	 */
	public void setLocations(List<LocationOfCar> locationList) {
		if (locationList != null && locationList.size() != 0) {
			// ���֮ǰ�ı��
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
