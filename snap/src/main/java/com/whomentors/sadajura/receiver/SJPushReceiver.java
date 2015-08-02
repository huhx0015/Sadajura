package com.whomentors.sadajura.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */
public class SJPushReceiver extends ParsePushBroadcastReceiver {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    public static final String LOG_TAG = SJPushReceiver.class.getSimpleName();

    /** PUSH RECEIVER METHODS __________________________________________________________________ **/

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
        super.onPushReceive(mContext, intent);

        Log.d(LOG_TAG, "onPushReceive: Parse push notification received.");

        Intent i = new Intent("com.whomentors.sadajura.VOICEACTIVITY");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i); // Launches the SJMainActivity intent.
    }

}