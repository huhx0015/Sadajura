package com.developer.appname.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditEmailActivity extends Activity {

    public static final String TAG = EditEmailActivity.class.getSimpleName();

	protected EditText mEmail;
	protected Button mSaveBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_email);

		mEmail = (EditText)findViewById(R.id.emailField);
		mSaveBtn = (Button)findViewById(R.id.savebtn);

        final ParseUser currentUser = ParseUser.getCurrentUser();

        mEmail.setText(currentUser.getEmail());
        String currentEmail = currentUser.getEmail();

        SharedPreferences sharedPreferences = getSharedPreferences("EMAIL", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Current", currentEmail);
        editor.commit();

        Log.i(TAG, currentEmail);
		
		mSaveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String email = mEmail.getText().toString().trim();
				
			    if (email.isEmpty()) {

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditEmailActivity.this);
                    qustomDialogBuilder.setMessage(R.string.edit_email_error_message)
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
					
					setProgressBarIndeterminateVisibility(true);
					
					currentUser.setEmail(email);
					
					currentUser.saveInBackground(new SaveCallback() {
						   public void done(ParseException e) {
							   setProgressBarIndeterminateVisibility(false);
						     
							   if (e == null) 
							   {
								   final Handler handler = new Handler();
								   handler.postDelayed(new Runnable() {
									   @Override
									   public void run() {

                                           finish();
                                       }
					    		 
								   }, 1000);
						     	}
						     	else
						     	{
                                    SharedPreferences sharedPreferences = getSharedPreferences("EMAIL", MODE_PRIVATE);

                                    currentUser.setEmail(sharedPreferences.getString("Current", "currentEmail"));

                                    mEmail.setText(sharedPreferences.getString("Current", "currentEmail"));

                                    Log.i(TAG, currentUser.getEmail());


                                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditEmailActivity.this);
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
