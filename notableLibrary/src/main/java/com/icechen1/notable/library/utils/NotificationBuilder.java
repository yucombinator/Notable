	package com.icechen1.notable.library.utils;

	import android.annotation.SuppressLint;
	import android.app.Notification;
	import android.app.PendingIntent;
	import android.content.Context;
	import android.content.Intent;
	import android.content.SharedPreferences;
	import android.graphics.Bitmap;
	import android.graphics.BitmapFactory;
	import android.net.Uri;
	import android.os.Build;
	import android.os.Bundle;
	import android.preference.PreferenceManager;
	import android.support.v4.app.NotificationCompat;
	import android.support.v4.app.NotificationManagerCompat;
	import android.support.v4.app.RemoteInput;
	import android.text.format.DateFormat;
	import android.util.Log;

	import com.icechen1.notable.library.PreferencesActivity;
	import com.icechen1.notable.library.R;

	import java.util.Calendar;

	public class NotificationBuilder {
		public static final String NOTABLE = "Notable";
		Context cxt;

        // Key for the string that's delivered in the action's intent
        public static final String KEY_NEW_NOTIFICATION = "key_new_notificaiton";

		public NotificationBuilder(Context cxt){
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

			Intent i = new Intent(cxt, com.icechen1.notable.library.MainActivity_.class);
			// i.setData(Uri.parse(url));
			//	Intent intent = new Intent(this, MainActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(cxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent j = new Intent(cxt, PreferencesActivity.class);
			// i.setData(Uri.parse(url));
			//	Intent intent = new Intent(this, MainActivity.class);
			PendingIntent jIntent = PendingIntent.getActivity(cxt, 0, j, PendingIntent.FLAG_UPDATE_CURRENT);

            String replyLabel = cxt.getResources().getString(R.string.add);
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_NEW_NOTIFICATION)
                    .setLabel(replyLabel)
                    .build();

            // Create the reply action and add the remote input
            NotificationCompat.Action addAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_action_add,
                            cxt.getString(R.string.add), pIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            // Build notification
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(cxt)
                    .setContentTitle(cxt.getResources().getString(R.string.add_long)) //TODO I8LN
					.setContentText(NOTABLE)
					.setSmallIcon(smallicon)
					.setContentIntent(pIntent)
					.setPriority(NotificationCompat.PRIORITY_MIN)
					.setVisibility(NotificationCompat.VISIBILITY_SECRET)
					.setLargeIcon(icon);

			if (PreferenceManager.getDefaultSharedPreferences(cxt).getBoolean("expand_buttons", true)){
				notiBuilder.addAction(addAction);
                notiBuilder.addAction(R.drawable.ic_action_image_edit_dark, cxt.getResources().getString(R.string.settings), jIntent);
            }

			Notification noti = notiBuilder.build();
			noti.deleteIntent = pIntent;

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(cxt);

            // Hide the notification after it's selected
			//noti.flags |= Notification.FLAG_AUTO_CANCEL;
			noti.flags |= Notification.FLAG_NO_CLEAR;

			notificationManager.notify(-1, noti);
			return false;
		}

		/**
		 * Builds a notification and shows it
		 * @param item object
		 * @return true if successful
		 */
		@SuppressLint("NewApi")
		public boolean buildNotif(NotificationItem item,boolean isAlarm){

			Log.i(NOTABLE, "Building id: " + item.getID());

			Log.i(NOTABLE, "Building title: " + item.getTitle());
			Log.i(NOTABLE, "Building time: " + item.getTime());
			Log.i(NOTABLE, "Building icon: " + item.getIcon());

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
			if (input.length < 2 && input[0].length() < 2){
				secondLine = "";
			}else{
				secondLine = input[0];
				tickerText += " : " + secondLine;
			}
			if(input.length>1){
				secondLine += "...";
			}
			String longtext = item.getLongText();
			if(item.getReminderTime()>0){
				//write the reminder time
				String alarmString = "\n"+ DateFormat.getLongDateFormat(cxt).format(item.getReminderTime()) + " " +
						DateFormat.getTimeFormat(cxt).format(item.getReminderTime());
				secondLine += alarmString;
				longtext+= alarmString;
			}

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
			Integer pref = Integer.parseInt(prefs.getString("onClickAction", "2"));
			//CREATE THE INTENT TO LAUNCH EDIT

			Intent j = new Intent(cxt, com.icechen1.notable.library.MainActivity_.class);

			Bundle jBundle = new Bundle();
			jBundle.putInt("id", item.getID());
			j.putExtras(jBundle);
			PendingIntent jIntent = PendingIntent.getActivity(cxt, item.getID(), j, PendingIntent.FLAG_CANCEL_CURRENT);

			//INTENT TO DISMISS
			Intent s = new Intent(cxt, com.icechen1.notable.library.NotificationService_.class);
			Bundle smBundle = new Bundle();
			smBundle.putString("action", "dismiss");
			smBundle.putInt("id", item.getID());
			s.putExtras(smBundle);
			PendingIntent spIntent = PendingIntent.getService(cxt, item.getID(), s, PendingIntent.FLAG_CANCEL_CURRENT);

			//CREATE THE INTENT TO LAUNCH SERVICE
			//INT 1 = DISMISS 2 = DETAIL 3=EDIT
			PendingIntent pIntent;
			if (pref == 2){
				Intent i = new Intent(cxt, com.icechen1.notable.library.DetailActivity_.class);
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

			//build the title
			String title;
			if(isAlarm){
				title = cxt.getResources().getString(R.string.alarm) + ": " + item.getTitle();
			}else{
				title = item.getTitle();
			}

			// Build notification
			NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt)
					.setContentTitle(title)
					.setContentText(secondLine)
					.setSmallIcon(smallicon)
					.setContentIntent(pIntent)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(longtext))
					.setTicker(tickerText)
					.setPriority(Notification.PRIORITY_HIGH)
					.setWhen(item.getTime())
					.setDeleteIntent(spIntent)
					.setVisibility(prefs.getBoolean("show_on_lock_screen", false) ? Notification.VISIBILITY_PRIVATE : Notification.VISIBILITY_SECRET)
					.setLargeIcon(icon);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				builder.setGroup("notable");
			}

			if (prefs.getBoolean("expand_buttons", true)){
				builder.addAction(R.drawable.ic_action_image_edit_dark, cxt.getResources().getString(R.string.edit), jIntent)
						.addAction(R.drawable.ic_action_ic_done, cxt.getResources().getString(R.string.done), spIntent);
						// setUsesChronometer option which subindication to display
						// setOngoing
						// setNumber
			}
			Notification noti = builder.build();
			noti.deleteIntent = spIntent;

			if(prefs.getString("priority", "normal").equals("low")){
                try{
                    //API 16+
                    if (Build.VERSION.SDK_INT > 15) {
                        noti.priority = Notification.PRIORITY_LOW;
                    }
                }catch(Exception e){

                }
			}

			if(prefs.getString("priority", "normal").equals("min")){
                try{
                    //API 16+
                    if (Build.VERSION.SDK_INT > 15) {
                        noti.priority = Notification.PRIORITY_MIN;
                    }
                }catch(Exception e){

                }
			}

			if(isAlarm){
				//Set off the Alarm
				noti.priority= Notification.PRIORITY_HIGH;
				noti.flags |= Notification.FLAG_ONGOING_EVENT;
				noti.flags |= Notification.FLAG_NO_CLEAR;
				noti.flags |= Notification.FLAG_SHOW_LIGHTS;

				noti.when = Calendar.getInstance().getTimeInMillis();

				//Set the alarms
				//noti.defaults |= Notification.DEFAULT_SOUND;
				noti.defaults |= Notification.DEFAULT_VIBRATE;
				noti.defaults |= Notification.DEFAULT_LIGHTS;

				//Load the ringtone
				String strRingtonePreference = prefs.getString("notification_sound", "DEFAULT_SOUND");
				noti.sound = Uri.parse(strRingtonePreference);
			}

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(cxt);
			// Hide the notification after it's selected

			if (!prefs.getBoolean("swipe", false)) {
				noti.flags |= Notification.FLAG_ONGOING_EVENT;
				noti.flags |= Notification.FLAG_NO_CLEAR;
			}
			//noti.priority = Notification.PRIORITY_MAX;
			notificationManager.notify(item.getID(), noti);
			return true;
		}
	}
