// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.localdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_COMMENTS = "comments";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_COMMENT = "comment";

	private static final String DATABASE_NAME = "bamboo.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
		+ TABLE_COMMENTS + "(" + COLUMN_ID
		+ " integer primary key autoincrement, " + COLUMN_COMMENT
		+ " text not null);";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.w(MySQLiteHelper.class.getName(),
				"Creating database: " + DATABASE_NAME);
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
		onCreate(db);
	}
}
