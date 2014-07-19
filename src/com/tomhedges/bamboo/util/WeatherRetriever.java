package com.tomhedges.bamboo.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import com.tomhedges.bamboo.config.Constants;

public class WeatherRetriever {
	
	private double longitude;
	private double latitude;
	
	private int temperature = Constants.default_Temperature; //degrees C
	private int rainfall = Constants.default_WaterLevel; // mm
	
	// JSON parser class
	JSONParser jsonParser = new JSONParser();
	
	public WeatherRetriever() {
		longitude = 0.0;
		latitude = 0.0;
	}
	
	public void checkWeather(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
		new RetrieveWeather().execute();
	}

	private class RetrieveWeather extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// Check for success tag
			int success;
			String q = latitude + "," + longitude;
			Log.d("Location", "Lat: " + latitude + ", Long:" + longitude);
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("q", q));
				params.add(new BasicNameValuePair("format", Constants.WEATHER_FORMAT));
				params.add(new BasicNameValuePair("key", Constants.WEATHER_KEY));

				Log.d(RetrieveWeather.class.getName(), "starting");
				// getting product details by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(Constants.WEATHER_URL, "GET", params);

				// check your log for json response
				Log.d(RetrieveWeather.class.getName(), json.toString());
				//weatherRawData = json.toString();

			    JSONObject data = json.getJSONObject("data");
			    JSONArray result = data.getJSONArray("current_condition");
			    JSONObject resultData = (JSONObject) result.get(0);
			    temperature = resultData.getInt("temp_C"); // \u00B0 is unicode for degrees symbol
				Log.d(RetrieveWeather.class.getName(), "Temp: " + temperature + "\u00B0C");
			    rainfall = resultData.getInt("precipMM");
				Log.d(RetrieveWeather.class.getName(), "Rainfall: " + rainfall + "mm");					
					
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;

		}
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// not sure... possibly set to send  notification to Game??
		}
	}

	public int getTemperature() {
		return temperature;
	}

	public int getRainfall() {
		return rainfall;
	}
	
	
}
