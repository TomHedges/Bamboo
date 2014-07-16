package com.tomhedges.bamboo.model;

import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

public class Game {

	private static Game game = null;
	private Calendar gameStartDate;
	private int numOfDaysPlayed;
	private String gameDate;

	// Private constructor
	private Game(){
		gameStartDate = Calendar.getInstance();
		numOfDaysPlayed = 0;
		setDateString();
	}
	
	// Singleton Factory method
	public static Game getGameDetails() {
		if(game == null){
			game = new Game();
		}
		return game;
	}

	public void advanceDate(){
		numOfDaysPlayed = numOfDaysPlayed + 1;
		gameStartDate.add(Calendar.DATE, 1);
		setDateString();
	}
	
	private void setDateString() {
		gameDate = gameStartDate.get(Calendar.DATE) + " " + gameStartDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + gameStartDate.get(Calendar.YEAR);
		Log.w(Game.class.getName(), "Date is: " + gameDate);
	}
	
	public String getDateString(){
		return gameDate;
	}
}
