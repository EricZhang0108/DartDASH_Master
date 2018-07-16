package edu.dartmouth.cs.myapplication.Model;

/**
 * Stores an ongoing event's information
 */

public class OngoingEvent {
    private String name;
    private String date;
    private String email;

    public OngoingEvent() {
    }

    public OngoingEvent(String name, String date, String email) {
        this.name = name;
        this.date = date;
        this.email = email;
    }


    // ************************************** SETTERS ******************************************* //

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // ************************************** GETTERS ******************************************* //

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getEmail() {
        return email;
    }
}
