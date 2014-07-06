package com.tomhedges.bamboo.config;

public interface Constants {
	public static final String ROOT_URL = "http://54.229.96.8/bamboo-test/";
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

	public static final String WEATHER_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx";
	public static final String WEATHER_FORMAT = "json";
	public static final String WEATHER_KEY = "98f9949d95d16e62af30217444c77d9c6d7c44b2";

	public static final String DOWNLOAD_TEST_REMOTE_PATH = ROOT_URL + "rules.csv";
	public static final String DOWNLOAD_TEST_LOCAL_PATH = "test_download.csv";

	public static final int ITERATION_TIME_DELAY = 1000;

	public static final int PLOT_MATRIX_COLUMNS = 4;
	public static final int PLOT_MATRIX_ROWS = 3;

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
}
