package tk.giesecke.weatherstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utils
 * unit conversion methods
 *
 * @author Bernd Giesecke
 * @version 0.1 beta May 5, 2015.
 */
class Utils {

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
	public static String pressFormatRange(int unit) {
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
	public static int pressFormatRangeDigit(int unit) {
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
				for (int i=0; i<WeatherStation.tempLevelsSeries.size(); i++) {
					checkedArray.add((float) WeatherStation.tempLevelsSeries.getY(i));
				}
				break;
			case 1:
				for (int i=0; i<WeatherStation.pressLevelsSeries.size(); i++) {
					checkedArray.add((float) WeatherStation.pressLevelsSeries.getY(i));
				}
				break;
			case 2:
				for (int i=0; i<WeatherStation.humidLevelsSeries.size(); i++) {
					checkedArray.add((float) WeatherStation.humidLevelsSeries.getY(i));
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
}
