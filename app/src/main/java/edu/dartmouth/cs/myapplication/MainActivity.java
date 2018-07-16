package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.dartmouth.cs.myapplication.Adapters.DartDASHPagerAdapter;
import edu.dartmouth.cs.myapplication.Fragments.DartDASHDialogFragment;
import edu.dartmouth.cs.myapplication.Fragments.EventHistoryFragment;
import edu.dartmouth.cs.myapplication.Fragments.NewEventFragment;
import edu.dartmouth.cs.myapplication.Model.DartDASHDataSource;
import edu.dartmouth.cs.myapplication.Model.Event;
import edu.dartmouth.cs.myapplication.Model.Preferences;
import edu.dartmouth.cs.myapplication.Tasks.EventTask;
import edu.dartmouth.cs.myapplication.Tasks.EventTaskInterface;

/**
 * Allows user to see their past Events and create new Events, has NewEventFragment and EventHistoryFragment
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private DartDASHPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;


    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create fragments
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new NewEventFragment());
        fragments.add(new EventHistoryFragment());

        // Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new DartDASHPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // If not logged in, go to LoginActivity so the user can log in
        Preferences preferences = new Preferences(MainActivity.this);
        if (!preferences.getLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // Set the listener on the BottomNavigationView
        mBottomNavigationView = findViewById(R.id.bottom_nav_bar_main);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // When the fragment changes, change the navigation button that is highlighted
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                // Resets the action bar so that the sync button can be removed or added based on the fragment
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    // Switch the fragment being viewed when other fragment selected in navigation bar
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_start:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.navigation_history:
                mViewPager.setCurrentItem(1);
                break;
            default:
                return false;
        }
        return true;
    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If on NewEventFragment, don't display the sync button
        if (mViewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.action_sync).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    // **************************** TO PROCEED TO OTHER ACTIVITIES ****************************** //

    // Proceeds to EventActivity to start new Event
    public void startNewEvent(View view) {
        final Intent intent = new Intent(MainActivity.this, EventActivity.class);

        final EditText nameInput = findViewById(R.id.event_name_text);
        final EditText dateInput = findViewById(R.id.event_date_text);

        // If the user inputs a name and date
        if (!nameInput.getText().toString().equals("") && !dateInput.getText().toString().equals("")) {

            String date = dateInput.getText().toString();
            String[] dateParts = date.split("/");
            String name = nameInput.getText().toString();
            final String wouldBeFirebaseId = name + " " + dateParts[0] + dateParts[1] + dateParts[2];

            final DartDASHDataSource dataSource= new DartDASHDataSource(MainActivity.this);
            EventTask eventTask=new EventTask(dataSource, 4, new EventTaskInterface() {
                @Override
                public void onTaskComplete(ArrayList<Event> events) {
                    dataSource.close();
                    if (events != null) {
                        boolean alreadyCreated = false;
                        // For each existing event
                        for (Event event : events) {
                            // Get Firebase id
                            String existingDate = event.getDate();
                            String[] existingDateParts = existingDate.split("/");
                            String existingName = event.getName();
                            String existingFirebaseId = existingName + " " + existingDateParts[0] +
                                    existingDateParts[1] + existingDateParts[2];
                            // If it's the same, can't make the event
                            if (wouldBeFirebaseId.equals(existingFirebaseId)) {
                                alreadyCreated = true;
                            }
                        }
                        // If doesn't conflict with already created event
                        if (!alreadyCreated) {
                            // Put event name in intent
                            intent.putExtra("NAME", nameInput.getText().toString());
                            // Put event date in intent
                            intent.putExtra("DATE", dateInput.getText().toString());

                            startActivity(intent);
                        }
                        // Otherwise, tell the user the event already exists
                        else {
                            Toast.makeText(MainActivity.this,
                                    "Event with given name and date already exists!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
            eventTask.execute();
        }
        // Otherwise, tell the user they must enter name/date
        else {
            if (nameInput.getText().toString().equals("")) {
                nameInput.setError("Must enter an event name!");
            }
            if (dateInput.getText().toString().equals("")) {
                dateInput.setError("Must enter an event date!");
            }
        }
    }

    // Proceeds to OngoingActivity
    public void toOngoingEvents(View view) {
        Intent intent = new Intent(MainActivity.this, OngoingActivity.class);
        startActivity(intent);
    }

    // Creates dialog for user to input event date
    public void getDate(View view) {
        DialogFragment fragment = DartDASHDialogFragment.newInstance(0);
        fragment.show(getSupportFragmentManager(), "new dialog");
    }

    // Sets date chosen by user
    public void setDate(String date) {
        TextView dateInput = findViewById(R.id.event_date_text);
        dateInput.setText(date);
    }

}
