package com.developer.appname.android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class RecipientsActivity extends ListActivity {

	public static final String TAG = RecipientsActivity.class.getSimpleName();

	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseRelation<ParseUser> mRecipientsRelation;
	protected ParseUser mCurrentUser;	
	protected List<ParseUser> mFriends;	
	protected List<ParseUser> mOtherFriends;
	protected MenuItem mShareMenuItem;
	protected ShareActionProvider mShareActionProvider;
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;
	protected String mFileTime;
	private ArrayList<ParseUser> mFriendsFound;
	private RecipientsAdapter recipientsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_recipients);
		
		mFriendsFound = new ArrayList<ParseUser>();
		
		recipientsAdapter = new RecipientsAdapter();
		
		// Show the Up button in the action bar.
		setupActionBar();

		updateRecipientsList();
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		mMediaUri = getIntent().getData();

		System.out.println(mMediaUri);
		
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
	}
	
	public class RecipientsAdapter extends BaseAdapter {

		List<ParseUser> parseRecipientsList = mFriendsFound;
		
    	public int getCount() {
			
			return parseRecipientsList.size();
		}

		@Override
		public ParseUser getItem(int arg0) {
			
			return parseRecipientsList.get(arg0);
		}
		
		@Override
		public long getItemId(int arg0) {
			
			return arg0;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			if(arg1==null)
			{
				LayoutInflater inflater = (LayoutInflater) RecipientsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				arg1 = inflater.inflate(R.layout.recipients_item, arg2, false);
			}
			
			final ParseImageView profileImageView = (ParseImageView) arg1.findViewById(R.id.profileImage);
			CheckedTextView username = (CheckedTextView)arg1.findViewById(R.id.senderLabel);
			
			ParseUser user = parseRecipientsList.get(arg0);

            ParseFile profileImage = user.getParseFile("profileImage");

            if (profileImage != null) {

                Picasso.with(arg2.getContext()).load(profileImage.getUrl()).into(profileImageView);
            }
            else
            {
                profileImageView.setImageResource(R.drawable.ic_profile);
            }

            username.setText(user.getUsername());
			
			return arg1;
		}
	}
	
	private void updateRecipientsList() {
		
		mFriendsRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				
				if (e == null) {
					// Success
					mFriends = friends;
					
					for (final ParseUser user : mFriends) {

						mRecipientsRelation = user.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

						ParseQuery<ParseUser> query = mRecipientsRelation.getQuery();
						query.addAscendingOrder(ParseConstants.KEY_USERNAME);
						query.findInBackground(new FindCallback<ParseUser>() {
				
							@Override
							public void done(List<ParseUser> otherFriends, ParseException e) {
								
								if (e == null)
								{
									// Success
									mOtherFriends = otherFriends;
									
									mCurrentUser = ParseUser.getCurrentUser();
					    
									for (ParseUser friend : mOtherFriends) {
										
										if (friend.getObjectId().equals(mCurrentUser.getObjectId()))
										{	
											mFriendsFound.add(user);
											
											setListAdapter(recipientsAdapter);
										}
									}
								}
							}
						});
					}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipients, menu);
		
		if (mFileType.equals(ParseConstants.TYPE_IMAGE))
		{
			// Get the ActionProvider for later usage
			mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
			
			mShareMenuItem = menu.findItem(R.id.action_share);
			mShareMenuItem.setVisible(true);
			mShareActionProvider = (ShareActionProvider) mShareMenuItem.getActionProvider();
			mShareActionProvider.setShareIntent(shareImage());
		}
		else
		{
			// Get the ActionProvider for later usage
			mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
			
			mShareMenuItem = menu.findItem(R.id.action_share);
			mShareMenuItem.setVisible(true);
			mShareActionProvider = (ShareActionProvider) mShareMenuItem.getActionProvider();
			mShareActionProvider.setShareIntent(shareVideo());
		}

		mSendMenuItem = menu.findItem(R.id.action_send);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                this.finish();
                return true;

            case R.id.action_send:

                if (getRecipientIds().size() == 0) {
                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(RecipientsActivity.this);
                    qustomDialogBuilder.setMessage("Please tap on a friend to select them as a recipient and try sending again.")
                            .setIcon(R.drawable.ic_sad_face)
                            .setTitle("Oops!")
                            .setTitleColor("#000000")
                            .setDividerColor("#10f8b7")
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog customDialog = qustomDialogBuilder.create();
                    customDialog.show();

                    Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(positiveBtn != null)
                        positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

                } else {
                    if (mFileTime == null) {
                        if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                            final QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(RecipientsActivity.this);

                            ListView cameraOptions = new ListView(RecipientsActivity.this);
                            cameraOptions.setBackgroundColor(Color.WHITE);
                            cameraOptions.setSelector(R.drawable.list_item_selector);

                            ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(RecipientsActivity.this, android.R.layout.simple_list_item_1,
                                    android.R.id.text1, getResources().getStringArray(R.array.image_message_time));

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
                                        case 0: // Never

                                            mFileTime = "0";

                                            ParseObject message = createMessage();

                                            send(message);
                                            finish();

                                            break;
                                        case 1: // 3 sec

                                            mFileTime = "3000";

                                            ParseObject message1 = createMessage();

                                            send(message1);
                                            finish();

                                            break;
                                        case 2: // 5 sec

                                            mFileTime = "5000";

                                            ParseObject message2 = createMessage();

                                            send(message2);
                                            finish();

                                            break;

                                        case 3: // 7 sec

                                            mFileTime = "7000";

                                            ParseObject message3 = createMessage();

                                            send(message3);
                                            finish();

                                            break;

                                        case 4: // 10 sec

                                            mFileTime = "10000";

                                            ParseObject message4 = createMessage();

                                            send(message4);
                                            finish();

                                            break;
                                    }
                                }
                            });
                        } else {
                            final QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(RecipientsActivity.this);

                            ListView cameraOptions = new ListView(RecipientsActivity.this);
                            cameraOptions.setBackgroundColor(Color.WHITE);
                            cameraOptions.setSelector(R.drawable.list_item_selector);

                            ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(RecipientsActivity.this, android.R.layout.simple_list_item_1,
                                    android.R.id.text1, getResources().getStringArray(R.array.video_message_time));

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
                                        case 0: // Never

                                            mFileTime = "0";

                                            ParseObject message = createMessage();

                                            send(message);
                                            finish();

                                            break;
                                        case 1: // 10 sec

                                            mFileTime = "10000";

                                            ParseObject message1 = createMessage();

                                            send(message1);
                                            finish();

                                            break;
                                    }
                                }

                            });
                        }
                    }

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
	
	private Intent shareImage() {
	    Intent shareIntent = new Intent();
	    shareIntent.setAction(Intent.ACTION_SEND);
	    shareIntent.putExtra(Intent.EXTRA_STREAM, mMediaUri);
	 
	    // If you want to share a png image only, you can do:
	    // setType("image/png"); OR for jpeg: setType("image/jpeg");
	    shareIntent.setType("image/*");
	 
	    return shareIntent;
	}
	
	private Intent shareVideo() {
	    Intent shareIntent = new Intent();
	    shareIntent.setAction(Intent.ACTION_SEND);
	    shareIntent.putExtra(Intent.EXTRA_STREAM, mMediaUri);
	 
	    shareIntent.setType("video/*");
	 
	    return shareIntent;
	}
	
	protected ParseObject createMessage() {
		ParseFile profileImage = ParseUser.getCurrentUser().getParseFile("profileImage");
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
		message.put(ParseConstants.KEY_FILE_TIME, mFileTime);
		message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
		message.put(ParseConstants.KEY_RECIPIENT_NAMES, getRecipientNames());
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		if (profileImage != null) {
			message.put("profileImage", profileImage);
		}
		
		byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
		
		if (fileBytes == null) {
			return null;
		}
		else {
			if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
				fileBytes = FileHelper.reduceImageForUpload(fileBytes);
			}
			
			String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
			ParseFile file = new ParseFile(fileName, fileBytes);
			message.put(ParseConstants.KEY_FILE, file);

			return message;
		}
	}
	
	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		for (int i = 0; i < getListView().getCount(); i++) {
			if (getListView().isItemChecked(i)) {
				recipientIds.add(mFriendsFound.get(i).getObjectId());
			}
		}
		
		return recipientIds;
	}
	
	protected ArrayList<String> getRecipientNames() {
		ArrayList<String> recipientNames = new ArrayList<String>();
		for (int i = 0; i < getListView().getCount(); i++) {
			if (getListView().isItemChecked(i)) {
				recipientNames.add(mFriendsFound.get(i).getUsername());
			}
		}
		
		return recipientNames;
	}
	
	protected ArrayList<String> getPushRecipientIds() {
		ArrayList<String> pushRecipientIds = new ArrayList<String>();
		for (int i = 0; i < getListView().getCount(); i++) {
			if (getListView().isItemChecked(i)) {
				pushRecipientIds.add("user_" + mFriendsFound.get(i).getObjectId());
			}
		}
		
		return pushRecipientIds;
	}
	
	protected void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// success!
					Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
					
					String pushMessage = ParseUser.getCurrentUser().getUsername() + " sent you a YourAppName " + mFileType + "!";
					
					// Notification for Android & iOS users
					JSONObject data = new JSONObject();
					try {
						data.put("action", "com.developer.appname.android.UPDATE_MESSAGES");
						data.put("alert", pushMessage);
                    	data.put("badge", "Increment");
                    	data.put("sound", "shutterClick.wav");
                       
                    	ParsePush push = new ParsePush();
                    	push.setChannels(getPushRecipientIds()); // Notice we use setChannels not setChannel
                    	push.setData(data);
                    	push.sendInBackground();

                    } catch (JSONException e1) {
						e1.printStackTrace();
				    }
					
					// System.out.println(getRecipientNames());
				}
				else {

                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(RecipientsActivity.this);
                    qustomDialogBuilder.setMessage(R.string.error_sending_message)
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






