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
	// 定义系统所用的照相机
	private Camera camera;
	// 是否在预览中
	private boolean isPreview = false;
	private String infoStr = "";
	private Intent intent;
	private String picName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		intent = getIntent();

		// 获取窗口管理器
		WindowManager wm = getWindowManager();
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		// 获取屏幕的宽和高
		display.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// 获取界面中SurfaceView组件
		sView = (SurfaceView) findViewById(R.id.sView);
		// 设置该Surface不需要自己维护缓冲区
		// sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 获得SurfaceView的SurfaceHolder
		surfaceHolder = sView.getHolder();

		// 为surfaceHolder添加一个回调监听器
		surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// 打开摄像头
				initCamera();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// 如果camera不为null ,释放摄像头
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
			// 此处默认打开后置摄像头。
			// 通过传入参数可以打开前置摄像头
			camera = Camera.open(0); // ①
			camera.setDisplayOrientation(90);
		}
		if (camera != null && !isPreview) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				// 设置预览照片的大小
				parameters.setPreviewSize(screenWidth, screenHeight);
				// 设置预览照片时每秒显示多少帧的最小值和最大值
				parameters.setPreviewFpsRange(4, 10);
				// 设置图片格式
				parameters.setPictureFormat(ImageFormat.JPEG);
				// 设置JPG照片的质量
				parameters.set("jpeg-quality", 85);
				// 设置照片的大小
				parameters.setPictureSize(screenWidth, screenHeight);
				// 通过SurfaceView显示取景画面
				camera.setPreviewDisplay(surfaceHolder); // ②
				// 开始预览
				camera.startPreview(); // ③
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
				// 控制摄像头自动对焦后才拍照
				camera.autoFocus(autoFocusCallback); // ④
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		// 当自动对焦时激发该方法
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				// takePicture()方法需要传入3个监听器参数
				// 第1个监听器：当用户按下快门时激发该监听器
				// 第2个监听器：当相机获取原始照片时激发该监听器
				// 第3个监听器：当相机获取JPG照片时激发该监听器
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						// 按下快门瞬间会执行此处代码
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera c) {
						// 此处代码可以决定是否需要保存原始照片信息
					}
				}, myJpegCallback); // ⑤
			}
		}
	};

	PictureCallback myJpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// 根据拍照所得的数据创建位图
			bm = BitmapFactory.decodeByteArray(data, 0, data.length);

			// 图片竟然不能旋转了，故这里要旋转下
			bm = ImageUtil.getRotateBitmap(bm, 90.0f);

			// 加载/layout/save.xml文件对应的布局资源
			View saveDialog = getLayoutInflater().inflate(R.layout.uploadpic,
					null);

			// 获取saveDialog对话框上的ImageView组件
			ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
			// 显示刚刚拍得的照片
			show.setImageBitmap(bm);
			// 使用对话框显示saveDialog组件

			new AlertDialog.Builder(CameraActivity.this).setView(saveDialog)
					.setPositiveButton("发送", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							picName = FileUtil.saveBitmap(bm);
							post();
							intent.putExtra("picName", picName);
							CameraActivity.this.setResult(RESULT_OK, intent);
							finish();
						}
					}).setNegativeButton("取消", null).show();
			// 重新浏览
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(CameraActivity.this, "上传成功！", Toast.LENGTH_LONG)
						.show();
				break;

			case 2:
				Toast.makeText(CameraActivity.this, "上传失败！" + infoStr,
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

	private void post() {
		dialog = new ProgressDialog(CameraActivity.this);
		dialog.setMessage("文件正在上传中，敬请等待...");
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
					// 上传位图到服务器
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
