package com.tomhedges.bamboo.config;

public interface Constants {
	public static final String ROOT_URL = "http://54.229.96.8/bamboo-test/";
	public static final String ROOT_URL_FIELD_NAME = "root_url";

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

	public static enum RetrievalType {
		COLUMNS,
		DATA
	}

	public static final String WEATHER_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx";
	public static final String WEATHER_FORMAT = "json";
	public static final String WEATHER_KEY = "98f9949d95d16e62af30217444c77d9c6d7c44b2";

	public static final String DOWNLOAD_TEST_REMOTE_PATH = ROOT_URL + "rules.csv";
	public static final String DOWNLOAD_TEST_LOCAL_PATH = "test_download.csv";

	public static final int ITERATION_TIME_DELAY = 1000;
	public static final int PLANT_TYPE_MENU_ID_START_RANGE = 1000;
	public static final int MENU_GROUP_PLANT_TYPES = 2;
	public static enum PLANT_DIALOG_TYPE {
		PLANT_TYPE,
		PLANT_INSTANCE,
		NONE
	}

	//public static final int PLOT_MATRIX_COLUMNS = 4;
	//public static final int PLOT_MATRIX_ROWS = 3;

	public static final GroundState[] PLOT_PATTERN = {
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.WATER,
		GroundState.MUD,
		GroundState.SOIL,
		GroundState.SOIL,
		GroundState.WATER,
		GroundState.MUD,
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

	public static int default_WaterLevel = 10;
	public static int default_Temperature = 15;
	public static int default_pHLevel = 0;

	public static double default_DISTANCE_USER = 0.05;
	public static double default_DISTANCE_SPONSOR = 0.2;
	public static int default_LAST_UPDATE_USER_TIMEGAP_MINUTES = 10;
	public static int default_LAST_UPDATE_SPONSOR_TIMEGAP_MINUTES = 10 * 6 * 24 * 7;

}
