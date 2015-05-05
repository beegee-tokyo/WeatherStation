package tk.giesecke.weatherstation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * WSDatabaseHelper
 * database access methods
 *
 * @author Bernd Giesecke
 * @version 0.1 beta May 5, 2015.
 */
class WSDatabaseHelper extends SQLiteOpenHelper {

	/** Name of the database */
	private static final String DATABASE_NAME="WSDatabase";
	/** Name of the table */
	private static final String TABLE_WEATHER = "weather";

	public WSDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL("CREATE TABLE " + TABLE_WEATHER + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"ts INTEGER, ds INTEGER, dn INTEGER, " +
				"t FLOAT, p FLOAT, h FLOAT," +
				"mat FLOAT, mit FLOAT, avt FLOAT," +
				"map FLOAT, mip FLOAT, avp FLOAT," +
				"mah FLOAT, mih FLOAT, avh FLOAT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS "+TABLE_WEATHER);
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

		/** Access to the database */
		//db = this.getWritableDatabase();
		long result = db.insert(TABLE_WEATHER, null, values);
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
		return db.query(TABLE_WEATHER,
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
	public Cursor getAll(SQLiteDatabase db) {
		/** Access to the database */
		//db = this.getReadableDatabase();
		/** Cursor holding all entries of the database */
		Cursor allRows = db.rawQuery("select * from " + TABLE_WEATHER, null);
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
			db.update(TABLE_WEATHER, updateData, "dn=" + dayNumber, null);
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
		db.delete(TABLE_WEATHER, "dn=" + 31, null);
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
		db.execSQL("DELETE FROM " + TABLE_WEATHER); //delete all rows in a table
	}
}
