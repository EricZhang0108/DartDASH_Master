package edu.dartmouth.cs.myapplication.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Insert/deletes/updates entries in SQLite database
 */

public class DartDASHDataSource {
    private static final String TAG = DartDASHDataSource.class.getSimpleName();
    private DartDASHSQLiteHelper helper;
    private String[] allColumns = {
            DartDASHSQLiteHelper.COLUMN_ID, DartDASHSQLiteHelper.COLUMN_NAME,
            DartDASHSQLiteHelper.COLUMN_DATE, DartDASHSQLiteHelper.COLUMN_AMOUNT,
            DartDASHSQLiteHelper.COLUMN_TRANSACTIONS, DartDASHSQLiteHelper.COLUMN_SYNCED,
            DartDASHSQLiteHelper.COLUMN_DELETED};
    private Context context;

    public DartDASHDataSource(Context context) {
        helper = new DartDASHSQLiteHelper(context);
        this.context = context;
    }

    public void close() {
        helper.close();
    }

    // Creates an event given the necessary information
    public void createEvent(Event event) {

        ContentValues values = new ContentValues();

        // Put the information in values
        values.put(DartDASHSQLiteHelper.COLUMN_NAME, event.getName());
        values.put(DartDASHSQLiteHelper.COLUMN_DATE, event.getDate());
        values.put(DartDASHSQLiteHelper.COLUMN_AMOUNT, event.getAmount());

        ArrayList<Transaction> transactions = event.getTransactions();
        String transactionString = "";
        // Get string from list
        if (transactions != null) {
            transactionString = toJSON(transactions);
        }
        values.put(DartDASHSQLiteHelper.COLUMN_TRANSACTIONS, transactionString);

        values.put(DartDASHSQLiteHelper.COLUMN_SYNCED, event.isSynced());
        values.put(DartDASHSQLiteHelper.COLUMN_DELETED, event.isDeleted());

        // Insert in SQLite database
        SQLiteDatabase database = helper.getWritableDatabase();
        database.insert(DartDASHSQLiteHelper.TABLE_TRANSACTIONS, null, values);
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
            Toast.makeText(context, "Could not save event!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "JSONException: could not store transactions.");
        }
        return  toReturn;
    }

    // Deletes a given event
    public void deleteEvent(String currColId) {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.delete(DartDASHSQLiteHelper.TABLE_TRANSACTIONS, DartDASHSQLiteHelper.COLUMN_ID
                + " = " + currColId, null);
    }

    // Updates event's information
    public void updateEvent(Event event) {
        ContentValues values = new ContentValues();

        // Put the information in values
        values.put(DartDASHSQLiteHelper.COLUMN_NAME, event.getName());
        values.put(DartDASHSQLiteHelper.COLUMN_DATE, event.getDate());
        values.put(DartDASHSQLiteHelper.COLUMN_AMOUNT, event.getAmount());

        ArrayList<Transaction> transactions = event.getTransactions();
        String transactionString = "";
        // Get string from list
        if (transactions != null) {
            transactionString = toJSON(transactions);
        }
        values.put(DartDASHSQLiteHelper.COLUMN_TRANSACTIONS, transactionString);

        values.put(DartDASHSQLiteHelper.COLUMN_SYNCED, event.isSynced());
        values.put(DartDASHSQLiteHelper.COLUMN_DELETED, event.isDeleted());

        String currId = Long.toString(event.getId());

        SQLiteDatabase database = helper.getWritableDatabase();
        database.update(DartDASHSQLiteHelper.TABLE_TRANSACTIONS, values, DartDASHSQLiteHelper.COLUMN_ID
                + " = " + currId, null);
    }

    // Returns a list of all events
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<Event>();

        // Make a cursor and use to get all exercises, add to list
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query(DartDASHSQLiteHelper.TABLE_TRANSACTIONS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Event event = cursorToEvent(cursor);
            events.add(event);
            cursor.moveToNext();
        }
        return events;
    }

    // Returns a list containing the event with the given id
    public ArrayList<Event> getEvent(String idString) {
        ArrayList<Event> events = new ArrayList<Event>();

        // Use the given id to retrieve the desired event with a cursor
        int id = Integer.parseInt(idString) - 1;
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query(DartDASHSQLiteHelper.TABLE_TRANSACTIONS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (cursor.getLong(0) <= id) {
            cursor.moveToNext();
        }
        events.add(cursorToEvent(cursor));
        return events;
    }

    // Makes an exercise using the information in a row of the database table
    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();

        // Set the id, input type, and activity type information for the new exercise
        event.setId(cursor.getLong(0));
        event.setName(cursor.getString(1));
        event.setDate(cursor.getString(2));
        event.setAmount(Double.parseDouble(cursor.getString(3)));

        // Get transaction json string
        String jsonString = cursor.getString(4);
        ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
        try {
            transactionList = fromJSON(jsonString);
        }
        catch (JSONException e) {
            Log.d(TAG, "JSONException: could not read JSON transaction list string.");
        }
        event.setTransactions(transactionList);

        // Set synced
        int synced = cursor.getInt(5);
        if (synced == 0) {
            event.setSynced(false);
        }
        else if (synced == 1){
            event.setSynced(true);
        }

        // Set deleted
        int deleted = cursor.getInt(6);
        if (deleted == 0) {
            event.setDeleted(false);
        }
        else if (deleted == 1){
            event.setDeleted(true);
        }

        return event;
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

}