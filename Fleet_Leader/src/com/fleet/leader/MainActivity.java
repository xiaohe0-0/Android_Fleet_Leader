package com.fleet.leader;

import java.util.ArrayList;
import java.util.List;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushListener.PushMessageListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.fleet.chat.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private String idPre = "本車身份：";
	private String idName = "Leader";
	
	private TextView text_id;
	private TextView text_all;
	private TextView text_group;
	private Button btn_bind;
	private ScrollView scroll_all;
	private ScrollView scroll_group;
	private MapView mMapView = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext()); 
		setContentView(R.layout.activity_main);
		 //获取地图控件引用  
        mMapView = (MapView) findViewById(R.id.bmapView); 
		text_id = (TextView)this.findViewById(R.id.text_id);
		text_id.setText(idPre + idName);
		
		text_all = (TextView) this.findViewById(R.id.text_all);
		text_group = (TextView) this.findViewById(R.id.text_group);
		scroll_all = (ScrollView) this.findViewById(R.id.scroll_all);
		scroll_group = (ScrollView) this.findViewById(R.id.scroll_group);
		
		btn_bind = (Button) this.findViewById(R.id.btn_bind);
		btn_bind.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				PushManager.startWork(getApplicationContext(),
						PushConstants.LOGIN_TYPE_API_KEY,
						"TPO9PYH8sULRrUuYHyeCqX7e");
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
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume(); 
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();
	}
	
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (Utils.logString != "") {
			text_all.append(Utils.logString + "\n");
			scroll2Bottom(scroll_all, text_all);
		}
	}
	
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
}
