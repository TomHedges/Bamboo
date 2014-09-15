package com.tomhedges.bamboo.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Container class for the key model objects, which can be serialized and saved to the local device, then retrieved and deserialized.
 * 
 * @see			MatrixOfPlots
 * @see			PlantCatalogue
 * @see			Objectives
 * @author      Tom Hedges
 */

public class SaveGame implements Serializable {
	private static final long serialVersionUID = 123L;
	
	MatrixOfPlots mxPlotsSave;
	PlantCatalogue plantCatalogueSave;
	Objectives objectivesSave;
	int day;
	int month;
	int year;
	int numOfDaysPlayedSave;
	int waterAllowance;

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
