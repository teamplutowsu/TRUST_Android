package edu.wsu.eecs.pluto.trust;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.IntegerRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import android.view.LayoutInflater;
import android.view.View;

import android.os.Vibrator;

import edu.wsu.eecs.pluto.trust.database.Bookmark;
import edu.wsu.eecs.pluto.trust.database.BookmarkDBAdapter;
import edu.wsu.eecs.pluto.trust.database.LogDBAdapter;
import edu.wsu.eecs.pluto.trust.database.ModuleDBAdapter;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import static android.R.attr.duration;
import static android.R.attr.max;
import static android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID;
import static android.speech.tts.TextToSpeech.SUCCESS;
import static edu.wsu.eecs.pluto.trust.R.id.add;
import static edu.wsu.eecs.pluto.trust.R.id.bookmark;
import static edu.wsu.eecs.pluto.trust.R.id.settings;
import static edu.wsu.eecs.pluto.trust.R.layout.page;

/**
 * Created by Gene on 10/28/16.
 */

public class PageActivity extends AppCompatActivity {

    WebView _pageWebView;
    TextView _pageSubtitleView;
    LinearLayout _imageBar;
    ImageView _image1;
    ImageView _image2;
    ImageView _image3;
    ProgressBar _pageProgressBar;
    ImageButton bookmark_button;
    ImageButton tts_button;
    Toast toast;
    Boolean adding_deleting_bookmark = false;

    String _mnumber, _content, _pid, _title, _subtitle, _npages, _image;
    String _duration;
    long _startTime, _endTime;
    int imageBackgroundAlpha = 0;

    BookmarkDBAdapter bda; //bookmarks database
    ModuleDBAdapter mda;
    LogDBAdapter lda;

    private TextToSpeech myTTS;

    private static final String TAG_RESULT = "result";
    private static final String TAG_DURATION = "duration";

    private Vibrator vibrator;

    // From ModuleActivity <-Not necessarily.
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
        setContentView(page);

        bda = new BookmarkDBAdapter(getApplicationContext());
        lda = new LogDBAdapter(getApplicationContext());
        // Get access to views
        _pageWebView = (WebView) findViewById(R.id.page_text);
        _imageBar = (LinearLayout) findViewById(R.id.imageBar);
        _image1 = (ImageView) findViewById(R.id.image1);
        _image2 = (ImageView) findViewById(R.id.image2);
        _image3 = (ImageView) findViewById(R.id.image3);
        _pageSubtitleView = (TextView) findViewById(R.id.subtitle);
        _pageProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        _pageProgressBar.setProgress(0);

        //Acessing settings, applying them to the web view.
        WebSettings webSettings = _pageWebView.getSettings();

        switch (fontsetting){
            case "Small": webSettings.setDefaultFontSize(20);break;
            case "Medium": webSettings.setDefaultFontSize(25);break;
            case "Medium Large": webSettings.setDefaultFontSize(27);break;
            case "Large": webSettings.setDefaultFontSize(30);break;
            default:  webSettings.setDefaultFontSize(25);break;
        }

        // Set image backgrounds to desired transparency (declared at the top)
        _imageBar.getBackground().setAlpha(imageBackgroundAlpha);
        _image1.getBackground().setAlpha(imageBackgroundAlpha);
        _image2.getBackground().setAlpha(imageBackgroundAlpha);
        _image3.getBackground().setAlpha(imageBackgroundAlpha);

        Bundle b = getIntent().getExtras();
        _mnumber = b.getString("mnumber");
        _pid = b.getString("pnumber");

        mda = new ModuleDBAdapter(getApplicationContext());
        if (_pid.equals("-1")) {
            getLastPage(); // get the last accessed page of module
            getPageData();
        } else {
            getPageData();//Otherwise gets the given page.
        }

        // Setup vibrator
        vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        GlobalClass.LogInteraction(String.format("Entered PageView: OnCreate(): Module: %s, Page: %s", _mnumber, _pid), getApplicationContext());
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == SUCCESS) {
                        myTTS.setLanguage(Locale.US);
                        myTTS.setSpeechRate((float) 0.8);
                        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {

                            }

                            @Override
                            public void onDone(String utteranceId) {
                                GlobalClass.LogInteraction("TTS Finished.", getApplicationContext());
                            }

                            @Override
                            public void onError(String utteranceId) {

                            }
                        });
                    }
                }
            });
       // }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bda.open();//This updates the bookmark icon in case they changed a bookmark while away.
        ArrayList<Bookmark> bookmarks = bda.getAllBookmarks();
        bda.close();
        bookmark_button.setImageResource(R.drawable.ic_bookmark_white_36dp);
        for (Bookmark bk : bookmarks) {
            if (bk.getPage().equals(_pid) && bk.getModule().equals(_mnumber))
                bookmark_button.setImageResource(R.drawable.ic_bookmark_red_36dp);
        }

        GlobalClass.LogInteraction(String.format("Entered PageView: OnResume(): Module: %s, Page: %s", _mnumber, _pid), getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
        myTTS.stop();
        //GlobalClass.test();
    }

    @Override
    public void onBackPressed() {
        if (adding_deleting_bookmark)
            return;

        if (toast != null)
            toast.cancel();

        super.onBackPressed();

        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
    }

    protected void showContent() {
        try {
            // Html.fromHtml is deprecated but its replacement will NOT work on API 24 and back, so using this anyway
            _pageWebView.loadData(_content, "text/html; charset=utf-8", "utf-8");
            _pageWebView.setBackgroundColor(Color.TRANSPARENT);             // This and line below are required to preserve the rounded corners of the background
            _pageWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            _pageSubtitleView.setText(_subtitle);
            _pageProgressBar.setMax(Integer.valueOf(_npages));
            _pageProgressBar.setProgress(Integer.valueOf(_pid));
            _startTime = System.currentTimeMillis();

        } catch (Exception e) {
            // TODO: catch something... or log something
        }
    }

    // gets last accessed page in module
    private void getLastPage() {

        mda.open();
        _pid = mda.getLastPageAccessed(_mnumber);
        mda.close();

    }

    // gets page of module
    public void getPageData() {
        String module = "Error";
        String pid = "Error";
        String image = "Error";
        String title = "Error";
        String subtitle = "Error";
        String content = "Error";

        try {
            // Update last page
            mda.open();
            mda.updateLastPage(_mnumber, _pid);
            mda.close();

            String page = _pid;
            if (Integer.parseInt(page) < 10) {
                page = "0" + page;
            }
            String mnumber = _mnumber;
            if (Integer.parseInt(_mnumber) < 10) {
                mnumber = "0" + mnumber;
            }

            InputStream istream = getResources().openRawResource(getResources().getIdentifier("page_" + mnumber + "_" + page, "raw", getPackageName()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
            StringBuilder builder = new StringBuilder();
            String fileLine;

            while ((fileLine = reader.readLine()) != null) {
                builder.append(fileLine);
            }

            //TODO: Make sure file module and page match what we're expecting

            String fileData = builder.toString();

            // Parse text from fileData
            module = fileData.substring(fileData.indexOf("<Module>") + "<Module>".length(), fileData.indexOf("</Module>"));
            pid = fileData.substring(fileData.indexOf("<Page>") + "<Page>".length(), fileData.indexOf("</Page>"));
            image = fileData.substring(fileData.indexOf("<Image>") + "<Image>".length(), fileData.indexOf("</Image>"));
            title = fileData.substring(fileData.indexOf("<Title>") + "<Title>".length(), fileData.indexOf("</Title>"));
            subtitle = fileData.substring(fileData.indexOf("<Subtitle>") + "<Subtitle>".length(), fileData.indexOf("</Subtitle>"));
            content = fileData.substring(fileData.indexOf("<Content>") + "<Content>".length(), fileData.indexOf("</Content>"));

        } catch (Exception e) {
            // TODO: Log file opening error
        }

        _image = image;
        _title = title;
        _subtitle = subtitle;
        _content = content;
        try {
            // TODO: Get _npages from database, shouldn't be hardcoded
            mda.open();
            _npages = mda.getNPages(_mnumber);
            mda.close();

        } catch (Exception e) {
            // TODO: Log database read error
        }

        getPageImage();
        SetActionBarFormat();
        showContent();
    }

    // gets page image data
    public void getPageImage() {

        if (_mnumber.equals("1")) {
            // Get resource ids
            int imageID1 = getResources().getIdentifier("img_01_02", "raw", getPackageName());
            int imageID2 = getResources().getIdentifier("img_01_03", "raw", getPackageName());
            int imageID3 = getResources().getIdentifier("img_01_04", "raw", getPackageName());

            // Populate image views using resource ids
            _image1.setImageResource(imageID1);
            _image2.setImageResource(imageID2);
            _image3.setImageResource(imageID3);
        }
        else {
            // Images belong to page in triplets
            int imageID = Integer.parseInt(_image) * 3;

            // Get image tag base string
            String imageTagBase = "";
            switch (_mnumber) {
                case "9":
                    imageTagBase = "img_09_";
                    break;
                case "4":
                    imageTagBase = "img_04_";
                    break;
                case "6":
                    imageTagBase = "img_06_";
                    break;
                default:
                    imageTagBase = "img_09_";
            }

            // Add '0's to the beginning if int value less than 10 ( 9 -> 09 )
            String tag1 = Integer.toString(imageID);
            if (imageID < 10) {
                tag1 = "0" + tag1;
            }
            String tag2 = Integer.toString(imageID - 1);
            if (imageID - 1 < 10) {
                tag2 = "0" + tag2;
            }
            String tag3 = Integer.toString(imageID - 2);
            if (imageID - 2 < 10) {
                tag3 = "0" + tag3;
            }

            // Get resource ids
            int imageID1 = getResources().getIdentifier(imageTagBase + tag1, "raw", getPackageName());
            int imageID2 = getResources().getIdentifier(imageTagBase + tag2, "raw", getPackageName());
            int imageID3 = getResources().getIdentifier(imageTagBase + tag3, "raw", getPackageName());

            // Populate image views using resource ids
            _image1.setImageResource(imageID1);
            _image2.setImageResource(imageID2);
            _image3.setImageResource(imageID3);
        }

    }

    // sets the title and previous/next buttons
    private void SetActionBarFormat() {

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.menu_page_custom, null);
        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);

        ImageButton prev = (ImageButton) customView.findViewById(R.id.action_prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidefromLeftToRight(_pageWebView);
                ChangePage(-1);
            }
        });

        bookmark_button = (ImageButton) customView.findViewById(R.id.bookmark);
        bda.open();
        ArrayList<Bookmark> bookmarks = bda.getAllBookmarks();
        bda.close();
        bookmark_button.setImageResource(R.drawable.ic_bookmark_white_36dp);
        for (Bookmark bk : bookmarks) {//Changes the color of the bookmark icon if the page is bookmarked or not.
            if (bk.getPage().equals(_pid) && bk.getModule().equals(_mnumber))
                bookmark_button.setImageResource(R.drawable.ic_bookmark_red_36dp);
        }

        bookmark_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookmarkButtonPressed();
            }
        });

        tts_button = (ImageButton) customView.findViewById(R.id.TTS);
        tts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTSButtonPressed();
            }
        });

        ImageButton tableOfContent = (ImageButton) customView.findViewById(R.id.tableOfContents);
        tableOfContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PageActivity.super.onBackPressed();
                Intent i = new Intent(getApplicationContext(), TableOfContents.class);
                i.putExtra("mnumber", _mnumber);
                startActivity(i);
            }
        });



        ImageButton next = (ImageButton) customView.findViewById(R.id.action_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidefromRightToLeft(_pageWebView);
                ChangePage(1);
            }
        });

        // Enable / Disable prev / next buttons base on location in module
        prev.setEnabled(true);
        next.setEnabled(true);
        if (Integer.parseInt(_pid) == 1) prev.setEnabled(false);
        if (Integer.parseInt(_pid) == Integer.parseInt(_npages)) next.setEnabled(false);
    }

    private void TTSButtonPressed() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (myTTS.isSpeaking()) {
                GlobalClass.LogInteraction("Pressed Text To Speech Button: Pressed to Stop.", getApplicationContext());
                myTTS.stop();
                tts_button.setImageResource(R.drawable.speaker_white);
            } else {
                GlobalClass.LogInteraction("Pressed Text To Speech Button: Pressed to Start.", getApplicationContext());
                tts_button.setImageResource(R.drawable.speaker_green);
                speakWords(_content);
            }
        }
        else{
            if (toast != null)
                toast.cancel();
            Toast t = Toast.makeText(getApplicationContext(),"Text to speech not supported.", Toast.LENGTH_SHORT);
            t.show();
        }

    }

    // Changes current page id within module then reloads all page content
    private void ChangePage(int incrementValue) {
        if (adding_deleting_bookmark)
            return;

        if (toast != null)
            toast.cancel();

        int max = Integer.parseInt(_npages);
        int min = 1;

        int page = Integer.parseInt(_pid);
        page += incrementValue;

        if (page < min || page > max) {             // Don't do anything if we hit an edge of the module
            return;
        }

        _pid = Integer.toString(page);

        if(incrementValue == 1)
            GlobalClass.LogInteraction(String.format("Pressed Next Page Button: From M%s, P%d To P%s", _mnumber, page-incrementValue, _pid), getApplicationContext());
        else if (incrementValue == -1)
            GlobalClass.LogInteraction(String.format("Pressed Previous Page Button: From M%s, P%d To P%s", _mnumber, page-incrementValue, _pid), getApplicationContext());
        // Run vibrator
        vibrator.vibrate(20);
        myTTS.stop();

        getPageData();
    }

    //Called each time the Bookmark button is pressed.
    private void BookmarkButtonPressed() {
        if (adding_deleting_bookmark)
            return;

        adding_deleting_bookmark = true;

        Boolean bookmarked = false;
        bda.open();
        ArrayList<Bookmark> bookmarks = bda.getAllBookmarks();
        //Check to see if this page is bookmarked or not.
        for (Bookmark bk : bookmarks) {
            if (bk.getPage().equals(_pid) && bk.getModule().equals(_mnumber))
                bookmarked = true;
        }

        if (!bookmarked)//If this page is not bookmarked:
        {
            GlobalClass.LogInteraction(String.format("Pressed Bookmark Button: Adding Bookmark for page %s", _pid), getApplicationContext());
            AddBookmark();
        } else {//If this page is bookmarked:
            GlobalClass.LogInteraction(String.format("Pressed Bookmark Button: Removing Bookmark for page %s", _pid), getApplicationContext());
            RemoveBookmark();
        }

        // Run vibrator
        vibrator.vibrate(20);
    }

    public void AddBookmark() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                bda.createBookmark(_mnumber, _pid);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                bda.close();

                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(PageActivity.this, "Bookmark added", Toast.LENGTH_LONG);
                toast.show();

                bookmark_button.setImageResource(R.drawable.ic_bookmark_red_36dp);

                GlobalClass.LogInteraction(String.format("Bookmarked Module: %s, Page: %s", _mnumber, _pid), getApplicationContext());

                adding_deleting_bookmark = false;
            }

        };
        task.execute((Void[]) null);
    }

    public void RemoveBookmark() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                bda.removeBookmark(_mnumber, _pid);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                bda.close();

                bookmark_button.setImageResource(R.drawable.ic_bookmark_white_36dp);

                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(PageActivity.this, "Bookmark removed", Toast.LENGTH_LONG);
                toast.show();

                GlobalClass.LogInteraction(String.format("Un-bookmarked Module: %s, Page: %s", _mnumber, _pid), getApplicationContext());

                adding_deleting_bookmark = false;
            }

        };
        task.execute((Void[]) null);
    }

    public void slidefromRightToLeft(View view) {

        TranslateAnimation animate;
        if (view.getHeight() == 0) {
            view.getHeight(); // parent layout
            animate = new TranslateAnimation(view.getWidth()/2, 0, 0, 0);
        } else {
            animate = new TranslateAnimation(view.getWidth(),0, 0, 0); // View for animation
        }

        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE); // Change visibility VISIBLE or GONE
    }

    public void slidefromLeftToRight(View view) {

        TranslateAnimation animate;
        if (view.getHeight() == 0) {
            view.getHeight(); // parent layout
            animate = new TranslateAnimation(-view.getWidth()/2, 0, 0, 0);
        } else {
            animate = new TranslateAnimation(-view.getWidth(), 0, 0, 0); // View for animation
        }

        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);

        view.setVisibility(View.GONE); // Change visibility VISIBLE or GONE
    }

    private void speakWords(String text) {
        //parse(text);
        CharSequence cs = parse(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myTTS.speak(cs,TextToSpeech.QUEUE_FLUSH, null, KEY_PARAM_UTTERANCE_ID);
        }
    }

    //Parses the <> and text in between <> for text to speech
    private String parse(String text){
        String s = "";

        for(int i = 0; i < text.length(); i++)
        {
            if(text.charAt(i) == '<')
            {
                i++;
                while(text.charAt(i) != '>')
                {
                    i++;
                }
            }
            else
            {
                s += text.charAt(i);
            }

        }
        return s;
    }
}

