package tk.giesecke.weatherstation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * AutoStart
 * <p/>
 * Broadcast receiver for boot completed
 *
 * @author Bernd Giesecke
 * @version 1.1 June 19th, 2015.
 */
public class AutoStart extends BroadcastReceiver {
	/** Debug tag */
	private final static String LOG_TAG = "CoolIt AutoStart";

	public AutoStart() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onReceive");
            /* Setting the alarm here */
			/** Intent of background service */
			Intent alarmIntent = new Intent(context, BGService.class);
			/** Pending intent of background service */
			PendingIntent pendingIntent = PendingIntent.getService(
					context,
					1700,
					alarmIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			/** AlarmManager for repeated call of background service */
			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 3600000, pendingIntent);

			/** Access to shared preferences of app widget */
			SharedPreferences wPrefs = context.getSharedPreferences("WidgetValues",0);
			if (wPrefs.getInt("wNums",0) != 0) {
				/** Update interval in ms */
				int alarmTime;
				switch (wPrefs.getInt("wUpdate",0)) {
					default:
						alarmTime = 60000;
						break;
					case 1:
						alarmTime = 300000;
						break;
					case 2:
						alarmTime = 600000;
						break;
					case 3:
						alarmTime = 3000000;
						break;
				}

				/** Intent for broadcast message to update widgets */
				Intent widgetIntent = new Intent(WidgetValues.WIDGET_VALUE_UPDATE);
				/** Pending intent for broadcast message to update widgets */
				PendingIntent pendingWidgetIntent = PendingIntent.getBroadcast(
						context, 1701, widgetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				/** Alarm manager for scheduled widget updates */
				AlarmManager alarmManager = (AlarmManager) context.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis() + 10000,
						alarmTime, pendingWidgetIntent);
			}
		}
	}
}
