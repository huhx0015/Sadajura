package com.developer.appname.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class FriendsAdapter extends ArrayAdapter<ParseUser> {
	
	public static final String TAG = FriendsFragment.class.getSimpleName();
	
	protected Context mContext;
	protected List<ParseUser> mFriends;
	
	public FriendsAdapter(Context context, List<ParseUser> friends) {
		super(context, R.layout.friends_item, friends);
		mContext = context;
		mFriends = friends;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.friends_item, null);
			holder = new ViewHolder();
			holder.profileImageView = (ParseImageView) convertView.findViewById(R.id.profileImage);
			holder.nameLabel = (TextView)convertView.findViewById(R.id.senderLabel);
			
			convertView.setTag(holder);
		}
		else {
			
			holder = (ViewHolder)convertView.getTag();
		}
		
		final ParseUser friendPosition = mFriends.get(position);

        ParseFile profileImage = friendPosition.getParseFile("profileImage");

        if (profileImage != null) {

            Picasso.with(mContext).load(profileImage.getUrl()).into(holder.profileImageView);
        }
        else
        {
            holder.profileImageView.setImageResource(R.drawable.ic_profile);
        }

		holder.nameLabel.setText(friendPosition.getUsername());
		
		return convertView;
	}
	
	private static class ViewHolder {
		ParseImageView profileImageView;
		TextView nameLabel;
	}
	
	public void refill(List<ParseUser> friends) {
		
		// System.out.println("Friends Refill");
		
		mFriends.clear();
		mFriends.addAll(friends);
		
		notifyDataSetChanged();
	}
}






