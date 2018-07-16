package edu.dartmouth.cs.myapplication.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Reads to and writes from the database
 */

public class DartDASHSQLiteHelper extends SQLiteOpenHelper {

    // Table name, version, description, and id number
    private static final String DATABASE_NAME = "transactions.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "_id";

    // Names of columns in table
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTIONS = "transactions";
    public static final String COLUMN_SYNCED = "synced";
    public static final String COLUMN_DELETED = "deleted";

    // Database creation string, sql statement
    private static final String DATABASE_CREATE = "create table " + TABLE_TRANSACTIONS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_DATE + " text not null, " +
            COLUMN_AMOUNT + " text not null, " +
            COLUMN_TRANSACTIONS + " text not null, " +
            COLUMN_SYNCED + " integer, " +
            COLUMN_DELETED + " integer);";

    DartDASHSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(database);
    }

}
