package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Random;

import android.util.Log;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.Season;

/**
 * Class which generates and holds weather conditions, intended to track a month-by-month average, but also produce some "unseasonal" weather
 * 
 * @see			Game
 * @author      Tom Hedges
 */

public class Weather {

	private static Weather weather = null;

	private enum WeatherType {
		Temperature,
		Rain
	}

	private Calendar currentDate;
	private Season currentSeason;

	private int currentTemp;
	private int currentRain;

	private boolean currentTempChangeIncreasing;
	private int currentTempChangeDirectionDaysRemaining = 0;
	private boolean currentRainChangeIncreasing;
	private int currentRainChangeDirectionDaysRemaining = 0;

	private int dayCounter = 0; //For averages - when starting game, and don't have full rolling average set

	private int[] previousTemperatureValues; 
	private int[] previousRainValues; 
	private int previousWeatherValuesPointer = 0;

	private int[] averageTempValues;
	private int[] averageRainValues;
	private Season[] seasonsForMonth;

	private Random randomGenerator;

	private int startOfSpring = 13; // this value will be set accurately at construction 

	// Private constructor
	private Weather(Calendar currentDate, int[] aveTempArray, int[] aveRainArray, Season[] seasonsForMonth) {
		Log.w(Weather.class.getName(), "Constructing Weather object...");

		this.currentDate = currentDate;

		this.seasonsForMonth = seasonsForMonth;

		averageTempValues = aveTempArray;
		averageRainValues = aveRainArray;

		this.currentSeason = seasonsForMonth[currentDate.get(Calendar.MONTH)];
		currentTemp = averageTempValues[currentDate.get(Calendar.MONTH)];
		currentRain = averageRainValues[currentDate.get(Calendar.MONTH)];
		previousTemperatureValues = new int [Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH];
		previousRainValues = new int [Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH];

		randomGenerator = new Random();
		calculateWeatherUpdate();
	}

	// Singleton Factory method
	public static Weather createWeather(Calendar currentDate, int[] aveTempArray, int[] aveRainArray, Season[] seasonsForMonth) {
		Log.w(Weather.class.getName(), "Creating Weather object...");

		if(weather == null){
			weather = new Weather(currentDate, aveTempArray, aveRainArray, seasonsForMonth);
		}
		return weather;
	}

	public void calculateWeatherUpdate() {
		Log.w(Weather.class.getName(), "Calculating Weather update...");

		currentSeason = seasonsForMonth[currentDate.get(Calendar.MONTH)];

		int changeFactor = calcChangeAmount(WeatherType.Temperature);
		applyChange(WeatherType.Temperature, changeFactor);
		currentTempChangeDirectionDaysRemaining--;

		changeFactor = calcChangeAmount(WeatherType.Rain);
		applyChange(WeatherType.Rain, changeFactor);
		currentRainChangeDirectionDaysRemaining--;

		dayCounter++;
	}

	private int calcChangeAmount(WeatherType type) {
		int changeFactor = 0;

		switch (type) {
		case Temperature:
			directionChangeCalculation(type, currentTempChangeDirectionDaysRemaining,
					previousTemperatureValues,
					" degrees C",
					averageTempValues[currentDate.get(Calendar.MONTH)],
					Constants.default_WEATHER_MAX_TEMP,
					Constants.default_WEATHER_MIN_TEMP
			);
			changeFactor = getChangeFactor(type, Constants.default_WEATHER_MAX_TEMP_CHANGE);
			break;

		case Rain:
			directionChangeCalculation(type, currentRainChangeDirectionDaysRemaining,
					previousRainValues,
					"mm",
					averageRainValues[currentDate.get(Calendar.MONTH)],
					Constants.default_WEATHER_MAX_RAINFALL,
					Constants.default_WEATHER_MIN_RAINFALL
			);
			changeFactor = getChangeFactor(type, Constants.default_WEATHER_MAX_RAIN_CHANGE);
			break;
		}

		return changeFactor;
	}

	private int getChangeFactor(WeatherType type, int maxChange) {
		int changeFactor;
		int outerLoop = 0; //to limit attempts
		do {
			int innerLoop = 0; //to limit attempts
			do {
				changeFactor = (int) Math.round(randomGenerator.nextGaussian() * Constants.default_WEATHER_STAN_DEV);
				innerLoop++;
			} while ((changeFactor < 0 || changeFactor > maxChange) && innerLoop<10);
			outerLoop++;
		} while ((!changesWithinRange(type, changeFactor)) && outerLoop<10); //repeat if false - ie. changes too extreme!

		if (outerLoop == 10) {
			changeFactor = 0;
		}

		Log.w(Weather.class.getName(), type + ": Change in value is: " + changeFactor);
		return changeFactor;
	}

	private void directionChangeCalculation(WeatherType type, int currentChangeDirectionDaysRemaining,
			int[] previousValues, String symbol,
			int averageValueForSeason, int maxValue, int minValue) {

		if (currentChangeDirectionDaysRemaining == 0) {
			int diffFromSeasonalAverage = 0;
			if (dayCounter > 0) {
				int totalOfDailyValues = 0;
				for (int previousTemp : previousValues) {
					totalOfDailyValues = totalOfDailyValues + previousTemp;
				}

				int numDays;
				if (dayCounter<Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH) {
					numDays = dayCounter;
				} else {
					numDays = Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH;
				}
				int aveValue = totalOfDailyValues / numDays;
				Log.w(Weather.class.getName(), type + ": Rolling average value: " + aveValue + symbol + ", Season average value: " + averageValueForSeason + symbol);

				diffFromSeasonalAverage = (aveValue - averageValueForSeason) * Constants.default_WEATHER_CHANGE_DIRECTION_SELECTION_SCALE_MULTIPLIER; //so if rolling average is higher than seasonal, this will generate positive number
			}

			// TODO - Improve this probabilistic algorithm
			boolean currentValueChangeIncreasing = false;
			int randomRange = (maxValue - minValue) * Constants.default_WEATHER_CHANGE_DIRECTION_SELECTION_SCALE_MULTIPLIER;
			int randomChangeLikelihood = randomGenerator.nextInt(randomRange) + 1;
			// if we are already high, bias random change lower, if we are already low, bias it higher
			randomChangeLikelihood = randomChangeLikelihood - (diffFromSeasonalAverage * Constants.default_WEATHER_CHANGE_DIRECTION_BIAS_MULTIPLIER);
			if (randomChangeLikelihood > (randomRange/2)) { //if randomChangeLikelihood is high, we go up in temp
				currentValueChangeIncreasing = true; 
			} else if (randomChangeLikelihood < (randomRange/2)) { //if randomChangeLikelihood is low, we go down in temp
				currentValueChangeIncreasing = false;
			} else {
				currentValueChangeIncreasing = randomGenerator.nextBoolean(); // all even - so lets make it random!
			}
			Log.w(Weather.class.getName(), type + ": Random change likelihood: " + randomChangeLikelihood +  ", Random Range/2: " + randomRange/2 + ", Weather value to rise?= " + currentValueChangeIncreasing);

			switch (type) {
			case Temperature:
				currentTempChangeIncreasing = currentValueChangeIncreasing;
				currentTempChangeDirectionDaysRemaining = randomGenerator.nextInt(Constants.default_WEATHER_MAX_RUN_SAME_DIRECTION) + 1;
				break;

			case Rain:
				currentRainChangeIncreasing = currentValueChangeIncreasing;
				currentRainChangeDirectionDaysRemaining = randomGenerator.nextInt(Constants.default_WEATHER_MAX_RUN_SAME_DIRECTION) + 1;
				break;
			}
		}
	}

	private void applyChange(WeatherType type, int changeFactor) {
		switch (type) {
		case Temperature:
			previousTemperatureValues[previousWeatherValuesPointer] = currentTemp;
			if (currentTempChangeIncreasing) { //weather improving
				currentTemp = currentTemp + changeFactor;
				if (currentTemp > Constants.default_WEATHER_MAX_TEMP) {
					currentTemp = Constants.default_WEATHER_MAX_TEMP;
				}
			} else {
				currentTemp = currentTemp - changeFactor;
				if (currentTemp < Constants.default_WEATHER_MIN_TEMP) {
					currentTemp = Constants.default_WEATHER_MIN_TEMP;
				}
			}
			Log.w(Weather.class.getName(), "New " + type + " value is: " + currentTemp);
			break;
		case Rain:
			previousRainValues[previousWeatherValuesPointer] = currentRain;
			if (currentRainChangeIncreasing) { //weather improving
				currentRain = currentRain + changeFactor;
				if (currentRain > Constants.default_WEATHER_MAX_RAINFALL) {
					currentTemp = Constants.default_WEATHER_MAX_RAINFALL;
				}
			} else {
				currentRain = currentRain - changeFactor;
				if (currentRain < Constants.default_WEATHER_MIN_RAINFALL) {
					currentTemp = Constants.default_WEATHER_MIN_RAINFALL;
				}
			}
			Log.w(Weather.class.getName(), "New " + type + " value is: " + currentRain);
			break;
		}

		previousWeatherValuesPointer++;
		if (previousWeatherValuesPointer == Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH) {
			previousWeatherValuesPointer = 0;
		}
	}

	private boolean changesWithinRange(WeatherType type, int changeFactor) {
		boolean currentValueIncreasing = false;
		int currentValue = 0;
		int maxValue = 0;
		int minValue = 0;

		switch (type) {
		case Temperature:
			currentValueIncreasing = currentTempChangeIncreasing;
			currentValue = currentTemp;
			maxValue = Constants.default_WEATHER_MAX_TEMP;
			minValue = Constants.default_WEATHER_MIN_TEMP;
			break;
		case Rain:
			currentValueIncreasing = currentRainChangeIncreasing;
			currentValue = currentRain;
			maxValue = Constants.default_WEATHER_MAX_RAINFALL;
			minValue = Constants.default_WEATHER_MIN_RAINFALL;
			break;
		}

		if (currentValueIncreasing) { //weather improving
			if (currentValue + changeFactor <= maxValue) {
				return true; //increase within limit
			} else {
				return false; //increase not within limit
			}
		} else { //weather getting worse!
			if (currentValue - changeFactor >= minValue) {
				return true; //decrease within limit
			} else {
				return false; //decrease not within limit
			}
		}
	}

	public int getCurrentTemp() {
		return currentTemp;
	}

	public int getCurrentRain() {
		return currentRain;
	}

	public Season getCurrentSeason() {
		return currentSeason;
	}

	public int getStartMonthOfSpring() {
		if (startOfSpring != 13) {
			return startOfSpring;
		} else {
			boolean springSearchReset = false;
			int monthCounter = 1;
			while (startOfSpring == 13) {
				int month = monthCounter % 12 ;
				if (month == 0) {
					month = 12;
				} 

				if (seasonsForMonth[month-1] != Season.SPRING) {
					springSearchReset = true;
				}

				if (springSearchReset == true && seasonsForMonth[month-1] == Season.SPRING) {
					startOfSpring = month;
				}

				monthCounter++;
			}

			return startOfSpring;
		}
	}
}