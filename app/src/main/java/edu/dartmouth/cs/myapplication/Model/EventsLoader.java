package edu.dartmouth.cs.myapplication.Model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads event data from the database for display in the EventHistoryFragment
 */

public class EventsLoader extends AsyncTaskLoader<List<Event>> {
    private static final String TAG = EventsLoader.class.getSimpleName();
    private DartDASHDataSource dataSource;
    private boolean wantAllEvents;
    private String index;

    public EventsLoader(@NonNull Context context, boolean wantAllEvents, String index) {
        super(context);
        Log.d(TAG, "EventsLoader created");
        this.dataSource = new DartDASHDataSource(context);
        this.wantAllEvents = wantAllEvents;
        this.index = index;

    }

    @Nullable
    @Override
    // Calls getAllEvents or getEvent method in DartDASHDataSource
    public List<Event> loadInBackground() {

        List<Event> returnList = new ArrayList<Event>();
        if (wantAllEvents) {
            returnList = dataSource.getAllEvents();
        }
        else {
            returnList = dataSource.getEvent(index);
        }

        return returnList;
    }

}
