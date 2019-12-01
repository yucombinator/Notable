
package com.icechen1.notable.library;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.RemoteInput;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.icechen1.notable.library.utils.NotificationBuilder;
import com.icechen1.notable.library.utils.NotificationDataSource;
import com.icechen1.notable.library.utils.NotificationItem;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@EActivity
public class MainActivity
    extends FragmentActivity
    implements OnClickListener,DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
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
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	NotificationManager notificationManager;
	private String icon;
	private int savedId;
	private String shareInfo;
    private boolean useAlarm = false;
    private Calendar reminderCalendar;
    private Toolbar mToolbar;

	@ViewById
	Button dateBtn;
	@ViewById
	Button TimeBtn;

	@ViewById
	ImageButton checkmarkGray;

	@ViewById
	ImageButton checkmarkGreen;

	@ViewById
	ImageButton checkmarkOrange;

	@ViewById
	ImageButton checkmarkRed;

	@ViewById
	RelativeLayout reminderSet;

	@ViewById
	RelativeLayout reminderNone;

	@Override
	public void onResume(){
		super.onResume();
        Intent Intent = new Intent(this, com.icechen1.notable.library.NotificationService_.class);
        Bundle bundle = new Bundle();
        bundle.putString("action", "boot");
        Intent.putExtras(bundle);
        startService(Intent);

		MenuItem historyItem = mToolbar.getMenu().findItem(R.id.menu_history);
		if(historyItem != null) {
			if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_history", true)){
                historyItem.setVisible(false);
			} else {
                historyItem.setVisible(true);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("dark_theme", false)){
			setTheme(R.style.TransDarkAppTheme);
		}

		setContentView(R.layout.activity_main);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		//Check if we are editing...
		Bundle saved = getIntent().getExtras();		
		
		//EDIT NOTIF
		try {
			savedId = saved.getInt("id",-1);
		} catch(Exception e){
			savedId = -1;
		}

		//RECEIVER INTENT
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

		Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

		if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	        	String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	            if (sharedText != null) {
	            	shareInfo = sharedText;
	            }
	        }
	    } else if(("com.google.android.gm.action.AUTO_SEND").equals(action) && type != null) {
	    		//automatically save the note then close
	        	String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	            if (sharedText != null) {
					autoSaveNote(sharedText);
	            }
		} else if (remoteInput != null){
			String remoteText = remoteInput.getString(NotificationBuilder.KEY_NEW_NOTIFICATION);
			if (remoteText != null) {
				autoSaveNote(remoteText);
			}
		}

		// Handle other intents, such as being started from the home screen
	    afterViews();
    	final ImageView voiceBtn = (ImageView) findViewById(R.id.voiceBtn);
	    if(!checkVoiceRecognition()){
	    	voiceBtn.setVisibility(View.GONE);
	    }
		
        if (savedId == -1 && prefs.getBoolean("save_on_long_click", false))
            ((ImageView)findViewById(R.id.addBtn)).setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    addAndStayBtn(v);
                    return true;
                }
            });

		super.onCreate(savedInstanceState);
	}

	private void autoSaveNote(String sharedText) {
		shareInfo = sharedText;
		shareFields(shareInfo);
		icon = NotificationItem.CHECKMARK_GRAY;
		addBtn(null);
	}
    private void shareFields(String share_info){
    	final EditText editText = (EditText) findViewById(R.id.entryText);
    	editText.setText(share_info);
    }
	
    private void updateFields() {
		//
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		NotificationItem item = datasource.getItem(savedId);
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
  		
		if(icon.equals(NotificationItem.CHECKMARK_GRAY)) {
	  		checkmarkGray.setSelected(true);
		}
		if(icon.equals(NotificationItem.CHECKMARK_ORANGE)) {
			checkmarkOrange.setSelected(true);
		}
		if(icon.equals(NotificationItem.CHECKMARK_RED)) {
			checkmarkRed.setSelected(true);
		}
		if(icon.equals(NotificationItem.CHECKMARK_GREEN)) {
			checkmarkGreen.setSelected(true);
		}

        //Create the reminder time calendar object according to last saved time
        if(item.getReminderTime()>0){
            reminderCalendar.setTimeInMillis(item.getReminderTime());
            //Show it
            dateBtn.setText(DateFormat.getDateFormat(this).format(reminderCalendar.getTime()));
            TimeBtn.setText(DateFormat.getTimeFormat(this).format(reminderCalendar.getTime()));
            reminderSet.setVisibility(View.VISIBLE);
            reminderNone.setVisibility(View.GONE);
            useAlarm = true;
        }

		
	}

    private void saveNotification() {
    	// Prepare intent which is triggered if the
    	// notification is selected
        
    	EditText editText = (EditText) findViewById(R.id.entryText);

    	String inputText = editText.getText().toString();
    	int lineBreakPos = inputText.indexOf('\n');
    	String firstLine;
    	if(lineBreakPos != -1){
    		//break the title away from the description
    		firstLine = inputText.substring(0, lineBreakPos);
    	}else{
    		//one liner notif
    		firstLine = inputText;
    	}
    	
    	Log.d(TAG, firstLine);
    	
    	String longText;
    	if (shareInfo != null)
    		longText = shareInfo;
    	else if (lineBreakPos != -1)
    		longText = inputText.substring(lineBreakPos + 1);
    	else
    		longText = "";

        Intent Intent = new Intent(this, com.icechen1.notable.library.NotificationService_.class);
        Bundle mBundle = new Bundle();
       // Bundle extras = Intent.getExtras();
        mBundle.putString("action", "add");
        mBundle.putString("title", firstLine);
        mBundle.putString("icon", icon);
        mBundle.putString("longtext", longText);
        /*
        if(reminderTime!=null){
            mBundle.putLong("reminder_time",reminderTime);
        } */
        
        //Delete old notif if editing
		if(savedId != -1){
	        mBundle.putInt("old_noif_id", savedId);
		}
        if(useAlarm){
            //Pass the calendar object to the intent bundle
            mBundle.putLong("reminder_time",reminderCalendar.getTimeInMillis());
        }
		
        Intent.putExtras(mBundle);
        startService(Intent);
    }

    public void addBtn(View v) {
        saveNotification();
        if(Build.VERSION.SDK_INT >= 21){
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    public void addAndStayBtn(View v) {
        saveNotification();
        ((EditText)findViewById(R.id.entryText)).setText("");
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

    	//icon = "checkmarkGray";
		addBtn.setEnabled(false);
		addBtn.setClickable(false);
  		checkmarkGray.setSelected(true);
  		icon = NotificationItem.CHECKMARK_GRAY;
        // Request focus and show soft keyboard automatically
        editText.requestFocus();
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		int theme = R.style.TransAppTheme;
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)){
			theme = R.style.TransDarkAppTheme;
		}

        TypedArray a = getTheme().obtainStyledAttributes(theme, new int[] {R.attr.ic_send});
        final int sendResourceId = a.getResourceId(0, 0);
        
        TypedArray b = getTheme().obtainStyledAttributes(theme, new int[] {R.attr.ic_send_disabled});
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

		
		if(shareInfo != null){
			Log.i(TAG, "Received an share intent...");
			shareFields(shareInfo);
		}

        //Create the reminder time calendar object
        reminderCalendar = Calendar.getInstance();
        //Set the seconds to 0
        reminderCalendar.set(Calendar.SECOND,0);
        //Preset the hour to current hour + 1
        reminderCalendar.set(Calendar.HOUR,reminderCalendar.get(Calendar.HOUR)+1);
        //Show it
        dateBtn.setText(DateFormat.getDateFormat(this).format(reminderCalendar.getTime()));
        TimeBtn.setText(DateFormat.getTimeFormat(this).format(reminderCalendar.getTime()));

        if(savedId != -1){
            Log.i(TAG, "Loading from database: " + savedId);
            updateFields();
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
		} else if (itemId == R.id.donate) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.icechen1.notable.pro")));
			return true;
		} else if (itemId == R.id.menu_history) {
			Intent gohist = new Intent(this, HistoryActivity.class);
			startActivity(gohist);
			return true;
		} else if (itemId == R.id.menu_share) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
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

    @Click
    void dateBtn(){
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
        datePickerDialog.show(getSupportFragmentManager(), "DATEPICKER");
    }
    @Click
    void TimeBtn(){
        final Calendar calendar = Calendar.getInstance();
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this), true);
        timePickerDialog.show(getSupportFragmentManager(), "TIMEPICKER");
    }

    @Click
    void reminderAddBtn(){
        reminderSet.setVisibility(View.VISIBLE);
        reminderNone.setVisibility(View.GONE);
        useAlarm = true;
    }
    @Click
    void cancelAlarmSet(){
        reminderSet.setVisibility(View.GONE);
        reminderNone.setVisibility(View.VISIBLE);
        useAlarm = false;
    }

  	@Click
    void checkmark_gray(){
        resetBkg();
	  	checkmarkGray.setSelected(true);
        icon = NotificationItem.CHECKMARK_GRAY;
    }

    @Click
    void checkmark_green(){
        resetBkg();
		checkmarkGreen.setSelected(true);
        icon= NotificationItem.CHECKMARK_GREEN;
    }
    @Click
    void checkmark_orange(){
        resetBkg();
		checkmarkOrange.setSelected(true);
        icon= NotificationItem.CHECKMARK_ORANGE;
    }
    @Click
    void checkmark_red(){
        resetBkg();
		checkmarkRed.setSelected(true);
        icon= NotificationItem.CHECKMARK_RED;
    }

    public void resetBkg(){
		checkmarkGray.setSelected(false);
		checkmarkGreen.setSelected(false);
		checkmarkOrange.setSelected(false);
		checkmarkRed.setSelected(false);

    }

	@Override
	public void onClick(View v) {

	}

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int y, int m, int d) {
        //Update
        reminderCalendar.set(y,m,d);
        //Show it
        dateBtn.setText(DateFormat.getDateFormat(this).format(reminderCalendar.getTime()));
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int min) {
        //update
        reminderCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        reminderCalendar.set(Calendar.MINUTE,min);
        //Show it
        TimeBtn.setText(DateFormat.getTimeFormat(this).format(reminderCalendar.getTime()));

    }
}
