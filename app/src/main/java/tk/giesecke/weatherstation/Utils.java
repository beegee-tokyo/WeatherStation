package tk.giesecke.weatherstation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Build;
import android.os.StrictMode;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.TextOrientationType;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Utils
 * unit conversion methods
 *
 * @author Bernd Giesecke
 * @version 1.0 May 31, 2015.
 */
public class Utils extends WeatherStation implements AdapterView.OnItemClickListener {

	/**
	 * Convert temperature to user selected unit
	 *
	 * @param centiGrade
	 *            Temperature in Centigrade
	 * @param unit
	 *            User requested temperature unit
	 * @return <code>float</code>
	 *            Temperature in Centigrade, Fahrenheit or Kelvin
	 */
	public static float cToU(float centiGrade, int unit) {
		if (unit == 0) { // centigrade
			return centiGrade;
		} else if (unit == 1) { // fahrenheit
			return (float)(centiGrade*1.8)+32;
		}
		return (float)(centiGrade+273.15); // kelvin
	}

	/**
	 * Returns user selected temperature unit as string
	 *
	 * @param appContext
	 *            Application context
	 * @param unit
	 *            User requested temperature unit
	 * @return <code>string</code>
	 *            Temperature unit as string (°C or °F or K)
	 */
	public static String tempUnit(Context appContext, int unit) {
		if (unit == 0) { // centigrade
			return appContext.getString(R.string.tempSign)+" ";
		} else if (unit == 1) { // fahrenheit
			return appContext.getString(R.string.tempSignF)+" ";
		}
		return " "+appContext.getString(R.string.tempSignK)+" "; // kelvin
	}

	/**
	 * Convert mBar into user selected unit
	 *
	 * @param mBar
	 *            Pressure in mBar
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>float</code>
	 *            Temperature in Centigrade, Fahrenheit or Kelvin
	 */
	public static float pToU(float mBar, int unit) {
		switch (unit) {
			case 1: // psi
				return (float)(mBar*0.01450378911491);
			case 2: // atm
				return (float) (mBar * 0.000986923266716);
			case 3: // Torr
				return (float)(mBar*0.7500616827042);
			case 4: // kPa
				return (float) (mBar * 0.1);
			case 5: // hPa
				return mBar;
			case 6: //mmHg
				return (float)(mBar*0.7500616827042);
			case 7: // inHg
				return (float) (mBar * 0.0295301);
			default: // mBar
				return mBar;
		}
	}

	/**
	 * Returns user selected pressure unit as string
	 *
	 * @param appContext
	 *            Application context
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>string</code>
	 *            Pressure unit as string (mBar, psi, atm, Torr, kPa, hPa, mmHg or inHg)
	 */
	public static String pressUnit(Context appContext, int unit) {
		switch (unit) {
			case 1: // psi
				return " "+appContext.getString(R.string.pressSignP)+" ";
			case 2: // atm
				return " "+appContext.getString(R.string.pressSignA)+" ";
			case 3: // Torr
				return " "+appContext.getString(R.string.pressSignT)+" ";
			case 4: // kPa
				return " "+appContext.getString(R.string.pressSignK)+" ";
			case 5: // hPa
				return " "+appContext.getString(R.string.pressSignH)+" ";
			case 6: //mmHg
				return " "+appContext.getString(R.string.pressSignM)+" ";
			case 7: // inHg
				return " "+appContext.getString(R.string.pressSignI)+" ";
			default: // mBar
				return " "+appContext.getString(R.string.pressSign)+" ";
		}
	}

	/**
	 * Returns formatting factor depending on user selected pressure unit as string
	 *
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>string</code>
	 *            formatting factor as string
	 *            %.02f for mBar, hPa, mmHg
	 *            %.03f for kPa, Torr
	 *            %.06f for atm, inHg, psi
	 */
	public static String pressFormatTitle(int unit) {
		switch (unit) {
			case 1: // psi
			case 7: // inHg
				return "%.04f";
			case 2: // atm
				return "%.05f";
			case 3: // Torr
			case 4: // kPa
				return "%.03f";
			case 5: // hPa
			case 6: //mmHg
			default: // mBar
				return "%.02f";
		}
	}

	/**
	 * Returns formatting factor depending on user selected pressure unit as string
	 *
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>string</code>
	 *            formatting factor as string
	 *            #.##### for atm
	 *            ##.#### for psi, inHg
	 *            ###.### for Torr, kPa
	 *            ####.## for hPa, mmHg, mBar
	 */
	private static String pressFormatRange(int unit) {
		switch (unit) {
			case 1: // psi
			case 7: // inHg
				return "##.####";
			case 2: // atm
				return "#.#####";
			case 3: // Torr
			case 4: // kPa
				return "###.###";
			case 5: // hPa
			case 6: // mmHg
			default: // mBar
				return "####.##";
		}
	}

	/**
	 * Returns minimum digits depending on user selected pressure unit as string
	 *
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>int</code>
	 *            minimum digits as int
	 *            5 for atm
	 *            4 for psi, inHg
	 *            3 for Torr, kPa
	 *            2 for hPa, mmHg, mBar
	 */
	private static int pressFormatRangeDigit(int unit) {
		switch (unit) {
			case 1: // psi
			case 7: // inHg
				return 4;
			case 2: // atm
				return 5;
			case 3: // Torr
			case 4: // kPa
				return 3;
			case 5: // hPa
			case 6: // mmHg
			default: // mBar
				return 2;
		}
	}

	/**
	 * Returns formatting values to calculate new upper and lower padding of plot
	 *
	 * @param unit
	 *            User requested pressure unit
	 * @return <code>float</code>
	 *            padding factor as float
	 */
	public static float pressBoundary(int unit) {
		switch (unit) {
			case 1: // psi
			case 7: // inHg
				return 0.00001f;
			case 2: // atm
				return 0.000001f;
			case 3: // Torr
			case 4: // kPa
				return 0.001f;
			case 5: // hPa
			case 6: //mmHg
			default: // mBar
				return 0.01f;
		}
	}

	/**
	 * Get current date & time as string
	 *
	 * @return <code>int[]</code>
	 *            Date and time as integer values
	 *            int[0] = hour
	 *            int[1] = day
	 *            int[2] = month
	 */
	public static int[] getCurrentDate() {
		/** Integer array for return values */
		int[] currTime = new int[3];
		/** Calendar to get current time and date */
		Calendar cal = Calendar.getInstance();

		/** Today's day */
		currTime[1] = cal.get(Calendar.DATE);

		/** Current hour */
		currTime[0] = cal.get(Calendar.HOUR_OF_DAY);

		/** Today's month */
		currTime[2] = cal.get(Calendar.MONTH) + 1;
		return currTime;
	}

	/**
	 * Get min and max values of a list
	 *
	 * @param continuousSeries
	 *            0 = tempLevelsSeries
	 *            1 = pressLevelSeries
	 *            2 = humidLevelSeries
	 * @return <code>float[]</code>
	 *            float[0] = minimum value found
	 *            float[1] = maximum value found
	 */
	public static float[] getMinMax(int continuousSeries) {

		/** Min and max value of a list */
		float[] returnMinMax = new float[2];
		returnMinMax[0] = returnMinMax[1] = 0;

		/** List to hold the values we check for min and max */
		List<Float> checkedArray = new ArrayList<>();

		/** Flag if the list is empty */
		boolean isListEmpty = false;

		switch (continuousSeries) {
			case 0:
				for (int i=0; i<tempLevelsSeries.size(); i++) {
					checkedArray.add((float) tempLevelsSeries.getY(i));
				}
				break;
			case 1:
				for (int i=0; i<pressLevelsSeries.size(); i++) {
					checkedArray.add((float) pressLevelsSeries.getY(i));
				}
				break;
			case 2:
				for (int i=0; i<humidLevelsSeries.size(); i++) {
					checkedArray.add((float) humidLevelsSeries.getY(i));
				}
				break;
		}
		if (checkedArray.size() == 0) isListEmpty = true;

		if (isListEmpty) {
			return returnMinMax;
		}
		returnMinMax[0] = Collections.min(checkedArray);
		returnMinMax[1] = Collections.max(checkedArray);

		return returnMinMax;
	}

	/**
	 * Initialize charts for hourly view
	 *
	 * @param isContinuous
	 *            true -> use .useImplicitXVals(); for X values
	 *            false -> X values
	 * @param isDay
	 *            true -> get hourly records
	 *            false -> get daily records
	 * @param day
	 *            day we want to get the records from
	 *            1 = today
	 *            2 = yesterday
	 *            3 = 2 days ago
	 *            ...
	 */
	public static void initCharts(boolean isContinuous, boolean isDay, int day, Activity activity){

		/** String with the X axis name */
		String xValueLabel = appContext.getString(R.string.currCont);
		/** List to hold the timestamps */
		ArrayList<Integer> timeStamps = new ArrayList<>();
		/** List to hold the day stamps */
		ArrayList<Integer> dayStamps = new ArrayList<>();
		/** List to hold the max temperature values */
		ArrayList<Float> tempMaxEntries = new ArrayList<>();
		/** List to hold the min temperature values */
		ArrayList<Float> tempMinEntries = new ArrayList<>();
		tempMinEntries.clear();
		/** List to hold the (average) temperature values */
		ArrayList<Float> tempEntries = new ArrayList<>();
		/** List to hold the max pressure values */
		ArrayList<Float> pressMaxEntries = new ArrayList<>();
		/** List to hold the min pressure values */
		ArrayList<Float> pressMinEntries = new ArrayList<>();
		/** List to hold the (average) pressure values */
		ArrayList<Float> pressEntries = new ArrayList<>();
		/** List to hold the max humidity values */
		ArrayList<Float> humidMaxEntries = new ArrayList<>();
		/** List to hold the min humidity values */
		ArrayList<Float> humidMinEntries = new ArrayList<>();
		/** List to hold the (average) humidity values */
		ArrayList<Float> humidEntries = new ArrayList<>();

		if(!isContinuous) {

			/** Instance of weather db helper */
			wsDbHelper = new WSDatabaseHelper(appContext);
			dataBase = wsDbHelper.getReadableDatabase();
			if (isDay) {
				xValueLabel = appContext.getString(R.string.currHour);
				/** Cursor filled with existing entries of today */
				Cursor dayEntry = wsDbHelper.getDay(dataBase, day);
				dayEntry.moveToFirst();

				for (int i = 0; i<dayEntry.getCount(); i++) {
					timeStamps.add(dayEntry.getInt(0));
					dayStamps.add(dayEntry.getInt(1));
					tempEntries.add(Utils.cToU(dayEntry.getFloat(3), tempUnit));
					pressEntries.add(Utils.pToU(dayEntry.getFloat(4), pressUnit));
					humidEntries.add(dayEntry.getFloat(5));
					dayEntry.moveToNext();
				}

				numOfDayRecords = 0;
				for (int i=1; i<32; i++) {
					/** Cursor filled with existing entries of today */
					dayEntry = wsDbHelper.getDay(dataBase, i);
					dayEntry.moveToFirst();
					if (dayEntry.getCount() == 0) {
						break;
					}
					numOfDayRecords++;
				}
				dayEntry.close();

			} else {
				xValueLabel = appContext.getString(R.string.currMonth);

				numOfDayRecords = 0;
				for (int i=1; i<32; i++) {
					/** Cursor filled with existing entries of today */
					Cursor dayEntry = wsDbHelper.getDay(dataBase, i);
					dayEntry.moveToFirst();
					if (dayEntry.getCount() == 0) {
						break;
					}
					numOfDayRecords++;
					dayEntry.moveToLast();
					timeStamps.add(dayEntry.getInt(0));
					dayStamps.add(dayEntry.getInt(1));
					tempMaxEntries.add(Utils.cToU(dayEntry.getFloat(6), tempUnit));
					tempMinEntries.add(Utils.cToU(dayEntry.getFloat(7), tempUnit));
					tempEntries.add(Utils.cToU(dayEntry.getFloat(8), tempUnit));
					pressMaxEntries.add(Utils.pToU(dayEntry.getFloat(9), pressUnit));
					pressMinEntries.add(Utils.pToU(dayEntry.getFloat(10), pressUnit));
					pressEntries.add(Utils.pToU(dayEntry.getFloat(11), pressUnit));
					humidMaxEntries.add(dayEntry.getFloat(12));
					humidMinEntries.add(dayEntry.getFloat(13));
					humidEntries.add(dayEntry.getFloat(14));
					dayEntry.close();
				}
			}
			dataBase.close();
			wsDbHelper.close();
		}

		// initialize chart for temperature for non-continuous update
		if (mTempSensor != null) {
			initTempChart(isContinuous, isDay, xValueLabel,
					timeStamps, dayStamps,
					tempEntries, tempMaxEntries, tempMinEntries, activity);
		}

		// initialize chart for pressure for non-continuous update
		if (mPressSensor != null) {
			initPressChart(isContinuous, isDay, xValueLabel,
					timeStamps, dayStamps,
					pressEntries, pressMaxEntries, pressMinEntries, activity);
		}

		// initialize chart for humidity for non-continuous update
		if (mHumidSensor != null) {
			initHumidChart(isContinuous, isDay, xValueLabel,
					timeStamps, dayStamps,
					humidEntries, humidMaxEntries, humidMinEntries, activity);
		}

		// Activate the advertisements
		// Enable access to internet
		if (android.os.Build.VERSION.SDK_INT > 9) {
			/** ThreadPolicy to get permission to access internet */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	/**
	 * Clear all charts
	 */
	public static void clearCharts() {
		if (mTempSensor != null) {
			clearTempChart();
		}
		if (mPressSensor != null) {
			clearPressChart();
		}
		if (mHumidSensor != null) {
			clearHumidChart();
		}
	}

	/**
	 * Initialize temperature chart
	 *
	 * @param isContinuous
	 *            true -> use .useImplicitXVals(); for X values
	 *            false -> X values
	 * @param isDay
	 *            true -> get hourly records
	 *            false -> get daily records
	 * @param xValueTitle
	 *            title for X values as string
	 * @param timeStamps
	 *            list with time stamps of existing records
	 * @param dayStamps
	 *            list with day stamps of existing records if day view
	 * @param tempEntries
	 *            list with temperatures of existing records if day view
	 *            list with average temperatures if month view
	 * @param tempMaxEntries
	 *            unused if day view
	 *            list with max temperatures if month view
	 * @param tempMinEntries
	 *            unused if day view
	 *            list with min temperatures if month view
	 */
	private static void initTempChart(boolean isContinuous, boolean isDay, String xValueTitle,
	                                  ArrayList<Integer> timeStamps,
	                                  ArrayList<Integer> dayStamps,
	                                  ArrayList<Float> tempEntries,
	                                  ArrayList<Float> tempMaxEntries,
	                                  ArrayList<Float> tempMinEntries,
	                                  Activity activity) {
		// find the temperature levels plot in the layout
		tempLevelsPlot = (XYPlot) activity.findViewById(R.id.xyTempPlot);
		// setup and format temperature data series
		tempLevelsSeries = new SimpleXYSeries(appContext.getString(R.string.currTemp));

		minTempValue = -100f;
		maxTempValue = +100f;

		if (!isContinuous && tempEntries.size() != 0) {
			if (isDay) {
				minTempValue = Collections.min(tempEntries);
				maxTempValue = Collections.max(tempEntries);
			} else {
				minTempValue = Collections.min(tempMinEntries);
				maxTempValue = Collections.max(tempMaxEntries);
			}
		}
		tempLevelsPlot.setRangeBottomMax(minTempValue - 0.1);
		tempLevelsPlot.setRangeBottomMin(minTempValue - 0.1);
		tempLevelsPlot.setRangeTopMax(maxTempValue + 0.1);
		tempLevelsPlot.setRangeTopMin(maxTempValue + 0.1);

		/** Formatter for (average or current) temperature plot */
		LineAndPointFormatter tempSeriesFormatter;
		/** Formatter for max temperature plot */
		LineAndPointFormatter tempMaxSeriesFormatter;
		/** Formatter for min temperature plot */
		LineAndPointFormatter tempMinSeriesFormatter;

		if (isContinuous) {
			tempLevelsSeries.useImplicitXVals();
			tempSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
			tempSeriesFormatter.getLinePaint().setStrokeWidth(10);
			tempLevelsPlot.addSeries(tempLevelsSeries, tempSeriesFormatter);
		} else {
			if (isDay) {
				if (dayStamps.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.hourOfDay) +" "+ Integer.toString(dayStamps.get(0));
					for (int i=0; i<timeStamps.size(); i++) {
						tempLevelsSeries.addLast(timeStamps.get(i),tempEntries.get(i));
					}
				}
				tempSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
				tempSeriesFormatter.getLinePaint().setStrokeWidth(10);
				tempLevelsPlot.addSeries(tempLevelsSeries, tempSeriesFormatter);
			} else {
				if (tempEntries.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					for (int i=0; i<dayStamps.size(); i++) {
						tempLevelsSeries.addLast(dayStamps.get(i),tempEntries.get(i));
					}
				}
				/** Data series for the max temperature */
				SimpleXYSeries tempMaxSeries = new SimpleXYSeries(appContext.getString(R.string.currTempMax));
				/** Data series for the min temperature */
				SimpleXYSeries tempMinSeries = new SimpleXYSeries(appContext.getString(R.string.currTempMin));
				for (int i=0; i<timeStamps.size(); i++) {
					tempMaxSeries.addLast(dayStamps.get(i), tempMaxEntries.get(i));
					tempMinSeries.addLast(dayStamps.get(i), tempMinEntries.get(i));
				}
				tempSeriesFormatter = new LineAndPointFormatter(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, null);
				tempSeriesFormatter.getLinePaint().setStrokeWidth(10);
				tempLevelsPlot.addSeries(tempLevelsSeries, tempSeriesFormatter);
				tempMaxSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
				tempMaxSeriesFormatter.getLinePaint().setStrokeWidth(10);
				tempLevelsPlot.addSeries(tempMaxSeries, tempMaxSeriesFormatter);
				tempMinSeriesFormatter = new LineAndPointFormatter(Color.GREEN, Color.TRANSPARENT, Color.TRANSPARENT, null);
				tempMinSeriesFormatter.getLinePaint().setStrokeWidth(10);
				tempLevelsPlot.addSeries(tempMinSeries, tempMinSeriesFormatter);
			}
		}

		tempLevelsPlot.setTicksPerRangeLabel(3);
		tempLevelsPlot.getLayoutManager()
				.remove(tempLevelsPlot.getLegendWidget());
		tempLevelsPlot.getDomainLabelWidget().pack();
		tempLevelsPlot.setDomainValueFormat(new DecimalFormat("#"));
		tempLevelsPlot.getGraphWidget().setMarginBottom(margin30dp);
		tempLevelsPlot.getGraphWidget().setMarginTop(margin10dp);
		tempLevelsPlot.getGraphWidget().setMarginLeft(margin25dp);
		tempLevelsPlot.getGraphWidget().setMarginRight(margin10dp);
		tempLevelsPlot.getGraphWidget().getDomainLabelPaint().setTextSize(textSizeSmall);
		tempLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		tempLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		tempLevelsPlot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
				0, YLayoutStyle.RELATIVE_TO_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
		tempLevelsPlot.setDomainLabel(xValueTitle);

		tempLevelsPlot.getRangeLabelWidget().getLabelPaint().setTextSize(textSizeMedium);
		tempLevelsPlot.getGraphWidget().getRangeLabelPaint().setTextSize(textSizeSmall);
		tempLevelsPlot.setRangeLabel(Utils.tempUnit(appContext, tempUnit));
		tempLevelsPlot.getRangeLabelWidget().position(0.04f, XLayoutStyle.RELATIVE_TO_LEFT,
				-0.1f, YLayoutStyle.RELATIVE_TO_BOTTOM,
				AnchorPosition.LEFT_BOTTOM);
		tempLevelsPlot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);
		tempLevelsPlot.getRangeLabelWidget().pack();

		// Change border color
		tempLevelsPlot.getBackgroundPaint().setColor(colorDark);
		// Change grid background color
		tempLevelsPlot.getGraphWidget().getGridBackgroundPaint().setColor(colorBright);
		// Change grid color
		tempLevelsPlot.getGraphWidget().getRangeGridLinePaint().setColor
				(Color.TRANSPARENT);
		tempLevelsPlot.getGraphWidget().getRangeSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		tempLevelsPlot.getGraphWidget().getDomainGridLinePaint().setColor
				(Color.TRANSPARENT);
		tempLevelsPlot.getGraphWidget().getDomainSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		// Change plot background color
		tempLevelsPlot.getGraphWidget().getBackgroundPaint().setColor(colorDark);
		// Change axis values text color and increment value
		if (isContinuous) {
			tempLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
		} else {
			tempLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
			tempLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
		}
		tempLevelsPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);

		tempLevelsPlot.redraw();
	}

	/**
	 * Clean up temperature chart for a new initialization
	 */
	private static void clearTempChart() {
		if (tempLevelsSeries.size() != 0) {
			for (int i=0; i<=tempLevelsSeries.size(); i++) {
				tempLevelsSeries.removeFirst();
			}
		}
		tempLevelsPlot.removeSeries(tempLevelsSeries);
		tempLevelsPlot.removeMarkers();
		tempLevelsPlot.clear();
	}

	/**
	 * Initialize pressure chart
	 *
	 * @param isContinuous
	 *            true -> use .useImplicitXVals(); for X values
	 *            false -> X values
	 * @param isDay
	 *            true -> get hourly records
	 *            false -> get daily records
	 * @param xValueTitle
	 *            title for X values as string
	 * @param timeStamps
	 *            list with time stamps of existing records
	 * @param dayStamps
	 *            list with day stamps of existing records if day view
	 * @param pressEntries
	 *            list with pressures of existing records if day view
	 *            list with average pressures if month view
	 * @param pressMaxEntries
	 *            unused if day view
	 *            list with max pressures if month view
	 * @param pressMinEntries
	 *            unused if day view
	 *            list with min pressures if month view
	 */
	private static void initPressChart(boolean isContinuous, boolean isDay, String xValueTitle,
	                                   ArrayList<Integer> timeStamps,
	                                   ArrayList<Integer> dayStamps,
	                                   ArrayList<Float> pressEntries,
	                                   ArrayList<Float> pressMaxEntries,
	                                   ArrayList<Float> pressMinEntries,
	                                   Activity activity) {
		// find the pressure levels plot in the layout
		pressLevelsPlot = (XYPlot) activity.findViewById(R.id.xyPressPlot);
		// setup and format pressure data series
		pressLevelsSeries = new SimpleXYSeries(appContext.getString(R.string.currPress));

		minPressValue = Utils.pToU(900f, pressUnit);
		maxPressValue = Utils.pToU(1100f, pressUnit);
		/** Padding for top and bottom of the plot depending on user selected unit */
		float plotPadding = Utils.pressBoundary(pressUnit);

		if (!isContinuous && pressEntries.size() != 0) {
			if (isDay) {
				minPressValue = Collections.min(pressEntries);
				maxPressValue = Collections.max(pressEntries);
			} else {
				minPressValue = Collections.min(pressMinEntries);
				maxPressValue = Collections.max(pressMaxEntries);
			}
		}
		pressLevelsPlot.setRangeBottomMax(minPressValue - plotPadding);
		pressLevelsPlot.setRangeBottomMin(minPressValue - plotPadding);
		pressLevelsPlot.setRangeTopMax(maxPressValue + plotPadding);
		pressLevelsPlot.setRangeTopMin(maxPressValue + plotPadding);

		/** Formatter for (average) pressure plot */
		LineAndPointFormatter pressSeriesFormatter;
		/** Formatter for max pressure plot */
		LineAndPointFormatter pressMaxSeriesFormatter;
		/** Formatter for min pressure plot */
		LineAndPointFormatter pressMinSeriesFormatter;

		if (isContinuous) {
			pressLevelsSeries.useImplicitXVals();
			pressSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
			pressSeriesFormatter.getLinePaint().setStrokeWidth(10);
			pressLevelsPlot.addSeries(pressLevelsSeries, pressSeriesFormatter);
		} else {
			if (isDay) {
				if (dayStamps.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.hourOfDay) +" "+
							Integer.toString(dayStamps.get(0));
					for (int i=0; i<timeStamps.size(); i++) {
						pressLevelsSeries.addLast(timeStamps.get(i),pressEntries.get(i));
					}
				}
				pressSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT,
						Color.TRANSPARENT, null);
				pressSeriesFormatter.getLinePaint().setStrokeWidth(10);
				pressLevelsPlot.addSeries(pressLevelsSeries, pressSeriesFormatter);
			} else {
				if (pressEntries.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					for (int i=0; i<dayStamps.size(); i++) {
						pressLevelsSeries.addLast(dayStamps.get(i),pressEntries.get(i));
					}
				}
				/** Data series for the max barometric pressure */
				SimpleXYSeries pressMaxSeries = new SimpleXYSeries(appContext.getString(R.string.currPressMax));
				/** Data series for the min barometric pressure */
				SimpleXYSeries pressMinSeries = new SimpleXYSeries(appContext.getString(R.string.currPressMin));
				for (int i=0; i<timeStamps.size(); i++) {
					pressMaxSeries.addLast(dayStamps.get(i), pressMaxEntries.get(i));
					pressMinSeries.addLast(dayStamps.get(i), pressMinEntries.get(i));
				}
				pressSeriesFormatter = new LineAndPointFormatter(Color.BLACK, Color.TRANSPARENT,
						Color.TRANSPARENT, null);
				pressSeriesFormatter.getLinePaint().setStrokeWidth(10);
				pressLevelsPlot.addSeries(pressLevelsSeries, pressSeriesFormatter);
				pressMaxSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT,
						Color.TRANSPARENT, null);
				pressMaxSeriesFormatter.getLinePaint().setStrokeWidth(10);
				pressLevelsPlot.addSeries(pressMaxSeries, pressMaxSeriesFormatter);
				pressMinSeriesFormatter = new LineAndPointFormatter(Color.GREEN, Color.TRANSPARENT,
						Color.TRANSPARENT, null);
				pressMinSeriesFormatter.getLinePaint().setStrokeWidth(10);
				pressLevelsPlot.addSeries(pressMinSeries, pressMinSeriesFormatter);
			}
		}

		pressLevelsPlot.setTicksPerRangeLabel(3);
		pressLevelsPlot.getLayoutManager()
				.remove(pressLevelsPlot.getLegendWidget());
		pressLevelsPlot.getDomainLabelWidget().pack();
		if (isContinuous) {
			pressLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
		} else {
			if (pressLevelsSeries.size() < 16) {
				pressLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
			} else {
				pressLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
			}
		}
		pressLevelsPlot.setDomainValueFormat(new DecimalFormat("#"));
		pressLevelsPlot.getGraphWidget().setMarginBottom(margin30dp);
		pressLevelsPlot.getGraphWidget().setMarginTop(margin10dp);
		pressLevelsPlot.getGraphWidget().setMarginLeft(margin40dp);
		pressLevelsPlot.getGraphWidget().setMarginRight(margin10dp);
		pressLevelsPlot.getGraphWidget().getDomainLabelPaint().setTextSize(textSizeSmall);
		pressLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		pressLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		pressLevelsPlot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
				0, YLayoutStyle.RELATIVE_TO_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
		pressLevelsPlot.setDomainLabel(xValueTitle);

		pressLevelsPlot.getRangeLabelWidget().getLabelPaint().setTextSize(textSizeMedium);
		pressLevelsPlot.getGraphWidget().getRangeLabelPaint().setTextSize(textSizeSmall);
		pressLevelsPlot.setRangeLabel(Utils.pressUnit(appContext, pressUnit));

		pressLevelsPlot.setRangeStepMode(XYStepMode.SUBDIVIDE);
		DecimalFormat rangeFormat = new DecimalFormat(Utils.pressFormatRange(pressUnit));
		rangeFormat.setMinimumFractionDigits(Utils.pressFormatRangeDigit(pressUnit));
		pressLevelsPlot.setRangeValueFormat(rangeFormat);
		pressLevelsPlot.getRangeLabelWidget().position(0.04f,XLayoutStyle.RELATIVE_TO_LEFT,
				-0.15f,YLayoutStyle.RELATIVE_TO_BOTTOM,
				AnchorPosition.LEFT_BOTTOM);
		pressLevelsPlot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);
		pressLevelsPlot.getRangeLabelWidget().pack();

		// Change border color
		pressLevelsPlot.getBackgroundPaint().setColor(colorDark);
		// Change grid background color
		pressLevelsPlot.getGraphWidget().getGridBackgroundPaint().setColor(colorBright);
		// Change grid color
		pressLevelsPlot.getGraphWidget().getRangeGridLinePaint().setColor
				(Color.TRANSPARENT);
		pressLevelsPlot.getGraphWidget().getRangeSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		pressLevelsPlot.getGraphWidget().getDomainGridLinePaint().setColor
				(Color.TRANSPARENT);
		pressLevelsPlot.getGraphWidget().getDomainSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		// Change plot background color
		pressLevelsPlot.getGraphWidget().getBackgroundPaint().setColor(colorDark);
		// Change axis values text color and increment value
		if (isContinuous) {
			pressLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
		} else {
			pressLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
			pressLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
		}
		pressLevelsPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);

		pressLevelsPlot.redraw();
	}

	/**
	 * Clean up pressure chart for a new initialization
	 */
	private static void clearPressChart() {
		if (pressLevelsSeries.size() != 0) {
			for (int i=0; i<=pressLevelsSeries.size(); i++) {
				pressLevelsSeries.removeFirst();
			}
		}
		pressLevelsPlot.removeSeries(pressLevelsSeries);
		pressLevelsPlot.removeMarkers();
		pressLevelsPlot.clear();
	}

	/**
	 * Initialize humidity chart
	 *
	 * @param isContinuous
	 *            true -> use .useImplicitXVals(); for X values
	 *            false -> X values
	 * @param isDay
	 *            true -> get hourly records
	 *            false -> get daily records
	 * @param xValueTitle
	 *            title for X values as string
	 * @param timeStamps
	 *            list with time stamps of existing records
	 * @param dayStamps
	 *            list with day stamps of existing records if day view
	 * @param humidEntries
	 *            list with humidity of existing records if day view
	 *            list with average humidity if month view
	 * @param humidMaxEntries
	 *            unused if day view
	 *            list with max humidity if month view
	 * @param humidMinEntries
	 *            unused if day view
	 *            list with min humidity if month view
	 */
	private static void initHumidChart(boolean isContinuous, boolean isDay, String xValueTitle,
	                                   ArrayList<Integer> timeStamps,
	                                   ArrayList<Integer> dayStamps,
	                                   ArrayList<Float> humidEntries,
	                                   ArrayList<Float> humidMaxEntries,
	                                   ArrayList<Float> humidMinEntries,
	                                   Activity activity) {
		// find the humidity levels plot in the layout
		humidLevelsPlot = (XYPlot) activity.findViewById(R.id.xyHumidPlot);
		// setup and format humidity data series
		humidLevelsSeries = new SimpleXYSeries(appContext.getString(R.string.currHumid));

		minHumidValue = -100f;
		maxHumidValue = +100f;

		if (!isContinuous && humidEntries.size() != 0) {
			if (isDay) {
				minHumidValue = Collections.min(humidEntries);
				maxHumidValue = Collections.max(humidEntries);
			} else {
				minHumidValue = Collections.min(humidMinEntries);
				maxHumidValue = Collections.max(humidMaxEntries);
			}
		}
		humidLevelsPlot.setRangeBottomMax(minHumidValue - 0.1);
		humidLevelsPlot.setRangeBottomMin(minHumidValue - 0.1);
		humidLevelsPlot.setRangeTopMax(maxHumidValue + 0.1);
		humidLevelsPlot.setRangeTopMin(maxHumidValue + 0.1);

		/** Formatter for (average) humidity plot */
		LineAndPointFormatter humidSeriesFormatter;
		/** Formatter for max humidity plot */
		LineAndPointFormatter humidMaxSeriesFormatter;
		/** Formatter for min humidity plot */
		LineAndPointFormatter humidMinSeriesFormatter;

		if (isContinuous) {
			humidLevelsSeries.useImplicitXVals();
			humidSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
			humidSeriesFormatter.getLinePaint().setStrokeWidth(10);
			humidLevelsPlot.addSeries(humidLevelsSeries, humidSeriesFormatter);
		} else {
			if (isDay) {
				if (dayStamps.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.hourOfDay) +" "+ Integer.toString(dayStamps.get(0));
					for (int i=0; i<timeStamps.size(); i++) {
						humidLevelsSeries.addLast(timeStamps.get(i),humidEntries.get(i));
					}
				}
				humidSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
				humidSeriesFormatter.getLinePaint().setStrokeWidth(10);
				humidLevelsPlot.addSeries(humidLevelsSeries, humidSeriesFormatter);
			} else {
				if (humidEntries.size() == 0) {
					xValueTitle = xValueTitle +" "+ appContext.getString(R.string.noData);
				} else {
					for (int i=0; i<dayStamps.size(); i++) {
						humidLevelsSeries.addLast(dayStamps.get(i),humidEntries.get(i));
					}
				}
				/** Data series for the max humidity */
				SimpleXYSeries humidMaxSeries = new SimpleXYSeries(appContext.getString(R.string.currHumidMax));
				/** Data series for the min humidity */
				SimpleXYSeries humidMinSeries = new SimpleXYSeries(appContext.getString(R.string.currHumidMin));
				for (int i=0; i<timeStamps.size(); i++) {
					humidMaxSeries.addLast(dayStamps.get(i), humidMaxEntries.get(i));
					humidMinSeries.addLast(dayStamps.get(i), humidMinEntries.get(i));
				}
				humidSeriesFormatter = new LineAndPointFormatter(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, null);
				humidSeriesFormatter.getLinePaint().setStrokeWidth(10);
				humidLevelsPlot.addSeries(humidLevelsSeries, humidSeriesFormatter);
				humidMaxSeriesFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
				humidMaxSeriesFormatter.getLinePaint().setStrokeWidth(10);
				humidLevelsPlot.addSeries(humidMaxSeries, humidMaxSeriesFormatter);
				humidMinSeriesFormatter = new LineAndPointFormatter(Color.GREEN, Color.TRANSPARENT, Color.TRANSPARENT, null);
				humidMinSeriesFormatter.getLinePaint().setStrokeWidth(10);
				humidLevelsPlot.addSeries(humidMinSeries, humidMinSeriesFormatter);
			}
		}

		humidLevelsPlot.setTicksPerRangeLabel(3);
		humidLevelsPlot.getLayoutManager().remove(humidLevelsPlot.getLegendWidget());
		humidLevelsPlot.getDomainLabelWidget().pack();
		if (isContinuous) {
			humidLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
		} else {
			if (humidLevelsSeries.size() < 16) {
				humidLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
			} else {
				humidLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
			}
		}
		humidLevelsPlot.setDomainValueFormat(new DecimalFormat("#"));
		humidLevelsPlot.getGraphWidget().setMarginBottom(margin30dp);
		humidLevelsPlot.getGraphWidget().setMarginTop(margin10dp);
		humidLevelsPlot.getGraphWidget().setMarginLeft(margin20dp);
		humidLevelsPlot.getGraphWidget().setMarginRight(margin10dp);
		humidLevelsPlot.getGraphWidget().getDomainLabelPaint().setTextSize(textSizeSmall);
		humidLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		humidLevelsPlot.getGraphWidget().getRangeLabelPaint().setTextSize(textSizeSmall);
		humidLevelsPlot.getDomainLabelWidget().getLabelPaint().setTextSize(textSizeSmall);
		humidLevelsPlot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
				0, YLayoutStyle.RELATIVE_TO_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
		humidLevelsPlot.setDomainLabel(xValueTitle);

		humidLevelsPlot.getRangeLabelWidget().getLabelPaint().setTextSize(textSizeMedium);
		humidLevelsPlot.setRangeLabel(appContext.getString(R.string.humidSign));
		humidLevelsPlot.getRangeLabelWidget().position(0.04f, XLayoutStyle.RELATIVE_TO_LEFT,
				-0.15f, YLayoutStyle.RELATIVE_TO_BOTTOM,
				AnchorPosition.LEFT_BOTTOM);
		humidLevelsPlot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);
		humidLevelsPlot.getRangeLabelWidget().pack();

		// Change border color
		humidLevelsPlot.getBackgroundPaint().setColor(colorDark);
		// Change grid background color
		humidLevelsPlot.getGraphWidget().getGridBackgroundPaint().setColor(colorBright);
		// Change grid color
		humidLevelsPlot.getGraphWidget().getRangeGridLinePaint().setColor
				(Color.TRANSPARENT);
		humidLevelsPlot.getGraphWidget().getRangeSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		humidLevelsPlot.getGraphWidget().getDomainGridLinePaint().setColor
				(Color.TRANSPARENT);
		humidLevelsPlot.getGraphWidget().getDomainSubGridLinePaint().setColor
				(Color.TRANSPARENT);
		// Change plot background color
		humidLevelsPlot.getGraphWidget().getBackgroundPaint().setColor(colorDark);
		// Change axis values text color and increment value
		if (isContinuous) {
			humidLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
		} else {
			humidLevelsPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
			humidLevelsPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 2);
		}
		humidLevelsPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);

		humidLevelsPlot.redraw();
	}

	/**
	 * Clean up humidity chart for a new initialization
	 */
	private static void clearHumidChart() {
		if (humidLevelsSeries.size() != 0) {
			for (int i=0; i<=humidLevelsSeries.size(); i++) {
				humidLevelsSeries.removeFirst();
			}
		}
		humidLevelsPlot.removeSeries(humidLevelsSeries);
		humidLevelsPlot.removeMarkers();
		humidLevelsPlot.clear();
	}

	/**
	 * Customized alert
	 *
	 * @param context
	 *            Context of app
	 * @param title
	 *            Title of alert dialog
	 * @param message
	 *            Message in alert dialog
	 */
	public static void myAlert(Context context, String title, String message) {

		/** Builder for alert dialog */
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(context.getResources().getString(android.R.string.ok),
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
	}

	/**
	 * Show dialog with available backup files
	 */
	public static ListView restoreFileDialog() {
		/** File pointer to directory */
		File directory = new File(path);

		// clear files array
		files.clear();
		// get all the files from a directory
		/** List with files in directory */
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				/** String with filename extension */
				String filenameArray[] = file.getName().split("\\.");
				if (filenameArray[filenameArray.length-1].equalsIgnoreCase("JSON")) {
					files.add(file.getName());
				}
			}
		}

		if (files.size() != 0) {
			/** Builder for restore file selection dialog */
			AlertDialog.Builder fileListBuilder = new AlertDialog.Builder(appContext);
			/** Inflater for restore file selection dialog */
			LayoutInflater fileListInflater = (LayoutInflater) appContext.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			/** View for restore file selection dialog */
			View fileListView = fileListInflater.inflate(R.layout.restore_dialog, null);
			fileListBuilder.setView(fileListView);
			/** Pointer to restore file selection dialog */
			AlertDialog fileList = fileListBuilder.create();
			fileList.setTitle(appContext.getString(R.string.sbRestore));

			fileList.setButton(AlertDialog.BUTTON_POSITIVE, appContext.getString(R.string.bRestore),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (isFileSelected) {
								dialog.dismiss();
								if (WSDatabaseHelper.restoreDBfromJSON()) {
									Utils.myAlert(appContext, appContext.getString(R.string.errorRestoreTitle),
											appContext.getString(R.string.errorRestore));
								} else {
									Utils.myAlert(appContext, appContext.getString(R.string.succRestoreTitle),
											appContext.getString(R.string.succRestore, restoreFilePath));
								}
							} else {
								Utils.myAlert(appContext, appContext.getString(R.string.errorRestoreTitle),
										appContext.getString(R.string.noFileSelected));
							}
						}
					});

			fileList.setButton(AlertDialog.BUTTON_NEGATIVE, appContext.getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							isFileSelected = false;
							dialog.dismiss();
						}
					});

			/** Pointer to list view with the files */
			ListView lvFileList = (ListView) fileListView.findViewById(R.id.lv_FileList);
			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
					appContext,
					android.R.layout.simple_list_item_1,
					files );

			lvFileList.setAdapter(arrayAdapter);

			fileList.show();
			return lvFileList;

		} else {
			Utils.myAlert(appContext,
					appContext.getResources().getString(R.string.errorRestoreTitle),
					appContext.getResources().getString(R.string.noRestoreFile));
		}
		return null;
	}

	/**
	 * Dialog to show sensor info
	 *
	 * @param sensorType
	 *            Which sensor info to show
	 *            0 = temperature sensor
	 *            1 = pressure sensor
	 *            2 = humidity sensor
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void sensorInfoDialog(int sensorType) {
		/** Builder for theme selection dialog */
		AlertDialog.Builder sensorInfoDialogBuilder = new AlertDialog.Builder(appContext);
		/** Inflater for theme selection dialog */
		LayoutInflater sensorInfoInflater = (LayoutInflater) appContext.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		/** View for theme selection dialog */
		View sensorInfoView = sensorInfoInflater.inflate(R.layout.sensor_info, null);
		sensorInfoDialogBuilder.setView(sensorInfoView);
		/** Pointer to theme selection dialog */
		AlertDialog sensorInfo = sensorInfoDialogBuilder.create();

		/** List of all available humidity sensors */
		List<Sensor> sensorInfoList;
		/** Text view for showing the sensor info */
		TextView tvValue;
		/** String for resolution unit */
		String resUnit;

		if (sensorType == 2) {
			/** List of all available humidity sensors */
			sensorInfoList = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
			sensorInfo.setTitle(appContext.getString(R.string.tvSensorTypeHumidity));
			resUnit = appContext.getResources().getString(R.string.humidSign);
		} else if (sensorType == 1) {
			/** List of all available pressure sensors */
			sensorInfoList = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
			sensorInfo.setTitle(appContext.getString(R.string.tvSensorTypePressure));
			resUnit = Utils.pressUnit(appContext, pressUnit);
		} else {
			/** List of all available temperature sensors */
			sensorInfoList = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
			sensorInfo.setTitle(appContext.getString(R.string.tvSensorTypeTemp));
			resUnit = Utils.pressUnit(appContext, pressUnit);
		}

		sensorInfo.setButton(AlertDialog.BUTTON_POSITIVE, appContext.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		sensorInfo.show();

		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_name_value);
		tvValue.setText(sensorInfoList.get(0).getName());
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_type_value);
		tvValue.setText(sensorInfoList.get(0).getStringType());
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_vendor_value);
		tvValue.setText(sensorInfoList.get(0).getVendor());
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_version_value);
		tvValue.setText(Integer.toString(sensorInfoList.get(0).getVersion()));
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_resolution_value);
		tvValue.setText(Float.toString(sensorInfoList.get(0).getResolution())+resUnit);
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_power_value);
		tvValue.setText(Float.toString(sensorInfoList.get(0).getPower())+appContext.getResources().getString(R.string.milliAmpere));
		tvValue = (TextView) sensorInfoView.findViewById(R.id.tv_sensor_rate_value);
		tvValue.setText(Integer.toString(sensorInfoList.get(0).getMaxDelay()/1000)+appContext.getResources().getString(R.string.milliSecond));
	}

	/**
	 * Reorder position of analogue gauges
	 *
	 * @param  context
	 *            Application context
	 * @param textID
	 *            Id of TextView for positioning
	 * @param gaugeID
	 *            Id of GaugeView for positioning
	 * @param gvBig
	 *            GaugeView for big gauge
	 * @param gvTopOrLeft
	 *            GaugeView to be shown left (portrait) or top right (landscape)
	 * @param gvBottomOrRight
	 *            GaugeView to be shown right (portrait) or bottom right (landscape)
	 * @param tvBig
	 *            TextView to display value for big gauge
	 * @param tvTopOrLeft
	 *            TextView to display value for left (portrait) or top right (landscape) gauge
	 * @param tvBottomOrRight
	 *            TextView to display value for right (portrait) or bottom right (landscape) gauge
	 */
	public static void reOrderGauge(Context context, int textID, int gaugeID,
	                                GaugeView gvBig, GaugeView gvTopOrLeft, GaugeView gvBottomOrRight,
	                         TextView tvBig, TextView tvTopOrLeft, TextView tvBottomOrRight) {
		/* Layout parameters to switch the gauges between different positions */
		RelativeLayout.LayoutParams layoutParams;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			layoutParams = (RelativeLayout.LayoutParams) gvBig.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
			layoutParams.width = (int) PixelUtils.dpToPix(360);
			layoutParams.height = (int) PixelUtils.dpToPix(360);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.START_OF, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) gvTopOrLeft.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			layoutParams.addRule(RelativeLayout.LEFT_OF, textID);
			layoutParams.width = (int) PixelUtils.dpToPix(125);
			layoutParams.height = (int) PixelUtils.dpToPix(125);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.START_OF, textID);
			}

			layoutParams = (RelativeLayout.LayoutParams) gvBottomOrRight.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.addRule(RelativeLayout.LEFT_OF, textID);
			layoutParams.width = (int) PixelUtils.dpToPix(125);
			layoutParams.height = (int) PixelUtils.dpToPix(125);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.START_OF, textID);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvBig.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvTopOrLeft.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvBottomOrRight.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			}
		} else {
			layoutParams = (RelativeLayout.LayoutParams) gvBig.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			layoutParams.width = (int) PixelUtils.dpToPix(360);
			layoutParams.height = (int) PixelUtils.dpToPix(360);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) gvTopOrLeft.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, textID);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			layoutParams.width = (int) PixelUtils.dpToPix(180);
			layoutParams.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) gvBottomOrRight.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, textID);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParams.width = (int) PixelUtils.dpToPix(180);
			layoutParams.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvBig.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			layoutParams.addRule(RelativeLayout.BELOW, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvTopOrLeft.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			layoutParams.addRule(RelativeLayout.BELOW, gaugeID);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
			}

			layoutParams = (RelativeLayout.LayoutParams) tvBottomOrRight.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			layoutParams.addRule(RelativeLayout.BELOW, gaugeID);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			}
		}
	}

	/**
	 * Get today max and min values from the database and compare with current measurement
	 * If current measurement is bigger or smaller, use current measurement instead
	 */
	public static void getTodayMinMax() {
		wsDbHelper = new WSDatabaseHelper(appContext);
		dataBase = wsDbHelper.getReadableDatabase();
		/** Cursor filled with existing entries of today */
		Cursor dayEntry = wsDbHelper.getDay(dataBase, 1);
		dayEntry.moveToLast();
		if (dayEntry.getCount() != 0) {
			// get min and max values of today
			if (dayEntry.getFloat(6) >= lastTempValue ) {
				todayMaxTemp = Utils.cToU(dayEntry.getFloat(6), tempUnit);
			} else {
				todayMaxTemp = Utils.cToU(lastTempValue, tempUnit);
			}
			if (dayEntry.getFloat(7) <= lastTempValue ) {
				todayMinTemp = Utils.cToU(dayEntry.getFloat(7), tempUnit);
			} else {
				todayMinTemp = Utils.cToU(lastTempValue, tempUnit);
			}
			if (dayEntry.getFloat(9) >= lastPressValue ) {
				todayMaxPress = Utils.cToU(dayEntry.getFloat(9), pressUnit);
			} else {
				todayMaxPress = Utils.cToU(lastPressValue, pressUnit);
			}
			if (dayEntry.getFloat(10) <= lastPressValue ) {
				todayMinPress = Utils.cToU(dayEntry.getFloat(10), pressUnit);
			} else {
				todayMinPress = Utils.cToU(lastPressValue, pressUnit);
			}
			if (dayEntry.getFloat(12) >= lastHumidValue ) {
				todayMaxHumid = dayEntry.getFloat(12);
			} else {
				todayMaxHumid = lastHumidValue;
			}
			if (dayEntry.getFloat(13) >= lastHumidValue ) {
				todayMinHumid = dayEntry.getFloat(13);
			} else {
				todayMinHumid = lastHumidValue;
			}
		} else {
			todayMaxTemp = Utils.cToU(lastTempValue, tempUnit);
			todayMinTemp = Utils.cToU(lastTempValue, tempUnit);
			todayMaxPress = Utils.cToU(lastPressValue, pressUnit);
			todayMinPress = Utils.cToU(lastPressValue, pressUnit);
			todayMaxHumid = lastHumidValue;
			todayMinHumid = lastHumidValue;
		}
		dataBase.close();
		wsDbHelper.close();
	}

	/**
	 * Get date and time and update station view.
	 * Show forecast icon depending on current air pressure
	 */
	public static void updateStationView() {

		/** Calendar to get current date and time */
		Calendar c = Calendar.getInstance();

		tvDate.setText(DateUtils.formatDateTime(appContext, c.getTimeInMillis(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));

		tvTime.setText(DateUtils.formatDateTime(appContext, c.getTimeInMillis(),
				DateUtils.FORMAT_SHOW_TIME));

		/** Time of day to select day or night forecast icon */
		int time = c.get(Calendar.HOUR_OF_DAY);

		/** ImageView for the tendency picture */
		ImageView ivForecast = (ImageView) appActivity.findViewById(R.id.ivForecast);

		// TODO this should be improved in the future by a better forecast algorithm than just the pressure
		if (lastPressValue2 < 995) {
			ivForecast.setImageDrawable(appActivity.getResources().getDrawable(R.mipmap.ic_rain));
		} else if (lastPressValue2 < 1025) {
			if (time < 6 || time >19) {
				ivForecast.setImageDrawable(appActivity.getResources().getDrawable(R.mipmap.ic_cloudmoon));
			} else {
				ivForecast.setImageDrawable(appActivity.getResources().getDrawable(R.mipmap.ic_cloudsun));
			}
		} else {
			if (time < 6 || time >19) {
				ivForecast.setImageDrawable(appActivity.getResources().getDrawable(R.mipmap.ic_moon));
			} else {
				ivForecast.setImageDrawable(appActivity.getResources().getDrawable(R.mipmap.ic_sun));
			}
		}
	}
}
