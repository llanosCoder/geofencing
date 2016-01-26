package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

public class ConectarFinal extends AsyncTask<String, String, String> {

	String[] parametros = new String[4];
	String token = "", MAC = "";
	String nuevoLink = "";

	protected String doInBackground(String... args) {
		parametros = args;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		token = parametros[0];
		MAC = parametros[1];
		params.add(new BasicNameValuePair("idUsuario", "nada"));
		if (!parametros[0].equals("")) {
			JSONParser jsonParser = new JSONParser();
			nuevoLink = jsonParser.followRedirects("http://www.terra.cl",
					"GET", params);
		}
		return null;
	}

	protected void onPostExecute(String file_url) {
		ServicioPrincipal.conexionG(nuevoLink, token, MAC);
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
