package edu.dartmouth.cs.myapplication.Fragments;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import edu.dartmouth.cs.myapplication.MainActivity;

/**
 * Prepares dialog for date choosing in EventActivity NewEventFragment
 */
public class DartDASHDialogFragment extends DialogFragment {

    public static final int ID_PICK_EVENT_DATE = 0;
    private static final String DIALOG_ID_KEY = "dialog_id";

    // Creates new dialog fragment
    public static DartDASHDialogFragment newInstance(int dialog_id) {
        DartDASHDialogFragment fragment = new DartDASHDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ID_KEY, dialog_id);
        fragment.setArguments(args);
        return fragment;
    }

    // Creates dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
        final Activity parent = getActivity();

        // Sets up a dialog and on listeners
        switch (dialog_id) {
            // To set the date in ManualInputActivity
            case ID_PICK_EVENT_DATE:
                DatePickerDialog datePickerDialog = new DatePickerDialog(parent);
                DatePickerDialog.OnDateSetListener partyDatePicker = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        // Correct day and month if one digit
                        int correctedMonth = monthOfYear+1;
                        String correctMonthString = Integer.toString(correctedMonth);
                        if (correctedMonth <= 9) {
                            correctMonthString = "0" + correctedMonth;
                        }

                        String correctedDayString = Integer.toString(dayOfMonth);
                        if (dayOfMonth <= 9) {
                            correctedDayString = "0" + dayOfMonth;
                        }

                        String textTime = correctMonthString + "/" + correctedDayString + "/" + year;
                        ((MainActivity)parent).setDate(textTime);
                    }
                };
                datePickerDialog.setOnDateSetListener(partyDatePicker);
                return datePickerDialog;

            default:
                return null;
        }
    }

}

