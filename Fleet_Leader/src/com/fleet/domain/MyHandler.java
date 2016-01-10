package com.fleet.domain;

import android.app.Application;
import android.os.Handler;

public class MyHandler extends Application{
	private Handler group_handler;
	
	public MyHandler() {
		// TODO Auto-generated constructor stub
		group_handler = new Handler();
	}

	public Handler getGroup_handler() {
		return group_handler;
	}

	public void setGroup_handler(Handler group_handler) {
		this.group_handler = group_handler;
	}
}
