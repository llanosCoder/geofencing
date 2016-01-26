package com.example.android.geofence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.Geofence;

public class ServicioPrincipal extends Service {
	static List<Geofence> mCurrentGeofences;

	private SimpleGeofence mUIGeofence1;
	private SimpleGeofence mUIGeofence2;
	private DecimalFormat mLatLngFormat;
	private DecimalFormat mRadiusFormat;
	private GeofenceSampleReceiver mBroadcastReceiver;
	private IntentFilter mIntentFilter;
	static Context context;
	static CanalComunicacion can;

	ArrayList<ArrayList<String>> sucursalesJ = new ArrayList<ArrayList<String>>();

	public void onCreate() {
		super.onCreate();

		context = this;
		/*
		 * Recibir esta variable a continuacion desde preferencias para controlar la inicializacion
		 * del servicio
		 */
		boolean usarGeofencing = true;
		if (!usarGeofencing) {
			stopSelf();//Finalización del monitoreo
		}

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
				Log.i("serv", "andando");

				Intent i = new Intent(this, ActivityRequester.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
				sucursalesJ.clear();
			}

		} catch (UnsupportedOperationException e) {
		}

	}

	public static List<Geofence> helo() {
		return mCurrentGeofences;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {

		}
	}

	public static void logueo(String idEquipo, String networkSSID,
			String address) {

		Login log = new Login();
		log.execute(idEquipo, networkSSID, address);
	}

	public static void conexionFinal(String token, String MAC) {
		ConectarFinal con = new ConectarFinal();
		con.execute(token, MAC);
	}

	public static void conexionG(String resultado22, String token, String MAC) {
		ConexionG conG = new ConexionG();
		conG.execute(resultado22, token, MAC);
	}

	public static void comunicacion(String token) {
		can = new CanalComunicacion();
		can.execute(token);
	}

	public static void comunicacionCancel() {
		Log.w("Comunicacion...", "Cancel");
		can.cancel(true);
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

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
