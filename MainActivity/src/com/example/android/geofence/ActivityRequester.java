package com.example.android.geofence;

import java.util.List;

import com.google.android.gms.location.Geofence;

import android.app.Activity;
import android.os.Bundle;

public class ActivityRequester extends Activity {

	Activity activ = this;
	private GeofenceRequester mGeofenceRequester;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<Geofence> mCurrentGeofences = ServicioPrincipal.helo();
		mGeofenceRequester = new GeofenceRequester(activ);
		mGeofenceRequester.addGeofences(mCurrentGeofences);
		finish();
	}

}
