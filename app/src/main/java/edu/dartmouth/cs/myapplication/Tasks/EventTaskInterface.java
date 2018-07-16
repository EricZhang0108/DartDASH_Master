package edu.dartmouth.cs.myapplication.Tasks;

import java.util.ArrayList;

import edu.dartmouth.cs.myapplication.Model.Event;

/**
 * Interface for managing what happens when an EventTask is completed
 */

public interface EventTaskInterface {
    public void onTaskComplete(ArrayList<Event> toReturn);
}
