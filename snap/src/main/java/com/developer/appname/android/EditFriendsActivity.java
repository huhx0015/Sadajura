package com.developer.appname.android;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class EditFriendsActivity extends ListActivity {
	
	public static final String TAG = EditFriendsActivity.class.getSimpleName();
	
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	protected List<ParseObject> mRequests;
	
	private ArrayList<ParseUser> mAllUsers;
	private ArrayList<ParseUser> mSearchedUsers;
	private ParseQuery<ParseUser> searchQuery;
	private ParseQuery<ParseUser> userQuery;
	private ParseQuery<ParseUser> queries;
	private EditFriendsAdapter editFriendsListAdapter;
	
	private int limit;
	private int skip;
    private View loadMoreView;
    private TextView footerText;
    private SearchView searchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_friends);

        // Show the Up button in the action bar.
        setupActionBar();

		mAllUsers = new ArrayList<ParseUser>();
		mSearchedUsers = new ArrayList<ParseUser>();

        loadMoreView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.load_more, null, false);
        footerText = (TextView) loadMoreView.findViewById(R.id.loadMore);

        getListView().addFooterView(loadMoreView);

		editFriendsListAdapter = new EditFriendsAdapter();

        loadMoreView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (queries == null)
                {
                    // System.out.println("queries == null");
                }
                else
                {
                    // System.out.println("queries == searchQuery");

                    queries = null;

                    mAllUsers.clear();

                    skip = 0;

                    if(searchView != null)
                    {
                        searchView.clearFocus();
                    }
                }

                updateFriendsList();
            }
        });

        updateFriendsList();

        handleSearch(getIntent());
    }
	
	public class EditFriendsAdapter extends BaseAdapter {

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
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			if(arg1==null)
			{
				LayoutInflater inflater = (LayoutInflater) EditFriendsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				arg1 = inflater.inflate(R.layout.edit_friends_item, arg2, false);
			}
			
			final ParseImageView profileImageView = (ParseImageView) arg1.findViewById(R.id.profileImage);
			TextView username = (TextView)arg1.findViewById(R.id.senderLabel);
			
			ParseUser user = parseUserList.get(arg0);

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
	
	@Override
	protected void onNewIntent(Intent intent) {
	        
		setIntent(intent);
	    handleSearch(intent);
	}
	 
	private void handleSearch(Intent intent) {
	        
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	            String query = intent.getStringExtra(SearchManager.QUERY);
	 
	            setProgressBarIndeterminateVisibility(true);
	            
	            searchQuery = ParseUser.getQuery();
	            searchQuery.addAscendingOrder(ParseConstants.KEY_USERNAME);
	            searchQuery.whereContains(ParseConstants.KEY_USERNAME, query);
	            searchQuery.setLimit(200);
	            queries = searchQuery;
	            searchQuery.findInBackground(new FindCallback<ParseUser>() {
	    				
	    			@Override
	    			public void done(List<ParseUser> users, ParseException e) {
	    				setProgressBarIndeterminateVisibility(false);
	    					
	    				if (e == null) {

                                mSearchedUsers.clear();

                                mSearchedUsers.addAll(users);
	    						
	    						mAllUsers.clear();
	    					
	    						mAllUsers.addAll(mSearchedUsers);

                                footerText.setText("Cancel search");
	    					
	    						setListAdapter(editFriendsListAdapter);

                                if(searchView != null)
                                {
                                    searchView.clearFocus();
                                }
	    					 }
	    			   }
	           });
	     }
	}
	
	private void updateFriendsList() {
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
   		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
		query.findInBackground(new FindCallback<ParseObject>() {
		    
			@Override
			public void done(List<ParseObject> requests, ParseException e) {
		        
				if (e == null) {
		           
		        	// Success
					mRequests = requests;
				}
			 }
		});
		
		setProgressBarIndeterminateVisibility(true);
		
		limit = 25;
		
		userQuery = ParseUser.getQuery();
		userQuery.orderByAscending(ParseConstants.KEY_USERNAME);
		userQuery.setLimit(limit);
		userQuery.setSkip(skip);
		userQuery.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> users, ParseException e) {
				
				if (e == null) {
					// Success
					
					if (queries == null)
					{
						if(users.size() == 0)
						{
							setProgressBarIndeterminateVisibility(false);
						}
						
						mAllUsers.addAll(users);
						if (users.size() == limit || users.size() > 1) {
						      
							  // There might be more objects in the table. Update the skip value and execute the query again.
							  // System.out.println("EditFriendsActivity Skip Count = " + skip);
						      
						      userQuery.cancel();
						      userQuery.findInBackground(new FindCallback<ParseUser>() {
						    			
						    	  @Override
						    	  public void done(List<ParseUser> users, ParseException e) { // Execute the query until all objects have been returned. Keep adding the results to the allObjects mutable array.
						    		  setProgressBarIndeterminateVisibility(false);
						    		  
						    		  if (e == null) {
						    			  
						    			  skip += limit;
						    			  userQuery.setSkip(skip);

                                          footerText.setText("Load more");

						    			  setListAdapter(editFriendsListAdapter);

						    			  if (mAllUsers.size() > 25)
						    			  {
                                              scrollListViewToNext();
						    			  }
						    		  }
						    	  }
						    });
						}
					}
				}
				else {
					
		            QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(EditFriendsActivity.this);
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

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

    private void scrollListViewToNext() {
        getListView().post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                getListView().setSelection(getListView().getAdapter().getCount() - 26);
            }
        });
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_friends, menu);
		
	    // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.BLACK); // set the text color
        searchEditText.setHintTextColor(Color.GRAY); // set the hint color


        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.append("username");
        Drawable searchIcon = getResources().getDrawable(R.drawable.ic_search_open);
        int textSize = (int) (searchEditText.getTextSize() * 1.25);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchEditText.setHint(ssb);

        int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        searchPlate.setBackgroundResource(R.drawable.search_text_background);

        int searchOpenId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchOpenIcon = (ImageView) searchView.findViewById(searchOpenId);
        searchOpenIcon.setImageResource(R.drawable.ic_search_open);

        int searchCloseId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView searchCloseIcon = (ImageView) searchView.findViewById(searchCloseId);
        searchCloseIcon.setBackgroundColor(Color.TRANSPARENT);
        searchCloseIcon.setImageResource(R.drawable.ic_search_close);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
			case android.R.id.home:
	
				this.finish();

			break;
		}
			
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final ParseUser friendPosition = mAllUsers.get(position);
		
		Intent intent = new Intent(this, ProfileViewActivity.class);
		intent.putExtra("selectedUserObjectId", friendPosition.getObjectId());
		intent.putExtra("selectedUsername", friendPosition.getUsername());
		startActivity(intent);
	}
}










