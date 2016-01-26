package com.example.android.geofence;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class ServicioUbicacion extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		LocationListener locationListener = new MyLocationListener();
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 35000, 10,
				locationListener);
	}

	private final class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location locFromGps) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.i("Enabling", "Iniciando servicio si claro");
			Intent miIntent = new Intent(getApplicationContext(),
					ServicioPrincipal.class);
			miIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startService(miIntent);

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}