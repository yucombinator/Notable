package com.icechen1.notable.library.utils;

public class NotificationItem {
    public static final String CHECKMARK_GRAY = "checkmark_gray";
    public static final String CHECKMARK_ORANGE = "checkmark_orange";
    public static final String CHECKMARK_RED = "checkmark_red";
    public static final String CHECKMARK_GREEN = "checkmark_green";

    //private variables
    public int id;
    String title;
    String icon;
	private String longtext;
	long timestamp;
    long reminderTimestamp;
    boolean dismissed;
 
    // constructor
    public NotificationItem(int id, String title, String longtext, String icon){
        this.id = id;
        this.title = title;
        this.longtext = longtext;
        this.icon = icon;
    }

    // constructor
    public NotificationItem(int id, String title, String longtext, String icon, long reminder_timestamp){
        this.id = id;
        this.title = title;
        this.longtext = longtext;
        this.icon = icon;
        this.reminderTimestamp = reminder_timestamp;
    }
 
    // constructor
    public NotificationItem(int id, String title, String icon){
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

	public NotificationItem() {
		// TODO Auto-generated constructor stub
	}

	// getting ID
    public int getID(){
        return this.id;
    }
 
    // setting id
    public void setID(int l){
        this.id = l;
    }
 
    // getting name
    public String getTitle(){
        return this.title;
    }
 
    // setting name
    public void setTitle(String title){
        this.title = title;
    }
 
    // getting longtext
    public String getLongText(){
        return this.longtext;
    }
 
    // setting longtext
    public void setLongText(String text){
        this.longtext = text;
    }
    
    // getting icon
    public String getIcon(){
        return this.icon;
    }
 
    // setting icon
    public void setIcon(String icon){
        this.icon = icon;
    }
    
    // getting time
    public long getTime(){
        return this.timestamp;
    }
 
    // setting time
    public void setTime(long time){
        this.timestamp = time;
    }

    // getting reminder time
    public long getReminderTime(){
        return this.reminderTimestamp;
    }

    // setting reminder time
    public void setReminderTime(long time){
        this.reminderTimestamp = time;
    }

    // getting dismissed state
    public boolean getDismissed(){
        return this.dismissed;
    }

    // setting dismissed state
    public void setDismissed(boolean b){
        this.dismissed = b;
    }
 
}