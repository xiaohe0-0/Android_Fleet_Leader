package com.fleet.activity;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TabHost;
import com.fleet.chat.*;

public class MainActivity extends TabActivity {
	/** Called when the activity is first created. */
	Intent intent;
	TabHost.TabSpec tabSpec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

		intent = new Intent(MainActivity.this, MapActivity.class);
		tabSpec = tabHost.newTabSpec("map"); // 创建一个新的标签页，标记为“tab1”
		tabSpec.setIndicator("",res.getDrawable(R.drawable.title_map));
		// tabSpec.setIndicator("map",res.getDrawable(R.drawable.ic_launcher));//设置tab页的名称和图像表示
		tabSpec.setContent(intent);// 设置此tab跳转到的Activity
		tabHost.addTab(tabSpec);// 将此tab加入到tabHost

		intent = new Intent(MainActivity.this, BroadcastActivity.class);
		tabSpec = tabHost.newTabSpec("Broad");
		tabSpec.setIndicator("",res.getDrawable(R.drawable.title_broadcast));
		// tabSpec.setIndicator("Broad",
		// res.getDrawable(R.drawable.ic_launcher));
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);

		intent = new Intent(MainActivity.this, GroupActivity.class);
		tabSpec = tabHost.newTabSpec("Group");
		tabSpec.setIndicator("",res.getDrawable(R.drawable.title_group));
		// tabSpec.setIndicator("Group",
		// res.getDrawable(R.drawable.ic_launcher));
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);

		tabHost.setCurrentTab(0);// 设置当期的tab页，从0开始偏移
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
