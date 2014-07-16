// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.localdatabase;

import java.util.Date;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.util.DateConverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ConfigSQLiteHelper extends SQLiteOpenHelper {
	//private String[] allColumns = { COLUMN_ID_LOCAL, COLUMN_GLOBAL_VERSION, COLUMN_GLOBAL_ROOT_URL, COLUMN_LAST_UPDATED };

	private static final String DATABASE_NAME = "bamboo.db";
	private static final int DATABASE_VERSION = 18;

	private DateConverter dateConverter;

	// GLOBALS table creation sql statement
	private static final String TABLE_CREATE_GLOBALS = "create table "
		+ Constants.TABLE_GLOBAL_SETTINGS + "(" + Constants.COLUMN_ID_LOCAL
		+ " integer primary key autoincrement, " + Constants.COLUMN_GLOBAL_VERSION
		+ " integer not null, " + Constants.COLUMN_GLOBAL_ROOT_URL
		+ " text not null, " + Constants.COLUMN_LAST_UPDATED
		+ " DATETIME not null);";

	// GLOBALS table creation sql statement
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

	// CONFIG table creation sql statement
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
		+ " integer not null, " + Constants.COLUMN_LAST_UPDATED
		+ " integer, " + Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS
		+ " integer);";

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
		Log.w(ConfigSQLiteHelper.class.getName(),"Created GLOBALS, TABLES, CONFIG and PLANT TYPES tables!");

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
		onCreate(db);
	}

	public void dropTable(SQLiteDatabase db, String tableName) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Dropping");
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}

	public void createTable(SQLiteDatabase db, String tableName) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Dropping");
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
}