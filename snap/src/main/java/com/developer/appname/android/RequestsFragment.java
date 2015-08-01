package com.developer.appname.android;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RequestsFragment extends ListFragment {

	public static final String TAG = RequestsFragment.class.getSimpleName();
	
	protected List<ParseObject> mRequests;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected List<ParseUser> mRequestedUser;
	protected ParseUser mCurrentUser;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_requests,
				container, false);
		
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver,
			      new IntentFilter("updateRequests"));
		
		return rootView;
	}
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    
			  updateRequestsList();
			  
		     Log.d("receiver", "Update Requests Received");
		  }
	};
	
	@Override
	public void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  // This is somewhat like [[NSNotificationCenter defaultCenter] removeObserver:name:object:] 
	  LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
	  
	  super.onDestroy();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		updateRequestsList();
	}
	
	private void updateRequestsList() {
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId());
		query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
		query.findInBackground(new FindCallback<ParseObject>() {
				
			@Override
			public void done(List<ParseObject> requests, ParseException e) {
				
				if (e == null) {
					
						 // We found requests!
						 mRequests = requests;
						
						 if (isFragmentUIActive())
						 {
								if (getListView().getAdapter() == null) {
									RequestsAdapter adapter = new RequestsAdapter(
											getListView().getContext(), 
											mRequests);
									setListAdapter(adapter);
								}
								else {
									// refill the adapter!
									((RequestsAdapter)getListView().getAdapter()).refill(mRequests);
								}
						   }
					 }
				}
		  });
	}
	
	public boolean isFragmentUIActive() {
	    return isAdded() && isVisible() && !isDetached() && !isRemoving();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		
				final ParseObject request = mRequests.get(position);
				
				// System.out.println(request.getString(ParseConstants.KEY_SENDER_ID));
				
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				try {
					query.get(request.getString(ParseConstants.KEY_SENDER_ID));
				} catch (ParseException e1) {
					
					e1.printStackTrace();
				}
				query.findInBackground(new FindCallback<ParseUser>() {
				  public void done(List<ParseUser> user, ParseException e) {
				    if (e == null) {
				        // Success
				    	mRequestedUser = user;
				    	
				    	// System.out.println(mRequestedUser);
				    }
				}
			});
		
		String requestMessage = "Add " + request.getString(ParseConstants.KEY_SENDER_NAME) + " as a confirmed friend to share YourAppName messages.";

        QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(getListView().getContext());
        qustomDialogBuilder.setMessage(requestMessage)
            .setIcon(R.drawable.ic_profile)
			.setTitle("Add Friend?")
            .setTitleColor("#000000")
            .setDividerColor("#10f8b7")
			.setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
	
					dialog.cancel();
				}
			})
			.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    request.deleteInBackground();

                    ((RequestsAdapter) getListView().getAdapter()).clear(mRequests);

                    mCurrentUser = ParseUser.getCurrentUser();

                    mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

                    mFriendsRelation.add(mRequestedUser.get(position));

                    mCurrentUser.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {

                                String pushMessage = "Whoo hoo! " + mCurrentUser.getUsername() + " just accepted your friend request!";

                                // Notification for Android & iOS users
                                JSONObject data = new JSONObject();
                                try {
                                    data.put("alert", pushMessage);
                                    data.put("badge", "Increment");
                                    data.put("sound", "shutterClick.wav");

                                    ParsePush push = new ParsePush();
                                    push.setChannel("user_" + mRequestedUser.get(position).getObjectId()); // Notice we use setChannel not setChannels
                                    push.setData(data);
                                    push.sendInBackground();

                                } catch (JSONException e1) {

                                    e1.printStackTrace();
                                }

                                QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(getListView().getContext());
                                qustomDialogBuilder.setMessage("You can now send and receive YourAppName messages with " + request.getString(ParseConstants.KEY_SENDER_NAME) + ".")
                                        .setIcon(R.drawable.ic_happy_face)
                                        .setTitle("Add Friend?")
                                        .setTitleColor("#000000")
                                        .setDividerColor("#10f8b7")
                                        .setTitle("Added!")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int id) {

                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        updateRequestsList();
                                                    }

                                                }, 1000);
                                            }
                                        });

                                AlertDialog customDialog = qustomDialogBuilder.create();
                                customDialog.show();

                                Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                if(positiveBtn != null)
                                    positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
                            }
                        }
                    });
                }
            })
			.setNeutralButton("Delete Request", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    request.deleteInBackground();

                    ((RequestsAdapter) getListView().getAdapter()).clear(mRequests);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateRequestsList();
                        }

                    }, 1000);
                }
            });

        AlertDialog customDialog = qustomDialogBuilder.create();
        customDialog.show();

        Button negativeBtn = customDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(negativeBtn != null)
            negativeBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

        Button positiveBtn = customDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(positiveBtn != null)
            positiveBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));

        Button neutralBtn = customDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if(neutralBtn != null)
            neutralBtn.setBackground(getResources().getDrawable(R.drawable.list_item_selector));
    }
}








