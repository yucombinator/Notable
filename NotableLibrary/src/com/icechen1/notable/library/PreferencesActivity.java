package com.icechen1.notable.library;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.icechen1.notable.library.NotificationService_;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;

 public class PreferencesActivity extends SherlockPreferenceActivity  {
	 
    //Called when the activity is first created. 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.xml_preferences);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT < 16) {
        	CheckBoxPreference expandPref = (CheckBoxPreference) findPreference("expand_buttons");
        	expandPref.setEnabled(false);
        	expandPref.setChecked(false);
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

        Intent Intent = new Intent(this, NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "boot");
        Intent.putExtras(mBundle);
        startService(Intent);
    }

    
}