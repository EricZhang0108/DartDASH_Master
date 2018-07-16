package edu.dartmouth.cs.myapplication.Tasks;

import android.os.AsyncTask;

import java.util.ArrayList;

import edu.dartmouth.cs.myapplication.Model.DartDASHDataSource;
import edu.dartmouth.cs.myapplication.Model.Event;

/**
 * Manages interaction with DartDASHDataSource through different tasks
 */

public class EventTask extends AsyncTask<Event, String, ArrayList<Event>> {

    private int operation;
    private DartDASHDataSource dataSource;
    private EventTaskInterface taskInterface;

    public EventTask(DartDASHDataSource dataSource, int operation, EventTaskInterface taskInterface) {
        this.dataSource = dataSource;
        this.operation = operation;
        this.taskInterface = taskInterface;
    }

    @Override
    protected ArrayList<Event> doInBackground(Event... events) {
        ArrayList<Event> toReturn = null;
        // Insert
        if(operation == 1) {
            dataSource.createEvent(events[0]);
        }
        // Delete
        else if(operation == 2){
            dataSource.deleteEvent(String.valueOf(events[0].getId()));
        }
        // Update
        else if(operation == 3){
            dataSource.updateEvent(events[0]);
        }
        // Get all events
        else if (operation == 4) {
            toReturn = dataSource.getAllEvents();
        }

        return toReturn;
    }

    @Override
    protected void onPostExecute(ArrayList<Event> events) {
        super.onPostExecute(events);
        taskInterface.onTaskComplete(events);
    }

}