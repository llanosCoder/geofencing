package com.example.android.geofence;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveTransitionsIntentService extends IntentService {

	boolean seActivoWiFi = false;
	String networkSSID = "Demo_BCI";
	String pass = "";
	private final HttpClient httpclient = new DefaultHttpClient();
	final HttpParams params = httpclient.getParams();
	JSONParser jsonParser = new JSONParser();
	public String conexionExito = "success";
	public String conexionMensaje = "message";
	String idEquipo, resultado22;
	Context context = this;
	/*
	 * Desde aquí modificar token
	 */
	public String token = "r8zb76e7z";

	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Intent broadcastIntent = new Intent();
		broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		if (LocationClient.hasError(intent)) {

			int errorCode = LocationClient.getErrorCode(intent);

			String errorMessage = LocationServiceErrorMessages.getErrorString(
					this, errorCode);

			Log.e(GeofenceUtils.APPTAG,
					getString(R.string.geofence_transition_error_detail,
							errorMessage));

			broadcastIntent
					.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
					.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);

		} else {

			int transition = LocationClient.getGeofenceTransition(intent);

			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

				List<Geofence> geofences = LocationClient
						.getTriggeringGeofences(intent);
				String[] geofenceIds = new String[geofences.size()];

				for (int index = 0; index < geofences.size(); index++) {
					geofenceIds[index] = geofences.get(index).getRequestId();
				}
				String acceso[] = getSSID(Integer.parseInt(geofenceIds[0]));
				networkSSID = acceso[0];
				pass = acceso[1];
				Log.i("Nombre de red", networkSSID);
				String ids = TextUtils.join(
						GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
				String transitionType = getTransitionString(transition);

				sendNotification(transitionType, ids);
				Context context = this;
				boolean wActivando = WifiControl.encenderWifi(context);
				
				if (!wActivando) {
					boolean helo = WifiControl.conectarWifi(context, networkSSID, pass, token);
					if(!helo){
						stopService(new Intent(this,
								ReceiveTransitionsIntentService.class));
					}
				} else {
					Log.w("Connection", "No se logro conectar a tiempo");
				}
			} else {
				if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
					ServicioPrincipal.comunicacionCancel();
					List<Geofence> geofences = LocationClient
							.getTriggeringGeofences(intent);
					String[] geofenceIds = new String[geofences.size()];
					for (int index = 0; index < geofences.size(); index++) {
						geofenceIds[index] = geofences.get(index)
								.getRequestId();
					}
					String ids = TextUtils.join(
							GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
					String transitionType = getTransitionString(transition);

					sendNotification(transitionType, ids);
					WifiControl.apagarWifi(context, seActivoWiFi);
				} else {

					if (transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
						List<Geofence> geofences = LocationClient
								.getTriggeringGeofences(intent);
						String[] geofenceIds = new String[geofences.size()];
						for (int index = 0; index < geofences.size(); index++) {
							geofenceIds[index] = geofences.get(index)
									.getRequestId();
						}
						String ids = TextUtils.join(
								GeofenceUtils.GEOFENCE_ID_DELIMITER,
								geofenceIds);
						String transitionType = getTransitionString(transition);

						sendNotification(transitionType, ids);
					}
					Log.e(GeofenceUtils.APPTAG,
							getString(
									R.string.geofence_transition_invalid_type,
									transition));
				}
			}
		}
	}

	private String[] getSSID(int id) {
		String linea = "";
		String[] acceso = { "0", "0" };
		ArrayList<ArrayList<String>> sucursalesJ = new ArrayList<ArrayList<String>>();
		try {
			InputStream fraw = getResources().openRawResource(R.raw.sucursales);

			BufferedReader brin = new BufferedReader(
					new InputStreamReader(fraw));

			linea = brin.readLine();

			fraw.close();
		} catch (Exception ex) {
			Log.e("Ficheros", "Error al leer fichero desde recurso raw");
		}
		Log.i("Contenido txt", linea);
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
		Log.i("Contenido txt", linea);
		try {
			JSONArray jsonArray = new JSONArray(linea);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				sucursalesJ.add(new ArrayList<String>());
				sucursalesJ.get(i).add(jsonObject.getString("id"));
				sucursalesJ.get(i).add(jsonObject.getString("ssid"));
				sucursalesJ.get(i).add(jsonObject.getString("key"));
			}
			for (int i = 0; i < sucursalesJ.size(); i++) {
				if (sucursalesJ.get(i).get(0).equals(String.valueOf(id))) {
					acceso[0] = String.valueOf(sucursalesJ.get(i).get(1));
					acceso[1] = String.valueOf(sucursalesJ.get(i).get(2));
				}
			}
		} catch (JSONException e) {
			Log.e("Error JSON", "Error al convertir de JSON a List");
		} catch (IndexOutOfBoundsException e) {
			Log.e("Error JSON", "Indice fuera de limite");
		}
		return acceso;
	}

	private void sendNotification(String transitionType, String ids) {

		Intent notificationIntent = new Intent(getApplicationContext(),
				MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		stackBuilder.addParentStack(MainActivity.class);

		stackBuilder.addNextIntent(notificationIntent);

		PendingIntent notificationPendingIntent = stackBuilder
				.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);

		builder.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(
						getString(
								R.string.geofence_transition_notification_title,
								transitionType, ids))
				.setContentText(
						getString(R.string.geofence_transition_notification_text))
				.setContentIntent(notificationPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(0, builder.build());
	}

	private String getTransitionString(int transitionType) {
		switch (transitionType) {

		case Geofence.GEOFENCE_TRANSITION_ENTER:
			return getString(R.string.geofence_transition_entered);

		case Geofence.GEOFENCE_TRANSITION_EXIT:
			return getString(R.string.geofence_transition_exited);

		default:
			return getString(R.string.geofence_transition_unknown);
		}
	}
}
