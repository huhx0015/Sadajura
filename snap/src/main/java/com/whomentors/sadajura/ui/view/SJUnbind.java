package com.whomentors.sadajura.ui.view;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/** -----------------------------------------------------------------------------------------------
 *  [SJMemory] CLASS
 *  DESCRIPTION: SJMemory class is a class that handles memory-related functionality, such as
 *  unbinding View groups that are no longer needed by activities.
 *  -----------------------------------------------------------------------------------------------
 */

public class SJUnbind {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    public static final String LOG_TAG = SJUnbind.class.getSimpleName();

    /** RECYCLE FUNCTIONALITY __________________________________________________________________ **/

    // recycleMemory(): Recycles View objects to clear up memory resources.
    public static void recycleMemory(View layout) {

        // Unbinds all Drawable objects attached to the current layout.
        try { unbindDrawables(layout); }

        // Null pointer exception catch.
        catch (NullPointerException e) {
            e.printStackTrace(); // Prints error message.
            Log.d(LOG_TAG, "ERROR: recycleMemory(): " + e);
        }
    }

    // unbindDrawables(): Unbinds all Drawable objects attached to the view layout by setting them
    // to null, freeing up memory resources and preventing Context-related memory leaks. This code
    // is borrowed from Roman Guy at www.curious-creature.org.
    private static void unbindDrawables(View view) {

        // If the View object's background is not null, a Callback is set to render them null.
        if (view.getBackground() != null) { view.getBackground().setCallback(null); }

        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }

            ((ViewGroup) view).removeAllViews(); // Removes all View objects in the ViewGroup.
        }
    }
}