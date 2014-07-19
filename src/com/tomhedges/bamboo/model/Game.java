package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.util.LocationRetrieve;
import com.tomhedges.bamboo.util.WeatherRetriever;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class Game extends Observable {

	private static Game game = null;
	private CoreSettings coreSettings;
	private Context context;
	private MatrixOfPlots mxPlots;
	private RemoteDBTableRetrieval remoteDataRetriever;
	private PlantCatalogue plantCatalogue;
	private WeatherRetriever weather;
	private Calendar gameStartDate;
	private int numOfDaysPlayed;
	private String gameDate;
	private GameDate gd2;
	private LocationRetrieve locator;
	private Handler handler;
	private int iteration_time_delay;
	
	public class GameDate {
		String gameDate;
		
		public String returnDate() {
			return gameDate;
		}
	}
	
	public class GameDetailsText {
		String gameDetails;
		
		public String returnDetails() {
			return gameDetails;
		}
	}
	
	public class PlotDetails {
		int plotID;
		String plotBasicText;
		String plotPlotFullDetails;

		public int returnPlotID() {
			return plotID;
		}

		public String returnPlotBasicText() {
			return plotBasicText;
		}
		
		public String returnPlotPlotFullDetails() {
			return plotPlotFullDetails;
		}
	}
	
	
	// Private constructor
	private Game(Context context){
		this.context = context;
		coreSettings = CoreSettings.accessCoreSettings();
		plantCatalogue = PlantCatalogue.getPlantCatalogue();
		mxPlots = MatrixOfPlots.getMatrix();
		handler = new Handler();

		gameStartDate = Calendar.getInstance();
		numOfDaysPlayed = 0;
		locator = new LocationRetrieve(context);
		weather = new WeatherRetriever();
		weather.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
		remoteDataRetriever = new RemoteDBTableRetrieval();
		setDateString();
		
		iteration_time_delay = checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
	}

	// Singleton Factory method
	public static Game getGameDetails(Context context) {
		if(game == null){
			game = new Game(context);
		}
		return game;
	}

	private void UpdateObservers(Object objectUpdated) {
		setChanged();
		notifyObservers(objectUpdated);
	}

	private void startRepeatedActivity() {
		handler.postDelayed(runnable, iteration_time_delay);
	}

	private void stopRepeatedActivity() {
		handler.removeCallbacks(runnable);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			/* do what you need to do */
			nextIteration();
			/* and here comes the "trick" */
			handler.postDelayed(this, iteration_time_delay);
		}
	};

	private void nextIteration() {
		advanceDate();
		updateGameDetailsText();
		sendPlotStateUpdate();
	}

	public void startGame() {
		// nothing to do here at present...
	}
	
	public void pauseGame() {
		stopRepeatedActivity();
		locator.disconnect();
	}

	public void resumeGame() {
		startRepeatedActivity();
		locator.connect();
	}
	
	public void advanceDate(){
		numOfDaysPlayed = numOfDaysPlayed + 1;
		gameStartDate.add(Calendar.DATE, 1);
		setDateString();

		if (numOfDaysPlayed == 1 || numOfDaysPlayed % 20 == 0) {
			weather.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
		}

		if (numOfDaysPlayed == 1 || numOfDaysPlayed % 20 == 10) {
			new RetrieveRemoteData().execute(1);
		}
	}

	private void updateGameDetailsText() {
		GameDetailsText gdText = new GameDetailsText();
		gdText.gameDetails = "Local Weather:\nTemperature = " + getRealTemp() + "\u00B0C\nRainfall = " + getRealRainfall() + "mm\nNumber of remote seeds = " + getRemoteSeedCount();
		UpdateObservers(gdText);
	}

	private void setDateString() {
		gameDate = gameStartDate.get(Calendar.DATE) + " " + gameStartDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + gameStartDate.get(Calendar.YEAR);
		GameDate gd2 = new GameDate();
		gd2.gameDate = gameDate;
		Log.w(Game.class.getName(), "Date is: " + gameDate);

		UpdateObservers(gd2);
	}

	private void sendPlotStateUpdate() {
		for (int loopCounter = 1; loopCounter <= (mxPlots.getNumCols() * mxPlots.getNumRows()); loopCounter++) {
			PlotDetails plotUpdate = new PlotDetails();
			plotUpdate.plotID = loopCounter;
			plotUpdate.plotBasicText = getPlotBasicText(loopCounter);
			plotUpdate.plotPlotFullDetails = getPlotBasicFullPlotDetails(loopCounter);
			
			UpdateObservers(plotUpdate);
		}
	}

	public String getDateString(){
		return gameDate;
	}

	public int getRealTemp() {
		return weather.getTemperature();
	}

	public int getRealRainfall() {
		return weather.getRainfall();
	}

	public int getRemoteSeedCount() {
		return plantCatalogue.getRemoteSeedCount();
	}

	private class RetrieveRemoteData extends AsyncTask<Integer, Void, Void> {

		protected void onPreExecute() {
			Log.w(RetrieveRemoteData.class.getName(), "Attempting retrieval of remote data from within Game");
		}

		@Override
		protected Void doInBackground(Integer... params) {
			switch (params[0]) {
			case 1:
				Log.w(RetrieveRemoteData.class.getName(), "Attempting retrieval of remote seed data...");
				plantCatalogue.setRemoteSeedArray(remoteDataRetriever.getSeedingPlants(locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), Constants.default_DISTANCE_USER, Constants.default_DISTANCE_SPONSOR, new Date()));
				Log.w(RetrieveRemoteData.class.getName(), "Remote seed data retrieved");
				break;
			default:
				Log.e(RetrieveRemoteData.class.getName(), "Attempted retrieval of unknown type!!!");
				break;
			}
			return null;
		}

	}


	public SubMenu getSubMenuPlantTypes(MenuItem rootMenuItem) {
		SubMenu submenu = rootMenuItem.getSubMenu();
		submenu.clear();
		PlantType[] plantArray = plantCatalogue.getPlantsSimple();
		for (int loopCounter = 0; loopCounter < plantArray.length; loopCounter++) {
			submenu.add(Constants.MENU_GROUP_PLANT_TYPES, Constants.PLANT_TYPE_MENU_ID_START_RANGE + plantArray[loopCounter].getPlantTypeId(), Menu.NONE, plantArray[loopCounter].getType());
		}
		return submenu;
	}

	public int getPlantTypesCount() {
		return plantCatalogue.getPlantTypeCountSimple();
	}

	public PlantType getPlantTypeByPlantTypeID(int id) {
		return plantCatalogue.getPlantTypeByPlantTypeID(id);
	}

	public void plantSeed(int plantTypeID, int plotID) {
		//What ID to use for plant instance id??? Plot for now...
		getPlotFrom1BasedID(plotID).setPlant(new PlantInstance(getPlantTypeByPlantTypeID(plantTypeID), plotID));
	}

	public void uprootPlant(int plotId) {
		getPlotFrom1BasedID(plotId).removePlant();
	}

	public int getNumPlotRows() {
		return mxPlots.getNumRows();
	}

	public int getNumPlotCols() {
		return mxPlots.getNumCols();
	}

	public Plot getPlotFromCoords(int xPos, int yPos) {
		return mxPlots.getPlot(xPos, yPos);
	}

	public int getXPosFromID(int plotID) {
		return ((plotID-1) % getNumPlotCols()) + 1;
	}

	public int getYPosFromID(int plotID) {
		return ((plotID-1) / getNumPlotCols()) + 1;
	}

	public Plot getPlotFrom1BasedID(int plotID) {
		int xPos = ((plotID-1) % getNumPlotCols()) + 1;
		int yPos = ((plotID-1) / getNumPlotCols()) + 1;
		return mxPlots.getPlot(xPos, yPos);
	}

	public int checkIntSetting(String settingName) {
		return coreSettings.checkIntSetting(settingName);
	}

	public String getPlotBasicText(int plotID) {
		Plot localCopy = game.getPlotFrom1BasedID(plotID);
		String plotText = localCopy.getGroundState().toString();
		if (localCopy.getPlant() == null) {
			plotText = plotText + "\nNo plant";
		} else {
			plotText = plotText + "\n" + localCopy.getPlant().getType();
		}
		Log.w(Game.class.getName(), "Plot Text: " + plotText);
		return(plotText);
	}

	public String getPlotBasicFullPlotDetails(int plotID) {
		Plot localCopy = game.getPlotFrom1BasedID(plotID);
		return localCopy.toString();
	}


}