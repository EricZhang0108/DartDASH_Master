package edu.dartmouth.cs.myapplication.Model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles saving and loading profile data using SharedPreferences
 */

public class Preferences {
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = Preferences.class.getName();

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ************************************** SETTERS ******************************************* //

    public void setEmail(String email) {
        sharedPreferences.edit().putString("EMAIL", email).apply();
    }

    public void setPassword(String password) {
        sharedPreferences.edit().putString("PASSWORD", password).apply();
    }

    // Keeps track of whether the user is logged in
    public void setLoggedIn(boolean loggedIn) {
        sharedPreferences.edit().putBoolean("LOGGED_IN", loggedIn).apply();
    }


    // ************************************** GETTERS ******************************************* //

    public String getEmail() {
        return sharedPreferences.getString("EMAIL", "");
    }

    public String getPassword() {
        return sharedPreferences.getString("PASSWORD", "");
    }

    // Retrieves whether the user is logged in
    public boolean getLoggedIn() {
        return sharedPreferences.getBoolean("LOGGED_IN", false);
    }

}
