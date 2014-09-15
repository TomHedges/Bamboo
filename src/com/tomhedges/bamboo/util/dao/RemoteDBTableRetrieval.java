package com.tomhedges.bamboo.util.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.model.ConfigValues;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.Objective;
import com.tomhedges.bamboo.model.PlantType;
import com.tomhedges.bamboo.model.RemoteSeed;
import com.tomhedges.bamboo.model.TableLastUpdateDates;
import com.tomhedges.bamboo.util.DateConverter;
import com.tomhedges.bamboo.util.FileDownloader;
import com.tomhedges.bamboo.util.JSONParser;

/**
 * Uses the JSONParser class to retrieve data form the remote database, and then construct objects for return to the Game.
 * 
 * Incorporates code sourced from:  http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/
 * 
 * @see			Game
 * @author      Tom Hedges
 */

public class RemoteDBTableRetrieval {

	// JSON parser class
	private JSONParser jsonParser;
	//private String[][] results;

	//An array of all our data
	private JSONArray jaData = null;

	// Storage for user preferences
	private CoreSettings coreSettings;

	public RemoteDBTableRetrieval() {
		coreSettings = CoreSettings.accessCoreSettings();
	}

	public Globals getGlobals() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLE_NAME_GLOBAL_SETTINGS));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLE_NAME_GLOBAL_SETTINGS);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLE_NAME_GLOBAL_SETTINGS);

				Globals globalsRemote = new Globals();
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);
				JSONObject jaFields = jaData.getJSONObject(0);
				DateConverter dateConverter = new DateConverter();
				//gets the interesting content of each element
				globalsRemote.setVersion(jaFields.getInt(Constants.COLUMN_GLOBAL_VERSION));
				globalsRemote.setRootURL(jaFields.getString(Constants.COLUMN_GLOBAL_ROOT_URL));
				globalsRemote.setLast_updated(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));

				Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data: " + globalsRemote.toString());

				return globalsRemote;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public TableLastUpdateDates getTableListing() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLE_NAME_TABLES));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLE_NAME_TABLES);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLE_NAME_TABLES);

				TableLastUpdateDates tablesUpdatedRemote = new TableLastUpdateDates();
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);

				for (int loopCounter = 0; loopCounter<jaData.length(); loopCounter++) {
					JSONObject jaFields = jaData.getJSONObject(loopCounter);
					DateConverter dateConverter = new DateConverter();

					//gets the interesting content of each element
					if (jaFields.getString(Constants.COLUMN_TABLES_TABLENAME).equals(Constants.TABLES_VALUES_CONFIG)) {
						tablesUpdatedRemote.setConfig(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
						Log.d(RemoteDBTableRetrieval.class.getName(), "Set remote ConfigValues last update date!");
					} else if (jaFields.getString(Constants.COLUMN_TABLES_TABLENAME).equals(Constants.TABLES_VALUES_PLANTTYPES)) {
						tablesUpdatedRemote.setPlants(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
						Log.d(RemoteDBTableRetrieval.class.getName(), "Set remote PlantTypes last update date!");
					} else if (jaFields.getString(Constants.COLUMN_TABLES_TABLENAME).equals(Constants.TABLES_VALUES_OBJECTIVES)) {
						tablesUpdatedRemote.setObjectives(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
						Log.d(RemoteDBTableRetrieval.class.getName(), "Set remote Objectives last update date!");
					} else if (jaFields.getString(Constants.COLUMN_TABLES_TABLENAME).equals(Constants.TABLES_VALUES_ITERATION_RULES)) {
						tablesUpdatedRemote.setIterationRules(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
						Log.d(RemoteDBTableRetrieval.class.getName(), "Set remote PlantTypes last update date!");
					} else if (jaFields.getString(Constants.COLUMN_TABLES_TABLENAME).equals(Constants.TABLES_VALUES_HELPANDINFO)) {
						tablesUpdatedRemote.setHelpAndInfo(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
						Log.d(RemoteDBTableRetrieval.class.getName(), "Set remote Help and Info last update date!");
					} else {
						// ADD EXTRA TABLES AS NEEDED!
						Log.e(RemoteDBTableRetrieval.class.getName(), "Unknown table in remote data: " + jaFields.getString(Constants.COLUMN_TABLES_TABLENAME));
					}

					Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data: " + tablesUpdatedRemote.toString());
				}
				return tablesUpdatedRemote;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public RemoteSeed[] getSeedingPlants(String username, double latitude, double longitude, double distance_user, double distance_sponsor, Date dateAndTime ) {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		DateConverter dateConverter = new DateConverter();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TAG_USERNAME, "" + username));
			params.add(new BasicNameValuePair(Constants.PARAM_LATITUDE, "" + latitude));
			params.add(new BasicNameValuePair(Constants.PARAM_LONGITUDE, "" + longitude));
			params.add(new BasicNameValuePair(Constants.PARAM_DISTANCE_USER, "" + distance_user));
			params.add(new BasicNameValuePair(Constants.PARAM_DISTANCE_SPONSOR, "" + distance_sponsor));
			params.add(new BasicNameValuePair(Constants.PARAM_LAST_UPDATED_USER, dateConverter.convertDateToString(dateConverter.reduceDateByMinutes(dateAndTime, Constants.default_LAST_UPDATE_USER_TIMEGAP_MINUTES))));
			params.add(new BasicNameValuePair(Constants.PARAM_LAST_UPDATED_SPONSOR, dateConverter.convertDateToString(dateConverter.reduceDateByMinutes(dateAndTime, Constants.default_LAST_UPDATE_SPONSOR_TIMEGAP_MINUTES))));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of nearby seeding plants - Lat=" + latitude + ", Long=" + longitude);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(Constants.ROOT_URL + Constants.GET_SEEDING_PLANTS_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Retrieved local seeding plant data...");

				jaData = json.getJSONArray(Constants.TAG_MESSAGE);

				RemoteSeed[] remoteSeedingPlants = getSeedsFromJSON(jaData);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data for " + remoteSeedingPlants.length + " nearby seeding plants.");
				return remoteSeedingPlants;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ConfigValues getConfig() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLES_VALUES_CONFIG));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLES_VALUES_CONFIG);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLES_VALUES_CONFIG);

				ConfigValues remoteConfigValues = new ConfigValues();
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);
				JSONObject jaFields = jaData.getJSONObject(0);
				DateConverter dateConverter = new DateConverter();
				//gets the interesting content of each element
				remoteConfigValues.setLast_updated(dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED)));
				remoteConfigValues.setIteration_time_delay(jaFields.getInt(Constants.COLUMN_CONFIG_ITERATION_DELAY));
				remoteConfigValues.setPlot_matrix_columns(jaFields.getInt(Constants.COLUMN_CONFIG_PLOT_MATRIX_COLUMNS));
				remoteConfigValues.setPlot_matrix_rows(jaFields.getInt(Constants.COLUMN_CONFIG_PLOT_MATRIX_ROWS));
				remoteConfigValues.setPlot_pattern(jaFields.getString(Constants.COLUMN_CONFIG_PLOT_PATTERN));

				Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data: " + remoteConfigValues.toString());

				return remoteConfigValues;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Objective[] getObjectives() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLE_OBJECTIVES));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLE_OBJECTIVES);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLE_OBJECTIVES);
				Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data for: " + jaData.length() + " objectives.");

				Objective[] objectives = getObjectivesFromJSON(jaData);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Completed retrieval of " + objectives.length + " objectives.");
				return objectives;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String[][] getHelpAndInfoData() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLE_HELPANDINFO));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLE_HELPANDINFO);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLE_HELPANDINFO);
				Log.d(RemoteDBTableRetrieval.class.getName(), "Got " + jaData.length() + " Help and Info entries.");

				String[][] helpAndInfoData = getHelpAndInfoFromJSON(jaData);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Completed retrieval of " + helpAndInfoData.length + " Help and Info entries.");
				return helpAndInfoData;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public PlantType[] getPlantTypes() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.TABLE_PLANT_TYPES));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.TABLE_PLANT_TYPES);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.TABLE_DATA_SCRIPT_NAME, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				jaData = json.getJSONArray(Constants.TAG_MESSAGE);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.TABLE_PLANT_TYPES);
				Log.d(RemoteDBTableRetrieval.class.getName(), "Got remote data for: " + jaData.length() + " plants.");

				PlantType[] plantTypes = getPlantTypesFromJSON(jaData);

				Log.d(RemoteDBTableRetrieval.class.getName(), "Completed retrieval of " + plantTypes.length + " plants.");
				return plantTypes;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	private PlantType[] getPlantTypesFromJSON(JSONArray jaData) {
		PlantType[] plantTypes = new PlantType[jaData.length()];
		
		try {
			for (int loopCounter = 0; loopCounter<jaData.length(); loopCounter++) {
				JSONObject jaFields = jaData.getJSONObject(loopCounter);

				int id = jaFields.getInt(Constants.COLUMN_ID_REMOTE);
				String type = jaFields.getString(Constants.COLUMN_PLANTTYPES_TYPE);
				int pref_temp = jaFields.getInt(Constants.COLUMN_PLANTTYPES_PREFTEMP);
				int req_water = jaFields.getInt(Constants.COLUMN_PLANTTYPES_REQWATER);
				int pref_ph = jaFields.getInt(Constants.COLUMN_PLANTTYPES_PREFPH);
				GroundState pref_gs = GroundState.valueOf(jaFields.getString(Constants.COLUMN_PLANTTYPES_PREFGROUNDSTATE));
				int lives_for = jaFields.getInt(Constants.COLUMN_PLANTTYPES_LIVESFOR);
				int com_fact = jaFields.getInt(Constants.COLUMN_PLANTTYPES_COMFACTOR);
				int mat_age = jaFields.getInt(Constants.COLUMN_PLANTTYPES_MATURES);
				int flow_tar = jaFields.getInt(Constants.COLUMN_PLANTTYPES_FLOWTARGET);
				int flow_for = jaFields.getInt(Constants.COLUMN_PLANTTYPES_FLOWFOR);
				int fruit_tar = jaFields.getInt(Constants.COLUMN_PLANTTYPES_FRUITTARGET);
				int fruit_for = jaFields.getInt(Constants.COLUMN_PLANTTYPES_FRUITFOR);
				String photoPath = jaFields.getString(Constants.COLUMN_PLANTTYPES_PHOTO);
				String imgGrowing = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_GROWING);
				String imgWilting = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_WILTING);
				String imgFlowering = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_FLOWERING);
				String imgFruiting = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_FRUITING);
				String imgChilly = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_CHILLY);
				String imgDead = jaFields.getString(Constants.COLUMN_PLANTTYPES_IMAGE_DEAD);
				int size_max = jaFields.getInt(Constants.COLUMN_PLANTTYPES_SIZE_MAX);
				int size_growth_rate = jaFields.getInt(Constants.COLUMN_PLANTTYPES_SIZE_GROWTH_RATE);
				int size_shrink_rate = jaFields.getInt(Constants.COLUMN_PLANTTYPES_SIZE_SHRINK_RATE);

				PlantType plant = new PlantType(id, type, pref_temp, req_water, pref_ph,
						pref_gs, lives_for, com_fact, mat_age, flow_tar, flow_for, fruit_tar,
						fruit_for, photoPath, imgGrowing, imgWilting, imgFlowering, imgFruiting,
						imgChilly, imgDead, size_max, size_growth_rate, size_shrink_rate);
				plantTypes[loopCounter] = plant;

				Log.d(RemoteDBTableRetrieval.class.getName(), "Accessed Plant Type data: " + plant.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return plantTypes;
	}

	private Objective[] getObjectivesFromJSON(JSONArray jaData) {
		Objective[] objectives = new Objective[jaData.length()];

		try {
			for (int loopCounter = 0; loopCounter<jaData.length(); loopCounter++) {
				JSONObject jaFields = jaData.getJSONObject(loopCounter);

				int objectiveID = jaFields.getInt(Constants.COLUMN_OBJECTIVES_ID);
				String description = jaFields.getString(Constants.COLUMN_OBJECTIVES_DESC);
				String message = jaFields.getString(Constants.COLUMN_OBJECTIVES_MESSAGE);

				Objective objective = new Objective(objectiveID, description, message, false);
				objectives[loopCounter] = objective;

				Log.d(RemoteDBTableRetrieval.class.getName(), "Accessed Objective data: " + objective.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return objectives;
	}

	private String[][] getHelpAndInfoFromJSON(JSONArray jaData) {
		String[][] helpAndInfoData = new String[jaData.length()][4];
		List<String> images = new LinkedList<String>();
		
		try {
			for (int loopCounter = 0; loopCounter<jaData.length(); loopCounter++) {
				JSONObject jaFields = jaData.getJSONObject(loopCounter);
				
				helpAndInfoData[loopCounter][0] = jaFields.getString(Constants.COLUMN_ID_REMOTE);
				helpAndInfoData[loopCounter][1] = jaFields.getString(Constants.COLUMN_HELPANDINFO_DATATYPE);
				helpAndInfoData[loopCounter][2] = jaFields.getString(Constants.COLUMN_HELPANDINFO_REFERENCE);
				helpAndInfoData[loopCounter][3] = jaFields.getString(Constants.COLUMN_HELPANDINFO_TEXT);
				
				if (jaFields.getString(Constants.COLUMN_HELPANDINFO_REFERENCE).equals(Constants.HELPANDINFO_HELP_REF_IMAGE)) {
					images.add(helpAndInfoData[loopCounter][3]);
				}
			}
			Log.d(RemoteDBTableRetrieval.class.getName(), "Handled " + jaData.length() + " Help and Info entries, including " + images.size() + " images!");

			if (images.size() > 0 ){
				String[] downloadFrom = new String[images.size()];
				String[] saveTo = new String[images.size()];
				String remoteRoot = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.ROOT_URL_IMAGE_EXT;
				String localRoot = coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH);
				
				int loopCounter = 0;
				for (String imageName : images) {
					downloadFrom[loopCounter] = remoteRoot + imageName;
					saveTo[loopCounter] = localRoot + imageName;
					loopCounter++;
				}
				
				FileDownloader imageDownloader = new FileDownloader();
				imageDownloader.download(downloadFrom, saveTo);
				imageDownloader.closedown();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return helpAndInfoData;
	}

	private RemoteSeed[] getSeedsFromJSON(JSONArray jaData) {
		RemoteSeed[] remoteSeedingPlants = new RemoteSeed[jaData.length()];

		try {
			for (int loopCounter = 0; loopCounter < jaData.length(); loopCounter++) {
				JSONObject jaFields  = jaData.getJSONObject(loopCounter);

				DateConverter dateConverter = new DateConverter();
				int plantTypeid = jaFields.getInt(Constants.COLUMN_PLANT_TYPE_ID);
				String username = jaFields.getString(Constants.TAG_USERNAME);
				Date last_updated = dateConverter.convertStringToDate(jaFields.getString(Constants.COLUMN_LAST_UPDATED));
				Double dist = jaFields.getDouble(Constants.COLUMN_DISTANCE);
				boolean spons = jaFields.getBoolean(Constants.COLUMN_SPONSORED);
				String message = jaFields.getString(Constants.COLUMN_MESSAGE);
				String success_copy = jaFields.getString(Constants.COLUMN_SUCCESS_COPY);

				RemoteSeed tempSeed = new RemoteSeed(plantTypeid, username, last_updated, dist, spons, message, success_copy);
				remoteSeedingPlants[loopCounter] = tempSeed;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return remoteSeedingPlants;
	}

	public boolean uploadSeed(Date date, String username, double latitude, double longitude, int plantTypeID) {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			DateConverter dateConverter = new DateConverter();
			params.add(new BasicNameValuePair(Constants.COLUMN_LAST_UPDATED, dateConverter.convertDateToString(date)));
			params.add(new BasicNameValuePair(Constants.TAG_USERNAME, username));
			params.add(new BasicNameValuePair(Constants.PARAM_LATITUDE, "" + latitude));
			params.add(new BasicNameValuePair(Constants.PARAM_LONGITUDE, "" + longitude));
			params.add(new BasicNameValuePair(Constants.COLUMN_PLANT_TYPE_ID, "" + plantTypeID));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting to upload a seed: plant_type_id=" + plantTypeID);
			if (coreSettings == null) {
				coreSettings = CoreSettings.accessCoreSettings();
			}
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.UPLOAD_SEED, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Upload Successful! ent seed of plant type: " + plantTypeID);
				return true;
			} else {
				Log.d(RemoteDBTableRetrieval.class.getName(), "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//shouldn't get here, unless error...
		return false;
	}
}
