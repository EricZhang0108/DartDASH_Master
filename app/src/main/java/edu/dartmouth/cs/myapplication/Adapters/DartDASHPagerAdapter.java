package edu.dartmouth.cs.myapplication.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Custom adapter for managing fragments in MainActivity
 */

public class DartDASHPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = DartDASHPagerAdapter.class.getSimpleName();
    private ArrayList<Fragment> fragments;

    // Identifiers for each fragment
    private static final int START = 0;
    private static final int HISTORY = 1;
    private static final String UI_TAB_START = "START";
    private static final String UI_TAB_HISTORY = "HISTORY";

    public DartDASHPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragments) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    // Returns the position of the retrieved item
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    // Reports how many fragments there are
    public int getCount() {
        return fragments.size();
    }

    // Returns the title of the fragment in the given position
    public CharSequence getFragmentTitle(int position) {
        Log.d(TAG, "got page title from position " + position);
        switch (position) {
            case START:
                return UI_TAB_START;
            case HISTORY:
                return UI_TAB_HISTORY;
            default:
                break;
        }
        return null;
    }

}
