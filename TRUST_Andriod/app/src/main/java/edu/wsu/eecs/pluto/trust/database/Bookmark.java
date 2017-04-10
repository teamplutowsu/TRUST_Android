package edu.wsu.eecs.pluto.trust.database;

/**
 * Created by Kameron on 1/25/2017.
 */

public class Bookmark {

    private  String module;
    private  String page;

    public Bookmark(String arg1, String arg2){
        module = arg1;
        page = arg2;
    }

    public String getModule() {
        return module;
    }

    public String getPage() {
        return page;
    }
}
