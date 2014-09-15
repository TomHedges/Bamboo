package com.tomhedges.bamboo.util;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.model.Objective;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Convert an array of objectives into a View for display in the UI
 * <br>
 * Based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
 *  
 * @see			GameUI3D
 * @author      Tom Hedges
 */

public class ArrayAdapterObjectives extends ArrayAdapter<Objective> {

    Context mContext;
    int layoutResourceId;
    Objective[] data = null;

    public ArrayAdapterObjectives(Context mContext, int layoutResourceId, Objective[] data) {

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
        Objective objective = data[position];

        // get the TextView and then set the text (item name) and tag (item ID) values
        ImageView completionImage = (ImageView) convertView.findViewById(R.id.objective_image);
        if (objective.isCompleted()) {
        	completionImage.setImageResource(R.drawable.completed);
        } else {
        	completionImage.setImageResource(R.drawable.not_completed);
        }
        
        TextView heading = (TextView) convertView.findViewById(R.id.firstLine);
        String completionText = null;
        if (objective.isCompleted()) {
        	completionText = "COMPLETED!";
        } else {
        	completionText = "Not yet completed...";
        }
        
        heading.setText("Objective " + objective.getID() + ": " + completionText);
        
        TextView description = (TextView) convertView.findViewById(R.id.secondLine);
        description.setText(objective.getDescription());

        return convertView;

    }

}