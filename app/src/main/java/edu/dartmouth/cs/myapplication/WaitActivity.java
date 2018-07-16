package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Displayed after client signs, signals the client to give the phone back to the event organizer
 */
public class WaitActivity extends AppCompatActivity {

    // ********************************* LIFE CYCLE METHODS ************************************* //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        // Get and set the user's chosen thank you message
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WaitActivity.this);
        String thank_you_msg = prefs.getString("message_pref", "Thank you for participating!");
        TextView message = findViewById(R.id.temp_message);
        message.setText(thank_you_msg);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("DartDASH");
        }
    }


    // ********************************* SET UP OPTIONS MENU ************************************ //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wait, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(WaitActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    // **************************** TO PROCEED TO OTHER ACTIVITIES ****************************** //

    // Sends user back to Event summary when done button clicked
    public void toEvent(View view) {
        finish();
    }

}
