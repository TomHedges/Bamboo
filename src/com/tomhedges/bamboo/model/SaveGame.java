package com.tomhedges.bamboo.model;

import java.io.Serializable;
import java.util.Calendar;

public class SaveGame implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123L;
	
	
	MatrixOfPlots mxPlotsSave;
	PlantCatalogue plantCatalogueSave;
	Objectives objectivesSave;
	int day;
	int month;
	int year;
	int numOfDaysPlayedSave;

	public MatrixOfPlots returnMatrix() {
		return mxPlotsSave;
	}

	public PlantCatalogue returnPlantCatalogue() {
		return plantCatalogueSave;
	}

	public Objectives returnObjectives() {
		return objectivesSave;
	}

	public int returnDay() {
		return day;
	}
	
	public int returnNumOfDaysPlayed() {
		return numOfDaysPlayedSave;
	}
}
