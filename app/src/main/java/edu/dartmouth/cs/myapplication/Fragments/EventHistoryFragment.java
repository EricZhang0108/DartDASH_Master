package edu.dartmouth.cs.myapplication.Fragments;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs.myapplication.Adapters.EventListViewAdapter;
import edu.dartmouth.cs.myapplication.EventActivity;
import edu.dartmouth.cs.myapplication.Model.DartDASHDataSource;
import edu.dartmouth.cs.myapplication.Model.Event;
import edu.dartmouth.cs.myapplication.Model.EventsLoader;
import edu.dartmouth.cs.myapplication.Model.Transaction;
import edu.dartmouth.cs.myapplication.R;
import edu.dartmouth.cs.myapplication.Tasks.EventTask;
import edu.dartmouth.cs.myapplication.Tasks.EventTaskInterface;

/**
 * Displays list of previously created Events
 */
public class EventHistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Event>> {
    private static final String TAG = EventHistoryFragment.class.getSimpleName();
    private static final int LOAD_ALL_EVENTS = 1;
    private List<Event> events = new ArrayList<Event>();
    private EventListViewAdapter mEventAdapter;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String mUserId;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Sync the events when sync button pressed
            case R.id.action_sync:
                startSync();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to the Firebase database
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if((mUser != null ? mUser.getUid() : null) != null)
            mUserId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get the ListView for display, make adapter and set it on ListView
        final ListView mEventListView = getActivity().findViewById(R.id.history_listview);
        events.clear();
        mEventAdapter = new EventListViewAdapter(getActivity(),
                R.layout.event_item, events);
        mEventListView.setAdapter(mEventAdapter);
        mEventAdapter.notifyDataSetChanged();

        // Set up ListView click listener--goes to EventActivity when clicked
        mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get id to put in intent
                String id = Long.toString(((Event) (mEventListView.getItemAtPosition(i))).getId());

                Intent intent = new Intent(getActivity(), EventActivity.class);
                intent.putExtra("ID", id);
                startActivity(intent);
            }
        });

        // If events added/deleted/changed
        if(mUserId != null){
            mDatabase.child("users").child(mUserId).child("events").addChildEventListener(new ChildEventListener() {
                 // Handles additions of event entries in Firebase
                 @Override
                 public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                     Log.d(TAG, "onChildAdded history");
                     // Get all events from SQLite
                     final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
                     EventTask eventTask=new EventTask(dataSource, 4, new EventTaskInterface() {
                         @Override
                         public void onTaskComplete(ArrayList<Event> events) {
                             dataSource.close();
                             if (events != null) {
                                 // Send to newEvents to check if any are new
                                 newEvents(events, dataSnapshot);
                                 refreshList();
                             }
                         }
                     });
                     eventTask.execute();
                 }

                 // Handles changes of individual pieces of data in Firebase
                 @Override
                 public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                     Log.d(TAG, "onChildChanged history");
                     // Get all events
                     final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
                     EventTask eventTask=new EventTask(dataSource, 4, new EventTaskInterface() {
                         @Override
                         public void onTaskComplete(ArrayList<Event> events) {
                             dataSource.close();
                             // For each event
                             if (events != null) {
                                 for (Event event : events) {
                                     // If the Firebase id matches
                                     String date = event.getDate();
                                     String[] dateParts = date.split("/");
                                     String firebaseChildId = event.getName() + " " + dateParts[0] + dateParts[1] + dateParts[2];
                                     if (dataSnapshot.getKey().equals(firebaseChildId)) {
                                         // Update the entry in SQLite
                                         updateEventInfo(event, dataSnapshot);
                                     }
                                 }
                             }
                             refreshList();
                         }
                     });
                     eventTask.execute();
                 }

                 // Removes event from app when removed from Firebase console
                 @Override
                 public void onChildRemoved(final DataSnapshot dataSnapshot) {
                     Log.d(TAG, "onChildRemoved history");
                     // Get all events
                     final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
                     EventTask eventTask=new EventTask(dataSource, 4, new EventTaskInterface() {
                         @Override
                         public void onTaskComplete(ArrayList<Event> events) {
                             dataSource.close();
                             if (events != null) {
                                 // For each events
                                 for (Event event : events) {
                                     // If the Firebase id matches
                                     String date = event.getDate();
                                     String[] dateParts = date.split("/");
                                     String firebaseChildId = event.getName() + " " + dateParts[0] + dateParts[1] + dateParts[2];
                                     if (dataSnapshot.getKey().equals(firebaseChildId)) {
                                         // Remove the entry from SQLite
                                         removeChild(event);
                                     }
                                 }
                             }
                         }
                     });
                     eventTask.execute();
                 }

                 @Override
                 public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                     // Not relevant
                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {
                     Log.w(TAG, "Failed to properly retrieve event information!", databaseError.toException());
                 }
            });
        }

        // Load events to display
        LoaderManager mLoaderManager = getActivity().getSupportLoaderManager();
        mLoaderManager.restartLoader(LOAD_ALL_EVENTS, null, this).forceLoad();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Load events to display
        LoaderManager mLoaderManager = getActivity().getSupportLoaderManager();
        mLoaderManager.restartLoader(LOAD_ALL_EVENTS, null, this).forceLoad();
    }


    // *********************************** SYNCING METHODS ************************************** //

    // Starts sync be obtaining list of all events in SQLite storage
    public void startSync() {
        // Get all events
        final DartDASHDataSource dataSource = new DartDASHDataSource(getActivity());
        EventTask eventTask = new EventTask(dataSource, 4, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> events) {
                dataSource.close();
                if (events != null) {
                    // If there are events in SQLite, pass on to to see if any need to be deleted
                    syncFirebase(events);
                }
            }
        });
        eventTask.execute();
    }

    // Update Firebase by deleting/adding
    public void syncFirebase(ArrayList<Event> events) {
        // Delete events from SQLite database and store their ids so they can be deleted from FireBase
        ArrayList<String> toDelete = new ArrayList<String>();
        for (Event event : events) {
            // If the event needs to be deleted from Firebase
            if (event.isDeleted()) {
                // Add the event's Firebase id to list
                String date = event.getDate();
                String[] dateParts = date.split("/");
                String firebaseChildId = event.getName() + " " + dateParts[0] + dateParts[1] + dateParts[2];
                toDelete.add(firebaseChildId);
                // Delete the event from SQLite
                final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
                EventTask eventTask=new EventTask(dataSource, 2, new EventTaskInterface() {
                    @Override
                    public void onTaskComplete(ArrayList<Event> toReturn) {
                        dataSource.close();
                        refreshList();
                    }
                });
                eventTask.execute(event);
            }
        }

        // Delete all events from Firebase whose times are in the toDelete list
        final ArrayList<String> times = toDelete;
        if(mUserId != null) {
            mDatabase.child("users").child(mUserId).child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        // If the id of the current entry is in the list of ids whose events need to be deleted
                        if (times.contains(child.getKey().toString())) {
                            // Remove the event entry
                            child.getRef().removeValue();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Could not remove deleted events from FireBase!");
                }
            });
        }

        // For each event in the SQLite database
        for (Event event : events) {
            // If any haven't been synced, sync them
            if (!event.isSynced()) {
                uploadEvent(event);
            }
        }

    }

    // Uploads events that are in SQLite but not Firebase
    public void uploadEvent(Event event) {
        // Get event's information
        ArrayList<Transaction> transactions = event.getTransactions();
        String name = event.getName();
        String date = event.getDate();

        String[] dateParts = date.split("/");

        String firebaseChildId = name + " " + dateParts[0] + dateParts[1] + dateParts[2];

        double amount = event.getAmount();


        DatabaseReference currEvent = null;
        // Get new event key
        if(mUserId != null) {
            currEvent = mDatabase.child("users").child(mUserId).child("events").child(firebaseChildId);
        }

        // Set key-value pairs
        if (currEvent != null) {
            // Put the information in Firebase
            String transactionString = "";
            // Get string from list
            if (transactions != null) {
                transactionString = toJSON(transactions);
            }
            currEvent.child("transactions").setValue(transactionString);
            currEvent.child("name").setValue(name);
            currEvent.child("date").setValue(date);
            currEvent.child("amount").setValue(amount);
        }

        // Set the newly uploaded event to synced
        Event toSetSynced = new Event();

        toSetSynced.setId(getId());
        toSetSynced.setTransactions(transactions);
        toSetSynced.setName(name);
        toSetSynced.setDate(date);
        toSetSynced.setAmount(amount);
        toSetSynced.setSynced(true);
        toSetSynced.setDeleted(event.isDeleted());

        // Update the event in SQLite as synced
        final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
        EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> toReturn) {
                dataSource.close();
                refreshList();
            }
        });
        eventTask.execute(event);
    }

    // Refreshes the ListView so changes can immediately be observed
    public void refreshList() {
        // Load the events using the LoaderManager
        LoaderManager mLoaderManager = getActivity().getSupportLoaderManager();
        mLoaderManager.restartLoader(LOAD_ALL_EVENTS, null, this).forceLoad();
    }

    // Converts list of Transactions to a String based on its JSON object
    private String toJSON(ArrayList<Transaction> transactions) {
        JSONArray json = new JSONArray();
        String toReturn = "";
        try {
            for (Transaction transaction : transactions) {
                String transactionString = transaction.toString();
                json.put(transactions.indexOf(transaction), transactionString);
            }
            toReturn = json.toString();
        }
        catch (JSONException e) {
            Toast.makeText(getContext(), "Could not save event!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "JSONException: could not store transactions.");
        }
        return  toReturn;
    }


    // ***************** EVENT UPDATE METHODS, TRIGGERED BY CHILDEVENTLISTENER ****************** //

    // Checks if an events is really new
    public void newEvents(ArrayList<Event> events, DataSnapshot dataSnapshot) {

        boolean newEvent = true;
        // For each event in the database
        for (Event event : events) {
            // If the Firebase Ids match
            String date = event.getDate();
            String[] dateParts = date.split("/");
            String firebaseChildId = event.getName() + " " + dateParts[0] + dateParts[1] + dateParts[2];
            if (dataSnapshot.getKey().equals(firebaseChildId)) {
                // Don't add the event again
                newEvent = false;
            }
        }
        // If actually a new event
        if (newEvent) {
            // Add the new event to SQLite
            newEventFromFirebase(dataSnapshot);
        }
    }

    // Adds a new event to SQLite when one is added to Firebase
    public void newEventFromFirebase(DataSnapshot dataSnapshot) {
        // Get the transactions list
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        if (dataSnapshot.hasChild("transactions")) {
            String transactionString = dataSnapshot.child("transactions").getValue().toString();
            try {
                transactions = fromJSON(transactionString);
            }
            catch (JSONException e) {
                Log.d(TAG, "JSONException: could not read JSON transaction list string.");
            }
        }
        // Get the name
        String name = "";
        if (dataSnapshot.hasChild("name")) {
            name = dataSnapshot.child("name").getValue().toString();
        }
        // Get the date
        String date = "";
        if (dataSnapshot.hasChild("date")) {
            date = dataSnapshot.child("date").getValue().toString();
        }
        // Get the amount
        double amount = (double)0;
        if (dataSnapshot.hasChild("amount")) {
            amount = Double.parseDouble(dataSnapshot.child("amount").getValue().toString());
        }

        // Create an event based on the information to pass to task
        Event event = new Event();
        event.setTransactions(transactions);
        event.setName(name);
        event.setDate(date);
        event.setAmount(amount);
        event.setSynced(true);
        event.setDeleted(false);

        // Insert the new event entry
        final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
        EventTask eventTask=new EventTask(dataSource, 1, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> toReturn) {
                dataSource.close();
                refreshList();
            }
        });
        eventTask.execute(event);
    }

    // Parses list of Transactions from JSON string
    private ArrayList<Transaction> fromJSON(String jsonString) throws JSONException {
        JSONArray array = new JSONArray(jsonString);
        ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
        for (int i = 0; i < array.length(); i++) {
            String[] parts = array.getString(i).split(", ");
            // If there are transactions to be returned
            if (parts.length > 1) {
                String name = parts[0];
                Long dash = Long.parseLong(parts[1]);
                Double amount = Double.parseDouble(parts[2]);
                boolean synced = Boolean.parseBoolean(parts[3]);
                boolean deleted = Boolean.parseBoolean(parts[4]);
                String signatureString = parts[5];
                byte[] byteArray = Base64.decode(signatureString, Base64.DEFAULT);
                Transaction newTransaction = new Transaction(name, dash, amount, synced, deleted,
                        BitmapFactory.decodeByteArray(byteArray,0,byteArray.length));

                transactionList.add(newTransaction);
            }
        }
        return transactionList;
    }

    // For individual changes of information in Firebase
    public void updateEventInfo(Event event, DataSnapshot dataSnapshot) {
        // Make sure changes are parceable as integers
        try {
            // Set transactions
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            if (dataSnapshot.hasChild("transactions")) {
                String transactionString = dataSnapshot.child("transactions").getValue().toString();
                try {
                    transactions = fromJSON(transactionString);
                }
                catch (JSONException e) {
                    Log.d(TAG, "JSONException: could not read JSON transaction list string.");
                }
            }
            event.setTransactions(transactions);

            // Set name, date, and amount
            event.setName(dataSnapshot.child("name").getValue().toString());
            event.setDate(dataSnapshot.child("date").getValue().toString());
            event.setAmount(Double.parseDouble(dataSnapshot.child("amount").getValue().toString()));

        }
        catch (Exception e) {
            Log.e(TAG, "Firebase changes formatted incorrectly.");
        }

        // Update the event
        final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
        EventTask eventTask=new EventTask(dataSource, 3, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> toReturn) {
                dataSource.close();
                refreshList();
            }
        });
        eventTask.execute(event);

    }

    // Removes child deleted from Firebase
    public void removeChild(Event event) {
        // Remove the event from SQLite
        final DartDASHDataSource dataSource= new DartDASHDataSource(getActivity());
        EventTask eventTask=new EventTask(dataSource, 2, new EventTaskInterface() {
            @Override
            public void onTaskComplete(ArrayList<Event> toReturn) {
                dataSource.close();
                refreshList();
            }
        });
        eventTask.execute(event);
    }


    // *********************************** LOADER METHODS *************************************** //

    @NonNull
    @Override
    // Create the loader
    public Loader<List<Event>> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "onCreateLoader history");
        if (id == LOAD_ALL_EVENTS) {
            return new EventsLoader(getContext(), true, "null");
        }
        return null;
    }

    @Override
    // When finished, add the events to the adapter to be displayed
    public void onLoadFinished(@NonNull Loader<List<Event>> loader, List<Event> dbEvents) {
        Log.d(TAG, "onLoadFinished history");
        if(loader.getId() == LOAD_ALL_EVENTS){
            if (dbEvents.size() > 0) {
                events.clear();
                for (Event event : dbEvents) {
                    if (!event.isDeleted()) {
                        // If an event is not deleted, add it to the list of events to be displayed
                        events.add(event);
                    }
                }
                mEventAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    // Reset the adapter
    public void onLoaderReset(@NonNull Loader<List<Event>> loader) {
        Log.d(TAG, "onLoaderReset history");
        if(loader.getId() == LOAD_ALL_EVENTS){
            mEventAdapter.clear();
            mEventAdapter.notifyDataSetChanged();
        }
    }

}
