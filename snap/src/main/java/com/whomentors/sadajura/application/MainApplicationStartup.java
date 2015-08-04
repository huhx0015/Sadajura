package com.whomentors.sadajura.application;

import android.app.Application;

import com.whomentors.sadajura.activities.SJMainActivity;
import com.parse.Parse;
import com.parse.PushService;

public class MainApplicationStartup extends Application {

	private static final String PARSE_ID = "NaN";
	private static final String PARSE_KEY = "NaN";

	@Override
	public void onCreate() { 
		super.onCreate();
	    Parse.initialize(this, PARSE_ID, PARSE_KEY);
	    
	    PushService.setDefaultPushCallback(this, SJMainActivity.class);
	}
}
