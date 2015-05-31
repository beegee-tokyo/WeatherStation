package tk.giesecke.weatherstation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * WSDatabaseHelper
 * database access methods
 *
 * @author Bernd Giesecke
 * @version 1.0 May 31, 2015.
 */
class WSDatabaseHelper extends SQLiteOpenHelper {

	/** Debug tag */
	private static final String LOG_TAG = "WeatherStation_DB";
	/** Name of the database */
	private static final String DATABASE_NAME="WSDatabase";
	/** Name of the table */
	private static final String TABLE_NAME = "weather";

	public WSDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"ts INTEGER, ds INTEGER, dn INTEGER, " +
				"t FLOAT, p FLOAT, h FLOAT," +
				"mat FLOAT, mit FLOAT, avt FLOAT," +
				"map FLOAT, mip FLOAT, avp FLOAT," +
				"mah FLOAT, mih FLOAT, avh FLOAT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
		onCreate(db);
	}

	/**
	 * Add an entry to the database
	 *
	 * @param db
	 *            pointer to database
	 * @param timeStamp
	 *            hour of measurment
	 * @param dayStamp
	 *            day of measurment
	 * @param dayNumber
	 *            recorded day (1-31)
	 * @param currTemp
	 *            measured temperature
	 * @param currPress
	 *            measured pressure
	 * @param currHumid
	 *            measured humidity
	 * @param currMaxTemp
	 *            max temperature of a day
	 * @param currMinTemp
	 *            min temperature of a day
	 * @param currAvgTemp
	 *            average temperature of a day
	 * @param currMaxPress
	 *            max pressure of a day
	 * @param currMinPress
	 *            min pressure of a day
	 * @param currAvgPress
	 *            average pressure of a day
	 * @param currMaxHumid
	 *            max humidity of a day
	 * @param currMinHumid
	 *            min humidity of a day
	 * @param currAvgHumid
	 *            average humidity of a day
	 * @return <code>boolean</code>
	 *            true if addDay was successful
	 *            false if addDay failed
	 */
	public boolean addDay(SQLiteDatabase db, int timeStamp, int dayStamp, int dayNumber,
	                   float currTemp, float currPress, float currHumid,
	                   float currMaxTemp, float currMinTemp, float currAvgTemp,
	                   float currMaxPress, float currMinPress, float currAvgPress,
	                   float currMaxHumid, float currMinHumid, float currAvgHumid) {

		/** ContentValues to hold the measured and calculated values to be added to the database */
		ContentValues values = new ContentValues(14);
		values.put("ts", timeStamp);
		values.put("ds", dayStamp);
		values.put("dn", dayNumber);
		values.put("t", currTemp);
		values.put("p", currPress);
		values.put("h", currHumid);
		values.put("mat", currMaxTemp);
		values.put("mit", currMinTemp);
		values.put("avt", currAvgTemp);
		values.put("map", currMaxPress);
		values.put("mip", currMinPress);
		values.put("avp", currAvgPress);
		values.put("mah", currMaxHumid);
		values.put("mih", currMinHumid);
		values.put("avh", currAvgHumid);

		/** result of database action */
		long result = db.insert(TABLE_NAME, null, values);
		return result != -1;
	}

	/**
	 * Read data of day "dayNumber" and returns the data as a cursor
	 *
	 * @param db
	 *            pointer to database
	 * @param dayNumber
	 *            the day we want to read (1-31)
	 * @return <code>Cursor</code> dayStamp
	 *            Cursor with all database entries matching with dayNumber
	 */
	public Cursor getDay(SQLiteDatabase db, int dayNumber) {
		/** Access to the database */
		//db = this.getReadableDatabase();
		/** Cursor holding the records of a day */
		return db.query(TABLE_NAME,
				new String[]{"ts", "ds", "dn", "t", "p", "h",
						"mat", "mit", "avt",
						"map", "mip", "avp",
						"mah", "mih", "avh"},
				"dn=" + dayNumber,
				null, null, null, null);
	}

	/**
	 * Read all rows and return the data as a cursor
	 *
	 * @param db
	 *            pointer to database
	 * @return <code>Cursor</code> dayStamp
	 *            Cursor with all database entries matching with dayNumber
	 */
	private Cursor getAll(SQLiteDatabase db) {
		/** Access to the database */
		//db = this.getReadableDatabase();
		/** Cursor holding all entries of the database */
		Cursor allRows = db.rawQuery("select * from " + TABLE_NAME, null);
		if (BuildConfig.DEBUG) Log.d("WeatherStation-DB", "Read all Rows Cursor = "+allRows);
		return allRows;
	}

	/**
	 * Makes space for the next day entries and deletes the oldest entry
	 * This way only records of the last 31 days are stored
	 *
	 * @param db
	 *            pointer to database
	 */
	public void shiftDays(SQLiteDatabase db) {
		deleteDay(db);

		/** Access to the database */
		//db = this.getWritableDatabase();

		for (int dayNumber=30; dayNumber>=1; dayNumber--) {
			/** Content value holding "dn", the value we want to change */
			ContentValues updateData = new ContentValues();
			updateData.put("dn", dayNumber + 1);
			db.update(TABLE_NAME, updateData, "dn=" + dayNumber, null);
		}
	}

	/**
	 * Deletes all entries matching with dayNumber
	 *  @param db
	 *            pointer to database
	 *
     */
	private void deleteDay(SQLiteDatabase db) {
		/** Access to the database */
		//db = this.getWritableDatabase();
		db.delete(TABLE_NAME, "dn=" + 31, null);
	}

	/**
	 * Delete all entries in the  data base
	 *
	 * @param db
	 *            pointer to database
	 */
	public void cleanDB(SQLiteDatabase db) {
		/** Access to the database */
		//db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME); //delete all rows in a table
	}

	/**
	 * Read all entries from the database and write them in CSV format to the external storage
	 * File is saved in /WeatherStation/MM-dd-WeatherStation.csv
	 * MM = month
	 * dd = today's day
	 *
	 * @return <code>boolean</code>
	 *              true if file could be created
	 *              false if there was an error
	 */
	public static boolean exportDatabase() {

		/**First of all we check if the external storage of the device is available for writing.
		 * Remember that the external storage is not necessarily the sd card. Very often it is
		 * the device storage.
		 */
		/** Status of external storage (mounted or not) */
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return false;
		}
		else {
			//We use our own directory for saving our .csv file.
			/** Current time as integer array */
			int[] currTime = Utils.getCurrentDate();
			WeatherStation.exportFilePath = WeatherStation.path + Integer.toString(currTime[2]) + "-" +
					Integer.toString(currTime[1]) + "-" +
					"WeatherStation.csv";
			/** Pointer to directory */
			File file = new File(WeatherStation.path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					// Big problem as we cannot create the path
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Cannot create folder on ext memory");
					return false;
				}
			}
			file = new File(WeatherStation.exportFilePath);
			if (file.exists()) {
				if (!file.delete()) {
					// Big problem as we cannot delete the old file
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Cannot delete existing file");
					return false;
				}
			}

			/** PrintWriter to write into CSV file */
			PrintWriter printWriter = null;
			try
			{
				//if (!file.createNewFile()) return false;
				printWriter = new PrintWriter(new FileWriter(file));

				/** Instance of weather db helper */
				WeatherStation.wsDbHelper = new WSDatabaseHelper(WeatherStation.appContext);
				WeatherStation.dataBase = WeatherStation.wsDbHelper.getReadableDatabase();

				/** Cursor holding all rows of the database */
				Cursor allRows = WeatherStation.wsDbHelper.getAll(WeatherStation.dataBase);

				allRows.moveToFirst();

				//Write the name of the table and the name of the columns (comma separated values) in the .csv file.
				/** String with the recorded values or description */
				String record = "Timestamp,Datestamp,Daynumber," +
						"Temperature,Pressure,Humidity," +
						"MaxTemperature,MinTemperature,AverageTemperature," +
						"MaxPressure,MinPressure,AveragePressure," +
						"MaxHumidity,MinHumidity,AverageHumidity";
				printWriter.println(record); //write the record in the .csv file
				for (int i=0; i<allRows.getCount(); i++)
				{
					/** The timestamps */
					int timeStamp = allRows.getInt(1);
					/** The day stamps */
					int dateStamp = allRows.getInt(2);
					/** The day number */
					int dayNumber = allRows.getInt(3);
					/** The temperature value */
					float hourTemp = allRows.getFloat(4);
					/** The pressure value */
					float hourPress = allRows.getFloat(5);
					/** The humidity value */
					float hourHumid = allRows.getFloat(6);
					/** The max temperature value */
					float maxTemp = allRows.getFloat(7);
					/** The min temperature value */
					float minTemp = allRows.getFloat(8);
					/** The average temperature value */
					float avgTemp = allRows.getFloat(9);
					/** The max pressure value */
					float maxPress = allRows.getFloat(10);
					/** The min pressure value */
					float minPress = allRows.getFloat(11);
					/** The average pressure value */
					float avgPress = allRows.getFloat(12);
					/** The max humidity value */
					float maxHumid = allRows.getFloat(13);
					/** The min humidity value */
					float minHumid = allRows.getFloat(14);
					/** The average humidity value */
					float avgHumid = allRows.getFloat(15);

					/**Create the line to write in the .csv file.
					 * We need a String where values are comma separated.
					 * The field date (Long) is formatted in a readable text. The amount field
					 * is converted into String.
					 */
					record = Integer.toString(timeStamp) + "," + Integer.toString(dateStamp) + "," + Integer.toString(dayNumber) + "," +
							Float.toString(hourTemp) + "," + Float.toString(hourPress) + "," + Float.toString(hourHumid) + "," +
							Float.toString(maxTemp) + "," + Float.toString(minTemp) + "," + Float.toString(avgTemp) + "," +
							Float.toString(maxPress) + "," + Float.toString(minPress) + "," + Float.toString(avgPress) + "," +
							Float.toString(maxHumid) + "," + Float.toString(minHumid) + "," + Float.toString(avgHumid) + ",";
					printWriter.println(record); //write the record in the .csv file
					allRows.moveToNext();
				}
				printWriter.flush();
				allRows.close();
				WeatherStation.dataBase.close();
				WeatherStation.wsDbHelper.close();
			}
			catch(Exception exc) {
				//if there are any exceptions, return false
				return false;
			}
			finally {
				if(printWriter != null) printWriter.close();
			}

			//If there are no errors, return true.
			return true;
		}
	}

	/**
	 * Backup database in JSON format on SD card
	 * File is saved in /WeatherStation/MM-dd-WeatherStation.JSON
	 * MM = month
	 * dd = today's day
	 *
	 * @return <code>boolean</code>
	 *            true => write to file successful
	 *            false => write to file failed
	 */
	public static boolean backupDBasJSON() {

		/**First of all we check if the external storage of the device is available for writing.
		 * Remember that the external storage is not necessarily the sd card. Very often it is
		 * the device storage.
		 */
		/** Status of external storage (mounted or not) */
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return false;
		}
		else {
			//We use our own directory for saving our .JSON file.
			/** Current time as integer array */
			int[] currTime = Utils.getCurrentDate();
			WeatherStation.exportFilePath = WeatherStation.path + Integer.toString(currTime[2]) + "-" +
					Integer.toString(currTime[1]) + "-" +
					"WeatherStation.JSON";
			/** Pointer to directory/file */
			File file = new File(WeatherStation.path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					// Big problem as we cannot create the path
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Cannot create folder on ext memory");
					return false;
				}
			}
			file = new File(WeatherStation.exportFilePath);
			if (file.exists()) {
				if (!file.delete()) {
					// Big problem as we cannot delete the old file
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Cannot delete existing file");
					return false;
				}
			}

			/** JSON object to hold the JSON objects and arrays with the recorded items */
			JSONArray jsonToday = new JSONArray();

			try
			{
				/** Instance of weather db helper */
				WeatherStation.wsDbHelper = new WSDatabaseHelper(WeatherStation.appContext);
				WeatherStation.dataBase = WeatherStation.wsDbHelper.getReadableDatabase();

				/** Cursor holding all rows of the database */
				Cursor allRows = WeatherStation.wsDbHelper.getAll(WeatherStation.dataBase);

				allRows.moveToFirst();

				for (int i=0; i<allRows.getCount(); i++)
				{
					/** JSON object to hold recorded measures */
					JSONObject obj = new JSONObject();
					obj.put("ts", allRows.getInt(1));
					obj.put("ds", allRows.getInt(2));
					obj.put("dn", allRows.getInt(3));
					obj.put("t", allRows.getFloat(4));
					obj.put("p", allRows.getFloat(5));
					obj.put("h", allRows.getFloat(6));
					obj.put("mat", allRows.getFloat(7));
					obj.put("mit", allRows.getFloat(8));
					obj.put("avt", allRows.getFloat(9));
					obj.put("map", allRows.getFloat(10));
					obj.put("mip", allRows.getFloat(11));
					obj.put("avp", allRows.getFloat(12));
					obj.put("mah", allRows.getFloat(13));
					obj.put("minh", allRows.getFloat(14));
					obj.put("avh", allRows.getFloat(15));
					jsonToday.put(obj);
					allRows.moveToNext();
				}

				allRows.close();
				WeatherStation.dataBase.close();
				WeatherStation.wsDbHelper.close();
			}
			catch(Exception exc) {
				//if there are any exceptions, return false
				return false;
			}
			try {
				/** File writer to JSON file */
				FileWriter fileJSON = new FileWriter(WeatherStation.exportFilePath);
				fileJSON.write(jsonToday.toString());
				fileJSON.flush();
				fileJSON.close();
			} catch (IOException e) {
				return false;
			}

			//If there are no errors, return true.
			return true;
		}
	}

	/**
	 * Restore database from JSON format file
	 *
	 * @return <code>boolean</code>
	 *            true => restore to database successful
	 *            false => restore to database failed
	 */
	public static boolean restoreDBfromJSON() {

		/** List to hold the timestamps */
		ArrayList<Integer> timeStamp = new ArrayList<>();
		/** List to hold the day stamps */
		ArrayList<Integer> dateStamp = new ArrayList<>();
		/** List to hold the day numbers */
		ArrayList<Integer> dayNumber = new ArrayList<>();
		/** List to hold the temperature values */
		ArrayList<Float>  hourTemp = new ArrayList<>();
		/** List to hold the pressure values */
		ArrayList<Float> hourPress = new ArrayList<>();
		/** List to hold the humidity values */
		ArrayList<Float> hourHumid = new ArrayList<>();
		/** List to hold the max temperature values */
		ArrayList<Float> maxTemp = new ArrayList<>();
		/** List to hold the min temperature values */
		ArrayList<Float> minTemp = new ArrayList<>();
		/** List to hold the average temperature values */
		ArrayList<Float> avgTemp = new ArrayList<>();
		/** List to hold the max pressure values */
		ArrayList<Float> maxPress = new ArrayList<>();
		/** List to hold the min pressure values */
		ArrayList<Float> minPress = new ArrayList<>();
		/** List to hold the average pressure values */
		ArrayList<Float> avgPress = new ArrayList<>();
		/** List to hold the max humidity values */
		ArrayList<Float> maxHumid = new ArrayList<>();
		/** List to hold the min humidity values */
		ArrayList<Float> minHumid = new ArrayList<>();
		/** List to hold the average humidity values */
		ArrayList<Float> avgHumid = new ArrayList<>();

		try {
			/** Pointer to directory/file */
			File file = new File(WeatherStation.restoreFilePath);

			/** Stream to read file */
			FileInputStream stream = new FileInputStream(file);
			/** String that holds the JSON object */
			String jsonStr = null;
			try {
				/** File channel for reading stream */
				FileChannel fc = stream.getChannel();
				/** Buffer for reading */
				MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

				jsonStr = Charset.defaultCharset().decode(bb).toString();
			} catch (Exception e) {
				return true;
			}
			finally {
				stream.close();
			}

			/** JSON array with the backup data */
			JSONArray jsonFromBackup = new JSONArray(jsonStr);

			// looping through all nodes
			for (int i = 0; i < jsonFromBackup.length(); i++) {
				/** JSON object for a single data record */
				JSONObject obj = jsonFromBackup.getJSONObject(i);

				timeStamp.add(obj.getInt("ts"));
				dateStamp.add(obj.getInt("ds"));
				dayNumber.add(obj.getInt("dn"));
				hourTemp.add((float) obj.getDouble("t"));
				hourPress.add((float) obj.getDouble("p"));
				hourHumid.add((float) obj.getDouble("h"));
				maxTemp.add((float) obj.getDouble("mat"));
				minTemp.add((float) obj.getDouble("mit"));
				avgTemp.add((float) obj.getDouble("avt"));
				maxPress.add((float) obj.getDouble("map"));
				minPress.add((float) obj.getDouble("mip"));
				avgPress.add((float) obj.getDouble("avp"));
				maxHumid.add((float) obj.getDouble("mah"));
				minHumid.add((float) obj.getDouble("minh"));
				avgHumid.add((float) obj.getDouble("avh"));
			}
		} catch (Exception e) {
			return true;
		}

		/** Instance of weather db helper */
		WeatherStation.wsDbHelper = new WSDatabaseHelper(WeatherStation.appContext);
		WeatherStation.dataBase = WeatherStation.wsDbHelper.getWritableDatabase();

		WeatherStation.wsDbHelper.cleanDB(WeatherStation.dataBase);
		// Write restore data to the database
		for (int i=0; i<timeStamp.size(); i++) {
			WeatherStation.wsDbHelper.addDay(WeatherStation.dataBase, timeStamp.get(i), dateStamp.get(i), dayNumber.get(i),
					hourTemp.get(i), hourPress.get(i), hourHumid.get(i),
					maxTemp.get(i), minTemp.get(i), avgTemp.get(i),
					maxPress.get(i), minPress.get(i), avgPress.get(i),
					maxHumid.get(i), minHumid.get(i), avgHumid.get(i));
		}
		WeatherStation.dataBase.close();
		WeatherStation.wsDbHelper.close();
		return false;
	}
}
