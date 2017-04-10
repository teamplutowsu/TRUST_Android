package edu.wsu.eecs.pluto.trust;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import edu.wsu.eecs.pluto.trust.database.UserDBAdapter;

import static edu.wsu.eecs.pluto.trust.R.id.backButton;

public class FontChangeActivity extends AppCompatActivity {

    Spinner FontSpinner;
    private static final String[] FontSizeList = new String[] {"Small", "Medium", "Medium Large", "Large"};
    String fontSetting;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load and save the settings for later use.
        settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        editor = settings.edit();
        fontSetting = settings.getString("font_size", "Error");

        setTheme(R.style.MediumTheme);

        if(fontSetting.equals("Error")){
            editor.putString("font_size", FontSizeList[1]);
            editor.apply();
            fontSetting = FontSizeList[1];
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_change);

        SetActionBarFormat();

        //Populate the drop down menu. (Spinner)
        FontSpinner = (Spinner) findViewById(R.id.ChangeFontSpinner);

        ArrayAdapter<String> fontsizeadapter = new ArrayAdapter<String>(FontChangeActivity.this, android.R.layout.simple_spinner_item, FontSizeList);
        fontsizeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        FontSpinner.setAdapter(fontsizeadapter);

        for(int i = 0; i < FontSizeList.length; i++){
            if(FontSizeList[i].equals(fontSetting))
                FontSpinner.setSelection(i,true);
        }

        FontSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view1, int pos, long id) {DropDownMenuItemSelected(arg0, view1, pos, id);}

            @Override
            public void onNothingSelected(AdapterView<?> arg1){}

        });

        GlobalClass.LogInteraction("Entered Font Change Activity: OnCreate()", getApplicationContext());
        }

    //Get rid of any toasts before backing out.
    @Override
    public void onBackPressed() {
        if(toast != null)
            toast.cancel();

        super.onBackPressed();

        GlobalClass.LogInteraction("Pressed Phones Back Button", getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalClass.sendLogData(getApplicationContext());
        //GlobalClass.test();
    }

    @Override
    public void onResume(){
        super.onResume();

        GlobalClass.LogInteraction("Entered Settings Activity: OnResume()", getApplicationContext());
    }

    //Sets up the action bar at the top, along with it's button.
    private void SetActionBarFormat() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.fontchange_actionbar, null);
        ab.setCustomView(customView);
        ab.setDisplayShowCustomEnabled(true);

        ImageButton backImageButton = (ImageButton) customView.findViewById(backButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {onBackPressed();}
        });
    }

    //Called when a item is selected from the drop down menu.
    private void DropDownMenuItemSelected(AdapterView<?> arg0, View view1, int pos, long id){
        editor.putString("font_size", FontSizeList[pos]);
        editor.apply();

        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(FontChangeActivity.this, "Settings Updated to "+ FontSizeList[pos]+" text", Toast.LENGTH_LONG);
        toast.show();

        GlobalClass.LogInteraction(String.format("Updated Font Setting To %s",FontSizeList[pos]), getApplicationContext());
    }
}
