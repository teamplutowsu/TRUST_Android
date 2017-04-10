package edu.wsu.eecs.pluto.trust.database;

/**
 * Created by Kameron on 1/30/2017.
 */

public class User {

    private String id;
    private String logsLastSentDate;

    User(String arg1, String arg2){

        id = arg1;
        logsLastSentDate = arg2;
    }

    public String get_id() {
        return id;
    }
    public String get_logsLastSentDate() { return logsLastSentDate; }
}

