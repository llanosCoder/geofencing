package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiControl {
	
	private static final int REGISTRATION_TIMEOUT = 30 * 1000;
	private static final int WAIT_TIMEOUT = 30 * 1000;

	public static boolean encenderWifi(Context context){
		
		WifiManager wifiManager = (WifiManager)context
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		boolean wActivando = false;
		int sleepy = 1;
		do {
			if (!wifiManager.isWifiEnabled()) {
				wActivando = true;
				try {
					Thread.sleep(sleepy * 1000);
				} catch (InterruptedException e) {
					Log.e("Sleep error",
							"Error al intentar poner en sleep.");
				}
				sleepy *= 2;
				Log.i("sleepy", String.valueOf(sleepy));
			} else {
				wActivando = false;
			}

		} while (wActivando && sleepy < 30);
		return wActivando;
	}
	
	public static boolean conectarWifi(Context context, String networkSSID, String pass, String token){
		HttpClient httpclient = new DefaultHttpClient();
		final HttpParams params2 = httpclient.getParams();
		int sleepy = 1;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isConnected = mWifi.isConnected();
		if (!isConnected) {
			Log.i("Connection", "No esta conectado");

			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (!pass.equals("")) {
				WifiConfiguration wc = new WifiConfiguration();
				wc.SSID = "\"" + networkSSID + "\"";
				wc.preSharedKey = "\"" + pass + "\"";
				wc.hiddenSSID = true;
				wc.status = WifiConfiguration.Status.ENABLED;
				wc.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);
				wc.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.CCMP);
				wc.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				wc.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.TKIP);
				wc.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.CCMP);
				wc.allowedProtocols
						.set(WifiConfiguration.Protocol.RSN);

				int res = wifi.addNetwork(wc);
				Log.d("WifiPreference", "add Network returned "
						+ res);

				boolean b = wifi.enableNetwork(res, true);
				Log.d("WifiPreference", "enableNetwork returned "
						+ b);
			} else {
				WifiConfiguration conf = new WifiConfiguration();
				conf.SSID = "\"" + networkSSID + "\"";
				conf.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.NONE);
				List<WifiConfiguration> list = wifi
						.getConfiguredNetworks();
				boolean existe = false;
				for (WifiConfiguration i : list) {
					if (i.SSID != null
							&& i.SSID.equals("\"" + networkSSID
									+ "\"")) {
						wifi.updateNetwork(conf);
						existe = true;
					}
				}
				if (!existe)
					wifi.addNetwork(conf);
				list = wifi.getConfiguredNetworks();
				for (WifiConfiguration i : list) {
					if (i.SSID != null
							&& i.SSID.equals("\"" + networkSSID
									+ "\"")) {
						wifi.disconnect();
						wifi.enableNetwork(i.networkId, true);
						wifi.reconnect();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							Log.e("Error", "No se pudo dormir aplicacion");
						}
						break;
					}
				}
			}
		}
		sleepy = 1;
		boolean conectando = true;
		mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		do {
			if (!mWifi.isConnected()) {
				try {
					Thread.sleep(sleepy * 1000);
					mWifi = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				} catch (InterruptedException e) {
					Log.e("Sleep error",
							"Error al intentar poner en sleep.");
				}
				sleepy *= 2;
				Log.i("sleepy", String.valueOf(sleepy));
			} else {
				conectando = false;
			}

		} while (conectando && sleepy < 30);
		mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			try {
				HttpConnectionParams.setConnectionTimeout(params2,
						REGISTRATION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params2,
						WAIT_TIMEOUT);
				ConnManagerParams.setTimeout(params2, WAIT_TIMEOUT);

				TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				String idEquipo = telephonyManager.getDeviceId();
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("idUsuario",
						idEquipo));
				params.add(new BasicNameValuePair("idBanco",
						networkSSID));
				Log.i("Request", "Preparing");
				LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				if (locManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					Log.i("Request", "Location enabled");
					WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = manager.getConnectionInfo();
					String address = info.getMacAddress();
					String ssid = info.getSSID();
					if (ssid.equals(networkSSID)) {
						ServicioPrincipal.logueo(token,
								networkSSID, address);
					} else {
						Log.e("Wifi", "SSID no corresponde");
					}
					Log.i("Request", "Executed");
				} else {
					Log.e("Request",
							"No se realizó, sin LocationServices");
					return false;
				}

			} catch (Exception e) {
				Log.e("HTTP4:", "e");

			}
		}
		return true;
	}
	
	public static void apagarWifi(Context context, boolean seActivoWiFi){
		WifiManager wifiManager = (WifiManager) 
				context.getSystemService(Context.WIFI_SERVICE);

		if (seActivoWiFi) {
			if (wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(false);
			}
		}
	}
}
