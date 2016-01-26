package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class CanalComunicacion extends AsyncTask<String, Void, String> {

	String resultado22 = "";
	String idEquipo = "";
	public boolean enSucursal = true;
	public int sleepComunicacion = 10;

	protected String doInBackground(String... args) {
		while (enSucursal) {
			for (String arg : args) {
				idEquipo = arg;
			}

			List<NameValuePair> paramsCom = new ArrayList<NameValuePair>();
			paramsCom.add(new BasicNameValuePair("idUsuario", "1"));
			paramsCom.add(new BasicNameValuePair("token", idEquipo.toString()));

			try {

				JSONObject json = new JSONObject();
				JSONParser jsonParser = new JSONParser();
				json = jsonParser.makeHttpRequest(
						"http://gestsol.cl/offloadbci/getMensajes.php", "GET",
						paramsCom);
				List<NameValuePair> resultados = new ArrayList<NameValuePair>();
				for (int i = 0; i < json.length(); i++) {

					resultados.add(new BasicNameValuePair(String.valueOf(i)
							+ "success", json.getString("success")));
					resultados.add(new BasicNameValuePair(String.valueOf(i)
							+ "message", json.getString("message")));
				}
				/*
				 * Gestsol: Control sobre qué hacer con el mensaje aquí
				 */
			} catch (Exception e) {
				Log.e("Comunicando...", "Error al recibir JSON");
			}
			try {
				Thread.sleep(sleepComunicacion * 1000);
			} catch (InterruptedException e) {
				break;
			}
		}

		return "true";
	}

	protected void onPostExecute(String file_url) {
		ConectarFinal conec = new ConectarFinal();
		conec.execute(resultado22, idEquipo);

	}

}
