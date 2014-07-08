// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.localdatabase;

import java.util.Date;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.Comment;
import com.tomhedges.bamboo.util.DateConverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ConfigSQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_GLOBAL_SETTINGS = "GlobalSettings";
	public static final String TABLE_TABLES = "Tables";
	public static final String TABLE_CONFIG = "Config";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LAST_UPDATED = "last_updated";
	public static final String COLUMN_GLOBAL_VERSION = "version";
	public static final String COLUMN_GLOBAL_ROOT_URL = "root_url";
	public static final String COLUMN_TABLES_TABLENAME = "tablename";
	//private String[] allColumns = { COLUMN_ID, COLUMN_GLOBAL_VERSION, COLUMN_GLOBAL_ROOT_URL, COLUMN_LAST_UPDATED };

	private static final String DATABASE_NAME = "bamboo.db";
	private static final int DATABASE_VERSION = 9;
	
	private DateConverter dateConverter;

	// GLOBALS table creation sql statement
	private static final String TABLE_CREATE_GLOBALS = "create table "
		+ TABLE_GLOBAL_SETTINGS + "(" + COLUMN_ID
		+ " integer primary key autoincrement, " + COLUMN_GLOBAL_VERSION
		+ " integer not null, " + COLUMN_GLOBAL_ROOT_URL
		+ " text not null, " + COLUMN_LAST_UPDATED
		+ " DATETIME not null);";
	
	// GLOBALS table creation sql statement
	private static final String TABLE_CREATE_TABLES = "create table "
		+ TABLE_TABLES + "(" + COLUMN_ID
		+ " integer primary key autoincrement, " + COLUMN_TABLES_TABLENAME
		+ " text not null, " + COLUMN_LAST_UPDATED
		+ " DATETIME not null);";

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
		Log.w(ConfigSQLiteHelper.class.getName(),"Created GLOBALS and TABLES tables!");

		dateConverter = new DateConverter();
		
		Log.w(ConfigSQLiteHelper.class.getName(),"Inserting defaults in GLOBALS table!");
		ContentValues values = new ContentValues();
		values.put(COLUMN_GLOBAL_VERSION, 1);
		values.put(COLUMN_GLOBAL_ROOT_URL, Constants.ROOT_URL);
		values.put(COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		long insertId = database.insert(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS, null, values);
		if (insertId == -1) {
			Log.w(ConfigSQLiteHelper.class.getName(), "ERROR inserting default in GLOBALS");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default in GLOBALS table OK!");
		}

		Log.w(ConfigSQLiteHelper.class.getName(),"Inserting defaults in TABLES table!");
		values = new ContentValues();
		values.put(COLUMN_TABLES_TABLENAME, "Config");
		values.put(COLUMN_LAST_UPDATED, dateConverter.convertDateToString(new Date(0)));
		insertId = database.insert(ConfigSQLiteHelper.TABLE_TABLES, null, values);
		if (insertId == -1) {
			Log.w(ConfigSQLiteHelper.class.getName(), "ERROR inserting default in TABLES");
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Entered default in TABLES table OK!");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ConfigSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GLOBAL_SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TABLES);
		onCreate(db);
	}
}
