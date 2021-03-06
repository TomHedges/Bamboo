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
import com.tomhedges.bamboo.config.Constants.RemoteDataExchangeDataType;
import com.tomhedges.bamboo.config.Constants.Season;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.rulesengine.RulesEngineController;
import com.tomhedges.bamboo.util.FileDownloader;
import com.tomhedges.bamboo.util.FileReaderAndWriter;
import com.tomhedges.bamboo.util.LocationRetrieve;
import com.tomhedges.bamboo.util.WeatherRetriever;
import com.tomhedges.bamboo.util.dao.LocalDBDataRetrieval;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

/**
 * The Game class is at the core of the business logic. It combines the functions of: regulating the iterations of the cellular automata;
 * creating and maintaining the other objects which contain the key data within the model; co-ordinating the exchange of data between them,
 * and between remote resources; saving and loading the key data between sessions of gameplay; and providing an interface to the UI when it
 * needs to interrogate the Game for further data (displaying menus, etc.). 
 * 
 * @see			Plot
 * @see			MatrixOfPlots
 * @author      Tom Hedges
 */

public class Game extends Observable {

	private static Game game = null;
	private FileReaderAndWriter fileReaderWriter;
	private FileDownloader downloader;
	private CoreSettings coreSettings;
	private Context context;
	private MatrixOfPlots mxPlots;
	private RulesEngineController rulesEngineController;
	private RemoteDBTableRetrieval remoteDataRetriever;
	private LocalDBDataRetrieval localDataRetriever;
	private PlantCatalogue plantCatalogue;
	private Objectives objectives;
	private WeatherRetriever weatherRetriever;
	private Weather gameWeather;
	private Calendar gameDateInPlay;
	private int numOfDaysPlayed;
	private LocationRetrieve locator;
	private Handler handler;
	private int iteration_time_delay;
	private boolean gameStarted = false;
	private boolean gameLoading;
	private boolean gameSaving;
	private boolean weatherRetrieved;
	private boolean seedsRetrieved;
	private boolean initialPlotUpdateSent;
	private int daysUntilNextRandomSeeding;
	private int waterAllowance;
	private int daysSinceLastWeatherMessage = 0;
	private String errorMessage;
	private static enum GameStartMode {
		NEW_GAME,
		LOAD_GAME,
		REDISPLAY
	}
	private GameStartMode gameStartMode;

	public class GameDate {
		private String gameDateString;
		private int dayInYear;
		public int startOfSpring;

		public String returnDate() {
			return gameDateString;
		}

		public int returnDayInYear() {
			return dayInYear;
		}

		public int returnStartOfSpring() {
			return startOfSpring;
		}
	}

	public class GameDetailsText {
		private String gameDetails;

		public String returnDetails() {
			return gameDetails;
		}
	}

	public class ErrorMessage {
		private String errorMessage;

		public String returnErrorMessage() {
			return errorMessage;
		}
	}

	public class WeatherMessage {
		private String weatherMessage;

		public String returnWeatherMessage() {
			return weatherMessage;
		}
	}

	public class PlotDetails {
		private int plotID;
		private String plotBasicText;
		private String plotPlotFullDetails;
		private boolean hasPlant;

		public int returnPlotID() {
			return plotID;
		}

		public String returnPlotBasicText() {
			return plotBasicText;
		}

		public String returnPlotPlotFullDetails() {
			return plotPlotFullDetails;
		}

		public boolean returnHasPlant() {
			return hasPlant;
		}
	}

	public class SeedPlanted {
		private int plotID;
		private String plantType;
		private String username;
		private boolean isSponsered;
		private boolean isRemote;

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
		private int plotID;

		public int returnPlotID() {
			return plotID;
		}
	}

	public class GameStartup {
		private boolean readyToPlay;
		private String message;

		public boolean returnReadyToPlay() {
			return readyToPlay;
		}

		public String returnMessage() {
			return message;
		}
	}

	public class WeatherValues {
		private int temperature;
		private int rainfall;
		private Season season;

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
		private String message;

		public String returnMessage() {
			return message;
		}
	}

	public class SponsoredSeedUnlocked {
		private String originUsername;
		private String sponsoredMessage;
		private String successCopy;

		public String returnOriginUsername() {
			return originUsername;
		}

		public String returnSponsoredMessage() {
			return sponsoredMessage;
		}

		public String returnSuccessCopy() {
			return successCopy;
		}
	}

	public class CompletedObjective {
		private int objectiveID;
		private String message;
		private int totalNum;
		private int numCompleted;

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

	public class WaterAllowanceLevel {
		private int waterAllowance;

		public int returnWaterAllowance() {
			return waterAllowance;
		}
	}

	public class ObjectiveUpdate {
		private int totalNum;
		private int numCompleted;

		public int returnTotalNum() {
			return totalNum;
		}

		public int returnNumCompleted() {
			return numCompleted;
		}
	}

	public class GardenDimensions {
		private int rows;
		private int cols;

		public int returnRows() {
			return rows;
		}

		public int returnCols() {
			return cols;
		}
	}

	// Private constructor
	private Game(Context context){
		Log.d(Game.class.getName(), "Constructing game controller!");

		//core elements for initial construction
		this.context = context;
		fileReaderWriter = new FileReaderAndWriter();
		rulesEngineController = RulesEngineController.getInstance(context);
		handler = new Handler();
		locator = new LocationRetrieve(context);
		weatherRetriever = new WeatherRetriever();
		weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
		remoteDataRetriever = new RemoteDBTableRetrieval();
		localDataRetriever = new LocalDBDataRetrieval(context);
		daysUntilNextRandomSeeding = getDaysUntilNextRandomSeeding();

		weatherRetrieved = false;
		seedsRetrieved = false;

		Log.d(Game.class.getName(), "Creating core settings preferences");
		CoreSettings.createCoreSettings(context);
		coreSettings = CoreSettings.accessCoreSettings();

		gameLoading = false;
		gameSaving = false;
	}

	public boolean startNewGame() {
		try {
			Log.d(SetupGame.class.getName(), "Game - STARTING NEW");
			gameStarted = false;
			boolean isNewGame = true;
			new SetupGame().execute(isNewGame);
			return true;
			
		} catch (Exception ex) {
			Log.e(Game.class.getName(), "Failed to start new game...");
			return false;
		}
	}

	public boolean continueExistingGame() {
		try {
			Log.d(SetupGame.class.getName(), "Game - LOADING EXISTING");
			boolean isNewGame = false;
			new SetupGame().execute(isNewGame);
			return true;
			
		} catch (Exception ex) {
			Log.e(Game.class.getName(), "Failed to restart game...");
			return false;
		}
	}

	public boolean savedGameExists() {
		File file = new File(context.getFilesDir() + "/" + Constants.FILENAME_LOCAL_GAME_SAVE);
		boolean exists = file.exists();

		if (exists) {
			Log.d(Game.class.getName(), "Saved game exists!");
			return true;
		} else {
			Log.d(Game.class.getName(), "Saved game does NOT exist!");
			return false;
		}
	}

	public void setUsername(String username) {
		coreSettings.addSetting(Constants.TAG_USERNAME, username);
		localDataRetriever.open();
		localDataRetriever.writeUsername(username);
	}

	private void completeGameSetup() {
		rulesEngineController.loadRules();
		setDateString();
		gameWeather = Weather.createWeather(gameDateInPlay, Constants.default_WEATHER_TEMPS, Constants.default_WEATHER_RAIN, Constants.default_WEATHER_SEASONS);

		gameStarted = false;
		initialPlotUpdateSent = false;

		nextIteration();
		Log.d(Game.class.getName(), "Game starting up!");
		preStartChecks("Starting up...");
		gameLoading = false;
		Log.d(SetupGame.class.getName(), "Game LOADED");
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
		return randomGenerator.nextInt(Constants.default_MAX_FREQUENCY_FOR_RANDOM_PLANTING) + 1;
	}

	private void UpdateObservers(Object objectUpdated) {
		Log.d(Game.class.getName(), "Sending update to Observers!");
		setChanged();
		notifyObservers(objectUpdated);
	}

	private void startRepeatedActivity() {
		handler.postDelayed(iterator, iteration_time_delay);
	}

	private void stopRepeatedActivity() {
		handler.removeCallbacks(iterator);
	}

	private Runnable iterator = new Runnable() {
		@Override
		public void run() {
			nextIteration();
			// Request next iteraton after specified delay
			handler.postDelayed(this, iteration_time_delay);
		}
	};

	private void nextIteration() {
		updateNeighbourhoods();
		Log.d(Game.class.getName(), "Pause start - fire!");
		if (gameStarted) rulesEngineController.fireRules();
		Log.d(Game.class.getName(), "Pause end - fire!");
		checkForCompletedObjectives();
		checkForUploadableSeeds();
		advanceDate();
		performRegularTasks();
		increaseResourceAllowance();
		updateWaterAllowanceInfo();
		updateGameDetailsText();
		sendPlotStateUpdate();
		updateWeather();
		checkForWeatherMatching();
	}

	private void increaseResourceAllowance() {
		if (waterAllowance + Constants.default_UserWaterAvailability_DailyChange < Constants.default_UserWaterAvailability_Max) {
			waterAllowance = waterAllowance + Constants.default_UserWaterAvailability_DailyChange; 
		} else {
			waterAllowance = Constants.default_UserWaterAvailability_Max;
		}		
	}

	private void preStartChecks(String message) {
		Log.d(Game.class.getName(), "Prestart checks: message=" + message + ", gameStarted=" + gameStarted + ", weatherRetrieved=" + weatherRetrieved + ", seedsRetrieved=" + seedsRetrieved);
		if (!gameStarted && weatherRetrieved && seedsRetrieved) {
			sendStartupUpdate(message);
		}
	}

	private void startGame() {
		Log.d(Game.class.getName(), "Starting GAME");
		gameStarted = true;
		startRepeatedActivity();
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void pauseGame() {
		Log.d(Game.class.getName(), "Pausing GAME");
		stopRepeatedActivity();
		locator.disconnect();
		rulesEngineController.closedownRuleEngine();
		if (localDataRetriever != null) {
			localDataRetriever.open();
		}
		stopAndSaveGame();
	}

	public void stopAndSaveGame() {
		if (!gameLoading && !gameSaving) {
			Log.d(Game.class.getName(), "Stopping and saving game!");
			gameSaving = true;

			SaveGame saveState = new SaveGame();
			saveState.mxPlotsSave = mxPlots;
			saveState.plantCatalogueSave = plantCatalogue;
			saveState.objectivesSave = objectives;
			saveState.day = gameDateInPlay.get(Calendar.DATE);
			saveState.month = gameDateInPlay.get(Calendar.MONTH);
			saveState.year = gameDateInPlay.get(Calendar.YEAR);
			saveState.numOfDaysPlayedSave = numOfDaysPlayed;
			saveState.waterAllowance = waterAllowance;

			if (fileReaderWriter.saveObject(saveState, context.getFilesDir() + "/" + Constants.FILENAME_LOCAL_GAME_SAVE)) {
				Log.d(Game.class.getName(), "Game successfully saved!");
			} else {
				Log.e(Game.class.getName(), "Game was not saved...");
			}

			Log.d(Game.class.getName(), "saveState which has been saved:" + 
					" mxPlots null? " + (saveState.mxPlotsSave == null) +
					",  plantCatalogue null? " + (saveState.plantCatalogueSave == null) +
					",  objectives null? " + (saveState.objectivesSave == null) +
					",  numOfDaysPlayed=" + (saveState.numOfDaysPlayedSave));

			destroyGame();

			gameSaving = false;
			Log.d(Game.class.getName(), "Game stopping NOW!");
		} else {
			Log.d(Game.class.getName(), "Not stopping and saving as game is still loading...");
		}
	}

	public void destroyGame() {
		if (mxPlots!=null) { mxPlots.destroy(); }
		if (plantCatalogue!=null) { plantCatalogue.destroy(); }
		if (objectives!=null) { objectives.destroy(); }
		gameDateInPlay = null;
	}

	public void resumeGame() {
		if (!gameLoading) {
			Log.d(Game.class.getName(), "Resuming GAME");
			if (gameStarted) {
				startRepeatedActivity();
			}

			if (savedGameExists()) {
				loadExistingGame();
			}

			locator.connect();
			localDataRetriever.open();
		} else {
			Log.d(Game.class.getName(), "Not resuming as game is still loading...");
		}
	}

	private void loadExistingGame() {
		Log.d(Game.class.getName(), "Loading existing game from file...");

		SaveGame savedState = fileReaderWriter.loadSavedGame(context.getFilesDir() + "/" + Constants.FILENAME_LOCAL_GAME_SAVE);

		Log.d(Game.class.getName(), "savedState being loaded:" + 
				" mxPlots null? " + (savedState.mxPlotsSave == null) +
				",  plantCatalogue null? " + (savedState.plantCatalogueSave == null) +
				",  objectives null? " + (savedState.objectivesSave == null) +
				",  numOfDaysPlayed=" + (savedState.numOfDaysPlayedSave));


		Log.d(Game.class.getName(), "Current state: mxPlots null? " + (mxPlots == null) + ", mxPlots=" + mxPlots + ", MOP.getMatrix=" + MatrixOfPlots.getMatrix());
		mxPlots = savedState.mxPlotsSave;
		Log.d(Game.class.getName(), "New state: mxPlots null? " + (mxPlots == null) + ", mxPlots=" + mxPlots + ", MOP.getMatrix=" + MatrixOfPlots.getMatrix());
		plantCatalogue = savedState.plantCatalogueSave;
		objectives = savedState.objectivesSave;
		Log.d(Game.class.getName(), "Current state: gameDateInPlay null? " + (gameDateInPlay == null) + ", gameDateInPlay=" + gameDateInPlay);
		gameDateInPlay = Calendar.getInstance();
		gameDateInPlay.set(savedState.year, savedState.month, savedState.day);
		Log.d(Game.class.getName(), "Current state: gameDateInPlay null? " + (gameDateInPlay == null) + ", gameDateInPlay=" + gameDateInPlay);
		numOfDaysPlayed = savedState.numOfDaysPlayedSave;
		waterAllowance = savedState.waterAllowance;

		Log.d(Game.class.getName(), "...loaded existing game!");

		if (fileReaderWriter.deleteFile(context.getFilesDir() + "/" + Constants.FILENAME_LOCAL_GAME_SAVE)) {
			Log.d(Game.class.getName(), "Deleted saved game data");
		} else {
			Log.e(Game.class.getName(), "Could not delete saved game data");
		}
	}

	private void updateNeighbourhoods() {
		if (gameStarted) {
			//regular exercise of updating

			Log.d(Game.class.getName(), "Pause start - create!");
			rulesEngineController.createRulesEngineSession(gameWeather.getCurrentTemp(), gameWeather.getCurrentRain());
			Log.d(Game.class.getName(), "Pause end - create!");

			LinkedList<Neighbourhood> unwateredList = new LinkedList<Neighbourhood>();
			for (Neighbourhood neighbourhood : mxPlots.getNeighbourhoodMatrix()) {
				rainAndSimplePlantWatering(neighbourhood, gameWeather.getCurrentRain(), gameWeather.getCurrentTemp());

				if (neighbourhood.getCentralPlot().getPlant() != null && neighbourhood.getCentralPlot().isPlantWatered() == false) {
					//build list of plots needing watering in pseudo-random order
					Random random = new Random();
					if (random.nextBoolean()) {
						unwateredList.addFirst(neighbourhood);
						Log.d(Game.class.getName(), "Added plot: " + neighbourhood.getCentralPlot().getPlotId() + " to FRONT of list needing watering");
					} else {
						unwateredList.addLast(neighbourhood);
						Log.d(Game.class.getName(), "Added plot: " + neighbourhood.getCentralPlot().getPlotId() + " to BACK of list needing watering");
					}
				}
			}

			while (unwateredList.size()>0) {
				int unwateredListLoopCounter = 0;
				while (unwateredListLoopCounter < unwateredList.size()) {
					Neighbourhood neighbourhood = unwateredList.get(unwateredListLoopCounter);
					int randomNeighbourWithWaterID = neighbourhood.getRandomNeighbourWithWaterID();

					if (randomNeighbourWithWaterID > -1) {
						neighbourhood.getNeighbour(randomNeighbourWithWaterID).setWaterLevel(neighbourhood.getNeighbour(randomNeighbourWithWaterID).getWaterLevel() - 1);
						neighbourhood.setImportedWater(neighbourhood.getImportedWater() + 1);
						Log.d(Game.class.getName(), "Found water for plant in plot: " + neighbourhood.getCentralPlot().getPlotId()
								+ ". Increased to " + (neighbourhood.getCentralPlot().getWaterLevel() + neighbourhood.getImportedWater())
								+ ". Pulled one from plot: " + neighbourhood.getNeighbour(randomNeighbourWithWaterID).getPlotId()
								+ ", leaving " + neighbourhood.getNeighbour(randomNeighbourWithWaterID).getWaterLevel());

						boolean plantWatered = localWaterAvailableForPlant(neighbourhood, gameWeather.getCurrentTemp());
						if (plantWatered) {
							neighbourhood.getCentralPlot().setPlantWatered(plantWatered);
							// plant was watered, so remove from list!
							unwateredList.remove(neighbourhood);
							unwateredListLoopCounter--;
						}
					} else {
						// plant cannot be watered, so remove from list, leaving it unwatered.
						unwateredList.remove(neighbourhood);
						unwateredListLoopCounter--;

						Log.d(Game.class.getName(), "UNWATERED plant in plot: " + neighbourhood.getCentralPlot().getPlotId() + ". 4 - No water available!");
					}

					unwateredListLoopCounter++;
				}
			}

			for (Neighbourhood neighbourhood : mxPlots.getNeighbourhoodMatrix()) {
				rulesEngineController.insertFact(neighbourhood.getCentralPlot());
				Log.d(Game.class.getName(), "Inserted Plot with ID: " + neighbourhood.getCentralPlot().getPlotId());
			}
			rulesEngineController.insertFact(objectives);
			rulesEngineController.insertFact(mxPlots);
		}
	}

	private void rainAndSimplePlantWatering(Neighbourhood neighbourhoodToUpdate, int rainfall, int temperature) {
		boolean centralPlantWatered = false;

		boolean hasPlantAtCentre;
		if (neighbourhoodToUpdate.getCentralPlot().getPlant() == null) {
			hasPlantAtCentre = false;
		} else {
			hasPlantAtCentre = true;
		}

		// if not watery, rain on. if watery, mark any plant as watered
		if (neighbourhoodToUpdate.getCentralPlot().getGroundState() != GroundState.WATER){
			int oldWaterLevel = neighbourhoodToUpdate.getCentralPlot().getWaterLevel();
			neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(rainfall);
			for (int loopCounter = 0; loopCounter < Constants.NEIGHBOURHOOD_STRUCTURE.length; loopCounter++) {
				// if plot is manufactured, its ID will be -1 : and it needs to get a water top up!
				if (neighbourhoodToUpdate.getNeighbour(loopCounter).getPlotId() == -1 && neighbourhoodToUpdate.getNeighbour(loopCounter).getWaterLevel()<=Constants.default_WEATHER_MAX_STANDING_WATER) {
					neighbourhoodToUpdate.getNeighbour(loopCounter).changeWaterLevel(rainfall/Constants.default_EDGE_PLOT_RESOURCE_DIVIDER);
				}
				if (neighbourhoodToUpdate.getNeighbour(loopCounter).getPlotId() == -1 && neighbourhoodToUpdate.getNeighbour(loopCounter).getWaterLevel()>Constants.default_WEATHER_MAX_STANDING_WATER) {
					neighbourhoodToUpdate.getNeighbour(loopCounter).changeWaterLevel(neighbourhoodToUpdate.getNeighbour(loopCounter).getWaterLevel() - Constants.default_WEATHER_MAX_STANDING_WATER);
				}
			}
			Log.d(Game.class.getName(), "Rainfall of " + rainfall + " on plot with ID: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". Water level changed from " + oldWaterLevel + " to " + neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
		} else {
			if (hasPlantAtCentre) {
				neighbourhoodToUpdate.getCentralPlot().setPlantWatered(true);
				centralPlantWatered = true;
				Log.d(Game.class.getName(), "Watered plant in plot: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". 1 - Water plot!");
			}
		}

		if (hasPlantAtCentre && !centralPlantWatered) {
			if (neighbourhoodToUpdate.isNeighbouringWaterPlot()) {
				neighbourhoodToUpdate.getCentralPlot().setPlantWatered(true);
				int requiredWater = neighbourhoodToUpdate.getCentralPlot().getPlant().getRequiredWater();
				int degreesOutsideComfortableRange = temperature - (neighbourhoodToUpdate.getCentralPlot().getPlant().getPreferredTemp() + Constants.default_PLANT_STATE_CHANGE_TEMPERATURE_COMFORTABLE_RANGE_ABOVE);
				if (degreesOutsideComfortableRange > 0) {
					float totalWaterRequired = requiredWater + (degreesOutsideComfortableRange * Constants.default_PLANT_STATE_CHANGE_TEMPERATURE_COMFORTABLE_RANGE_ABOVE_WATER_MULTIPLIER_PER_DEGREE);
					requiredWater = (int) totalWaterRequired;
				}
				neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(0-requiredWater);
				if (neighbourhoodToUpdate.getCentralPlot().getWaterLevel() < 0) {
					neighbourhoodToUpdate.getCentralPlot().changeWaterLevel(0 - neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
				}
				Log.d(Game.class.getName(), "Watered plant in plot: " + neighbourhoodToUpdate.getCentralPlot().getPlotId() + ". 2 - Neighbours a water plot! Water level now: " + neighbourhoodToUpdate.getCentralPlot().getWaterLevel());
				centralPlantWatered = true;
			}
		}

		if (hasPlantAtCentre && !centralPlantWatered) {
			neighbourhoodToUpdate.getCentralPlot().setPlantWatered(localWaterAvailableForPlant(neighbourhoodToUpdate, gameWeather.getCurrentTemp()));
			centralPlantWatered = true;
		}
	}

	private boolean localWaterAvailableForPlant(Neighbourhood neighbourhood, int temperature) {
		//do the necessary if there is water available locally	
		int centralPlotWaterLevel = neighbourhood.getCentralPlot().getWaterLevel();
		int importedWater = neighbourhood.getImportedWater();

		int plantReqWater = neighbourhood.getCentralPlot().getPlant().getRequiredWater();
		int degreesOutsideComfortableRange = temperature - (neighbourhood.getCentralPlot().getPlant().getPreferredTemp() + Constants.default_PLANT_STATE_CHANGE_TEMPERATURE_COMFORTABLE_RANGE_ABOVE);
		if (degreesOutsideComfortableRange > 0) {
			float totalWaterRequired = plantReqWater + (degreesOutsideComfortableRange * Constants.default_PLANT_STATE_CHANGE_TEMPERATURE_COMFORTABLE_RANGE_ABOVE_WATER_MULTIPLIER_PER_DEGREE);
			plantReqWater = (int) totalWaterRequired;
		}

		if (plantReqWater <= (centralPlotWaterLevel + importedWater)) {	
			neighbourhood.getCentralPlot().changeWaterLevel(0 - (plantReqWater - importedWater));
			neighbourhood.setImportedWater(0);

			Log.d(Game.class.getName(), "Water available for plant in plot " + neighbourhood.getCentralPlot().getPlotId() + ", plant " + neighbourhood.getCentralPlot().getPlant().getType() +
					". plot water was: " + centralPlotWaterLevel + " plus imported water of: " + importedWater + ", plant water req: " + plantReqWater +
					". So plot water level now: " + neighbourhood.getCentralPlot().getWaterLevel());
			Log.d(Game.class.getName(), "Watered plant in plot: " + neighbourhood.getCentralPlot().getPlotId() + ". 3 - Local water!");

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

				if (neighbourhood.getCentralPlot().getPlant().getSuccessCopy() != null && !neighbourhood.getCentralPlot().getPlant().getSuccessCopy().isEmpty()) {
					recordSponsoredSeedUnlocked(neighbourhood.getCentralPlot().getPlant());
				}
			}

			//flowering of a non-fruiting plant, or fruiting of a fruiting plant
			if (neighbourhood.getCentralPlot().getPlant() != null && ((neighbourhood.getCentralPlot().getPlant().getFruitingTarget() == 0 && neighbourhood.getCentralPlot().getPlant().getPlantState() == PlantState.FLOWERING && neighbourhood.getCentralPlot().getPlant().getDaysInCurrentState() == 1) || (neighbourhood.getCentralPlot().getPlant().getFruitingTarget() != 0 && neighbourhood.getCentralPlot().getPlant().getPlantState() == PlantState.FRUITING && neighbourhood.getCentralPlot().getPlant().getDaysInCurrentState() == 1))) {
				localSeedDistribution(neighbourhood);
			}
		}
	}

	public void uploadSeed(PlantInstance seedToUpload) {
		new RemoteDataExchange().execute( RemoteDataExchangeDataType.UPLOAD_SEED, seedToUpload );
	}

	public void advanceDate(){
		numOfDaysPlayed = numOfDaysPlayed + 1;
		gameDateInPlay.add(Calendar.DATE, 1);
		setDateString();
	}

	public void performRegularTasks(){
		if (errorMessage!=null && (!gameStarted || numOfDaysPlayed % Constants.default_ERROR_DISPLAY_FREQ == Constants.default_ERROR_DISPLAY_OFFSET)) {
			Log.d(Game.class.getName(), "Sending error message..." + gameStarted);
			ErrorMessage errMsg = new ErrorMessage();
			errMsg.errorMessage = errorMessage;
			UpdateObservers(errMsg);
		}

		if (!gameStarted || numOfDaysPlayed % Constants.default_GAME_WEATHER_RETRIEVE_FREQ == Constants.default_GAME_WEATHER_RETRIEVE_OFFSET) {
			Log.d(Game.class.getName(), "Requesting update to local weather state..." + gameStarted);
			new RemoteDataExchange().execute(RemoteDataExchangeDataType.WEATHER);
		}

		if (!gameStarted || numOfDaysPlayed % Constants.default_GAME_REMOTE_SEEDS_RETRIEVE_FREQ == Constants.default_GAME_REMOTE_SEEDS_RETRIEVE_OFFSET) {
			Log.d(Game.class.getName(), "Requesting update to remote seed list..." + gameStarted);
			new RemoteDataExchange().execute(RemoteDataExchangeDataType.DOWNLOAD_SEEDS);
		}

		if (daysUntilNextRandomSeeding == 0) {
			Random randomGenerator = new Random();
			if (randomGenerator.nextBoolean() && plantCatalogue.getRemoteSeedCount() > 0) {
				Log.d(Game.class.getName(), "Planting remote seed...");
				plantRandomRemoteSeed();
			} else {
				Log.d(Game.class.getName(), "Planting local seed...");
				plantRandomLocalSeed(); 
			}
			daysUntilNextRandomSeeding = getDaysUntilNextRandomSeeding();
		}
		daysUntilNextRandomSeeding--;
	}

	private void sendStartupUpdate(String message) {
		GameStartup gs = new GameStartup();
		if (weatherRetrieved && seedsRetrieved && initialPlotUpdateSent) {
			Log.d(Game.class.getName(), "Ready to start game!");
			gs.readyToPlay = true;

			//taken out to begin game from outside! now revoked...
			startGame();
		} else {
			gs.readyToPlay = false;
		}
		gs.message = message;
		Log.d(Game.class.getName(), "Sending update to UI!");
		UpdateObservers(gs);
	}

	private void updateGameDetailsText() {
		GameDetailsText gdText = new GameDetailsText();
		float waterUsage = (waterAllowance * 100.0f)/Constants.default_UserWaterAvailability_Max;

		gdText.gameDetails = "Local Weather:\nTemperature = " + getRealTemp() + "\u00B0C\nRainfall = " + getRealRainfall() + "mm\nNumber of remote seeds = " + getRemoteSeedCount() + "\nLat = " + locator.getLocation().getLatitude() + "\nLong = " + locator.getLocation().getLongitude() + "\nWater Allowance=" + waterUsage + "%";

		UpdateObservers(gdText);
	}

	private void setDateString() {
		GameDate gameDate = new GameDate();
		gameDate.gameDateString = gameDateInPlay.get(Calendar.DATE) + " " + gameDateInPlay.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + gameDateInPlay.get(Calendar.YEAR);
		gameDate.dayInYear = gameDateInPlay.get(Calendar.DAY_OF_YEAR);
		Calendar springStartDate = Calendar.getInstance();
		if (gameWeather!=null) {springStartDate.set(2001, gameWeather.getStartMonthOfSpring()-1, 1); }
		gameDate.startOfSpring = springStartDate.get(Calendar.DAY_OF_YEAR);
		Log.d(Game.class.getName(), "Date is: " + gameDate.gameDateString);

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
		if (getPlotFrom1BasedID(plotID).getPlant() != null) {
			plotUpdate.hasPlant = true;
		} else  {
			plotUpdate.hasPlant = false;
		}
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

	private void checkForWeatherMatching() {
		if (daysSinceLastWeatherMessage >= Constants.default_WEATHER_MESSAGE_MAXIMUM_FREQUENCY) {
			if (gameWeather.getCurrentTemp()>=weatherRetriever.getTemperature()-Constants.default_WEATHER_MESSAGE_TEMP_RANGE || gameWeather.getCurrentTemp()<=weatherRetriever.getTemperature()+Constants.default_WEATHER_MESSAGE_TEMP_RANGE) {
				WeatherMessage wm = new WeatherMessage();
				String message = "";

				if (gameWeather.getCurrentTemp() == weatherRetriever.getTemperature()) {
					message = "Your garden is the same temperature as your current location, ";
				} else if (gameWeather.getCurrentTemp() < weatherRetriever.getTemperature()) {
					message = "Your garden is " + (weatherRetriever.getTemperature() - gameWeather.getCurrentTemp()) + "\u00B0C cooler than your current location, ";
				} else {
					message = "Your garden is " + (gameWeather.getCurrentTemp() - weatherRetriever.getTemperature()) + "\u00B0C warmer than your current location, ";
				}

				if (gameWeather.getCurrentRain() == weatherRetriever.getRainfall()) {
					message = message + "and is experiencing the same level of rainfall (" + gameWeather.getCurrentRain() + "mm)";
				} else if (gameWeather.getCurrentRain() < weatherRetriever.getRainfall()) {
					message = message + "and is experiencing " + (weatherRetriever.getRainfall() - gameWeather.getCurrentRain()) + "mm less rain";
				} else {
					message = message + "and is experiencing " + (gameWeather.getCurrentRain() - weatherRetriever.getRainfall()) + "mm more rain";
				}

				wm.weatherMessage = message;
				UpdateObservers(wm);

				daysSinceLastWeatherMessage = 0;
			}
		}
		daysSinceLastWeatherMessage++;
	}

	public int getRealTemp() {
		errorMessage = weatherRetriever.getErrorMessage();
		return weatherRetriever.getTemperature();
	}

	public int getRealRainfall() {
		errorMessage = weatherRetriever.getErrorMessage();
		return weatherRetriever.getRainfall();
	}

	public int getRemoteSeedCount() {
		return plantCatalogue.getRemoteSeedCount();
	}

	private class RemoteDataExchange extends AsyncTask<Object, Void, Void> {

		protected void onPreExecute() {
			Log.d(RemoteDataExchange.class.getName(), "Attempting remote data exchange from within Game");
		}

		@Override
		protected Void doInBackground(Object... params) {
			String message = "";
			switch ((RemoteDataExchangeDataType) params[0]) {
			case DOWNLOAD_SEEDS:
				Log.d(RemoteDataExchange.class.getName(), "Attempting retrieval of remote seed data...");
				plantCatalogue.setRemoteSeedArray(remoteDataRetriever.getSeedingPlants(coreSettings.checkStringSetting(Constants.TAG_USERNAME), locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), Constants.default_DISTANCE_USER, Constants.default_DISTANCE_SPONSOR, new Date()));
				message = "Retrieved nearby seeds...";
				seedsRetrieved = true;
				Log.d(RemoteDataExchange.class.getName(), "Remote seed data retrieved");
				break;

			case WEATHER:
				Log.d(RemoteDataExchange.class.getName(), "Attempting retrieval of local weatherRetriever data...");
				weatherRetriever.checkWeather(locator.getLocation().getLongitude(),locator.getLocation().getLatitude());
				message = "Retrieved local weatherRetriever details...";
				weatherRetrieved = true;
				Log.d(RemoteDataExchange.class.getName(), "Local weatherRetriever data retrieved");
				break;

			case UPLOAD_SEED:
				PlantInstance seedToUpload = (PlantInstance) params[1];
				Log.d(RemoteDataExchange.class.getName(), "Attempting to upload seed of flowering " + seedToUpload.getType());
				boolean seedUploadStatus = remoteDataRetriever.uploadSeed(new Date(),
						coreSettings.checkStringSetting(Constants.TAG_USERNAME),
						locator.getLocation().getLatitude(),
						locator.getLocation().getLongitude(),
						seedToUpload.getId()
				);
				if (seedUploadStatus) {
					Log.d(RemoteDataExchange.class.getName(), "Seed upload successful!");
					SeedUploaded seedUploaded = new SeedUploaded();
					seedUploaded.message = "Seeds from your flowering " + seedToUpload.getType() + " have been released into the environment!";
					UpdateObservers(seedUploaded);
				} else {
					Log.e(RemoteDataExchange.class.getName(), "Seed upload NOT successful!");
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

	private void recordSponsoredSeedUnlocked(PlantInstance seedUnlocked) {
		//only send update if this is a newly unlocked seed, not one previously unlocked...
		if (localDataRetriever.writeNewSponsoredPlantUnlocked(Calendar.getInstance().getTime(), seedUnlocked.getOriginUsername(), seedUnlocked.getSponsoredMessage(), seedUnlocked.getSuccessCopy())) {
			Log.d(Game.class.getName(), "Recording and displaying an unlocked seed from: " + seedUnlocked.getOriginUsername());
			SponsoredSeedUnlocked ssu = new SponsoredSeedUnlocked();
			ssu.originUsername = seedUnlocked.getOriginUsername();
			ssu.sponsoredMessage = seedUnlocked.getSponsoredMessage();
			ssu.successCopy = seedUnlocked.getSuccessCopy();

			UpdateObservers(ssu);
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
			Log.d(Game.class.getName(), "Random plot selected");

			PlantInstance newLocalPlant = new PlantInstance(chosenPlant, plotForPlanting);

			getPlotFrom1BasedID(plotForPlanting).setPlant(newLocalPlant);
			Log.d(Game.class.getName(), "Random seed planted in plot: " + plotForPlanting);

			SeedPlanted locSeedPlanted = new SeedPlanted();
			locSeedPlanted.plotID = plotForPlanting;
			locSeedPlanted.plantType = chosenPlant.getType();
			locSeedPlanted.isRemote = false;

			Log.d(Game.class.getName(), "Returning random plant notification to obervers: Local plant=" + locSeedPlanted.plantType);

			PlotDetails plotUpdate = singlePlotDetails(locSeedPlanted.plotID);
			UpdateObservers(plotUpdate);
			UpdateObservers(locSeedPlanted);
		} else {
			Log.d(Game.class.getName(), "No plot selected! So no planting happening");
		}
	}

	private void plantRandomRemoteSeed() {
		Log.d(Game.class.getName(), "Random seed being planted...");

		RemoteSeed remoteSeed = plantCatalogue.getRandomRemoteSeed();
		GroundState gsPlantPrefers = plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()).getPreferredGroundState();

		int plotForPlanting = getPlotForPlanting(gsPlantPrefers);

		if (plotForPlanting > -1) {
			Log.d(Game.class.getName(), "Random plot selected");
			PlantInstance newRemotePlant;
			boolean isSponsored = remoteSeed.isSponsored();
			if (isSponsored) {
				newRemotePlant = new PlantInstance(plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()), plotForPlanting, remoteSeed.getUsername(), remoteSeed.getMessage(), remoteSeed.getSuccess_copy());
			} else {
				newRemotePlant = new PlantInstance(plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()), plotForPlanting, remoteSeed.getUsername());
			}
			getPlotFrom1BasedID(plotForPlanting).setPlant(newRemotePlant);
			Log.d(Game.class.getName(), "Random seed planted in plot: " + plotForPlanting);

			SeedPlanted remSeedPlanted = new SeedPlanted();
			remSeedPlanted.plotID = plotForPlanting;
			remSeedPlanted.username = remoteSeed.getUsername();
			remSeedPlanted.isSponsered = isSponsored;
			remSeedPlanted.isRemote = true;
			remSeedPlanted.plantType = plantCatalogue.getPlantTypeByPlantTypeID(remoteSeed.getPlantTypeId()).getType();

			Log.d(Game.class.getName(), "Returning random plant notification to obervers: From=" + remSeedPlanted.username + ", Plant=" + remSeedPlanted.plantType + ", isSponsored=" + remSeedPlanted.isSponsered);

			PlotDetails plotUpdate = singlePlotDetails(remSeedPlanted.plotID);
			UpdateObservers(plotUpdate);
			UpdateObservers(remSeedPlanted);
		} else {
			Log.d(Game.class.getName(), "No plot selected! So no planting happening");
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

	private void localSeedDistribution(Neighbourhood neighbourhood) {
		for (int loopCounter = 0; loopCounter<neighbourhood.getNeighbourCounter(); loopCounter++) {
			Random rand = new Random();
			if (neighbourhood.getNeighbour(loopCounter).getPlotId() != -1 && neighbourhood.getNeighbour(loopCounter).getPlant() == null && rand.nextInt(Constants.default_PLANT_MAX_COMMONESS_FACTOR)+1<neighbourhood.getCentralPlot().getPlant().getCommonnessFactor()) {
				neighbourhood.getNeighbour(loopCounter).setPlant(new PlantInstance(plantCatalogue.getPlantTypeByPlantTypeID(neighbourhood.getCentralPlot().getPlant().getId()), neighbourhood.getNeighbour(loopCounter).getPlotId()));
				Log.d(SetupGame.class.getName(), "Flowering/Fruiting plant self-seeded. Plant=" + neighbourhood.getCentralPlot().getPlant().getType() + ", From Plot ID=" + neighbourhood.getCentralPlot().getPlotId() + ", Seeded into Plot ID=" + neighbourhood.getNeighbour(loopCounter).getPlotId());
			}
		}
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
		Log.d(Game.class.getName(), "Plot Text: " + plotText);
		return(plotText);
	}

	public String getPlotBasicFullPlotDetails(int plotID) {
		Plot localCopy = getPlotFrom1BasedID(plotID);
		return localCopy.toString();
	}

	public Objective[] getObjectiveList() {
		if (objectives == null || objectives.getObjectiveList() ==null) {
			localDataRetriever.open();
			Objective[] allObjectives = localDataRetriever.getObjectives();
			if (allObjectives.length>0) {
				Objective[] objectiveList = new Objective[allObjectives.length-1];
				for (int loopCounter = 1; loopCounter<allObjectives.length; loopCounter++) {
					objectiveList[loopCounter-1] = allObjectives[loopCounter];
				}
				localDataRetriever.close();			
				return objectiveList;
			} else {
				return null;
			}
		} else {
			return objectives.getObjectiveList();
		}
	}

	public String[][] getUnlockedSeedsList() {
		localDataRetriever.open();
		String[][] unlockedSeeds = localDataRetriever.getUnlockedSeeds();
		localDataRetriever.close();	
		return unlockedSeeds;
	}

	private void sendObjectiveUpdate() {
		ObjectiveUpdate ou = new ObjectiveUpdate();
		ou.totalNum = objectives.getTotalNumberOfObjectives();
		ou.numCompleted = objectives.getNumberOfCompletedObjectives();
		UpdateObservers(ou);
	}

	public boolean isNewGame() {
		if (gameStartMode == GameStartMode.LOAD_GAME) {
			return false;
		} else {
			gameStartMode = GameStartMode.LOAD_GAME; // set to reload for future screen loads. This will be reset when game recreated...
			return true;
		}
	}


	class SetupGame extends AsyncTask<Boolean, String, Boolean> {
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

			// TODO Provide graceful failure if no internet connection - run game purey from local data

			boolean forceUpdate = false;

			// TODO This loads existing game - provide way to update plant catalogue, etc. mid game.
			boolean isNewGame = params[0];
			Log.d(SetupGame.class.getName(), "Is this a new game? " + isNewGame);
			if (!isNewGame) {
				gameStartMode = GameStartMode.LOAD_GAME;
				publishProgress("Loading saved game state...");
				loadExistingGame();
			} else {
				gameStartMode = GameStartMode.NEW_GAME;
				destroyGame();
			}

			publishProgress("Checking for updates to local data");
			localDataRetriever = new LocalDBDataRetrieval(context);
			localDataRetriever.open();
			Log.d(SetupGame.class.getName(), "Opened Data source!");
			Log.d(SetupGame.class.getName(), "Get local globals");
			Globals globalsLocal = localDataRetriever.getGlobals();

			// save URL, etc. to preferences
			coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, globalsLocal.getRootURL());
			coreSettings.addSetting(Constants.CORESETTING_LOCAL_FILEPATH, context.getFilesDir() + "/");

			remoteDataRetriever = new RemoteDBTableRetrieval();
			Globals globalsRemote = remoteDataRetriever.getGlobals();
			downloader = new FileDownloader();

			if (forceUpdate || globalsRemote.getLast_updated().after(globalsLocal.getLast_updated())) {
				publishProgress("Updating local data with updated values!");
				Log.d(SetupGame.class.getName(), "NEED TO UPDATE SOME LOCAL DATA!");
				
				// if update of local data is successful, then check for further updates...
				if (localDataRetriever.writeGlobals(globalsRemote)) {
					Log.d(SetupGame.class.getName(), "Get local table update values");
					
					//update dynamic copy of local settings
					globalsLocal = localDataRetriever.getGlobals();
					// save URL, etc. to preferences
					coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, globalsLocal.getRootURL());
					
					TableLastUpdateDates tablesUpdatedLocal = localDataRetriever.getTableUpdateDates();
					Log.d(SetupGame.class.getName(), "ConfigValues date (local): " + tablesUpdatedLocal.getConfig().toString());
					TableLastUpdateDates tablesUpdatedRemote = remoteDataRetriever.getTableListing();
					Log.d(SetupGame.class.getName(), "ConfigValues date (remote): " + tablesUpdatedRemote.getConfig().toString());


					// CONFIG
					if (isNewGame && (forceUpdate || tablesUpdatedRemote.getConfig().after(tablesUpdatedLocal.getConfig()))) {
						///WE ARE REMOTE
						Log.d(SetupGame.class.getName(), "NEED TO UPDATE CONFIG TABLE!");
						publishProgress("Updating Configuration data with updated values!");
						ConfigValues remoteConfigValues = remoteDataRetriever.getConfig();
						
						if (localDataRetriever.writeConfig(remoteConfigValues) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_CONFIG, tablesUpdatedRemote.getConfig())) {
							Log.d(SetupGame.class.getName(), "Updated Config date in Tables table!");
							Log.d(SetupGame.class.getName(), "Updated Config data!");
							publishProgress("Local Config table data updated");

							Log.d(SetupGame.class.getName(), "Creating Plot matrix from remote data...");
							Constants.GroundState[] gsGroundStates = remoteConfigValues.getGroundStates();

							int num_rows = remoteConfigValues.getPlot_matrix_rows();
							int num_cols = remoteConfigValues.getPlot_matrix_columns();

							Plot[][] plotArray = new Plot[num_rows][num_cols];

							for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
								for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
									Plot newPlot = new Plot((rowCounter * num_cols) + columnCounter + 1, columnCounter + 1, rowCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel);
									plotArray[rowCounter][columnCounter] = newPlot;
								}
							}

							coreSettings.addSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY, remoteConfigValues.getIteration_time_delay());

							if (MatrixOfPlots.createMatrix(plotArray)) {
								Log.d(SetupGame.class.getName(), "Created plot matrix from remote data!");
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
						Log.d(SetupGame.class.getName(), "NEED TO UPDATE PLANTTYPES TABLE!");
						publishProgress("Updating Plant Types data with updated values!");
						PlantType[] remotePlantTypes = remoteDataRetriever.getPlantTypes();

						if (localDataRetriever.writePlantTypes(remotePlantTypes)) {
							String[] downloadFrom = localDataRetriever.getPlantImagePaths();
							String[] saveTo = new String[downloadFrom.length];
							for (int loopCounter = 0; loopCounter<downloadFrom.length; loopCounter++) {
								saveTo[loopCounter] = coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + downloadFrom[loopCounter];
								downloadFrom[loopCounter] = coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.ROOT_URL_IMAGE_EXT + downloadFrom[loopCounter];
							}

							if (downloader.download(downloadFrom, saveTo) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_PLANTTYPES, tablesUpdatedRemote.getPlants())) {
								Log.d(SetupGame.class.getName(), "Updated Plant Types data!");
								publishProgress("Local Plant Types table data updated");

								if (PlantCatalogue.createPlantCatalogue(localDataRetriever.getPlantTypes())) {
									Log.d(SetupGame.class.getName(), "Created plant catalogue!");
								} else {
									Log.e(SetupGame.class.getName(), "Could not create plant catalogue!");
								}
							} else {
								Log.e(SetupGame.class.getName(), "Could not update Plant image files or last update date...");
								publishProgress("Local Plant image files could not be updated");
							}
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Plant types data...");
							publishProgress("Local Plant types data could not be updated");
						}
					}

					// OBJECTIVES
					if (forceUpdate || tablesUpdatedRemote.getObjectives().after(tablesUpdatedLocal.getObjectives())) {
						Log.d(SetupGame.class.getName(), "NEED TO UPDATE OBJECTIVES TABLE + RULES FILE!");
						publishProgress("Updating Objectives data with updated values!");
						Objective[] remoteObjectives = remoteDataRetriever.getObjectives();

						if (localDataRetriever.writeObjectives(remoteObjectives) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_OBJECTIVES, tablesUpdatedRemote.getObjectives())) {
							Log.d(SetupGame.class.getName(), "Updated Objectives data!");
							publishProgress("Local Objectives table data updated");

							if (Objectives.createObjectives(localDataRetriever.getObjectives())) {
								Log.d(SetupGame.class.getName(), "Created local objectives list!");
							} else {
								Log.e(SetupGame.class.getName(), "Could not create objectives list!");
							}
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Objectives date and/or data...");
							publishProgress("Local Objectives data could not be updated");
						}

						String[] downloadFrom = { coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_OBJECTIVES };
						String[] saveTo = { coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + Constants.FILENAME_LOCAL_OBJECTIVES };
						Log.d(SetupGame.class.getName(), "Downloading updated objectives rules file from server. From: " + downloadFrom[0] + ", To: " + saveTo[0]);
						publishProgress("Downloading updated objectives rules file...");
						if (downloader.download(downloadFrom, saveTo)) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(SetupGame.class.getName(), "Unable to download updated objectives rules file from server.");
						}
					}

					// ITERATION RULES FILES
					if (forceUpdate || tablesUpdatedRemote.getIterationRules().after(tablesUpdatedLocal.getIterationRules())) {
						Log.d(SetupGame.class.getName(), "NEED TO UPDATE ITERATION RULES FILE!");

						String[] downloadFrom = { coreSettings.checkStringSetting(Constants.ROOT_URL_FIELD_NAME) + Constants.FILENAME_REMOTE_ITERATION_RULES };
						String[] saveTo = { coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + Constants.FILENAME_LOCAL_ITERATION_RULES };
						Log.d(SetupGame.class.getName(), "Downloading updated iteration rules file from server. From: " + downloadFrom[0] + ", To: " + saveTo[0]);
						publishProgress("Downloading updated iteration rules file...");
						if (downloader.download(downloadFrom, saveTo) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_ITERATION_RULES, tablesUpdatedRemote.getIterationRules())) {
							publishProgress("...Successful!");
						} else {
							publishProgress("...not successful!");
							Log.e(SetupGame.class.getName(), "Unable to download updated iteration rules file from server.");
						}
					}

					// HELP AND INFO DATA
					if (forceUpdate || tablesUpdatedRemote.getHelpAndInfo().after(tablesUpdatedLocal.getHelpAndInfo())) {
						Log.d(SetupGame.class.getName(), "NEED TO UPDATE HELP AND INFO TABLE!");
						publishProgress("Updating Help and Info data with updated values!");
						String[][] remoteHelpAndInfoData= remoteDataRetriever.getHelpAndInfoData();

						if (localDataRetriever.writeHelpAndInfoData(remoteHelpAndInfoData) && localDataRetriever.writeTableUpdateDate(Constants.TABLES_VALUES_HELPANDINFO, tablesUpdatedRemote.getHelpAndInfo())) {
							Log.d(SetupGame.class.getName(), "Updated Help and Info data!");
							publishProgress("Local Help and Info data updated");
						} else {
							Log.e(SetupGame.class.getName(), "Could not update Help and Info date and/or data...");
							publishProgress("Local Help and Info data could not be updated");
						}
					}
					
					publishProgress("Local data updated");
				} else {
					//local 'globals' could not be updated
					publishProgress("Local data could not be updated");
				}
			} else {
				//no force update and no more recent data
				publishProgress("All local data already up to date!");
			}

			//BUILD FROM LOCAL DATA

			// PLOT MATRIX - only build if empty and not loading existing game
			if (isNewGame) { //MatrixOfPlots.getMatrix() == null) {
				publishProgress("Setting up game from local details!");
				Log.d(SetupGame.class.getName(), "Creating Plot matrix from local data...");

				ConfigValues localConfigValues = localDataRetriever.getConfigValues();
				Constants.GroundState[] gsGroundStates = localConfigValues.getGroundStates();

				int num_rows = localConfigValues.getPlot_matrix_rows();
				int num_cols = localConfigValues.getPlot_matrix_columns();

				Plot[][] plotArray = new Plot[num_rows][num_cols];

				for (int rowCounter = 0; rowCounter<num_rows; rowCounter++) {
					for (int columnCounter = 0; columnCounter<num_cols; columnCounter++) {
						plotArray[rowCounter][columnCounter] = new Plot((rowCounter * num_cols) + columnCounter + 1, columnCounter + 1, rowCounter + 1, gsGroundStates[(rowCounter * num_cols) + columnCounter], Constants.default_WaterLevel);
					}
				}

				coreSettings.addSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY, localConfigValues.getIteration_time_delay());

				if (MatrixOfPlots.createMatrix(plotArray)) {
					Log.d(SetupGame.class.getName(), "Created plot matrix from local data!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create plot matrix from local data!");
				}
			}

			Log.d(SetupGame.class.getName(), "static matrix equals: " + MatrixOfPlots.getMatrix());
			if (isNewGame || MatrixOfPlots.getMatrix()!=null) {
				mxPlots = MatrixOfPlots.getMatrix();
			}

			Log.d(SetupGame.class.getName(), "Sending Garden dimension information...");
			GardenDimensions gardenDimensions = new GardenDimensions();
			gardenDimensions.cols = mxPlots.getNumCols();
			gardenDimensions.rows = mxPlots.getNumRows();
			UpdateObservers(gardenDimensions);

			// PLANT CATALOGUE - only build if empty
			if (PlantCatalogue.getPlantCatalogue() == null) {
				publishProgress("Building plant catalogue!");
				Log.d(SetupGame.class.getName(), "Building plant catalogue from local data...");

				if (PlantCatalogue.createPlantCatalogue(localDataRetriever.getPlantTypes())) {
					Log.d(SetupGame.class.getName(), "Created plant catalogue!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create plant catalogue!");
				}
			}

			// OBJECTIVES - only build if empty
			if (Objectives.getObjectives() == null) {
				publishProgress("Building Objectives list!");
				Log.d(SetupGame.class.getName(), "Building Objectives list from local data...");

				if (Objectives.createObjectives(localDataRetriever.getObjectives())) {
					Log.d(SetupGame.class.getName(), "Created local objectives list!");
				} else {
					Log.e(SetupGame.class.getName(), "Could not create objectives list!");
				}
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

						Log.d(SetupGame.class.getName(), "Building " +  Constants.NEIGHBOURHOOD_STRUCTURE.length + " plot neighbourhood for plot: " + localPlot.getPlotId() + ", at X=" + xPosCentral + ",Y=" + yPosCentral + ". Plot marked as 'neighbourhood created'?: " + localPlot.isNeighbourhoodCreated());

						for (int[] neighbourRelPos : Constants.NEIGHBOURHOOD_STRUCTURE) {
							neigbourhoodToInsert.addNeighbour(mxPlots.getNeigbouringPlot(xPosCentral, yPosCentral, neighbourRelPos[0], neighbourRelPos[1]));
						}

						localPlot.setNeighbourhoodCreated(true);
						neighbourhoodArray[localPlot.getPlotId()-1] = neigbourhoodToInsert;
						Log.d(SetupGame.class.getName(), "Built neighbourhood of: " + neigbourhoodToInsert.getNeighbourCounter() + " plots with central plot id " + localPlot.getPlotId());
					}
				}
				mxPlots.setNeighbourhoodMatrix(neighbourhoodArray);

				Log.d(SetupGame.class.getName(), "Created neighbourhood matrix from plot matrix data!");
			}

			publishProgress("Starting game...");
			return isNewGame;
		}

		protected void onProgressUpdate(String... progress) {
			sendMessage(progress[0], false);
		}

		protected void onPostExecute(Boolean isNewGame) {
			//for new game or resumed game, handle differently:

			if (isNewGame) {
				Log.d(SetupGame.class.getName(), "Set remaining key values for new game");
				plantCatalogue = PlantCatalogue.getPlantCatalogue();
				objectives = Objectives.getObjectives();
				gameDateInPlay = Calendar.getInstance();
				gameDateInPlay.add(Calendar.DATE, -1);
				numOfDaysPlayed = -1;
				waterAllowance = Constants.default_UserWaterAvailability_Initial;
			} else {
				Log.d(SetupGame.class.getName(), "Remaining key values already set - loading existing game");
			}
			iteration_time_delay = checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
			sendObjectiveUpdate();

			completeGameSetup();
		}
	}

	public void WaterPlotWithID(int plotID) {
		int amountToWater = getAmountToWater();
		Plot plotBeingWatered = getPlotFrom1BasedID(plotID);
		int oldWaterLevel = plotBeingWatered.getWaterLevel();
		plotBeingWatered.changeWaterLevel(amountToWater);
		PlotWatered sw = new PlotWatered();
		sw.plotID = plotID;
		Log.d(Game.class.getName(), "Player watered plot with ID: " + plotID + ". Water level changed from " + oldWaterLevel + " to " + getPlotFrom1BasedID(plotID).getWaterLevel());
		UpdateObservers(sw);
		updateGameDetailsText();
		updateWaterAllowanceInfo();
	}

	private void updateWaterAllowanceInfo() {
		WaterAllowanceLevel wal = new WaterAllowanceLevel();
		int waterAllPercent = waterAllowance * 100;
		waterAllPercent = waterAllPercent / Constants.default_UserWaterAvailability_Max;
		wal.waterAllowance = waterAllPercent;
		Log.d(Game.class.getName(), "Sending water allowance update. Level is " + waterAllowance);
		UpdateObservers(wal);
	}

	private int getAmountToWater() {
		if (waterAllowance >= Constants.default_WateringAmount) {
			waterAllowance = waterAllowance - Constants.default_WateringAmount;
			return Constants.default_WateringAmount;
		} else {
			int remainder = waterAllowance;
			waterAllowance = 0;
			return remainder;
		}
	}

	public String getTextElement(String dataType, String reference) {
		localDataRetriever.open();
		return localDataRetriever.getHelpAndInfo(dataType, reference);
	}
}
