package com.tomhedges.bamboo.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		}
	}

	// Singleton Factory method
	public static boolean createPlantCatalogue(PlantType[] plantTypes) {
		if(plantCatalogue == null){
			plantCatalogue = new PlantCatalogue(plantTypes);
		}
		return true;
	}

	// Singleton access method
	public static PlantCatalogue getPlantCatalogue() {
		return plantCatalogue;
	}

	public PlantType getPlantType(int id) {
		int loopCounter = 0;

		while (loopCounter < plantArray.length && plantArray[loopCounter].getPlantTypeId() != id) {
			loopCounter++;
		}

		if (plantArray[loopCounter].getPlantTypeId() == id) {
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
}