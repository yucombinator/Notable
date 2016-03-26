package com.icechen1.notable.library;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.icechen1.notable.library.utils.NotificationDataSource;
import com.icechen1.notable.library.utils.NotificationItem;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity
public class DetailActivity extends FragmentActivity {

	private int saved_id;
	private NotificationItem item;
	private Toolbar mToolbar;

	@Override
	public void onResume(){
		super.onResume();
		if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_history", true)){
			mToolbar.getMenu().findItem(R.id.menu_history).setVisible(false);
		} else {
            mToolbar.getMenu().findItem(R.id.menu_history).setVisible(true);
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.menu_settings) {
			// launch prefs
            Intent gopref = new Intent(this, PreferencesActivity.class);
			gopref.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(gopref);
			//Intent intent = getIntent();
			//finish();
			startActivity(gopref);
			return true;
		} else if (itemId == R.id.more_apps) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Yu+Chen+Hou")));
			return true;
		} else if (itemId == R.id.menu_history) {
			Intent gohist = new Intent(this, HistoryActivity.class);
			startActivity(gohist);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)){
			setTheme(R.style.TransDarkAppTheme);
		}
		//setTheme(R.style.TransDarkAppTheme);
		setContentView(R.layout.activity_confirm);
		//Check if we are editing...
		Bundle saved = getIntent().getExtras();		
		
		//EDIT NOTIF
		try{
		saved_id = saved.getInt("id",-1);
		}catch(Exception e){
			saved_id = -1;
		}
		
    	if(saved_id != -1){
    		updateFields();
    	}

		super.onCreate(savedInstanceState);
	}
	@ViewById
	TextView notifText;

    @ViewById
    TextView alarmText;
	
    private void updateFields() {

				/*
		 * Toolbar setup
		 */
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(getResources().getString(R.string.app_name));
		mToolbar.setTitleTextColor(Color.WHITE);
		// Set an OnMenuItemClickListener to handle menu item clicks
		mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// Handle the menu item
				return onOptionsItemSelected(item);
			}
		});

		// Inflate a menu to be displayed in the toolbar
		mToolbar.inflateMenu(R.menu.activity_main);

		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		item = datasource.getItem(saved_id);
		datasource.close();
		//Log.i(TAG, item.getLongText());
    	String newLine = System.getProperty("line.separator");
       	String text = item.getTitle();
       	String text_long = null;
       	if (!item.getLongText().equals("Notable")){
       		text_long = item.getLongText();
       	}
       	String final_text;
       	if (text_long != null)  {final_text = text + newLine + text_long;}
       	else {final_text = text;}
       	
       	notifText.setText(final_text);

        // Add links
		Linkify.addLinks(notifText, Linkify.ALL);

		alarmText.setText(getResources().getText(R.string.no_alarm));
        //check if there is an alarm
        long time = item.getReminderTime();
        if (time > 0) {
            String alarmtime = DateFormat.getLongDateFormat(this).format(time) + " " +
                    DateFormat.getTimeFormat(this).format(time);
            alarmText.setText(alarmtime);
        }
	}
    
    public void closeBtn(View v){
    	finish();
    }
    public void menuBtn(View v){
    	openOptionsMenu();
    }
    
    public void editBtn(View v){
		Intent j = new Intent(this, com.icechen1.notable.library.MainActivity_.class);
		
        Bundle jBundle = new Bundle();
        jBundle.putInt("id", item.getID());
        j.putExtras(jBundle);
        startActivity(j);
    	finish();
    }
    
    public void doneBtn(View v){
		Intent i = new Intent(this, com.icechen1.notable.library.NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "dismiss");
        mBundle.putInt("id", item.getID());
        i.putExtras(mBundle);
        
        startService(i);
    	finish();
    }
}
