package com.icechen1.notable.pro;

import android.content.Intent;
import android.view.View;

import com.icechen1.notable.library.PreferencesActivity;

public class MainActivity extends com.icechen1.notable.library.MainActivity{
	@Override
	public void menuBtn(View v){
		// launch prefs
		Intent gopref = new Intent(this, PreferencesActivity.class);
		gopref.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//startActivity(gopref);
		//Intent intent = getIntent();
		//finish();
		startActivity(gopref);
	}
}
