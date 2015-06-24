package tk.giesecke.weatherstation;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * WidgetValuesService
 * Service to update the widgets
 *
 * @author Bernd Giesecke
 * @version 1.1 June 19th, 2015.
 */
public class WidgetValuesService extends IntentService implements SensorEventListener {

	/** Debug tag */
	private static final String LOG_TAG = "WeatherStation-Widget";
	/** Broadcast parameter widget ID */
	public static final String PARAM_WID_ID = "id";
	/** Broadcast parameter widget number */
	public static final String PARAM_WID_NUM = "num";

	/** List of all active widgets */
	private int[] appWidgetIds;
	/** App widget manager for all widgets of this app */
	private AppWidgetManager appWidgetManager;

	/** SensorManager to get info about available sensors */
	private SensorManager mSensorManager;
	/** Access to temp sensor */
	private Sensor mTempSensor;
	/** Access to pressure sensor */
	private Sensor mPressSensor;
	/* Access to humidity sensor */
	private Sensor mHumidSensor;
	/** Last temperature for hourly recording */
	private float lastTempValue;
	/** Last pressure for hourly recording */
	private float lastPressValue;
	/** Last humidity for hourly recording */
	private float lastHumidValue;

	public WidgetValuesService() {
		super(WidgetValuesService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onHandleIntent");

		/** App widget manager for all widgets of this app */
		appWidgetManager = AppWidgetManager.getInstance(this);

		/** Number of widgets */
		int numWidgets = intent.getIntExtra(PARAM_WID_NUM, 0);

		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "numWidgets = " + numWidgets);
		/** List of all active widgets */
		appWidgetIds = new int[numWidgets];
		if (numWidgets != 0) {
			for (int i = 0; i < numWidgets; i++) {
				appWidgetIds[i] = intent.getIntExtra(PARAM_WID_ID + i, 0);
				if (BuildConfig.DEBUG)
					Log.d(LOG_TAG, "appWidgetIds[" + i + "] = " + appWidgetIds[i]);
			}
			// preset values
			lastTempValue = 0;
			lastPressValue = 0;
			lastHumidValue = 0;

			// connect to temperature sensor
			mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			// connect to temperature sensor
			/* Access to temp sensor */
			mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			// connect to air pressure sensor
			/* Access to pressure sensor */
			mPressSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			// connect to humidity sensor
			/* Access to humidity sensor */
			mHumidSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

			if (mTempSensor != null) {
				mSensorManager.registerListener(this, mTempSensor, SensorManager.SENSOR_DELAY_FASTEST);
				lastTempValue = -9999;
			}
			if (mPressSensor != null) {
				mSensorManager.registerListener(this, mPressSensor, SensorManager.SENSOR_DELAY_FASTEST);
				lastPressValue = -9999;
			}
			if (mHumidSensor != null) {
				mSensorManager.registerListener(this, mHumidSensor, SensorManager.SENSOR_DELAY_FASTEST);
				lastHumidValue = -9999;
			}
		} else { // Somethings terrible wrong here, this should not be called when numWidgets is 0
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "numWidgets = " + numWidgets);
		}
	}

	/**
	 * Listen to temperature sensor accuracy changes
	 *
	 * @param sensor
	 *            Sensor sensor.
	 * @param accuracy
	 *            Sensor accuracy.
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Listen to weather sensor change events
	 * @see <a href="http://androidcookbook.com/Recipe.seam?recipeId=2385">
	 * Reading the Temperature Sensor</a>
	 * @see <a href="http://www.survivingwithandroid.com/2013/09/android-sensor-tutorial-barometer-sensor.html">
	 * Android Sensor Tutorial: Barometer Sensor</a>
	 * @see <a href="http://code.tutsplus.com/tutorials/building-apps-with-environment-sensors--pre-46879">
	 * Building Apps with Environment Sensors</a>
	 *
	 * @param event
	 *            SensorEvent event.
	 */
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				lastTempValue = Math.round(event.values[0] * 1000.0f) / 1000.0f;
				break;
			case Sensor.TYPE_PRESSURE:
				lastPressValue = Math.round(event.values[0] * 1000.0f) / 1000.0f;
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				lastHumidValue = Math.round(event.values[0] * 1000.0f) / 1000.0f;
				break;
		}
		if (lastTempValue != -9999 && lastPressValue != -9999 && lastHumidValue != -9999)
		{
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Widget update ready");

			if (appWidgetIds.length != 0) {
				for (int appWidgetId : appWidgetIds) {
					WidgetValues.updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId,
							lastTempValue,
							lastPressValue,
							lastHumidValue);
				}
			}

			if (mTempSensor != null) {
				mSensorManager.unregisterListener(this, mTempSensor);
			}
			if (mPressSensor != null) {
				mSensorManager.unregisterListener(this, mPressSensor);
			}
			if (mHumidSensor != null) {
				mSensorManager.unregisterListener(this, mHumidSensor);
			}
		}
	}
}
