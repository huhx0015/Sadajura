package com.developer.appname.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	protected EditText mUsername;
	protected EditText mPassword;
	protected Button mLoginBtn;
	protected Button mSignUpBtn;
	protected TextView mPasswordResetTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);

		mSignUpBtn = (Button)findViewById(R.id.signupbtn);
		mSignUpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivity(intent);
			}
		});
		
		mUsername = (EditText)findViewById(R.id.usernameField);
		mPassword = (EditText)findViewById(R.id.passwordField);
		mLoginBtn = (Button)findViewById(R.id.loginbtn);
		mLoginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				
				username = username.trim();
				password = password.trim();
				
				if (username.isEmpty() || password.isEmpty()) {

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(LoginActivity.this);
                    qustomDialogBuilder.setMessage(R.string.login_error_message)
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.login_error_title))
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog customDialog = qustomDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

                }
				else {
					// Login
					setProgressBarIndeterminateVisibility(true);
					
					ParseUser.logInInBackground(username, password, new LogInCallback() {
						@Override
						public void done(ParseUser user, ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							
							if (e == null) {
								// Success!
								
								ParseInstallation.getCurrentInstallation().addUnique("channels", "user_" + user.getObjectId());
								ParseInstallation.getCurrentInstallation().saveInBackground();
								
								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							}
							else {

                                QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(LoginActivity.this);
                                qustomDialogBuilder.setMessage(e.getMessage())
                                        .setIcon(R.drawable.ic_sad_face)
                                        .setTitle(getResources().getString(R.string.login_error_title))
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog customDialog = qustomDialogBuilder.create();
                                customDialog.show();

                                Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if(positiveBtn != null)
                                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                            }
				}
					});
			}
			}
		});
		
		mPasswordResetTextView = (TextView)findViewById(R.id.resetTextView);
		mPasswordResetTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
				startActivity(intent);
			}
		});
	}
}
