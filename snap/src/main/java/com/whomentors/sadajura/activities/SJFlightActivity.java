package com.whomentors.sadajura.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.whomentors.sadajura.data.SJPreferences;
import com.whomentors.sadajura.ui.view.SJSpinner;
import com.whomentors.sadajura.ui.view.SJUnbind;
import com.whomentors.sarajura.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */

public class SJFlightActivity extends Activity implements AdapterView.OnItemSelectedListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOCATION VARIABLES
    private String origin = "United States of America";
    private String destination = "United States of America";

    // PREFERENCE VARIABLES
    private SharedPreferences SJ_prefs;
    private static final String SJ_OPTIONS = "sj_options";

    // VIEW INJECTION VARIABLES
    @Bind(R.id.sj_flight_destination_spinner) Spinner destSpinner;
    @Bind(R.id.sj_flight_origin_spinner) Spinner originSpinner;

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only
    // runs when the activity is first started.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLayout(); // Sets up the layout for the activity.
        setUpActionBar(); // Sets up the action bar for the activity.
        setUpSpinner(); // Sets up the spinner for the activity.
    }

    // onStop(): This function runs when screen is no longer visible and the activity is in a
    // state prior to destruction.
    @Override
    protected void onStop() {
        super.onStop();
        finish(); // The activity is terminated at this point.
    }

    // onDestroy(): This function runs when the activity has terminated and is being destroyed.
    // Calls recycleMemory() to free up memory allocation.
    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Recycles all View objects to free up memory resources.
        SJUnbind.recycleMemory(findViewById(R.id.sj_flight_activity_layout));
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the activity.
    private void setUpLayout() {
        setContentView(R.layout.sj_flight_activity); // Sets the XML layout file.
        ButterKnife.bind(this); // ButterKnife view injection initialization.
        loadPreferences(); // Loads the preference values.
    }

    // setUpActionBar(): Sets up the action bar attributes.
    private void setUpActionBar() {

        // Sets up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false); // Disables the home icon.
        actionBar.setDisplayUseLogoEnabled(false); // Disables the display of the logo.
    }

    // setUSpinner(): Sets up the spinner list for the location selection menu.
    private void setUpSpinner() {
        String[] locationList = setLocationList(); // Sets up the lists for the Spinner object.

        SJSpinner.createLocationSpinner(this, destSpinner, locationList); // Initializes the spinner.
        destSpinner.setOnItemSelectedListener(this); // Sets a listener to the spinner object.

        SJSpinner.createLocationSpinner(this, originSpinner, locationList); // Initializes the spinner.
        originSpinner.setOnItemSelectedListener(this); // Sets a listener to the spinner object.
    }

    // setLocationList(): Sets up the list of locations for the spinner.
    public String[] setLocationList() {
        return getResources().getStringArray(R.array.duty_locations);
    }

    // onItemSelected(): Override function for setOnTouchListener for the Spinner object.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        try {
            origin = originSpinner.getSelectedItem().toString();
            destination = destSpinner.getSelectedItem().toString();
            SJPreferences.setOrigin(origin, SJ_prefs);
            SJPreferences.setDestination(destination, SJ_prefs);
        }

        catch (NullPointerException e) {
            e.printStackTrace(); // Prints error message.
        }
    }

    // onNothingSelected(): Override function for setOnTouchListener for the Spinner object.
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /** PREFERENCES METHODS ____________________________________________________________________ **/

    private void loadPreferences() {

        // PREFERENCES:
        SJ_prefs = SJPreferences.initializePreferences(SJ_OPTIONS, this);
        origin = SJPreferences.getOrigin(SJ_prefs);
        destination = SJPreferences.getDestination(SJ_prefs);
    }
}