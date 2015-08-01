package com.developer.appname.android;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.developer.appname.android.TextureVideoView.MediaPlayerListener;

public class ViewVideoActivity extends Activity {
	
	public static final String TAG = ViewVideoActivity.class.getSimpleName();
	
	private int secondsLeft = 0;
	private ImageView closeBtn;
	private ImageView clockBg;
	
    TextureVideoView videoView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        setContentView(R.layout.activity_view_video);
        videoView = (TextureVideoView) findViewById(R.id.videoView);
        
        final String messageTime = getIntent().getExtras().getString("messageTime");
        final TextView countDownText = (TextView)findViewById(R.id.counterTextView);
        clockBg = (ImageView)findViewById(R.id.clockBg);
		
        try {
            Uri video = getIntent().getData();
            String vidUrl = video.toString();
            videoView.setDataSource(vidUrl);
            
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
 
        videoView.requestFocus();
        videoView.setListener(new MediaPlayerListener() {
            // Close the progress bar and play the video
            public void onVideoPrepared() {
            	
            	//setProgressBarIndeterminateVisibility(false);
            	
               	//hideSystemUi();
               	
               	closeBtn = (ImageView) findViewById(R.id.closeBtn);
               	closeBtn.setImageResource(R.drawable.closebtn);
    			
    			closeBtn.setOnClickListener(new View.OnClickListener() {

    	            @Override
    	            public void onClick(View v) {
    	                
    	            	videoView.stop();
    	    			
    	    			finish();
    	            }
    			});

    			if (messageTime.equals("0") || messageTime.equals("puzzle"))
                {
                	clockBg.setImageDrawable(null);
                	
                  	videoView.play();
                }
                else
                {
                	videoView.play();
                	
                	clockBg.setImageResource(R.drawable.clockbg);
                	
                	int secondsToDestruct = Integer.valueOf(videoView.getDuration()); 
                	
                     new CountDownTimer(secondsToDestruct, 100) {
                    	 public void onTick(long ms) {
                    		 if (Math.round((float)ms / 1000.0f) != secondsLeft)
                    		 {  
                    			 secondsLeft = Math.round((float)ms / 1000.0f);
                    			 countDownText.setText("" + secondsLeft);
                    		 }
               	         
                    		 // Log.i("test","ms="+ms+" till finished="+secondsLeft);
                    	 }
               	     
                    	 public void onFinish() {
                    		 countDownText.setText("0");
               	    	 }
                    }.start();
                }
            }
            
            @Override
       		public void onVideoEnd() {
            
            	if (messageTime.equals("0") || messageTime.equals("puzzle"))
                {
                  	videoView.play();
                }
                else
                {
                	finish();
                }
             }
        });
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                
            	//Log.d(TAG, "Device back button pressed.");
            
            	return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}