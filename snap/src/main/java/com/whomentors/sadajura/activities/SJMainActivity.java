package com.whomentors.sadajura.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.whomentors.sadajura.data.ParseConstants;
import com.whomentors.sadajura.ui.dialog.SJDialogBuilder;
import com.whomentors.sadajura.ui.adapters.SectionsPagerAdapter;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.whomentors.sadajura.ui.view.SJUnbind;
import com.whomentors.sarajura.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  [SJMainActivity] CLASS
 *  DESCRIPTION: An activity class that displays the main view of Sadajura application and manages
 *  the fragments for the selected tabs of the activity.
 *  -----------------------------------------------------------------------------------------------
 */

public class SJMainActivity extends FragmentActivity implements ActionBar.TabListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ACTIONBAR VARIABLES
    private ActionBar actionBar;

    // LOGGING VARIABLES
	public static final String LOG_TAG = SJMainActivity.class.getSimpleName();

    // MEDIA VARIABLES
    public static final int MEDIA_TYPE_IMAGE = 5;
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int PICK_PHOTO_REQUEST = 2;
    protected Uri mMediaUri;

    // PAGER VARIABLES
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // PARSE VARIABLES
	protected List<ParseObject> subscribedChannels;

    // VIEW INJECTION VARIABLES
    @Bind(R.id.sj_main_pager) ViewPager mainViewPager;

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only
    // runs when the activity is first started.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setUpLayout(); // Sets up the layout for the activity.
        setUpActionBar(); // Sets up the action bar attributes.
        retrieveParseData(); // Retrieves the Parse data about the current user.
		setUpPagerAdapter(); // Sets up the pager adapter for the activity.
	}

    // onDestroy(): This function runs when the activity has terminated and is being destroyed.
    // Calls recycleMemory() to free up memory allocation.
    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Recycles all View objects to free up memory resources.
        SJUnbind.recycleMemory(findViewById(R.id.sj_main_activity_layout));
    }

    /** PHYSICAL BUTTON FUNCTIONALITY __________________________________________________________ **/

    // BACK KEY:
    // onBackPressed(): Defines the action to take when the physical back button key is pressed.
    @Override
    public void onBackPressed() {
        finish(); // Finishes the activity.
    }

    /** ACTIVITY EXTENSION METHODS _____________________________________________________________ **/

    // onActivityResult(): Run after returning to the activity after taking or choosing a photo.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {			
			if (requestCode == PICK_PHOTO_REQUEST) {
				if (data == null) {
					Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
				}
				else {
					mMediaUri = data.getData();
                }

			}

			else {
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(mMediaUri);
				sendBroadcast(mediaScanIntent);
			}

			if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {

                Intent recipientsIntent = new Intent(getApplicationContext(), RecipientsActivity.class);
                recipientsIntent.setData(mMediaUri);

                String fileType = ParseConstants.TYPE_IMAGE;

                recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                startActivity(recipientsIntent);
			}
		}

        else if (resultCode == RESULT_CANCELED) {}
	}

    // onCreateOptionsMenu(): Inflates the menu when the menu key is pressed. This adds items to
    // the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    // onOptionsItemSelected(): Defines the action to take when the menu options are selected.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		
		switch(itemId) {

            // CAMERA ACTION:
			case R.id.action_camera:

                // Creates a new dialog displaying the picture options.
                final SJDialogBuilder SJDialogBuilder = new SJDialogBuilder(SJMainActivity.this);

                // Creates a ListView for the dialog.
                ListView cameraOptions = new ListView(SJMainActivity.this);
                cameraOptions.setBackgroundColor(Color.WHITE);
                cameraOptions.setSelector(R.drawable.list_item_selector);

                // Sets the list of choices from the defined array string.
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(SJMainActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, getResources().getStringArray(R.array.camera_choices));
                cameraOptions.setAdapter(modeAdapter);
                SJDialogBuilder.setView(cameraOptions);

                // Displays the dialog window.
                final AlertDialog customDialog = SJDialogBuilder.create();
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();

                // CAMERA BUTTON:
                cameraOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                        customDialog.cancel();

                        switch(position) {

                            // TAKE PICTURE:
                            case 0:

                                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                                if (mMediaUri == null) {

                                    // Displays an error message.
                                    Toast.makeText(SJMainActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                }

                                else {
                                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                                }

                                break;

                            // CHOOSE PICTURE:
                            case 2:
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                                break;
                        }
                    }

                    // getOutputMediaFileUri(): Retrieves the output media file URI.
                    private Uri getOutputMediaFileUri(int mediaType) {

                        // To be safe, you should check that the SDCard is mounted using
                        // Environment.getExternalStorageState() before doing this.
                        if (isExternalStorageAvailable()) {

                            // Retrieves the external storage directory.
                            String appName = SJMainActivity.this.getString(R.string.app_name);
                            File mediaStorageDir = new File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    appName);

                            // Creates the subdirectory.
                            if (! mediaStorageDir.exists()) {
                                if (! mediaStorageDir.mkdirs()) {
                                    Log.e(LOG_TAG, "getOutputMediaFileUri: Failed to create directory.");
                                    return null;
                                }
                            }

                            // Creates a file name and the file.
                            File mediaFile;
                            Date now = new Date();

                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                            String path = mediaStorageDir.getPath() + File.separator;
                            if (mediaType == MEDIA_TYPE_IMAGE) {
                                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                            }

                            else { return null; }

                            Log.d(LOG_TAG, "File: " + Uri.fromFile(mediaFile));

                            // Returns the file's URI.
                            return Uri.fromFile(mediaFile);
                        }

                        else { return null; }
                    }

                    private boolean isExternalStorageAvailable() {
                        String state = Environment.getExternalStorageState();

                        if (state.equals(Environment.MEDIA_MOUNTED)) {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                });

            break;

            // EDIT FRIENDS:
			case R.id.action_edit_friends:
				
				Intent editFriendsIntent = new Intent(this, EditFriendsActivity.class);
				startActivity(editFriendsIntent);
			
			break;

            // SETTINGS:
			case R.id.action_settings:
					
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				
			break;

            // LOGOUT:
			case R.id.action_logout:
				
				final ParseUser currentUser = ParseUser.getCurrentUser();
				String requestMessage = currentUser.getUsername() + ", Are you sure you want to log out? Please note you will not be notified of any new YourAppName messages or friend requests until after you log in again.";

                SJDialogBuilder SJDialogBuilder1 = new SJDialogBuilder(SJMainActivity.this);
                SJDialogBuilder1.setMessage(requestMessage)
                    .setIcon(R.drawable.ic_sad_face)
					.setTitle("Log Out?")
                    .setTitleColor("#000000")
                    .setDividerColor("#10f8b7")
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {

							dialog.cancel();
						}
					})
					.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							
							subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
							
							System.out.println(subscribedChannels);
							
							ParseInstallation.getCurrentInstallation().removeAll("channels", subscribedChannels);
							ParseInstallation.getCurrentInstallation().saveInBackground();
							
							ParseUser.logOut();
						
							final Handler handler = new Handler();
				    		 handler.postDelayed(new Runnable() {
				    		   @Override
				    		   public void run() {
				    		    
				    			   launchLoginIntent();
				    			}
				    		 
				    		 }, 1000);
						}
				});

                AlertDialog customDialog1 = SJDialogBuilder1.create();
                customDialog1.show();

                Button positiveBtn = customDialog1.getButton(DialogInterface.BUTTON_POSITIVE);
                if(positiveBtn != null)
                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

                Button negativeBtn = customDialog1.getButton(DialogInterface.BUTTON_NEGATIVE);
                if(negativeBtn != null)
                    negativeBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
				
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {

		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mainViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // launchLoginIntent(): Launches a Intent to return to the SJLoginActivity.
    private void launchLoginIntent() {
        Intent intent = new Intent(this, SJLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // setUpLayout(): Sets up the layout for the activity.
    private void setUpLayout() {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.sj_main_activity);
        ButterKnife.bind(this); // ButterKnife view injection initialization.
    }

    // setUpActionBar(): Sets up the action bar attributes for the activity.
    private void setUpActionBar() {

        // Sets up the action bar.
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        String appName = SJMainActivity.this.getString(R.string.app_name);
        actionBar.setTitle(appName); // Sets the title of the action bar.
        actionBar.setDisplayShowHomeEnabled(false); // Disables the home icon.
        actionBar.setDisplayUseLogoEnabled(false); // Disables the display of the logo.
    }

    // setUpPagerAdapter(): Sets up the pager adapter for the activity.
    private void setUpPagerAdapter() {

        // Creates the adapter that will return a fragment for each of the primary sections of the
        // app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Sets up the ViewPager with the sections adapter.
        mainViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab. We can also use
        // ActionBar.Tab select() to do this if we have a reference to the Tab.
        mainViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);

                        // MESSAGES:
                        if (position == 0) {
                            Intent newMessage = new Intent("updateMessages");
                            LocalBroadcastManager.getInstance(SJMainActivity.this).sendBroadcast(newMessage);
                        }

                        // REQUESTS:
                        else if (position == 1) {
                            Intent newRequest = new Intent("updateRequests");
                            LocalBroadcastManager.getInstance(SJMainActivity.this).sendBroadcast(newRequest);
                        }

                        // FRIENDS:
                        else if (position == 2) {
                            Intent newFriend = new Intent("updateFriends");
                            LocalBroadcastManager.getInstance(SJMainActivity.this).sendBroadcast(newFriend);
                        }
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    /** PARSE METHODS __________________________________________________________________________ **/

    // retrieveParseData(): Retrieves the Parse data about the current user and data environment.
    private void retrieveParseData() {

        // Retrieves the current Parse user data.
        ParseUser currentUser = ParseUser.getCurrentUser();

        // Returns to the SJLoginActivity if the current user is null.
        if (currentUser == null) { launchLoginIntent(); }

        // Retrieves the Parse data environment information.
        else {
            ParseInstallation.getCurrentInstallation().addUnique("channels", "user_" + currentUser.getObjectId());
            ParseInstallation.getCurrentInstallation().saveInBackground();
            ParseAnalytics.trackAppOpened(getIntent());
            Log.i(LOG_TAG, currentUser.getUsername());
        }
    }
}
