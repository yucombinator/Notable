package com.icechen1.notable.library;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

 public class PreferencesActivity extends com.lb.material_preferences_library.PreferenceActivity {

     @Override
     protected int getPreferencesXmlId() {
         return R.xml.xml_preferences;
     }

     //Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)){
            setTheme(R.style.AppThemeDark);
        }

        super.onCreate(savedInstanceState);

        try {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(Exception e){
            //Not on Android ICS+
        }

    	final ListPreference prioPref = (ListPreference) findPreference("priority");
    	
    	if (Build.VERSION.SDK_INT < 16) {
        	//Disable options that aren't for the API level
        	CheckBoxPreference expandPref = (CheckBoxPreference) findPreference("expand_buttons");
        	expandPref.setEnabled(false);
        	expandPref.setChecked(false);
        	prioPref.setEnabled(false);
        }
        
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // app icon in action bar clicked; go home
            	finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.cancelAll();

        Intent Intent = new Intent(this, com.icechen1.notable.library.NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "boot");
        Intent.putExtras(mBundle);
        startService(Intent);
    }

    
}