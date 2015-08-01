package com.whomentors.sadajura;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Michael Yoon Huh on 8/1/2015.
 */
public class SJMainActivity extends FragmentActivity {

    /** LIFECYCLE METHODS ______________________________________________________________________ **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void setUpLayout() {
        setContentView(R.layout.main_activity); // Sets the XML layout file for the activity.
    }

}
