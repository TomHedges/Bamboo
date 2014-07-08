// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.Comment;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.TableLastUpdateDates;
import com.tomhedges.bamboo.util.DateConverter;
import com.tomhedges.bamboo.util.localdatabase.ConfigSQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ConfigDataSource {

	// Database fields
	private SQLiteDatabase database;
	private ConfigSQLiteHelper dbHelper;
	private String[] globalsColumns = { ConfigSQLiteHelper.COLUMN_GLOBAL_VERSION, ConfigSQLiteHelper.COLUMN_GLOBAL_ROOT_URL, ConfigSQLiteHelper.COLUMN_LAST_UPDATED };
	private String[] tableUpdatesColumns = { ConfigSQLiteHelper.COLUMN_ID, ConfigSQLiteHelper.COLUMN_TABLES_TABLENAME, ConfigSQLiteHelper.COLUMN_LAST_UPDATED };

	public ConfigDataSource(Context context) {
		dbHelper = new ConfigSQLiteHelper(context);
	}

	public void open() throws SQLException {
		Log.w(ConfigSQLiteHelper.class.getName(), "Open writeable database");
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		Log.w(ConfigSQLiteHelper.class.getName(), "Close database");
		dbHelper.close();
	}

	public Globals getGlobals() {
		Cursor cursor = database.query(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS,
				globalsColumns, ConfigSQLiteHelper.COLUMN_ID + " = 1", null,
				null, null, null);
		cursor.moveToFirst();
		Globals globals = cursorToGlobals(cursor);
		cursor.close();
		Log.w(ConfigSQLiteHelper.class.getName(), "Retrieved data. Sample: " + globals.getRootURL() + ", " + globals.getLast_updated());
		return globals;
	}

	public TableLastUpdateDates getTableUpdateDates() {
		Cursor cursor = database.query(ConfigSQLiteHelper.TABLE_TABLES,
				tableUpdatesColumns, null, null, null, null, null);
		cursor.moveToFirst();
		TableLastUpdateDates lastUpdateDates = cursorToLastUpdates(cursor);
		cursor.close();
		Log.w(ConfigSQLiteHelper.class.getName(), "Retrieved data. Config last update date: " + lastUpdateDates.getConfig());
		return lastUpdateDates;
	}

	// not used
	public Comment createComment(String comment) {
		ContentValues values = new ContentValues();
		values.put(ConfigSQLiteHelper.COLUMN_GLOBAL_VERSION, comment);
		long insertId = database.insert(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS, null,
				values);
		Cursor cursor = database.query(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS,
				globalsColumns, ConfigSQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Comment newComment = cursorToComment(cursor);
		cursor.close();
		Log.w(ConfigSQLiteHelper.class.getName(), "Create comment '" + comment + "' at index: " + insertId);
		return newComment;
	}


	public boolean writeGlobals(Globals globalsRemote) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Updating local Globals table with more recent remote data");

		ContentValues values = new ContentValues();
		values.put(ConfigSQLiteHelper.COLUMN_GLOBAL_VERSION, globalsRemote.getVersion());
		values.put(ConfigSQLiteHelper.COLUMN_GLOBAL_ROOT_URL, globalsRemote.getRootURL());
		DateConverter dateConverter = new DateConverter();
		values.put(ConfigSQLiteHelper.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(globalsRemote.getLast_updated()));

		long updateId = database.update(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS, values, null, null);

		if (updateId == 1) {
			Log.w(ConfigSQLiteHelper.class.getName(), "Update successful!");
			return true;
		} else {
			Log.w(ConfigSQLiteHelper.class.getName(), "Update NOT successful!");
			return false;
		}
	}

	// not used
	public void deleteComment(Comment comment) {
		long id = comment.getId();
		Log.w(ConfigSQLiteHelper.class.getName(), "Delete comment '" + comment + "' at index: " + id);
		database.delete(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS, ConfigSQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	// not used
	public List<Comment> getAllComments() {
		Log.w(ConfigSQLiteHelper.class.getName(), "Get all comments");

		List<Comment> comments = new ArrayList<Comment>();

		Cursor cursor = database.query(ConfigSQLiteHelper.TABLE_GLOBAL_SETTINGS,
				globalsColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Comment comment = cursorToComment(cursor);
			comments.add(comment);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return comments;
	}

	// not used
	private Comment cursorToComment(Cursor cursor) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Convert Cursor to Comment");
		Comment comment = new Comment();
		comment.setId(cursor.getLong(0));
		comment.setComment(cursor.getString(1));
		return comment;
	}

	private Globals cursorToGlobals(Cursor cursor) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Convert Cursor to Global");
		Globals globals = new Globals();
		globals.setVersion(cursor.getInt(cursor.getColumnIndex(ConfigSQLiteHelper.COLUMN_GLOBAL_VERSION)));
		globals.setRootURL(cursor.getString(cursor.getColumnIndex(ConfigSQLiteHelper.COLUMN_GLOBAL_ROOT_URL)));
		DateConverter dateConverter = new DateConverter();
		globals.setLast_updated(dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(ConfigSQLiteHelper.COLUMN_LAST_UPDATED))));
		return globals;
	}

	private TableLastUpdateDates cursorToLastUpdates(Cursor cursor) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Convert Cursor to Last Table Updates");
		TableLastUpdateDates lastUpdates = new TableLastUpdateDates();

		DateConverter dateConverter = new DateConverter();
		while (!cursor.isAfterLast()) {
			String table_name = cursor.getString(cursor.getColumnIndex(ConfigSQLiteHelper.COLUMN_TABLES_TABLENAME));

			if (table_name.equals(ConfigSQLiteHelper.TABLE_CONFIG)) {
				lastUpdates.setConfig(dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(ConfigSQLiteHelper.COLUMN_LAST_UPDATED))));
			} else {
				//ADD IN THE UPDATES FOR OTHER TABLE NAMES!
			}
			cursor.moveToNext();
		}

		return lastUpdates;
	}

	// not used
	private String[][] tableData(Cursor cursor) {
		Log.w(ConfigSQLiteHelper.class.getName(), "Converting Cursor to String[][]");
		String[][] tableData = new String [2][cursor.getColumnCount()];
		for (int loopCount = 0; loopCount<cursor.getColumnCount(); loopCount++) {
			tableData[0][loopCount] = cursor.getColumnName(loopCount);
			tableData[1][loopCount] = cursor.getString(loopCount);
		}
		return tableData;
	}
}
