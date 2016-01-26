package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

public class ConexionG extends AsyncTask<String, String, String> {

	String[] parametros = new String[4];
	String token = "", MAC = "";
	String link = "";

	protected String doInBackground(String... args) {
		parametros = args;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		token = parametros[1];
		MAC = parametros[2];
		link = parametros[0];
		Log.i("Conexion", link);
		params.add(new BasicNameValuePair("idUsuario", token));
		params.add(new BasicNameValuePair("idUsuario", MAC));
		if (!link.equals("") && URLUtil.isValidUrl(link)) {
			JSONParser jsonParser = new JSONParser();
			jsonParser.makeHttpRequest(link, "GET", params);
		}
		return null;
	}

	protected void onPostExecute(String file_url) {
		Log.i("Conexion G", "Proceso terminado");
		ServicioPrincipal.comunicacion(parametros[1]);
	}

	protected String limpiarURL(String url) {
		int i = 0;
		String nuevaUrl = "";
		while (i < url.length()) {
			nuevaUrl += "";
			i++;
		}
		return nuevaUrl;
	}

}
