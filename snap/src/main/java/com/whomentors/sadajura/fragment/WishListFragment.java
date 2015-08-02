package com.whomentors.sadajura.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.whomentors.sadajura.data.CJWishListModel;
import com.whomentors.sadajura.ui.recyclerview.CJListAdapter;
import com.whomentors.sarajura.R;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */

public class WishListFragment extends Fragment {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ACTIVITY VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.

    // LIST VARIABLES
    private ArrayList<CJWishListModel> wishListResult = new ArrayList<>();

    // LOGGING VARIABLES
    private static final String LOG_TAG = WishListFragment.class.getSimpleName();

    // VIEW INJECTION VARIABLES
    @Bind(R.id.wish_list_recycler_view) RecyclerView wishListView;

    /** INITIALIZATION METHODS _________________________________________________________________ **/

    // WishListFragment(): Default constructor for the WishListFragment fragment class.
    private final static WishListFragment wishlist_fragment = new WishListFragment();

    // WishListFragment(): Deconstructor method for the WishListFragment fragment class.
    public WishListFragment() {}

    // getInstance(): Returns the artists_fragment instance.
    public static WishListFragment getInstance() { return wishlist_fragment; }

    /** FRAGMENT LIFECYCLE METHODS _____________________________________________________________ **/

    // onAttach(): The initial function that is called when the Fragment is run. The activity is
    // attached to the fragment.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.currentActivity = activity; // Sets the currentActivity to attached activity object.
    }

    // onCreateView(): Creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View wishListView = (ViewGroup) inflater.inflate(R.layout.fragment_wishlist, container, false);
        ButterKnife.bind(this, wishListView); // ButterKnife view injection initialization.

        setUpLayout(); // Sets up the layout for the fragment.

        return wishListView;
    }

    // onDestroyView(): This function runs when the screen is no longer visible and the view is
    // destroyed.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this); // Sets all injected views to null.
    }

    // onDetach(): This function is called immediately prior to the fragment no longer being
    // associated with its activity.
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the fragment.
    private void setUpLayout() {
        setUpRecyclerView(); // Sets up the RecyclerView object.
        setListAdapter(wishListResult); // Sets the adapter for the RecyclerView object.
    }

    /** RECYCLERVIEW METHODS ___________________________________________________________________ **/

    // setListAdapter(): Sets the recycler list adapter based on the artistList.
    private void setListAdapter(ArrayList<CJWishListModel> wishList){
        CJListAdapter adapter = new CJListAdapter(wishList, true, currentActivity);
        wishListView.setAdapter(adapter);
    }

    // setUpRecyclerView(): Sets up the RecyclerView object.
    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(currentActivity);
        wishListView.setLayoutManager(layoutManager);
    }
}