// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.RepeatingActivity;
import com.tomhedges.bamboo.activities.TableDisplayActivity;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.model.ConfigValues;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.MatrixOfPlots;
import com.tomhedges.bamboo.model.PlantCatalogue;
import com.tomhedges.bamboo.model.PlantType;
import com.tomhedges.bamboo.model.Plot;
import com.tomhedges.bamboo.model.TableLastUpdateDates;
import com.tomhedges.bamboo.util.dao.ConfigDataSource;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LaunchFragment extends Fragment implements OnClickListener, Constants {

	private Button btnRepetitonTest, btnTableDisplayTest;
	private Intent i;

	// Progress Dialog
	private ProgressDialog pDialog;

	// to build local settings
	private RemoteDBTableRetrieval remoteDataRetriever;

	private ConfigDataSource localDataRetriever;

	// Storage for user preferences
	private CoreSettings coreSettings;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.launch, container, false);        

		//setup buttons
		btnRepetitonTest = (Button)v.findViewById(R.id.launchRepeatingActivity);
		btnTableDisplayTest = (Button)v.findViewById(R.id.launchTableDisplayActivity);

		//register listeners
		btnRepetitonTest.setOnClickListener(this);
		btnTableDisplayTest.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.launchRepeatingActivity:
			i = new Intent(this.getActivity(), RepeatingActivity.class);
			startActivity(i);
			break;

		case R.id.launchTableDisplayActivity:
			new RetrieveData().execute();

			//i = new Intent(this.getActivity(), TableDisplayActivity.class);
			//startActivity(i);
			break;

		default:
			break;
		}
	}

	@Override
	public void onResume() {
		if (localDataRetriever != null) {
			localDataRetriever.open();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (localDataRetriever != null) {
			localDataRetriever.close();
		}
		super.onPause();
	}

	class RetrieveData extends AsyncTask<Void, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Setting up...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected String doInBackground(Void... params) {
			
			/// NEED TO MAKE THIS WORK IF NO INTERNET CONNECTION - SHOULD BE EASY AS ALL DATA STORED LOCALLY!!!
			
			Log.w(LaunchFragment.class.getName(), "Creating core settings preferences");
			CoreSettings.createCoreSettings(getActivity());
			coreSettings = CoreSettings.accessCoreSettings();

			publishProgress("Checking for updates to local data");
			localDataRetriever = new ConfigDataSource(getActivity());
			localDataRetriever.open();
			Log.w(LaunchFragment.class.getName(), "Opened Data source!");
			Log.w(LaunchFragment.class.getName(), "Get local globals");
			Globals globalsLocal = localDataRetriever.getGlobals();

			// save URL, etc. to preferences
			coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, globalsLocal.getRootURL());
			coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, globalsLocal.getRootURL());

			remoteDataRetriever = new RemoteDBTableRetrieval();
			Globals globalsRemote = remoteDataRetriever.getGlobals();

			//Log.w(LaunchFragment.class.getName(), "globalsRemote last updated: " + globalsRemote.getLast_updated());
			//Log.w(LaunchFragment.class.getName(), "globalsLocal last updated: " + globalsLocal.getLast_updated());

			// for testing purposes - change to false for production! Forces all tables to update.
			boolean forceUpdate = false;

			if (forceUpdate || globalsRemote.getLast_updated().after(globalsLocal.getLast_updated())) {
				publishProgress("Updating local data with updated values!");
				Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE SOME LOCAL DATA!");

				// if update of local data is successful, then check for further updates...
				if (localDataRetriever.writeGlobals(globalsRemote)) {
					Log.w(LaunchFragment.class.getName(), "Get local table update values");

					TableLastUpdateDates tablesUpdatedLocal = localDataRetriever.getTableUpdateDates();
					Log.w(LaunchFragment.class.getName(), "ConfigValues date (local): " + tablesUpdatedLocal.getConfig().toString());
					TableLastUpdateDates tablesUpdatedRemote = remoteDataRetriever.getTableListing();
					Log.w(LaunchFragment.class.getName(), "ConfigValues date (remote): " + tablesUpdatedRemote.getConfig().toString());


					// CONFIG
					if (forceUpdate || tablesUpdatedRemote.getConfig().after(tablesUpdatedLocal.getConfig())) {
						Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE CONFIG TABLE!");
						publishProgress("Updating Configuration data with updated values!");
						ConfigValues remoteConfigValues = remoteDataRetriever.getConfig();

						if (localDataRetriever.writeConfig(remoteConfigValues) && localDataRetriever.writeTableUpdateDate(TABLES_VALUES_CONFIG, tablesUpdatedRemote.getConfig())) {
							Log.w(LaunchFragment.class.getName(), "Updated Config date in Tables table!");
							Log.w(LaunchFragment.class.getName(), "Updated Config data!");
							publishProgress("Local Config table data updated");

							Log.w(LaunchFragment.class.getName(), "Creating Plot matrix from remote data...");
							GroundState[] gsGroundStates = remoteConfigValues.getGroundStates();

							int num_rows = remoteConfigValues.getPlot_matrix_rows();
							int num_cols = remoteConfigValues.getPlot_matrix_columns();

							Plot[][] plotArray = new Plot[num_rows][num_cols];

							for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
								for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
									plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, rowCounter + 1, columnCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);
								}
							}

							coreSettings.addSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY, remoteConfigValues.getIteration_time_delay());
							
							if (MatrixOfPlots.createMatrix(plotArray)) {
								Log.w(LaunchFragment.class.getName(), "Created plot matrix from remote data!");
							} else {
								Log.e(LaunchFragment.class.getName(), "Could not create plot matrix from remote data!");
							}
						} else {
							Log.e(LaunchFragment.class.getName(), "Could not update Config date and/or data...");
							publishProgress("Local ConfigValues data could not be updated");
						}
					}

					// PLANT TYPES
					if (forceUpdate || tablesUpdatedRemote.getPlants().after(tablesUpdatedLocal.getPlants())) {
						Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE PLANTTYPES TABLE!");
						publishProgress("Updating Plant Types data with updated values!");
						PlantType[] remotePlantTypes = remoteDataRetriever.getPlantTypes();

						if (localDataRetriever.writePlantTypes(remotePlantTypes) && localDataRetriever.writeTableUpdateDate(TABLES_VALUES_PLANTTYPES, tablesUpdatedRemote.getPlants())) {
							Log.w(LaunchFragment.class.getName(), "Updated Plant Types data!");
							publishProgress("Local Plant Types table data updated");
							
							if (PlantCatalogue.createPlantCatalogue(remoteDataRetriever.getPlantTypes())) {
								Log.w(LaunchFragment.class.getName(), "Created plant catalogue!");
							} else {
								Log.e(LaunchFragment.class.getName(), "Could not create plant catalogue!");
							}
						} else {
							Log.e(LaunchFragment.class.getName(), "Could not update Plant types date and/or data...");
							publishProgress("Local Plant types data could not be updated");
						}
					}

					//Now update other local tables!!! AND WE NEED TO DO SOMETHING WITH DATA -eg. URL (use downloaded rather than constants. Shouldthis actually be read from downloaded text file???!

					publishProgress("Local data updated");
				} else {
					publishProgress("Local data could not be updated");
				}
			} else {
				publishProgress("All local data already up to date!");
			}
			
			//BUILD FROM LOCAL DATA
			
			// PLOT MATRIX - only build if empty
			if (MatrixOfPlots.getMatrix() == null) {
				publishProgress("Setting up game from local details!");
				Log.w(LaunchFragment.class.getName(), "Creating Plot matrix from local data...");
				
				ConfigValues localConfigValues = localDataRetriever.getConfigValues();
				GroundState[] gsGroundStates = localConfigValues.getGroundStates();

				int num_rows = localConfigValues.getPlot_matrix_rows();
				int num_cols = localConfigValues.getPlot_matrix_columns();

				Plot[][] plotArray = new Plot[num_rows][num_cols];

				for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
					for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
						plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, rowCounter + 1, columnCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);
					}
				}

				coreSettings.addSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY, localConfigValues.getIteration_time_delay());
				
				if (MatrixOfPlots.createMatrix(plotArray)) {
					Log.w(LaunchFragment.class.getName(), "Created plot matrix from local data!");
				} else {
					Log.e(LaunchFragment.class.getName(), "Could not create plot matrix from local data!");
				}
			}

			// PLANT CATALOGUE - only build if empty
			if (PlantCatalogue.getPlantCatalogue() == null) {
				publishProgress("Building plant catalogue!");
				Log.w(LaunchFragment.class.getName(), "Building plant catalogue from local data...");

				if (PlantCatalogue.createPlantCatalogue(localDataRetriever.getPlantTypes())) {
					Log.w(LaunchFragment.class.getName(), "Created plant catalogue!");
				} else {
					Log.e(LaunchFragment.class.getName(), "Could not create plant catalogue!");
				}
			}
			
			return "Starting game...";
		}

		protected void onProgressUpdate(String... progress) {
			pDialog.setMessage(progress[0]);

		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String retrievedData) {
			pDialog.setMessage(retrievedData);
			//			String showData;
			//			if (retrievedData != null){
			//				showData = retrievedData;
			//			} else {
			//				showData = "sorry - null!";
			//			}

			//Toast.makeText(getActivity(), "First field in array: " + showData, Toast.LENGTH_LONG).show();



			i = new Intent(getActivity(), TableDisplayActivity.class);
			startActivity(i);

			// dismiss the dialog once product deleted
			pDialog.dismiss();

			//make sure it gets closed?
			localDataRetriever.close();
		}

	}
}
