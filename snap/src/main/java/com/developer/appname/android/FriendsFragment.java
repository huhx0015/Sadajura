package com.developer.appname.android;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class FriendsFragment extends ListFragment {
	
	public static final String TAG = FriendsFragment.class.getSimpleName();

	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	protected List<ParseUser> mFriends;
	
	private ArrayList<ParseUser> mAllFriends;
	private ParseQuery<ParseUser> friendsQuery;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friends,
				container, false);
		
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver,
			      new IntentFilter("updateFriends"));
		
		return rootView;
	}
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    
			 updateFriendsList();
			  
		     Log.d("receiver", "Update Friends Received");
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
		
		mAllFriends = new ArrayList<ParseUser>();
		mAllFriends.clear();
		
		updateFriendsList();
	}
	
	private void updateFriendsList() {
	
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		friendsQuery = mFriendsRelation.getQuery();
		friendsQuery.addAscendingOrder(ParseConstants.KEY_USERNAME);
	    friendsQuery.findInBackground(new FindCallback<ParseUser>() {
				
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				
				if (e == null) {
					
					  mFriends = friends;
					
	    			  if (isFragmentUIActive())
					  {
	    				  if (getListView().getAdapter() == null) {
								FriendsAdapter adapter = new FriendsAdapter(
										getListView().getContext(), 
										mFriends);
								setListAdapter(adapter);
							}
							else {
								
								((FriendsAdapter)getListView().getAdapter()).refill(mFriends);
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
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final ParseUser friendPosition = mFriends.get(position);
		
		Intent intent = new Intent(getActivity(), ProfileViewActivity.class);
		intent.putExtra("selectedUserObjectId", friendPosition.getObjectId());
		intent.putExtra("selectedUsername", friendPosition.getUsername());
		startActivity(intent);
	}
}