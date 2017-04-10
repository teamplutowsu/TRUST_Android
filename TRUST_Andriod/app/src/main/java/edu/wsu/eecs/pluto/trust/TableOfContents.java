package edu.wsu.eecs.pluto.trust;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.wsu.eecs.pluto.trust.database.Module;
import edu.wsu.eecs.pluto.trust.database.ModuleDBAdapter;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import static edu.wsu.eecs.pluto.trust.R.layout.page;

public class TableOfContents extends AppCompatActivity {

    Module currentModule;
    ArrayList<String> chapterTitleStrings;//The list of strings to be displayed, deriveed from those bookmarks.
    ArrayList<String> chapterPageNumbers;
    ArrayList<String> chapterTitle;
    ListView chapterListView;//The LiewView containing the list of bookmarks.
    ProgressDialog pd;
    String _mnumber;
    Boolean populatingTable = false;

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
        setContentView(R.layout.activity_table_of_contents);

        Bundle b = getIntent().getExtras();
        _mnumber = b.getString("mnumber");

        chapterTitleStrings = new ArrayList<>();
        chapterPageNumbers = new ArrayList<>();
        chapterTitle = new ArrayList<>();
        chapterListView = (ListView) findViewById(R.id.chapterList);

        SetActionBarFormat();
        GetChapterTable();

        GlobalClass.LogInteraction(String.format("Entered Table Of Contents for Module %s: OnCreate()", _mnumber), getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if(!populatingTable) {
            GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
            super.onBackPressed();
            Intent i = new Intent(getApplicationContext(), PageActivity.class);
            i.putExtra("mnumber", String.valueOf(Integer.parseInt(_mnumber)));
            i.putExtra("pnumber", "-1");
            startActivity(i);
        }
        GlobalClass.LogInteraction("Tried to Press Phones Back Button But App is Busy Loading", getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalClass.LogInteraction(String.format("Entered Table Of Contents for Module %s: onResume()", _mnumber), getApplicationContext());
    }

    //The action bar is the strip at the top of the page.
    private void SetActionBarFormat() {
        ActionBar mActionBar = getSupportActionBar();
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.fontchange_actionbar, null);
        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);

        TextView title = (TextView) customView.findViewById(R.id.textView);
        title.setText("Table Of Contents");

        ImageButton backButton = (ImageButton) customView.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {onBackPressed();}
        });
    }

    //Takes the array of chapter titles extracted from the database and puts them onscreen.
    private void PopulateChapterTable(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.bookmark_textview, R.id.BookmarkEntry, chapterTitleStrings);

        chapterListView.setAdapter(adapter);

        //Set a listener for when a bookmark is clicked.
        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChapterPressed(parent, view, position, id);}
        });
    }

    //This 'function' creates a background task to do the extraction of the chapters form the database and puts a pop-up progress bar during the operation.
    public void GetChapterTable() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(TableOfContents.this);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle("Fetching Contents...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(false);
                pd.show();

                populatingTable = true;
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                ModuleDBAdapter moduleDBAdapter = new ModuleDBAdapter(getBaseContext());
                moduleDBAdapter.open();

                currentModule = moduleDBAdapter.getModule(_mnumber);
                int numberOfPages = Integer.parseInt(currentModule.getNpages());

                //------------------The the Subtitle from each page.----------------------------------------------------------------------------------
                String subtitle = "Error";
                String page = "Error";
                if (Integer.parseInt(_mnumber) < 10) {
                    _mnumber = "0" + _mnumber;
                }

                for(int i = 1; i <= numberOfPages; i++) {
                    try {
                        page = String.valueOf(i);
                        if (Integer.parseInt(page) < 10) {
                            page = "0" + page;
                        }

                        InputStream istream = getResources().openRawResource(getResources().getIdentifier("page_" + _mnumber + "_" + page, "raw", getPackageName()));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
                        StringBuilder builder = new StringBuilder();

                        String fileLine;
                        for (int j = 0; j < 5 && (fileLine = reader.readLine()) != null; j++) {
                            builder.append(fileLine);
                        }

                        String fileData = builder.toString();

                        // Parse text from fileData
                        subtitle = fileData.substring(fileData.indexOf("<Subtitle>") + "<Subtitle>".length(), fileData.indexOf("</Subtitle>"));

                    } catch (Exception e) {}

                    if(!subtitle.equals("Error")){//If we correctly read from the database:

                        //if(chapterTitle.size() == 0 || !chapterTitle.get(chapterTitle.size()-1).equals(subtitle)) {
                            chapterPageNumbers.add(String.valueOf(i));
                            chapterTitleStrings.add((Integer.parseInt(page))+" | "+ subtitle);
                            chapterTitle.add(subtitle);
                        //}

                    } else {//If there was an error reading from the database:
                        chapterPageNumbers.add(String.valueOf(-1));
                        chapterTitleStrings.add("Exception Reading From Database! Panic!");
                        chapterTitle.add(subtitle);
                    }

                    pd.setProgressNumberFormat(i + " / " + numberOfPages);
                    pd.setProgress((i * 100) / numberOfPages);
                }
                //----------------------------------------------------------------------------------------------------

                moduleDBAdapter.close();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd!=null) {
                    pd.dismiss();
                }
                populatingTable = false;
                PopulateChapterTable();
            }

        };
        task.execute((Void[])null);
    }

    //Function called each time one of the displayed chapters are pressed.
    private void ChapterPressed(AdapterView<?> parent, View view, int position, long id) {
        if (!(chapterPageNumbers == null || chapterPageNumbers.isEmpty())) {

            super.onBackPressed();

            Intent i = new Intent(getApplicationContext(), PageActivity.class);
            i.putExtra("mnumber", String.valueOf(Integer.parseInt(_mnumber)));
            i.putExtra("pnumber", chapterPageNumbers.get(position));

            GlobalClass.LogInteraction("Pressed a Chapter on the Table Of Contents linking to: Module " + Integer.parseInt(_mnumber) + " Page " + chapterPageNumbers.get(position), getApplicationContext());

            startActivity(i);
        }
    }
}
