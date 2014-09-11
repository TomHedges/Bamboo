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
import com.tomhedges.bamboo.model.Objective;
import com.tomhedges.bamboo.model.PlantType;
import com.tomhedges.bamboo.model.TableLastUpdateDates;
import com.tomhedges.bamboo.util.DateConverter;
import com.tomhedges.bamboo.util.localdatabase.LocalDBSQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocalDBDataRetrieval {

	// Database fields
	private SQLiteDatabase database;
	private LocalDBSQLiteHelper dbHelper;
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
			Constants.COLUMN_PLANTTYPES_FRUITFOR,
			Constants.COLUMN_PLANTTYPES_PHOTO,
			Constants.COLUMN_PLANTTYPES_IMAGE_GROWING,
			Constants.COLUMN_PLANTTYPES_IMAGE_WILTING,
			Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING,
			Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING,
			Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY,
			Constants.COLUMN_PLANTTYPES_IMAGE_DEAD,
			Constants.COLUMN_PLANTTYPES_SIZE_MAX,
			Constants.COLUMN_PLANTTYPES_SIZE_GROWTH_RATE,
			Constants.COLUMN_PLANTTYPES_SIZE_SHRINK_RATE };
	private String[] plantImageFileColumns = { Constants.COLUMN_PLANTTYPES_PHOTO,
			Constants.COLUMN_PLANTTYPES_IMAGE_GROWING,
			Constants.COLUMN_PLANTTYPES_IMAGE_WILTING,
			Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING,
			Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING,
			Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY,
			Constants.COLUMN_PLANTTYPES_IMAGE_DEAD };
	private String[] objectivesColumns = { Constants.COLUMN_ID_LOCAL, Constants.COLUMN_OBJECTIVES_ID, Constants.COLUMN_OBJECTIVES_DESC, Constants.COLUMN_OBJECTIVES_MESSAGE, Constants.COLUMN_OBJECTIVES_COMPLETED };
	private String[] helpAndInfoColumns = { Constants.COLUMN_ID_LOCAL, Constants.COLUMN_HELPANDINFO_DATATYPE, Constants.COLUMN_HELPANDINFO_REFERENCE, Constants.COLUMN_HELPANDINFO_TEXT };
	private String[] seedUnlockedColumns = { Constants.COLUMN_ID_LOCAL, Constants.COLUMN_TIMESTAMP, Constants.TAG_USERNAME, Constants.COLUMN_MESSAGE, Constants.COLUMN_SUCCESS_COPY };

	public LocalDBDataRetrieval(Context context) {
		dbHelper = new LocalDBSQLiteHelper(context);
	}

	public void open() throws SQLException {
		Log.d(LocalDBDataRetrieval.class.getName(), "Open writeable database");
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		Log.d(LocalDBDataRetrieval.class.getName(), "Close database");
		dbHelper.close();
	}

	public Globals getGlobals() {
		Cursor cursor = database.query(Constants.TABLE_GLOBAL_SETTINGS, globalsColumns, Constants.COLUMN_ID_LOCAL + " = 1", null, null, null, null);
		cursor.moveToFirst();
		Globals globals = cursorToGlobals(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved data. Sample: " + globals.getRootURL() + ", " + globals.getLast_updated());
		return globals;
	}

	public TableLastUpdateDates getTableUpdateDates() {
		Cursor cursor = database.query(Constants.TABLE_TABLES, tableUpdatesColumns, null, null, null, null, null);
		cursor.moveToFirst();
		TableLastUpdateDates lastUpdateDates = cursorToLastUpdates(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved data. ConfigValues last update date: " + lastUpdateDates.getConfig());
		return lastUpdateDates;
	}

	public PlantType[] getPlantTypes() {
		Cursor cursor = database.query(Constants.TABLE_PLANT_TYPES, plantTypeColumns, null, null, null, null, Constants.COLUMN_PLANTTYPES_TYPE + " ASC");
		cursor.moveToFirst();
		PlantType[] plantTypes = cursorToPlantTypeArray(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved local plant tyes data for " + plantTypes.length + " plants!");

		return plantTypes;
	}

	public String[] getPlantImagePaths() {
		Cursor cursor = database.query(Constants.TABLE_PLANT_TYPES, plantImageFileColumns, null, null, null, null, null);
		cursor.moveToFirst();
		String[] imageFiles = cursorToPlantImageFileArray(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved list of " + imageFiles.length + " plant images!");

		return imageFiles;
	}

	public Objective[] getObjectives() {
		Cursor cursor = database.query(Constants.TABLE_OBJECTIVES, objectivesColumns, null, null, null, null, null);
		cursor.moveToFirst();
		Objective[] objectivesArray = cursorToObjectiveArray(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved local " + objectivesArray.length + " objectives!");

		return objectivesArray;
	}

	public String[][] getUnlockedSeeds() {
		Log.d(LocalDBDataRetrieval.class.getName(), "Retriving unlocked sponsored seeds!");
		Cursor cursor = database.query(Constants.TABLE_SPONSORED_PLANTS_UNLOCKED, seedUnlockedColumns, null, null, null, null, Constants.COLUMN_ID_LOCAL + " DESC");
		cursor.moveToFirst();
		String[][] unlockedSeedArray = cursorToUnlockedSeedArray(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved " + unlockedSeedArray.length + " unlocked sponsored seeds!");

		return unlockedSeedArray;
	}

	public String getHelpAndInfo(String dataType, String reference) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieving HAI data for dataType=" + dataType + ", reference=" + reference);

		String selection = Constants.COLUMN_HELPANDINFO_DATATYPE + " = ? AND " + Constants.COLUMN_HELPANDINFO_REFERENCE + " = ?";
		String[] selectionArgs = { dataType, reference };

		Cursor cursor = database.query(Constants.TABLE_HELPANDINFO, helpAndInfoColumns, selection, selectionArgs, null, null, null);
		String helpOrInfoText = cursorToHelpAndInfoText(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved HAI data: " + helpOrInfoText);

		return helpOrInfoText;
	}

	public ConfigValues getConfigValues() {
		Cursor cursor = database.query(Constants.TABLE_CONFIG, configColumns, Constants.COLUMN_ID_LOCAL + " = 1", null, null, null, null);
		cursor.moveToFirst();
		ConfigValues configValues = cursorToConfig(cursor);
		cursor.close();
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved local Config data: " + configValues.toString());
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
		Log.d(LocalDBDataRetrieval.class.getName(), "Create comment '" + comment + "' at index: " + insertId);
		return newComment;
	}


	public boolean writeGlobals(Globals globalsRemote) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating local Globals table with more recent remote data");

		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_GLOBAL_VERSION, globalsRemote.getVersion());
		values.put(Constants.COLUMN_GLOBAL_ROOT_URL, globalsRemote.getRootURL());
		DateConverter dateConverter = new DateConverter();
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(globalsRemote.getLast_updated()));

		long updatedNumRows = database.update(Constants.TABLE_GLOBAL_SETTINGS, values, null, null);

		Log.d(LocalDBDataRetrieval.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writeTableUpdateDate(String tableName, Date updatedDate) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Attempting to update Tables to show: " + tableName + " updated at " + updatedDate);
		ContentValues values = new ContentValues();
		DateConverter dateConverter = new DateConverter();

		//values.put(Constants.COLUMN_TABLES_TABLENAME, dateConverter.convertDateToString(updatedDate));
		//values.put(Constants.COLUMN_TABLES_TABLENAME, tableName);
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(updatedDate));

		long updatedNumRows = database.update(Constants.TABLE_TABLES, values, Constants.COLUMN_TABLES_TABLENAME + " = ?", new String[] {tableName});

		Log.d(LocalDBDataRetrieval.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writeConfig(ConfigValues remoteConfigValues) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating local Config table with more recent remote data");

		ContentValues values = new ContentValues();
		DateConverter dateConverter = new DateConverter();
		values.put(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(remoteConfigValues.getLast_updated()));
		values.put(Constants.COLUMN_CONFIG_ITERATION_DELAY, remoteConfigValues.getIteration_time_delay());
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS, remoteConfigValues.getPlot_matrix_columns());
		values.put(Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS, remoteConfigValues.getPlot_matrix_rows());
		values.put(Constants.COLUMN_CONFIG_PLOT_PATTERN, remoteConfigValues.getPlot_pattern());

		long updatedNumRows = database.update(Constants.TABLE_CONFIG, values, null, null);

		Log.d(LocalDBDataRetrieval.class.getName(), "Update occured! Changes to " + updatedNumRows + " rows.");

		if (updatedNumRows == 1) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Updated 1 row as expected!");
			return true;
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Update effected wrong number of rows!");
			return false;
		}
	}

	public boolean writePlantTypes(PlantType[] remotePlantTypes) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating local Plant Types table with more recent remote data");
		int deletedRows = database.delete(Constants.TABLE_PLANT_TYPES, null, null);
		Log.d(LocalDBDataRetrieval.class.getName(), "Deleted " + deletedRows + " local plant types.");

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
			values.put(Constants.COLUMN_PLANTTYPES_PHOTO, remotePlantTypes[loopCounter].getPhoto());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_GROWING, remotePlantTypes[loopCounter].getImageGrowing());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_WILTING, remotePlantTypes[loopCounter].getImageWilting());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING, remotePlantTypes[loopCounter].getImageFlowering());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING, remotePlantTypes[loopCounter].getImageFruiting());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY, remotePlantTypes[loopCounter].getImageChilly());
			values.put(Constants.COLUMN_PLANTTYPES_IMAGE_DEAD, remotePlantTypes[loopCounter].getImageDead());
			values.put(Constants.COLUMN_PLANTTYPES_SIZE_MAX, remotePlantTypes[loopCounter].getSizeMax());
			values.put(Constants.COLUMN_PLANTTYPES_SIZE_GROWTH_RATE, remotePlantTypes[loopCounter].getSizeGrowthRate());
			values.put(Constants.COLUMN_PLANTTYPES_SIZE_SHRINK_RATE, remotePlantTypes[loopCounter].getSizeShrinkRate());

			long newRowNum = database.insert(Constants.TABLE_PLANT_TYPES, null, values);
			if ((int) newRowNum == -1) {
				Log.e(LocalDBDataRetrieval.class.getName(), "Error inserting new row into Plant Types table");
				return false;
			} else {
				numRowsAdded++;
			}
		}

		Log.d(LocalDBDataRetrieval.class.getName(), "Added " + numRowsAdded + " plants from remote source!");

		return true;
	}

	public boolean writeHelpAndInfoData(String[][] remoteHAIdata) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating local HelpAndInfo table with more recent remote data");
		int deletedRows = database.delete(Constants.TABLE_HELPANDINFO, null, null);
		Log.d(LocalDBDataRetrieval.class.getName(), "Deleted " + deletedRows + " local help and info entries.");

		int numRowsAdded = 0;

		for (int loopCounter = 0; loopCounter<remoteHAIdata.length; loopCounter++) {
			ContentValues values = new ContentValues();
			values.put(Constants.COLUMN_ID_LOCAL, remoteHAIdata[loopCounter][0]);
			values.put(Constants.COLUMN_HELPANDINFO_DATATYPE, remoteHAIdata[loopCounter][1]);
			values.put(Constants.COLUMN_HELPANDINFO_REFERENCE, remoteHAIdata[loopCounter][2]);
			values.put(Constants.COLUMN_HELPANDINFO_TEXT, remoteHAIdata[loopCounter][3]);

			long newRowNum = database.insert(Constants.TABLE_HELPANDINFO, null, values);
			if ((int) newRowNum == -1) {
				Log.e(LocalDBDataRetrieval.class.getName(), "Error inserting new row into Help and Info table");
				return false;
			} else {
				numRowsAdded++;
			}
		}

		Log.d(LocalDBDataRetrieval.class.getName(), "Added " + numRowsAdded + " Help and Info entries from remote source!");

		return true;
	}

	public boolean writeNewSponsoredPlantUnlocked(Date whenUnlocked, String originUsername, String sponsoredMessage, String successCopy) {
		// TODO Auto-generated method stub
		Log.d(LocalDBDataRetrieval.class.getName(), "Checking whether newly unlocked seed is already in database. Data to check: username=" + originUsername + ", successCopy=" + successCopy);

		String selection = Constants.TAG_USERNAME + " = ? AND " + Constants.COLUMN_SUCCESS_COPY + " = ?";
		String[] selectionArgs = { originUsername, successCopy };
		Cursor cursor = database.query(Constants.TABLE_SPONSORED_PLANTS_UNLOCKED, seedUnlockedColumns, selection, selectionArgs, null, null, null);

		if (cursor.getCount()>0) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Already in database! Need to do nothing further. Data checked: username=" + originUsername + ", successCopy=" + successCopy);
			return true;
		} else {
			Log.d(LocalDBDataRetrieval.class.getName(), "Unlocked seed not yet in database - adding new entry...");

			ContentValues values = new ContentValues();
			values.put(Constants.COLUMN_TIMESTAMP, new DateConverter().convertDateToString(whenUnlocked));
			values.put(Constants.TAG_USERNAME, originUsername);
			values.put(Constants.COLUMN_MESSAGE, sponsoredMessage);
			values.put(Constants.COLUMN_SUCCESS_COPY, successCopy);

			long newRowNum = database.insert(Constants.TABLE_SPONSORED_PLANTS_UNLOCKED, null, values);
			if ((int) newRowNum == -1) {
				Log.e(LocalDBDataRetrieval.class.getName(), "Error inserting new row into Sponsored Plant Unlocked table");
				return false;
			} else {
				Log.d(LocalDBDataRetrieval.class.getName(), "Added new entry to the Sponsored Plant Unlocked table! ID=" + newRowNum);
				return true;
			}
		}
	}

	public boolean writeObjectives(Objective[] remoteObjectives) {
		// TODO - Need to do something about preserving existing values when updating the objectives table
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating local Objectives table with more recent remote data");

		Objective[] existingObjectives = getObjectives();
		boolean[] archivedObjectiveStates = dbHelper.getObjectiveCompletionStates();
		boolean[] completionStatuses = null;

		if (existingObjectives.length==0 && archivedObjectiveStates!=null) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Using archived objective statuses from berore DB upgrade");
			completionStatuses = archivedObjectiveStates;
			dbHelper.clearObjectiveCompletionStates();
		} else if (existingObjectives.length!=0 && archivedObjectiveStates==null){
			Log.d(LocalDBDataRetrieval.class.getName(), "Using current completion statuses as updating list");
			completionStatuses = new boolean[existingObjectives.length];
			for (int loopCounter = 0; loopCounter<existingObjectives.length; loopCounter++) {
				if (existingObjectives[loopCounter].isCompleted() == true) {
					completionStatuses[loopCounter] = true;
				} else {
					completionStatuses[loopCounter] = false;
				}
			}
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Unknown objective update state - wiping completion history");
		}

		int deletedRows = database.delete(Constants.TABLE_OBJECTIVES, null, null);
		Log.d(LocalDBDataRetrieval.class.getName(), "Deleted " + deletedRows + " local objectives.");

		int numRowsAdded = 0;

		for (int loopCounter = 0; loopCounter<remoteObjectives.length; loopCounter++) {
			ContentValues values = new ContentValues();
			values.put(Constants.COLUMN_OBJECTIVES_ID, remoteObjectives[loopCounter].getID());
			values.put(Constants.COLUMN_OBJECTIVES_DESC, remoteObjectives[loopCounter].getDescription());
			values.put(Constants.COLUMN_OBJECTIVES_MESSAGE, remoteObjectives[loopCounter].getCompletionMessage());
			boolean completed;
			//first clause handles test objective
			if (remoteObjectives[loopCounter].getID() == 0 || (completionStatuses != null && loopCounter < completionStatuses.length && completionStatuses[loopCounter] == true)) {
				completed = true;
			} else {
				completed = false;
			}
			values.put(Constants.COLUMN_OBJECTIVES_COMPLETED, completed);

			long newRowNum = database.insert(Constants.TABLE_OBJECTIVES, null, values);
			if ((int) newRowNum == -1) {
				Log.e(LocalDBDataRetrieval.class.getName(), "Error inserting new row into Objectives table");
				return false;
			} else {
				numRowsAdded++;
			}
		}

		Log.d(LocalDBDataRetrieval.class.getName(), "Added " + numRowsAdded + " objectives from remote source!");

		return true;
	}

	public boolean updateObjective(Objective toUpdate, boolean completed) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Updating objective " + toUpdate.getID() + " to " + completed);

		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_OBJECTIVES_COMPLETED, completed);
		String whereClause = Constants.COLUMN_OBJECTIVES_ID + " = ?";
		String[] whereArgs = { "" + toUpdate.getID() };

		Log.d(LocalDBDataRetrieval.class.getName(), "Update with whereClause='" + whereClause + "',  whereArgs='" + whereArgs[0] + "'");
		int rowsAffected = database.update(Constants.TABLE_OBJECTIVES, values, whereClause, whereArgs);	

		if (rowsAffected == 1) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Updated one row, as expected!");
			return true;
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Error updating objective.");
			return false;
		}
	}

	public boolean resetObjectiveCompletionStatuses() {
		// TODO Auto-generated method stub
		Log.d(LocalDBDataRetrieval.class.getName(), "Reseting objective completion statuses to FALSE");

		Cursor cursor = database.query(Constants.TABLE_OBJECTIVES, objectivesColumns, null, null, null, null, null);
		int numObjectives = cursor.getCount();

		//handles a 0-value first test objective
		ContentValues values = new ContentValues();
		values.put(Constants.COLUMN_OBJECTIVES_COMPLETED, false);
		String whereClause = Constants.COLUMN_OBJECTIVES_ID + " > ?";
		String[] whereArgs = { "0" };
		int numSetFalse = database.update(Constants.TABLE_OBJECTIVES, values, whereClause, whereArgs);

		if (numObjectives == (numSetFalse+1)) {
			Log.d(LocalDBDataRetrieval.class.getName(), "Set " + numSetFalse + " objective(s) to FALSE, as expected!");
			return true;
		} else {
			Log.e(LocalDBDataRetrieval.class.getName(), "Set " + numSetFalse + " objective(s) to FALSE, but expected " + (numObjectives - 1));
			return false;
		}
	}

	// not used
	public void deleteComment(Comment comment) {
		long id = comment.getId();
		Log.d(LocalDBDataRetrieval.class.getName(), "Delete comment '" + comment + "' at index: " + id);
		database.delete(Constants.TABLE_GLOBAL_SETTINGS, Constants.COLUMN_ID_LOCAL
				+ " = " + id, null);
	}

	// not used
	public List<Comment> getAllComments() {
		Log.d(LocalDBDataRetrieval.class.getName(), "Get all comments");

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
		Log.d(LocalDBDataRetrieval.class.getName(), "Convert Cursor to Comment");
		Comment comment = new Comment();
		comment.setId(cursor.getLong(0));
		comment.setComment(cursor.getString(1));
		return comment;
	}

	private Globals cursorToGlobals(Cursor cursor) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Convert Cursor to Global");
		Globals globals = new Globals();
		globals.setVersion(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_GLOBAL_VERSION)));
		globals.setRootURL(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_GLOBAL_ROOT_URL)));
		DateConverter dateConverter = new DateConverter();
		globals.setLast_updated(dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_UPDATED))));
		return globals;
	}

	private TableLastUpdateDates cursorToLastUpdates(Cursor cursor) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Convert Cursor to Last Table Updates");
		TableLastUpdateDates lastUpdates = new TableLastUpdateDates();

		DateConverter dateConverter = new DateConverter();
		while (!cursor.isAfterLast()) {
			String table_name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TABLES_TABLENAME));
			Date table_date = dateConverter.convertStringToDate(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_UPDATED)));
			Log.d(LocalDBDataRetrieval.class.getName(), "Data for: " + table_name);

			if (table_name.equals(Constants.TABLE_CONFIG)) {
				Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setConfig(table_date);
			} else if (table_name.equals(Constants.TABLE_PLANT_TYPES)) {
				Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setPlants(table_date);
			} else if (table_name.equals(Constants.TABLE_OBJECTIVES)) {
				Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setObjectives(table_date);
			} else if (table_name.equals(Constants.TABLE_ITERATION_RULES)) {
				Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setIterationRules(table_date);
			} else if (table_name.equals(Constants.TABLE_HELPANDINFO)) {
				Log.d(LocalDBDataRetrieval.class.getName(), "Retrieved last update for: " + table_name + ", last updated: " + table_date.toString());
				lastUpdates.setHelpAndInfo(table_date);
			} else {
				//ADD IN THE UPDATES FOR OTHER TABLE NAMES if necessary!
				Log.e(LocalDBDataRetrieval.class.getName(), "Unknown table!");
			}
			cursor.moveToNext();
		}

		return lastUpdates;
	}

	private ConfigValues cursorToConfig(Cursor cursor) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Convert Cursor to Config values");
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
			String photoPath = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_PHOTO));
			String imgGrowing = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_GROWING));
			String imgWilting = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_WILTING));
			String imgFlowering = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING));
			String imgFruiting = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING));
			String imgChilly = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY));
			String imgDead = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_DEAD));
			int size_max = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_SIZE_MAX));
			int size_growth_rate = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_SIZE_GROWTH_RATE));
			int size_shrink_rate = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_SIZE_SHRINK_RATE));

			PlantType plant = new PlantType(id, type, pref_temp, req_water, pref_ph, pref_gs, lives_for, com_fact, mat_age,
					flow_tar, flow_for, fruit_tar, fruit_for, photoPath, imgGrowing, imgWilting, imgFlowering, imgFruiting,
					imgChilly, imgDead, size_max, size_growth_rate, size_shrink_rate);
			plantTypes[loopCounter] = plant;

			cursor.moveToNext();
		}
		return plantTypes;
	}

	private String[] cursorToPlantImageFileArray(Cursor cursor) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Retrieving plant image file names from cursor...");
		String[] plantImageFiles = new String[cursor.getCount() * plantImageFileColumns.length];
		cursor.moveToFirst();
		for (int loopCounter = 0; loopCounter < plantImageFiles.length; loopCounter++) {
			String photoPath = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_PHOTO));
			String imgGrowing = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_GROWING));
			String imgWilting = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_WILTING));
			String imgFlowering = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING));
			String imgFruiting = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING));
			String imgChilly = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY));
			String imgDead = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PLANTTYPES_IMAGE_DEAD));

			plantImageFiles[loopCounter] = photoPath;
			plantImageFiles[loopCounter+1] = imgGrowing;
			plantImageFiles[loopCounter+2] = imgWilting;
			plantImageFiles[loopCounter+3] = imgFlowering;
			plantImageFiles[loopCounter+4] = imgFruiting;
			plantImageFiles[loopCounter+5] = imgChilly;
			plantImageFiles[loopCounter+6] = imgDead;

			loopCounter = loopCounter + 6;
			cursor.moveToNext();
		}
		return plantImageFiles;
	}

	private Objective[] cursorToObjectiveArray(Cursor cursor) {
		Objective[] objectiveArray = new Objective[cursor.getCount()];
		cursor.moveToFirst();
		// maybe not... start from 1 to remove the test objective...
		for (int loopCounter = 0; loopCounter < cursor.getCount(); loopCounter++) {
			int objectiveID = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_OBJECTIVES_ID));
			String description = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_OBJECTIVES_DESC));
			String message = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_OBJECTIVES_MESSAGE));
			int completed = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_OBJECTIVES_COMPLETED));
			boolean completedBoolean;
			if (completed == 1) {
				completedBoolean = true;
			} else {
				completedBoolean = false;
			}

			//Log.d(LocalDBDataRetrieval.class.getName(), "TEST: " + completed + " or " + completedBoolean);
			Objective objective = new Objective(objectiveID, description, message, completedBoolean);
			objectiveArray[loopCounter] = objective;

			cursor.moveToNext();
		}
		return objectiveArray;
	}

	private String[][] cursorToUnlockedSeedArray(Cursor cursor) {
		String[][] objectiveArray = new String[cursor.getCount()][4];
		cursor.moveToFirst();
		for (int loopCounter = 0; loopCounter < cursor.getCount(); loopCounter++) {
			//DateConverter dateConverter = new DateConverter();
			objectiveArray[loopCounter][0] = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIMESTAMP));
			objectiveArray[loopCounter][1] = cursor.getString(cursor.getColumnIndex(Constants.TAG_USERNAME));
			objectiveArray[loopCounter][2] = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_MESSAGE));
			objectiveArray[loopCounter][3] = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SUCCESS_COPY));

			cursor.moveToNext();
		}
		return objectiveArray;
	}

	private String cursorToHelpAndInfoText(Cursor cursor) {
		Log.d(LocalDBDataRetrieval.class.getName(), "Convert Cursor to HelpAndInfo value");
		String textToReturn;

		switch (cursor.getCount()) {
		case 0:
			textToReturn = "No data available!";
			break;
		case 1:
			cursor.moveToFirst();
			textToReturn = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_HELPANDINFO_TEXT));
			break;
		default:
			textToReturn = "Multiple data items - source needs de-duping!";
			break;
		}

		return textToReturn;
	}
}
