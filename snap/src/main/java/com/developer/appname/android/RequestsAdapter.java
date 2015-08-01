package com.developer.appname.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

public class RequestsAdapter extends ArrayAdapter<ParseObject> {
	
	protected Context mContext;
	protected List<ParseObject> mRequests;
	
	public RequestsAdapter(Context context, List<ParseObject> requests) {
		super(context, R.layout.requests_item, requests);
		mContext = context;
		mRequests = requests;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.requests_item, null);
			holder = new ViewHolder();
			holder.profileImageView = (ParseImageView) convertView.findViewById(R.id.profileImage);
			holder.nameLabel = (TextView)convertView.findViewById(R.id.senderLabel);
			holder.nameLabel2 = (TextView)convertView.findViewById(R.id.requestTime);
			convertView.setTag(holder);
		}
		else {

            holder = (ViewHolder)convertView.getTag();
		}
		
		ParseObject request = mRequests.get(position);
		
		ParseFile profileImage = request.getParseFile("profileImage");

        if (profileImage != null) {

            Picasso.with(mContext).load(profileImage.getUrl()).into(holder.profileImageView);
        }
        else
        {
            holder.profileImageView.setImageResource(R.drawable.ic_profile);
        }
		
		Date date = request.getCreatedAt();
		
		String timestamp = new SimpleDateFormat("EEE MMM dd, yyyy, hh:mm a", Locale.US).format(date);
		
		holder.nameLabel.setText(request.getString(ParseConstants.KEY_SENDER_NAME));
		
		System.out.println(timestamp);
		
	    holder.nameLabel2.setText(timestamp);
		
		return convertView;
	}
	
	private static class ViewHolder {
		ParseImageView profileImageView;
		TextView nameLabel;
		TextView nameLabel2;
	}
	
	public void refill(List<ParseObject> requests) {
		
		// System.out.println("Request Refill");
		
		mRequests.clear();
		mRequests.addAll(requests);
		notifyDataSetChanged();
	}
	
	public void clear(List<ParseObject> requests) {
		
		// System.out.println("Request Clear");
		
		mRequests.clear();
		notifyDataSetChanged();
	}
}






