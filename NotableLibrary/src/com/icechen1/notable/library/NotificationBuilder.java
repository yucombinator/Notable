package com.icechen1.notable.library;

import com.icechen1.notable.library.DetailActivity_;
import com.icechen1.notable.library.MainActivity_;
import com.icechen1.notable.library.NotificationService_;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class NotificationBuilder {
	Context cxt;


	NotificationBuilder(Context cxt){
		this.cxt = cxt;
	}


	/**
	 * Creates a new instance of NotificationBuilder
	 * @param cxt Context
	 * @return a NotificationBuilder object
	 */
	public static NotificationBuilder newInstance(Context cxt){
		return new NotificationBuilder(cxt);
	}
	public boolean buildShortCutNotif(){
		Bitmap icon = BitmapFactory.decodeResource(cxt.getResources(), R.drawable.ic_checkmark_blue);
		int smallicon = R.drawable.ic_stat_add_msg;
    	
		Intent i = new Intent(cxt, MainActivity_.class);
		// i.setData(Uri.parse(url));
		//	Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(cxt, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent j = new Intent(cxt, PreferencesActivity.class);
		// i.setData(Uri.parse(url));
		//	Intent intent = new Intent(this, MainActivity.class);
		PendingIntent jIntent = PendingIntent.getActivity(cxt, 0, j, Intent.FLAG_ACTIVITY_NEW_TASK);

		
		// Build notification
		Notification noti;
		if (PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("expand_buttons", true)){

			noti = new NotificationCompat.Builder(cxt)
			.setContentTitle(cxt.getResources().getString(R.string.add_long)) //TODO I8LN
			.setContentText("Notable")
			.setSmallIcon(smallicon)
			.setContentIntent(pIntent)
			.addAction(R.drawable.ic_action_ic_edit, cxt.getResources().getString(R.string.settings), jIntent)
			.addAction(R.drawable.ic_action_add, cxt.getResources().getString(R.string.add), pIntent)
			.setPriority(Notification.PRIORITY_MIN)
			.setLargeIcon(icon)
			.build();
		}else{
			noti = new NotificationCompat.Builder(cxt)
			.setContentTitle(cxt.getResources().getString(R.string.add_long)) //TODO I8LN
			.setContentText("Notable")
			.setSmallIcon(smallicon)
			.setContentIntent(pIntent)
			.setPriority(Notification.PRIORITY_MIN)
			.setLargeIcon(icon)
			.build();
		}

		noti.deleteIntent = pIntent;
		
		NotificationManager notificationManager = 
				(NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);

		// Hide the notification after it's selected
		//noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.flags |= Notification.FLAG_NO_CLEAR;

		notificationManager.notify(-1, noti);
		return false;
	}

	/**
	 * Builds a notification and shows it
	 * @param NotificationItem object
	 * @return true if successful
	 */
	@SuppressLint("NewApi")
	public boolean buildNotif(NotificationItem item){
		
	    System.out.println("Building id: " + item.getID());
	    
	    System.out.println("Building title: " + item.getTitle());
	    System.out.println("Building time: " + item.getTime());
	    System.out.println("Building icon: " + item.getIcon());
		
		int smallicon = R.drawable.ic_stat_status_icon;
		int iconId = R.drawable.ic_checkmark_gray;
		if (item.getIcon().equals("checkmark_gray")) {
			// Do nothing
		} else if (item.getIcon().equals("checkmark_orange"))
			iconId = R.drawable.ic_checkmark_orange;
		else if (item.getIcon().equals("checkmark_red"))
			iconId = R.drawable.ic_checkmark_red;
		else if (item.getIcon().equals("checkmark_green"))
			iconId = R.drawable.ic_checkmark_green;
		Bitmap icon = BitmapFactory.decodeResource(cxt.getResources(), iconId);
		
    	String[] input = item.getLongText().toString().split("\n");
    	String secondLine, tickerText = item.getTitle();
    	if (input.length < 2 && input[0].length() < 20){
    		secondLine = "";
    	}else{
    		secondLine = input[0];
			tickerText += " : " + secondLine;
    	}
    	
    	Integer pref = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(cxt).getString("onClickAction", "2"));
		//CREATE THE INTENT TO LAUNCH EDIT
		
		Intent j = new Intent(cxt, MainActivity_.class);
		
        Bundle jBundle = new Bundle();
        jBundle.putInt("id", item.getID());
        j.putExtras(jBundle);
		PendingIntent jIntent = PendingIntent.getActivity(cxt, item.getID(), j, PendingIntent.FLAG_CANCEL_CURRENT);
		
		//INTENT TO DISMISS
	    Intent s = new Intent(cxt, NotificationService_.class);
		Bundle smBundle = new Bundle();
		smBundle.putString("action", "delete");
		smBundle.putInt("id", item.getID());
		s.putExtras(smBundle);
		PendingIntent spIntent = PendingIntent.getService(cxt, item.getID(), s, PendingIntent.FLAG_CANCEL_CURRENT);

		//CREATE THE INTENT TO LAUNCH SERVICE
		//INT 1 = DISMISS 2 = DETAIL 3=EDIT
		PendingIntent pIntent;
		if (pref == 2){
			Intent i = new Intent(cxt, DetailActivity_.class);
			Bundle iBundle = new Bundle();
			iBundle.putInt("id", item.getID());
			i.putExtras(iBundle);
			pIntent = PendingIntent.getActivity(cxt, item.getID(), i, PendingIntent.FLAG_CANCEL_CURRENT);
		}else{
			if (pref == 3){
				pIntent = jIntent;
			}else{
				pIntent = spIntent;
			}
		}

		// Build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt)
				.setContentTitle(item.getTitle())
				.setContentText(secondLine)
				.setSmallIcon(smallicon)
				.setContentIntent(pIntent)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(item.getLongText())) 
				.setTicker(tickerText)
				.setPriority(Notification.PRIORITY_HIGH)
				.setWhen(item.getTime())
				.setDeleteIntent(spIntent)
				.setLargeIcon(icon);
		if (PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("expand_buttons", true)){
			builder.addAction(R.drawable.ic_action_ic_edit, cxt.getResources().getString(R.string.edit), jIntent)
					.addAction(R.drawable.ic_action_ic_done, cxt.getResources().getString(R.string.done), spIntent);
					// setUsesChronometer option which subindication to display
					// setOngoing
					// setNumber
		}
		Notification noti = builder.build();
		noti.deleteIntent = spIntent;
		
		if(PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("low_prio", false)){
		try{
			//API 16+
			if (Build.VERSION.SDK_INT > 15) { 
			noti.priority = Notification.PRIORITY_LOW;
			}
		}catch(Exception e){
			
		}
		}
		
		
		NotificationManager notificationManager = 
				(NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		

		// Hide the notification after it's selected
		
		if(PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("swipe", false)){

		}else{
			noti.flags |= Notification.FLAG_ONGOING_EVENT;
			noti.flags |= Notification.FLAG_NO_CLEAR;
		}
		//noti.priority = Notification.PRIORITY_MAX;
		notificationManager.notify(item.getID(), noti);
		return true;
	}
}
