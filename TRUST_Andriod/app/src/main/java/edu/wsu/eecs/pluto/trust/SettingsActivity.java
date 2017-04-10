package edu.wsu.eecs.pluto.trust;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import static edu.wsu.eecs.pluto.trust.BuildConfig.*;


public class SettingsActivity extends AppCompatActivity {

    String _version = VERSION_NAME;
    Toast toast;
    ListView settingsListView;
    TextView versionUIDTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set theme for the activity.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String fontSetting = settings.getString("font_size", "Error");
        switch (fontSetting) {
            case "Small":
                setTheme(R.style.SmallTheme);
                break;
            case "Medium":
                setTheme(R.style.MediumTheme);
                break;
            case "Medium Large":
                setTheme(R.style.MediumLargeTheme);
                break;
            case "Large":
                setTheme(R.style.LargeTheme);
                break;
            default:
                setTheme(R.style.MediumTheme);
                break;
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Sets up the actions bars on the top and bottom.
        SetActionBarFormat();


        //Populate settings' list view:
        settingsListView = (ListView) findViewById(R.id.settingsListView);
        ArrayList<String> settingsList = new ArrayList<String>();
        settingsList.add("Font Settings");
        settingsList.add("Administrator Settings");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingsActivity.this, R.layout.bookmark_textview, R.id.BookmarkEntry, settingsList);

        settingsListView.setAdapter(adapter);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {SettingsItemPressed(parent, view, position, id);}
        });

        GlobalClass.LogInteraction("Entered Settings Activity: OnCreate()", getApplicationContext());
    }

    //On pause take this time to syc with the server.
    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
        //GlobalClass.test();
    }

    //Get rid of any toasts before backing out of the activity.
    @Override
    public void onBackPressed() {
        if (toast != null)
            toast.cancel();

        super.onBackPressed();
        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
    }

    @Override
    public void onResume(){
        super.onResume();

        //Get the uid for logging.
        UserDBAdapter udb = new UserDBAdapter(this.getApplicationContext());
        udb.open();
        String _userID = udb.getID();
        udb.close();
        //Set the bar on the bottom's text so it reports the app version and user id.
        if(versionUIDTextView == null)
            versionUIDTextView = (TextView) findViewById(R.id.VersionUIDTextBox);

        if (_userID.equals(""))
            versionUIDTextView.setText("App Version: "+_version+"      UserID: -");
        else
            versionUIDTextView.setText("App Version: "+_version+"      UserID:" + _userID);

        GlobalClass.LogInteraction("Entered Settings Activity: OnResume()", getApplicationContext());
    }

    //For setting up the bars at the bottom and top of the screen.
    private void SetActionBarFormat() {
        //Create and initalize the action bar on the top.
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.settings_actionbar, null);
        ab.setCustomView(customView);
        ab.setDisplayShowCustomEnabled(true);

        //Set it's title to settings.
        TextView titleTextView = (TextView) customView.findViewById(R.id.titleTextView);
        titleTextView.setText("Settings");


        //Get the uid for logging.
        UserDBAdapter udb = new UserDBAdapter(this.getApplicationContext());
        udb.open();
        String _userID = udb.getID();
        udb.close();
        //Set the bar on the bottom's text so it reports the app version and user id.
        TextView versionUIDTextView = (TextView) findViewById(R.id.VersionUIDTextBox);
        if (_userID.equals(""))
            versionUIDTextView.setText("App Version: "+_version+"      UserID: -");
        else
            versionUIDTextView.setText("App Version: "+_version+"      UserID:" + _userID);
    }

    //Hardcoded method for which item in the list leads to what view.
    private void SettingsItemPressed(AdapterView<?> parent, View view, int position, long id) {
        switch(position){
            case 0: FontSettingsPressed(); break;
            case 1: AdminSettingsPressed(); break;
            default: return;
        }
    }

    private  void FontSettingsPressed(){
        if(toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pressed Font Settings Button", getApplicationContext());
        Intent i = new Intent(getApplicationContext(), FontChangeActivity.class);
        startActivity(i);
    }

    private void AdminSettingsPressed(){
        if(toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pressed Admin Settings Button", getApplicationContext());
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }

}
