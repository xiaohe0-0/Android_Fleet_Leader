package com.fleet.domain;

import java.io.File;
import java.util.List;

import com.fleet.activity.BroadcastActivity;
import com.fleet.activity.GroupActivity;
import com.fleet.chat.R;
import com.fleet.utils.Utils;

import android.R.integer;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ChatMsgViewAdapter extends BaseAdapter {
	private final int SIGN_GROUP = 0;
	private final int SIGN_BROAD = 1;
	private int sign_type;

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();

	private List<ChatMsgEntity> coll;

	//private Context ctx;
	private GroupActivity groupContext;
	private BroadcastActivity broadContext;
	
	private LayoutInflater mInflater;
	private MediaPlayer mMediaPlayer = new MediaPlayer();

	public ChatMsgViewAdapter(GroupActivity context, List<ChatMsgEntity> coll) {
		//ctx = context;
		this.groupContext = context;
		this.coll = coll;
		sign_type = SIGN_GROUP;
		mInflater = LayoutInflater.from(context);
	}

	public ChatMsgViewAdapter(BroadcastActivity context,
			List<ChatMsgEntity> coll) {
		// TODO Auto-generated constructor stub
		this.broadContext = context;
		this.coll = coll;
		sign_type = SIGN_BROAD;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		ChatMsgEntity entity = coll.get(position);

		if (entity.getMsgType()) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		final ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.ivContent = (ImageView) convertView
					.findViewById(R.id.tv_imgcontent);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.getDate());

		if (entity.getText().contains(".amr")) {
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.ivContent.setVisibility(View.GONE);
			viewHolder.tvContent.setText("");
			viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.chatto_voice_playing, 0);
			viewHolder.tvTime.setText(entity.getTime());
		} else if (entity.getText().contains(".jpg")) {
			String picturePath = Utils.savePath_pic + entity.getText();
			File file = new File(picturePath);
			if (file.exists()) {
				viewHolder.tvContent.setVisibility(View.GONE);
				viewHolder.ivContent.setVisibility(View.VISIBLE);
				
				BitmapFactory.Options opts = new BitmapFactory.Options();   //压缩，用于节省BITMAP内存空间--解决BUG的关键步骤    
				opts.inSampleSize = 4;    //这个的值压缩的倍数（2的整数倍），数值越小，压缩率越小，图片越清晰    
				
				viewHolder.ivContent.setImageBitmap(BitmapFactory
						.decodeFile(picturePath,opts));
			}

		} else {
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.ivContent.setVisibility(View.GONE);
			viewHolder.tvContent.setText(entity.getText());
			viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					0, 0);
			viewHolder.tvTime.setText("");
		}
		viewHolder.tvContent.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (entity.getText().contains(".amr")) {
					playMusic(Utils.savePath_voice + entity.getText());
				}
				
			}
		});
		viewHolder.ivContent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(entity.getText().contains(".jpg")){
					showPicture(Utils.savePath_pic+entity.getText());
				}
			}
		});
		viewHolder.tvUserName.setText(entity.getName());

		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public TextView tvTime;
		public ImageView ivContent;
		public ImageView ivShow;
		public boolean isComMsg = true;
	}

	/**
	 * @Description
	 * @param name
	 */
	private void playMusic(String name) {
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void showPicture(String fileName) {
		switch (sign_type) {
		case SIGN_GROUP:
			groupContext.showPic(fileName);
			break;
		case SIGN_BROAD:
			broadContext.showPic(fileName);
			break;
		default:
			break;
		}
		
	}
	

	private void stop() {

	}

}
