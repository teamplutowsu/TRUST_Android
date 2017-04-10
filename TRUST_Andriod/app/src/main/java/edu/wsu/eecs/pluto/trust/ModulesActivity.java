package edu.wsu.eecs.pluto.trust;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import edu.wsu.eecs.pluto.trust.database.LogDBAdapter;
import edu.wsu.eecs.pluto.trust.database.ModuleDBAdapter;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import android.os.Vibrator;

import static android.os.Build.VERSION_CODES.M;

// TODO: add network timeouts

public class ModulesActivity extends AppCompatActivity {

    ImageView module1ImageView;
    ImageView module4ImageView;
    ImageView module6ImageView;
    TextView module1TextView;
    TextView module4TextView;
    TextView module6TextView;
    ProgressBar module1ProgressBar;
    ProgressBar module4ProgressBar;
    ProgressBar module6ProgressBar;
    ModuleDBAdapter mda;

    Toast toast;

    private Vibrator vibrator;

    @Override protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String fontsetting = settings.getString("font_size","Error");

        switch (fontsetting){
            case "Small": setTheme(R.style.SmallTheme);break;
            case "Medium": setTheme(R.style.MediumTheme); break;
            case "Medium Large": setTheme(R.style.MediumLargeTheme);break;
            case "Large": setTheme(R.style.LargeTheme);break;
            default: setTheme(R.style.MediumTheme); break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.additional_reading);
        SetActionBarFormat();


        //TODO: Get userID from database

        // Load widgets
        module1ImageView = (ImageView) findViewById(R.id.module1Image);
        module4ImageView = (ImageView) findViewById(R.id.module4Image);
        module6ImageView = (ImageView) findViewById(R.id.module6Image);
        module1TextView = (TextView) findViewById(R.id.module1Text);
        module4TextView = (TextView) findViewById(R.id.module4Text);
        module6TextView = (TextView) findViewById(R.id.module6Text);
        module1ProgressBar = (ProgressBar) findViewById(R.id.module1Progress);
        module4ProgressBar = (ProgressBar) findViewById(R.id.module4Progress);
        module6ProgressBar = (ProgressBar) findViewById(R.id.module6Progress);

        mda = new ModuleDBAdapter(getApplicationContext());

        // Setup vibrator
        vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        loadImages();
        setText();
        setProgress();

        GlobalClass.LogInteraction("Entered the Additional Reading Page: OnCreate()", getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalClass.LogInteraction("Entered the Additional Reading Page: OnResume()", getApplicationContext());
        setProgress();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (toast != null)
            toast.cancel();

        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
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
        tv.setText("Additional Reading");

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

    private void loadImages() {
        // TODO: Set images to image views once images are in filesystem

        // Placeholders for now
        // Get resource ids
        int imageID1 = getResources().getIdentifier("img_01_01", "raw", getPackageName());
        int imageID2 = getResources().getIdentifier("img_04_thumb", "raw", getPackageName());
        int imageID3 = getResources().getIdentifier("img_06_thumb", "raw", getPackageName());

        // Populate image views using resource ids
        module1ImageView.setImageResource(imageID1);
        module4ImageView.setImageResource(imageID2);
        module6ImageView.setImageResource(imageID3);
    }

    private void setText() {
        module1TextView.setText("Taking Control of Your Heart Failure");
        module4TextView.setText("Self-Care");
        module6TextView.setText("Managing Feelings About Heart Failure");
    }

    private void setProgress() {
        // Query database for each module page last accessed and npages
        mda.open();
        module1ProgressBar.setMax(Integer.parseInt(mda.getNPages("1")));
        module1ProgressBar.setProgress(Integer.parseInt(mda.getLastPageAccessed("1")));
        module4ProgressBar.setMax(Integer.parseInt(mda.getNPages("4")));
        module4ProgressBar.setProgress(Integer.parseInt(mda.getLastPageAccessed("4")));
        module6ProgressBar.setMax(Integer.parseInt(mda.getNPages("6")));
        module6ProgressBar.setProgress(Integer.parseInt(mda.getLastPageAccessed("6")));
        mda.close();

    }

    public void onClickedModule(View view) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);

        switch (view.getId()) {
            case R.id.module1:
                i = new Intent(getApplicationContext(), PageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("mnumber", "1");
                i.putExtra("pnumber", "-1");
                if(toast != null) {
                    toast.cancel();
                }
                GlobalClass.LogInteraction("Pressed Module 1 Button", getApplicationContext());
                break;
            case R.id.module4:
                i = new Intent(getApplicationContext(), PageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("mnumber", "4");
                i.putExtra("pnumber", "-1");
                if(toast != null) {
                    toast.cancel();
                }
                GlobalClass.LogInteraction("Pressed Module 4 Button", getApplicationContext());
                break;
            case R.id.module6:
                i = new Intent(getApplicationContext(), PageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("mnumber", "6");
                i.putExtra("pnumber", "-1");
                if(toast != null) {
                    toast.cancel();
                }
                GlobalClass.LogInteraction("Pressed Module 6 Button", getApplicationContext());
                break;
        }

        // Run vibrator
        vibrator.vibrate(20);

        startActivity(i);
    }

    public void openNotAvailableToast() {
        if(toast != null)
            toast.cancel();
        toast = Toast.makeText(ModulesActivity.this, "Sorry, this module is not yet available.", Toast.LENGTH_LONG);
        toast.show();
    }
}
