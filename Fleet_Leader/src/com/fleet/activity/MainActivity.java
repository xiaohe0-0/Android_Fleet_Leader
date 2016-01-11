package com.fleet.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mapapi.SDKInitializer;
import com.fleet.chat.*;
import com.fleet.utils.Utils;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	/** Called when the activity is first created. */
	private Intent intent_map, intent_group, intent_broad;
	private TabHost.TabSpec tabSpec;
	private Vibrator vibrator;

	// 常参
	private final String apiKey = "TPO9PYH8sULRrUuYHyeCqX7e";// 百度PUSH的API KEY

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);// 震动

		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// 添加标签页
		Resources res = getResources();
		TabHost tabHost = getTabHost();

		intent_map = new Intent(MainActivity.this, MapActivity.class);
		tabSpec = tabHost.newTabSpec("map"); // 创建一个新的标签页，标记为“tab1”
		tabSpec.setIndicator("", res.getDrawable(R.drawable.title_map));
		// tabSpec.setIndicator("map",res.getDrawable(R.drawable.ic_launcher));//设置tab页的名称和图像表示
		tabSpec.setContent(intent_map);// 设置此tab跳转到的Activity
		tabHost.addTab(tabSpec);// 将此tab加入到tabHost

		intent_broad = new Intent(MainActivity.this, BroadcastActivity.class);
		tabSpec = tabHost.newTabSpec("Broad");
		tabSpec.setIndicator("", res.getDrawable(R.drawable.title_broadcast));
		// tabSpec.setIndicator("Broad",
		// res.getDrawable(R.drawable.ic_launcher));
		tabSpec.setContent(intent_broad);
		tabHost.addTab(tabSpec);

		intent_group = new Intent(MainActivity.this, GroupActivity.class);
		tabSpec = tabHost.newTabSpec("Group");
		tabSpec.setIndicator("", res.getDrawable(R.drawable.title_group));
		// tabSpec.setIndicator("Group",
		// res.getDrawable(R.drawable.ic_launcher));
		tabSpec.setContent(intent_group);
		tabHost.addTab(tabSpec);

		tabHost.setCurrentTab(0);// 设置当期的tab页，从0开始偏移
		tabHost.setCurrentTab(2);// 激活一下GroupActivity 否则默认接收不到数据
		tabHost.setCurrentTab(0);

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
	 * 显示消息
	 */
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		// 仅在来新消息时显示消息
		if (Utils.intentSign) {
			if (Utils.deliverMsg.getMessage_type().equals("location")) {// 定位信息

			} else if (Utils.deliverMsg.getSrc_tag().equals("")) {
				if (!Utils.logString.equals("")) {// 系统提醒消息 如绑定信息

					Toast.makeText(getApplicationContext(), Utils.logString,
							Toast.LENGTH_LONG).show();
				}
			} else if (Utils.deliverMsg.getSrc_tag().contains("group")) {// 群组消息

				if (Utils.mhHandler_group != null) {
					// 震动手机
					long pattern = 300;
					vibrator.vibrate(pattern);
					Utils.mhHandler_group.sendEmptyMessage(0);
				} else {
					Toast.makeText(getApplicationContext(), "No Handler",
							Toast.LENGTH_LONG).show();
				}
			}

		}
		Utils.intentSign = false;// 确定消息只显示一次
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		vibrator.cancel();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		PushManager.stopWork(getApplicationContext());
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
}
