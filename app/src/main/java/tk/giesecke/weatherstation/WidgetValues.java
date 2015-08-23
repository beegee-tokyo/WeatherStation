package tk.giesecke.weatherstation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

/**
 * WidgetValues
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in
 * {@link WidgetValuesConfigure}
 *
 * @author Bernd Giesecke
 * @version 1.3 August 23, 2015
 */
public class WidgetValues extends AppWidgetProvider {

	/** broadcast signature for widget update */
	public static final String WIDGET_VALUE_UPDATE = "WIDGET_VALUE_UPDATE";

	@Override
	public void onReceive(@NonNull Context context, @NonNull Intent intent) {
		//*******************************************************
		// Receiver for the widget (click on widget, update service,
		// disable, ... we handle only the update requests here
		//*******************************************************

		super.onReceive(context, intent);

		/** App widget manager for all widgets of this app */
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		/** Component name of this widget */
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
				WidgetValues.class.getName());
		/** List of all active widgets */
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		/** Remote views of the widgets */
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_values);

		if (WIDGET_VALUE_UPDATE.equals(intent.getAction())) {

			/** Access to shared preferences of the app widgets */
			SharedPreferences wPrefs = context.getSharedPreferences("WidgetValues", 0);
			/** Flag if the found widget is a ghost widget that the system didn't remove */
			boolean isGhostWidget = true;
			/** Pointer to host of the widgets */
			AppWidgetHost appWidgetHost = new AppWidgetHost(context, 1);

			for (int i = 0; i < appWidgetIds.length-1; i++) {
				/** Number of this widget */
				int widgetNum = wPrefs.getInt("wID"+appWidgetIds[i],9999);
				if (widgetNum != 9999) {
					isGhostWidget = false;
				}
				if (isGhostWidget) {
					appWidgetHost.deleteAppWidgetId(appWidgetIds[i]);
				}
			}

			for (int appWidgetId : appWidgetIds) {
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}

			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		startUpdateService(context, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		/** Access to shared preferences of the app widgets */
		SharedPreferences wPrefs = context.getSharedPreferences("WidgetValues", 0);
		/** Number of widgets */
		int numWidgets = wPrefs.getInt("wNums", 0);
		for (int appWidgetId : appWidgetIds) {
			/** Number of this widget */
			int widgetNum = wPrefs.getInt("wID"+appWidgetId,9999);
			if (widgetNum == 9999) { //No entries for this widget ID, probably fake widget
				wPrefs.edit().remove("wID"+appWidgetId);
			} else {
				wPrefs.edit().remove("wType" + widgetNum).apply();
				wPrefs.edit().remove("wValueRow1" + widgetNum).apply();
				wPrefs.edit().remove("wValueRow2" + widgetNum).apply();
				wPrefs.edit().remove("wValueRow3" + widgetNum).apply();
				wPrefs.edit().remove("wID" + appWidgetId).apply();
				if (numWidgets != 0) {
					wPrefs.edit().putInt("wNums", numWidgets - 1).apply();
				}
				wPrefs.edit().commit();
			}
		}
	}

	@Override
	public void onDisabled(Context context) {
		/** Intent to start scheduled update of the widgets */
		Intent intent = new Intent(WidgetValues.WIDGET_VALUE_UPDATE);
		/** Pending intent for broadcast message to update widgets */
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context, 1701, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		/** Alarm manager for scheduled widget updates */
		AlarmManager alarmManager = (AlarmManager) context.getSystemService
				(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	/**
	 * Update widget content
	 *
	 * @param context
	 *            Application context
	 * @param appWidgetManager
	 *            Manager for this app widgets
	 * @param appWidgetId
	 *            ID of the widget to be updated
	 * @param temp
	 *            Temperature value
	 * @param press
	 *            Pressure value
	 * @param humid
	 *            Humidity value
	 */
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
	                            int appWidgetId,
	                            float temp,
	                            float press,
	                            float humid) {

		// Construct the RemoteViews object
		/** View pointer to the widget */
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_values);

		// Create an Intent to launch MainActivity
		/** Intent to start app if battery icon is pushed */
		Intent intent1 = new Intent(context, WeatherStation.class);
		intent1.putExtra("appWidgetId", appWidgetId);
		// Creating a pending intent, which will be invoked when the user
		// clicks on the widget
		/** Pending intent to start app if widget is pushed */
		PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		//  Attach an on-click listener to the widget
		views.setOnClickPendingIntent(R.id.rlWidget1, pendingIntent1);

		/** Access to shared preferences of the app widgets */
		SharedPreferences wPrefs = context.getSharedPreferences("WidgetValues", 0);
		/** Access to shared preferences of the app */
		SharedPreferences mPrefs = context.getSharedPreferences("WeatherStation", 0);
		/* User selected temperature unit */
		int tempUnit = mPrefs.getInt("temp_unit", 0);
		/* User selected pressure unit */
		int pressUnit = mPrefs.getInt("press_unit", 0);
		/** Number of this widget */
		int numOfWidget = wPrefs.getInt("wID"+appWidgetId,9999);
		if (numOfWidget != 9999) {
			/** Array with selected values per row */
			int[] selectWValR = new int[3];
			/** Array with widget IDs for values per row */
			int[] tv_widgetRowValue = {R.id.tv_widgetRow1Value, R.id.tv_widgetRow2Value, R.id.tv_widgetRow3Value};
			/** Array with widget IDs for value signs per row */
			int[] tv_widgetRowSign = {R.id.tv_widgetRow1Sign, R.id.tv_widgetRow2Sign, R.id.tv_widgetRow3Sign};
			selectWValR[0] = wPrefs.getInt("wValueRow1"+numOfWidget,0);
			selectWValR[1] = wPrefs.getInt("wValueRow2"+numOfWidget,0);
			selectWValR[2] = wPrefs.getInt("wValueRow3"+numOfWidget,0);
			/** Type of this widget */
			int selectWtype = wPrefs.getInt("wType"+numOfWidget,0);

			for (int i=2; i>=0; i--) {
				if (selectWtype < i) {
					views.setViewVisibility(tv_widgetRowValue[i], View.GONE);
					views.setViewVisibility(tv_widgetRowSign[i], View.GONE);
				}
			}

			for (int i=0; i<=selectWtype; i++) {
				switch (selectWValR[i]) {
					case 0:
						views.setViewVisibility(tv_widgetRowValue[i], View.GONE);
						views.setViewVisibility(tv_widgetRowSign[i], View.GONE);
						break;
					case 1:
						views.setTextViewText(tv_widgetRowValue[i], String.format("%.01f", Utils.cToU(temp, tempUnit)));
						views.setTextViewText(tv_widgetRowSign[i], Utils.tempUnit(context, tempUnit));
						break;
					case 2:
						views.setTextViewText(tv_widgetRowValue[i], String.format("%.01f", Utils.pToU(press, pressUnit)));
						views.setTextViewText(tv_widgetRowSign[i], Utils.pressUnit(context, pressUnit));
						break;
					case 3:
						views.setTextViewText(tv_widgetRowValue[i], String.format("%.01f", humid));
						views.setTextViewText(tv_widgetRowSign[i], context.getString(R.string.humidSign));
						break;
				}
			}
		}

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Start background service to receive new data and update
	 * the widget content
	 *
	 * @param context
	 *            Context of this application
	 * @param appWidgetIds
	 *            List of all active widgets
	 */
	public static void startUpdateService(Context context, int[] appWidgetIds) {
		/** Intent to start widget update process */
		Intent intent = new Intent(Intent.ACTION_SYNC, null, context, WidgetValuesService.class);
		/** Number of widget in work */
		int numWidgets = 0;
		for (int appWidgetId : appWidgetIds) {
			intent.putExtra(WidgetValuesService.PARAM_WID_ID + numWidgets, appWidgetId);
			numWidgets++;
		}
		intent.putExtra(WidgetValuesService.PARAM_WID_NUM, numWidgets);
		context.startService(intent);
	}

	/**
	 * Force update of widgets with current values
	 *
	 * @param context
	 *          Application context.
	 * @param temp
	 *          Temperature value
	 * @param press
	 *          Pressure value
	 * @param humid
	 *          Humidity value
	 */
	public static void forceUpdate(Context context,
	                               float temp,
	                               float press,
	                               float humid) {
		/** AppWidgetManager for this widget */
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		/** ComponentName for this widget */
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
				WidgetValues.class.getName());
		/** Array of existing widgets */
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		/** Access to shared preferences of the app widgets */
		SharedPreferences wPrefs = context.getSharedPreferences("WidgetValues", 0);
		/** Flag if the found widget is a ghost widget that the system didn't remove */
		boolean isGhostWidget = true;
		/** Pointer to host of the widgets */
		AppWidgetHost appWidgetHost = new AppWidgetHost(context, 1);

		for (int i = 0; i < appWidgetIds.length-1; i++) {
			/** Number of this widget */
			int widgetNum = wPrefs.getInt("wID"+appWidgetIds[i],9999);
			if (widgetNum != 9999) {
				isGhostWidget = false;
			}
			if (isGhostWidget) {
				appWidgetHost.deleteAppWidgetId(appWidgetIds[i]);
			}
		}

		for (int appWidgetId : appWidgetIds) {
			WidgetValues.updateAppWidget(context, appWidgetManager, appWidgetId,
					temp,
					press,
					humid);
		}
	}
}

