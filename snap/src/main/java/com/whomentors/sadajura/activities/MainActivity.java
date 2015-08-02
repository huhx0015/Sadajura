package com.whomentors.sadajura.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.whomentors.sadajura.ParseConstants;
import com.whomentors.sadajura.ui.QustomDialogBuilder;
import com.developer.appname.android.R;
import com.whomentors.sadajura.ui.SectionsPagerAdapter;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int TAKE_VIDEO_REQUEST = 1;
	public static final int PICK_PHOTO_REQUEST = 2;
	public static final int PICK_VIDEO_REQUEST = 3;
	public static final int AVIARY_EDIT_REQUEST = 4;
	
	public static final int MEDIA_TYPE_IMAGE = 5;
	public static final int MEDIA_TYPE_VIDEO = 6;
	
	public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10 MB

    protected Uri mMediaUri;
    protected Uri mOutputUri;
	protected List<ParseObject> subscribedChannels;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			navigateToLogin();
		}
		else {
			Log.i(TAG, currentUser.getUsername());
			
			ParseInstallation.getCurrentInstallation().addUnique("channels", "user_" + currentUser.getObjectId());
			ParseInstallation.getCurrentInstallation().saveInBackground();
			
			String appName = MainActivity.this.getString(R.string.app_name);
			actionBar.setTitle(appName); 
			
			ParseAnalytics.trackAppOpened(getIntent());
		}

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this, 
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						
						if (position == 0)
						{
							Intent newMessage = new Intent("updateMessages");
					        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newMessage);	
						}
						else if (position == 1)
						{
							Intent newRequest = new Intent("updateRequests");
					        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newRequest);	 
						}
						else if (position == 2)
						{
							Intent newFriend = new Intent("updateFriends");
					        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newFriend);	 
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {			
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
				if (data == null) {
					Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
				}
				else {
					mMediaUri = data.getData();
                }
	
				if (requestCode == PICK_VIDEO_REQUEST) {
					// make sure the file is less than 10 MB
					int fileSize = 0;
					InputStream inputStream = null;
					
					try {
						inputStream = getContentResolver().openInputStream(mMediaUri);
						fileSize = inputStream.available();
					}
					catch (FileNotFoundException e) {
						Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
						return;
					}
					catch (IOException e) {
						Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
						return;
					}
					finally {
						try {
							inputStream.close();
						} catch (IOException e) { /* Intentionally blank */ }
					}
					
					if (fileSize >= FILE_SIZE_LIMIT) {
						Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
						return;
					}
				}
			}
			else {
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(mMediaUri);
				sendBroadcast(mediaScanIntent);
			}
			
			if (requestCode == PICK_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST) {
				
				String fileType;
				Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
				recipientsIntent.setData(mMediaUri);
				
				fileType = ParseConstants.TYPE_VIDEO;
				
				recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
				startActivity(recipientsIntent);
			}
			else if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST)
			{
                launchAviaryEditor();
			}
			else if (requestCode == AVIARY_EDIT_REQUEST)
			{
				final Uri mImageUri = data.getData();

                File convert = new File(mImageUri.getPath());
                mOutputUri = getImageContentUri(this, convert);

                System.out.println(mOutputUri.toString());

                Bundle extra = data.getExtras();
                    if( null != extra ) {
                        // image has been changed by the user?
                        boolean changed = extra.getBoolean("bitmap-changed");

                        //dumpIntent(data);

                        if (changed)
                        {
                        	System.out.println("Photo Changed");

                            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
                			recipientsIntent.setData(mOutputUri);

                			String fileType = ParseConstants.TYPE_IMAGE;

                			recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                			startActivity(recipientsIntent);
                        }
                        else
                        {
                        	System.out.println("Photo Not Changed");

                            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
                			recipientsIntent.setData(mOutputUri);

                			String fileType = ParseConstants.TYPE_IMAGE;

                			recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                			startActivity(recipientsIntent);
                        }
                   }
              }
		}
		else if (resultCode == RESULT_CANCELED) {
			
		}
	}

    public static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(TAG,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(TAG,"Dumping Intent end");
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void launchAviaryEditor() {
        if (!isAviaryInstalled())
        {
            QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(MainActivity.this);
            qustomDialogBuilder.setMessage("Install the FREE photo editor from Google Play to add fun Stickers, Frames, Text, Draw, Crop, Memes, Splash Color and Effects.")
                    .setIcon(R.drawable.ic_happy_face)
                    .setTitle("Install Aviary Photo Editor")
                    .setTitleColor("#000000")
                    .setDividerColor("#10f8b7")
                    .setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {

                            dialog.cancel();

                            Intent recipientsIntent = new Intent(getApplicationContext(), RecipientsActivity.class);
                            recipientsIntent.setData(mMediaUri);

                            String fileType = ParseConstants.TYPE_IMAGE;

                            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
                            startActivity(recipientsIntent);

                        }
                    })
                    .setPositiveButton("Get Photo Editor", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {

                            QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(MainActivity.this);
                            qustomDialogBuilder.setMessage("After installing Aviary Photo Editor from Google Play, You must reopen YourAppName (this only happens once).")
                                    .setIcon(R.drawable.ic_instructions)
                                    .setTitle("Instructions")
                                    .setTitleColor("#000000")
                                    .setDividerColor("#10f8b7")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog,int id) {

                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=com.aviary.android.feather"));
                                            startActivity(intent);
                                        }
                                    });

                            AlertDialog customDialog = qustomDialogBuilder.create();
                            customDialog.show();

                            Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if(positiveBtn != null)
                                positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
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
        else
        {
            Intent newIntent = new Intent("aviary.intent.action.EDIT");
            newIntent.setDataAndType(mMediaUri, "image/*"); // required
            newIntent.putExtra("app-id", getPackageName());
            startActivityForResult(newIntent, AVIARY_EDIT_REQUEST);
        }
    }

    public boolean isAviaryInstalled() {
        Intent intent = new Intent( "aviary.intent.action.EDIT" );
        intent.setType( "image/*" );
        List<ResolveInfo> list = getPackageManager()
                .queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY );
        return list.size() > 0;
    }
	
	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		
		switch(itemId) {
		
			case R.id.action_camera:

                final QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(MainActivity.this);

                ListView cameraOptions = new ListView(MainActivity.this);
                cameraOptions.setBackgroundColor(Color.WHITE);
                cameraOptions.setSelector(R.drawable.list_item_selector);

                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, getResources().getStringArray(R.array.camera_choices));

                cameraOptions.setAdapter(modeAdapter);
                qustomDialogBuilder.setView(cameraOptions);

                final AlertDialog customDialog = qustomDialogBuilder.create();
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();

                cameraOptions.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {
                        customDialog.cancel();

                        switch(position) {
                            case 0: // Take picture

                                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                                if (mMediaUri == null) {
                                    // display an error
                                    Toast.makeText(MainActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                                }
                                break;
                            case 1: // Take video
                                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                                if (mMediaUri == null) {
                                    // display an error
                                    Toast.makeText(MainActivity.this, R.string.error_external_storage,
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                    videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);

                                    String manufacturer = android.os.Build.MANUFACTURER;

                                    if (manufacturer.equals("HTC") || manufacturer.equals("HTC Corporation"))
                                    {
                                        System.out.println("manufacturer = HTC");

                                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 = lowest res
                                    }
                                    else
                                    {
                                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 = lowest res

                                        final Toast tag = Toast.makeText(getBaseContext(), R.string.video_size_warning,Toast.LENGTH_SHORT);

                                        tag.show();

                                        new CountDownTimer(7000, 1000)
                                        {
                                            public void onTick(long millisUntilFinished) {tag.show();}
                                            public void onFinish() {tag.show();}

                                        }.start();

                                        long maxVideoSize = 7*1024*1024; // 10 MB // 10491520L

                                        videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                                    }

                                    startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                                }
                                break;
                            case 2: // Choose picture
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                                break;
                            case 3: // Choose video
                                Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                chooseVideoIntent.setType("video/*");
                                Toast.makeText(MainActivity.this, R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
                                startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                                break;
                        }
                    }

                    private Uri getOutputMediaFileUri(int mediaType) {
                        // To be safe, you should check that the SDCard is mounted
                        // using Environment.getExternalStorageState() before doing this.
                        if (isExternalStorageAvailable()) {
                            // get the URI

                            // 1. Get the external storage directory
                            String appName = MainActivity.this.getString(R.string.app_name);
                            File mediaStorageDir = new File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    appName);

                            // 2. Create our subdirectory
                            if (! mediaStorageDir.exists()) {
                                if (! mediaStorageDir.mkdirs()) {
                                    Log.e(TAG, "Failed to create directory.");
                                    return null;
                                }
                            }

                            // 3. Create a file name
                            // 4. Create the file
                            File mediaFile;
                            Date now = new Date();

                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                            String path = mediaStorageDir.getPath() + File.separator;
                            if (mediaType == MEDIA_TYPE_IMAGE) {
                                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                            }
                            else if (mediaType == MEDIA_TYPE_VIDEO) {

                                String manufacturer = android.os.Build.MANUFACTURER;

                                if (manufacturer.equals("HTC") || manufacturer.equals("HTC Corporation"))
                                {
                                    System.out.println("manufacturer = HTC");

                                    mediaFile = new File(path + "VID_" + timestamp + ".3gp");
                                }
                                else
                                {
                                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                                }
                            }
                            else {
                                return null;
                            }

                            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                            // 5. Return the file's URI
                            return Uri.fromFile(mediaFile);
                        }
                        else {
                            return null;
                        }
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
			case R.id.action_edit_friends:
				
				Intent editFriendsIntent = new Intent(this, EditFriendsActivity.class);
				startActivity(editFriendsIntent);
			
			break;
			case R.id.action_settings:
					
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				
			break;
			case R.id.action_logout:
				
				final ParseUser currentUser = ParseUser.getCurrentUser();
				
				String requestMessage = currentUser.getUsername() + ", Are you sure you want to log out? Please note you will not be notified of any new YourAppName messages or friend requests until after you log in again.";

                QustomDialogBuilder qustomDialogBuilder1 = new QustomDialogBuilder(MainActivity.this);
                qustomDialogBuilder1.setMessage(requestMessage)
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
				    		    
				    			   navigateToLogin();
				    			}
				    		 
				    		 }, 1000);
						}
				});

                AlertDialog customDialog1 = qustomDialogBuilder1.create();
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
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
}
