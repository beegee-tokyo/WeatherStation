package tk.giesecke.weatherstation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidplot.util.PixelUtils;

import com.androidplot.xy.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * WeatherStation
 * main activity
 *
 * @author Bernd Giesecke
 * @version 1.0 May 31, 2015.
 */
public class WeatherStation extends ActionBarActivity implements
		View.OnClickListener, SensorEventListener, AdapterView.OnItemClickListener {

	/** Debug tag */
	private static final String LOG_TAG = "WeatherStation";
    /** Application context */
    public static Context appContext;
	/** Application context */
	static Activity appActivity;
	/** Access to shared preferences of application*/
	private SharedPreferences mPrefs;
	/** Pointer to drawer layout */
	private DrawerLayout mDrawerLayout;
	/** Time for updating content of main UI */
	private int autoUpdate;
	/** Number picker index of time for updating content of main UI */
	private int autoUpdateIndex;

	/** Custom view for analog type thermometer */
	private GaugeView gvThermo;
	/** Custom view for analog type barometer */
	private GaugeView gvBaro;
	/** Custom view for analog type hygrometer */
	private GaugeView gvHygro;
	/** Current main gauge viewed */
	private GaugeView gvBig;

	/** Flag for update request content of main UI */
	private boolean isUpdateRequest;
	/** Last temperature for ui display */
	static float lastTempValue;
	/** Last pressure for ui display  */
	static float lastPressValue;
	/** Last pressure for vintage ui display  */
	static float lastPressValue2;
	/** Last humidity for ui display */
	static float lastHumidValue;

	/** User selected theme */
	private int themeColor;
    /** User clicked theme */
    private int selectColor;
	/** Current selected UI layout */
	private int uiLayout;
	/** User selected temperature unit */
	static int tempUnit;
	/** User clicked temperature unit */
	private int selectTempUnit;
	/** User selected pressure unit */
	static int pressUnit;
	/** User clicked pressure unit */
	private int selectPressUnit;
    /** Radio group column 1 of double column radio group */
    private RadioGroup rgPressUnit1;
	/** Radio group column 2 of double column radio group */
	private RadioGroup rgPressUnit2;
	/** Radio group listener for column 1 of an double column radio group */
	private RadioGroup.OnCheckedChangeListener PressUnit1;
	/** Radio group listener for column 2 of an double column radio group */
	private RadioGroup.OnCheckedChangeListener PressUnit2;

	/** Dark color of selected theme */
	static int colorDark;
    /** Bright color of selected theme */
    static int colorBright;
	/** Color of status bar when in vintage view */
	private static int colorStatusVintage;
	/** Color of status bar when in station view */
	private static int colorStatusStation;
	/** Bright color of selected theme */
	private static Drawable toolBarDrawable;

	/** SensorManager to get info about available sensors */
	static SensorManager mSensorManager;
	/** Access to temp sensor */
	static Sensor mTempSensor;
	/** Access to pressure sensor */
	static Sensor mPressSensor;
	/** Access to humidity sensor */
	static Sensor mHumidSensor;
	/** Textview to show current temperature in plot view */
	private TextView tvCurrTempPlot;
	/** Textview to show current temperature in vintage view*/
	private TextView tvCurrTempVintage;
	/** Textview to show current temperature in weather station view*/
	private TextView tvCurrTempStation;
	/** Textview to show current pressure in plot view */
	private TextView tvCurrPressPlot;
	/** Textview to show current pressure in vintage view*/
	private TextView tvCurrPressVintage;
	/** Textview to show current pressure in weather station view*/
	private TextView tvCurrPressStation;
	/** Textview to show current humidity in plot view */
	private TextView tvCurrHumidPlot;
	/** Textview to show current humidity in vintage view*/
	private TextView tvCurrHumidVintage;
	/** Textview to show current humidity in weather station view*/
	private TextView tvCurrHumidStation;
	/** Textview to show time in weather station view*/
	static TextView tvTime;
	/** Textview to show date in weather station view*/
	static TextView tvDate;
	/** Textview to show max temperature in weather station view*/
	private TextView tvTodayMaxTemp;
	/** Textview to show min temperature in weather station view*/
	private TextView tvTodayMinTemp;
	/** Textview to show max pressure in weather station view*/
	private TextView tvTodayMaxPress;
	/** Textview to show min pressure in weather station view*/
	private TextView tvTodayMinPress;
	/** Textview to show max humidity in weather station view*/
	private TextView tvTodayMaxHumid;
	/** Textview to show min humidity in weather station view*/
	private TextView tvTodayMinHumid;

	/** Last measured temperature for tendency detection */
	private float lastTemp;
	/** Last measured pressure for tendency detection */
	private float lastPress;
	/** Last measured humidity for tendency detection */
	private float lastHumid;

	/** Number of plot y values */
    private static int plotValues = 20;
	/** Day to show in day view */
    private int dayToShow = 1;
	/** Flag for continuous update of charts */
    private boolean isContinuous = true;
	/** Number of recorded days */
	static int numOfDayRecords;

	/** XYPlot view for the temperature chart */
	static XYPlot tempLevelsPlot;
	/** Data series for the temperature */
	static SimpleXYSeries tempLevelsSeries = null;
	/** XYPlot view for the barometric pressure chart */
	static XYPlot pressLevelsPlot;
	/** Data series for the barometric pressure */
	static SimpleXYSeries pressLevelsSeries = null;
	/** XYPlot view for the humidity chart */
	static XYPlot humidLevelsPlot;
	/** Data series for the humidity */
	static SimpleXYSeries humidLevelsSeries = null;
    /** Min value of temperature series */
    static float minTempValue;
	/** Max value of temperature series */
	static float maxTempValue;
	/** Min value of pressure series */
	static float minPressValue;
	/** Max value of pressure series */
	static float maxPressValue;
	/** Min value of humidity series */
	static float minHumidValue;
	/** Max value of humidity series */
	static float maxHumidValue;
	/** Min value of temperature series */
	static float todayMinTemp;
	/** Max value of temperature series */
	static float todayMaxTemp;
	/** Min value of pressure series */
	static float todayMinPress;
	/** Max value of pressure series */
	static float todayMaxPress;
	/** Min value of humidity series */
	static float todayMinHumid;
	/** Max value of humidity series */
	static float todayMaxHumid;

	/** Action bar drawer toggle */
	private ActionBarDrawerToggle mDrawerToggle;
	/** Drawer title when drawer is open */
	private CharSequence mDrawerTitle;
	/** Drawer title when drawer is closed */
	private CharSequence mTitle;

	/** Instance of weather db helper */
    public static WSDatabaseHelper wsDbHelper;
	/** Access to weather db  */
    public static SQLiteDatabase dataBase;
    /** String with path and filename to location of backup file */
    public static String exportFilePath;
    /** String with path and filename to location of restore file */
    public static String restoreFilePath;
    /** String with path to read location of JSON file */
    public static String path;
    /** Flag for result of file selection dialog */
    static boolean isFileSelected = false;
    /** List with available backup files */
    static final List<String> files = new ArrayList<>();

	/** Small text size as float */
	static float textSizeSmall;
	/** Medium text size as float */
	static float textSizeMedium;
	/** 10dp margin as float */
    static float margin10dp;
	/** 20dp margin as float */
	static float margin20dp;
	/** 25dp margin as float */
	static float margin25dp;
	/** 30dp margin as float */
	static float margin30dp;
	/** 40dp margin as float */
	static float margin40dp;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** Access to shared preferences of application*/
		mPrefs = getSharedPreferences("WeatherStation", 0);
		/** Holds user selected theme */
		themeColor = mPrefs.getInt("dark_theme", 0);
        switch (themeColor) {
            case 1: // green
                setTheme(R.style.AppTheme_Green_Base);
                colorDark = getResources().getColor(android.R.color.holo_green_dark);
                colorBright = getResources().getColor(R.color.my_green_bright);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(android.R.color.holo_green_light));
                break;
            case 2: // grey
                setTheme(R.style.AppTheme_Gray_Base);
                colorDark = getResources().getColor(android.R.color.darker_gray);
                colorBright = getResources().getColor(R.color.my_gray_bright);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(R.color.my_gray_light));
                break;
            case 3: // orange
                setTheme(R.style.AppTheme_Orange_Base);
                colorDark = getResources().getColor(android.R.color.holo_orange_dark);
                colorBright = getResources().getColor(R.color.my_orange_bright);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(android.R.color.holo_orange_light));
                break;
            case 4: // red
                setTheme(R.style.AppTheme_Red_Base);
                colorDark = getResources().getColor(android.R.color.holo_red_dark);
                colorBright = getResources().getColor(R.color.my_red_bright);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(android.R.color.holo_red_light));
                break;
            case 5: // white
                setTheme(R.style.AppTheme_White_Base);
                colorDark = getResources().getColor(R.color.my_white_dark);
                colorBright = getResources().getColor(android.R.color.white);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(android.R.color.white));
                break;
            default: // == 0 == blue
                setTheme(R.style.AppTheme_Base);
                colorDark = getResources().getColor(android.R.color.holo_blue_dark);
                colorBright = getResources().getColor(android.R.color.holo_blue_bright);
	            toolBarDrawable = new ColorDrawable(getResources().getColor(android.R.color.holo_blue_light));
                break;
        }

		colorStatusVintage = getResources().getColor(R.color.my_gold);
		colorStatusStation = getResources().getColor(android.R.color.darker_gray);

		if (android.os.Build.VERSION.SDK_INT >= 21) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setStatusBarColor(colorDark);
		}

		setContentView(R.layout.weatherstation);

		/** Pointer to action bar */
		Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
		if (actionBar != null) {
			setSupportActionBar(actionBar);
		}

		mTitle = mDrawerTitle = getTitle();
		/** Pointer to drawer layout */
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
		mDrawerLayout.setStatusBarBackgroundColor(colorDark);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				actionBar,
				R.string.drawer_open,
				R.string.drawer_close
		) {

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
			}

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		// Register listener for left & right swipes in the action bar
		new SwipeDetector(actionBar).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
			@Override
			public void SwipeEventDetected(SwipeDetector.SwipeTypeEnum swipeType) {
				if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM ||
						swipeType == SwipeDetector.SwipeTypeEnum.BOTTOM_TO_TOP) {
					return;
				}
				/** Layout for modern view */
				LinearLayout plotLayout = (LinearLayout) findViewById(R.id.modern);
				/** Layout for vintage view */
				RelativeLayout vintageLayout = (RelativeLayout) findViewById(R.id.vintage);
				/** Layout for station view */
				RelativeLayout stationLayout = (RelativeLayout) findViewById(R.id.station);
				if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT) {
					switch (uiLayout) {
						case 0:
							plotLayout.setVisibility(View.GONE);
							vintageLayout.setVisibility(View.VISIBLE);
							findViewById(R.id.b_infinite).setVisibility(View.GONE);
							findViewById(R.id.b_day_view).setVisibility(View.GONE);
							findViewById(R.id.b_month_view).setVisibility(View.GONE);
							getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gold_brown));
							findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gold_brown));
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorStatusVintage);
							}
							uiLayout = 1;
							break;
						case 1:
							vintageLayout.setVisibility(View.GONE);
							stationLayout.setVisibility(View.VISIBLE);
							getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gray_bright));
							findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gray_bright));
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorStatusStation);
							}
							uiLayout = 2;
							break;
						case 2:
							stationLayout.setVisibility(View.GONE);
							plotLayout.setVisibility(View.VISIBLE);
							findViewById(R.id.b_infinite).setVisibility(View.VISIBLE);
							findViewById(R.id.b_day_view).setVisibility(View.VISIBLE);
							findViewById(R.id.b_month_view).setVisibility(View.VISIBLE);
							getSupportActionBar().setBackgroundDrawable(toolBarDrawable);
							findViewById(R.id.b_tb_up).setBackground(toolBarDrawable);
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorDark);
							}
							uiLayout = 0;
							break;
					}
					mPrefs.edit().putInt("UI_Layout", uiLayout).apply();
				} else if (swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
					switch (uiLayout) {
						case 0:
							plotLayout.setVisibility(View.GONE);
							stationLayout.setVisibility(View.VISIBLE);
							findViewById(R.id.b_infinite).setVisibility(View.GONE);
							findViewById(R.id.b_day_view).setVisibility(View.GONE);
							findViewById(R.id.b_month_view).setVisibility(View.GONE);
							getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gray_bright));
							findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gray_bright));
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorStatusStation);
							}
							uiLayout = 2;
							break;
						case 2:
							stationLayout.setVisibility(View.GONE);
							vintageLayout.setVisibility(View.VISIBLE);
							getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gold_brown));
							findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gold_brown));
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorStatusVintage);
							}
							uiLayout = 1;
							break;
						case 1:
							vintageLayout.setVisibility(View.GONE);
							plotLayout.setVisibility(View.VISIBLE);
							findViewById(R.id.b_infinite).setVisibility(View.VISIBLE);
							findViewById(R.id.b_day_view).setVisibility(View.VISIBLE);
							findViewById(R.id.b_month_view).setVisibility(View.VISIBLE);
							getSupportActionBar().setBackgroundDrawable(toolBarDrawable);
							findViewById(R.id.b_tb_up).setBackground(toolBarDrawable);
							if (android.os.Build.VERSION.SDK_INT >= 21) {
								getWindow().setStatusBarColor(colorDark);
							}
							uiLayout = 0;
							break;
					}
					mPrefs.edit().putInt("UI_Layout", uiLayout).apply();
				}
			}
		});
		// Register listener for up & down swipes in the main layout
/*		new SwipeDetector(mDrawerLayout).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
			@Override
			public void SwipeEventDetected(SwipeDetector.SwipeTypeEnum swipeType) {
				if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT ||
						swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
					return;
				}
				ImageButton b_nav;
				if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM &&
						uiLayout == 0 && !isContinuous && plotValues == 24) {
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Next day view");
					if (dayToShow > 1) {
						dayToShow--;
						Utils.clearCharts();
						Utils.initCharts(false, true, dayToShow, appActivity);
						tempLevelsPlot.redraw();
						pressLevelsPlot.redraw();
						humidLevelsPlot.redraw();
						if (dayToShow == 1) {
							b_nav = (ImageButton) findViewById(R.id.b_next);
							b_nav.setVisibility(View.GONE);
						}
						if (numOfDayRecords != 1) {
							b_nav = (ImageButton) findViewById(R.id.b_last);
							b_nav.setVisibility(View.VISIBLE);
						}
					}
				} else if (swipeType == SwipeDetector.SwipeTypeEnum.BOTTOM_TO_TOP &&
						uiLayout == 0 && !isContinuous && plotValues == 24) {
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Previous day view");
					//numOfDayRecords = DataHolder.sizeOfDayEntry();
					if (dayToShow < numOfDayRecords) {
						dayToShow++;
						Utils.clearCharts();
						Utils.initCharts(false, true, dayToShow, appActivity);
						tempLevelsPlot.redraw();
						pressLevelsPlot.redraw();
						humidLevelsPlot.redraw();
						if (dayToShow != 1) {
							b_nav = (ImageButton) findViewById(R.id.b_next);
							b_nav.setVisibility(View.VISIBLE);
						}
						if (dayToShow == numOfDayRecords) {
							b_nav = (ImageButton) findViewById(R.id.b_last);
							b_nav.setVisibility(View.GONE);
						}
					}
				}
			}
		});
*/
		// Prepare views of weather values
		tvCurrTempPlot = (TextView) findViewById(R.id.tvCurrTempPlot);
		tvCurrTempVintage = (TextView) findViewById(R.id.tvCurrTempVintage);
		tvCurrTempStation = (TextView) findViewById(R.id.tvCurrTempStation);
		/* Current big gauge value viewed */
		tvCurrPressPlot = (TextView) findViewById(R.id.tvCurrPressPlot);
		tvCurrPressVintage = (TextView) findViewById(R.id.tvCurrPressVintage);
		tvCurrPressStation = (TextView) findViewById(R.id.tvCurrPressStation);
		tvCurrHumidPlot = (TextView) findViewById(R.id.tvCurrHumidPlot);
		tvCurrHumidVintage = (TextView) findViewById(R.id.tvCurrHumidVintage);
		tvCurrHumidStation = (TextView) findViewById(R.id.tvCurrHumidStation);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvTodayMaxTemp = (TextView) findViewById(R.id.tvTodayMaxTemp);
		tvTodayMinTemp = (TextView) findViewById(R.id.tvTodayMinTemp);
		tvTodayMaxPress = (TextView) findViewById(R.id.tvTodayMaxPress);
		tvTodayMinPress = (TextView) findViewById(R.id.tvTodayMinPress);
		tvTodayMaxHumid = (TextView) findViewById(R.id.tvTodayMaxHumid);
		tvTodayMinHumid = (TextView) findViewById(R.id.tvTodayMinHumid);

		/** Typeface for this apps font */
		Typeface type = Typeface.createFromAsset(getAssets(), "LiquidCrystal-Normal.otf");
		tvCurrTempStation.setTypeface(type);
		tvCurrPressStation.setTypeface(type);
		tvCurrHumidStation.setTypeface(type);
		tvTime.setTypeface(type);
		tvDate.setTypeface(type);
		tvTodayMaxTemp.setTypeface(type);
		tvTodayMinTemp.setTypeface(type);
		tvTodayMaxPress.setTypeface(type);
		tvTodayMinPress.setTypeface(type);
		tvTodayMaxHumid.setTypeface(type);
		tvTodayMinHumid.setTypeface(type);

		tvCurrTempPlot.setText(getString(R.string.waitForSensor));
		tvCurrTempVintage.setText(getString(R.string.waitForSensor));
		tvCurrTempStation.setText(getString(R.string.waitForSensor));
		tvCurrPressPlot.setText(getString(R.string.waitForSensor));
		tvCurrPressVintage.setText(getString(R.string.waitForSensor));
		tvCurrPressStation.setText(getString(R.string.waitForSensor));
		tvCurrHumidPlot.setText(getString(R.string.waitForSensor));
		tvCurrHumidPlot.setText(getString(R.string.waitForSensor));
		tvCurrHumidStation.setText(getString(R.string.waitForSensor));

		// set pointers to the gauges
		gvThermo=(GaugeView)findViewById(R.id.thermometer);
		gvBaro=(GaugeView)findViewById(R.id.barometer);
		gvHygro =(GaugeView)findViewById(R.id.hygrometer);
		gvBig = gvThermo;

		// connect to temperature sensor
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

		// connect to temperature sensor
		mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		// connect to air pressure sensor
		mPressSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		// connect to humidity sensor
		mHumidSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

		if (mTempSensor == null) {
			tvCurrTempPlot.setText(getString(R.string.sensorNotAvail));
			tvCurrTempVintage.setText(getString(R.string.sensorNotAvail));
			tvCurrTempStation.setText(getString(R.string.sensorNotAvail));
			findViewById(R.id.trTemp).setVisibility(View.GONE);
			findViewById(R.id.xyTempPlot).setVisibility(View.GONE);
		}
		if (mPressSensor == null) {
			tvCurrPressPlot.setText(getString(R.string.sensorNotAvail));
			tvCurrPressVintage.setText(getString(R.string.sensorNotAvail));
			tvCurrPressStation.setText(getString(R.string.sensorNotAvail));
			findViewById(R.id.trPress).setVisibility(View.GONE);
			findViewById(R.id.xyPressPlot).setVisibility(View.GONE);
		}
		if (mHumidSensor == null) {
			tvCurrHumidPlot.setText(getString(R.string.sensorNotAvail));
			tvCurrHumidVintage.setText(getString(R.string.sensorNotAvail));
			tvCurrHumidStation.setText(getString(R.string.sensorNotAvail));
			findViewById(R.id.trHumid).setVisibility(View.GONE);
			findViewById(R.id.xyHumidPlot).setVisibility(View.GONE);
		}

		// calculate some sizes from dp/sp to float for AndroidPlot
		//displayMetrics = getResources().getDisplayMetrics();
		//textSizeSmall = displayMetrics.density*14f;
		textSizeSmall = PixelUtils.spToPix(14);
		//textSizeMedium = displayMetrics.density*18f;
		textSizeMedium = PixelUtils.spToPix(18);
		//margin10dp = displayMetrics.density*10f;
		margin10dp = PixelUtils.dpToPix(10);
		//margin20dp = displayMetrics.density*20f;
		margin20dp = PixelUtils.dpToPix(20);
		//margin25dp = displayMetrics.density*25f;
		margin25dp = PixelUtils.dpToPix(25);
		//margin30dp = displayMetrics.density*30f;
		margin30dp = PixelUtils.dpToPix(30);
		//margin40dp = displayMetrics.density*40f;
		margin40dp = PixelUtils.dpToPix(40);

        appContext = this;
		appActivity = this;
		uiLayout = mPrefs.getInt("UI_Layout",2);
		/** Layout for modern view */
		LinearLayout modernLayout = (LinearLayout)findViewById(R.id.modern);
		/** Layout for vintage view */
		RelativeLayout vintageLayout = (RelativeLayout)findViewById(R.id.vintage);
		/** Layout for station view */
		RelativeLayout stationLayout = (RelativeLayout)findViewById(R.id.station);
		switch (uiLayout) {
			case 0: // Plot view
				vintageLayout.setVisibility(View.GONE);
				stationLayout.setVisibility(View.GONE);
				modernLayout.setVisibility(View.VISIBLE);
				findViewById(R.id.b_infinite).setVisibility(View.VISIBLE);
				findViewById(R.id.b_day_view).setVisibility(View.VISIBLE);
				findViewById(R.id.b_month_view).setVisibility(View.VISIBLE);
				break;
			case 1: // Vintage view
				modernLayout.setVisibility(View.GONE);
				stationLayout.setVisibility(View.GONE);
				vintageLayout.setVisibility(View.VISIBLE);
				findViewById(R.id.b_infinite).setVisibility(View.GONE);
				findViewById(R.id.b_day_view).setVisibility(View.GONE);
				findViewById(R.id.b_month_view).setVisibility(View.GONE);
				getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gold_brown));
				findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gold_brown));
				if (android.os.Build.VERSION.SDK_INT >= 21) {
					getWindow().setStatusBarColor(colorStatusVintage);
				}
				break;
			case 2: // Station view
				modernLayout.setVisibility(View.GONE);
				vintageLayout.setVisibility(View.GONE);
				stationLayout.setVisibility(View.VISIBLE);
				findViewById(R.id.b_infinite).setVisibility(View.GONE);
				findViewById(R.id.b_day_view).setVisibility(View.GONE);
				findViewById(R.id.b_month_view).setVisibility(View.GONE);
				getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.my_gray_bright));
				findViewById(R.id.b_tb_up).setBackground(getResources().getDrawable(R.color.my_gray_bright));
				if (android.os.Build.VERSION.SDK_INT >= 21) {
					getWindow().setStatusBarColor(colorStatusStation);
				}
				break;
		}

        path = Environment.getExternalStorageDirectory() + "/" + "WeatherStation/";

		tempUnit = mPrefs.getInt("temp_unit", 0);
		pressUnit = mPrefs.getInt("press_unit", 0);
		lastTemp = lastPress = lastHumid = 0f;

		Utils.initCharts(true, true, 1, this); // continuous, day view, view today

		/** Intent of background service */
		Intent alarmIntent = new Intent(this, BGService.class);
		/** Pending intent of background service */
		PendingIntent pendingStartIntent = PendingIntent.getService(this, 1700, alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		/** AlarmManager for repeated call of background service */
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				3600000, pendingStartIntent);

		/** Access to shared preferences of app widget */
		SharedPreferences wPrefs = getSharedPreferences("WidgetValues",0);
		if (wPrefs.getInt("wNums",0) != 0) {
			/** IntentFilter to receive Screen on/off broadcast msgs */
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			BroadcastReceiver mReceiver = new ScreenReceiver();
			registerReceiver(mReceiver, filter);
		}

		/** View for Google Adsense ads */
		AdView mAdView = (AdView) findViewById(R.id.adView);
		/** Request for ad from Google */
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onPostCreate called");
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTempSensor != null) {
			mSensorManager.registerListener(this, mTempSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if (mPressSensor != null) {
			mSensorManager.registerListener(this, mPressSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if (mHumidSensor != null) {
			mSensorManager.registerListener(this, mHumidSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}

		/** Handler to update UI with an delay of <code>autoUpdate</code> seconds */
		int[] intUpdateRate = getResources().getIntArray(R.array.intUpdateRate);
		autoUpdateIndex = mPrefs.getInt("UpdateRate", autoUpdateIndex);
		autoUpdate = intUpdateRate[autoUpdateIndex];
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						plotRefresh();
					}
				});
			}
		}, autoUpdate);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mTempSensor != null) {
			mSensorManager.unregisterListener(this, mTempSensor);
		}
		if (mPressSensor != null) {
			mSensorManager.unregisterListener(this, mPressSensor);
		}
		if (mHumidSensor != null) {
			mSensorManager.unregisterListener(this, mHumidSensor);
		}
		WidgetValues.forceUpdate(this,
				lastTemp,
				lastPress,
				lastHumid);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onConfiguration called");
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "OnClick id = " + v.getId());
		final ScrollView vDrawer = (ScrollView) this.findViewById(R.id.v_drawer);
		switch (v.getId()) {
			case R.id.thermometer:
				if (gvBig != gvThermo) {
					Utils.reOrderGauge(this, R.id.tvCurrPressVintage, R.id.thermometer,
							gvThermo, gvBaro, gvHygro,
							tvCurrTempVintage, tvCurrPressVintage, tvCurrHumidVintage);
					gvBig = gvThermo;
				}
				break;
			case R.id.barometer:
				if (gvBig != gvBaro) {
					Utils.reOrderGauge(this, R.id.tvCurrTempVintage, R.id.barometer,
							gvBaro, gvThermo, gvHygro,
							tvCurrPressVintage, tvCurrTempVintage, tvCurrHumidVintage);
					gvBig = gvBaro;
				}
				break;
			case R.id.hygrometer:
				if (gvBig != gvHygro) {
					Utils.reOrderGauge(this, R.id.tvCurrPressVintage, R.id.hygrometer,
							gvHygro, gvBaro, gvThermo,
							tvCurrHumidVintage, tvCurrPressVintage, tvCurrTempVintage);
					gvBig = gvHygro;
				}
				break;
			case R.id.b_tb_up:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Finish");
				finish();
				break;
			case R.id.b_infinite:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Infinite view");
				isContinuous = true;
				plotValues = 20;
                Utils.clearCharts();
				Utils.initCharts(true, true, 1, this);
				tempLevelsPlot.redraw();
				pressLevelsPlot.redraw();
				humidLevelsPlot.redraw();
				/** Image button to jump to previous or next day */
				ImageButton b_nav = (ImageButton) findViewById(R.id.b_next);
				b_nav.setVisibility(View.GONE);
				b_nav = (ImageButton) findViewById(R.id.b_last);
				b_nav.setVisibility(View.GONE);
				break;
			case R.id.b_day_view:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Day view");
				isContinuous = false;
				plotValues = 24;
				dayToShow = 1;
				Utils.clearCharts();
				Utils.initCharts(false, true, 1, this);
				tempLevelsPlot.redraw();
				pressLevelsPlot.redraw();
				humidLevelsPlot.redraw();
				if (numOfDayRecords != 1) {
					b_nav = (ImageButton) findViewById(R.id.b_last);
					b_nav.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.b_month_view:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Month view");
				isContinuous = false;
				plotValues = 31;
				Utils.clearCharts();
				Utils.initCharts(false, false, 1, this);
				tempLevelsPlot.redraw();
				pressLevelsPlot.redraw();
				humidLevelsPlot.redraw();
				b_nav = (ImageButton) findViewById(R.id.b_next);
				b_nav.setVisibility(View.GONE);
				b_nav = (ImageButton) findViewById(R.id.b_last);
				b_nav.setVisibility(View.GONE);
				break;
			case R.id.b_next:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Next day view");
				if (dayToShow > 1) {
					dayToShow--;
					Utils.clearCharts();
					Utils.initCharts(false, true, dayToShow, this);
					tempLevelsPlot.redraw();
					pressLevelsPlot.redraw();
					humidLevelsPlot.redraw();
					if (dayToShow == 1) {
						b_nav = (ImageButton) findViewById(R.id.b_next);
						b_nav.setVisibility(View.GONE);
					}
					if (numOfDayRecords != 1) {
						b_nav = (ImageButton) findViewById(R.id.b_last);
						b_nav.setVisibility(View.VISIBLE);
					}
				}
				break;
			case R.id.b_last:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Previous day view");
				//numOfDayRecords = DataHolder.sizeOfDayEntry();
				if (dayToShow < numOfDayRecords) {
					dayToShow++;
					Utils.clearCharts();
					Utils.initCharts(false, true, dayToShow, this);
					tempLevelsPlot.redraw();
					pressLevelsPlot.redraw();
					humidLevelsPlot.redraw();
					if (dayToShow != 1) {
						b_nav = (ImageButton) findViewById(R.id.b_next);
						b_nav.setVisibility(View.VISIBLE);
					}
					if (dayToShow == numOfDayRecords) {
						b_nav = (ImageButton) findViewById(R.id.b_last);
						b_nav.setVisibility(View.GONE);
					}
				}
				break;
			case R.id.sb_export:
				if (!WSDatabaseHelper.exportDatabase()) {
					Utils.myAlert(this, getString(R.string.errorExportTitle), getString(R.string.errorExport));
				} else {
					Utils.myAlert(this, getString(R.string.succExportTitle), getString(R.string.succExport, exportFilePath));
				}
				findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
				findViewById(R.id.sb_info_group).setVisibility(View.GONE);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.sb_backup:
				if (!WSDatabaseHelper.backupDBasJSON()) {
					Utils.myAlert(this, getString(R.string.errorBackupTitle), getString(R.string.errorBackup));
				} else {
					Utils.myAlert(this, getString(R.string.succBackupTitle), getString(R.string.succBackup, exportFilePath));
				}
				findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
				findViewById(R.id.sb_info_group).setVisibility(View.GONE);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.sb_restore:
				/** List view to add onItemClickListener */
				ListView lvFileList = Utils.restoreFileDialog();
				if (lvFileList != null) {
					lvFileList.setOnItemClickListener(this);
				}
				findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
				findViewById(R.id.sb_info_group).setVisibility(View.GONE);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.sb_clear:
				/** Builder for alert dialog */
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				// set title
				alertDialogBuilder.setTitle(getString(R.string.clearTitle));
				// set dialog message
				alertDialogBuilder
						.setMessage(getString(R.string.clearText))
						.setCancelable(false)
						.setPositiveButton(this.getResources().getString(android.R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										/** Instance of weather db helper */
										wsDbHelper = new WSDatabaseHelper(appContext);
										dataBase = wsDbHelper.getReadableDatabase();
										wsDbHelper.cleanDB(dataBase);
										dataBase.close();
										wsDbHelper.close();
										dialog.cancel();
									}
								})
						.setNegativeButton(this.getResources().getString(android.R.string.cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				// create alert dialog
				/** Alert dialog to be shown */
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
				findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
				findViewById(R.id.sb_info_group).setVisibility(View.GONE);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.sb_info:
				findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
				findViewById(R.id.sb_info_group).setVisibility(View.VISIBLE);
				vDrawer.post(new Runnable() {
					@Override
					public void run() {
						vDrawer.fullScroll(ScrollView.FOCUS_UP);
					}
				});
				break;
			case R.id.sb_info_temp:
				if (mTempSensor != null) {
					Utils.sensorInfoDialog(0);
				} else {
					Utils.myAlert(appContext, getString(R.string.tvSensorTypeTemp),
							getString(R.string.sensorNotAvail));
				}
				break;
			case R.id.sb_info_pressure:
				if (mPressSensor != null) {
					Utils.sensorInfoDialog(1);
				} else {
					Utils.myAlert(appContext, getString(R.string.tvSensorTypePressure),
							getString(R.string.sensorNotAvail));
				}
				break;
			case R.id.sb_info_humidity:
				if (mHumidSensor != null) {
					Utils.sensorInfoDialog(2);
				} else {
					Utils.myAlert(appContext, getString(R.string.tvSensorTypeHumidity),
							getString(R.string.sensorNotAvail));
				}
				break;
			case R.id.sb_settings:
				findViewById(R.id.sb_settings_group).setVisibility(View.VISIBLE);
				findViewById(R.id.sb_info_group).setVisibility(View.GONE);
				vDrawer.post(new Runnable() {
					@Override
					public void run() {
						vDrawer.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				break;
			case R.id.sb_temp:
				/** Builder for theme selection dialog */
				AlertDialog.Builder selTempUnitBuilder = new AlertDialog.Builder(this);
				/** Inflater for theme selection dialog */
				LayoutInflater selTempUnitInflater = getLayoutInflater();
				/** View for theme selection dialog */
				View selTempUnitView = selTempUnitInflater.inflate(R.layout.settings_temp_units, null);
				selTempUnitBuilder.setView(selTempUnitView);
				/** Pointer to theme selection dialog */
				AlertDialog selTempUnit = selTempUnitBuilder.create();
				selTempUnit.setTitle(getString(R.string.sbTempUnits));

				selTempUnit.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								mPrefs.edit().putInt("temp_unit", selectTempUnit).apply();
								tempUnit = selectTempUnit;
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								mDrawerLayout.closeDrawers();
								Utils.clearCharts();
								Utils.initCharts(true, true, 1, appActivity); // continuous, day view, view today
								dialog.dismiss();
							}
						});

				selTempUnit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								mDrawerLayout.closeDrawers();
								dialog.dismiss();
							}
						});

				selTempUnit.show();
				/** Radio group with radio buttons for temperature unit selection */
				RadioGroup rgTempUnit = (RadioGroup) selTempUnitView.findViewById(R.id.rg_temp);
				rgTempUnit.clearCheck();
				rgTempUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.rb_centigrade:
								selectTempUnit = 0;
								break;
							case R.id.rb_fahrenheit:
								selectTempUnit = 1;
								break;
							case R.id.rb_kelvin:
								selectTempUnit = 2;
								break;
						}
					}
				});
				/** Holds user selected theme */
				selectTempUnit = tempUnit = mPrefs.getInt("temp_unit", 0);
				/** Pointer to preselected radio button */
				RadioButton rbTempUnit;
				switch (tempUnit) {
					case 0:
						rbTempUnit = (RadioButton) rgTempUnit.findViewById(R.id.rb_centigrade);
						rbTempUnit.setChecked(true);
						break;
					case 1:
						rbTempUnit = (RadioButton) rgTempUnit.findViewById(R.id.rb_fahrenheit);
						rbTempUnit.setChecked(true);
						break;
					case 2:
						rbTempUnit = (RadioButton) rgTempUnit.findViewById(R.id.rb_kelvin);
						rbTempUnit.setChecked(true);
						break;
				}
				break;
			case R.id.sb_pressure:
				/** Builder for theme selection dialog */
				AlertDialog.Builder selPressUnitBuilder = new AlertDialog.Builder(this);
				/** Inflater for theme selection dialog */
				LayoutInflater selPressUnitInflater = getLayoutInflater();
				/** View for theme selection dialog */
				View selPressUnitView = selPressUnitInflater.inflate(R.layout.settings_press_units, null);
				selPressUnitBuilder.setView(selPressUnitView);
				/** Pointer to theme selection dialog */
				AlertDialog selPressUnit = selPressUnitBuilder.create();
				selPressUnit.setTitle(getString(R.string.sbPressUnits));

				selPressUnit.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								mPrefs.edit().putInt("press_unit", selectPressUnit).apply();
								pressUnit = selectPressUnit;
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								mDrawerLayout.closeDrawers();
								Utils.clearCharts();
								Utils.initCharts(true, true, 1, appActivity); // continuous, day view, view today
								dialog.dismiss();
							}
						});

				selPressUnit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								mDrawerLayout.closeDrawers();
								dialog.dismiss();
							}
						});

				selPressUnit.show();
				/** Radio group with radio buttons for pressure unit selection */
				rgPressUnit1 = (RadioGroup) selPressUnitView.findViewById(R.id.rg_press1);
				rgPressUnit2 = (RadioGroup) selPressUnitView.findViewById(R.id.rg_press2);
				rgPressUnit1.clearCheck();
				rgPressUnit2.clearCheck();

				/** Radio group listener for column 1 of an double column radio group */
				PressUnit1 = new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId != -1) {
							rgPressUnit2.setOnCheckedChangeListener(null);
							rgPressUnit2.clearCheck();
							rgPressUnit2.setOnCheckedChangeListener(PressUnit2);
						}
						switch (checkedId) {
							case R.id.rb_mBar:
								selectPressUnit = 0;
								break;
							case R.id.rb_psi:
								selectPressUnit = 1;
								break;
							case R.id.rb_atm:
								selectPressUnit = 2;
								break;
							case R.id.rb_Torr:
								selectPressUnit = 3;
								break;
						}
					}
				};

				/** Radio group listener for column 1 of an double column radio group  */
				PressUnit2 = new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId != -1) {
							rgPressUnit1.setOnCheckedChangeListener(null);
							rgPressUnit1.clearCheck();
							rgPressUnit1.setOnCheckedChangeListener(PressUnit1);
						}
						switch (checkedId) {
							case R.id.rb_kPa:
								selectPressUnit = 4;
								break;
							case R.id.rb_hPa:
								selectPressUnit = 5;
								break;
							case R.id.rb_mmHg:
								selectPressUnit = 6;
								break;
							case R.id.rb_inHg:
								selectPressUnit = 7;
								break;
						}
					}
				};

				rgPressUnit1.setOnCheckedChangeListener(PressUnit1);
				rgPressUnit2.setOnCheckedChangeListener(PressUnit2);
				/** Holds user selected theme */
				selectPressUnit = pressUnit = mPrefs.getInt("press_unit", 0);
				/** Pointer to preselected radio button */
				RadioButton rbPressUnit;
				switch (pressUnit) {
					case 0:
						rbPressUnit = (RadioButton) rgPressUnit1.findViewById(R.id.rb_mBar);
						rbPressUnit.setChecked(true);
						break;
					case 1:
						rbPressUnit = (RadioButton) rgPressUnit1.findViewById(R.id.rb_psi);
						rbPressUnit.setChecked(true);
						break;
					case 2:
						rbPressUnit = (RadioButton) rgPressUnit1.findViewById(R.id.rb_atm);
						rbPressUnit.setChecked(true);
						break;
					case 3:
						rbPressUnit = (RadioButton) rgPressUnit1.findViewById(R.id.rb_Torr);
						rbPressUnit.setChecked(true);
						break;
					case 4:
						rbPressUnit = (RadioButton) rgPressUnit2.findViewById(R.id.rb_kPa);
						rbPressUnit.setChecked(true);
						break;
					case 5:
						rbPressUnit = (RadioButton) rgPressUnit2.findViewById(R.id.rb_hPa);
						rbPressUnit.setChecked(true);
						break;
					case 6:
						rbPressUnit = (RadioButton) rgPressUnit2.findViewById(R.id.rb_mmHg);
						rbPressUnit.setChecked(true);
						break;
					case 7:
						rbPressUnit = (RadioButton) rgPressUnit2.findViewById(R.id.rb_inHg);
						rbPressUnit.setChecked(true);
						break;
				}
				mDrawerLayout.closeDrawers();
				break;
			case R.id.sb_update:
				/** Builder for update rates selection dialog */
				AlertDialog.Builder selUpdateBuilder = new AlertDialog.Builder(this);
				/** Inflater for update rates selection dialog */
				LayoutInflater selUpdateInflater = getLayoutInflater();
				/** View for update rates selection dialog */
				View selUpdateView = selUpdateInflater.inflate(R.layout.screen_update_rate, null);
				selUpdateBuilder.setView(selUpdateView);
				/** Pointer to update rates selection dialog */
				AlertDialog selUpdate = selUpdateBuilder.create();
				selUpdate.setTitle(getString(R.string.sbScreenUpdate));

				selUpdate.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								int[] intUpdateRate = getResources().getIntArray(R.array.intUpdateRate);
								mPrefs.edit().putInt("UpdateRate", autoUpdateIndex).apply();
								autoUpdate = intUpdateRate[autoUpdateIndex];
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								dialog.dismiss();
							}
						});

				selUpdate.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
								dialog.dismiss();
							}
						});

				selUpdate.show();

				// Initialize the NumberPicker with selectable update rates
				NumberPicker.OnValueChangeListener onIntervalChanged
						= new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(
							NumberPicker picker,
							int oldVal,
							int newVal) {
						autoUpdateIndex = newVal;
					}
				};

				// Get list with available update rates
				/** Array of available update rates */
				String[] updateRateArray = getResources().getStringArray(R.array.updateRate);
				/** pointer to NumberPicker for update rates list */
				NumberPicker np_update =
						(NumberPicker) selUpdateView.findViewById(R.id.np_update);
				np_update.setSaveFromParentEnabled(false);
				np_update.setSaveEnabled(true);
				np_update.setMaxValue(updateRateArray.length - 1);
				np_update.setMinValue(0);
				np_update.setDisplayedValues(updateRateArray);
				np_update.setOnValueChangedListener(onIntervalChanged);
				np_update.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				autoUpdateIndex = 0;
				autoUpdateIndex = mPrefs.getInt("UpdateRate", autoUpdateIndex);
				np_update.setValue(autoUpdateIndex);
				break;
			case R.id.sb_theme:
                /** Builder for theme selection dialog */
                AlertDialog.Builder selThemeBuilder = new AlertDialog.Builder(this);
                /** Inflater for theme selection dialog */
                LayoutInflater selThemeInflater = getLayoutInflater();
                /** View for theme selection dialog */
                View selThemeView = selThemeInflater.inflate(R.layout.settings_theme, null);
                selThemeBuilder.setView(selThemeView);
                /** Pointer to theme selection dialog */
                AlertDialog selTheme = selThemeBuilder.create();
                selTheme.setTitle(getString(R.string.sbThemes));

                selTheme.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mPrefs.edit().putInt("dark_theme", selectColor).apply();
	                            findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
                                dialog.dismiss();
                                finish();
                                /** Intent to restart this app */
                                Intent intent = new Intent(appContext, WeatherStation.class);
                                startActivity(intent);
                            }
                        });

                selTheme.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
	                            findViewById(R.id.sb_settings_group).setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        });

                selTheme.show();
                /** Radio group with radio buttons for theme selection */
                RadioGroup rgTheme = (RadioGroup) selThemeView.findViewById(R.id.rg_theme);
                rgTheme.clearCheck();
                rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        Log.d("chk", "id" + checkedId);

                        switch (checkedId) {
                            case R.id.rb_blue:
                                selectColor = 0;
                                break;
                            case R.id.rb_green:
                                selectColor = 1;
                                break;
                            case R.id.rb_gray:
                                selectColor = 2;
                                break;
                            case R.id.rb_orange:
                                selectColor = 3;
                                break;
                            case R.id.rb_red:
                                selectColor = 4;
                                break;
                            case R.id.rb_white:
                                selectColor = 5;
                                break;
                        }
                    }

                });
                /** Holds user selected theme */
                themeColor = mPrefs.getInt("dark_theme", 0);
                selectColor = themeColor;
                /** Pointer to preselected radio button */
                RadioButton rbTheme;
                switch (themeColor) {
                    case 0:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_blue);
                        rbTheme.setChecked(true);
                        break;
                    case 1:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_green);
                        rbTheme.setChecked(true);
                        break;
                    case 2:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_gray);
                        rbTheme.setChecked(true);
                        break;
                    case 3:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_orange);
                        rbTheme.setChecked(true);
                        break;
                    case 4:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_red);
                        rbTheme.setChecked(true);
                        break;
                    case 5:
                        rbTheme = (RadioButton) rgTheme.findViewById(R.id.rb_white);
                        rbTheme.setChecked(true);
                        break;
                }
				mDrawerLayout.closeDrawers();
                break;
		}
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        restoreFilePath = path + files.get(position);
        isFileSelected = true;
    }

	/**
	 * Listen to weather sensor accuracy changes
	 *
	 * @param sensor
	 *            Sensor sensor.
	 * @param accuracy
	 *            int accuracy.
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
				lastTempValue = event.values[0];
				gvThermo.setTargetValue(Utils.cToU(event.values[0], 1));
				break;
			case Sensor.TYPE_PRESSURE:
				lastPressValue = event.values[0];
				lastPressValue2 = Utils.pToU(event.values[0], 0);
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				lastHumidValue = event.values[0];
				gvBaro.setTargetValue(lastPressValue2);
				gvHygro.setTargetValue(lastHumidValue);
				break;
		}
		if (lastTempValue != -9999 && lastPressValue != -9999 && lastHumidValue != -9999 && isUpdateRequest)
		{
			// Get min and max values of today
			Utils.getTodayMinMax();
			// Update time, location and date
			Utils.updateStationView();

			/** String for tendency */
			String tendency = getString(R.string.straightTendency);

			if (lastTemp != 0f) {
				if (Math.round(lastTempValue) > Math.round(lastTemp)) {
					tendency = getString(R.string.upTendency);
				} else if (Math.round(lastTempValue) < Math.round(lastTemp)) {
					tendency = getString(R.string.downTendency);
				}
			}
			lastTemp = lastTempValue;
			lastTempValue = Utils.cToU(lastTempValue, tempUnit);
			// update temperature plot
			tvCurrTempPlot.setText(String.format("%.01f",
					lastTempValue) + Utils.tempUnit(appContext, tempUnit) + tendency);
			tvCurrTempVintage.setText(String.format("%.01f",
					lastTempValue) + Utils.tempUnit(appContext, tempUnit) + tendency);
			tvCurrTempStation.setText(String.format("%.01f",
					lastTempValue) + Utils.tempUnit(appContext, tempUnit) + tendency);
			tvTodayMaxTemp.setText(String.format("%.01f",
					todayMaxTemp) + Utils.tempUnit(appContext, tempUnit));
			tvTodayMinTemp.setText(String.format("%.01f",
					todayMinTemp) + Utils.tempUnit(appContext, tempUnit));
			if (isContinuous) {
				if (tempLevelsSeries.size() < 2) {
					tempLevelsSeries.addLast(null,(Math.round(lastTempValue * 10000.0f) / 10000.0f)+0.1f);
					tempLevelsSeries.addLast(null,(Math.round(lastTempValue * 10000.0f) / 10000.0f)-0.1f);
				}
				tempLevelsSeries.addLast(null, Math.round(lastTempValue * 10000.0f) / 10000.0f);

				if (tempLevelsSeries.size() > plotValues) {
					tempLevelsSeries.removeFirst();
				}

				/** Min and Max values to calculate new top and bottom range values of temperature plot */
				float[] minMax = Utils.getMinMax(0);
				minTempValue = minMax[0];
				maxTempValue = minMax[1];
				tempLevelsPlot.setRangeBottomMax(minTempValue-0.1);
				tempLevelsPlot.setRangeBottomMin(minTempValue - 0.1);
				tempLevelsPlot.setRangeTopMax(maxTempValue + 0.1);
				tempLevelsPlot.setRangeTopMin(maxTempValue + 0.1);
				tempLevelsPlot.redraw();
			}

			// update pressure plot
			tendency = getString(R.string.straightTendency);
			if (lastPress != 0f) {
				if (Math.round(lastPressValue) > Math.round(lastPress)) {
					tendency = getString(R.string.upTendency);
				} else if (Math.round(lastPressValue) < Math.round(lastPress)) {
					tendency = getString(R.string.downTendency);
				}
			}
			lastPress = lastPressValue;
			lastPressValue = Utils.pToU(lastPressValue, pressUnit);
			/** Padding for top and bottom of the plot depending on user selected unit */
			float plotPadding = Utils.pressBoundary(pressUnit);
			tvCurrPressPlot.setText(String.format(Utils.pressFormatTitle(pressUnit),
					lastPressValue) + Utils.pressUnit(appContext, pressUnit) + tendency);
			tvCurrPressVintage.setText(String.format(Utils.pressFormatTitle(pressUnit),
					lastPressValue) + Utils.pressUnit(appContext, pressUnit) + tendency);
			tvCurrPressStation.setText(String.format(Utils.pressFormatTitle(pressUnit),
					lastPressValue) + Utils.pressUnit(appContext, pressUnit) + tendency);
			tvTodayMaxPress.setText(String.format(Utils.pressFormatTitle(pressUnit),
					todayMaxPress) + Utils.pressUnit(appContext, pressUnit));
			tvTodayMinPress.setText(String.format(Utils.pressFormatTitle(pressUnit),
					todayMinPress) + Utils.pressUnit(appContext, pressUnit));
			if (isContinuous) {
				if (pressLevelsSeries.size() < 2) {
					pressLevelsSeries.addLast(null,(Math.round(lastPressValue * 10000.0f) / 10000.0f)+plotPadding);
					pressLevelsSeries.addLast(null,(Math.round(lastPressValue * 10000.0f) / 10000.0f)-plotPadding);
				}
				pressLevelsSeries.addLast(null, Math.round(lastPressValue * 10000.0f) / 10000.0f);

				if (pressLevelsSeries.size() > plotValues) {
					pressLevelsSeries.removeFirst();
				}

				/** Min and Max values to calculate new top and bottom range values of pressure plot */
				float[] minMax = Utils.getMinMax(1);
				minPressValue = minMax[0];
				maxPressValue = minMax[1];
				pressLevelsPlot.setRangeBottomMax(minPressValue - plotPadding);
				pressLevelsPlot.setRangeBottomMin(minPressValue - plotPadding);
				pressLevelsPlot.setRangeTopMax(maxPressValue + plotPadding);
				pressLevelsPlot.setRangeTopMin(maxPressValue + plotPadding);
				pressLevelsPlot.redraw();
			}

			// update humidity plot
			if (lastHumid != 0f) {
				if (Math.round(lastHumidValue) > Math.round(lastHumid)) {
					tendency = getString(R.string.upTendency);
				} else if (Math.round(lastHumidValue) < Math.round(lastHumid)) {
					tendency = getString(R.string.downTendency);
				}
			}
			lastHumid = lastHumidValue;
			tvCurrHumidPlot.setText(String.format("%.01f",
					lastHumidValue) + getString(R.string.humidSign) + tendency);
			tvCurrHumidVintage.setText(String.format("%.01f",
					lastHumidValue) + getString(R.string.humidSign) + tendency);
			tvCurrHumidStation.setText(String.format("%.01f",
					lastHumidValue) + getString(R.string.humidSign) + tendency);
			tvTodayMaxHumid.setText(String.format("%.01f",
					todayMaxHumid) + getString(R.string.humidSign));
			tvTodayMinHumid.setText(String.format("%.01f",
					todayMinHumid) + getString(R.string.humidSign));
			if (isContinuous) {
				if (humidLevelsSeries.size() < 2) {
					humidLevelsSeries.addLast(null,(Math.round(lastHumidValue * 10000.0f) / 10000.0f)+0.1f);
					humidLevelsSeries.addLast(null,(Math.round(lastHumidValue * 10000.0f) / 10000.0f)-0.1f);
				}
				humidLevelsSeries.addLast(null, Math.round(lastHumidValue * 10000.0f) / 10000.0f);

				if (humidLevelsSeries.size() > plotValues) {
					humidLevelsSeries.removeFirst();
				}

				/** Min and Max values to calculate new top and bottom range values of humidity plot */
				float[] minMax = Utils.getMinMax(2);
				minHumidValue = minMax[0];
				maxHumidValue = minMax[1];
				humidLevelsPlot.setRangeBottomMax(minHumidValue - 0.1);
				humidLevelsPlot.setRangeBottomMin(minHumidValue - 0.1);
				humidLevelsPlot.setRangeTopMax(maxHumidValue + 0.1);
				humidLevelsPlot.setRangeTopMin(maxHumidValue + 0.1);
				humidLevelsPlot.redraw();
			}

			// reset measured values for next sampling round
			lastTempValue = lastPressValue = lastHumidValue = -9999;

			if (autoUpdate != 1000) {
				// reset update request flag
				isUpdateRequest = false;

				/** Handler to update UI with an delay of <code>autoUpdate</code> seconds */
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							public void run() {
								plotRefresh();
							}
						});
					}
				}, autoUpdate);
			}
		}
	}

	/**
	 * Enable plot update every autoUpdate ms
	 * if autoUpdate is 1000ms this is not used as the temp & humidity sensors smallest
	 * update frequency is 1000ms
	 */
	private void plotRefresh() {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "plotRefresh called");

		isUpdateRequest = true;
		lastTempValue = lastPressValue = lastHumidValue = -9999;
	}
}
