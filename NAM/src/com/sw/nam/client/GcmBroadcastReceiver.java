package com.sw.nam.client;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sw.nam.ChatActivity;
import com.sw.nam.Common;
import com.sw.nam.DataProvider;
import com.sw.nam.DataProvider.MessageType;
import com.sw.nam.R;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "GcmBroadcastReceiver";
	private Context ctx;	

	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			String messageType = gcm.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server");
			} else {
				String msg = intent.getStringExtra(DataProvider.COL_MESSAGE);
				String senderEmail = intent.getStringExtra(DataProvider.COL_SENDER_EMAIL);
				String receiverEmail = intent.getStringExtra(DataProvider.COL_RECEIVER_EMAIL);
				if(!contactExists(senderEmail, context))
				{
          ContentValues values = new ContentValues(2);
          values.put(DataProvider.COL_NAME, senderEmail.substring(0, senderEmail.indexOf('@')));
          values.put(DataProvider.COL_EMAIL, senderEmail);
          context.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
        }
				ContentValues values = new ContentValues(2);
				values.put(DataProvider.COL_TYPE,  MessageType.INCOMING.ordinal());				
				values.put(DataProvider.COL_MESSAGE, msg);
				values.put(DataProvider.COL_SENDER_EMAIL, senderEmail);
				values.put(DataProvider.COL_RECEIVER_EMAIL, receiverEmail);
				context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
				
				if (Common.isNotify()) {
					newMessageNotification("New message", senderEmail);
				}
			}
			setResultCode(Activity.RESULT_OK);
		} finally {
			mWakeLock.release();
		}
	}
	
	private boolean contactExists(String senderEmail, Context context)
	{
	  Cursor c = context.getContentResolver().query(DataProvider.CONTENT_URI_PROFILE, 
        null, DataProvider.COL_EMAIL + " LIKE ?", new String[]{senderEmail}, null);

	  if(c.getCount()<1)
	    return false;
	  
	  return true;
	}
	
	private void newMessageNotification(String text,  String senderEmail) {
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx);
		notification.setContentTitle(ctx.getString(R.string.app_name)+"-"+text);
		notification.setContentText(senderEmail);
		notification.setAutoCancel(true);
		if(Common.isVibrate()) {
			notification.setDefaults(Notification.DEFAULT_VIBRATE);
		}
		notification.setSmallIcon(R.drawable.ic_launcher);
		if (!TextUtils.isEmpty(Common.getRingtone())) {
			notification.setSound(Uri.parse(Common.getRingtone()));
		}
				
		Intent intent = new Intent(ctx, ChatActivity.class);
		intent.putExtra(Common.IS_NOTIF, true);
		intent.putExtra(Common.PROFILE_NAME, senderEmail);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);		
		PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setContentIntent(pi);
		
		mNotificationManager.notify(1, notification.build());
	}
	private void sendNotification(String text) {
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx);
		notification.setContentTitle(ctx.getString(R.string.app_name));
		notification.setContentText(text);
		notification.setAutoCancel(true);
		if(Common.isVibrate()) {
			notification.setDefaults(Notification.DEFAULT_VIBRATE);
		}
		notification.setSmallIcon(R.drawable.ic_launcher);
		if (!TextUtils.isEmpty(Common.getRingtone())) {
			notification.setSound(Uri.parse(Common.getRingtone()));
		}
		mNotificationManager.notify(0, notification.build());
	}
}