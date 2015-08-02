package com.whomentors.sadajura.ui.recyclerview;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.whomentors.sadajura.data.SJWishListModel;
import com.whomentors.sarajura.R;
import java.util.ArrayList;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */

public class SJListAdapter extends RecyclerView.Adapter<SJListAdapter.CJResultViewHolder> {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ACTIVITY VARIABLES
    private Activity currentActivity; // References the attached activity.

    // LAYOUT VARIABLES:
    private Boolean isClickable = true; // Used to determine if the items are clickable or not.

    // LIST VARIABLES
    private ArrayList<SJWishListModel> listResult;

    // LOGGING VARIABLES
    private static final String LOG_TAG = SJListAdapter.class.getSimpleName();

    /** INITIALIZATION METHODS _________________________________________________________________ **/

    // CJListAdapter(): Constructor method for CJListAdapter.
    public SJListAdapter(ArrayList<SJWishListModel> list, Boolean clickable, Activity act){
        this.currentActivity = act;
        this.isClickable = clickable;
        this.listResult = list;
    }

    /** EXTENSION METHODS ______________________________________________________________________ **/

    // onCreateViewHolder: This method is called when the custom ViewHolder needs to be initialized.
    // The layout of each item of the RecyclerView is inflated using LayoutInflater, passing the
    // output to the constructor of the custom ViewHolder.
    @Override
    public CJResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflates the layout given the XML layout file for the item view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_card, parent, false);

        // Sets the action if the RecyclerView item property is set to be clickable.
        if (isClickable) {

            // Sets the view holder for the item view. This is needed to handle the individual item
            // clicks.
            final CJResultViewHolder viewHolder = new CJResultViewHolder(view, new CJResultViewHolder.OnResultViewHolderClick() {

                // onItemClick(): Defines an action to take when the item in the list is clicked.
                @Override
                public void onItemClick(View caller, int position) {

                }
            });

            return viewHolder;
        }

        return new CJResultViewHolder(view, null);
    }

    // onBindViewHolder(): Overrides the onBindViewHolder to specify the contents of each item of
    // the RecyclerView. This method is similar to the getView method of a ListView's adapter.
    @Override
    public void onBindViewHolder(CJResultViewHolder holder, int position) {

        // Sets the song, album, and artist name into the TextView objects.
        holder.wishListTitle.setText(listResult.get(position).getWish_list_title());
        holder.wishListPrice.setText(listResult.get(position).getWish_list_price());
        holder.wishListDescription.setText(listResult.get(position).getWish_list_description());

        // Loads the image into the ImageView object.
        Picasso.with(currentActivity)
                .load(listResult.get(position).getWishImage())
                .into(holder.wishListImage);
    }

    // getItemCount(): Returns the number of items present in the data.
    @Override
    public int getItemCount() {
        return listResult.size();
    }

    // onAttachedToRecyclerView(): Overrides the onAttachedToRecyclerView method.
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /** SUBCLASSES _____________________________________________________________________________ **/

    /**
     * --------------------------------------------------------------------------------------------
     * [CJResultViewHolder] CLASS
     * DESCRIPTION: This subclass is responsible for referencing the view for an item in the
     * RecyclerView list view object.
     * --------------------------------------------------------------------------------------------
     */
    public static class CJResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /** SUBCLASS VARIABLES _________________________________________________________________ **/

        // LAYOUT VARIABLES:
        CardView wishCardView;
        ImageView wishListImage;
        TextView wishListTitle;
        TextView wishListDescription;
        TextView wishListPrice;

        // LISTENER VARIABLES
        public OnResultViewHolderClick resultItemListener; // Interface on-click listener variable.

        /** SUBCLASS METHODS ___________________________________________________________________ **/

        CJResultViewHolder(View itemView, OnResultViewHolderClick listener) {

            super(itemView);

            // Sets the references for the View objects in the adapter layout.
            wishCardView = (CardView) itemView.findViewById(R.id.wish_list_cardview_container);
            wishListImage = (ImageView) itemView.findViewById(R.id.wish_list_album_image);
            wishListTitle = (TextView) itemView.findViewById(R.id.wish_list_name_text);
            wishListDescription = (TextView) itemView.findViewById(R.id.wish_list_description_text);
            wishListPrice = (TextView) itemView.findViewById(R.id.wish_list_price_text);

            // Sets the listener for the item view.
            if (listener != null) {
                resultItemListener = listener; // Sets the OnResultViewHolderClick listener.
                itemView.setOnClickListener(this);
            }
        }

        // onClick(): Defines an action to take when an item is clicked.
        @Override
        public void onClick(View v) {

            int itemPos = getAdapterPosition(); // Retrieves the clicked item position.
            resultItemListener.onItemClick(v, itemPos); // Sets the item listener.
        }

        /** INTERFACE METHODS __________________________________________________________________ **/

        /**
         * -----------------------------------------------------------------------------------------
         * [OnResultViewHolderClick] INTERFACE
         * DESCRIPTION: This is an interface subclass that is used to provide methods to call when
         * the RecyclerView items are clicked.
         * -----------------------------------------------------------------------------------------
         */
        public interface OnResultViewHolderClick {

            // onItemClick(): The method that is called when an item in the RecyclerView is clicked.
            void onItemClick(View caller, int position);
        }
    }
}