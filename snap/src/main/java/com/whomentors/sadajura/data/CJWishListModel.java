package com.whomentors.sadajura.data;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */

public class CJWishListModel {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    private String wish_list_image;
    private String wish_list_description;
    private String wish_list_price;
    private String wish_list_title;
    private String origin;
    private String destination;

    /** INITIALIZATION METHODS _________________________________________________________________ **/

    // SSSpotifyModel(): Constructor method for the class.
    public CJWishListModel(String wish_list_description, String wish_list_price, String wish_list_title, String ori, String dest, String image) {
        this.wish_list_price = wish_list_price;
        this.wish_list_image = image;
        this.wish_list_description = wish_list_description;
        this.wish_list_title = wish_list_title;
        this.origin = ori;
        this.destination = dest;
    }

    /** GET / SET METHODS ______________________________________________________________________ **/

    public String getWishImage() { return wish_list_image; }

    public String getWish_list_description() {
        return wish_list_description;
    }

    public String getWish_list_price() {
        return wish_list_price;
    }

    public String getWish_list_title() {
        return wish_list_title;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setWishImage(String image) { this.wish_list_image = image; }

    public void setWish_list_description(String wish_list_description) { this.wish_list_description = wish_list_description; }

    public void setWish_list_price(String wish_list_price) {
        this.wish_list_price = wish_list_price;
    }

    public void setWish_list_title(String wish_list_title) {
        this.wish_list_title = wish_list_title;
    }

    public void setOrigin(String org) { this.origin = org; }

    public void setDestination(String dest) {
        this.destination = dest;
    }
}