package com.whomentors.sadajura.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.whomentors.sarajura.R;

public class SJPreferences {

    /** SHARED PREFERENCES FUNCTIONALITY _______________________________________________________ **/

    // getPreferenceResource(): Selects the appropriate resource based on the shared preference type.
    private static int getPreferenceResource(String prefType) {
        return R.xml.sj_preferences;
    }

    // initializePreferences(): Initializes and returns the SharedPreferences object.
    public static SharedPreferences initializePreferences(String prefType, Context context) {
        return context.getSharedPreferences(prefType, Context.MODE_PRIVATE);
    }

    // setDefaultPreferences(): Sets the shared preference values to default values.
    public static void setDefaultPreferences(String prefType, Boolean isReset, Context context) {

        int prefResource = getPreferenceResource(prefType); // Determines the appropriate resource file to use.

        // Resets the preference values to default values.
        if (isReset == true) {
            SharedPreferences preferences = initializePreferences(prefType, context);
            preferences.edit().clear().apply();
        }

        // Sets the default values for the SharedPreferences object.
        PreferenceManager.setDefaultValues(context, prefType, Context.MODE_PRIVATE, prefResource, true);
    }

    /** GET PREFERENCES FUNCTIONALITY __________________________________________________________ **/

    public static String getOrigin(SharedPreferences preferences) {
        return preferences.getString("sj_origin", "United States of America");
    }

    public static String getDestination(SharedPreferences preferences) {
        return preferences.getString("sj_destination", "United States of America");
    }

    /** SET PREFERENCES FUNCTIONALITY __________________________________________________________ **/

    public static void setOrigin(String origin, SharedPreferences preferences) {

        // Prepares the SharedPreferences object for editing.
        SharedPreferences.Editor prefEdit = preferences.edit();

        prefEdit.putString("sj_origin", origin);
        prefEdit.apply(); // Applies the changes to SharedPreferences.
    }

    public static void setDestination(String destination, SharedPreferences preferences) {

        // Prepares the SharedPreferences object for editing.
        SharedPreferences.Editor prefEdit = preferences.edit();

        prefEdit.putString("sj_destination", destination);
        prefEdit.apply(); // Applies the changes to SharedPreferences.
    }

}
