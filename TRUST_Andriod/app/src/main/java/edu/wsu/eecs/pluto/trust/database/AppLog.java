package edu.wsu.eecs.pluto.trust.database;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kameron on 1/22/2017.
 */

public class AppLog {
    private String event_num;
    private String event;
    private String id;
    private String date;
    private String time;
    private String platform;

    AppLog(String arg1, String arg2, String arg3, String arg4, String arg5, String arg6){
        event_num = arg1;
        event = arg2;
        id = arg3;
        date = arg4;
        time = arg5;
        platform = arg6;
    }

    public String get_event_num() {
        return event_num;
    }
    public String get_event() {
        return event;
    }
    public String get_id() { return id; }
    public String get_date() { return date; }
    public String get_time() { return time; }
    public String get_platform() { return platform; }


    public JSONObject toJSON () {
        JSONObject logJSON = new JSONObject();

        try {
            logJSON.put("event_num", event_num);
            logJSON.put("event", event);
            logJSON.put("id", id);
            logJSON.put("date", date);
            logJSON.put("time", time);
            logJSON.put("platform", platform);
        } catch (JSONException e) {
            String s = e.getMessage();
        }

        return logJSON;
    }
}
