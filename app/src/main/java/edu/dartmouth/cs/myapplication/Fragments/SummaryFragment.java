package edu.dartmouth.cs.myapplication.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import edu.dartmouth.cs.myapplication.EventActivity;
import edu.dartmouth.cs.myapplication.R;

/**
 * Overview of current Event in EventActivity
 */

public class SummaryFragment extends Fragment {
    TextView eventNameField;
    TextView eventDateField;
    TextView eventCompletedField;
    TextView eventAmountField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventNameField = view.findViewById(R.id.summary_event_name);
        eventDateField = view.findViewById(R.id.summary_event_date);
        eventCompletedField = view.findViewById(R.id.number_transactions);
        eventAmountField = view.findViewById(R.id.summary_dollar_amount);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    // Updates all TextViews
    public void refresh() {
        // Set event name
        if (eventNameField != null) {
            eventNameField.setText(((EventActivity) getActivity()).getName());
        }

        // Set event date
        if (eventDateField != null) {
            eventDateField.setText(((EventActivity) getActivity()).getDate());
        }

        // Set number of complete transactions
        if (eventCompletedField != null) {
            eventCompletedField.setText(Integer.toString(((EventActivity) getActivity()).getTransactionsLength()));
        }

        // Set amount raised
        if (eventAmountField != null) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            eventAmountField.setText("$" + formatter.format(((EventActivity) getActivity()).getAmount()));
        }
    }
}
