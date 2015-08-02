package com.whomentors.sadajura.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.whomentors.sarajura.R;

/**
 * Created by Michael Yoon Huh on 8/2/2015.
 */
public class SJSpinner {

    // createLocationSpinner(): Sets up the layout for the spinner dropdown object.
    public static void createLocationSpinner(final Context con, Spinner spin, final String locations[]) {

        // Initializes and creates a new ArrayAdapter object for the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(con,
                R.layout.sj_location_textview, locations) {

            // getView(): Sets up the spinner view attributes.
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }

            // getView(): Sets up the spinner attributes for the drop down list.
            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {

                // Sets up the infrastructure for the drop down list.
                LayoutInflater inflater = LayoutInflater.from(con);
                View worldSpinnerDrop = inflater.inflate(R.layout.sj_location_spinner, parent, false);

                // Sets up the custom font attributes for the spinner's drop down list.
                TextView mapChoice = (TextView) worldSpinnerDrop.findViewById(R.id.sj_location_spinner_choice);
                mapChoice.setText(locations[position]);

                return worldSpinnerDrop;
            }
        };

        spin.setAdapter(adapter); // Sets the new spinner object.
    }
}
