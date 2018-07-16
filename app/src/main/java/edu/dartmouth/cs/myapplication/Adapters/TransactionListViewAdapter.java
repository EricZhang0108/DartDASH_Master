package edu.dartmouth.cs.myapplication.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import edu.dartmouth.cs.myapplication.Model.Transaction;
import edu.dartmouth.cs.myapplication.R;

/**
 * Adapter for displaying transactions in TransactionHistoryFragment
 */

public class TransactionListViewAdapter extends ArrayAdapter<Transaction> {

    private List<Transaction> mTransactionList;
    private int mResourceId;

    public TransactionListViewAdapter(Context context, int resourceId, List<Transaction> transactionList) {
        super(context, resourceId, transactionList);
        this.mResourceId = resourceId;
        this.mTransactionList = transactionList;
    }

    @Override
    // Returns the number of Transaction entries there are
    public int getCount() {
        return mTransactionList.size();
    }

    @Override
    // Returns the item at given position
    public long getItemId(int position) {                   // DO I NEED ANY OF THESE METHODS THO
        return position;
    }

    @NonNull
    @Override
    // Returns new view
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the current transaction
        Transaction curr = getItem(position);
        LinearLayout currTransaction;

        // If there is no view to begin with, create one
        if (convertView == null) {
            currTransaction = new LinearLayout(getContext());
            String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater theInflater;

            theInflater = (LayoutInflater) getContext().getSystemService(inflaterString);
            theInflater.inflate(mResourceId, currTransaction, true);
        }

        // Otherwise cast convertView
        else {
            currTransaction = (LinearLayout) convertView;
        }

        // Update the TextViews using the current transaction's information
        TextView name = currTransaction.findViewById(R.id.transaction_name);
        name.setText(getItem(position).getName());

        TextView dash = currTransaction.findViewById(R.id.transaction_dash);
        dash.setText(Long.toString(getItem(position).getDash()));

        NumberFormat formatter = new DecimalFormat("#0.00");
        TextView amount = currTransaction.findViewById(R.id.transaction_amount);
        amount.setText("$" + formatter.format(getItem(position).getAmount()));

        // Return the new view
        return currTransaction;
    }
}
