package com.developer.appname.android;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
	
	private WebView webView;
	private String webViewUrl;
	private String actionBarTitle;
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
 
		actionBarTitle = getIntent().getExtras().getString("navTitle");
		webViewUrl = getIntent().getExtras().getString("webUrl");

		ActionBar ab = getActionBar();
		ab.setTitle(actionBarTitle);
		
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(webViewUrl);
	}
}
