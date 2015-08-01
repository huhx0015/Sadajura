package com.developer.appname.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {
	
	protected EditText mUsername;
	protected EditText mPassword;
	protected EditText mEmail;
	protected TextView mTermsPrivacyTextView;
	protected Button mSignUpBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_sign_up);

		mUsername = (EditText)findViewById(R.id.usernameField);
		mPassword = (EditText)findViewById(R.id.passwordField);
		mEmail = (EditText)findViewById(R.id.emailField);
		
		mTermsPrivacyTextView = (TextView)findViewById(R.id.termsPrivacyTextView);
		mTermsPrivacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
		mTermsPrivacyTextView.setText(Html.fromHtml(getResources().getString(R.string.terms_privacy)));
		
		mSignUpBtn = (Button)findViewById(R.id.signupbtn);
		mSignUpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				String email = mEmail.getText().toString();
				
				username = username.trim();
				password = password.trim();
				email = email.trim();
				
				Pattern pattern = Pattern.compile("\\s");
				Matcher matcher = pattern.matcher(username);
				boolean found = matcher.find();
				
				if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SignUpActivity.this);
                    qustomDialogBuilder.setMessage(R.string.signup_error_message)
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.signup_error_title))
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog customDialog = qustomDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

                }
				else if (username.startsWith("1") || username.startsWith("2") || username.startsWith("3") || username.startsWith("4") || username.startsWith("5") || username.startsWith("6") || username.startsWith("7") || username.startsWith("8") || username.startsWith("9") || username.startsWith("0"))
			    {
                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SignUpActivity.this);
                    qustomDialogBuilder.setMessage("The username must not start with a number! Please try again by entering a username starting with a letter.")
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.signup_error_title))
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog customDialog = qustomDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                }
				else if (found)
				{
                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SignUpActivity.this);
                    qustomDialogBuilder.setMessage("The username must not contain any spaces! Please try again by entering a username without spaces.")
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.signup_error_title))
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
					// create the new user!
					setProgressBarIndeterminateVisibility(true);
					
					final ParseUser newUser = new ParseUser();
					newUser.setUsername(username);
					newUser.setPassword(password);
					newUser.setEmail(email);
					newUser.signUpInBackground(new SignUpCallback() {
						@Override
						public void done(ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							
							if (e == null) {
								// Success!
								
								ParseInstallation.getCurrentInstallation().addUnique("channels", "user_" + newUser.getObjectId());
								ParseInstallation.getCurrentInstallation().saveInBackground();
								
								Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							}
							else {

                                QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SignUpActivity.this);
                                qustomDialogBuilder.setMessage(e.getMessage())
                                        .setIcon(R.drawable.ic_sad_face)
                                        .setTitle(getResources().getString(R.string.signup_error_title))
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
	}
}
