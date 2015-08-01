package com.developer.appname.android;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

public class MainApplicationStartup extends Application {

	private static final String PARSE_ID = "NNrnhbm9IvWTfoshaJ2kqNJWi3f6dw7dViDdj6xO";
	private static final String PARSE_KEY = "maANGWA1B4xDX54uvZM8ll0ertuQ1eUYR8knIqy8";

	@Override
	public void onCreate() { 
		super.onCreate();
	    Parse.initialize(this, PARSE_ID, PARSE_KEY);
	    
	    PushService.setDefaultPushCallback(this, MainActivity.class);
	}
}
