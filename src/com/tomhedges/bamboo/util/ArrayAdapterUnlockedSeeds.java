package com.tomhedges.bamboo.util;

import com.tomhedges.bamboo.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Convert an array of unlocked seed data into a View for display in the UI
 * <br>
 * Based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
 * 
 * @see			GameUI3D
 * @author      Tom Hedges
 */

public class ArrayAdapterUnlockedSeeds extends ArrayAdapter<String[]> {

    Context mContext;
    int layoutResourceId;
    String[][] data = null;

    public ArrayAdapterUnlockedSeeds(Context mContext, int layoutResourceId, String[][] data) {

        super(mContext, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        // object item based on the position
        String[] unlockedSeed = data[position];

        // get the TextView and then set the text (item name) and tag (item ID) values     
        TextView date = (TextView) convertView.findViewById(R.id.unlocked_list_date);
        date.setText("Revealed: " + unlockedSeed[0]);
        
        TextView username = (TextView) convertView.findViewById(R.id.unlocked_list_sponsor_name);
        username.setText("From: " + unlockedSeed[1]);
        
        TextView message = (TextView) convertView.findViewById(R.id.unlocked_list_sponsor_message);
        message.setText(unlockedSeed[2]);
        
        TextView successMessage = (TextView) convertView.findViewById(R.id.unlocked_list_success_message);
        successMessage.setText("Revealed message: " + unlockedSeed[3]);

        return convertView;
    }
}