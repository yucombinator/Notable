package com.icechen1.notable.library;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EService;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

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
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("shortcut", true)){
    	addQuickAdd(); //TODO CHECK SETTINGS
		}else{
			removeQuickAdd();
		}
		Bundle action = intent.getExtras();
		if(action != null){
			if(action.containsKey("action")){
				String _action = action.getString("action");
				if (_action.equals("boot")){
					bootAdd();
				}else{
					if (_action.equals("delete")){
						delete(action.getInt("id"));
					}else{
						if (_action.equals("add")){
							add(action);
						}
					}
				}
			}
			//Don't shut me off until I'm done
			return START_STICKY;
		}
		return START_NOT_STICKY;

	}
	

	private void add(Bundle action) {
    	//Get data from bundle
    	String title = action.getString("title");
    	String longtext = action.getString("longtext");
    	String icon = action.getString("icon");
    	
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		NotificationItem notif = datasource.createNotif(title, longtext, icon);
		datasource.close();
	    //System.out.println("Notification added with id: " + notif.getID());

		NotificationBuilder build = new NotificationBuilder(this);
		build.buildNotif(notif);
		
		//Delete old notif item if editing
		int old_noif_id = action.getInt("old_noif_id",-1);
		
		if(old_noif_id != -1){
			delete(old_noif_id);
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
    
    private void setUpNextRun() {
    	//Run service every hour or so
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        //Create an offset from the current time in which the alarm will go off.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);   
        am.set(AlarmManager.RTC_WAKEUP,   
        		cal.getTimeInMillis(), pendingIntent);   //TODO make this user configurable; right now it's 1 hour
		
	} 

	private void finish() {
		stopSelf();
	}
	
	@Background
	public void bootAdd(){
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		List<NotificationItem> notif = datasource.getAllItems();
		datasource.close();

		NotificationBuilder build = new NotificationBuilder(this);
		for (NotificationItem i: notif) build.buildNotif(i);
    	onResultFinish();
	}
	
	@Background
	public void delete(int id){
	    //System.out.println("Deleting id: " + id);
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		datasource.deleteItem(id);
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
