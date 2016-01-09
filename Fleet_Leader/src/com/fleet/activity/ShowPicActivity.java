package com.fleet.activity;

import com.fleet.chat.R;
import com.fleet.chat.R.id;
import com.fleet.chat.R.layout;
import com.fleet.chat.R.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class ShowPicActivity extends Activity {
	private ImageView showPic;
	private Bitmap bm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_pic);
		Intent intent = getIntent();
		String picName = intent.getStringExtra("picName");

		showPic = (ImageView) this.findViewById(R.id.iv_showpic);
		bm = BitmapFactory.decodeFile(picName);
		showPic.setImageBitmap(bm);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_pic, menu);
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
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//Release the memory where bitmap occupied
		if (bm != null && !bm.isRecycled()) {
			bm.recycle();
			bm = null;
		}
		System.gc();

	}
}
