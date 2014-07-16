// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.model.Comment;
import com.tomhedges.bamboo.model.ConfigValues;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.PlantType;
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
	private String[] globalsColumns = { Constants.COLUMN_GLOBAL_VERSION, Constants.COLUMN_GLOBAL_ROOT_URL, Constants.COLUMN_LAST_UPDATED };
	private String[] tableUpdatesColumns = { Constants.COLUMN_ID_LOCAL, Constants.COLUMN_TABLES_TABLENAME, Constants.COLUMN_LAST_UPDATED };
	private String[] configColumns = { Constants.COLUMN_ID_LOCAL, Constants.COLUMN_LAST_UPDATED, Constants.COLUMN_CONFIG_ITERATION_DELAY, Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS, Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS, Constants.COLUMN_CONFIG_PLOT_PATTERN };
	private String[] plantTypeColumns = { Constants.COLUMN_ID_LOCAL,
			Constants.COLUMN_PLANTTYPES_TYPE,
			Constants.COLUMN_PLANTTYPES_PREFTEMP,
			Constants.COLUMN_PLANTTYPES_REQWATER,
			Constants.COLUMN_PLANTTYPES_PREFPH,
			Constants.COLUMN_PLANTTYPES_PREFGROUNDSTATE,
			Constants.COLUMN_PLANTTYPES_LIVESFOR,
			Constants.COLUMN_PLANTTYPES_COMFACTOR,
			Constants.COLUMN_PLANTTYPES_MATURES,
			Constants.COLUMN_PLANTTYPES_FLOWTARGET,
			Constants.COLUMN_PLANTTYPES_FLOWFOR,
			Constants.COLUMN_PLANTTYPES_FRUITTARGET,
			Constants.COLUMN_PLANTTYPES_FRUITFOR };
	
	public ConfigDataSource(Context context) {
		dbHelper = new ConfigSQLiteHelper(context);
	}

	public void open() throws SQLException {
		Log.w(Constants.class.getName(), "Open writeable database");
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		Log.w(Constants.class.getName(), "Close database");
		dbHelper.close();
	}

	public Globals getGlobals() {
		Cursor cursor = database.query(Constants.TABLE_GLOBAL_SETTINGS, globalsColumns, Constants.COLUMN_ID_LOCAL + " = 1", null, null, null, null);
		cursor.moveToFirst();
		Globals globals = cursorToGlobals(cursor);
		cursor.close();
		Log.w(Constants.class.getName(), "Retrieved data. Sample: " + globals.getRootURL() + ", " + globals.getLast_updated());
		return globals;
	}

	public TableLastUpdateDates getTableUpdateDates() {
		Cursor cursor = database.query(Constants.TABLE_TABLES, tableUpdatesColumns, null, null, null, null, null);
		cursor.moveToFirst();
		TableLastUpdateDates lastUpdateDates = cursorToLastUpdates(cursor);
		cursor.close();
		Log.w(Constants.class.getName(), "Retrieved data. ConfigValues last update date: " + lastUpdateDates.getConfig());
		return lastUpdateDates;
	}

	public PlantType[] getPlantTypes() {
		Cursor cursor = database.query(Constants.TABLE_PLANT_TYPES, plantTypeColumns, null, null, null, null, null);
		cursor.moveToFirst();
		PlantType[] plantTypes = cursorToPlantTypeArray(cursor);
		cursor.close();
		Log.w(Constants.class.getName(), "Retrieved local plant tyes data for " + plantTypes.length + " plants!");
		
		return plantTypes;
	}

	public ConfigValues getConfigValues() {
		Cursor cursor = database.query(Constants.TABLE_CONFIG, configColumns, Constants.COLUMN_ID_LOCAL + " = 1", null, null, null, null);
		cursor.moveToFirst();
		ConfigValues configValues = cursorToConfig(cursor);
		cursor.close();
		Log.w(Constants.class.getName(), "Retrieved local Config data: " + configValues.toString());
		return configValues;
	}

	// not used
	public Comment createComment(String comment) {
		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_GLOBAL_VERSION, comment);
		long insertId = database.insert(Constants.TABLE_GLOBAL_SETTINGS, null,
				values);
		Cursor cursor = database.query(Constants.TABLE_GLOBAL_SETTINGS,
				globalsColumns, Constants.COLUMN_ID_LOCAL + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Comment newComment = cursorToComment(cursor);
		cursor.close();
		Log.w(Constants.class.getName(), "Create comment '" + comment + "' at index: " + insertId);
		return newComment;
	}


	public boolean writeGlobals(Globals globalsRemote) {
		Log.w(Constants.class.getName(), "Updating local Globals table with more recent remote data");

		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_GLOBAL_VERSION, globalsRemote.getVersion());
		values.put(Constants.COLUMN_GLOBAL_ROOT_URL, globalsRemote.getRootURL());
		DateConverter dateConverter = new DateConverter();
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(globalsRemote.getLast_updated()));

		long updatedNumRows = database.update(Constants.TABLE_GLOBAL_SETTINGS, values, null, null);

		Log.w(Constants.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.w(Constants.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(Constants.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writeTableUpdateDate(String tableName, Date updatedDate) {
		Log.w(Constants.class.getName(), "Attempting to update Tables to show: " + tableName + " updated at " + updatedDate);
		ContentValues values = new ContentValues();
		DateConverter dateConverter = new DateConverter();

		//values.put(Constants.COLUMN_TABLES_TABLENAME, dateConverter.convertDateToString(updatedDate));
		//values.put(Constants.COLUMN_TABLES_TABLENAME, tableName);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(updatedDate));

		long updatedNumRows = database.update(Constants.TABLE_TABLES, values, Constants.COLUMN_TABLES_TABLENAME + " = ?", new String[] {tableName});

		Log.w(Constants.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.w(Constants.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(Constants.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writeConfig(ConfigValues remoteConfigValues) {
		Log.w(Constants.class.getName(), "Updating local Config table with more recent remote data");

		ContentValues values = new ContentValues();
		DateConverter dateConverter = new DateConverter();
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(remoteConfigValues.getLast_updated()));
		values.put(Constants.COLUMN_CONFIG_ITERATION_DELAY, remoteConfigValues.getIteration_time_delay());
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS, remoteConfigValues.getPlot_matrix_columns());
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS, remoteConfigValues.getPlot_matrix_rows());
		values.put(Constants.COLUMN_CONFIG_PLOT_PATTERN, remoteConfigValues.getPlot_pattern());

		long updatedNumRows = database.update(Constants.TABLE_CONFIG, values, null, null);

		Log.w(Constants.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.w(Constants.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(Constants.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writePlantTypes(PlantType[] remotePlantTypes) {
		Log.w(Constants.class.getName(), "Updating local Plant Types table with more recent remote data");
		int deletedRows = database.delete(Constants.TABLE_PLANT_TYPES, null, null);
		Log.w(Constants.class.getName(), "Deleted " + deletedRows + " local plant types.");
		
		int numRowsAdded = 0;
		
		for (int loopCounter = 0; loopCounter<remotePlantTypes.length; loopCounter++) {
			ContentValues values = new ContentValues();
			values.put(Constants.COLUMN_ID_LOCAL, remotePlantTypes[loopCounter].getPlantTypeId());
			values.put(Constants.COLUMN_PLANTTYPES_TYPE, remotePlantTypes[loopCounter].getType());
			values.put(Constants.COLUMN_PLANTTYPES_PREFTEMP, remotePlantTypes[loopCounter].getPreferredTemp());
			values.put(Constants.COLUMN_PLANTTYPES_REQWATER, remotePlantTypes[loopCounter].getRequiredWater());
			values.put(Constants.COLUMN_PLANTTYPES_PREFPH, remotePlantTypes[loopCounter].getPreferredPH());
			values.put(Constants.COLUMN_PLANTTYPES_PREFGROUNDSTATE, remotePlantTypes[loopCounter].getPreferredGroundState().toString());
			values.put(Constants.COLUMN_PLANTTYPES_LIVESFOR, remotePlantTypes[loopCounter].getLivesFor());
			values.put(Constants.COLUMN_PLANTTYPES_COMFACTOR, remotePlantTypes[loopCounter].getCommonnessFactor());
			values.put(Constants.COLUMN_PLANTTYPES_MATURES, remotePlantTypes[loopCounter].getMaturesAtAge());
			values.put(Constants.COLUMN_PLANTTYPES_FLOWTARGET, remotePlantTypes[loopCounter].getFloweringTarget());
			values.put(Constants.COLUMN_PLANTTYPES_FLOWFOR, remotePlantTypes[loopCounter].getFlowersFor());
			values.put(Constants.COLUMN_PLANTTYPES_FRUITTARGET, remotePlantTypes[loopCounter].getFruitingTarget());
			values.put(Constants.COLUMN_PLANTTYPES_FRUITFOR, remotePlantTypes[loopCounter].getFruitsFor());

			long newRowNum = database.insert(Constants.TABLE_PLANT_TYPES, null, values);
			if ((int) newRowNum == -1) {
				Log.e(Constants.class.getName(), "Error inserting new row into Plant Types table");
				return false;
			} else {
				numRowsAdded++;
			}
		}

		Log.w(Constants.class.getName(), "Added " + numRowsAdded + " plants from remote source!");

		return true;
	}

	// not used
	public void deleteComment(Comment comment) {
		long id = comment.getId();
		Log.w(Constants.class.getName(), "Delete comment '" + comment + "' at index: " + id);
		database.delete(Constants.TABLE_GLOBAL_SETTINGS, Constants.COLUMN_ID_LOCAL
				+ " = " + id, null);
	}

	// not used
	public List<Comment> getAllComments() {
		Log.w(Constants.class.getName(), "Get all comments");

		List<Comment> comments = new ArrayList<Comment>();

		Cursor cursor = database.query(Constants.TABLE_GLOBAL_SETTINGS,
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
		Log.w(Constants.class.getName(), "Convert Cursor to Comment");
		Comment comment = new Comment();
		comment.setId(cursor.getLong(0));
		comment.setComment(cursor.getString(1));
		return comment;
	}

	private Globals cursorToGlobals(Cursor cursor) {
		Log.w(Constants.class.getName(), "Convert Cursor to Global");
		Globals globals = new Globals();
		globals.setVersion(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_GLOBAL_VERSION)));
		globals.setRootURL(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_GLOBAL_ROOT_URL)));
		DateConverter dateConverter = new DateConverter();
		globals.setLast_updated(dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_UPDATED))));
		return globals;
	}

	private TableLastUpdateDates cursorToLastUpdates(Cursor cursor) {
		Log.w(Constants.class.getName(), "Convert Cursor to Last Table Updates");
		TableLastUpdateDates lastUpdates = new TableLastUpdateDates();

		DateConverter dateConverter = new DateConverter();
		while (!cursor.isAfterLast()) {
			String table_name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TABLES_TABLENAME));
			Date table_date = dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_UPDATED)));
			Log.w(Constants.class.getName(), "Data for: " + table_name);

			if (table_name.equals(Constants.TABLE_CONFIG)) {
				Log.w(Constants.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setConfig(table_date);
			} else if (table_name.equals(Constants.TABLE_PLANT_TYPES)) {
				Log.w(Constants.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setPlants(table_date);
			} else {
				//ADD IN THE UPDATES FOR OTHER TABLE NAMES!
				Log.e(Constants.class.getName(), "Unknown table!");
			}
			cursor.moveToNext();
		}

		return lastUpdates;
	}

	private ConfigValues cursorToConfig(Cursor cursor) {
		Log.w(Constants.class.getName(), "Convert Cursor to Config values");
		ConfigValues configValues = new ConfigValues();
		DateConverter dateConverter = new DateConverter();
		configValues.setLast_updated(dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_UPDATED))));
		configValues.setIteration_time_delay(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_CONFIG_ITERATION_DELAY)));
		configValues.setPlot_matrix_columns(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS)));
		configValues.setPlot_matrix_rows(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS)));
		configValues.setPlot_pattern(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_CONFIG_PLOT_PATTERN)));

		return configValues;
	}

	private PlantType[] cursorToPlantTypeArray(Cursor cursor) {
		PlantType[] plantTypes = new PlantType[cursor.getCount()];
		cursor.moveToFirst();
		for (int loopCounter = 0; loopCounter < cursor.getCount(); loopCounter++) {
			int id = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID_LOCAL));
			String type = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_TYPE));
			int pref_temp = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_PREFTEMP));
			int req_water = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_REQWATER));
			int pref_ph = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_PREFPH));
			GroundState pref_gs = GroundState.valueOf(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_PREFGROUNDSTATE)));
			int lives_for = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_LIVESFOR));
			int com_fact = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_COMFACTOR));
			int mat_age = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_MATURES));
			int flow_tar = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_FLOWTARGET));
			int flow_for = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_FLOWFOR));
			int fruit_tar = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_FRUITTARGET));
			int fruit_for = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_FRUITFOR));
			
			PlantType plant = new PlantType(id, type, pref_temp, req_water, pref_ph, pref_gs, lives_for, com_fact, mat_age, flow_tar, flow_for, fruit_tar, fruit_for);
			plantTypes[loopCounter] = plant;
			
			cursor.moveToNext();
		}
		return plantTypes;
	}
}
