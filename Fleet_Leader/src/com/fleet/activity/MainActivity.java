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

	// ����
	private final String apiKey = "TPO9PYH8sULRrUuYHyeCqX7e";// �ٶ�PUSH��API KEY

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);// ��

		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// ��ӱ�ǩҳ
		Resources res = getResources();
		TabHost tabHost = getTabHost();

		intent_map = new Intent(MainActivity.this, MapActivity.class);
		tabSpec = tabHost.newTabSpec("map"); // ����һ���µı�ǩҳ�����Ϊ��tab1��
		tabSpec.setIndicator("", res.getDrawable(R.drawable.title_map));
		// tabSpec.setIndicator("map",res.getDrawable(R.drawable.ic_launcher));//����tabҳ�����ƺ�ͼ���ʾ
		tabSpec.setContent(intent_map);// ���ô�tab��ת����Activity
		tabHost.addTab(tabSpec);// ����tab���뵽tabHost

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

		tabHost.setCurrentTab(0);// ���õ��ڵ�tabҳ����0��ʼƫ��
		tabHost.setCurrentTab(2);// ����һ��GroupActivity ����Ĭ�Ͻ��ղ�������
		tabHost.setCurrentTab(0);

		// �󶨰ٶ����ͷ���
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
	 * ��ʾ��Ϣ
	 */
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		// ����������Ϣʱ��ʾ��Ϣ
		if (Utils.intentSign) {
			if (Utils.deliverMsg.getMessage_type().equals("location")) {// ��λ��Ϣ

			} else if (Utils.deliverMsg.getSrc_tag().equals("")) {
				if (!Utils.logString.equals("")) {// ϵͳ������Ϣ �����Ϣ

					Toast.makeText(getApplicationContext(), Utils.logString,
							Toast.LENGTH_LONG).show();
				}
			} else if (Utils.deliverMsg.getSrc_tag().contains("group")) {// Ⱥ����Ϣ

				if (Utils.mhHandler_group != null) {
					// ���ֻ�
					long pattern = 300;
					vibrator.vibrate(pattern);
					Utils.mhHandler_group.sendEmptyMessage(0);
				} else {
					Toast.makeText(getApplicationContext(), "No Handler",
							Toast.LENGTH_LONG).show();
				}
			}

		}
		Utils.intentSign = false;// ȷ����Ϣֻ��ʾһ��
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
