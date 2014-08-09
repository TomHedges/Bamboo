package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Observable;
import java.util.Random;
import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.config.Constants.REMOTE_DATA_EXCHANGE_DATA_TYPE;
import com.tomhedges.bamboo.config.Constants.Season;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.rulesengine.RulesEngineController;
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
	private RulesEngineController rulesEngineController;
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

	// Private constructor
	private Game(Context context){
		this.context = context;
		coreSettings = CoreSettings.accessCoreSettings();
		plantCatalogue = PlantCatalogue.getPlantCatalogue();
		mxPlots = MatrixOfPlots.getMatrix();
		rulesEngineController = RulesEngineController.getInstance(context);
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

		rulesEngineController.loadRules();

		//Carry out once to set up!
		nextIteration();
		preStartChecks("Starting up...");
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
		updateNeighbourhoods();
		advanceDate();
		updateGameDetailsText();
		sendPlotStateUpdate();
		updateWeather();
	}

	private void preStartChecks(String message) {
		if (!gameStarted && weatherRetrieved && seedsRetrieved) {
			sendStartupUpdate(message);
		}
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

	private void updateNeighbourhoods() {
		//TEST that uploading of seeds works as expected - and it does!
		//if (numOfDaysPlayed == 3 && mxPlots.getPlot(1, 1).getPlant() != null) {
		//	uploadSeed(mxPlots.getPlot(1, 1).getPlant());
		//}

		// TODO Auto-generated method stub
		if (gameStarted) {
			//regular exercise of updating

			rulesEngineController.createRulesEngineSession(gameWeather.getCurrentTemp(),gameWeather.getCurrentRain());

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

			rulesEngineController.fireRules();
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
}
