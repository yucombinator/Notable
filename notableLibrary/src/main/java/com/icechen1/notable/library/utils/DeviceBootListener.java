package com.icechen1.notable.library.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.icechen1.notable.library.NotificationService_;

public class DeviceBootListener extends BroadcastReceiver 
{

@Override
public void onReceive(Context context, Intent intent) 
{
   // Intent Service = new Intent(context, NotificationService.class);
   // context.startService(Service);
        Intent Intent = new Intent(context, NotificationService_.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("action", "boot");
        Intent.putExtras(mBundle);
        context.startService(Intent);
}
}