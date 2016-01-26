package com.example.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class IniciadorServicio extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent arg1) {
		Intent miIntent = new Intent(context, ServicioPrincipal.class);
		miIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(miIntent);
		
	}
}