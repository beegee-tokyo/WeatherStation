package tk.giesecke.weatherstation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.Spinner;


/**
 * WidgetValuesConfigure
 * The configuration screen for the {@link WidgetValues WidgetValues} AppWidget.
 *
 * @author Bernd Giesecke
 * @version 1.0 May 31, 2015.
 */
public class WidgetValuesConfigure extends Activity {

	/** Access to shared preferences of application widgets*/
	private SharedPreferences wPrefs;
	/** Widget type (1, 2 or 3 rows of values) */
	private int selectWtype;
	/** Widget update rate (1, 5, 10 or 30 min) */
	private int selectWupdate;
	/** Selected value for row 1 */
	private int selectWvalR1;
	/** Selected value for row 2 */
	private int selectWvalR2;
	/** Selected value for row 3 */
	private int selectWvalR3;
	/** Number of active widgets */
	private int numOfWidget;
	/** Context of widget configurator */
	private static Context widgetContext;

	/** ID of this widget */
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public WidgetValuesConfigure() {
		super();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		widgetContext = this;

		setContentView(R.layout.widget_values_configure);
		findViewById(R.id.bWadd).setOnClickListener(mOnClickListener);
		findViewById(R.id.bWcancel).setOnClickListener(mOnClickListener);
		wPrefs = getSharedPreferences("WidgetValues",0);
		numOfWidget = wPrefs.getInt("wNums",0);

		// Find the widget id from the intent.
		/** Intent of this class call */
		Intent intent = getIntent();
		/** Bundle with the extras coming with the intent */
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		/** Spinner for row 1 value selection */
		final Spinner spWrow1 = (Spinner) findViewById(R.id.spWrow1);
		spWrow1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					if ((position == selectWvalR2) || (position == selectWvalR3)) {
						configError(position);
						spWrow1.setSelection(selectWvalR1);
					}
				}
				selectWvalR1 = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Auto-generated method stub
			}
		});
		selectWvalR1 = wPrefs.getInt("wValueRow1",1);
		spWrow1.setSelection(selectWvalR1);

		/** Spinner for row 2 value selection */
		final Spinner spWrow2 = (Spinner) findViewById(R.id.spWrow2);
		spWrow2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					if ((position == selectWvalR1) || (position == selectWvalR3)) {
						configError(position);
						spWrow2.setSelection(selectWvalR2);
					}
				}
				selectWvalR2 = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Auto-generated method stub
			}
		});
		selectWvalR2 = wPrefs.getInt("wValueRow2",2);
		spWrow2.setSelection(selectWvalR2);

		/** Spinner for row 3 value selection */
		final Spinner spWrow3 = (Spinner) findViewById(R.id.spWrow3);
		spWrow3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					if ((position == selectWvalR1) || (position == selectWvalR2)) {
						configError(position);
						spWrow3.setSelection(selectWvalR3);
					}
				}
				selectWvalR3 = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Auto-generated method stub
			}
		});
		selectWvalR3 = wPrefs.getInt("wValueRow3",3);
		spWrow3.setSelection(selectWvalR3);

		selectWtype =  2;
		/** Radio group with radio buttons for widget type */
		RadioGroup rgWidgetType = (RadioGroup) findViewById(R.id.rgWidgetType);
		rgWidgetType.clearCheck();
		rgWidgetType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.rbWtype1:
						selectWtype = 0;
						findViewById(R.id.tvWvalueRow2).setVisibility(View.GONE);
						findViewById(R.id.tvWvalueRow3).setVisibility(View.GONE);
						findViewById(R.id.spWrow2).setVisibility(View.GONE);
						findViewById(R.id.spWrow3).setVisibility(View.GONE);
						selectWvalR2 = 0;
						selectWvalR3 = 0;
						spWrow2.setSelection(selectWvalR2);
						spWrow3.setSelection(selectWvalR3);
						break;
					case R.id.rbWtype2:
						selectWtype = 1;
						findViewById(R.id.tvWvalueRow2).setVisibility(View.VISIBLE);
						findViewById(R.id.tvWvalueRow3).setVisibility(View.GONE);
						findViewById(R.id.spWrow2).setVisibility(View.VISIBLE);
						findViewById(R.id.spWrow3).setVisibility(View.GONE);
						spWrow2.setSelection(selectWvalR2);
						selectWvalR3 = 0;
						spWrow3.setSelection(selectWvalR3);
						break;
					case R.id.rbWtype3:
						selectWtype = 2;
						findViewById(R.id.tvWvalueRow2).setVisibility(View.VISIBLE);
						findViewById(R.id.tvWvalueRow3).setVisibility(View.VISIBLE);
						findViewById(R.id.spWrow2).setVisibility(View.VISIBLE);
						findViewById(R.id.spWrow3).setVisibility(View.VISIBLE);
						spWrow2.setSelection(selectWvalR2);
						spWrow3.setSelection(selectWvalR3);
						break;
				}
			}
		});

		selectWupdate = wPrefs.getInt("wUpdate", 0);
		/** Radio group with radio buttons for widget update rate */
		RadioGroup rgWidgetUpdate = (RadioGroup) findViewById(R.id.rgWidgetUpdate);
		rgWidgetUpdate.clearCheck();
		switch (selectWupdate) {
			case 0:
				rgWidgetUpdate.check(R.id.rbWupd1);
				break;
			case 1:
				rgWidgetUpdate.check(R.id.rbWupd2);
				break;
			case 2:
				rgWidgetUpdate.check(R.id.rbWupd3);
				break;
			case 3:
				rgWidgetUpdate.check(R.id.rbWupd4);
				break;
		}
		rgWidgetUpdate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.rbWupd1:
						selectWupdate = 0;
						break;
					case R.id.rbWupd2:
						selectWupdate = 1;
						break;
					case R.id.rbWupd3:
						selectWupdate = 2;
						break;
					case R.id.rbWupd4:
						selectWupdate = 3;
						break;
				}
			}
		});

	}

	/**
	 * Listener for click events
	 */
	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.bWcancel:
					finish();
					break;
				case R.id.bWadd:
					/** Context for this configuration class */
					final Context context = WidgetValuesConfigure.this;

					// Check if at least a value for row 1 was selected
					if (selectWvalR1 == 0 && selectWvalR2 == 0 && selectWvalR3 == 0) {
						Utils.myAlert(widgetContext,
								widgetContext.getString(R.string.configErrorHead),
								widgetContext.getString(R.string.configError));
						return;
					}

					// Store widget info in shared preferences
					wPrefs.edit().putInt("wID"+mAppWidgetId, numOfWidget).apply();
					wPrefs.edit().putInt("wUpdate",selectWupdate).apply();
					wPrefs.edit().putInt("wNums", numOfWidget +1).apply();
					wPrefs.edit().putInt("wType" + numOfWidget, selectWtype).apply();
					wPrefs.edit().putInt("wValueRow1"+ numOfWidget,selectWvalR1).apply();
					wPrefs.edit().putInt("wValueRow2"+ numOfWidget,selectWvalR2).apply();
					wPrefs.edit().putInt("wValueRow3"+ numOfWidget,selectWvalR3).apply();

					// It is the responsibility of the configuration activity to update the app widget
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					WidgetValues.updateAppWidget(context, appWidgetManager, mAppWidgetId,
							0,
							0,
							0);

					// Start alarm to update the widget in the requested interval
					/** Update interval in ms */
					int alarmTime;
					switch (selectWupdate) {
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
					Intent intent = new Intent(WidgetValues.WIDGET_VALUE_UPDATE);
					intent.putExtra("id", mAppWidgetId);
					/** Pending intent for broadcast message to update widgets */
					PendingIntent pendingIntent = PendingIntent.getBroadcast(
							context, 1701, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					/** Alarm manager for scheduled widget updates */
					AlarmManager alarmManager = (AlarmManager) context.getSystemService
							(Context.ALARM_SERVICE);
					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
							System.currentTimeMillis(),
							alarmTime, pendingIntent);
					// Make sure we pass back the original appWidgetId
					/** Intent to broadcast update request to widget */
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
					break;
			}
		}
	};

	/**
	 * Handler for configuration errors in case of double entries for measure types
	 *
	 * @param position
	 *          position of the double entry
	 */
	private static void configError(int position) {
		/** Error message for double selection of a measure */
		String errorMsg;
		switch (position) {
			case 1:
				errorMsg = widgetContext.getString(R.string.configErrorPos1);
				break;
			case 2:
				errorMsg = widgetContext.getString(R.string.configErrorPos2);
				break;
			default:
				errorMsg = widgetContext.getString(R.string.configErrorPos3);
				break;
		}
		Utils.myAlert(widgetContext,
				widgetContext.getString(R.string.configErrorHead),
				errorMsg);
	}
}

