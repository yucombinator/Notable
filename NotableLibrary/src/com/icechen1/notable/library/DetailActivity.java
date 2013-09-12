package com.icechen1.notable.library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.icechen1.notable.library.MainActivity_;
import com.icechen1.notable.library.NotificationService_;

@EActivity
public class DetailActivity extends SherlockActivity{

	private int saved_id;
	private NotificationItem item;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
	
    private void updateFields() {
		//
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
	}
    
    public void closeBtn(View v){
    	finish();
    }
    public void menuBtn(View v){
    	openOptionsMenu();
    }
    
    public void editBtn(View v){
		Intent j = new Intent(this, MainActivity_.class);
		
        Bundle jBundle = new Bundle();
        jBundle.putInt("id", item.getID());
        j.putExtras(jBundle);
        startActivity(j);
    	finish();
    }
    
    public void doneBtn(View v){
		Intent i = new Intent(this, NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "delete");
        mBundle.putInt("id", item.getID());
        i.putExtras(mBundle);
        
        startService(i);
    	finish();
    }
}
