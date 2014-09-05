// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.fragments;

import java.util.Date;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.GameUI3D;
import com.tomhedges.bamboo.activities.RepeatingActivity;
import com.tomhedges.bamboo.activities.TableDisplayActivity;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
//import com.tomhedges.bamboo.model.ConfigValues;
import com.tomhedges.bamboo.model.Game;
//import com.tomhedges.bamboo.model.Globals;
//import com.tomhedges.bamboo.model.MatrixOfPlots;
//import com.tomhedges.bamboo.model.Neighbourhood;
//import com.tomhedges.bamboo.model.Objective;
//import com.tomhedges.bamboo.model.Objectives;
//import com.tomhedges.bamboo.model.PlantCatalogue;
//import com.tomhedges.bamboo.model.PlantType;
//import com.tomhedges.bamboo.model.Plot;
//import com.tomhedges.bamboo.model.TableLastUpdateDates;
//import com.tomhedges.bamboo.util.FileDownloader;
import com.tomhedges.bamboo.util.ArrayAdapterObjectives;
import com.tomhedges.bamboo.util.ArrayAdapterUnlockedSeeds;
import com.tomhedges.bamboo.util.dao.ConfigDataSource;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class LaunchFragment extends Fragment implements OnClickListener {

	private Button btnRepetitonTest, btnNewGame, btnLoadGame, btnNewGame3D, btnLoadGame3D, btnViewUnclockedSeeds, btnViewObjectives, btnResetObjectives, btnTestSeedUpload;
	private EditText etUsername;
	private Intent i;

	// Progress Dialog
	private ProgressDialog pDialog;

	// to build local settings
	private RemoteDBTableRetrieval remoteDataRetriever;
	private ConfigDataSource localDataRetriever;
	//private FileDownloader downloader;

	// Storage for user preferences
	private CoreSettings coreSettings;

	//master...
	private Game game;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.launch, container, false);  

		etUsername = (EditText)v.findViewById(R.id.username);

		//setup buttons
		//btnRepetitonTest = (Button)v.findViewById(R.id.launchRepeatingActivity);
		btnNewGame = (Button)v.findViewById(R.id.launchStartNewGame);
		btnLoadGame = (Button)v.findViewById(R.id.launchContinueCurrentGame);
		btnNewGame3D = (Button)v.findViewById(R.id.launchStartNewGame3D);
		btnLoadGame3D = (Button)v.findViewById(R.id.launchContinueCurrentGame3D);
		btnViewUnclockedSeeds = (Button)v.findViewById(R.id.view_unlocked_sponsored_seeds);
		btnViewObjectives = (Button)v.findViewById(R.id.view_objective_completion);
		btnResetObjectives = (Button)v.findViewById(R.id.reset_objective_completion);
		btnTestSeedUpload = (Button)v.findViewById(R.id.test_seed_upload);

		//register listeners
		//btnRepetitonTest.setOnClickListener(this);
		btnNewGame.setOnClickListener(this);
		btnLoadGame.setOnClickListener(this);
		btnNewGame3D.setOnClickListener(this);
		btnLoadGame3D.setOnClickListener(this);
		btnViewUnclockedSeeds.setOnClickListener(this);
		btnViewObjectives.setOnClickListener(this);
		btnResetObjectives.setOnClickListener(this);
		btnTestSeedUpload.setOnClickListener(this);

		game = Game.getGameDetails(getActivity());
		btnLoadGame.setEnabled(game.savedGameExists());
		btnLoadGame3D.setEnabled(game.savedGameExists());

		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.launchRepeatingActivity:
//			i = new Intent(this.getActivity(), RepeatingActivity.class);
//			startActivity(i);
//			break;

		case R.id.launchStartNewGame:
			//new RetrieveData().execute();

			//i = new Intent(this.getActivity(), TableDisplayActivity.class);
			//startActivity(i);
			
			game.setUsername(etUsername.getText().toString());
			i = new Intent(this.getActivity(), TableDisplayActivity.class);
			game.startNewGame();
			startActivity(i);

			break;

		case R.id.launchContinueCurrentGame:
			i = new Intent(this.getActivity(), TableDisplayActivity.class);
			game.continueExistingGame();
			startActivity(i);

			break;

		case R.id.launchStartNewGame3D:
			game.setUsername(etUsername.getText().toString());
			i = new Intent(this.getActivity(), GameUI3D.class);
			game.startNewGame();
			startActivity(i);
			break;

		case R.id.launchContinueCurrentGame3D:
			i = new Intent(this.getActivity(), GameUI3D.class);
			game.continueExistingGame();
			startActivity(i);
			break;

		case R.id.view_unlocked_sponsored_seeds:
			//based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
			// our adapter instance
			ArrayAdapterUnlockedSeeds adapterUnlockedSeeds = new ArrayAdapterUnlockedSeeds(getActivity(), R.layout.list_element_unlocked_seeds, game.getUnlockedSeedsList());

			// create a new ListView, set the adapter and item click listener
			ListView listViewItemsUnlockedSeeds = new ListView(getActivity());
			listViewItemsUnlockedSeeds.setAdapter(adapterUnlockedSeeds);
			// should make this cancel??
			//listViewItems.setOnItemClickListener(new OnItemClickListenerListViewItem());

			// put the ListView in the pop up
			AlertDialog alertDialogStoresUnlockedSeeds = new AlertDialog.Builder(getActivity())
			.setView(listViewItemsUnlockedSeeds)
			.setTitle("Objectives")
			.show();
			break;

		case R.id.view_objective_completion:
			//based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
			// our adapter instance
			ArrayAdapterObjectives adapterObjectives = new ArrayAdapterObjectives(getActivity(), R.layout.list_element_objectives, game.getObjectiveList());

			// create a new ListView, set the adapter and item click listener
			ListView listViewItemsObjectives = new ListView(getActivity());
			listViewItemsObjectives.setAdapter(adapterObjectives);
			// should make this cancel??
			//listViewItems.setOnItemClickListener(new OnItemClickListenerListViewItem());

			// put the ListView in the pop up
			AlertDialog alertDialogStoresObjectives = new AlertDialog.Builder(getActivity())
			.setView(listViewItemsObjectives)
			.setTitle("Objectives")
			.show();
			break;

		case R.id.reset_objective_completion:

			localDataRetriever = new ConfigDataSource(getActivity());
			localDataRetriever.open();

			String message = null;
			if (localDataRetriever.resetObjectiveCompletionStatuses()) {
				message = "Reset all objectives - enjoy playing again!";
			} else {
				message = "Problem resetting objectives.";
			}
			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

			localDataRetriever.close();
			localDataRetriever = null;

			break;

		case R.id.test_seed_upload:
			new UploadTest().execute();
			break;
			
		default:
			break;
		}
	}

	class UploadTest extends AsyncTask<Void, Void, Boolean> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Testing seed upload");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.w(UploadTest.class.getName(), "Testing seed upload...");

			CoreSettings.createCoreSettings(getActivity());
			remoteDataRetriever = new RemoteDBTableRetrieval();
			coreSettings = CoreSettings.accessCoreSettings();
			coreSettings.addSetting(Constants.TAG_USERNAME, etUsername.getText().toString());
			coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, Constants.ROOT_URL.toString());

			java.util.Random randomGenerator = new java.util.Random();
			boolean seedUploadStatus = remoteDataRetriever.uploadSeed(new Date(), coreSettings.checkStringSetting(Constants.TAG_USERNAME), randomGenerator.nextDouble(), randomGenerator.nextDouble(), randomGenerator.nextInt(2)+3);

			if (seedUploadStatus) {
				Log.w(UploadTest.class.getName(), "Upload successful!");
			} else {
				Log.e(UploadTest.class.getName(), "Upload NOT successful!");
			}

			return seedUploadStatus;
		}

		@Override
		protected void onPostExecute(Boolean uploadStatus) {
			pDialog.cancel();

			if (uploadStatus) {
				Toast.makeText(getActivity(), "Upload successful!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity(), "Upload NOT successful!", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onResume() {
		if (localDataRetriever != null) {
			localDataRetriever.open();
		}
		super.onResume();
		btnLoadGame.setEnabled(game.savedGameExists());
		btnLoadGame3D.setEnabled(game.savedGameExists());
	}

	@Override
	public void onPause() {
		if (localDataRetriever != null) {
			localDataRetriever.close();
		}
		super.onPause();
	}

	/*	class RetrieveData extends AsyncTask<Void, String, String> {

	 *//**
	 * Before starting background thread Show Progress Dialog
	 * *//*
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

			// for testing purposes - change to false for production! Forces all tables to update.
			boolean forceUpdate = false;


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
			coreSettings.addSetting(Constants.TAG_USERNAME, etUsername.getText().toString());

			remoteDataRetriever = new RemoteDBTableRetrieval();
			Globals globalsRemote = remoteDataRetriever.getGlobals();
			downloader = new FileDownloader();

			//Log.w(LaunchFragment.class.getName(), "globalsRemote last updated: " + globalsRemote.getLast_updated());
			//Log.w(LaunchFragment.class.getName(), "globalsLocal last updated: " + globalsLocal.getLast_updated())

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
						///WE ARE REMOTE!!!
						Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE CONFIG TABLE!");
						publishProgress("Updating Configuration data with updated values!");
						ConfigValues remoteConfigValues = remoteDataRetriever.getConfig();

						if (localDataRetriever.writeConfig(remoteConfigValues) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_CONFIG, tablesUpdatedRemote.getConfig())) {
							Log.w(LaunchFragment.class.getName(), "Updated Config date in Tables table!");
							Log.w(LaunchFragment.class.getName(), "Updated Config data!");
							publishProgress("Local Config table data updated");

							Log.w(LaunchFragment.class.getName(), "Creating Plot matrix from remote data...");
							Constants.GroundState[] gsGroundStates = remoteConfigValues.getGroundStates();

							int num_rows = remoteConfigValues.getPlot_matrix_rows();
							int num_cols = remoteConfigValues.getPlot_matrix_columns();

							Plot[][] plotArray = new Plot[num_rows][num_cols];

							for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
								for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
									//plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, rowCounter + 1, columnCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);

									Plot newPlot = new Plot((rowCounter * num_cols) + columnCounter + 1, columnCounter + 1, rowCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);
									plotArray[rowCounter][columnCounter] = newPlot;
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

						if (localDataRetriever.writePlantTypes(remotePlantTypes) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_PLANTTYPES, tablesUpdatedRemote.getPlants())) {
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

					// OBJECTIVES
					if (forceUpdate || tablesUpdatedRemote.getObjectives().after(tablesUpdatedLocal.getObjectives())) {
						Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE OBJECTIVES TABLE + RULES FILE!");
						publishProgress("Updating Objectives data with updated values!");
						Objective[] remoteObjectives = remoteDataRetriever.getObjectives();

						if (localDataRetriever.writeObjectives(remoteObjectives) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_OBJECTIVES, tablesUpdatedRemote.getObjectives())) {
							Log.w(LaunchFragment.class.getName(), "Updated Objectives data!");
							publishProgress("Local Objectives table data updated");

							if (Objectives.createObjectives(remoteDataRetriever.getObjectives())) {
								Log.w(LaunchFragment.class.getName(), "Created local objectives list!");
							} else {
								Log.e(LaunchFragment.class.getName(), "Could not create objectives list!");
							}
						} else {
							Log.e(LaunchFragment.class.getName(), "Could not update Objectives date and/or data...");
							publishProgress("Local Objectives data could not be updated");
						}

						String downloadFrom = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_OBJECTIVES;
						String saveTo = getActivity().getFilesDir() + Constants.FILENAME_LOCAL_OBJECTIVES;
						Log.w(LaunchFragment.class.getName(), "Downloading updated objectives rules file from server. From: " + downloadFrom + ", To: " + saveTo);
						publishProgress("Downloading updated objectives rules file...");
						if (downloader.download(getActivity(), downloadFrom, saveTo)) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(LaunchFragment.class.getName(), "Unable to download updated objectives rules file from server.");
						}
					}

					// ITERATION RULES FILES
					if (forceUpdate || tablesUpdatedRemote.getIterationRules().after(tablesUpdatedLocal.getIterationRules())) {
						Log.w(LaunchFragment.class.getName(), "NEED TO UPDATE ITERATION RULES FILE!");

						String downloadFrom = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_ITERATION_RULES;
						String saveTo = getActivity().getFilesDir() + Constants.FILENAME_LOCAL_ITERATION_RULES;
						Log.w(LaunchFragment.class.getName(), "Downloading updated iteration rules file from server. From: " + downloadFrom + ", To: " + saveTo);
						publishProgress("Downloading updated iteration rules file...");
						if (downloader.download(getActivity(), downloadFrom, saveTo)) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(LaunchFragment.class.getName(), "Unable to download updated iteration rules file from server.");
						}
					}

					//Now update other local tables!!! AND WE NEED TO DO SOMETHING WITH DATA -eg. URL (use downloaded rather than constants.

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
				Constants.GroundState[] gsGroundStates = localConfigValues.getGroundStates();

				int num_rows = localConfigValues.getPlot_matrix_rows();
				int num_cols = localConfigValues.getPlot_matrix_columns();

				Plot[][] plotArray = new Plot[num_rows][num_cols];

				for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
					for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
						//plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, rowCounter + 1, columnCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);
						plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, columnCounter + 1, rowCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel, Constants.default_Temperature, Constants.default_pHLevel);
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

			// OBJECTIVES - only build if empty
			if (Objectives.getObjectives() == null) {
				publishProgress("Building Objectives list!");
				Log.w(LaunchFragment.class.getName(), "Building Objectives list from local data...");

				if (Objectives.createObjectives(localDataRetriever.getObjectives())) {
					Log.w(LaunchFragment.class.getName(), "Created local objectives list!");
				} else {
					Log.e(LaunchFragment.class.getName(), "Could not create objectives list!");
				}

				//Objectives objectives = Objectives.getObjectives();
				//objectives.addObjective(0, "test objective", "this is only a test", true);
				//objectives.addObjective(1, "Plant something!", "Your garden now has a plant!", false);
				//objectives.addObjective(2, "Flowering Carnation", "You have a blooming Carnation!", false);
				//objectives.addObjective(3, "Multiple flowering Carnations", "You several flowering Carnations!", false);
			}

			//BUILD NEIGHBOURHOODS FROM MATRIX OF PLOTS, AND ADD
			MatrixOfPlots mxPlots = MatrixOfPlots.getMatrix();
			int num_rows = mxPlots.getNumRows();
			int num_cols = mxPlots.getNumCols();
			Neighbourhood[] neighbourhoodArray = new Neighbourhood[num_rows * num_cols];
			for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
				for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
					Plot localPlot = mxPlots.getPlot(columnCounter+1, rowCounter+1);
					Neighbourhood neigbourhoodToInsert = new Neighbourhood(localPlot, Constants.NEIGHBOURHOOD_STRUCTURE.length);

					int xPosCentral = localPlot.getXPosInMatrix();
					int yPosCentral = localPlot.getYPosInMatrix();

					Log.w(LaunchFragment.class.getName(), "Building " +  Constants.NEIGHBOURHOOD_STRUCTURE.length + " plot neighbourhood for plot: " + localPlot.getPlotId() + ", at X=" + xPosCentral + ",Y=" + yPosCentral + ". Plot marked as 'neighbourhood created'?: " + localPlot.isNeighbourhoodCreated());

					for (int[] neighbourRelPos : Constants.NEIGHBOURHOOD_STRUCTURE) {
						neigbourhoodToInsert.addNeighbour(mxPlots.getNeigbouringPlot(xPosCentral, yPosCentral, neighbourRelPos[0], neighbourRelPos[1]));
					}

					localPlot.setNeighbourhoodCreated(true);
					neighbourhoodArray[localPlot.getPlotId()-1] = neigbourhoodToInsert;
					Log.w(LaunchFragment.class.getName(), "Built neighbourhood of: " + neigbourhoodToInsert.getNeighbourCounter() + " plots with central plot id " + localPlot.getPlotId());

					// DROOLS LINE - PUT INTO GAME???S ksession.insert(localPlot);
					//Log.w(LaunchFragment.class.getName(), "Inserted plot with ID: " + localPlot.getPlotId());
				}
			}
			mxPlots.setNeighbourhoodMatrix(neighbourhoodArray);

			Log.w(LaunchFragment.class.getName(), "Created neighbourhood matrix from plot matrix data!");

			return "Starting game...";
		}

		protected void onProgressUpdate(String... progress) {
			pDialog.setMessage(progress[0]);

		}

	  *//**
	  * After completing background task Dismiss the progress dialog
	  * **//*
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

	}*/
}
