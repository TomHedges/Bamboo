package com.tomhedges.bamboo.config;

public interface Constants {
	public static final String ROOT_URL = "http://54.229.96.8/bamboo-test/";
	public static final String ROOT_URL_FIELD_NAME = "root_url";
	public static final String ROOT_URL_IMAGE_EXT = "images/";

	// tests and samples
	public static final String LOGIN_URL = ROOT_URL + "login.php";
	public static final String REGISTER_URL = ROOT_URL + "register.php";
	public static final String POST_COMMENT_URL = ROOT_URL + "addcomment.php";
	public static final String READ_COMMENTS_URL = ROOT_URL + "comments.php";
	public static final String TAG_SUCCESS = "success";
	public static final String TAG_MESSAGE = "message";
	public static final String TAG_TITLE = "title";
	public static final String TAG_POSTS = "posts";
	public static final String TAG_POST_ID = "post_id";
	public static final String TAG_USERNAME = "username";

	// project relevant
	public static final String TABLE_NAME_VARIABLE = "tablename";
	public static final String TABLE_NAME_GLOBAL_SETTINGS = "GlobalSettings";
	public static final String TABLE_NAME_TABLES = "Tables";
	public static final String TABLE_NAME_HELPANDINFO = "HelpAndInfo";
	public static final String HTML_VERB_POST = "POST";
	//public static final String COLUMN_DETAILS_URL = ROOT_URL + "getcolumnnames.php";
	//public static final String TABLE_DATA_URL = ROOT_URL + "gettabledata.php";
	public static final String TABLE_DATA_SCRIPT_NAME = "gettabledata.php";
	public static final String GET_SEEDING_PLANTS_SCRIPT_NAME = "getseedingplants.php";
	public static final String UPLOAD_SEED = "uploadseed.php";
	public static final String TAG_FIELD = "Field";
	public static final String TAG_TYPE = "Type";

	public static final String TABLE_GLOBAL_SETTINGS = "GlobalSettings";
	public static final String TABLE_TABLES = "Tables";
	public static final String TABLE_CONFIG = "Config";
	public static final String TABLE_PLANT_TYPES = "PlantTypes";
	public static final String TABLE_OBJECTIVES = "Objectives";
	public static final String TABLE_ITERATION_RULES = "IterationRules";
	public static final String TABLE_HELPANDINFO = "HelpAndInfo";

	public static final String COLUMN_PLANTTYPES_TYPE = "type";
	public static final String COLUMN_PLANTTYPES_PREFTEMP = "preferredTemp";
	public static final String COLUMN_PLANTTYPES_REQWATER = "requiredWater";
	public static final String COLUMN_PLANTTYPES_PREFPH = "preferredPH";
	public static final String COLUMN_PLANTTYPES_PREFGROUNDSTATE = "preferredGroundState";
	public static final String COLUMN_PLANTTYPES_LIVESFOR = "livesFor";
	public static final String COLUMN_PLANTTYPES_COMFACTOR = "commonnessFactor";
	public static final String COLUMN_PLANTTYPES_MATURES = "maturesAtAge";
	public static final String COLUMN_PLANTTYPES_FLOWTARGET = "floweringTarget";
	public static final String COLUMN_PLANTTYPES_FLOWFOR = "flowersFor";
	public static final String COLUMN_PLANTTYPES_FRUITTARGET = "fruitingTarget";
	public static final String COLUMN_PLANTTYPES_FRUITFOR = "fruitsFor";
	public static final String COLUMN_PLANTTYPES_PHOTO = "photo";

	public static final String COLUMN_HELPANDINFO_DATATYPE = "data_type";
	public static final String COLUMN_HELPANDINFO_REFERENCE = "reference";
	public static final String COLUMN_HELPANDINFO_TEXT = "text";
	
	public static final String COLUMN_CONFIG_ITERATION_DELAY = "iteration_time_delay";
	public static final String COLUMN_CONFIG_PLOT_MATRIX_COLUMNS = "plot_matrix_columns";
	public static final String COLUMN_CONFIG_PLOT_MATRIX_ROWS = "plot_matrix_rows";
	public static final String COLUMN_CONFIG_PLOT_PATTERN = "plot_pattern";

	public static final String COLUMN_ID_LOCAL = "_id";
	public static final String COLUMN_ID_REMOTE = "id";
	public static final String COLUMN_LAST_UPDATED = "last_updated";
	public static final String COLUMN_GLOBAL_VERSION = "version";
	public static final String COLUMN_GLOBAL_ROOT_URL = "root_url";
	public static final String COLUMN_TABLES_TABLENAME = "tablename";

	public static final String COLUMN_OBJECTIVES_ID = "objective_id";
	public static final String COLUMN_OBJECTIVES_DESC = "description";
	public static final String COLUMN_OBJECTIVES_MESSAGE = "completion_message";
	public static final String COLUMN_OBJECTIVES_COMPLETED = "completed";
	
	public static final String PARAM_LATITUDE = "latitude";
	public static final String PARAM_LONGITUDE = "longitude";
	public static final String PARAM_DISTANCE_USER = "distance_user";
	public static final String PARAM_DISTANCE_SPONSOR = "distance_sponsor";
	public static final String PARAM_LAST_UPDATED_USER = "last_updated_user";
	public static final String PARAM_LAST_UPDATED_SPONSOR = "last_updated_sponsor";

	public static final String COLUMN_PLANT_TYPE_ID = "plant_type_id";
	public static final String COLUMN_DISTANCE = "distance";
	public static final String COLUMN_SPONSORED = "sponsored";
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_SUCCESS_COPY = "success_copy";

	public static final String TABLES_VALUES_CONFIG = "Config";
	public static final String TABLES_VALUES_PLANTTYPES = "PlantTypes";
	public static final String TABLES_VALUES_OBJECTIVES = "Objectives";
	public static final String TABLES_VALUES_ITERATION_RULES = "IterationRules";
	public static final String TABLES_VALUES_HELPANDINFO = "HelpAndInfo";

	public static final String HELPANDINFO_PLOT_TYPE_SHORT = "plot_type_short";
	public static final String HELPANDINFO_PLOT_TYPE_LONG = "plot_type_long";

	public static enum RetrievalType {
		COLUMNS,
		DATA
	}

	public static final String FILENAME_LOCAL_ITERATION_RULES = "IterationRules.pkg";
	public static final String FILENAME_REMOTE_ITERATION_RULES = "bambootestv2.pkg";
	public static final String FILENAME_LOCAL_OBJECTIVES = "Objectives.pkg";
	public static final String FILENAME_REMOTE_OBJECTIVES = "bambooobjectivesv1.pkg";
	public static final String FILENAME_LOCAL_GAME_SAVE = "game.data";

	public static final String WEATHER_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx";
	public static final String WEATHER_FORMAT = "json";
	public static final String WEATHER_KEY = "98f9949d95d16e62af30217444c77d9c6d7c44b2";

	public static final String DOWNLOAD_TEST_REMOTE_PATH = ROOT_URL + "rules.csv";
	public static final String DOWNLOAD_TEST_LOCAL_PATH = "test_download.csv";

	public static final int ERROR_INT = -10000;
	
	public static final int ITERATION_TIME_DELAY = 1000;
	public static final int PLANT_TYPE_MENU_ID_START_RANGE = 1000;
	public static final int MENU_GROUP_PLANT_TYPES = 2;

	public static enum REMOTE_DATA_EXCHANGE_DATA_TYPE {
		DOWNLOAD_SEEDS,
		WEATHER,
		UPLOAD_SEED
	}

	public static enum PLANT_DIALOG_TYPE {
		PLANT_TYPE,
		PLANT_INSTANCE,
		NONE
	}
	
	public static final int[][] NEIGHBOURHOOD_STRUCTURE = {
		{0,-1},
		{1,-1},
		{1,0},
		{1,1},
		{0,1},
		{-1,1},
		{-1,0},
		{-1,-1}
		};

	public static final int PLOT_MATRIX_COLUMNS = 5;
	public static final int PLOT_MATRIX_ROWS = 4;

	public static final GroundState[] PLOT_PATTERN = {
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.SOIL
	};

	public static enum GroundState {
		SOIL,
		WATER,
		GRAVEL,
		SAND,
		MUD
	}

	public static final int default_WaterLevel = 3;
	public static final int default_Temperature = 15;
	public static final int default_pHLevel = 0;
	
	public static enum Season {
		SPRING,
		SUMMER,
		AUTUMN,
		WINTER
	}
	
	public static enum PlantState {
		NEW_SEED,
		GROWING,
		WILTING,
		FLOWERING,
		FRUITING,
		CHILLY,
		DEAD
	}

	public static final Season[] default_WEATHER_SEASONS = {Season.WINTER, Season.WINTER,
		Season.SPRING, Season.SPRING, Season.SPRING,
		Season.SUMMER, Season.SUMMER, Season.SUMMER,
		Season.AUTUMN, Season.AUTUMN, Season.AUTUMN,
		Season.WINTER};
	public static final int[] default_WEATHER_TEMPS = {3, 5, 8, 11, 14, 17, 20, 21, 17, 11, 7, 5};
	public static final int[] default_WEATHER_RAIN = {3, 2, 3, 3, 2, 1, 1, 0, 1, 2, 3, 4};

	public static final int default_WEATHER_ROLLING_AVERAGE_LENGTH = 7;
	public static final int default_WEATHER_STAN_DEV = 3; //ie 70% of values will have values in this range of degrees from the average
	public static final int default_WEATHER_MAX_TEMP_CHANGE = 5;
	public static final int default_WEATHER_MAX_RAIN_CHANGE = 2;
	public static final int default_WEATHER_MAX_RUN_SAME_DIRECTION = 3;
	public static final int default_WEATHER_MAX_TEMP = 35;
	public static final int default_WEATHER_MIN_TEMP = -10;
	public static final int default_WEATHER_MAX_RAINFALL = 10;
	public static final int default_WEATHER_MIN_RAINFALL = 0;
	public static final int default_WEATHER_CHANGE_DIRECTION_BIAS_MULTIPLIER = 4;
	public static final int default_WEATHER_CHANGE_DIRECTION_SELECTION_SCALE_MULTIPLIER = 10;
	public static final int default_WEATHER_GROUND_WATER = 2;
	public static final int default_WEATHER_MAX_STANDING_WATER = 15;
	public static final int default_WEATHER_WATER_LEVEL_REDUCTION_EACH_DAY = 2;
	public static final int default_PLANT_EXCESS_WATER_TOLERANCE = 4;
	public static final int default_EDGE_PLOT_RESOURCE_DIVIDER = 2;

	public static final int default_PLANT_STATE_CHANGE_TEMPERATURE_COMFORTABLE_RANGE = 8;
	public static final int default_PLANT_STATE_CHANGE_HEALTH_MOVEMENT = 1;
	public static final int default_PLANT_HEALTH_AT_PLANTING = 60;
	public static final int default_PLANT_HEALTH_FOR_FLOWERING = 50;
	public static final int default_PLANT_HEALTH_FOR_FRUITING = 70;
	public static final int default_PLANT_HEALTH_MINIMUM_FOR_STAYING_ALIVE = 35;
	public static final int default_PLANT_WILTING_LIMIT_BEFORE_DYING = 3;
	public static final int default_PLANT_DISAPPEARS_AFTER = 3;
	
	public static final double default_DISTANCE_USER = 0.05;
	public static final double default_DISTANCE_SPONSOR = 0.2;
	public static final int default_LAST_UPDATE_USER_TIMEGAP_MINUTES = 10;
	public static final int default_LAST_UPDATE_SPONSOR_TIMEGAP_MINUTES = 10 * 6 * 24 * 7;

	public static final int default_GAME_WEATHER_RETRIEVE_FREQ = 20;
	public static final int default_GAME_WEATHER_RETRIEVE_OFFSET = 0;
	public static final int default_GAME_REMOTE_SEEDS_RETRIEVE_FREQ = 20;
	public static final int default_GAME_REMOTE_SEEDS_RETRIEVE_OFFSET = 10;
	public static final int default_ERROR_DISPLAY_FREQ = 30;
	public static final int default_ERROR_DISPLAY_OFFSET  = 0;

	public static final int default_WateringAmount = 4;
	public static final int default_UserWaterAvailability_Initial = 25;
	public static final int default_UserWaterAvailability_Max = 50;
	public static final int default_UserWaterAvailability_DailyChange = 2;
}
