package edu.wsu.eecs.pluto.trust;

import edu.wsu.eecs.pluto.trust.database.Bookmark;
import edu.wsu.eecs.pluto.trust.database.BookmarkDBAdapter;
import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    ArrayList<Bookmark> _bookmarks_list;//The list of bookmarks returned by the database
    ArrayList<String> _bookmarkstext_list;//The list of strings to be displayed, deriveed from those bookmarks.
    ArrayList<Boolean> _bookmarks_to_delete;//A lookup table mapping to each bookmark.
    ListView _bookmarkListView;//The LiewView containing the list of bookmarks.
    TextView title;//The title at the top of the screen.
    ImageButton delete_button;//The Delete button.
    Boolean delete_button_active = false;//The flag indicating the difference between viewing mode and deletion mode.
    Toast toast;
    ProgressDialog pd;

    int deleted_items;

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
        setContentView(R.layout.activity_boomark);

        SetActionBarFormat();

        populateBookmarks();

        GlobalClass.LogInteraction("Entered the Bookmarks Activity: OnCreate()", getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (delete_button_active) {//If you were in delete mode, revert back.
            title.setText("Bookmarks");
            title.setTextSize(30);
            delete_button.setImageResource(R.drawable.trashbin_white);
            delete_button_active = false;
            _bookmarkListView.setBackgroundResource(R.drawable.rounded_view_front);
        }

        populateBookmarks();

        GlobalClass.LogInteraction("Entered the Bookmarks Activity: OnResume()", getApplicationContext());
    }

    //An overridden function which returns you to the main menu if you press back.
    @Override
    public void onBackPressed() {
        if(!delete_button_active) {
            if(toast != null)
                toast.cancel();

            super.onBackPressed();
            GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
        } else {
            title.setText("Bookmarks");
            title.setTextSize(30);
            delete_button.setImageResource(R.drawable.trashbin_white);

            _bookmarkListView.setBackgroundResource(R.drawable.rounded_view_front);
            
            populateBookmarks();
            delete_button_active = false;

            GlobalClass.LogInteraction("Pressed Phones Back Button: Going out of Delete Mode", getApplicationContext());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
        //GlobalClass.test();
    }

    //The action bar is the strip at the top of the page.
    private void SetActionBarFormat() {
        ActionBar mActionBar = getSupportActionBar();
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.bookmark_actionbar, null);
        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);

        title = (TextView) customView.findViewById(R.id.bookmark_tittle);

        delete_button = (ImageButton) customView.findViewById(R.id.bookmark_delete_button);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteButtonPressed();
            }
        });

    }

    //Function which rebuilds the list of bookmarks, getting them fresh from the database.
    private void populateBookmarks() {

        BookmarkDBAdapter bookmarkdbadpt = new BookmarkDBAdapter(getBaseContext());
        bookmarkdbadpt.open();

        _bookmarks_to_delete = new ArrayList<Boolean>();
        _bookmarkstext_list = new ArrayList<String>();
        _bookmarkListView = (ListView) findViewById(R.id.bookmarkslist);

        _bookmarks_list = bookmarkdbadpt.getAllBookmarks();

        if (_bookmarks_list == null || _bookmarks_list.isEmpty()) {
            _bookmarkstext_list.add("No Bookmarks");
        }

        for (Bookmark bkmk : _bookmarks_list) {//Creates an array of strings for each bookmark, they are what is shown onscreen.
            _bookmarkstext_list.add(MakeBookmarkString(bkmk.getModule(), bkmk.getPage()));
        }

        ArrayAdapter<String> adapter = new BookmarkAdapter(this, R.layout.bookmark_textview, R.id.BookmarkEntry, _bookmarkstext_list);

        _bookmarkListView.setAdapter(adapter);
        bookmarkdbadpt.close();

        //Set a listener for when a bookmark is clicked.
        _bookmarkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {BookmarkPressed(parent, view, position, id);}
        });

        //Initialize the bookmarks deletion lookup table for later.
        for(int i = 0; i < _bookmarks_list.size(); i++){
            _bookmarks_to_delete.add(false);
        }
    }

    private String MakeBookmarkString(String module, String page) {
        String subtitle;
        String title;
        try {
            String mypage = page;
            if (Integer.parseInt(mypage) < 10) {
                mypage = "0" + mypage;
            }

            String mymodule = module;
            if (Integer.parseInt(mymodule) < 10) {
                mymodule = "0" + mymodule;
            }

            InputStream istream = getResources().openRawResource(getResources().getIdentifier("page_"+ mymodule+"_" + mypage, "raw", getPackageName()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
            StringBuilder builder = new StringBuilder();
            String fileLine;

            while ((fileLine = reader.readLine()) != null) {
                builder.append(fileLine);
            }

            String fileData = builder.toString();

            // Parse text from fileData
            subtitle = fileData.substring(fileData.indexOf("<Subtitle>") + "<Subtitle>".length(), fileData.indexOf("</Subtitle>"));
            title = fileData.substring(fileData.indexOf("<Title>") + "<Title>".length(), fileData.indexOf("</Title>"));

        } catch (Exception e) {
            title = "Exception in finding page in module " + module;
            subtitle = "Exception!";
        }

        return (title + " | Page " + page + " | " + subtitle);
    }

    //Function called each time the delete button is pressed.
    private void DeleteButtonPressed() {
        if(_bookmarks_list.size() == 0)
            return;

        if (!delete_button_active) {//If we are in viewing mode switch to delete mode:
            title.setText("Select Bookmarks to Delete:");
            title.setTextSize(20);
            delete_button.setImageResource(R.drawable.trashbin_yellow);

            for(int i = 0; i < _bookmarkListView.getChildCount(); i++){
                    _bookmarkListView.getChildAt(i).setBackgroundColor(Color.WHITE);
            }

            _bookmarkListView.setBackgroundColor(Color.WHITE);

            delete_button_active = true;
            GlobalClass.LogInteraction("Pressed the Delete Button: Begin Delete Mode", getApplicationContext());
        } else {//If we are in delete mode switch to viewing mode:
            DeletingBookmarks();
            GlobalClass.LogInteraction("Pressed the Delete Button: End Delete Mode", getApplicationContext());
        }
    }

    //Function called each time one of the displayed bookmarks are pressed.
    private void BookmarkPressed(AdapterView<?> parent, View view, int position, long id) {
        if (!(_bookmarks_list == null || _bookmarks_list.isEmpty())) {

            if (!delete_button_active) {//Go to the corisponding module page:
                if (toast != null)
                    toast.cancel();

                Intent i = new Intent(getApplicationContext(), PageActivity.class);
                i.putExtra("mnumber", _bookmarks_list.get(position).getModule());
                i.putExtra("pnumber", _bookmarks_list.get(position).getPage());

                GlobalClass.LogInteraction("Pressed a Bookmark linking to: Module " + _bookmarks_list.get(position).getModule() + " Page " + _bookmarks_list.get(position).getPage(), getApplicationContext());

                startActivity(i);

            }else {//Mark/Unmark the bookmark for delete:

                if(_bookmarks_to_delete.get(position) == false) {
                    view.setBackgroundColor(Color.rgb(225, 65, 65));
                    _bookmarks_to_delete.set(position,true);

                    int toDeleteBookmarks = 0;
                    for(boolean bol : _bookmarks_to_delete){
                        if(bol)
                            toDeleteBookmarks++;
                    }

                    if(toDeleteBookmarks == 1)
                        delete_button.setImageResource(R.drawable.trashbin_red);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                    _bookmarks_to_delete.set(position,false);

                    int toDeleteBookmarks = 0;
                    for(boolean bol : _bookmarks_to_delete){
                        if(bol)
                            toDeleteBookmarks++;
                    }

                    if(toDeleteBookmarks == 0)
                        delete_button.setImageResource(R.drawable.trashbin_yellow);
                }

            }
        }
    }

    //This 'fucntion' creates a background task to do the deletion and puts a pop-up progress bar during the operation.
    public void DeletingBookmarks() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(BookmarkActivity.this);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle("Deleting Bookmarks...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(false);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                int to_delete = 0;
                for (Boolean bol : _bookmarks_to_delete) {//Need to know how many to delete for the progress bar.
                    if (bol)
                        to_delete++;
                }

                BookmarkDBAdapter bookmarkdbadpt = new BookmarkDBAdapter(getBaseContext());
                bookmarkdbadpt.open();

                deleted_items = 0;
                for (int i = 0; i < _bookmarks_to_delete.size(); i++) {
                    if (_bookmarks_to_delete.get(i)) {
                        bookmarkdbadpt.removeBookmark(_bookmarks_list.get(i).getModule(), _bookmarks_list.get(i).getPage());
                        deleted_items++;

                        GlobalClass.LogInteraction("Deleted Bookmark from the Bookmark View: Module " + _bookmarks_list.get(i).getModule() + " Page " + _bookmarks_list.get(i).getPage(), getApplicationContext());

                        pd.setProgressNumberFormat(deleted_items + " / " + to_delete);
                        pd.setProgress((deleted_items * 100) / to_delete);
                    }
                }

                bookmarkdbadpt.close();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd!=null) {
                    pd.dismiss();

                    delete_button_active = false;
                    title.setText("Bookmarks");
                    title.setTextSize(30);

                    delete_button.setImageResource(R.drawable.trashbin_white);

                    _bookmarkListView.setBackgroundResource(R.drawable.rounded_view_front);
                    populateBookmarks();

                    if(deleted_items > 0){//If we removed some bookmarks make a Toast to report it.
                        if (toast != null)
                            toast.cancel();
                        if(deleted_items == 1) {
                            toast = Toast.makeText(BookmarkActivity.this, "Bookmark removed", Toast.LENGTH_LONG);
                        } else {
                            toast = Toast.makeText(BookmarkActivity.this, "Bookmarks removed", Toast.LENGTH_LONG);
                        }
                        toast.show();
                        deleted_items = 0;
                    }
                }

            }

        };
        task.execute((Void[])null);
    }

    //A special class for displaying the bookmarks, necessary for them to be highlighted properly.
    private class BookmarkAdapter extends ArrayAdapter<String> {
        public BookmarkAdapter(Context context, int resource, int textViewResourceId, List<String> objects){
            super(context, resource, textViewResourceId, objects);
        }

        //This method simply highlights the bookmark if it has been selected for deletion.
        @Override
        public View getView(int pos, View convertView, ViewGroup parent){
            View mod = super.getView(pos, convertView, parent);

            if(delete_button_active) {
                if (_bookmarks_to_delete.get(pos) == true) {
                    mod.setBackgroundColor(Color.rgb(225, 65, 65));
                } else {
                    mod.setBackgroundColor(Color.WHITE);
                }
            }

            return mod;
        }
    }
}