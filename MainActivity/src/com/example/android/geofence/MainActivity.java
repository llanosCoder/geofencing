package com.example.android.geofence;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.android.geofence.GeofenceUtils.REMOVE_TYPE;
import com.example.android.geofence.GeofenceUtils.REQUEST_TYPE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity {

	private REQUEST_TYPE mRequestType;

	private REMOVE_TYPE mRemoveType;

	List<Geofence> mCurrentGeofences;

	private GeofenceRequester mGeofenceRequester;
	private GeofenceRemover mGeofenceRemover;
	private SimpleGeofence mUIGeofence1;
	private SimpleGeofence mUIGeofence2;
	private DecimalFormat mLatLngFormat;
	private DecimalFormat mRadiusFormat;
	private GeofenceSampleReceiver mBroadcastReceiver;
	private IntentFilter mIntentFilter;
	private List<String> mGeofenceIdsToRemove;
	static Context context;

	ArrayList<ArrayList<String>> sucursalesJ = new ArrayList<ArrayList<String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getBaseContext();
		boolean usarGeofencing = true;
		/*
		 * Recibir esta variable a continuacion desde preferencias para controlar la inicializacion
		 * del servicio
		 */
		if (!usarGeofencing) {
			finish();//Finalización del monitoreo
		}
		LocationListener locationListener = new MyLocationListener();
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 35000, 10,
				locationListener);
		String latLngPattern = getString(R.string.lat_lng_pattern);

		mLatLngFormat = new DecimalFormat(latLngPattern);
		mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());
		String radiusPattern = getString(R.string.radius_pattern);
		mRadiusFormat = new DecimalFormat(radiusPattern);
		mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());
		mBroadcastReceiver = new GeofenceSampleReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		mCurrentGeofences = new ArrayList<Geofence>();
		mGeofenceRequester = new GeofenceRequester(this);
		mGeofenceRemover = new GeofenceRemover(this);
		setContentView(R.layout.activity_main);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
		String linea = "";
		try {
			InputStream fraw = getResources().openRawResource(R.raw.sucursales);

			BufferedReader brin = new BufferedReader(
					new InputStreamReader(fraw));

			linea = brin.readLine();

			fraw.close();
		} catch (Exception ex) {
			Log.e("Ficheros", "Error al leer fichero desde recurso raw");
		}
		try {
			byte[] encriptado = Base64.decode(linea, Base64.DEFAULT);
			try {
				linea = new String(encriptado, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e("Encriptacion", "No se pudo parsear linea");
			}

		} catch (Exception e1) {
			Log.e("Encriptacion", "No se pudo desencriptar linea");
		}
		try {
			JSONArray jsonArray = new JSONArray(linea);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				sucursalesJ.add(new ArrayList<String>());
				sucursalesJ.get(i).add(jsonObject.getString("id"));
				sucursalesJ.get(i).add(jsonObject.getString("lat"));
				sucursalesJ.get(i).add(jsonObject.getString("lng"));
				sucursalesJ.get(i).add(jsonObject.getString("rd"));
			}
		} catch (JSONException e) {
			Log.e("Error JSON", "Error al convertir de JSON a List");
		}
		int jcont = 0;
		for (jcont = 0; jcont < sucursalesJ.size(); jcont++) {
			mUIGeofence1 = new SimpleGeofence(
					String.valueOf(sucursalesJ.get(jcont).get(0)),
					Double.valueOf(String
							.valueOf(sucursalesJ.get(jcont).get(1))),
					Double.valueOf(String
							.valueOf(sucursalesJ.get(jcont).get(2))),
					Float.valueOf(String.valueOf(sucursalesJ.get(jcont).get(3))),
					Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
			mUIGeofence2 = new SimpleGeofence(String.valueOf(sucursalesJ.get(
					jcont).get(0)
					+ "_exit"), Double.valueOf(String.valueOf(sucursalesJ.get(
					jcont).get(1))), Double.valueOf(String.valueOf(sucursalesJ
					.get(jcont).get(2))), Float.valueOf(String
					.valueOf(sucursalesJ.get(jcont).get(3))),
					Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_EXIT);
			mCurrentGeofences.add(mUIGeofence1.toGeofence());
			mCurrentGeofences.add(mUIGeofence2.toGeofence());
		}
		try {
			LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Log.w("Localization", "Network localization disabled");

			} else {
				mGeofenceRequester.addGeofences(mCurrentGeofences);
				sucursalesJ.clear();
			}

		} catch (UnsupportedOperationException e) {
		}

	}

	private final class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location locFromGps) {
		}

		@Override
		public void onProviderDisabled(String provider) {
			stopService(new Intent(MainActivity.this,
					ReceiveTransitionsIntentService.class));
			finish();
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {

		case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			case Activity.RESULT_OK:

				if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

					mGeofenceRequester.setInProgressFlag(false);

					mGeofenceRequester.addGeofences(mCurrentGeofences);

				} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

					mGeofenceRemover.setInProgressFlag(false);

					if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

						mGeofenceRemover
								.removeGeofencesByIntent(mGeofenceRequester
										.getRequestPendingIntent());

					} else {

						mGeofenceRemover
								.removeGeofencesById(mGeofenceIdsToRemove);
					}
				}
				break;

			default:

				Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
			}

		default:
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.unknown_activity_request_code,
							requestCode));

			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean servicesConnected() {

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == resultCode) {

			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.play_services_available));

			return true;

		} else {

			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(),
						GeofenceUtils.APPTAG);
			}
			return false;
		}
	}

	public void onUnregisterByPendingIntentClicked(View view) {
		if (!servicesConnected()) {

			return;
		}
		try {
			mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester
					.getRequestPendingIntent());

		} catch (UnsupportedOperationException e) {
		}

	}

	public static void logueo(String idEquipo, String networkSSID,
			String address) {

		Login log = new Login();
		log.execute(idEquipo, networkSSID, address);
	}

	public static void conexionFinal(String resultado22, String networkSSID) {
		ConectarFinal con = new ConectarFinal();
		con.execute(resultado22, networkSSID);
	}

	public static void comunicacion(String networkSSID) {
	}

	public static void comunicacionCancel() {
	}

	public void onUnregisterGeofence1Clicked(View view) {
		mGeofenceIdsToRemove = Collections.singletonList("1");
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		if (!servicesConnected()) {

			return;
		}

		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

		} catch (IllegalArgumentException e) {
			Log.e("Geofence", "No se pudieron remover Geofences");
		} catch (UnsupportedOperationException e) {
		}
	}

	public void onUnregisterGeofence2Clicked(View view) {
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		mGeofenceIdsToRemove = Collections.singletonList("2");

		if (!servicesConnected()) {

			return;
		}

		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

		} catch (IllegalArgumentException e) {
			Log.e("Geofence", "No se pudieron remover Geofences");
		} catch (UnsupportedOperationException e) {
		}
	}

	public class GeofenceSampleReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

				handleGeofenceError(context, intent);

			} else if (TextUtils.equals(action,
					GeofenceUtils.ACTION_GEOFENCES_ADDED)
					|| TextUtils.equals(action,
							GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

				handleGeofenceStatus(context, intent);

			} else if (TextUtils.equals(action,
					GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

				handleGeofenceTransition(context, intent);

			} else {
				Log.e(GeofenceUtils.APPTAG,
						getString(R.string.invalid_action_detail, action));
			}

		}

		private void handleGeofenceStatus(Context context, Intent intent) {

		}

		private void handleGeofenceTransition(Context context, Intent intent) {
		}

		private void handleGeofenceError(Context context, Intent intent) {
			String msg = intent
					.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
			Log.e(GeofenceUtils.APPTAG, msg);
		}
	}

	public static class ErrorDialogFragment extends DialogFragment {

		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
