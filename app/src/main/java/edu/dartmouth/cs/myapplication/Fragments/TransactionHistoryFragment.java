package edu.dartmouth.cs.myapplication.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs.myapplication.Adapters.TransactionListViewAdapter;
import edu.dartmouth.cs.myapplication.ConfirmationActivity;
import edu.dartmouth.cs.myapplication.Model.Transaction;
import edu.dartmouth.cs.myapplication.R;

/**
 * Displays list of Transactions for current Event
 */

public class TransactionHistoryFragment extends Fragment {
    private List<Transaction> transactions = new ArrayList<Transaction>();
    private TransactionListViewAdapter mTransactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ListView for display, make adapter and set it on ListView
        final ListView mTransactionListView = getActivity().findViewById(R.id.history_listview);
        mTransactionAdapter = new TransactionListViewAdapter(getActivity(),
                R.layout.transaction_item, transactions);
        mTransactionListView.setAdapter(mTransactionAdapter);
        mTransactionAdapter.notifyDataSetChanged();

        // Set up ListView click listener--goes to EventActivity when clicked
        mTransactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // When transaction entry clicked on, go to ConfirmationActivity

                String name = ((Transaction) (mTransactionListView.getItemAtPosition(i))).getName();
                long dash = ((Transaction) (mTransactionListView.getItemAtPosition(i))).getDash();
                double amount = ((Transaction) (mTransactionListView.getItemAtPosition(i))).getAmount();
                boolean synced = ((Transaction) (mTransactionListView.getItemAtPosition(i))).isSynced();
                boolean deleted = ((Transaction) (mTransactionListView.getItemAtPosition(i))).isDeleted();

                Bitmap signature = ((Transaction) (mTransactionListView.getItemAtPosition(i))).getSignature();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                signature.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                Intent intent = new Intent(getActivity(), ConfirmationActivity.class);

                intent.putExtra("name", name);
                intent.putExtra("dash", dash);
                intent.putExtra("amount", amount);
                intent.putExtra("synced", synced);
                intent.putExtra("deleted", deleted);
                intent.putExtra("signature", byteArray);

                startActivity(intent);

            }
        });

    }

    // Updates Transaction ListView
    public void refresh(ArrayList<Transaction> toUpdate) {
        transactions.clear();
        transactions.addAll(toUpdate);
        if (mTransactionAdapter != null) {
            mTransactionAdapter.notifyDataSetChanged();
        }
    }

    // Sets transaction list
    public void setTransactions(List<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
    }

}
