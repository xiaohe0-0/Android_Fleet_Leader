package com.fleet.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fleet.chat.R;
import com.fleet.chat.R.layout;
import com.fleet.domain.ChatMsgEntity;
import com.fleet.domain.ChatMsgViewAdapter;
import com.fleet.function.SoundMeter;
import com.fleet.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BroadcastActivity extends Activity implements OnClickListener {
	private ImageView chatting_mode_text, chatting_mode_voice,
			chatting_mode_camera, volume;
	private ListView mListView;
	private Button mBtnRcd, mBtnSend;
	private EditText mEditTextContent;
	private Handler mHandler = new Handler();
	private RelativeLayout mBottom;
	private View rcChat_popup;
	private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding,
			voice_rcd_hint_tooshort;
	private ImageView img1;
	private LinearLayout del_re;
	private long startVoiceT, endVoiceT;
	private SoundMeter mSensor;
	private String voiceName;
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
	private ChatMsgViewAdapter mAdapter;
	private int flag = 1;
	private boolean isShosrt = false;
	private static final int POLL_INTERVAL = 300;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_broadcast);
		initView();// 初始化界面
	}

	private void initView() {
		// TODO Auto-generated method stub

		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
		// 录音相关
		rcChat_popup = this.findViewById(R.id.rcChat_popup);
		voice_rcd_hint_rcding = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_rcding);
		voice_rcd_hint_loading = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_loading);
		voice_rcd_hint_tooshort = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_tooshort);
		volume = (ImageView) this.findViewById(R.id.volume);
		mSensor = new SoundMeter();
		mBtnRcd = (Button) findViewById(R.id.btn_rcd);
		mBottom = (RelativeLayout) findViewById(R.id.btn_bottom);
		img1 = (ImageView) this.findViewById(R.id.img1);
		del_re = (LinearLayout) this.findViewById(R.id.del_re);
		chatting_mode_text = (ImageView) this.findViewById(R.id.ivPopUp);
		chatting_mode_text.setOnClickListener(new View.OnClickListener() {

			// 录音->文字
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mBtnRcd.setVisibility(View.GONE);
				mBottom.setVisibility(View.VISIBLE);
			}
		});

		chatting_mode_voice = (ImageView) this.findViewById(R.id.ivVoice);
		chatting_mode_voice.setOnClickListener(new View.OnClickListener() {

			// 文字 ->录音
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mBtnRcd.setVisibility(View.VISIBLE);
				mBottom.setVisibility(View.GONE);
			}
		});

		chatting_mode_camera = (ImageView) this.findViewById(R.id.ivCamera);
		chatting_mode_camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ShowCamera();
			}
		});

		mBtnRcd.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				// 按下语音录制按钮时返回false执行父类OnTouch
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!Environment.getExternalStorageDirectory().exists()) {
						Toast.makeText(getApplicationContext(), "No SDCard",
								Toast.LENGTH_LONG).show();
						return false;
					}

					mBtnRcd.setBackgroundResource(R.drawable.voice_rcd_btn_pressed);
					rcChat_popup.setVisibility(View.VISIBLE);
					voice_rcd_hint_loading.setVisibility(View.VISIBLE);

					// 显示缓冲圈
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					voice_rcd_hint_tooshort.setVisibility(View.GONE);
					// 显示录音界面
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (!isShosrt) {
								voice_rcd_hint_loading.setVisibility(View.GONE);
								voice_rcd_hint_rcding
										.setVisibility(View.VISIBLE);
							}
						}
					}, 200);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					startVoiceT = SystemClock.currentThreadTimeMillis();
					voiceName = startVoiceT + ".amr";
					start(voiceName);
					flag = 2;
				}
				if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {
					mBtnRcd.setBackgroundResource(R.drawable.voice_rcd_btn_nor);
					rcChat_popup.setVisibility(View.GONE);
					flag = 1;
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);

					voice_rcd_hint_rcding.setVisibility(View.GONE);
					stop();
					endVoiceT = SystemClock.currentThreadTimeMillis();
					flag = 1;
					int time = (int) ((endVoiceT - startVoiceT) / 100);// 此处是判断时间是否过短的地方
																		// 有问题
					if (time < 1) {
						isShosrt = true;
						voice_rcd_hint_loading.setVisibility(View.GONE);
						voice_rcd_hint_rcding.setVisibility(View.GONE);
						voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
						mHandler.postDelayed(new Runnable() {
							public void run() {
								voice_rcd_hint_tooshort
										.setVisibility(View.GONE);
								rcChat_popup.setVisibility(View.GONE);
								isShosrt = false;
							}
						}, 500);
						return false;
					}

					UpdateVoice(Utils.SendTitle, voiceName, false, time);
				}

				return false;
			}
		});

		mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
	}

	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = mSensor.getAmplitude();
			updateDisplay(amp);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);

		}
	};

	private void start(String name) {
		mSensor.start(name);
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	// 录音时的动态效果
	private void updateDisplay(double signalEMA) {

		switch ((int) signalEMA) {
		case 0:
		case 1:
			volume.setImageResource(R.drawable.amp1);
			break;
		case 2:
		case 3:
			volume.setImageResource(R.drawable.amp2);

			break;
		case 4:
		case 5:
			volume.setImageResource(R.drawable.amp3);
			break;
		case 6:
		case 7:
			volume.setImageResource(R.drawable.amp4);
			break;
		case 8:
		case 9:
			volume.setImageResource(R.drawable.amp5);
			break;
		case 10:
		case 11:
			volume.setImageResource(R.drawable.amp6);
			break;
		default:
			volume.setImageResource(R.drawable.amp7);
			break;
		}
	}

	private void stop() {
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();
		volume.setImageResource(R.drawable.amp1);
	}

	private Runnable mSleepTask = new Runnable() {
		public void run() {
			stop();
		}
	};

	private String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":"
				+ mins);

		return sbBuffer.toString();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			send();
			break;
		}
	}

	private void send() {
		// TODO Auto-generated method stub
		String contString = mEditTextContent.getText().toString().trim();
		if (contString.length() < 1) {
			Toast.makeText(getApplicationContext(), "发送内容不能为空",
					Toast.LENGTH_LONG).show();
		} else {
			UpdateMsg(Utils.SendTitle, contString, false);
			mEditTextContent.setText("");
		}
	}

	public void showPic(String picName) {
		Intent intent = new Intent(this, ShowPicActivity.class);
		intent.putExtra("picName", picName);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			String picName = data.getStringExtra("picName");
			UpdatePhoto(Utils.SendTitle, picName, false);
			break;

		default:
			break;
		}
	}

	private void ShowCamera() {
		Intent intent = new Intent();
		intent.setClass(this, CameraActivity.class);
		startActivityForResult(intent, 0);
	}

	public void UpdateMsg(String name, String contString, boolean msgType) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.setDate(getDate());
		entity.setName(name);
		entity.setMsgType(msgType);
		entity.setText(contString);

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount() - 1);
	}

	public void UpdateVoice(String name, String voiceName, boolean msgType,
			int time) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.setDate(getDate());
		entity.setName(name);
		entity.setMsgType(msgType);
		entity.setTime(time + "\"");
		entity.setText(voiceName);
		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount() - 1);
		rcChat_popup.setVisibility(View.GONE);
	}

	public void UpdatePhoto(String name, String photoName, boolean msgType) {
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.setDate(getDate());
		entity.setName(name);
		entity.setMsgType(msgType);
		entity.setText(photoName);
		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount() - 1);
	}
}
