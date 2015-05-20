package tk.giesecke.weatherstation;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;


/**
 * BGService
 * background service to get hourly updates from the sensors
 * and save them into the shared preferences
 *
 * @author Bernd Giesecke
 * @version 0.1 beta May 5, 2015.
 */
public class WSWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		/** Number of active widgets */
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		/** Number of active widgets */
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			WSWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
	                            int appWidgetId) {

		/** Number of active widgets */
		CharSequence widgetText = WSWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single);
		views.setTextViewText(R.id.appwidget_text, widgetText);

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}

