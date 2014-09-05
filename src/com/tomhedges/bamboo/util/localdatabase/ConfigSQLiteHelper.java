// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.localdatabase;

import java.util.Date;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.Objective;
import com.tomhedges.bamboo.util.DateConverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ConfigSQLiteHelper extends SQLiteOpenHelper {
	//private String[] allColumns = { COLUMN_ID_LOCAL, COLUMN_GLOBAL_VERSION, COLUMN_GLOBAL_ROOT_URL, COLUMN_LAST_UPDATED };

	private static final String DATABASE_NAME = "bamboo.db";
	private static final int DATABASE_VERSION = 24;

	private DateConverter dateConverter;
	private boolean[] objectiveCompletionStates = null;

	// GLOBALS table creation sql statement
	private static final String TABLE_CREATE_GLOBALS = "create table "
		+ Constants.TABLE_GLOBAL_SETTINGS + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_GLOBAL_VERSION
		+ " integer not null, " + Constants.COLUMN_GLOBAL_ROOT_URL
		+ " text not null, " + Constants.COLUMN_LAST_UPDATED
		+ " DATETIME not null);";

	// TABLES table creation sql statement
	private static final String TABLE_CREATE_TABLES = "create table "
		+ Constants.TABLE_TABLES + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_TABLES_TABLENAME
		+ " text not null, " + Constants.COLUMN_LAST_UPDATED
		+ " DATETIME not null);";

	// CONFIG table creation sql statement
	private static final String TABLE_CREATE_CONFIG = "create table "
		+ Constants.TABLE_CONFIG + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_LAST_UPDATED
		+ " DATETIME not null, " + Constants.COLUMN_CONFIG_ITERATION_DELAY
		+ " integer not null, " + Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS
		+ " integer not null, " + Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS
		+ " integer not null, " + Constants.COLUMN_CONFIG_PLOT_PATTERN
		+ " text not null);";

	// PLANTTYPES table creation sql statement
	private static final String TABLE_CREATE_PLANTTYPES = "create table "
		+ Constants.TABLE_PLANT_TYPES + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key, " + Constants.COLUMN_PLANTTYPES_TYPE
		+ " text not null, " + Constants.COLUMN_PLANTTYPES_PREFTEMP
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_REQWATER
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_PREFPH
		+ " DOUBLE not null, " + Constants.COLUMN_PLANTTYPES_PREFGROUNDSTATE
		+ " TEXT not null, " + Constants.COLUMN_PLANTTYPES_LIVESFOR
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_COMFACTOR
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_MATURES
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_FLOWTARGET
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_FLOWFOR
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_FRUITTARGET
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_FRUITFOR
		+ " integer not null, " + Constants.COLUMN_PLANTTYPES_PHOTO
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_GROWING
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_WILTING
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_IMAGE_DEAD
		+ " TEXT, " + Constants.COLUMN_PLANTTYPES_SIZE_MAX
		+ " INTEGER, " + Constants.COLUMN_PLANTTYPES_SIZE_GROWTH_RATE
		+ " INTEGER, " + Constants.COLUMN_PLANTTYPES_SIZE_SHRINK_RATE
		+ " INTEGER);";

	// OBJECTIVES table creation sql statement
	private static final String TABLE_CREATE_OBJECTIVES = "create table "
		+ Constants.TABLE_OBJECTIVES + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_OBJECTIVES_ID
		+ " integer not null, " + Constants.COLUMN_OBJECTIVES_DESC
		+ " text not null, " + Constants.COLUMN_OBJECTIVES_MESSAGE
		+ " text not null, " + Constants.COLUMN_OBJECTIVES_COMPLETED
		+ " boolean not null);";

	// HELPANDINFO table creation sql statement
	private static final String TABLE_CREATE_HELPANDINFO = "create table "
		+ Constants.TABLE_HELPANDINFO + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key, " + Constants.COLUMN_HELPANDINFO_DATATYPE
		+ " text not null, " + Constants.COLUMN_HELPANDINFO_REFERENCE
		+ " text not null, " + Constants.COLUMN_HELPANDINFO_TEXT
		+ " text not null);";

	// SPONSORED_PLANTS_UNLOCKED table creation sql statement
	private static final String TABLE_CREATE_SPONSORED_PLANTS_UNLOCKED = "create table "
		+ Constants.TABLE_SPONSORED_PLANTS_UNLOCKED + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_TIMESTAMP
		+ " DATETIME not null, " + Constants.TAG_USERNAME
		+ " text not null, " + Constants.COLUMN_MESSAGE
		+ " text not null, " + Constants.COLUMN_SUCCESS_COPY
		+ " text not null);";

	public ConfigSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating database: " + DATABASE_NAME);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating GLOBALS table: " + TABLE_CREATE_GLOBALS);
		database.execSQL(TABLE_CREATE_GLOBALS);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating TABLES table: " + TABLE_CREATE_TABLES);
		database.execSQL(TABLE_CREATE_TABLES);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating CONFIG table: " + TABLE_CREATE_CONFIG);
		database.execSQL(TABLE_CREATE_CONFIG);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating PLANT TYPES table: " + TABLE_CREATE_PLANTTYPES);
		database.execSQL(TABLE_CREATE_PLANTTYPES);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating OBJECTIVES table: " + TABLE_CREATE_OBJECTIVES);
		database.execSQL(TABLE_CREATE_OBJECTIVES);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating HELPANDINFO table: " + TABLE_CREATE_HELPANDINFO);
		database.execSQL(TABLE_CREATE_HELPANDINFO);
		Log.w(ConfigSQLiteHelper.class.getName(),"Creating SPONSORED_PLANTS_UNLOCKED table: " + TABLE_CREATE_SPONSORED_PLANTS_UNLOCKED);
		database.execSQL(TABLE_CREATE_SPONSORED_PLANTS_UNLOCKED);
		
		Log.w(ConfigSQLiteHelper.class.getName(),"Created GLOBALS, TABLES, CONFIG, PLANT TYPES, OBJECTIVES and HELPANDINFO tables!");

		dateConverter = new DateConverter();

		//--------------------------------------------------
		
		Log.w(ConfigSQLiteHelper.class.getName(),"Inserting defaults in GLOBALS table!");
		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_GLOBAL_VERSION, 1);
		values.put(Constants.COLUMN_GLOBAL_ROOT_URL, Constants.ROOT_URL);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		long insertId = database.insert(Constants.TABLE_GLOBAL_SETTINGS, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting default in GLOBALS");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default in GLOBALS table OK!");
		}

		//--------------------------------------------------

		Log.w(ConfigSQLiteHelper.class.getName(),"Inserting defaults in TABLES table!");
		values = new ContentValues();
		values.put(Constants.COLUMN_TABLES_TABLENAME, Constants.TABLE_CONFIG);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(Constants.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting " + Constants.TABLE_CONFIG + " default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default " + Constants.TABLE_CONFIG + " in TABLES table OK!");
		}

		values = new ContentValues();
		values.put(Constants.COLUMN_TABLES_TABLENAME, Constants.TABLE_PLANT_TYPES);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(Constants.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting " + Constants.TABLE_PLANT_TYPES + " default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default " + Constants.TABLE_PLANT_TYPES + " in TABLES table OK!");
		}

		values = new ContentValues();
		values.put(Constants.COLUMN_TABLES_TABLENAME, Constants.TABLE_OBJECTIVES);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(Constants.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting " + Constants.TABLE_OBJECTIVES + " default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default " + Constants.TABLE_OBJECTIVES + " in TABLES table OK!");
		}

		values = new ContentValues();
		values.put(Constants.COLUMN_TABLES_TABLENAME, Constants.TABLE_ITERATION_RULES);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(Constants.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting " + Constants.TABLE_ITERATION_RULES + " default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default " + Constants.TABLE_ITERATION_RULES + " in TABLES table OK!");
		}

		values = new ContentValues();
		values.put(Constants.COLUMN_TABLES_TABLENAME, Constants.TABLE_HELPANDINFO);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(Constants.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting " + Constants.TABLE_HELPANDINFO + " default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default " + Constants.TABLE_HELPANDINFO + " in TABLES table OK!");
		}

		//--------------------------------------------------

		Log.w(ConfigSQLiteHelper.class.getName(),"Inserting defaults in CONFIG table!");
		values = new ContentValues();
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		values.put(Constants.COLUMN_CONFIG_ITERATION_DELAY, 1000);
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS, 1);
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS, 1);
		values.put(Constants.COLUMN_CONFIG_PLOT_PATTERN, "{\"" + Constants.COLUMN_CONFIG_PLOT_PATTERN + "\":[\"" + Constants.GroundState.SOIL.toString() + "\"]}");
		insertId = database.insert(Constants.TABLE_CONFIG, null, values);
		if (insertId == -1) {
			Log.e(ConfigSQLiteHelper.class.getName(), "ERROR inserting default in CONFIG");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default in CONFIG table OK!");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ConfigSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data in selected tables");
		dropTable(db, Constants.TABLE_GLOBAL_SETTINGS);
		dropTable(db, Constants.TABLE_TABLES);
		dropTable(db, Constants.TABLE_CONFIG);
		dropTable(db, Constants.TABLE_PLANT_TYPES);
		objectiveCompletionStates = retrieveObjectiveStates(db);
		dropTable(db, Constants.TABLE_OBJECTIVES);
		dropTable(db, Constants.TABLE_HELPANDINFO);
		dropTable(db, Constants.TABLE_SPONSORED_PLANTS_UNLOCKED); //should add a way to preserve these on update...
		onCreate(db);
	}

	private boolean[] retrieveObjectiveStates(SQLiteDatabase db) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Retrieveing and storing current objective completion statuses...");		
		Cursor cursor = db.query(Constants.TABLE_OBJECTIVES, null, null, null, null, null, null);
		boolean[] objCompStates = new boolean[cursor.getCount()];
		cursor.moveToFirst();
		// maybe not... start from 1 to remove the test objective...
		for (int loopCounter = 0; loopCounter < cursor.getCount(); loopCounter++) {
			int completed = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_OBJECTIVES_COMPLETED));
			if (completed == 1) {
				objCompStates[loopCounter] = true;
			} else {
				objCompStates[loopCounter] = false;
			}
			cursor.moveToNext();
		}
		return objCompStates;
	}

	private void dropTable(SQLiteDatabase db, String tableName) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Dropping");
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}

	public boolean[] getObjectiveCompletionStates() {
		return objectiveCompletionStates;
	}
	
	public void clearObjectiveCompletionStates() {
		objectiveCompletionStates = null;
	}

//	private void createTable(SQLiteDatabase db, String tableName) {
//		Log.w(ConfigSQLiteHelper.class.getName(), "Dropping");
//		db.execSQL("DROP TABLE IF EXISTS " + tableName);
//	}
}