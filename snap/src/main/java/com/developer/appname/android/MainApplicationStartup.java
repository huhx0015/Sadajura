package com.developer.appname.android;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

public class MainApplicationStartup extends Application {
	
	@Override
	public void onCreate() { 
		super.onCreate();
	    Parse.initialize(this, "YourParseAppID", "YourParseClientKey");
	    
	    PushService.setDefaultPushCallback(this, MainActivity.class);
	}
}
