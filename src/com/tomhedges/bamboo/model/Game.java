package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Random;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.RETRIEVE_REMOTE_DATA_TYPE;
import com.tomhedges.bamboo.config.Constants.Season;
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
	private WeatherRetriever weatherRetriever;
	private Weather gameWeather;
	private Calendar gameStartDate;
	private int numOfDaysPlayed;
	private LocationRetrieve locator;
	private Handler handler;
	private int iteration_time_delay;
	private boolean gameStarted;
	private boolean weatherRetrieved;
	private boolean seedsRetrieved;
	private boolean initialPlotUpdateSent;
	private int daysUntilNextRandomSeeding;

	public class GameDate {
		String gameDateString;

		public String returnDate() {
			return gameDateString;
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

	public class SeedPlanted {
		int plotID;
		String plantType;
		String username;
		boolean isSponsered;
		boolean isRemote;

		public int returnPlotID() {
			return plotID;
		}

		public String returnPlantType() {
			return plantType;
		}

		public String returnUsername() {
			return username;
		}

		public boolean returnIsSponsored() {
			return isSponsered;
		}
		public boolean returnIsRemote() {
			return isRemote;
		}
	}

	public class GameStartup {
		boolean readyToPlay;
		String message;


		public boolean returnReadyToPlay() {
			return readyToPlay;
		}

		public String returnMessage() {
			return message;
		}
	}

	public class WeatherValues {
		int temperature;
		int rainfall;
		Season season;

		public int returnTemperature() {
			return temperature;
		}

		public int returnRainfall() {
			return rainfall;
		}

		public Season returnSeason() {
			return season;
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
		gameStartDate.add(Calendar.DATE, -1);
		numOfDaysPlayed = -1;
		locator = new LocationRetrieve(context);
		weatherRetriever = new WeatherRetriever();
		weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
		remoteDataRetriever = new RemoteDBTableRetrieval();
		setDateString();

		gameWeather = Weather.createWeather(gameStartDate, Constants.default_WEATHER_TEMPS, Constants.default_WEATHER_RAIN, Constants.default_WEATHER_SEASONS);

		iteration_time_delay = checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
		gameStarted = false;
		weatherRetrieved = false;
		seedsRetrieved = false;
		initialPlotUpdateSent = false;

		daysUntilNextRandomSeeding = getDaysUntilNextRandomSeeding();

		//Carry out once to set up!
		nextIteration();
	}

	private int getDaysUntilNextRandomSeeding() {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(25) + 1;
	}

	// Singleton Factory method
	public static Game getGameDetails(Context context) {
		if(game == null){
			game = new Game(context);
		}
		return game;
	}

	private void UpdateObservers(Object objectUpdated) {
		Log.w(Game.class.getName(), "Sending update to Observers!");
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
		updateWeather();
	}

	private void startGame() {
		gameStarted = true;
		startRepeatedActivity();
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void pauseGame() {
		stopRepeatedActivity();
		locator.disconnect();
	}

	public void resumeGame() {
		if (gameStarted) {startRepeatedActivity();}
		locator.connect();
	}

	public void advanceDate(){
		numOfDaysPlayed = numOfDaysPlayed + 1;
		gameStartDate.add(Calendar.DATE, 1);
		setDateString();

		if (!gameStarted || numOfDaysPlayed % 20 == 0) {
			new RetrieveRemoteData().execute(RETRIEVE_REMOTE_DATA_TYPE.WEATHER);
		}

		if (!gameStarted || numOfDaysPlayed % 20 == 10) {
			new RetrieveRemoteData().execute(RETRIEVE_REMOTE_DATA_TYPE.SEEDS);
		}

		if (daysUntilNextRandomSeeding == 0) {
			Random randomGenerator = new Random();
			if (randomGenerator.nextBoolean() && plantCatalogue.getRemoteSeedCount() > 0) {
				Log.w(Game.class.getName(), "Planting remote seed...");
				plantRandomRemoteSeed();
			} else {
				Log.w(Game.class.getName(), "Planting local seed...");
				plantRandomLocalSeed(); 
			}
			daysUntilNextRandomSeeding = getDaysUntilNextRandomSeeding();
		}
		daysUntilNextRandomSeeding--;

	}

	private void sendStartupUpdate(String message) {
		GameStartup gs = new GameStartup();
		if (weatherRetrieved && seedsRetrieved && initialPlotUpdateSent) {
			Log.w(Game.class.getName(), "Ready to start game!");
			gs.readyToPlay = true;
			startGame();
		} else {
			gs.readyToPlay = false;
		}
		gs.message = message;
		Log.w(Game.class.getName(), "Sending update to UI!");
		UpdateObservers(gs);
	}

	private void updateGameDetailsText() {
		GameDetailsText gdText = new GameDetailsText();
		gdText.gameDetails = "Local Weather:\nTemperature = " + getRealTemp() + "\u00B0C\nRainfall = " + getRealRainfall() + "mm\nNumber of remote seeds = " + getRemoteSeedCount();
		UpdateObservers(gdText);
	}

	private void setDateString() {
		GameDate gameDate  = new GameDate();
		gameDate.gameDateString = gameStartDate.get(Calendar.DATE) + " " + gameStartDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + gameStartDate.get(Calendar.YEAR);
		Log.w(Game.class.getName(), "Date is: " + gameDate.gameDateString);

		UpdateObservers(gameDate);
	}

	private void sendPlotStateUpdate() {
		for (int loopCounter = 1; loopCounter <= (mxPlots.getNumCols() * mxPlots.getNumRows()); loopCounter++) {
			PlotDetails plotUpdate = singlePlotDetails(loopCounter);

			UpdateObservers(plotUpdate);
		}
		initialPlotUpdateSent = true;
	}

	private PlotDetails singlePlotDetails(int plotID) {
		PlotDetails plotUpdate = new PlotDetails();
		plotUpdate.plotID = plotID;
		plotUpdate.plotBasicText = getPlotBasicText(plotID);
		plotUpdate.plotPlotFullDetails = getPlotBasicFullPlotDetails(plotID);

		return plotUpdate;
	}

	private void updateWeather() {
		gameWeather.calculateWeatherUpdate();
		int newTemp = gameWeather.getCurrentTemp();
		WeatherValues weatherVals = new WeatherValues();
		weatherVals.temperature = newTemp;
		weatherVals.rainfall = gameWeather.getCurrentRain();
		weatherVals.season = gameWeather.getCurrentSeason();

		UpdateObservers(weatherVals);
	}

	public int getRealTemp() {
		return weatherRetriever.getTemperature();
	}

	public int getRealRainfall() {
		return weatherRetriever.getRainfall();
	}

	public int getRemoteSeedCount() {
		return plantCatalogue.getRemoteSeedCount();
	}

	private class RetrieveRemoteData extends AsyncTask<RETRIEVE_REMOTE_DATA_TYPE, Void, Void> {

		protected void onPreExecute() {
			Log.w(RetrieveRemoteData.class.getName(), "Attempting retrieval of remote data from within Game");
		}

		@Override
		protected Void doInBackground(RETRIEVE_REMOTE_DATA_TYPE... params) {
			String message = "";
			switch (params[0]) {
			case SEEDS:
				Log.w(RetrieveRemoteData.class.getName(), "Attempting retrieval of remote seed data...");
				plantCatalogue.setRemoteSeedArray(remoteDataRetriever.getSeedingPlants(coreSettings.checkStringSetting(Constants.TAG_USERNAME), locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), Constants.default_DISTANCE_USER, Constants.default_DISTANCE_SPONSOR, new Date()));
				message = "Retrieved nearby seeds...";
				weatherRetrieved = true;
				Log.w(RetrieveRemoteData.class.getName(), "Remote seed data retrieved");
				break;

			case WEATHER:
				Log.w(RetrieveRemoteData.class.getName(), "Attempting retrieval of local weatherRetriever data...");
				weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
				message = "Retrieved local weatherRetriever details...";
				seedsRetrieved = true;
				Log.w(RetrieveRemoteData.class.getName(), "Local weatherRetriever data retrieved");
				break;

			default:
				Log.e(RetrieveRemoteData.class.getName(), "Attempted retrieval of unknown type!!!");
				break;
			}

			if (!gameStarted) {
				sendStartupUpdate(message);
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


	private void plantRandomLocalSeed() {
		int localSeedCommonnessTotal = 0;
		for (PlantType plant : plantCatalogue.getPlantsSimple()) {
			localSeedCommonnessTotal = localSeedCommonnessTotal + plant.getCommonnessFactor();
		}

		Random randomGenerator = new Random();
		int chosen = randomGenerator.nextInt(localSeedCommonnessTotal) + 1;

		int lowerBound = 0;
		PlantType chosenPlant = null;
		int loopCounter = 0;
		PlantType[] plantArray = plantCatalogue.getPlantsSimple();
		do {
			if (chosen>lowerBound && chosen<= (lowerBound + plantArray[loopCounter].getCommonnessFactor())) {
				chosenPlant = plantArray[loopCounter];
			}
			lowerBound = lowerBound + plantArray[loopCounter].getCommonnessFactor();
			loopCounter++;
		} while (chosenPlant == null);

		int plotForPlanting = getPlotForPlanting();
		
		if (plotForPlanting > -1) {
			Log.w(Game.class.getName(), "Random plot selected");
			
			PlantInstance newLocalPlant = new PlantInstance(chosenPlant, plotForPlanting);
			getPlotFrom1BasedID(plotForPlanting).setPlant(newLocalPlant);
			Log.w(Game.class.getName(), "Random seed planted in plot: " + plotForPlanting);

			SeedPlanted locSeedPlanted = new SeedPlanted();
			locSeedPlanted.plotID = plotForPlanting;
			locSeedPlanted.plantType = chosenPlant.getType();
			locSeedPlanted.isRemote = false;

			Log.w(Game.class.getName(), "Returning random plant notification to obervers: Local plant=" + locSeedPlanted.plantType);

			PlotDetails plotUpdate = singlePlotDetails(locSeedPlanted.plotID);
			UpdateObservers(plotUpdate);
			UpdateObservers(locSeedPlanted);
		} else {
			Log.w(Game.class.getName(), "No plot selected! So no planting happening");
		}
	}
	
	private void plantRandomRemoteSeed() {
		Log.w(Game.class.getName(), "Random seed being planted...");

		int plotForPlanting = getPlotForPlanting();

		if (plotForPlanting > -1) {
			Log.w(Game.class.getName(), "Random plot selected");
			RemoteSeed remoteSeed = plantCatalogue.getRandomRemoteSeed();
			PlantInstance newRemotePlant;
			boolean isSponsored = remoteSeed.isSponsored();
			if (isSponsored) {
				newRemotePlant = new PlantInstance(plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()), plotForPlanting, remoteSeed.getUsername(), remoteSeed.getMessage(), remoteSeed.getSuccess_copy());
			} else {
				newRemotePlant = new PlantInstance(plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()), plotForPlanting);
			}
			getPlotFrom1BasedID(plotForPlanting).setPlant(newRemotePlant);
			Log.w(Game.class.getName(), "Random seed planted in plot: " + plotForPlanting);

			SeedPlanted remSeedPlanted = new SeedPlanted();
			remSeedPlanted.plotID = plotForPlanting;
			remSeedPlanted.username = remoteSeed.getUsername();
			remSeedPlanted.isSponsered = isSponsored;
			remSeedPlanted.isRemote = true;
			remSeedPlanted.plantType = plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()).getType();

			Log.w(Game.class.getName(), "Returning random plant notification to obervers: From=" + remSeedPlanted.username + ", Plant=" + remSeedPlanted.plantType + ", isSponsored=" + remSeedPlanted.isSponsered);

			PlotDetails plotUpdate = singlePlotDetails(remSeedPlanted.plotID);
			UpdateObservers(plotUpdate);
			UpdateObservers(remSeedPlanted);
		} else {
			Log.w(Game.class.getName(), "No plot selected! So no planting happening");
		}
	}

	private int getPlotForPlanting() {
		Random randomGenerator = new Random();
		//find plot without plant already - but only try as many times as there are plots, to prevent infinite loop if all hold plants
		int plotForPlanting = -1;
		int loopLimiter = 0;
		int num_plots = mxPlots.getNumCols() * mxPlots.getNumRows();
		while (plotForPlanting == -1 && loopLimiter <= num_plots) {
			int temp = randomGenerator.nextInt(num_plots) + 1;
			if (getPlotFrom1BasedID(temp).getPlant() == null) {
				plotForPlanting = temp;
			}
			loopLimiter++;
		}
		return plotForPlanting;
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
		Plot localCopy = getPlotFrom1BasedID(plotID);
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
		Plot localCopy = getPlotFrom1BasedID(plotID);
		return localCopy.toString();
	}
}
