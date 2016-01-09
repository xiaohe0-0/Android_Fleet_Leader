package com.fleet.activity;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fleet.utils.*;
import com.fleet.chat.R;
import com.fleet.chat.R.layout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.widget.ImageView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class CameraActivity extends Activity {
	private SurfaceView sView;
	private Bitmap bm;
	private SurfaceHolder surfaceHolder;
	private int screenWidth, screenHeight;
	private ProgressDialog dialog;
	// ����ϵͳ���õ������
	private Camera camera;
	// �Ƿ���Ԥ����
	private boolean isPreview = false;
	private String infoStr = "";
	private Intent intent;
	private String picName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		intent = getIntent();

		// ��ȡ���ڹ�����
		WindowManager wm = getWindowManager();
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		// ��ȡ��Ļ�Ŀ�͸�
		display.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// ��ȡ������SurfaceView���
		sView = (SurfaceView) findViewById(R.id.sView);
		// ���ø�Surface����Ҫ�Լ�ά��������
		// sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// ���SurfaceView��SurfaceHolder
		surfaceHolder = sView.getHolder();

		// ΪsurfaceHolder���һ���ص�������
		surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// ������ͷ
				initCamera();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// ���camera��Ϊnull ,�ͷ�����ͷ
				if (camera != null) {
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
			}
		});
	}

	private void initCamera() {
		if (!isPreview) {
			// �˴�Ĭ�ϴ򿪺�������ͷ��
			// ͨ������������Դ�ǰ������ͷ
			camera = Camera.open(0); // ��
			camera.setDisplayOrientation(90);
		}
		if (camera != null && !isPreview) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				// ����Ԥ����Ƭ�Ĵ�С
				parameters.setPreviewSize(screenWidth, screenHeight);
				// ����Ԥ����Ƭʱÿ����ʾ����֡����Сֵ�����ֵ
				parameters.setPreviewFpsRange(4, 10);
				// ����ͼƬ��ʽ
				parameters.setPictureFormat(ImageFormat.JPEG);
				// ����JPG��Ƭ������
				parameters.set("jpeg-quality", 85);
				// ������Ƭ�Ĵ�С
				parameters.setPictureSize(screenWidth, screenHeight);
				// ͨ��SurfaceView��ʾȡ������
				camera.setPreviewDisplay(surfaceHolder); // ��
				// ��ʼԤ��
				camera.startPreview(); // ��
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}

	// Button pressed
	public void capture(View source) {
		try {
			if (camera != null) {
				// ��������ͷ�Զ��Խ��������
				camera.autoFocus(autoFocusCallback); // ��
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		// ���Զ��Խ�ʱ�����÷���
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				// takePicture()������Ҫ����3������������
				// ��1�������������û����¿���ʱ�����ü�����
				// ��2�����������������ȡԭʼ��Ƭʱ�����ü�����
				// ��3�����������������ȡJPG��Ƭʱ�����ü�����
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						// ���¿���˲���ִ�д˴�����
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera c) {
						// �˴�������Ծ����Ƿ���Ҫ����ԭʼ��Ƭ��Ϣ
					}
				}, myJpegCallback); // ��
			}
		}
	};

	PictureCallback myJpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// �����������õ����ݴ���λͼ
			bm = BitmapFactory.decodeByteArray(data, 0, data.length);

			// ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
			bm = ImageUtil.getRotateBitmap(bm, 90.0f);

			// ����/layout/save.xml�ļ���Ӧ�Ĳ�����Դ
			View saveDialog = getLayoutInflater().inflate(R.layout.uploadpic,
					null);

			// ��ȡsaveDialog�Ի����ϵ�ImageView���
			ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
			// ��ʾ�ո��ĵõ���Ƭ
			show.setImageBitmap(bm);
			// ʹ�öԻ�����ʾsaveDialog���

			new AlertDialog.Builder(CameraActivity.this).setView(saveDialog)
					.setPositiveButton("����", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							picName = FileUtil.saveBitmap(bm);
							post();
							intent.putExtra("picName", picName);
							CameraActivity.this.setResult(RESULT_OK, intent);
							finish();
						}
					}).setNegativeButton("ȡ��", null).show();
			// �������
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(CameraActivity.this, "�ϴ��ɹ���", Toast.LENGTH_LONG)
						.show();
				break;

			case 2:
				Toast.makeText(CameraActivity.this, "�ϴ�ʧ�ܣ�" + infoStr,
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

	private void post() {
		dialog = new ProgressDialog(CameraActivity.this);
		dialog.setMessage("�ļ������ϴ��У�����ȴ�...");
		dialog.setCancelable(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(false);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

		new Thread() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				try {
					HttpPost httppost = new HttpPost(Utils.upload_pic_ip);
					MultipartEntity entity = new MultipartEntity();
					// �ϴ�λͼ��������
					Bitmap bmpCompressed = Bitmap.createScaledBitmap(bm, 640,
							480, true);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();

					// CompressFormat set up to JPG, you can change to PNG or
					// whatever you want;

					bmpCompressed.compress(CompressFormat.JPEG, 100, bos);
					byte[] data = bos.toByteArray();
					// sending a Image;
					// note here, that you can send more than one image, just
					// add another param, same rule to the String;

					entity.addPart("picture", new ByteArrayBody(data,
							"temp.jpg"));
					// entity.addPart("longitude", new StringBody(lng));
					// entity.addPart("latitude", new StringBody(lat));
					// entity.addPart("tag", new StringBody(search));
					httppost.setEntity(entity);
					HttpResponse response = httpclient.execute(httppost);
					if (response.getStatusLine().getStatusCode() == 200) {
						dialog.dismiss();
						String result = EntityUtils.toString(response
								.getEntity());
						// Looper.prepare();
						// JSONObject resultObject = new JSONObject(result);

						// String name = resultObject.getString("name");
						// String content = resultObject.getString("content");
						// String url = resultObject.getString("url");
						// int tag=resultObject.getInt("tag");
						mHandler.sendEmptyMessage(1);

						// Looper.loop();
					} else {
						dialog.dismiss();
						infoStr = response.getStatusLine().getStatusCode() + "";
						mHandler.sendEmptyMessage(2);
					}

				} catch (Exception e) {

					e.printStackTrace();
				}
			};
		}.start();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Release the memory where bitmap occupied
		if (bm != null && !bm.isRecycled()) {
			bm.recycle();
			bm = null;
		}
		System.gc();
	}
}
