
package com.icechen1.notable.library;

import java.util.ArrayList;
import java.util.List;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.icechen1.notable.library.NotificationService_;

@EActivity
//@EActivity
public class MainActivity
    extends SherlockActivity
    implements OnClickListener
{
	/* TODO
	 * use ICS swipe circle for add confirmation
	 * cardUI for main app
	 * showcaseview
	 * voice add done
	 * shake add
	 * location vibrate
	 * time vibrate remind
	 * sync with gtasks astrid
	 * implement edit
	 * widget [Notable ----- +]
	 * blank icon for notif shortcut
	 * fix donate button, icons for confirm ad option for confirm
	 */
	
	/**
	 * Changelog:
	 * dark holo
	 * sllovenian update
	 * dutch by Peter Henkel
	 * 
	 */
	static String TAG = "NOTABLE";

	NotificationManager notificationManager;
	private String icon;
	private int saved_id;
	private String share_info;

	
	@Override
	public void onResume(){
		super.onResume();
        Intent Intent = new Intent(this, NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "boot");
        Intent.putExtras(mBundle);
        startService(Intent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)){
			setTheme(R.style.TransDarkAppTheme);
		}
		setContentView(R.layout.activity_main);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		//Check if we are editing...
		Bundle saved = getIntent().getExtras();		
		
		//EDIT NOTIF
		try{
		saved_id = saved.getInt("id",-1);
		}catch(Exception e){
			saved_id = -1;
		}
		
		//RECEIVER INTENT
		
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	        	String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	            if (sharedText != null) {
	            	share_info = sharedText;
	            }
	        }
	    } else {
	    	if(("com.google.android.gm.action.AUTO_SEND").equals(action) && type != null){
	    		//automatically save the note then close
	        	String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	            if (sharedText != null) {
	            	share_info = sharedText;
	            	shareFields(share_info);
	            	icon = "checkmark_gray";
	            	addBtn(null);
	            }
	    	}else{
	    		
	    	}
	        // Handle other intents, such as being started from the home screen
	    }
	    afterViews();
    	final ImageView voiceBtn = (ImageView) findViewById(R.id.voiceBtn);
	    if(!checkVoiceRecognition()){
	    	voiceBtn.setVisibility(View.GONE);
	    }
		
		super.onCreate(savedInstanceState);
	}
    private void shareFields(String share_info){
    	final EditText editText = (EditText) findViewById(R.id.entryText);
    	editText.setText(share_info);
    }
	
    private void updateFields() {
		//
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		NotificationItem item = datasource.getItem(saved_id);
		datasource.close();
		//Log.i(TAG, item.getLongText());
    	final EditText editText = (EditText) findViewById(R.id.entryText);
    	String newLine = System.getProperty("line.separator");
       	
       	String final_text = item.getTitle();
		if (!item.getLongText().equals(""))
			final_text += newLine + item.getLongText();
       	
    	editText.setText(final_text);
		
  		resetBkg();
  		icon = item.getIcon();
  		
		if(icon.equals("checkmark_gray"))
		{
	  		checkmark_gray.setBackgroundColor(getResources().getColor(R.color.holo_blue));
		}
		if(icon.equals("checkmark_orange"))
		{
			checkmark_orange.setBackgroundColor(getResources().getColor(R.color.holo_blue));
		}
		if(icon.equals("checkmark_red"))
		{
			checkmark_red.setBackgroundColor(getResources().getColor(R.color.holo_blue));
		}
		if(icon.equals("checkmark_green"))
		{
			checkmark_green.setBackgroundColor(getResources().getColor(R.color.holo_blue));
		}

		
	}

	public void addBtn(View v) {
    	// Prepare intent which is triggered if the
    	// notification is selected
        
    	EditText editText = (EditText) findViewById(R.id.entryText);

    	String inputText = editText.getText().toString();
    	int lineBreakPos = inputText.indexOf('\n');
    	String firstLine = inputText.substring(0, lineBreakPos);
    	Log.d(TAG, firstLine);
    	
    	String longText;
    	if (share_info != null)
    		longText = share_info;
    	else if (lineBreakPos != -1)
    		longText = inputText.substring(lineBreakPos + 1);
    	else
    		longText = "";

        Intent Intent = new Intent(this, NotificationService_.class);
        Bundle mBundle = new Bundle();
       // Bundle extras = Intent.getExtras();
        mBundle.putString("action", "add");
        mBundle.putString("title", firstLine);
        mBundle.putString("icon", icon);
        mBundle.putString("longtext", longText);
        
        //Delete old notif if editing
		if(saved_id != -1){
	        mBundle.putInt("old_noif_id", saved_id);	
		}
		
        Intent.putExtras(mBundle);
        startService(Intent);
        
    	finish();
    }
    
    public void menuBtn(View v){
    	if(getApplicationContext().getPackageName().contains("pro")){
			// launch prefs
            Intent gopref = new Intent(this, PreferencesActivity.class);
			gopref.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(gopref);
    	}else{
    		openOptionsMenu();
    	}
    	
    }

    void afterViews() {
    	final EditText editText = (EditText) findViewById(R.id.entryText);
    	final ImageButton addBtn = (ImageButton) findViewById(R.id.addBtn);
    	final ImageView voiceBtn = (ImageView) findViewById(R.id.voiceBtn);
    	//icon = "checkmark_gray";
		addBtn.setEnabled(false);
		addBtn.setClickable(false);
  		checkmark_gray.setBackgroundColor(getResources().getColor(R.color.holo_blue));
  		icon = "checkmark_gray";
    	//Re-add notifs after an update and whatnot
//        Intent Intent = new Intent(this, NotificationService_.class);
//        Bundle mBundle = new Bundle();
//        mBundle.putString("action", "boot");
//        Intent.putExtras(mBundle);
//        startService(Intent);
        

    	
    	
        // Request focus and show soft keyboard automatically
        editText.requestFocus();
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {R.attr.ic_send});     
        final int sendResourceId = a.getResourceId(0, 0);
        
        TypedArray b = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {R.attr.ic_send_disabled});     
        final int disabledResourceId = b.getResourceId(0, 0);
        editText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
            	if(editText.length() > 0){
            		addBtn.setImageDrawable(getResources().getDrawable(sendResourceId));
            		addBtn.setEnabled(true);
            		addBtn.setClickable(true);
            		voiceBtn.setVisibility(View.GONE);
            	}else{
            		addBtn.setImageDrawable(getResources().getDrawable(disabledResourceId));           		
            		addBtn.setEnabled(false);
            		addBtn.setClickable(false);
            		voiceBtn.setVisibility(View.VISIBLE);
            	}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        
		if(saved_id != -1){
			Log.i(TAG, "Loading from database: " + saved_id);
			updateFields();		
		}
		
		if(share_info != null){
			Log.i(TAG, "Received an share intent...");
			shareFields(share_info);		
		}
    }

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
		} else if (itemId == R.id.donate) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.icechen1.notable.library.plus")));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	 public boolean checkVoiceRecognition() {
		  // Check if voice recognition is present
		  PackageManager pm = getPackageManager();
		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
		    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		  if (activities.size() == 0) {
			  return false;
		  }
		  return true;
	}
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    public void startVoiceRecognition(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
            "Say something.");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        
      }
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
          ArrayList<String> matches = data
              .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          EditText editText = (EditText) findViewById(R.id.entryText);
          String firstMatch  = matches.get(0);
          editText.setText(firstMatch);
      }
    }
      
  	
  	@ViewById
  	ImageButton checkmark_gray;
  	
  	@ViewById
  	ImageButton checkmark_green;
  	
  	@ViewById
  	ImageButton checkmark_orange;
  	
  	@ViewById
  	ImageButton checkmark_red;
  	
  	@Click
  	void checkmark_gray(){
  		resetBkg();
  		checkmark_gray.setBackgroundColor(getResources().getColor(R.color.holo_blue));
  		icon = "checkmark_gray";
  	}
  	
  	@Click
  	void checkmark_green(){
  		resetBkg();
  		checkmark_green.setBackgroundColor(getResources().getColor(R.color.holo_blue));
  		icon= "checkmark_green";
  	}
  	@Click
  	void checkmark_orange(){
  		resetBkg();
  		checkmark_orange.setBackgroundColor(getResources().getColor(R.color.holo_blue));
  		icon= "checkmark_orange";
  	}
  	@Click
  	void checkmark_red(){
  		resetBkg();
  		checkmark_red.setBackgroundColor(getResources().getColor(R.color.holo_blue));
  		icon= "checkmark_red";
  	}
  	
  	public void resetBkg(){
  		checkmark_gray.setBackgroundResource(getResources().getColor(android.R.color.transparent));
  		checkmark_green.setBackgroundResource(getResources().getColor(android.R.color.transparent));
  		checkmark_orange.setBackgroundResource(getResources().getColor(android.R.color.transparent));
  		checkmark_red.setBackgroundResource(getResources().getColor(android.R.color.transparent));

  	}

	@Override
	public void onClick(View v) {

	}
}
