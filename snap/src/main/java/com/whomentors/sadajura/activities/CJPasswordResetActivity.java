package com.whomentors.sadajura.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.whomentors.sadajura.ui.CJDialogBuilder;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.whomentors.sarajura.R;

public class CJPasswordResetActivity extends Activity {
	
	protected EditText mEmail;
	protected Button mResetBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_password_reset);

		mEmail = (EditText)findViewById(R.id.emailField);
		mResetBtn = (Button)findViewById(R.id.resetbtn);
		mResetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = mEmail.getText().toString();
				
				email = email.trim();
				
				if (email.isEmpty()) {

                    CJDialogBuilder CJDialogBuilder = new CJDialogBuilder(CJPasswordResetActivity.this);
                    CJDialogBuilder.setMessage(R.string.password_reset_error_message)
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle(getResources().getString(R.string.signup_error_title))
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog customDialog = CJDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                }
				else {
					// create the new user!
					setProgressBarIndeterminateVisibility(true);
					
					ParseUser.requestPasswordResetInBackground(email,
                            new RequestPasswordResetCallback() {
						public void done(ParseException e) {
							if (e == null) {
								// Success!
								
								hideSoftKeyboard(CJPasswordResetActivity.this);
								
								String requestMessage = "Please check your email for instructions on resetting your password.";

                                CJDialogBuilder CJDialogBuilder = new CJDialogBuilder(CJPasswordResetActivity.this);
                                CJDialogBuilder.setMessage(requestMessage)
                                        .setIcon(R.drawable.ic_happy_face)
                                        .setTitle("Email Sent!")
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {

                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        Intent intent = new Intent(CJPasswordResetActivity.this, CJLoginActivity.class);
                                                        startActivity(intent);
                                                    }

                                                }, 1000);
                                            }
                                        });

                                AlertDialog customDialog = CJDialogBuilder.create();
                                customDialog.show();

                                Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if(positiveBtn != null)
                                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                            }
							else {

                                CJDialogBuilder CJDialogBuilder = new CJDialogBuilder(CJPasswordResetActivity.this);
                                CJDialogBuilder.setMessage(e.getMessage())
                                        .setIcon(R.drawable.ic_sad_face)
                                        .setTitle(getResources().getString(R.string.signup_error_title))
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog customDialog = CJDialogBuilder.create();
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
	
	public static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
