package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

public class Login extends AsyncTask<String, String, String> {

	String resultado22 = "";
	String token = "", mac = "";
	String[] parametros = new String[3];

	protected String doInBackground(String... args) {

		parametros = args;
		token = parametros[0];
		mac = parametros[1];
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", token));
		JSONParser jsonParser = new JSONParser();
		resultado22 = jsonParser.followRedirects(
				"http://gestsol.cl/offloadbci/setLlego.php", "GET", params);
		return null;
	}

	protected void onPostExecute(String file_url) {
		ServicioPrincipal.conexionFinal(token, mac);
		Log.i("Login", "Proceso terminado");
	}

}
