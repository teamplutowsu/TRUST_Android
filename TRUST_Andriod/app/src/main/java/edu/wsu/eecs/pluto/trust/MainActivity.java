package edu.wsu.eecs.pluto.trust;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.util.TypedValue;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.GregorianCalendar;
import java.util.TimeZone;

import edu.wsu.eecs.pluto.trust.database.AppLog;
import edu.wsu.eecs.pluto.trust.database.Database;
import edu.wsu.eecs.pluto.trust.database.LogDBAdapter;
import edu.wsu.eecs.pluto.trust.database.ModuleDBAdapter;
import edu.wsu.eecs.pluto.trust.database.User;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import android.os.Vibrator;

/**
 * Created by Gene on 11/2/16.
 */

public class MainActivity extends AppCompatActivity {

    ProgressBar _progressBar;
    ArrayList<String> main_list;
    String _mnumber;
    String _rindex;
    Toast toast;
    Database DB;

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _mnumber = "9";
        _rindex = "1";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String fontsetting = settings.getString("font_size","Error");

        switch (fontsetting){
            case "Small": setTheme(R.style.SmallTheme);break;
            case "Medium": setTheme(R.style.MediumTheme); break;
            case "Medium Large": setTheme(R.style.MediumLargeTheme);break;
            case "Large": setTheme(R.style.LargeTheme);break;
            default: setTheme(R.style.MediumTheme); break;
        }

        DB = new Database(getApplicationContext());
        DB.open(); DB.close();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        _progressBar = (ProgressBar) findViewById(R.id.moduleProgress);
        _progressBar.setMax(52);
        getProgress();
        SetActionBarFormat();

        // Setup vibrator
        vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        ArrayAdapter<String> mainAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, main_list) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                TextView selection = (TextView) view.findViewById(android.R.id.text1);

                // Set the text size 25 dip for ListView each item
                selection.setTextSize(TypedValue.COMPLEX_UNIT_DIP,36);
                selection.setHeight(30);
                selection.setMinimumHeight(300);
                selection.setGravity(Gravity.CENTER);

                // Return the view
                return view;
            }
        };

        GlobalClass.LogInteraction("Entered the Main Menu: OnCreate()", getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
        //GlobalClass.test();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getProgress();

        GlobalClass.LogInteraction("Entered the Main Menu: OnResume()", getApplicationContext());
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
    }

    // Get's the JSON format string from given url
    protected String getData(String uri) {
        InputStream inputStream = null;
        String result = null;
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            inputStream = conn.getInputStream();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            if(toast != null)
                toast.cancel();
            toast = Toast.makeText(MainActivity.this, "Error connecting to network.", Toast.LENGTH_LONG);
            toast.show();

        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
        return result;
    }

    // gets last accessed page in module
    private void getProgress() {

        ModuleDBAdapter mdb = new ModuleDBAdapter(getApplicationContext());
        mdb.open();
        _rindex = mdb.getLastPageAccessed(_mnumber);
        mdb.close();

        _progressBar.setProgress(Integer.valueOf(_rindex));
    }

    private void SetActionBarFormat() {
        // Get the ActionBar
        ActionBar ab = getSupportActionBar();

        // Create a TextView programmatically.
        TextView tv = new TextView(getApplicationContext());

        // Create a LayoutParams for TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView

        // Apply the layout parameters to TextView widget
        tv.setLayoutParams(lp);

        // Set text to display in TextView
        tv.setText("TRUST");

        tv.setTextSize(30);

        // Set the text color of TextView
        tv.setTextColor(android.graphics.Color.WHITE);

        // Set TextView text alignment to center
        tv.setGravity(Gravity.CENTER);

        // Set the ActionBar display option
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Finally, set the newly created TextView as ActionBar custom view
        ab.setCustomView(tv);
    }

    public void onClickedACP(View view) {
        if(toast != null)
            toast.cancel();

        // Calling to PageActivity
        Intent i = new Intent(getApplicationContext(), PageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra("mnumber", "9");
        i.putExtra("pnumber", "-1");

        GlobalClass.LogInteraction("Pushed ACP Button", getApplicationContext());

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public void onClickedSettings(View view) {
        if(toast != null)
            toast.cancel();


        GlobalClass.LogInteraction("Pushed Settings Button", getApplicationContext());

        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public void onClickedBookmarks(View view) {
        if(toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pushed Bookmarks Button", getApplicationContext());

        Intent i = new Intent(getApplicationContext(), BookmarkActivity.class);

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public void onClickedAR(View view) {
        if(toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pushed Additional Reading Button", getApplicationContext());

        Intent i = new Intent(getApplicationContext(), ModulesActivity.class);

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public  void onClickedTutorials(View view){
        if(toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pushed Tutorials Button", getApplicationContext());

        Intent i = new Intent(getApplicationContext(), TutorialListActivity.class);

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public void onClickedNotAvailable(View view) {
        if(toast != null)
            toast.cancel();
        toast = Toast.makeText(MainActivity.this, "Sorry, this feature is not yet available.", Toast.LENGTH_LONG);

        // Run vibrator
        vibrator.vibrate(20);

        toast.show();

    }
}


