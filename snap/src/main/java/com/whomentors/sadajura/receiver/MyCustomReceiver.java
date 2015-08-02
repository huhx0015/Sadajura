package com.whomentors.sadajura.receiver;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyCustomReceiver extends BroadcastReceiver {

  /** CLASS VARIABLES __________________________________________________________________________ **/

  // LOGGING VARIABLES
  private static final String TAG = "MyCustomReceiver";

  // SPEECH VARIABLES
  private TextToSpeech speechInstance; // Used to reference the TTS instance for the class.

  /** BROADCAST RECEIVER METHODS _______________________________________________________________ **/

  @Override
  public void onReceive(Context context, Intent intent) {

    //startSpeech("SA-RA JU-RA ALERT!", context); // Starts TTS speech.

    try {

      String action = intent.getAction();

      if (action.equals("com.whomentors.sadajura.UPDATE_MESSAGES")) {
        Intent newMessage = new Intent("updateMessages");
        LocalBroadcastManager.getInstance(context).sendBroadcast(newMessage);
      }

      else if (action.equals("com.whomentors.sadajura.UPDATE_REQUESTS")) {
        Intent newRequest = new Intent("updateRequests");
        LocalBroadcastManager.getInstance(context).sendBroadcast(newRequest);
      }

      String channel = intent.getExtras().getString("com.parse.Channel");
      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

      Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
      @SuppressWarnings("rawtypes")
      Iterator itr = json.keys();
      while (itr.hasNext()) {
        String key = (String) itr.next();
        Log.d(TAG, "..." + key + " => " + json.getString(key));
      }
    }

    catch (JSONException e) {
      Log.d(TAG, "JSONException: " + e.getMessage());
    }
  }

  /** NARRATION FUNCTIONALITY ________________________________________________________________ **/

  // startSpeech(): Activates voice functionality to say something.
  private void startSpeech(final String script, Context context) {

    // Creates a new instance to begin TextToSpeech functionality.
    speechInstance = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

      @Override
      public void onInit(int status) {
        speechInstance.speak(script, TextToSpeech.QUEUE_FLUSH, null); // The script is spoken by the TTS system.
      }
    });
  }

}
