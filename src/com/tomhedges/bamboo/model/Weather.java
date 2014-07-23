package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.util.Log;

import com.tomhedges.bamboo.activities.TableDisplayActivity;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.Season;

public class Weather {

	private static Weather weather = null;

	private Calendar currentDate;
	private Season currentSeason;

	private int currentTemp;
	private int currentRain;

	private boolean currentChangeIncreasing;
	private int currentChangeDaysRemaining = 0;

	private int dayCounter = 0; //For averages - when starting game, and don't have full rollign average set

	private int[] previousTempValues; 
	private int previousTempValuesPointer = 0;
	private int[] previousRainValues; 

	private int[] averageTempValues;
	private int[] averageRainValues;
	private Season[] seasonsForMonth;

	private Random randomGenerator;
	
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
		previousTempValues = new int [Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH];

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
		int diffFromSeasonalAverage = 0;
		if (currentChangeDaysRemaining == 0) {
			if (dayCounter > 0) {
				int totalTemp = 0;
				for (int previousTemp : previousTempValues) {
					totalTemp = totalTemp + previousTemp;
				}

				int numDays;
				if (dayCounter<Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH) {
					numDays = dayCounter;
				} else {
					numDays = Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH;
				}
				int aveTemp = totalTemp / numDays;
				Log.w(Weather.class.getName(), "Rolling average temp: " + aveTemp +  ", Season average temp: " + averageTempValues[currentDate.get(Calendar.MONTH)]);

				diffFromSeasonalAverage = aveTemp - averageTempValues[currentDate.get(Calendar.MONTH)]; //so if rolling average is higher than seasonal, this will generate positive number
			}

			//need better way to do this...
			int randomRange = Constants.default_WEATHER_MAX_TEMP - Constants.default_WEATHER_MIN_TEMP;
			int randomChangeLikelihood = randomGenerator.nextInt(randomRange) + 1;
			// if we are already high, bias random change lower, if we are already low, bias it higher
			randomChangeLikelihood = randomChangeLikelihood - (diffFromSeasonalAverage * Constants.default_WEATHER_CHANGE_DIRECTION_BIAS_MULTIPLIER);
			if (randomChangeLikelihood > (randomRange/2)) { //if randomChangeLikelihood is high, we go up in temp
				currentChangeIncreasing = true; 
			} else if (randomChangeLikelihood < (randomRange/2)) { //if randomChangeLikelihood is low, we go down in temp
				currentChangeIncreasing = false;
			} else {
				currentChangeIncreasing = randomGenerator.nextBoolean(); // all even - so lets make it random!
			}
			Log.w(Weather.class.getName(), "Random change likelihood: " + randomChangeLikelihood +  ", Random Range/2: " + randomRange/2 + ", Temp to rise?= " + currentChangeIncreasing);

			currentChangeDaysRemaining = randomGenerator.nextInt(Constants.default_WEATHER_MAX_RUN_SAME_DIRECTION) + 1;
		}

		int changeFactor;
		int outerLoop = 0; //to limit attempts
		do {
			int innerLoop = 0; //to limit attempts
			do {
				changeFactor = (int) Math.round(randomGenerator.nextGaussian() * Constants.default_WEATHER_STAN_DEV);
				innerLoop++;
			} while ((changeFactor < 0 || changeFactor > Constants.default_WEATHER_MAX_CHANGE) && innerLoop<10);
			outerLoop++;
		} while ((!changesWithinRange(changeFactor)) && outerLoop<10); //repeat if false - ie. changes too extreme!

		if (outerLoop == 10) {
			changeFactor = 0;
		}
		
		Log.w(Weather.class.getName(), "Change in temp is: " + changeFactor);

		applyChange(changeFactor);

		currentChangeDaysRemaining--;
		dayCounter++;
	}

	private void applyChange(int changeFactor) {
		previousTempValues[previousTempValuesPointer] = currentTemp;
		previousTempValuesPointer++;
		if (previousTempValuesPointer == Constants.default_WEATHER_ROLLING_AVERAGE_LENGTH) {
			previousTempValuesPointer = 0;
		}

		if (currentChangeIncreasing) { //weather improving
			currentTemp = currentTemp + changeFactor;
		} else {
			currentTemp = currentTemp - changeFactor;
		}
		
		Log.w(Weather.class.getName(), "New temp is: " + currentTemp);
	}

	private boolean changesWithinRange(int changeFactor) {
		if (currentChangeIncreasing) { //weather improving
			if (currentTemp + changeFactor <= Constants.default_WEATHER_MAX_TEMP) {
				return true; //increase within limit
			} else {
				return false; //increase not within limit
			}
		} else { //weather getting worse!
			if (currentTemp - changeFactor >= Constants.default_WEATHER_MIN_TEMP) {
				return true; //decerase within limit
			} else {
				return false; //decerase not within limit
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
}
