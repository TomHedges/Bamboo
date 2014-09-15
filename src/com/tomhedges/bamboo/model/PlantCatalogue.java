package com.tomhedges.bamboo.model;

import java.io.Serializable;
import java.util.Random;
import android.util.Log;

/**
 * Holds the collection of all PLantTypes, and also any remote seeds which have been retrieved for the local area
 * 
 * @see			Game
 * @see			PlantType
 * @see			PlantInstance
 * @see			RemoteSeed
 * @see			Plot
 * @author      Tom Hedges
 */

public class PlantCatalogue implements Serializable {
	private static final long serialVersionUID = 123L;

	private static PlantCatalogue plantCatalogue = null;

	private PlantType[] plantArray;
	private RemoteSeed[] remoteSeedArray;

	// Private constructor
	private PlantCatalogue(PlantType[] plantTypes) {
		if (plantTypes != null) {
			this.plantArray = plantTypes;
			remoteSeedArray = new RemoteSeed[0];
		}
	}

	// Singleton Factory method
	public static boolean createPlantCatalogue(PlantType[] plantTypes) {
		if(plantCatalogue == null){
			Log.d(PlantCatalogue.class.getName(), "Creating plant catalogue...");
			plantCatalogue = new PlantCatalogue(plantTypes);
			Log.d(PlantCatalogue.class.getName(), "...created catalogue!");
			return true;
		} else {
			Log.e(PlantCatalogue.class.getName(), "Plant catalogue already exists, not updating");
			return false;
		}
	}

	// Singleton access method
	public static PlantCatalogue getPlantCatalogue() {
		return plantCatalogue;
	}

	public PlantType getPlantTypeByPlantTypeID(int id) {
		Log.d(PlantCatalogue.class.getName(), "Retrieving plant type with ID: " + id);
		int loopCounter = 0;

		while (loopCounter < plantArray.length && plantArray[loopCounter].getPlantTypeId() != id) {
			loopCounter++;
		}

		if (loopCounter != plantArray.length) {
			return plantArray[loopCounter];
		} else {
			return null;
		}
	}

	public void setRemoteSeedArray(RemoteSeed[] remoteSeedArray) {
		this.remoteSeedArray = remoteSeedArray;
	}

	public RemoteSeed[] getRemoteSeedArray() {
		return remoteSeedArray;
	}

	public RemoteSeed getRandomRemoteSeed() {
		Random randomGenerator = new Random();
		int idToReturn = randomGenerator.nextInt(remoteSeedArray.length);
		Log.d(PlantCatalogue.class.getName(), "Random remote seed to be returned: " + idToReturn + " from min of 0 and max of " + (remoteSeedArray.length-1));
		return remoteSeedArray[randomGenerator.nextInt(remoteSeedArray.length)];
	}

	public int getRemoteSeedCount() {
		return remoteSeedArray.length;
	}

	public PlantType[] getPlantsSimple() {
		return plantArray;
	}

	public int getPlantTypeCountSimple() {
		return plantArray.length;
	}

	public void destroy() {
		Log.d(PlantCatalogue.class.getName(), "Destroying plant catalogue!");
		plantCatalogue = null;
	}
}