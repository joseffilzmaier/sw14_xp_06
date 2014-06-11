package com.sw.nam.client;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sw.nam.Common;

public class GcmUtil {

	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	private Context ctx;
	private SharedPreferences prefs;
	private GoogleCloudMessaging gcm;
	private AsyncTask<Void, Void, Boolean> registrationTask;

	public GcmUtil(Context applicationContext) {
		super();
		ctx = applicationContext;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		String regid = getRegistrationId();
		if (regid.length() == 0) {
			registerBackground();
		} else {
			broadcastStatus(true);
		}
		gcm = GoogleCloudMessaging.getInstance(ctx);
	}

	private String getRegistrationId() {
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			return "";
		}

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion || isRegistrationExpired()) {

			return "";
		}
		return registrationId;
	}

	private void setRegistrationId(String regId) {
		int appVersion = getAppVersion();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis()
				+ REGISTRATION_EXPIRY_TIME_MS;

		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}

	private int getAppVersion() {
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private boolean isRegistrationExpired() {
		long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME,
				-1);
		return System.currentTimeMillis() > expirationTime;
	}

	private void registerBackground() {
		registrationTask = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(ctx);
					}
					String regid = gcm.register(Common.getSenderId());
					ServerUtilities.register(Common.getPreferredEmail(), regid);
					setRegistrationId(regid);
					return Boolean.TRUE;

				} catch (IOException ex) {
					return Boolean.FALSE;
				}
			}

			@Override
			protected void onPostExecute(Boolean status) {
				broadcastStatus(status);
			}
		}.execute(null, null, null);
	}

	private void broadcastStatus(boolean status) {
		Intent intent = new Intent(Common.ACTION_REGISTER);
		intent.putExtra(Common.EXTRA_STATUS, status ? Common.STATUS_SUCCESS
				: Common.STATUS_FAILED);
		ctx.sendBroadcast(intent);
	}

	public void cleanup() {
		if (registrationTask != null) {
			registrationTask.cancel(true);
		}
		if (gcm != null) {
			gcm.close();
		}
	}

}
