package com.whomentors.sadajura.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.whomentors.sadajura.ui.dialog.SJDialogBuilder;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.whomentors.sadajura.ui.view.SJUnbind;
import com.whomentors.sarajura.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  SJPasswordResetActivity] CLASS
 *  DESCRIPTION: An activity class that displays the password reset screen.
 *  -----------------------------------------------------------------------------------------------
 */

public class SJPasswordResetActivity extends Activity {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    public static final String LOG_TAG = SJPasswordResetActivity.class.getSimpleName();

    // VIEW INJECTION VARIABLES
    @Bind(R.id.sj_reset_button) Button resetButton;
    @Bind(R.id.sj_email_field) EditText emailText;

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only
    // runs when the activity is first started.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setUpLayout(); // Sets up the layout for the activity.
        setUpButtons(); // Sets up the Button listeners for the activity.
        setUpActionBar(); // Sets up the action bar attributes for the activity.
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
        SJUnbind.recycleMemory(findViewById(R.id.sj_password_reset_activity_layout));
    }

    /** ACTIVITY EXTENSION METHODS _____________________________________________________________ **/

    // onOptionsItemSelected(): Defines the action to take when the menu options are selected.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            launchLoginIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    /** PHYSICAL BUTTON FUNCTIONALITY __________________________________________________________ **/

    // BACK KEY:
    // onBackPressed(): Defines the action to take when the physical back button key is pressed.
    @Override
    public void onBackPressed() {
        launchLoginIntent();
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // launchLoginIntent(): Launches a intent to the SJLoginActivity.
    private void launchLoginIntent() {

        // Creates an intent to CJLoginActivity.
        Intent intent = new Intent(SJPasswordResetActivity.this, SJLoginActivity.class);
        startActivity(intent);
    }

    // setUpLayout(): Sets up the layout for the activity.
    private void setUpLayout() {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.sj_password_reset_activity);
        ButterKnife.bind(this); // ButterKnife view injection initialization.
    }

    // setUpButtons(): Sets up the listeners for the Buttons in the activity.
    private void setUpButtons() {

        // RESET BUTTON:
        resetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = "";

                // Retrieves the e-mail field String value.
                try {
                    email = emailText.getText().toString();
                }

                // NullPointerException error handler.
                catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "ERROR: resetButton: Failed to retrieve e-mail address: " + e);
                }

                email = email.trim(); // Trims the e-mail string of whitespace.

                // If the email string value is empty, an error dialog window is shown.
                if (email.isEmpty()) {

                    // Builds the dialog window.
                    SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJPasswordResetActivity.this);
                    SJDialogBuilder.setMessage(R.string.password_reset_error_message)
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.signup_error_title))
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    // Displays the dialog window.
                    AlertDialog customDialog = SJDialogBuilder.create();
                    customDialog.show();

                    // Sets the background properties of the positive button.
                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positiveBtn != null) {
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                    }
                }

                // Creates the new user.
                else {

                    setProgressBarIndeterminateVisibility(true); // Displays the progress bar.

                    // Performs a password reset onto the Parse environment for the account
                    // associated with the inputted e-mail address.
                    ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {

                        // done(): Runs when the Parse request has completed.
                        public void done(ParseException e) {

                            if (e == null) {

                                // Hides the soft keyboard.
                                hideSoftKeyboard(SJPasswordResetActivity.this);

                                // Sets the request message.
                                String requestMessage = "Please check your e-mail address for instructions on resetting your password.";

                                // Builds the dialog window.
                                SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJPasswordResetActivity.this);
                                SJDialogBuilder.setMessage(requestMessage)
                                        .setIcon(R.drawable.ic_happy_face)
                                        .setTitle("Email Sent!")
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                // Delays the launch of the CJLoginActivity
                                                // by 1000 ms.
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        launchLoginIntent();
                                                    }

                                                }, 1000);
                                            }
                                        });

                                // Displays the dialog window.
                                AlertDialog customDialog = SJDialogBuilder.create();
                                customDialog.show();

                                // Sets the background properties for the positive button for the dialog.
                                Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positiveBtn != null) {
                                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                                }
                            }

                            // An error has occurred during the Parse request and a error
                            // dialog window is shown.
                            else {

                                // Builds the dialog window.
                                SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJPasswordResetActivity.this);
                                SJDialogBuilder.setMessage(e.getMessage())
                                        .setIcon(R.drawable.ic_sad_face)
                                        .setTitle(getResources().getString(R.string.signup_error_title))
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setPositiveButton(android.R.string.ok, null);

                                // The dialog window is shown.
                                AlertDialog customDialog = SJDialogBuilder.create();
                                customDialog.show();

                                // Sets the background properties for the positive button for the dialog.
                                Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positiveBtn != null) {
                                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    // setUpActionBar(): Sets up the action bar attributes.
    private void setUpActionBar() {

        // Sets up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false); // Disables the home icon.
        actionBar.setDisplayUseLogoEnabled(false); // Disables the display of the logo.
    }

    // hideSoftKeyBoard(): Hides the soft keyboard from the user focus.
	public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
