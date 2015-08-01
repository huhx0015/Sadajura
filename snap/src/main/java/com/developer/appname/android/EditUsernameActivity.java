package com.developer.appname.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class EditUsernameActivity extends Activity {

    public static final String TAG = EditUsernameActivity.class.getSimpleName();

	protected EditText mUsername;
	protected Button mSaveBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_username);

		mUsername = (EditText)findViewById(R.id.usernameField);
		mSaveBtn = (Button)findViewById(R.id.savebtn);
		
		final ParseUser currentUser = ParseUser.getCurrentUser();
		
		mUsername.setText(currentUser.getUsername());
        String currentUserName = currentUser.getUsername();

        SharedPreferences sharedPreferences = getSharedPreferences("USERNAME", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Current", currentUserName);
        editor.commit();

        Log.i(TAG, currentUserName);
		
		mSaveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString().trim();
				
		        Pattern pattern = Pattern.compile("\\s");
				Matcher matcher = pattern.matcher(username);
				boolean found = matcher.find();
				
				if (username.isEmpty()) {

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditUsernameActivity.this);
                    qustomDialogBuilder.setMessage(R.string.edit_username_error_message)
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
                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditUsernameActivity.this);
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
                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditUsernameActivity.this);
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
					
					setProgressBarIndeterminateVisibility(true);

                    currentUser.setUsername(username);

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
                                    SharedPreferences sharedPreferences = getSharedPreferences("USERNAME", MODE_PRIVATE);

                                    currentUser.setUsername(sharedPreferences.getString("Current", "currentUserName"));

                                    mUsername.setText(sharedPreferences.getString("Current", "currentUserName"));

                                    Log.i(TAG, currentUser.getUsername());


                                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditUsernameActivity.this);
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
