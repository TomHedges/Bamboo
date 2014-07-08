package com.tomhedges.bamboo.util.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.RetrievalType;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.util.DateConverter;
import com.tomhedges.bamboo.util.JSONParser;

public class RemoteDBTableRetrieval {

	// JSON parser class
	private JSONParser jsonParser;
	private String[][] results;

	//An array of all our data
	private JSONArray jaData = null;

	public RemoteDBTableRetrieval() {

	}

	public String[][] getTableStructure(RetrievalType dataType, String tableName) {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();


		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, tableName));

			Log.d("getTableStructure", "Starting");
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(Constants.COLUMN_DETAILS_URL, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d("getTableStructure", "Request attempt for: " + tableName);

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d("getTableStructure", "Request Successful! For: " + tableName);

				jaData = json.getJSONArray(Constants.TAG_MESSAGE);
				results = new String[2][jaData.length()];
				for (int arrayLooper = 0; arrayLooper < jaData.length(); arrayLooper++) {
					JSONObject tableField = jaData.getJSONObject(arrayLooper);

					//gets the interesting content of each element
					results[0][arrayLooper] = tableField.getString(Constants.TAG_FIELD);
					results[1][arrayLooper] = tableField.getString(Constants.TAG_TYPE);
				}
				Log.d("getTableStructure", "Partial results: " + results[0][0] + ", " + results[1][0]);

				return results;
			} else {
				Log.d("getTableStructure", "ERROR: " + json.getString(Constants.TAG_MESSAGE));
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Globals getGlobals() {
		// Check for success tag
		int success;

		jsonParser = new JSONParser();

		try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Constants.TABLE_NAME_VARIABLE, Constants.GLOBAL_SETTINGS_TABLE_NAME));

			Log.d(RemoteDBTableRetrieval.class.getName(), "Attempting retrieval of data from: " + Constants.GLOBAL_SETTINGS_TABLE_NAME);
			// getting product details by making HTTP request
			JSONObject json = jsonParser.makeHttpRequest(Constants.TABLE_DATA_URL, Constants.HTML_VERB_POST, params);

			// check your log for json response
			Log.d(RemoteDBTableRetrieval.class.getName(), "Response: " + json.toString());

			// json success tag
			success = json.getInt(Constants.TAG_SUCCESS);
			if (success == 1) {
				Log.d(RemoteDBTableRetrieval.class.getName(), "Request Successful! Data for: " + Constants.GLOBAL_SETTINGS_TABLE_NAME);

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
}
