package com.fleet.activity;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.fleet.function.*;
import com.fleet.utils.HttpUtils;
import com.fleet.utils.ImageUtil;
import com.fleet.utils.Utils;
import com.fleet.utils.FileUtil;
import com.fleet.chat.R;
import com.fleet.domain.*;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;

public class GroupActivity extends Activity implements OnClickListener {

	// 控件
	private ImageView chatting_mode_text, chatting_mode_voice,
			chatting_mode_camera, volume;
	private ListView mListView;
	private Button mBtnRcd, mBtnSend;
	private EditText mEditTextContent;
	private RelativeLayout mBottom;
	private View rcChat_popup;
	private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding,
			voice_rcd_hint_tooshort;
	private ImageView img1;
	private LinearLayout del_re;

	// 变量
	private Handler mHandler = new Handler();
	private Handler mHandler_send;
	private Handler pic_hdl, voice_hdl;
	private long startVoiceT, endVoiceT;
	private SoundMeter mSensor;
	private String voiceName;
	private String getVoiceName;
	private int voice_time;
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
	private ChatMsgViewAdapter mAdapter;
	private int flag = 1;
	private boolean isShosrt = false;
	private String contString;
	private String postStr;

	// 常量
	private static final int POLL_INTERVAL = 300;
	private final String msgPre = "push_message";// 解析接收到的消息时的前缀

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);

		initView();// 初始化界面
		initData();// 初始化数据
	}

	private void initData() {
		// TODO Auto-generated method stub
		pic_hdl = new PicHandler();
		voice_hdl = new VoiceHandler();
		Utils.mhHandler_group = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					Toast.makeText(
							getApplicationContext(),
							"Receive Msg From " + Utils.deliverMsg.getSrc_tag(),
							Toast.LENGTH_LONG).show();
					if (Utils.deliverMsg.getMessage_type().equals("text")) {
						UpdateMsg(Utils.deliverMsg.getSrc_tag(),
								Utils.deliverMsg.getContent(), true);
					} else if (Utils.deliverMsg.getMessage_type().equals(
							"picture")) {
						Toast.makeText(getApplicationContext(),
								Utils.deliverMsg.getContent(),
								Toast.LENGTH_LONG).show();
						new Thread() {
							public void run() {
								Bitmap img = ImageUtil
										.getUrlImage(Utils.deliverMsg
												.getContent());
								Message msg = pic_hdl.obtainMessage();
								msg.what = 0;
								msg.obj = img;
								pic_hdl.sendMessage(msg);
							};
						}.start();
					} else if (Utils.deliverMsg.getMessage_type().equals(
							"voice")) {
						Toast.makeText(getApplicationContext(),
								Utils.deliverMsg.getContent(),
								Toast.LENGTH_LONG).show();
						new Thread() {
							public void run() {
								InputStream is = FileUtil
										.getUrlVoice(Utils.deliverMsg
												.getContent());
								//getVoiceName = FileUtil.saveVoice(is);
								Message msg = voice_hdl.obtainMessage();
								msg.what = 0;
								// msg.obj = is;
								voice_hdl.sendMessage(msg);
							};
						}.start();

					}
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

		mHandler_send = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					Toast.makeText(getApplicationContext(), postStr,
							Toast.LENGTH_LONG).show();
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	private void initView() {
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
					voice_time = time;

					UpdateVoice(Utils.SendTitle, voiceName, false, voice_time);
					new Thread() {
						public void run() {
							HttpClient httpclient = new DefaultHttpClient();
							try {

								HttpPost httppost = new HttpPost(
										Utils.upload_voice_ip);

								MultipartEntity entity = new MultipartEntity();
								// 文件流读取文件
								FileInputStream fin = new FileInputStream(
										Utils.savePath_voice + voiceName);
								// 获得字符长度
								int length = fin.available();
								// 创建字节数组
								byte[] data = new byte[length];
								// 把字节流读入数组中
								fin.read(data);
								// 关闭文件流
								fin.close();

								entity.addPart("content", new ByteArrayBody(
										data, "temp.amr"));
								entity.addPart("message_type", new StringBody(
										"voice"));
								entity.addPart("src_tag", new StringBody(
										Utils.MyTag));
								entity.addPart("src_id", new StringBody(
										Utils.MyUserID));
								entity.addPart("user_id", new StringBody(
										Utils.MyUserID));
								entity.addPart("attr", new StringBody("common"));
								entity.addPart("location", new StringBody(
										voice_time + ""));
								entity.addPart("push_type", new StringBody("2"));
								entity.addPart("tag_name", new StringBody(
										"group2"));

								httppost.setEntity(entity);
								HttpResponse response = httpclient
										.execute(httppost);
								postStr = response.getStatusLine()
										.getStatusCode() + "";
								mHandler_send.sendEmptyMessage(0);

								postStr = Utils.upload_voice_ip;
								mHandler_send.sendEmptyMessage(0);

								if (response.getStatusLine().getStatusCode() != 200) {
									postStr = response.getStatusLine()
											.getStatusCode() + "";
									mHandler_send.sendEmptyMessage(0);
								}

							} catch (Exception e) {

								e.printStackTrace();
							}
						};
					}.start();
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

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
		contString = mEditTextContent.getText().toString().trim();
		if (contString.length() < 1) {
			Toast.makeText(getApplicationContext(), "发送内容不能为空",
					Toast.LENGTH_LONG).show();
		} else {
			UpdateMsg(Utils.SendTitle, contString, false);
			mEditTextContent.setText("");

			new Thread() {
				public void run() {
					JSONObject jsonObject1 = new JSONObject();// push_message消息内容
					List<NameValuePair> params = new ArrayList<NameValuePair>();// push_message消息实体

					// 添加消息内容
					try {
						jsonObject1.put("message_type", "text");
						jsonObject1.put("src_tag", Utils.MyTag);
						jsonObject1.put("src_id", Utils.MyChannelId);
						jsonObject1.put("attr", "common");
						jsonObject1.put("location", "");
						jsonObject1.put("push_type", "2");
						jsonObject1.put("tag_name", "group" + "2");
						jsonObject1.put("content", contString);
						jsonObject1.put("user_id", Utils.MyUserID);
						params.add(new BasicNameValuePair(msgPre, jsonObject1
								.toString()));// 封装消息实体
						String resFromServer = HttpUtils.PostData(params);
						if (!resFromServer.equals("200")) {
							postStr = "Send Failed";
							mHandler_send.sendEmptyMessage(0);

						} else {
							postStr = resFromServer;
							mHandler_send.sendEmptyMessage(0);
						}
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						postStr = e2.toString();
						mHandler_send.sendEmptyMessage(0);
					} catch (Exception e) {
						// TODO: handle exception
						postStr = e.toString();
						mHandler_send.sendEmptyMessage(0);
					}
				};
			}.start();
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
		intent.putExtra("activityfrom", "group");
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

	class PicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// String s = (String)msg.obj;
			// ptv.setText(s);
			Bitmap myimg = (Bitmap) msg.obj;
			if (myimg != null) {
				String photoName = FileUtil.saveBitmap(myimg);
				UpdatePhoto(Utils.deliverMsg.getSrc_tag(), photoName, true);
			}
		}
	}

	class VoiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			try {
				UpdateVoice(Utils.deliverMsg.getSrc_tag(), getVoiceName, true,
						Integer.parseInt(Utils.deliverMsg.getLocation()));
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group, menu);
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
