package edu.wsu.eecs.pluto.trust;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.wsu.eecs.pluto.trust.database.AppLog;
import edu.wsu.eecs.pluto.trust.database.LogDBAdapter;
import edu.wsu.eecs.pluto.trust.database.User;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import static android.R.attr.action;

/**
 * Created by gene on 2/7/17.
 */

public class GlobalClass {

    private static final Object lockObject = new Object();

    // Logs the interaction to db
    public static void LogInteraction (final String action, final Context context) {
        class LogData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                synchronized (lockObject) {
                    UserDBAdapter udb = new UserDBAdapter(context);
                    LogDBAdapter lda = new LogDBAdapter(context);
                    udb.open();
                    String userID = udb.getID();
                    if (!userID.isEmpty()) {
                        lda.open();
                        lda.createLog(action, udb.getID());
                        lda.close();
                    }
                    udb.close();
                }
                return "";
            }
        }

        LogData p = new LogData();
        p.execute();
    }

    public static void sendLogData (final Context context) {

        class PostData extends AsyncTask<String, Void, String> {

            private int lenJSONArray;
            LogDBAdapter lda;
            private StringBuffer response = new StringBuffer(); // the response from server

            @Override
            protected String doInBackground(String... params) {
                synchronized (lockObject) {
                    lda = new LogDBAdapter(context); // initialize database instance

                    try {
                        // open logs table

                        lda.open();
                        ArrayList<AppLog> logs = lda.getAllLogs();
                        lda.close();

                        // create a json array of logs
                        JSONArray logJSONArray = new JSONArray();
                        for (AppLog log : logs) {
                            logJSONArray.put(log.toJSON());
                        }

                        // size of the json array to check valid response from server
                        lenJSONArray = logJSONArray.length();

                        try { // lets try sending it to the server
                            String url = "" /* This string would point to a php script on our server */;
                            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

                            //con.setRequestProperty("User-Agent", USER_AGENT);
                            con.setRequestProperty("Accept", "*/*");
                            con.setRequestProperty("Content-Type", "application/json");

                            con.setDoOutput(true);
                            con.setDoInput(true);

                            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                            wr.writeBytes(logJSONArray.toString());
                            wr.flush();
                            wr.close();

                            System.out.println("\nSending 'POST' request to URL : " + url);

                            InputStream it = con.getInputStream();
                            InputStreamReader inputs = new InputStreamReader(it);

                            BufferedReader in = new BufferedReader(inputs);
                            String inputLine;

                            // get the response from server
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }

                            in.close();

                            System.out.println("Server says : " + response.toString());

                        } catch (IOException e) {
                            // catch
                        }
                    } catch (Exception e) {
                        // catch exception
                        String s = e.getMessage();
                    }

                    String result = response.toString();
                    if (!result.equals("") && lenJSONArray == Integer.parseInt(result)) {
                        // TODO delete from in-app db -> table log
                        System.out.println("Logs have been successfully transferred to server db");

                        // TODO: Delete logs from inapp db
                        lda.open();
                        lda.deleteAllLogs();
                        lda.close();
                    }
                }
                return "";
            }
        }

        UserDBAdapter udb = new UserDBAdapter(context);
        udb.open();
        if (!udb.getID().isEmpty()) {
            udb.close();
            PostData p = new PostData();
            p.execute();
        } else {
            udb.close();
        }
    }
}
