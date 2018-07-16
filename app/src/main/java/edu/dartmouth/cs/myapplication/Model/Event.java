package edu.dartmouth.cs.myapplication.Model;

import java.util.ArrayList;

/**
 * Stores an event's information
 */

public class Event {
    private long id;
    private ArrayList<Transaction> transactions;
    private String name;
    private String date;
    private double amount;
    private boolean synced;
    private boolean deleted;

    public Event() {
    }

    public Event(ArrayList<Transaction> transactions, String name, String date, double amount,
                 boolean synced, boolean deleted) {
        this.transactions = transactions;
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.synced = synced;
        this.deleted = deleted;
    }


    // ************************************** SETTERS ******************************************* //

    public void setId(long id) {
        this.id = id;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
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


    // ************************************** GETTERS ******************************************* //

    public long getId() {
        return id;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
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

}
