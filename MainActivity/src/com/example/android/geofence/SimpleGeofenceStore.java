package com.example.android.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SimpleGeofenceStore {

	private final SharedPreferences mPrefs;

	private static final String SHARED_PREFERENCE_NAME = MainActivity.class
			.getSimpleName();

	public SimpleGeofenceStore(Context context) {
		mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	public SimpleGeofence getGeofence(String id) {

		double lat = mPrefs.getFloat(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
				GeofenceUtils.INVALID_FLOAT_VALUE);

		double lng = mPrefs.getFloat(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
				GeofenceUtils.INVALID_FLOAT_VALUE);

		float radius = mPrefs.getFloat(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
				GeofenceUtils.INVALID_FLOAT_VALUE);

		long expirationDuration = mPrefs.getLong(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
				GeofenceUtils.INVALID_LONG_VALUE);

		int transitionType = mPrefs.getInt(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
				GeofenceUtils.INVALID_INT_VALUE);

		if (lat != GeofenceUtils.INVALID_FLOAT_VALUE
				&& lng != GeofenceUtils.INVALID_FLOAT_VALUE
				&& radius != GeofenceUtils.INVALID_FLOAT_VALUE
				&& expirationDuration != GeofenceUtils.INVALID_LONG_VALUE
				&& transitionType != GeofenceUtils.INVALID_INT_VALUE) {

			return new SimpleGeofence(id, lat, lng, radius, expirationDuration,
					transitionType);

		} else {
			return null;
		}
	}

	public void setGeofence(String id, SimpleGeofence geofence) {

		Editor editor = mPrefs.edit();

		editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
				(float) geofence.getLatitude());

		editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
				(float) geofence.getLongitude());

		editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
				geofence.getRadius());

		editor.putLong(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
				geofence.getExpirationDuration());

		editor.putInt(
				getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
				geofence.getTransitionType());

		editor.commit();
	}

	public void clearGeofence(String id) {

		Editor editor = mPrefs.edit();
		editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE));
		editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE));
		editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS));
		editor.remove(getGeofenceFieldKey(id,
				GeofenceUtils.KEY_EXPIRATION_DURATION));
		editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE));
		editor.commit();
	}

	private String getGeofenceFieldKey(String id, String fieldName) {

		return GeofenceUtils.KEY_PREFIX + id + "_" + fieldName;
	}
}
