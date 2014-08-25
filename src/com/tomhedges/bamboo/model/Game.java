package com.tomhedges.bamboo.model;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Observable;
import java.util.Random;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.config.Constants.PlantState;
import com.tomhedges.bamboo.config.Constants.REMOTE_DATA_EXCHANGE_DATA_TYPE;
import com.tomhedges.bamboo.config.Constants.Season;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.rulesengine.RulesEngineController;
import com.tomhedges.bamboo.util.FileDownloader;
import com.tomhedges.bamboo.util.FileReaderAndWriter;
import com.tomhedges.bamboo.util.LocationRetrieve;
import com.tomhedges.bamboo.util.WeatherRetriever;
import com.tomhedges.bamboo.util.dao.ConfigDataSource;
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
	private FileReaderAndWriter fileReaderWriter;
	private FileDownloader downloader;
	private CoreSettings coreSettings;
	private Context context;
	private MatrixOfPlots mxPlots;
	private RulesEngineController rulesEngineController;
	private RemoteDBTableRetrieval remoteDataRetriever;
	private ConfigDataSource localDataRetriever;
	private PlantCatalogue plantCatalogue;
	private Objectives objectives;
	private WeatherRetriever weatherRetriever;
	private Weather gameWeather;
	private Calendar gameStartDate;
	private int numOfDaysPlayed;
	private LocationRetrieve locator;
	private Handler handler;
	private int iteration_time_delay;
	private boolean gameStarted;
	private boolean gameLoading;
	private boolean gameSaving;
	private boolean weatherRetrieved;
	private boolean seedsRetrieved;
	private boolean initialPlotUpdateSent;
	private int daysUntilNextRandomSeeding;
	private PlantInstance plantToUploadMaster;

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

	public class PlotWatered {
		int plotID;

		public int returnPlotID() {
			return plotID;
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

	public class SeedUploaded {
		String message;

		public String returnMessage() {
			return message;
		}
	}

	public class CompletedObjective {
		int objectiveID;
		String message;
		int totalNum;
		int numCompleted;

		public int returnID() {
			return objectiveID;
		}

		public String returnMessage() {
			return message;
		}

		public int returnTotalNum() {
			return totalNum;
		}

		public int returnNumCompleted() {
			return numCompleted;
		}
	}

	public class ObjectiveUpdate {
		int totalNum;
		int numCompleted;

		public int returnTotalNum() {
			return totalNum;
		}

		public int returnNumCompleted() {
			return numCompleted;
		}
	}

	public class GardenDimensions {
		int rows;
		int cols;

		public int returnRows() {
			return rows;
		}

		public int returnCols() {
			return cols;
		}
	}

	// Private constructor
	private Game(Context context){
		Log.w(Game.class.getName(), "Constructing game controller!");

		//core elements for initial construction
		this.context = context;
		fileReaderWriter = new FileReaderAndWriter();//context);
		rulesEngineController = RulesEngineController.getInstance(context);
		handler = new Handler();
		locator = new LocationRetrieve(context);
		weatherRetriever = new WeatherRetriever();
		weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
		remoteDataRetriever = new RemoteDBTableRetrieval();
		localDataRetriever = new ConfigDataSource(context);
		daysUntilNextRandomSeeding = getDaysUntilNextRandomSeeding();

		weatherRetrieved = false;
		seedsRetrieved = false;

		Log.w(Game.class.getName(), "Creating core settings preferences");
		CoreSettings.createCoreSettings(context);
		coreSettings = CoreSettings.accessCoreSettings();

		gameLoading = false;
		gameSaving = false;
	}

	public boolean startNewGame() {
		try {
			Log.w(SetupGame.class.getName(), "Game - STARTING NEW");
			boolean isNewGame = true;
			new SetupGame().execute(isNewGame);

			//Log.w(Game.class.getName(), "New game starting up!");
			return true;
		} catch (Exception ex) {
			Log.e(Game.class.getName(), "Failed to start new game...");
			return false;
		}
	}

	public boolean continueExistingGame() {
		try {
			Log.w(SetupGame.class.getName(), "Game - LOADING EXISTING");
			boolean isNewGame = false;
			new SetupGame().execute(isNewGame);

			//Log.w(Game.class.getName(), "Restarted existing game ok!");
			return true;
		} catch (Exception ex) {
			Log.e(Game.class.getName(), "Failed to restart game...");
			return false;
		}
	}

	public boolean savedGameExists() {
		File file = new File(context.getFilesDir() + Constants.FILENAME_LOCAL_GAME_SAVE);
		boolean exists = file.exists();
		
		if (exists) {
			Log.w(Game.class.getName(), "Saved game exists!");
			return true;
		} else {
			Log.w(Game.class.getName(), "Saved game does NOT exist!");
			return false;
		}
	}

	public void setUsername(String username) {
		coreSettings.addSetting(Constants.TAG_USERNAME, username);
	}


	private void completeGameSetup() {
		rulesEngineController.loadRules();
		setDateString();
		gameWeather = Weather.createWeather(gameStartDate, Constants.default_WEATHER_TEMPS, Constants.default_WEATHER_RAIN, Constants.default_WEATHER_SEASONS);

		gameStarted = false;
		initialPlotUpdateSent = false;

		nextIteration();
		Log.w(Game.class.getName(), "Game starting up!");
		preStartChecks("Starting up...");
		gameLoading = false;
		Log.w(SetupGame.class.getName(), "Game LOADED");
	}

	// Singleton Factory method
	public static Game getGameDetails(Context context) {
		if(game == null){
			game = new Game(context);
		}
		return game;
	}

	private int getDaysUntilNextRandomSeeding() {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(25) + 1;
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
		// TODO
		updateNeighbourhoods();
		if (gameStarted) rulesEngineController.fireRules();
		checkForCompletedObjectives();
		checkForUploadableSeeds();
		advanceDate();
		updateGameDetailsText();
		sendPlotStateUpdate();
		updateWeather();
	}

	private void preStartChecks(String message) {
		Log.w(Game.class.getName(), "Prestart checks: message=" + message + ", gameStarted=" + gameStarted + ", weatherRetrieved=" + weatherRetrieved + ", seedsRetrieved=" + seedsRetrieved);
		if (!gameStarted && weatherRetrieved && seedsRetrieved) {
			sendStartupUpdate(message);
		}
	}

	private void startGame() {
		Log.w(Game.class.getName(), "Starting GAME");
		gameStarted = true;
		startRepeatedActivity();
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void pauseGame() {
		Log.w(Game.class.getName(), "Pausing GAME");
		stopRepeatedActivity();
		locator.disconnect();
		if (localDataRetriever != null) {
			localDataRetriever.open();
		}
		stopAndSaveGame();
	}

	public void stopAndSaveGame() {
		if (!gameLoading && !gameSaving) {
			Log.w(Game.class.getName(), "Stopping and saving game!");
			gameSaving = true;

			SaveGame saveState = new SaveGame();
			saveState.mxPlotsSave = mxPlots;
			saveState.plantCatalogueSave = plantCatalogue;
			saveState.objectivesSave = objectives;
			saveState.day = gameStartDate.get(Calendar.DATE);
			saveState.month = gameStartDate.get(Calendar.MONTH);
			saveState.year = gameStartDate.get(Calendar.YEAR);
			saveState.numOfDaysPlayedSave = numOfDaysPlayed;

			if (fileReaderWriter.saveObject(saveState, context.getFilesDir() + Constants.FILENAME_LOCAL_GAME_SAVE)) {
				Log.w(Game.class.getName(), "Game successfully saved!");
			} else {
				Log.e(Game.class.getName(), "Game was not saved...");
			}

			Log.w(Game.class.getName(), "saveState which has been saved:" + 
					" mxPlots null? " + (saveState.mxPlotsSave == null) +
					",  plantCatalogue null? " + (saveState.plantCatalogueSave == null) +
					",  objectives null? " + (saveState.objectivesSave == null) +
					",  numOfDaysPlayed=" + (saveState.numOfDaysPlayedSave));
			
			mxPlots.destroy();
			plantCatalogue.destroy();
			objectives.destroy();
			gameStartDate = null;

			gameSaving = false;
			Log.w(Game.class.getName(), "Game stopping NOW!");
		} else {
			Log.w(Game.class.getName(), "Not stopping and saving as game is still loading...");
		}
	}

	public void resumeGame() {
		if (!gameLoading) {
			Log.w(Game.class.getName(), "Resuming GAME");
			if (gameStarted) {
				startRepeatedActivity();
			}

			if (savedGameExists()) {
				// Try removing this...
				loadExistingGame();
			}

			locator.connect();
			localDataRetriever.open();
		} else {
			Log.w(Game.class.getName(), "Not resuming as game is still loading...");
		}
	}

	private void loadExistingGame() {
		// TODO
		Log.w(Game.class.getName(), "Loading existing game from file...");

		SaveGame savedState = fileReaderWriter.loadSavedGame(context.getFilesDir() + Constants.FILENAME_LOCAL_GAME_SAVE);

		Log.w(Game.class.getName(), "savedState being loaded:" + 
				" mxPlots null? " + (savedState.mxPlotsSave == null) +
				",  plantCatalogue null? " + (savedState.plantCatalogueSave == null) +
				",  objectives null? " + (savedState.objectivesSave == null) +
				",  numOfDaysPlayed=" + (savedState.numOfDaysPlayedSave));

		
		Log.w(Game.class.getName(), "Current state: mxPlots null? " + (mxPlots == null) + ", mxPlots=" + mxPlots + ", MOP.getMatrix=" + MatrixOfPlots.getMatrix());
		mxPlots = savedState.mxPlotsSave;
		Log.w(Game.class.getName(), "New state: mxPlots null? " + (mxPlots == null) + ", mxPlots=" + mxPlots + ", MOP.getMatrix=" + MatrixOfPlots.getMatrix());
		plantCatalogue = savedState.plantCatalogueSave;
		objectives = savedState.objectivesSave;
		Log.w(Game.class.getName(), "Current state: gameStartDate null? " + (gameStartDate == null) + ", gameStartDate=" + gameStartDate);
		gameStartDate = Calendar.getInstance();
		gameStartDate.set(savedState.year, savedState.month, savedState.day);
		Log.w(Game.class.getName(), "Current state: gameStartDate null? " + (gameStartDate == null) + ", gameStartDate=" + gameStartDate);
		numOfDaysPlayed = savedState.numOfDaysPlayedSave;
			
		Log.w(Game.class.getName(), "...loaded existing game!");

		if (fileReaderWriter.deleteFile(context.getFilesDir() + Constants.FILENAME_LOCAL_GAME_SAVE)) {
			Log.w(Game.class.getName(), "Deleted saved game data");
		} else {
			Log.e(Game.class.getName(), "Could not delete saved game data");
		}
	}

	private void updateNeighbourhoods() {
		if (gameStarted) {
			//regular exercise of updating

			rulesEngineController.createRulesEngineSession(gameWeather.getCurrentTemp(), gameWeather.getCurrentRain());

			//Log.w(Game.class.getName(), "TEST 1");
			LinkedList<Neighbourhood> unwateredList = new LinkedList<Neighbourhood>();
			//Log.w(Game.class.getName(), "TEST 2");
			int loopCounter = 0;
			for (Neighbourhood neighbourhood : mxPlots.getNeighbourhoodMatrix()) {
				neighbourhood = rainAndSimplePlantWatering(neighbourhood, gameWeather.getCurrentRain(), gameWeather.getCurrentTemp());

				if (neighbourhood.getCentralPlot().getPlant() != null && neighbourhood.getCentralPlot().isPlantWatered() == false) {
					//build list of plots needing watering in pseudo-random order
					Random random = new Random();
					if (random.nextBoolean()) {
						unwateredList.addFirst(neighbourhood);
						Log.w(Game.class.getName(), "Added plot: " + neighbourhood.getCentralPlot().getPlotId() + " to FRONT of list needing watering");
					} else {
						unwateredList.addLast(neighbourhood);
						Log.w(Game.class.getName(), "Added plot: " + neighbourhood.getCentralPlot().getPlotId() + " to BACK of list needing watering");
					}
				}

				rulesEngineController.insertFact(neighbourhood.getCentralPlot());
				Log.w(Game.class.getName(), "Inserted Plot with ID: " + neighbourhood.getCentralPlot().getPlotId());
				loopCounter++;
			}

			while (unwateredList.size()>0) {
				int unwateredListLoopCounter = 0;
				while (unwateredListLoopCounter < unwateredList.size()) {
					Neighbourhood neighbourhood = unwateredList.get(unwateredListLoopCounter);
					int randomNeighbourWithWaterID = neighbourhood.getRandomNeighbourWithWaterID();

					if (randomNeighbourWithWaterID > -1) {
						neighbourhood.getNeighbour(randomNeighbourWithWaterID).setWaterLevel(neighbourhood.getNeighbour(randomNeighbourWithWaterID).getWaterLevel() - 1);
						neighbourhood.setImportedWater(neighbourhood.getImportedWater() + 1);
						Log.w(Game.class.getName(), "Found water for plant in plot: " + neighbourhood.getCentralPlot().getPlotId() + ". Increased to " + (neighbourhood.getCentralPlot().getWaterLevel() + neighbourhood.getImportedWater()) + ". Pulled one from plot: " + neighbourhood.getNeighbour(randomNeighbourWithWaterID).getPlotId() + ", leaving " + neighbourhood.getNeighbour(randomNeighbourWithWaterID).getWaterLevel());

						boolean plantWatered = localWaterAvailableForPlant(neighbourhood);
						if (plantWatered) {
							neighbourhood.getCentralPlot().setPlantWatered(plantWatered);
							// plant was watered, so remove from list!
							unwateredList.remove(neighbourhood);
							unwateredListLoopCounter--;

							rulesEngineController.insertFact(neighbourhood.getCentralPlot());
							Log.w(Game.class.getName(), "Inserted Plot with ID: " + neighbourhood.getCentralPlot().getPlotId());
						}
					} else {
						// plant cannot be watered, so remove from list, leaving it unwatered.
						unwateredList.remove(neighbourhood);
						unwateredListLoopCounter--;

						rulesEngineController.insertFact(neighbourhood.getCentralPlot());
						Log.w(Game.class.getName(), "Inserted Plot with ID: " + neighbourhood.getCentralPlot().getPlotId());

						Log.w(Game.class.getName(), "UNWATERED plant in plot: " + neighbourhood.getCentralPlot().getPlotId() + ". 4 - No water available!");
					}

					unwateredListLoopCounter++;
				}
			}

			rulesEngineController.insertFact(objectives);
			rulesEngineController.insertFact(mxPlots);


			//rulesEngineController.fireRules();
		} else {
			//one-off building at start of game!

			//this has already been done??
		}
	}

	private Neighbourhood rainAndSimplePlantWatering(Neighbourhood neighbourhoodToUpdate, int rainfall, int temperature) {
		boolean centralPlantWatered = false;

		boolean hasPlantAtCentre;
		if (neighbourhoodToUpdate.getCentralPlot().getPlant() == null) {
			hasPlantAtCentre = false;
		} else {
			hasPlantAtCentre = true;
		}

		// if not watery, rain on, otherwise, mark any plant as watered
		if (neighbourhoodToUpdate.getCentralPlot().getGroundState() != GroundState.WATER){
			int oldWaterLevel = neighbourhoodToUpdate.getCentralPlot().getWaterLevel();
			neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(rainfall);
			for (int loopCounter = 0; loopCounter < Constants.NEIGHBOURHOOD_STRUCTURE.length; loopCounter++) {
				// if plot is manufactured, its ID will be -1 : and it needs to get a water top up!
				if (neighbourhoodToUpdate.getNeighbour(loopCounter).getPlotId() == -1) {
					neighbourhoodToUpdate.getNeighbour(loopCounter).changeWaterLevel(rainfall/Constants.default_EDGE_PLOT_RESOURCE_DIVIDER);
				}
			}
			Log.w(Game.class.getName(), "Rainfall of " + rainfall + " on plot with ID: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". Water level changed from " + oldWaterLevel + " to " + neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
		} else {
			if (hasPlantAtCentre) {
				neighbourhoodToUpdate.getCentralPlot().setPlantWatered(true);
				centralPlantWatered = true;
				Log.w(Game.class.getName(), "Watered plant in plot: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". 1 - Water plot!");
			}
		}

		if (hasPlantAtCentre && !centralPlantWatered) {
			if (neighbourhoodToUpdate.isNeighbouringWaterPlot()) {
				neighbourhoodToUpdate.getCentralPlot().setPlantWatered(true);
				neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(0-neighbourhoodToUpdate.getCentralPlot().getPlant().getRequiredWater());
				if (neighbourhoodToUpdate.getCentralPlot().getWaterLevel() < 0) {
					neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(0 - neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
				}
				Log.w(Game.class.getName(), "Watered plant in plot: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". 2 - Neighbours a water plot! Water level now: " + neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
				centralPlantWatered = true;
			}
		}

		if (hasPlantAtCentre && !centralPlantWatered) {
			neighbourhoodToUpdate.getCentralPlot().setPlantWatered(localWaterAvailableForPlant(neighbourhoodToUpdate));
			centralPlantWatered = true;
		}

		return neighbourhoodToUpdate;
	}

	private boolean localWaterAvailableForPlant(Neighbourhood neighbourhood) {
		//do the necessary if there is water available locally	
		int centralPlotWaterLevel = neighbourhood.getCentralPlot().getWaterLevel();
		int importedWater = neighbourhood.getImportedWater();
		int plantReqWater = neighbourhood.getCentralPlot().getPlant().getRequiredWater();

		if (plantReqWater <= (centralPlotWaterLevel + importedWater)) {	
			neighbourhood.getCentralPlot().changeWaterLevel(0 - (plantReqWater - importedWater));
			neighbourhood.setImportedWater(0);
			//this line actually done higher up
			//neighbourhood.getCentralPlot().setPlantWatered(true);

			Log.w(Game.class.getName(), "Water available for plant in plot " + neighbourhood.getCentralPlot().getPlotId() + ", plant " + neighbourhood.getCentralPlot().getPlant().getType() +
					". plot water was: " + centralPlotWaterLevel + " plus imported water of: " + importedWater + ", plant water req: " + plantReqWater +
					". So plot water level now: " + neighbourhood.getCentralPlot().getWaterLevel());
			Log.w(Game.class.getName(), "Watered plant in plot: " + neighbourhood.getCentralPlot().getPlotId() + ". 3 - Local water!");

			//plant could be watered
			return true;
		} else {
			//plant could NOT be watered
			return false;
		}
	}

	private void checkForCompletedObjectives() {
		int[] completedObjectives = objectives.getRecentlyCompletedObjectives();
		for (int loopCounter = 0; loopCounter<completedObjectives.length; loopCounter++) {
			localDataRetriever.updateObjective(objectives.getObjective(completedObjectives[loopCounter]), true);

			CompletedObjective compObj = new CompletedObjective();
			compObj.objectiveID = completedObjectives[loopCounter];
			compObj.message = objectives.getObjective(completedObjectives[loopCounter]).getCompletionMessage();
			compObj.totalNum = objectives.getTotalNumberOfObjectives();
			compObj.numCompleted = objectives.getNumberOfCompletedObjectives();
			UpdateObservers(compObj);
		}
	}

	private void checkForUploadableSeeds() {
		for (Neighbourhood neighbourhood : mxPlots.getNeighbourhoodMatrix()) {
			if (neighbourhood.getCentralPlot().getPlant() != null && neighbourhood.getCentralPlot().getPlant().getPlantState() == PlantState.FLOWERING && neighbourhood.getCentralPlot().getPlant().getDaysInCurrentState() == 1) {
				//Freshly flowering plant! Upload seed...
				uploadSeed(neighbourhood.getCentralPlot().getPlant());
			}
		}
	}

	private void uploadSeed(PlantInstance seedToUpload) {
		plantToUploadMaster = seedToUpload;
		new RemoteDataExchange().execute(REMOTE_DATA_EXCHANGE_DATA_TYPE.UPLOAD_SEED);
	}

	public void advanceDate(){
		numOfDaysPlayed = numOfDaysPlayed + 1;
		gameStartDate.add(Calendar.DATE, 1);
		setDateString();

		if (!gameStarted || numOfDaysPlayed % Constants.default_GAME_WEATHER_RETRIEVE_FREQ == Constants.default_GAME_WEATHER_RETRIEVE_OFFSET) {
			new RemoteDataExchange().execute(REMOTE_DATA_EXCHANGE_DATA_TYPE.WEATHER);
		}

		if (!gameStarted || numOfDaysPlayed % Constants.default_GAME_REMOTE_SEEDS_RETRIEVE_FREQ == Constants.default_GAME_REMOTE_SEEDS_RETRIEVE_OFFSET) {
			new RemoteDataExchange().execute(REMOTE_DATA_EXCHANGE_DATA_TYPE.DOWNLOAD_SEEDS);
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

			//taken out to begin game from outside! now revoked...
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
		gdText.gameDetails = "Local Weather:\nTemperature = " + getRealTemp() + "\u00B0C\nRainfall = " + getRealRainfall() + "mm\nNumber of remote seeds = " + getRemoteSeedCount() + "\nLat = " + locator.getLocation().getLatitude() + "\nLong = " + locator.getLocation().getLongitude();
		UpdateObservers(gdText);
	}

	private void setDateString() {
		GameDate gameDate = new GameDate();
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
		WeatherValues weatherVals = new WeatherValues();
		weatherVals.temperature = gameWeather.getCurrentTemp();
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

	private class RemoteDataExchange extends AsyncTask<REMOTE_DATA_EXCHANGE_DATA_TYPE, Void, Void> {

		protected void onPreExecute() {
			Log.w(RemoteDataExchange.class.getName(), "Attempting remote data exchange from within Game");
		}

		@Override
		protected Void doInBackground(REMOTE_DATA_EXCHANGE_DATA_TYPE... params) {
			String message = "";
			switch (params[0]) {
			case DOWNLOAD_SEEDS:
				Log.w(RemoteDataExchange.class.getName(), "Attempting retrieval of remote seed data...");
				plantCatalogue.setRemoteSeedArray(remoteDataRetriever.getSeedingPlants(coreSettings.checkStringSetting(Constants.TAG_USERNAME), locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), Constants.default_DISTANCE_USER, Constants.default_DISTANCE_SPONSOR, new Date()));
				message = "Retrieved nearby seeds...";
				seedsRetrieved = true;
				Log.w(RemoteDataExchange.class.getName(), "Remote seed data retrieved");
				break;

			case WEATHER:
				Log.w(RemoteDataExchange.class.getName(), "Attempting retrieval of local weatherRetriever data...");
				weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
				message = "Retrieved local weatherRetriever details...";
				weatherRetrieved = true;
				Log.w(RemoteDataExchange.class.getName(), "Local weatherRetriever data retrieved");
				break;

			case UPLOAD_SEED:
				if (plantToUploadMaster != null) {
					Log.w(RemoteDataExchange.class.getName(), "Attempting to upload seed of flowering plant...");
					boolean seedUploadStatus = remoteDataRetriever.uploadSeed(new Date(),
							coreSettings.checkStringSetting(Constants.TAG_USERNAME),
							locator.getLocation().getLatitude(),
							locator.getLocation().getLongitude(),
							plantToUploadMaster.getId()
					);
					if (seedUploadStatus) {
						Log.w(RemoteDataExchange.class.getName(), "Seed upload successful!");
						SeedUploaded seedUploaded = new SeedUploaded();
						seedUploaded.message = "Seeds from your flowering " + plantToUploadMaster.getType() + " have been released into the environment!";
						UpdateObservers(seedUploaded);
						plantToUploadMaster = null;
					} else {
						Log.e(RemoteDataExchange.class.getName(), "Seed upload NOT successful!");
					}
				} else {
					Log.e(RemoteDataExchange.class.getName(), "No seed to upload to remote server!");
				}
				break;

			default:
				Log.e(RemoteDataExchange.class.getName(), "Attempted retrieval of unknown type!!!");
				break;
			}

			if (!gameStarted) {
				preStartChecks(message);
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

		int plotForPlanting = getPlotForPlanting(chosenPlant.getPreferredGroundState());

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

		RemoteSeed remoteSeed = plantCatalogue.getRandomRemoteSeed();
		GroundState gsPlantPrefers = plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()).getPreferredGroundState();

		int plotForPlanting = getPlotForPlanting(gsPlantPrefers);

		if (plotForPlanting > -1) {
			Log.w(Game.class.getName(), "Random plot selected");
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

	private int getPlotForPlanting(GroundState gsPlantPrefers) {
		//find plot with acceptable GroundState without plant already - if none,  return -1, else pick one randomly to return

		Random randomGenerator = new Random();
		int plotForPlanting = -1;
		int num_plots = mxPlots.getNumCols() * mxPlots.getNumRows();

		boolean needWater = false;
		if (gsPlantPrefers == GroundState.WATER) {
			needWater = true;
		}

		int[] possiblePlots = new int[mxPlots.getNumCols() * mxPlots.getNumRows()];	
		int totalPossPlots = 0;
		for (int loopCounter = 1; loopCounter<=num_plots; loopCounter++) {
			if ((getPlotFrom1BasedID(loopCounter).getPlant() == null && needWater && getPlotFrom1BasedID(loopCounter).getGroundState() == GroundState.WATER)
					|| (getPlotFrom1BasedID(loopCounter).getPlant() == null && !needWater && getPlotFrom1BasedID(loopCounter).getGroundState() != GroundState.WATER)) {
				possiblePlots[totalPossPlots] = getPlotFrom1BasedID(loopCounter).getPlotId();
				totalPossPlots++;
			}
		}

		if (totalPossPlots>0) {
			plotForPlanting = possiblePlots[randomGenerator.nextInt(totalPossPlots)];
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
			plotText = plotText + "\n" + localCopy.getPlant().getType() + "\nAge=" + localCopy.getPlant().getAge() + "\n" + localCopy.getPlant().getPlantState() + "\nDays in state=" + localCopy.getPlant().getDaysInCurrentState();
		}
		Log.w(Game.class.getName(), "Plot Text: " + plotText);
		return(plotText);
	}

	public String getPlotBasicFullPlotDetails(int plotID) {
		Plot localCopy = getPlotFrom1BasedID(plotID);
		return localCopy.toString();
	}

	public Objective[] getObjectiveList() {
		return objectives.getObjectiveList();
	}

	private void sendObjectiveUpdate() {
		ObjectiveUpdate ou = new ObjectiveUpdate();
		ou.totalNum = objectives.getTotalNumberOfObjectives();
		ou.numCompleted = objectives.getNumberOfCompletedObjectives();
		UpdateObservers(ou);
	}


	class SetupGame extends AsyncTask<Boolean, String, Boolean> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		private void sendMessage(String message, boolean readyToPlay) {
			GameStartup gs = new GameStartup();
			gs.readyToPlay = readyToPlay;
			gs.message = message;
			UpdateObservers(gs);
		}

		protected void onPreExecute() {
			super.onPreExecute();
			gameLoading = true;
			sendMessage("Setting up...", false);
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {

			/// NEED TO MAKE THIS WORK IF NO INTERNET CONNECTION - SHOULD BE EASY AS ALL DATA STORED LOCALLY!!!

			// for testing purposes - change to false for production! Forces all tables to update.
			boolean forceUpdate = false;

			// TODO loads existing game - need way to update plant catalogue, etc. mid game??
			boolean isNewGame = params[0];
			Log.w(SetupGame.class.getName(), "Is this a new game? " + isNewGame);
			if (!isNewGame) {
				publishProgress("Loading saved game state...");
				loadExistingGame();
			}

			publishProgress("Checking for updates to local data");
			localDataRetriever = new ConfigDataSource(context);
			localDataRetriever.open();
			Log.w(SetupGame.class.getName(), "Opened Data source!");
			Log.w(SetupGame.class.getName(), "Get local globals");
			Globals globalsLocal = localDataRetriever.getGlobals();

			// save URL, etc. to preferences
			coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, globalsLocal.getRootURL());

			// now in a seperate method...
			//coreSettings.addSetting(Constants.TAG_USERNAME, etUsername.getText().toString());

			remoteDataRetriever = new RemoteDBTableRetrieval();
			Globals globalsRemote = remoteDataRetriever.getGlobals();
			downloader = new FileDownloader();

			//Log.w(SetupGame.class.getName(), "globalsRemote last updated: " + globalsRemote.getLast_updated());
			//Log.w(SetupGame.class.getName(), "globalsLocal last updated: " + globalsLocal.getLast_updated())

			if (forceUpdate || globalsRemote.getLast_updated().after(globalsLocal.getLast_updated())) {
				publishProgress("Updating local data with updated values!");
				Log.w(SetupGame.class.getName(), "NEED TO UPDATE SOME LOCAL DATA!");

				// if update of local data is successful, then check for further updates...
				if (localDataRetriever.writeGlobals(globalsRemote)) {
					Log.w(SetupGame.class.getName(), "Get local table update values");

					TableLastUpdateDates tablesUpdatedLocal = localDataRetriever.getTableUpdateDates();
					Log.w(SetupGame.class.getName(), "ConfigValues date (local): " + tablesUpdatedLocal.getConfig().toString());
					TableLastUpdateDates tablesUpdatedRemote = remoteDataRetriever.getTableListing();
					Log.w(SetupGame.class.getName(), "ConfigValues date (remote): " + tablesUpdatedRemote.getConfig().toString());


					// CONFIG
					if (!isNewGame && (forceUpdate || tablesUpdatedRemote.getConfig().after(tablesUpdatedLocal.getConfig()))) {
						///WE ARE REMOTE!!!
						Log.w(SetupGame.class.getName(), "NEED TO UPDATE CONFIG TABLE!");
						publishProgress("Updating Configuration data with updated values!");
						ConfigValues remoteConfigValues = remoteDataRetriever.getConfig();

						if (localDataRetriever.writeConfig(remoteConfigValues) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_CONFIG, tablesUpdatedRemote.getConfig())) {
							Log.w(SetupGame.class.getName(), "Updated Config date in Tables table!");
							Log.w(SetupGame.class.getName(), "Updated Config data!");
							publishProgress("Local Config table data updated");

							Log.w(SetupGame.class.getName(), "Creating Plot matrix from remote data...");
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
								Log.w(SetupGame.class.getName(), "Created plot matrix from remote data!");
							} else {
								Log.e(SetupGame.class.getName(), "Could not create plot matrix from remote data!");
							}
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Config date and/or data...");
							publishProgress("Local ConfigValues data could not be updated");
						}
					}

					// PLANT TYPES
					if (forceUpdate || tablesUpdatedRemote.getPlants().after(tablesUpdatedLocal.getPlants())) {
						Log.w(SetupGame.class.getName(), "NEED TO UPDATE PLANTTYPES TABLE!");
						publishProgress("Updating Plant Types data with updated values!");
						PlantType[] remotePlantTypes = remoteDataRetriever.getPlantTypes();

						if (localDataRetriever.writePlantTypes(remotePlantTypes) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_PLANTTYPES, tablesUpdatedRemote.getPlants())) {
							Log.w(SetupGame.class.getName(), "Updated Plant Types data!");
							publishProgress("Local Plant Types table data updated");

							if (PlantCatalogue.createPlantCatalogue(remoteDataRetriever.getPlantTypes())) {
								Log.w(SetupGame.class.getName(), "Created plant catalogue!");
							} else {
								Log.e(SetupGame.class.getName(), "Could not create plant catalogue!");
							}
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Plant types date and/or data...");
							publishProgress("Local Plant types data could not be updated");
						}
					}

					// OBJECTIVES
					if (forceUpdate || tablesUpdatedRemote.getObjectives().after(tablesUpdatedLocal.getObjectives())) {
						Log.w(SetupGame.class.getName(), "NEED TO UPDATE OBJECTIVES TABLE + RULES FILE!");
						publishProgress("Updating Objectives data with updated values!");
						Objective[] remoteObjectives = remoteDataRetriever.getObjectives();

						if (localDataRetriever.writeObjectives(remoteObjectives) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_OBJECTIVES, tablesUpdatedRemote.getObjectives())) {
							Log.w(SetupGame.class.getName(), "Updated Objectives data!");
							publishProgress("Local Objectives table data updated");

							if (Objectives.createObjectives(remoteDataRetriever.getObjectives())) {
								Log.w(SetupGame.class.getName(), "Created local objectives list!");
							} else {
								Log.e(SetupGame.class.getName(), "Could not create objectives list!");
							}
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Objectives date and/or data...");
							publishProgress("Local Objectives data could not be updated");
						}

						String downloadFrom = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_OBJECTIVES;
						String saveTo = context.getFilesDir() + Constants.FILENAME_LOCAL_OBJECTIVES;
						Log.w(SetupGame.class.getName(), "Downloading updated objectives rules file from server. From: " + downloadFrom + ", To: " + saveTo);
						publishProgress("Downloading updated objectives rules file...");
						if (downloader.download(context, downloadFrom, saveTo)) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(SetupGame.class.getName(), "Unable to download updated objectives rules file from server.");
						}
					}

					// ITERATION RULES FILES
					if (forceUpdate || tablesUpdatedRemote.getIterationRules().after(tablesUpdatedLocal.getIterationRules())) {
						Log.w(SetupGame.class.getName(), "NEED TO UPDATE ITERATION RULES FILE!");

						String downloadFrom = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_ITERATION_RULES;
						String saveTo = context.getFilesDir() + Constants.FILENAME_LOCAL_ITERATION_RULES;
						Log.w(SetupGame.class.getName(), "Downloading updated iteration rules file from server. From: " + downloadFrom + ", To: " + saveTo);
						publishProgress("Downloading updated iteration rules file...");
						if (downloader.download(context, downloadFrom, saveTo)) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(SetupGame.class.getName(), "Unable to download updated iteration rules file from server.");
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

			// PLOT MATRIX - only build if empty and not loading existing game
			if (isNewGame && MatrixOfPlots.getMatrix() == null) {
				publishProgress("Setting up game from local details!");
				Log.w(SetupGame.class.getName(), "Creating Plot matrix from local data...");

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
					Log.w(SetupGame.class.getName(), "Created plot matrix from local data!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create plot matrix from local data!");
				}

				//should this be outside if statement?
				mxPlots = MatrixOfPlots.getMatrix();
			}


			GardenDimensions gardenDimensions = new GardenDimensions();
			gardenDimensions.cols = mxPlots.getNumCols();
			gardenDimensions.rows = mxPlots.getNumRows();
			UpdateObservers(gardenDimensions);

			// PLANT CATALOGUE - only build if empty
			if (PlantCatalogue.getPlantCatalogue() == null) {
				publishProgress("Building plant catalogue!");
				Log.w(SetupGame.class.getName(), "Building plant catalogue from local data...");

				if (PlantCatalogue.createPlantCatalogue(localDataRetriever.getPlantTypes())) {
					Log.w(SetupGame.class.getName(), "Created plant catalogue!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create plant catalogue!");
				}
			}

			// OBJECTIVES - only build if empty
			if (Objectives.getObjectives() == null) {
				publishProgress("Building Objectives list!");
				Log.w(SetupGame.class.getName(), "Building Objectives list from local data...");

				if (Objectives.createObjectives(localDataRetriever.getObjectives())) {
					Log.w(SetupGame.class.getName(), "Created local objectives list!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create objectives list!");
				}

				//Objectives objectives = Objectives.getObjectives();
				//objectives.addObjective(0, "test objective", "this is only a test", true);
				//objectives.addObjective(1, "Plant something!", "Your garden now has a plant!", false);
				//objectives.addObjective(2, "Flowering Carnation", "You have a blooming Carnation!", false);
				//objectives.addObjective(3, "Multiple flowering Carnations", "You several flowering Carnations!", false);
			}

			if (isNewGame) {
				//BUILD NEIGHBOURHOODS FROM MATRIX OF PLOTS, AND ADD
				int num_rows = mxPlots.getNumRows();
				int num_cols = mxPlots.getNumCols();
				Neighbourhood[] neighbourhoodArray = new Neighbourhood[num_rows * num_cols];
				for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
					for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
						Plot localPlot = mxPlots.getPlot(columnCounter+1, rowCounter+1);
						Neighbourhood neigbourhoodToInsert = new Neighbourhood(localPlot, Constants.NEIGHBOURHOOD_STRUCTURE.length);

						int xPosCentral = localPlot.getXPosInMatrix();
						int yPosCentral = localPlot.getYPosInMatrix();

						Log.w(SetupGame.class.getName(), "Building " +  Constants.NEIGHBOURHOOD_STRUCTURE.length + " plot neighbourhood for plot: " + localPlot.getPlotId() + ", at X=" + xPosCentral + ",Y=" + yPosCentral + ". Plot marked as 'neighbourhood created'?: " + localPlot.isNeighbourhoodCreated());

						for (int[] neighbourRelPos : Constants.NEIGHBOURHOOD_STRUCTURE) {
							neigbourhoodToInsert.addNeighbour(mxPlots.getNeigbouringPlot(xPosCentral, yPosCentral, neighbourRelPos[0], neighbourRelPos[1]));
						}

						localPlot.setNeighbourhoodCreated(true);
						neighbourhoodArray[localPlot.getPlotId()-1] = neigbourhoodToInsert;
						Log.w(SetupGame.class.getName(), "Built neighbourhood of: " + neigbourhoodToInsert.getNeighbourCounter() + " plots with central plot id " + localPlot.getPlotId());

						// DROOLS LINE - PUT INTO GAME???S ksession.insert(localPlot);
						//Log.w(SetupGame.class.getName(), "Inserted plot with ID: " + localPlot.getPlotId());
					}
				}
				mxPlots.setNeighbourhoodMatrix(neighbourhoodArray);

				Log.w(SetupGame.class.getName(), "Created neighbourhood matrix from plot matrix data!");
			}

			publishProgress("Starting game...");
			return isNewGame;
		}

		protected void onProgressUpdate(String... progress) {
			sendMessage(progress[0], false);
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(Boolean isNewGame) {
			//now not wanted???
			//localDataRetriever.close();


			//for new game or resumed game, handle differently:
			// moved up... mxPlots = MatrixOfPlots.getMatrix();
			//what do do with core setting when not a new game???

			if (isNewGame) {
				Log.w(SetupGame.class.getName(), "Set remaining key values for new game");
				plantCatalogue = PlantCatalogue.getPlantCatalogue();
				objectives = Objectives.getObjectives();
				gameStartDate = Calendar.getInstance();
				gameStartDate.add(Calendar.DATE, -1);
				numOfDaysPlayed = -1;
			} else {
				Log.w(SetupGame.class.getName(), "Remaining key values already set - loading existing game");
			}
			iteration_time_delay = checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
			sendObjectiveUpdate();

			completeGameSetup();
		}
	}

	public void WaterPlotWithID(int plotID) {
		Plot plotBeingWatered = getPlotFrom1BasedID(plotID);
		int oldWaterLevel = plotBeingWatered.getWaterLevel();
		plotBeingWatered.changeWaterLevel(Constants.default_WateringAmount);
		PlotWatered sw = new PlotWatered();
		sw.plotID = plotID;
		Log.w(Game.class.getName(), "Player watered plot with ID: " + plotID + ". Water level changed from " + oldWaterLevel + " to " + getPlotFrom1BasedID(plotID).getWaterLevel());
		UpdateObservers(sw);
	}
}
