package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs.myapplication.Adapters.OngoingEventListViewAdapter;
import edu.dartmouth.cs.myapplication.Model.OngoingEvent;

/**
 * Displays ongoing events
 */
public class OngoingActivity extends AppCompatActivity {
    private static final String TAG = OngoingActivity.class.getSimpleName();
    private List<OngoingEvent> events = new ArrayList<OngoingEvent>();
    private OngoingEventListViewAdapter mEventAdapter;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String mUserId;


    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);

        // Set up the action bar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("DartDASH");
        }

        // Get a reference to the Firebase database
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if((mUser != null ? mUser.getUid() : null) != null)
            mUserId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get the ListView for display, make adapter and set it on ListView
        final ListView mEventListView = findViewById(R.id.ongoing_listview);
        events.clear();
        mEventAdapter = new OngoingEventListViewAdapter(OngoingActivity.this, R.layout.event_item, events);
        mEventListView.setAdapter(mEventAdapter);
        mEventAdapter.notifyDataSetChanged();

    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        else if (id == R.id.action_sync) {
            pullOngoingEvents();
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(OngoingActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    // **************************** TO PROCEED TO OTHER ACTIVITIES ****************************** //

    public void pullOngoingEvents() {
        // Get reference to database
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("ongoing");

        // Get all ongoing events
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                events.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    // Get each events information
                    String newName = child.getKey().toString();
                    String newDate = child.child("date").getValue().toString();
                    String newEmail = child.child("email").getValue().toString();

                    OngoingEvent newEvent = new OngoingEvent();
                    newEvent.setName(newName);
                    newEvent.setDate(newDate);
                    newEvent.setEmail(newEmail);

                    // Add the new event to the list of events
                    events.add(newEvent);
                }
                mEventAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Could not remove deleted exercises from FireBase!");
            }
        });
    }

}
