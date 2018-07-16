package edu.dartmouth.cs.myapplication.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.dartmouth.cs.myapplication.Model.OngoingEvent;
import edu.dartmouth.cs.myapplication.R;

/**
 * Adapter for displaying events in OngoingActivity
 */

public class OngoingEventListViewAdapter extends ArrayAdapter<OngoingEvent> {

    private List<OngoingEvent> mEventList;
    private int mResourceId;

    public OngoingEventListViewAdapter(Context context, int resourceId, List<OngoingEvent> eventList) {
        super(context, resourceId, eventList);
        this.mResourceId = resourceId;
        this.mEventList = eventList;
    }

    @Override
    // Returns the number of OngoingEvent entries there are
    public int getCount() {
        return mEventList.size();
    }

    @Override
    // Returns the item at given position
    public long getItemId(int position) {                   // DO I NEED ANY OF THESE METHODS THO
        return position;
    }

    @NonNull
    @Override
    // Returns new view
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the current event
        OngoingEvent curr = getItem(position);
        LinearLayout currEvent;

        // If there is no view to begin with, create one
        if (convertView == null) {
            currEvent = new LinearLayout(getContext());
            String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater theInflater;

            theInflater = (LayoutInflater) getContext().getSystemService(inflaterString);
            theInflater.inflate(mResourceId, currEvent, true);
        }

        // Otherwise cast convertView
        else {
            currEvent = (LinearLayout) convertView;
        }

        // Update the TextViews using the current OngoingEvent's information
        TextView name = currEvent.findViewById(R.id.event_title);
        name.setText(getItem(position).getName());

        TextView date = currEvent.findViewById(R.id.event_date);
        date.setText(getItem(position).getDate());

        TextView amount = currEvent.findViewById(R.id.event_amount);
        amount.setText(getItem(position).getEmail());

        // Return the new view
        return currEvent;
    }
}
