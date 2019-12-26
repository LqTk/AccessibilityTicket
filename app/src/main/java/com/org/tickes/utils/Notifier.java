package com.org.tickes.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.org.tickes.MainActivity;
import com.org.tickes.R;
import com.org.tickes.ui.BrokenRestart;

/**
 * Notification
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Notifier {

	private static Notifier instance = null;

	private NotificationManager notificationManager;

	private static Object INSTANCE_LOCK = new Object();

	public static final int SERVICE_RUNNING = 1;

	private static final String TAG = "Notifier";

	Intent mLauncherIntent = null;
	Notification notification = null;

	int count = 0;

	public static Notifier getInstance() {
		if (instance == null)
			synchronized (INSTANCE_LOCK) {
				if (instance == null) {
					instance = new Notifier();
				}
			}
		return instance;
	}

	private Notifier() {
		this.notificationManager = (NotificationManager) BrokenRestart.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * 清除所有通知
	 * */
	public void cleanAll() {
		if (notificationManager != null) {
			notificationManager.cancelAll();
		}
	}

	public void cancelByType(int type) {
		if (notificationManager != null) {
			notificationManager.cancel(type);
		}
	}

	/**
	 */
	public void notify(String title, String message, String tickerText, int type, boolean canClear) {
		try {
			Context context = BrokenRestart.getInstance();
			Notification.Builder builder = new Notification.Builder(context);

			Intent intent = new Intent();
			PendingIntent contentIntent = null;
			switch (type) {
			case SERVICE_RUNNING:
				intent.setClass(context, MainActivity.class);
				contentIntent = PendingIntent.getActivity(context, SERVICE_RUNNING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				break;
			}
			
			builder.setSmallIcon(R.mipmap.ic_launcher)
					.setDefaults(Notification.DEFAULT_LIGHTS)
					.setTicker(tickerText).setWhen(System.currentTimeMillis())
					.setContentTitle(title).setContentText(message);
			if (Build.VERSION.SDK_INT >= 16) {// Android 4.1之后才有
				builder.setPriority(Notification.PRIORITY_MAX);
			}
						
			if (contentIntent != null) {
				Notification nt = builder.setContentIntent(contentIntent).build();
				
				if (canClear)
					nt.flags |= Notification.FLAG_AUTO_CANCEL;
				else
					nt.flags |= Notification.FLAG_NO_CLEAR;
				
				notificationManager.notify(type, nt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
