package edu.dartmouth.cs.myapplication.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Store's a transaction's information
 */

public class Transaction {
    private String name;
    private long dash;
    private double amount;
    private boolean synced;
    private boolean deleted;
    private Bitmap signature;

    public Transaction(String name, long dash, double amount, boolean synced, boolean deleted, Bitmap signature) {
        this.name = name;
        this.dash = dash;
        this.amount = amount;
        this.synced = synced;
        this.deleted = deleted;
        this.signature = signature;
    }


    // ************************************** SETTERS ******************************************* //

    public void setName(String name) {
        this.name = name;
    }

    public void setDash(long dash) {
        this.dash = dash;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setSignature(Bitmap signature) {
        this.signature = signature;
    }


    // ************************************** GETTERS ******************************************* //

    public String getName() {
        return name;
    }

    public long getDash() {
        return dash;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSynced() {
        return synced;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Bitmap getSignature() {
        return signature;
    }


    // ************************************** TOSTRING ****************************************** //

    // Returns unique String for given Transaction
    public String toString() {

        String signatureString = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (signature != null) {
            signature.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            signatureString = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
        }

        return name + ", " + Long.toString(dash) + ", " + Double.toString(amount) + ", "
                + synced + ", " + deleted + ", "
                + signatureString;
    }

}
