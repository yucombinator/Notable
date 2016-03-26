package com.icechen1.notable.library;

import android.content.Intent;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.icechen1.notable.library.utils.NotificationDataSource;
import com.icechen1.notable.library.utils.NotificationItem;

import java.util.List;

public class DeskClockExtension extends DashClockExtension{

	private static final String TAG = "NotableExtension";

	public static final String PREF_NAME = "pref_name";

	@Override
	protected void onInitialize(boolean b){
		setUpdateWhenScreenOn(true);
	}
	@Override
	protected void onUpdateData(int reason) {
		setUpdateWhenScreenOn(true);
		NotificationDataSource datasource = new NotificationDataSource(this);
		datasource.open();
		List<NotificationItem> notif = datasource.getAllItems();
		datasource.close();
		String longBody;
		StringBuilder sb = new StringBuilder();
		for (NotificationItem i: notif){
			if(i.getDismissed())
				continue; //do not recreate dismissed ones

			sb.append(i.getTitle() + "\n");
		}
		longBody = sb.toString();

		boolean visibility = true;

		if(notif.size() == 0){
			visibility = false;
		}

		// Publish the extension data update.

		publishUpdate(new ExtensionData()
				.visible(visibility)
				.icon(R.drawable.ic_checkmark_gray)
				.status(""+notif.size())
				.expandedTitle(notif.size() + " Reminders")
				.expandedBody(longBody)
				.clickIntent(new Intent(this, com.icechen1.notable.library.MainActivity_.class)));
	}

}
