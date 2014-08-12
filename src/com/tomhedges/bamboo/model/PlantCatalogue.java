package com.tomhedges.bamboo.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import android.util.Log;

public class PlantCatalogue {

	private static PlantCatalogue plantCatalogue = null;

	private PlantType[] plantArray;
	private HashMap<Integer, PlantType>  hmPlantType;
	private Map<Integer, PlantType>  test;
	private RemoteSeed[] remoteSeedArray;

	// Private constructor
	private PlantCatalogue(PlantType[] plantTypes) {
		if (plantTypes != null) {
			this.plantArray = plantTypes;
			hmPlantType = new LinkedHashMap<Integer, PlantType> ();
			for (PlantType plant : plantTypes) {
				hmPlantType.put(plant.getPlantTypeId(), plant);
			}
			test = sortByPlantType(hmPlantType);
			remoteSeedArray = new RemoteSeed[0];
		}
	}

	// Singleton Factory method
	public static boolean createPlantCatalogue(PlantType[] plantTypes) {
		if(plantCatalogue == null){
			Log.w(PlantCatalogue.class.getName(), "Creating plant catalogue...");
			plantCatalogue = new PlantCatalogue(plantTypes);
			Log.w(PlantCatalogue.class.getName(), "...created catalogue!");
			return true;
		} else {
			Log.w(PlantCatalogue.class.getName(), "Plant catalogue already exists, not updating");
			return false;
		}
	}

	// Singleton access method
	public static PlantCatalogue getPlantCatalogue() {
		return plantCatalogue;
	}

	public PlantType getPlantTypeByPlantTypeID(int id) {
		Log.w(PlantCatalogue.class.getName(), "Retrieving plant type with ID: " + id);
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
		// apparently this will never return number in brackets, so shouldn't cause array out of bounds error.
		int idToReturn = randomGenerator.nextInt(remoteSeedArray.length);
		Log.w(PlantCatalogue.class.getName(), "Random remote seed to be returned: " + idToReturn + " from min of 0 and max of " + (remoteSeedArray.length-1));
		return remoteSeedArray[randomGenerator.nextInt(remoteSeedArray.length)];
	}

	public int getRemoteSeedCount() {
		return remoteSeedArray.length;
	}

	// uses code from http://javarevisited.blogspot.co.uk/2012/12/how-to-sort-hashmap-java-by-key-and-value.html
	public static <Integer extends Comparable,PlantType extends Comparable> Map<Integer,PlantType> sortByPlantType(Map<Integer,PlantType> map){
		List<Map.Entry<Integer,PlantType>> entries = new LinkedList<Map.Entry<Integer,PlantType>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<Integer,PlantType>>() {
			@Override
			public int compare(Entry<Integer, PlantType> plantTypeA, Entry<Integer, PlantType> plantTypeB) {
				return plantTypeA.getValue().compareTo(plantTypeB.getValue());
			}
		});

		//LinkedHashMap will keep the keys in the order they are inserted
		//which is currently sorted on natural ordering
		Map<Integer,PlantType> sortedMap = new LinkedHashMap<Integer,PlantType>();

		for(Map.Entry<Integer,PlantType> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public Map<java.lang.Integer, com.tomhedges.bamboo.model.PlantType> getPlants() {
		return test;
	}

	public PlantType[] getPlantsSimple() {
		return plantArray;
	}

	public int getPlantTypeCountSimple() {
		return plantArray.length;
	}
}