package com.developer.appname.android;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class SettingsActivity extends Activity {

	public static final String TAG = SettingsActivity.class.getSimpleName();
	
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int PICK_PHOTO_REQUEST = 1;
	public static final int PHOTO_CROP = 2;
	
	protected Uri mMediaUri;
	private ParseImageView profileImageView;
	
	private ListView currentAccountListView;
	protected List<ParseObject> mMessages;
	private ListView spreadTheWordListView;
	private ListView moreInfoListView;
	private ListView contactUsListView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Show the Up button in the action bar.
		setupActionBar();

        final ParseUser currentUser = ParseUser.getCurrentUser();

        profileImageView = (ParseImageView) findViewById(R.id.profileImage);

        ParseFile profileImage = currentUser.getParseFile("profileImage");

        if (profileImage != null) {

            Picasso.with(getApplicationContext()).load(profileImage.getUrl()).into(profileImageView);
        }
        else
        {
            profileImageView.setImageResource(R.drawable.ic_profile_main);
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SettingsActivity.this);

                ListView cameraOptions = new ListView(SettingsActivity.this);
                cameraOptions.setBackgroundColor(Color.WHITE);
                cameraOptions.setSelector(R.drawable.list_item_selector);

                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, getResources().getStringArray(R.array.camera_choices2));

                cameraOptions.setAdapter(modeAdapter);
                qustomDialogBuilder.setView(cameraOptions);

                final AlertDialog customDialog = qustomDialogBuilder.create();
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();

                cameraOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        customDialog.cancel();

                        switch (position) {
                            case 0: // Take picture

                                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);

                                break;
                            case 1: // Choose picture

                                Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);

                                break;
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final ParseUser currentUser = ParseUser.getCurrentUser();

        TextView userName = (TextView) findViewById(R.id.userName);
        userName.setText(currentUser.getUsername());

        String[] accountDetails = new String[] { "Username: " + currentUser.getUsername(), "Email: " + currentUser.getEmail(), "Clear My Messages"};

        ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountDetails);

        currentAccountListView = (ListView) findViewById(R.id.currentAccountList);
        currentAccountListView.setAdapter(accountArrayAdapter);

        currentAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                if (position == 0)
                {
                    Intent intent = new Intent(SettingsActivity.this, EditUsernameActivity.class);
                    SettingsActivity.this.startActivity(intent);
                }
                else if (position == 1)
                {
                    Intent intent = new Intent(SettingsActivity.this, EditEmailActivity.class);
                    SettingsActivity.this.startActivity(intent);
                }
                else if (position == 2)
                {
                    String requestMessage = "This will delete all saved/non self destructing YourAppName messages. Don't worry we wont delete any self destructing YourAppName messages you have not viewed yet.";

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(SettingsActivity.this);
                    qustomDialogBuilder.setMessage(requestMessage)
                            .setIcon(R.drawable.ic_happy_face)
                            .setTitle("Clear My Messages?")
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {

                                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
                                    query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
                                    query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
                                    query.findInBackground(new FindCallback<ParseObject>() {

                                        @Override
                                        public void done(List<ParseObject> messages, ParseException e) {

                                            if (e == null) {

                                                // We found messages!
                                                mMessages = messages;

                                                for (ParseObject message : mMessages) {

                                                    String messageTime = message.getString(ParseConstants.KEY_FILE_TIME);

                                                    if (messageTime.equals("0"))
                                                    {
                                                        // Delete it!
                                                        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);

                                                        if (ids.size() == 1) {
                                                            // last recipient - delete the whole thing!
                                                            message.deleteInBackground();
                                                        }
                                                        else {
                                                            // remove the recipient and save
                                                            ids.remove(ParseUser.getCurrentUser().getObjectId());

                                                            ArrayList<String> idsToRemove = new ArrayList<String>();
                                                            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

                                                            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
                                                            message.saveInBackground();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            });

                    AlertDialog customDialog = qustomDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

                    Button negativeBtn = customDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if(negativeBtn != null)
                        negativeBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                }
            }
        });

        String[] spreadDetails = new String[] { "Invite Your Friends"};

        ArrayAdapter<String> spreadArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, spreadDetails);

        spreadTheWordListView = (ListView) findViewById(R.id.spreadTheWordList);
        spreadTheWordListView.setAdapter(spreadArrayAdapter);

        spreadTheWordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                //emailIntent.setType("text/html");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey! Found this awesome app!");
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        Html.fromHtml(new StringBuilder()
                                .append("Check out this app it lets you share private photo and video messages between you and your friends. " +
                                        "The coolest part is you only have 10 seconds after you tap on the message to view it before it vanishes forever.<br><br>")
                                .append("<a href='https://play.google.com/store/apps/details?id=com.developer.appname.android'>Download</a> it now then add (" + currentUser.getUsername() +
                                        ") to your friends list so we can chat. See you soon!<br><br>Sent from YourAppName on my Android device.")

                                .toString()));

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));

                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String[] moreDetails = new String[] { "Terms of Use","Privacy Policy","Getting Started"};

        ArrayAdapter<String> moreArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, moreDetails);

        moreInfoListView = (ListView) findViewById(R.id.moreInfoList);
        moreInfoListView.setAdapter(moreArrayAdapter);

        moreInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                if (position == 0)
                {
                    Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
                    intent.putExtra("webUrl", "http://yourserver.com/snapsecret/termsofuse.html");
                    intent.putExtra("navTitle", "Terms of Use");
                    startActivity(intent);
                }
                else if (position == 1)
                {
                    Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
                    intent.putExtra("webUrl", "http://yourserver.com/snapsecret/privacypolicy.html");
                    intent.putExtra("navTitle", "Privacy Policy");
                    startActivity(intent);
                }
                else if (position == 2)
                {
                    Uri fileUri = Uri.parse("http://yourserver.com/snapsecret/demoAndroid.mp4");

                    Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                    intent.setDataAndType(fileUri, "video/*");
                    startActivity(intent);
                }
            }
        });

        String[] contactDetails = new String[] { "Send Email"};

        ArrayAdapter<String> contactArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactDetails);

        contactUsListView = (ListView) findViewById(R.id.contactUsList);
        contactUsListView.setAdapter(contactArrayAdapter);

        contactUsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:support@yourserver.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "YourAppName (Android) Support Ver. 1.0");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
		
			this.finish();
	        return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {			
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
			
				if (data == null) {
					
					Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
				}
				else {
					
					mMediaUri = data.getData();
					
					System.out.println(mMediaUri);
					
					photoCrop();
				}
			}
			// user is returning from cropping the image
			else if (requestCode == PHOTO_CROP) {
				
				// get the returned data
				Bundle extras = data.getExtras();
				// get the cropped bitmap
				Bitmap thePic = extras.getParcelable("data");
				// display the returned cropped image
				GraphicsUtil graphicUtil = new GraphicsUtil();
				Bitmap picture = (Bitmap) (graphicUtil.getCircleBitmap(thePic, 16));
				
				profileImageView.setImageBitmap(picture);
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
			    picture.compress(Bitmap.CompressFormat.PNG, 100, stream);
			    // get byte array here
			    final byte[] bytearray = stream.toByteArray();
			    
			    if (bytearray != null) {

					final ParseUser currentUser = ParseUser.getCurrentUser();
					final ParseFile fileCircularImage = new ParseFile(bytearray);
					
					fileCircularImage.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
						     if (e == null) {
						    	 
						    	 currentUser.put("profileImage", fileCircularImage);
						    	 try {
									currentUser.save();
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
						    }
						}
					});
				}
			    else
			    {
			    	// System.out.println("bytearray is null");
			    }
			}
		}
		else if (resultCode != RESULT_CANCELED) {
		
		}
	}
	
	private void photoCrop() {
		// take care of exceptions
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(mMediaUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 600);
			cropIntent.putExtra("outputY", 600);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PHOTO_CROP);
		}
		// respond to users whose devices do not support the crop action
		catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Oops! -Your device doesn't support the crop action!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
}
