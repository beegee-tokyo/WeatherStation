package tk.giesecke.weatherstation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * ScreenReceiver
 * Get screen on/off broadcast messages to start/stop the update of the app widgets
 *
 * @author Bernd Giesecke
 * @version 1.0 May 31, 2015.
 */
public class ScreenReceiver extends BroadcastReceiver {

	/** Flag showing screen status */
	public ScreenReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		/* Access to shared preferences of app widget */
		SharedPreferences wPrefs;
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			/** Access to shared preferences of app widget */
			wPrefs = context.getSharedPreferences("WidgetValues",0);
			if (wPrefs.getInt("wNums", 0) != 0) {
				/** Intent to start scheduled update of the widgets */
				Intent stopIntent = new Intent(WidgetValues.WIDGET_VALUE_UPDATE);
				/** Pending intent for broadcast message to update widgets */
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, 1701, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				/** Alarm manager for scheduled widget updates */
				AlarmManager alarmManager = (AlarmManager) context.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.cancel(pendingIntent);
			}
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			/** Access to shared preferences of app widget */
			wPrefs = context.getSharedPreferences("WidgetValues",0);
			if (wPrefs.getInt("wNums", 0) != 0) {
				/** Update interval in ms */
				int alarmTime;
				switch (wPrefs.getInt("wUpdate", 0)) {
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
				Intent startIntent = new Intent(WidgetValues.WIDGET_VALUE_UPDATE);
				/** Pending intent for broadcast message to update widgets */
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, 1701, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				/** Alarm manager for scheduled widget updates */
				AlarmManager alarmManager = (AlarmManager) context.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis(),
						alarmTime, pendingIntent);

				/** AppWidgetManager for this widget */
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				/** ComponentName for this widget */
				ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
						WidgetValues.class.getName());
				/** Array of existing widgets */
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
				WidgetValues.startUpdateService(context, appWidgetIds);
			}
		}
	}
}
