package com.icechen1.notable.library;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.icechen1.notable.library.utils.NotificationBuilder;
import com.icechen1.notable.library.utils.NotificationDataSource;
import com.icechen1.notable.library.utils.NotificationItem;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;

import java.util.Calendar;
import java.util.List;

@EService
public class NotificationService extends Service{
    private final IBinder mBinder = new LocalBinder();
    
    public class LocalBinder extends Binder {
    	NotificationService getService() {
            return NotificationService.this;
        }
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
        System.out.println("Notable Service Started");
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("shortcut", true)){
    	    addQuickAdd();
		}else{
			removeQuickAdd();
		}
		Bundle action = null;
		try{
		action = intent.getExtras();
		}catch(Exception e){
			Log.e("NOTABLE","Error acquiring the Intent Bundle...");
		}
		if(action != null){
			if(action.containsKey("action")){
				String _action = action.getString("action");
				if (_action.equals("boot")){
					bootAdd();
				}else{
					if (_action.equals("dismiss")){
						dismiss(action.getInt("id"));
					}else{
						if (_action.equals("add")){
							add(action);
						}else{
                            if(_action.equals("alarm")){
                                createAlarmNotification(action.getInt("id"));
                            }
                        }
					}
				}
			}
			//Don't shut me off until I'm done
			return START_STICKY;
		}
		return START_NOT_STICKY;

	}

    private void createAlarmNotification(int id) {
        System.out.println("Set up Alarm");
        //Cancel the old notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
        //load from db
        NotificationDataSource datasource = new NotificationDataSource(this);
        datasource.open();
        //create the alarm notif
        NotificationBuilder build = new NotificationBuilder(this);
        NotificationItem item = datasource.getItem(id);
        if(item!=null){
            build.buildNotif(item, true);
        }

        datasource.close();
        onResultFinish();
    }


    private void add(Bundle action) {
    	//Get data from bundle
    	String title = action.getString("title");
    	String longtext = action.getString("longtext");
    	String icon = action.getString("icon");
    	long reminder_time = action.getLong("reminder_time",0);

		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		NotificationItem notif = datasource.createNotif(title, longtext, icon,reminder_time);
		datasource.close();
	    //System.out.println("Notification added with id: " + notif.getID());

		NotificationBuilder build = new NotificationBuilder(this);
		build.buildNotif(notif,false);
		
		//Delete old notif item if editing
		int old_noif_id = action.getInt("old_noif_id",-1);
		
		if(old_noif_id != -1){
			dismiss(old_noif_id);
		}

        if(reminder_time>0){
            //Create an alarm for the next run
            setUpNextRun(notif);
        }
		
    	onResultFinish();
		
	}

	@Override
    public void onCreate() {
    	//Fire up the asynctask to get the info we need

    }
    
    public void onResultFinish(){
    	//setUpNextRun();
    	finish();
    }
    
    private void setUpNextRun(NotificationItem notif) {
    	//Execute the alarms at the right time
        Intent intent = new Intent(this, com.icechen1.notable.library.NotificationService_.class);

        Bundle jBundle = new Bundle();
        jBundle.putString("action", "alarm");
        jBundle.putInt("id", notif._id);
        intent.putExtras(jBundle);

        PendingIntent pendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT > 18) {
            //use KK's exact timer
            am.setExact(AlarmManager.RTC_WAKEUP,
                    notif.getReminderTime(), pendingIntent);
        }else{
            am.set(AlarmManager.RTC_WAKEUP,
                    notif.getReminderTime(), pendingIntent);
        }

        Log.d("Notable", "Created alarm at: " + notif.getReminderTime());
		Log.d("Notable", "with id: " + notif._id);
	} 

	private void finish() {
		stopSelf();
	}
	
	@Background
	public void bootAdd(){
        System.out.println("Notable Booting");
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		List<NotificationItem> notif = datasource.getAllItems();
		datasource.close();

		NotificationBuilder build = new NotificationBuilder(this);
		for (NotificationItem i: notif){
			if(i.getDismissed())
				continue; //do not recreate dismissed ones

            if(i.getReminderTime()>0){
                //(Re)Create the alarms
                if(i.getReminderTime()>Calendar.getInstance().getTimeInMillis()){
                    setUpNextRun(i);
                }else{
                    build.buildNotif(i,true);
                }
            }else{
                build.buildNotif(i,false);
            }
        }


    	onResultFinish();
	}
	
	@Background
	public void dismiss(int id){
	    //System.out.println("Deleting id: " + id);
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();

		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_history", true)){
			datasource.dismissItem(id);
		} else {
			datasource.deleteItem(id);
		}

		datasource.close();

		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
		
    	onResultFinish();
	}
	
	public void addQuickAdd(){
		NotificationBuilder build = new NotificationBuilder(this);
		build.buildShortCutNotif();
	}
	
    private void removeQuickAdd() {
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		try{
			notificationManager.cancel(-1);
		}catch(Exception e){
			
		}
	}
}
