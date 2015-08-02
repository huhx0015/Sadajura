package com.whomentors.sadajura.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.whomentors.sadajura.ui.dialog.SJDialogBuilder;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.whomentors.sadajura.ui.view.SJUnbind;
import com.whomentors.sarajura.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  [SJLoginActivity] CLASS
 *  DESCRIPTION: An activity class that displays the login screen.
 *  -----------------------------------------------------------------------------------------------
 */

public class SJLoginActivity extends Activity {

	/** CLASS VARIABLES ________________________________________________________________________ **/

	// LOGGING VARIABLES
	public static final String LOG_TAG = SJLoginActivity.class.getSimpleName();

	// VIEW INJECTION VARIABLES
	@Bind(R.id.sj_login_button) Button loginButton;
	@Bind(R.id.sj_signup_btn) Button signUpButton;
	@Bind(R.id.sj_username_field) EditText userNameText;
	@Bind(R.id.sj_password_field) EditText passwordText;
	@Bind(R.id.sj_reset_text) TextView passwordResetText;

	/** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

	// onCreate(): The initial function that is called when the activity is run. onCreate() only
	// runs when the activity is first started.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpLayout(); // Sets up the layout for the activity.
		setUpButtons(); // Sets up the button listeners for the activity.
		setUpActionBar(); // Sets up the action bar.
	}

	// onStop(): This function runs when screen is no longer visible and the activity is in a
	// state prior to destruction.
	@Override
	protected void onStop() {
		super.onStop();
		finish(); // The CJLoginActivity is terminated at this point.
	}

	// onDestroy(): This function runs when the activity has terminated and is being destroyed.
	// Calls recycleMemory() to free up memory allocation.
	@Override
	protected void onDestroy() {

		super.onDestroy();

		// Recycles all View objects to free up memory resources.
		SJUnbind.recycleMemory(findViewById(R.id.sj_login_activity_layout));
	}

	/** PHYSICAL BUTTON FUNCTIONALITY __________________________________________________________ **/

	// BACK KEY:
	// onBackPressed(): Defines the action to take when the physical back button key is pressed.
	@Override
	public void onBackPressed() {
		finish(); // Finishes the activity.
	}

	/** LAYOUT METHODS _________________________________________________________________________ **/

	// setUpLayout(); Sets up the layout for the activity.
	private void setUpLayout() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Sets the window feature.
		setContentView(R.layout.sj_login_activity); // Sets the XML layout file.
		ButterKnife.bind(this); // ButterKnife view injection initialization.
	}

	// setUpButtons(): Sets up the Button listeners for the activity.
	private void setUpButtons() {

		// SIGN UP BUTTON:
		signUpButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SJLoginActivity.this, SignUpActivity.class);
				startActivity(intent);
			}
		});

		// LOGIN BUTTON:
		loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String username = ""; // Stores the username.
				String password = ""; // Stores the password.

				// Retrieves the username and password from the EditText fields.
				try {
					username = userNameText.getText().toString();
					password = passwordText.getText().toString();
				}

				// NullPointerException handler.
				catch (NullPointerException e) {
					e.printStackTrace();
					Log.e(LOG_TAG, "ERROR: loginButton: Failed to retrieve username and/or password: " + e);
				}

				// Trims the username and password of whitespaces.
				username = username.trim();
				password = password.trim();

				// If either the username or password is empty, the login error dialog window is
				// shown.
				if (username.isEmpty() || password.isEmpty()) {

					// Builds the dialog window.
					SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJLoginActivity.this);
					SJDialogBuilder.setMessage(R.string.login_error_message)
							.setIcon(R.drawable.ic_sad_face)
							.setTitle(getResources().getString(R.string.login_error_title))
							.setTitleColor("#000000")
							.setDividerColor("#10f8b7")
							.setPositiveButton(android.R.string.ok, null);

					// Displays the built dialog window.
					AlertDialog customDialog = SJDialogBuilder.create();
					customDialog.show();

					// References the positive button for the dialog.
					Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);

					// Sets the background for the positive button for the dialog.
					if (positiveBtn != null) {
						positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
					}

				}

				// Begins the login process.
				else {

					setProgressBarIndeterminateVisibility(true); // Sets the progress bar.

					// Logs in using the Parse backend environment.
					ParseUser.logInInBackground(username, password, new LogInCallback() {

						// done(): Runs when the login process has completed.
						@Override
						public void done(ParseUser user, ParseException e) {

							setProgressBarIndeterminateVisibility(false); // Hides the progress bar.

							// If no error has occurred, the Intent to the CJMainActivity is
							// launched.
							if (e == null) {

								ParseInstallation.getCurrentInstallation().addUnique("channels", "user_" + user.getObjectId());
								ParseInstallation.getCurrentInstallation().saveInBackground();

								// Creates an intent to the CJMainActivity.
								Intent intent = new Intent(SJLoginActivity.this, SJMainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							}

							// An error has occurred during the Parse request and a error dialog
							// window is shown.
							else {

								// Builds the dialog window.
								SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJLoginActivity.this);
								SJDialogBuilder.setMessage(e.getMessage())
										.setIcon(R.drawable.ic_sad_face)
										.setTitle(getResources().getString(R.string.login_error_title))
										.setTitleColor("#000000")
										.setDividerColor("#10f8b7")
										.setPositiveButton(android.R.string.ok, null);

								// Displays the dialog window.
								AlertDialog customDialog = SJDialogBuilder.create();
								customDialog.show();

								// Sets the attributes for the positive button for the dialog.
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

		// PASSWORD RESET:
		passwordResetText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Creates the Intent to launch the CJPasswordResetActivity.
				Intent intent = new Intent(SJLoginActivity.this, SJPasswordResetActivity.class);
				startActivity(intent);
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
}
