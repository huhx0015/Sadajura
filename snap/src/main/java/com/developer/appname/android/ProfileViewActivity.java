package com.developer.appname.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class ProfileViewActivity extends ListActivity {

	public static final String TAG = ProfileViewActivity.class.getSimpleName();
	
	protected List<ParseUser> mFriends;
	protected List<ParseUser> mOtherFriends;
	protected List<ParseObject> mRequests;
	protected List<ParseUser> mSelectedUser;
	protected ParseUser mCurrentUser;
	protected ParseRelation<ParseUser> mRecipientsRelation;
	protected ParseRelation<ParseUser> mFriendsRelation;
	
	private ArrayList<ParseUser> mRequestsFound;
	private ArrayList<ParseUser> mFriendsFound;
	private ArrayList<ParseUser> mCurrentFriends;
	private ArrayList<ParseUser> mAllUsers;
	private String selectedUserObjectId;
	
	private ParseImageView profileImageView;
	private TextView userName;
	private TextView userNameCell;
	private TextView friendStatus;
	private TextView activeStatus;
	private ImageView friendRequest;
	protected MenuItem mRemoveMenuItem;
	protected MenuItem mAddMenuItem;
	
	private ProfileViewAdapter profileViewListAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        
		// Show the Up button in the action bar.
		setupActionBar();

        ActionBar ab = getActionBar();
		ab.setTitle("Profile");
		
		mFriendsFound = new ArrayList<ParseUser>();
		mRequestsFound = new ArrayList<ParseUser>();
		mCurrentFriends = new ArrayList<ParseUser>();
		mAllUsers = new ArrayList<ParseUser>();
		
		mFriendsFound.clear();
		mRequestsFound.clear();
		mCurrentFriends.clear();
		mAllUsers.clear();
		
        profileViewListAdapter = new ProfileViewAdapter();
        
        updateFriendsList();
    }
    
	public class ProfileViewAdapter extends BaseAdapter {

		List<ParseUser> parseUserList = mAllUsers;
		
    	public int getCount() {
			return parseUserList.size();
		}

		@Override
		public ParseUser getItem(int arg0) {
			return parseUserList.get(arg0);
		}
		
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		
		@Override
		public boolean isEnabled(int position) {
			   
			ParseUser friendPosition = parseUserList.get(position);
			
			   if (mRequestsFound.contains(friendPosition) || mFriendsFound.contains(friendPosition)) {
		            
					  return false;
			       }
				         
			    return true;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			if(arg1==null)
			{
				LayoutInflater inflater = (LayoutInflater) ProfileViewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				arg1 = inflater.inflate(R.layout.friends_profile_item, arg2, false);
			}
			
			userNameCell = (TextView)arg1.findViewById(R.id.senderLabel);
			friendStatus = (TextView)arg1.findViewById(R.id.statusLabel);
			friendRequest = (ImageView)arg1.findViewById(R.id.requestIcon);
			
			final ParseUser selectedUser = parseUserList.get(arg0);
			
			userNameCell.setText(selectedUser.getUsername());
			
			friendStatus.setText("Tap to send a friend request");
			friendRequest.setImageResource(R.drawable.ic_sendrequest);
			
    		mCurrentUser = ParseUser.getCurrentUser();
    		mRecipientsRelation = selectedUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
    		
    		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
    		query.findInBackground(new FindCallback<ParseObject>() {
    		    
    			@Override
    			public void done(List<ParseObject> requests, ParseException e) {
    		        
    				if (e == null) {
    		           
    		        	// Success
    					mRequests = requests;
    					
    					mRecipientsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
    					
    	        				@Override
    	        				public void done(List<ParseUser> otherFriends, ParseException e) {
    					
    	        					if (e == null)
    	        					{ 
    	        						mOtherFriends = otherFriends;
    	        						
    	        						for (ParseUser friend : mOtherFriends) {
    	        							
    	        							if (friend.getObjectId().equals(mCurrentUser.getObjectId()))
    	        							{	
    	        								mFriendsFound.add(selectedUser);
    	        								
    	        								System.out.println("mFriendsFound = " + selectedUser.getUsername());
    	        							}
    	        						}
    	        					}
    	        					
    	        					if (mFriendsFound.contains(selectedUser))
        							{
    	        						if (isFriend(selectedUser))
    	        						{
    	        							friendStatus.setText("Mutual friends! Have fun!");
        	        						friendRequest.setImageDrawable(null);
        	        						
        	        						mRemoveMenuItem.setVisible(true);
    	        						}
    	        						else
    	        						{
    	        							friendStatus.setText("Added you as a friend. Add to make it mutual.");
        	        						friendRequest.setImageDrawable(null);
        	        						
        	        						mAddMenuItem.setVisible(true);
    	        						}
    	        					}
    	        					else if (isPending(selectedUser))
    	    						{
    	    							friendStatus.setText("Friend request pending.");
    	    							friendRequest.setImageDrawable(null);
    	    							
    	    							mRemoveMenuItem.setVisible(true);
    	    						}
    	    						else
    	    						{
    	    							if (isFriend(selectedUser))
    	    							{
    	    								friendStatus.setText("Tap to send a friend request.");
        	    							friendRequest.setImageResource(R.drawable.ic_sendrequest);
        	    							
        	    							mRemoveMenuItem.setVisible(true);
    	    							}
    	    							else
    	    							{
    	    								friendStatus.setText("Tap to send a friend request.");
        	    							friendRequest.setImageResource(R.drawable.ic_sendrequest);
    	    							}
    	    						}
    	        				}
    	        		   });
    		          }
    			 }
    		});
			
			return arg1;
		}
	}
    
	private void updateFriendsList() {
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        selectedUserObjectId = getIntent().getExtras().getString("selectedUserObjectId");
        
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			try {
				query.get(selectedUserObjectId);
			} catch (ParseException e1) {
				
				e1.printStackTrace();
			}
			query.findInBackground(new FindCallback<ParseUser>() {
			  public void done(List<ParseUser> user, ParseException e) {
			    if (e == null) {
			        
			    	// Success
			    	mSelectedUser = user;
			    	
			    	for (final ParseUser selectedUser : mSelectedUser) {
			    	
				        profileImageView = (ParseImageView) findViewById(R.id.profileImage);

                        ParseFile profileImage = selectedUser.getParseFile("profileImage");

                        if (profileImage != null) {

                            Picasso.with(getApplicationContext()).load(profileImage.getUrl()).into(profileImageView);
                        }
                        else
                        {
                            profileImageView.setImageResource(R.drawable.ic_profile_main);
                        }

						Date date = selectedUser.getUpdatedAt();
						String timestamp = new SimpleDateFormat("EEE MMM dd, yyyy, hh:mm a", Locale.US).format(date);
						
						userName = (TextView) findViewById(R.id.userName);
						userName.setText(selectedUser.getUsername());
						
						activeStatus = (TextView) findViewById(R.id.activeLabel);
						activeStatus.setText("Last active on " + timestamp);
						
				    	mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
							@Override
							public void done(List<ParseUser> friends, ParseException e) {
								if (e == null) {
									
									mFriends = friends;
								}
								else {
									
									Log.e(TAG, e.getMessage());
								}
							}
						});
			    	}
			    	
			    	mAllUsers.addAll(user);
			    	
			    	setListAdapter(profileViewListAdapter);
			    }
			 }	
		});
	}
	
 	public boolean isFriend(ParseUser user) {
		   
 		try {
 			
 			for (ParseUser friend : mFriends) {
 				
 				if (friend.getObjectId().equals (user.getObjectId()))
 			    {
 					mCurrentFriends.add(friend);
 					
 					System.out.println("mCurrentFriends = " + friend.getUsername());
 					
 					return true;
 				}
 			}
 			   
 			return false;
 			
 		} catch (Throwable e) {
 		    
 			System.out.println("There was a problem determining isFriend");
 		}
 	
 		return false;
	}
 	
 	public boolean isPending(ParseUser user) {
		
 		try {
 			
 			for (ParseObject request : mRequests) {
 				
 				if (request.getString(ParseConstants.KEY_RECIPIENT_ID).equals (user.getObjectId()) && request.getString(ParseConstants.KEY_SENDER_ID).equals (mCurrentUser.getObjectId()))
 	    		{
 	    			mRequestsFound.add(user);
 	    			
 	    			return true;
 	    		}
 	    	}
 			   
 			return false;
 			
 		} catch (Throwable e) {
 		    
 			System.out.println("There was a problem determining isPending");
 		}
		
 		return false;
	}
 	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_profile, menu);
		
		mRemoveMenuItem = menu.findItem(R.id.action_remove);
		mAddMenuItem = menu.findItem(R.id.action_add);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			
			case android.R.id.home:
				
				this.finish();
		        
			break;    
		    case R.id.action_remove:
				
				for (ParseUser selectedUser : mSelectedUser) {
					
					for (ParseObject request : mRequests) {
						
						if (request.getString(ParseConstants.KEY_RECIPIENT_ID).equals (selectedUser.getObjectId()) && request.getString(ParseConstants.KEY_SENDER_ID).equals (mCurrentUser.getObjectId()))
			    		{
							// System.out.println("Friend Request Found! Lets Remove It!");
							
							request.deleteInBackground();
			    		}
					}
					
					mFriendsRelation.remove(selectedUser);
					
					System.out.println("Remove Friend");
				}
				
				mCurrentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							
							finish();
						}
					}
				});
				
			break;
			case R.id.action_add:
				
				for (ParseUser selectedUser : mSelectedUser) {
					
					mFriendsRelation.add(selectedUser);
					
					System.out.println("Add Friend");
				}
				
				mCurrentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							
							finish();
						}
					}
				});
				
			break;
		}
			
		return super.onOptionsItemSelected(item);
	}
	
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final ParseUser friendPosition = mSelectedUser.get(position);
		
		String requestMessage = "Send " + friendPosition.getUsername() + " a friend request so you can share YourAppName messages.";

        QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(getListView().getContext());
        qustomDialogBuilder.setMessage(requestMessage)
                .setIcon(R.drawable.ic_profile)
                .setTitle("Send Friend Request?")
                .setTitleColor("#000000")
                .setDividerColor("#10f8b7")
		        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                })
			    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					
					if (isPending(friendPosition) || mFriendsFound.contains(friendPosition))
					{
						Toast.makeText(ProfileViewActivity.this, R.string.already_requested, Toast.LENGTH_LONG).show();
						
						// System.out.println("Friend Request Already Pending");
						
						return;
					}
					
					ParseFile profileImage = ParseUser.getCurrentUser().getParseFile("profileImage");
					ParseObject message = new ParseObject(ParseConstants.CLASS_REQUESTS);
					message.put(ParseConstants.KEY_RECIPIENT_ID, friendPosition.getObjectId());
					message.put(ParseConstants.KEY_RECIPIENT_NAME, friendPosition.getUsername());
					message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
					message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
					if (profileImage != null) {
						message.put("profileImage", profileImage);
					}
					
					message.saveInBackground(new SaveCallback() {
						   public void done(ParseException e) {
						     if (e == null) {
						    	 
						    	 	String pushMessage = ParseUser.getCurrentUser().getUsername() + " sent you a friend request!";

									// Notification for Android & iOS users
									JSONObject data = new JSONObject();
									try {
										data.put("action", "com.developer.appname.android.UPDATE_REQUESTS");
										data.put("alert", pushMessage);
				                    	data.put("badge", "Increment");
				                    	data.put("sound", "shutterClick.wav");
				                       
				                    	ParsePush push = new ParsePush();
				                    	push.setChannel("user_" + friendPosition.getObjectId()); // Notice we use setChannel not setChannels
				                    	push.setData(data);
				                    	push.sendInBackground();
				                    	
					                } catch (JSONException e1) {
					                	
											e1.printStackTrace();
					                }


                                    QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(ProfileViewActivity.this);
                                    qustomDialogBuilder.setMessage("Your request has been sent!")
                                            .setIcon(R.drawable.ic_happy_face)
                                            .setTitle("Request Status!")
                                            .setTitleColor("#000000")
                                            .setDividerColor("#10f8b7")
									        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface dialog, int id) {

                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            finish();
                                                        }

                                                    }, 400);

                                                }
                                            });

                                 AlertDialog customDialog = qustomDialogBuilder.create();
                                 customDialog.show();

                                 Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                 if(positiveBtn != null)
                                     positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
									
									friendStatus.setText("friend request pending");
									friendRequest.setImageDrawable(null);
									
									profileViewListAdapter.notifyDataSetChanged();
									
									for (ParseUser selectedUser : mSelectedUser) {
										
										mFriendsRelation.add(selectedUser);
									}
									
									mCurrentUser.saveInBackground(new SaveCallback() {
										@Override
										public void done(ParseException e) {
											if (e == null) {
											
												mRemoveMenuItem.setVisible(true);
											}
										}
									});
									
							} else {
						    	 
							     Log.e(TAG, e.getMessage());

                                 QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(ProfileViewActivity.this);
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
