package edu.wsu.eecs.pluto.trust;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.wsu.eecs.pluto.trust.database.Database;
import edu.wsu.eecs.pluto.trust.database.ModuleDBAdapter;

public class TutorialListActivity extends AppCompatActivity {

    private ArrayList<Pair<String,String>> tutorials = new ArrayList<>();
    private ListView tutorialListView;//The LiewView containing the list of bookmarks.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_tutorial_list);



        tutorials.add(new Pair<>("Application Overview", "https://www.youtube.com/embed/ZZK1Y_CKoSg?ecver=1"));
        tutorials.add(new Pair<>("Page View Overview", "https://www.youtube.com/embed/UzNrbJGWmO4?ecver=1"));
        tutorials.add(new Pair<>("How to Use Table of Contents", "https://www.youtube.com/embed/NxQCadfbwrs?ecver=1"));
        tutorials.add(new Pair<>("How to Change Font Size", "https://www.youtube.com/embed/pYyl68EA1E8?ecver=1"));
        tutorials.add(new Pair<>("How to Add a Bookmark", "https://www.youtube.com/embed/5sFTNgB1-BE?ecver=1"));
        tutorials.add(new Pair<>("How to Delete a Bookmark", "https://www.youtube.com/embed/YRP8Z1X6ORY?ecver=1"));
        tutorials.add(new Pair<>("How to Use Text to Speech", "https://www.youtube.com/embed/NryKbrCP4I4?ecver=1"));
        tutorials.add(new Pair<>("How to Access Additional Reading", "https://www.youtube.com/embed/DwWkMQwJxPk?ecver=1"));

        ArrayList<String> tutorialStrings = new ArrayList<>();
        for(int i = 0; i < tutorials.size(); i++){ tutorialStrings.add(tutorials.get(i).first); }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.bookmark_textview, R.id.BookmarkEntry, tutorialStrings);


        tutorialListView = (ListView) findViewById(R.id.tutorialslist);
        tutorialListView.setAdapter(adapter);

        tutorialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {TutorialPressed(parent, view, position, id);}
        });

        GlobalClass.LogInteraction("Entered the Tutorial List View: OnCreate()", getApplicationContext());
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
        GlobalClass.LogInteraction("Entered the Tutorial List View: OnResume()", getApplicationContext());
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
    }

    public void TutorialPressed(AdapterView<?> parent, View view, int position, long id) {

        GlobalClass.LogInteraction("Pushed Tutorial Video Button: "+tutorials.get(position).first, getApplicationContext());

        if(position >= 0 && position < tutorials.size()){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(tutorials.get(position).second)));
        }
    }
}


