package com.icechen1.notable.library.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationDataSource {

    private final Context mContext;
    // Database fields
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = {
			SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_TITLE,
			SQLiteHelper.COLUMN_LONGTEXT,
			SQLiteHelper.COLUMN_TIME ,
			SQLiteHelper.COLUMN_ICON,
			SQLiteHelper.COLUMN_REMINDER_TIME,
			SQLiteHelper.COLUMN_DISMISSED};

	public NotificationDataSource(Context context) {
        mContext = context;
		dbHelper = new SQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public NotificationItem createNotif(String title, String longtext, String icon,long reminder_Time) {
		ContentValues values = new ContentValues();

		//System.out.println("Building icon: " + icon);

		values.put(SQLiteHelper.COLUMN_TITLE, title);
		values.put(SQLiteHelper.COLUMN_LONGTEXT, longtext);
		values.put(SQLiteHelper.COLUMN_TIME, Calendar.getInstance().getTimeInMillis());
		values.put(SQLiteHelper.COLUMN_ICON, icon);
		values.put(SQLiteHelper.COLUMN_REMINDER_TIME, reminder_Time);

		long insertId = database.insert(SQLiteHelper.TABLE_NOTIFS, null,
				values);
		Cursor cursor = database.query(SQLiteHelper.TABLE_NOTIFS,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		NotificationItem newNotif = cursorToItem(cursor);
		cursor.close();
		return newNotif;
	}

	public void updateItem(long rowId, String title, String longtext, String icon) { //TODO FINISH
		ContentValues args = new ContentValues();
		args.put(SQLiteHelper.COLUMN_TITLE, title);
		args.put(SQLiteHelper.COLUMN_LONGTEXT, longtext);
		args.put(SQLiteHelper.COLUMN_TIME, Calendar.getInstance().getTimeInMillis());
		args.put(SQLiteHelper.COLUMN_ICON, icon);

		database.update(SQLiteHelper.TABLE_NOTIFS, args, "_id=" + rowId, null);
	}

	public void setDismissed(long rowId, boolean dismissed) {
		ContentValues args = new ContentValues();
		args.put(SQLiteHelper.COLUMN_DISMISSED, dismissed ? 1 : 0);
		database.update(SQLiteHelper.TABLE_NOTIFS, args, "_id=" + rowId, null);

        //broadcast db change
        Intent intent = new Intent();
        intent.setAction("com.icechen1.notable.DATABASE_CHANGED");
        mContext.sendBroadcast(intent);
	}

	public void deleteItem(NotificationItem notif) {
		long id = notif.getID();
		System.out.println("Notification deleted with id: " + id);
		database.delete(SQLiteHelper.TABLE_NOTIFS, SQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}


	public void deleteItem(int id) {
		System.out.println("Notification deleted with id: " + id);
		database.delete(SQLiteHelper.TABLE_NOTIFS, SQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}


	public List<NotificationItem> getAllItems() {
		List<NotificationItem> comments = new ArrayList<NotificationItem>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_NOTIFS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			NotificationItem comment = cursorToItem(cursor);
			comments.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}


	public NotificationItem getItem(long id) {
		//   long id = notif.getID();
		Cursor cursor = database.query(SQLiteHelper.TABLE_NOTIFS,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + id, null,
				null, null, null);
		cursor.moveToFirst();
		NotificationItem newNotif = cursorToItem(cursor);
		cursor.close();
		return newNotif;
	}

	public Cursor query(){
		return database.query(SQLiteHelper.TABLE_NOTIFS,
				allColumns, null, null,
				null, null, SQLiteHelper.COLUMN_TIME + " DESC");
	}

	private NotificationItem cursorToItem(Cursor cursor) {
		NotificationItem item = new NotificationItem();
		try{
			item.setID(cursor.getInt(0));
			item.setTitle(cursor.getString(1));
			item.setLongText(cursor.getString(2));
			item.setTime(cursor.getLong(3));
			item.setIcon(cursor.getString(4));
			item.setReminderTime(cursor.getLong(5));
			item.setDismissed(cursor.getInt(6) == 1);
		}catch(Exception e){
			Log.e("NOTABLE","Error with database cursor!");
			return null;
		}

		return item;
	}

	public void dismissItem(int id) {
		setDismissed(id, true);
	}

	class SQLiteHelper extends SQLiteOpenHelper {

		public static final String TABLE_NOTIFS = "NOTIF";
		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_TITLE = "title";
		public static final String COLUMN_LONGTEXT = "longtext";
		public static final String COLUMN_ICON = "icon";
		public static final String COLUMN_TIME = "time";
		public static final String COLUMN_REMINDER_TIME = "reminder";
		public static final String COLUMN_DISMISSED = "dismissed";

		private static final String DATABASE_NAME = "notifs.db";
		private static final int DATABASE_VERSION = 7;

		// Database creation sql statement
		private static final String DATABASE_CREATE = "create table "
				+ TABLE_NOTIFS + "(" + COLUMN_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_TITLE + " text not null,"
				+ COLUMN_LONGTEXT + " text not null,"
				+ COLUMN_TIME + " long not null,"
				+ COLUMN_ICON + " text not null,"
				+ COLUMN_REMINDER_TIME+" LONG DEFAULT 0,"
				+ COLUMN_DISMISSED+" INTEGER DEFAULT 0"
				+ ");";

		public SQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(SQLiteHelper.class.getName(), "Upgrading database from version " +
					oldVersion + " to " + newVersion);
			// If you need to add a column
			if (oldVersion < 6) {
				db.execSQL("ALTER TABLE "+ TABLE_NOTIFS +" ADD COLUMN "+COLUMN_REMINDER_TIME+" LONG DEFAULT 0");
			} else if (oldVersion < 7) {
				db.execSQL("ALTER TABLE "+ TABLE_NOTIFS +" ADD COLUMN "+COLUMN_DISMISSED+" INTEGER DEFAULT 0");
			}
              /*
			if (oldVersion < 4) {
				Log.w(SQLiteHelper.class.getName(), "Recreating database");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFS);
				onCreate(db);
			} else {
				if (oldVersion < 5)
					db.execSQL("UPDATE " + TABLE_NOTIFS + " SET " + COLUMN_LONGTEXT +
							" = '' WHERE " + COLUMN_LONGTEXT + " = 'Notable'");
			} */
		}

	}
}
