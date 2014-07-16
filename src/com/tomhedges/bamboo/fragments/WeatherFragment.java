package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.PlantCatalogue;
import com.tomhedges.bamboo.model.RemoteSeed;
import com.tomhedges.bamboo.util.JSONParser;
import com.tomhedges.bamboo.util.LocationRetrieve;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherFragment extends Fragment implements OnClickListener, Constants {

	private TextView weatherData;
	private String weatherRawData;
	private String cloud_level;
	private String temperature;
	private String rainfall;
	LocationRetrieve locator;
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	private RemoteDBTableRetrieval remoteDataRetriever;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.weather, container, false);

        weatherData = (TextView)v.findViewById(R.id.weatherText);

        Button bAdd = (Button) v.findViewById(R.id.getWeather);
        bAdd.setOnClickListener(this);
        
        return v;
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.getWeather:
			new RetrieveWeather().execute();
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
        locator = new LocationRetrieve(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		locator.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		locator.disconnect();
	}
	
	public void updateWeather() {
		weatherData.setText(weatherRawData);
	}

	class RetrieveWeather extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Attempting to retrieve weather details...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Check for success tag
			int success;
			String q = locator.getLocation().getLatitude() + "," + locator.getLocation().getLongitude();
			Log.d("Location", locator.getLocation().toString());
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("q", q));
				params.add(new BasicNameValuePair("format", WEATHER_FORMAT));
				params.add(new BasicNameValuePair("key", WEATHER_KEY));

				Log.d("request!", "starting");
				// getting product details by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(WEATHER_URL, "GET", params);

				// check your log for json response
				Log.d("Login attempt", json.toString());
				//weatherRawData = json.toString();

			    JSONObject data = json.getJSONObject("data");
				Log.d("Login attempt", data.toString());
			    JSONArray result = data.getJSONArray("current_condition");
			    JSONObject resultData = (JSONObject) result.get(0);
				Log.d("Login attempt", result.toString());
			    cloud_level = resultData.getString("cloudcover") + "%";
				Log.d("Login attempt", cloud_level);
			    temperature = resultData.getString("temp_C") + "\u00B0C"; // \u00B0 is unicode for degrees symbol
				Log.d("Login attempt", temperature);
			    rainfall = resultData.getString("precipMM") + "mm";
				Log.d("Login attempt", rainfall);
			    
			    weatherRawData = "Location: " + q + "\n\nCloud: " + cloud_level + "\nTemp: " + temperature + "\nRain: "  + rainfall;
			    
				remoteDataRetriever = new RemoteDBTableRetrieval();
				PlantCatalogue.createPlantCatalogue(null);
				PlantCatalogue plantCat = PlantCatalogue.getPlantCatalogue();
				RemoteSeed[] remoteSeeds = remoteDataRetriever.getSeedingPlants(locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), Constants.default_DISTANCE_USER, Constants.default_DISTANCE_SPONSOR, new Date());
				plantCat.setRemoteSeedArray(remoteSeeds);
				remoteSeeds = null;
				
				remoteSeeds = plantCat.getRemoteSeedArray();
				String strTest = "";
				for (RemoteSeed remSeed : remoteSeeds) {
					strTest = strTest + "\n" + remSeed.toString();
				}
				weatherRawData = "Got data for " + remoteSeeds.length + " plants..." + strTest;
				
				getActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						updateWeather();
					}
				});
				
				// json success tag
				//success = json.getInt(TAG_SUCCESS);
				//if (success == 1) {
					
//					Log.d("Login Successful!", json.toString());
//
//					// save user data
//					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//					Editor edit = sp.edit();
//					edit.putString("username", q);
//					edit.commit();
//
//					//Intent i = new Intent(getActivity(), ReadCommentsFragment.class);
//					Intent i = new Intent(getActivity(), ReadCommentsActivity.class);
//					// finish(); ??? Don't know what this does
//					startActivity(i);
//					return json.getString(TAG_MESSAGE);
//				}else{
//					Log.d("Login Failure!", json.getString(TAG_MESSAGE));
//					return json.getString(TAG_MESSAGE);
//
					
				//}
					
					
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;

		}
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			if (file_url != null){
				Toast.makeText(getActivity(), file_url, Toast.LENGTH_LONG).show();
			}
		}
	}
}
