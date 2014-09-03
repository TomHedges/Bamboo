package com.tomhedges.bamboo.model;

import java.io.Serializable;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;

public class PlantType implements Comparable<PlantType>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123L;
	
	private int plantTypeId;
	private String type;
	private int preferredTemp;
	private int requiredWater;
	private double preferredPH;
	private GroundState preferredGroundState;
	private int livesFor; // in years!
	private int commonnessFactor;
	private int maturesAtAge; //in days!
	private int floweringTarget;
	private int flowersFor;
	private int fruitingTarget;
	private int fruitsFor;
	private String photoPath;

	public PlantType(int plantTypeId, String type, int preferredTemp, int requiredWater,
			double preferredPH, Constants.GroundState preferredGroundState, int livesFor, int commonnessFactor,
			int maturesAtAge, int floweringTarget, int flowersFor, int fruitingTarget, int fruitsFor, String photoPath) {
		this.plantTypeId = plantTypeId;
		this.type = type;
		this.preferredTemp = preferredTemp;
		this.requiredWater = requiredWater;
		this.preferredPH = preferredPH;
		this.preferredGroundState = preferredGroundState;
		this.livesFor = livesFor;
		this.commonnessFactor = commonnessFactor;
		this.maturesAtAge = maturesAtAge;
		this.floweringTarget = floweringTarget;
		this.flowersFor = flowersFor;
		this.fruitingTarget = fruitingTarget;
		this.fruitsFor = fruitsFor;
		this.photoPath = photoPath;
	}

	public int getPlantTypeId() {
		return plantTypeId;
	}

	public String getType() {
		return type;
	}

	public int getPreferredTemp() {
		return preferredTemp;
	}

	public int getRequiredWater() {
		return requiredWater;
	}

	public GroundState getPreferredGroundState() {
		return preferredGroundState;
	}

	public double getPreferredPH() {
		return preferredPH;
	}

	public int getLivesFor() {
		return livesFor;
	}

	public int getCommonnessFactor() {
		return commonnessFactor;
	}

	public void setMaturesAtAge(int maturesAtAge) {
		this.maturesAtAge = maturesAtAge;
	}

	public int getMaturesAtAge() {
		return maturesAtAge;
	}

	public int getFloweringTarget() {
		return floweringTarget;
	}

	public int getFlowersFor() {
		return flowersFor;
	}

	public int getFruitingTarget() {
		return fruitingTarget;
	}

	public int getFruitsFor() {
		return fruitsFor;
	}

	public String getPhoto() {
		return photoPath;
	}

	@Override
	public String toString() {
		return "PlantType:\nplantTypeId=" + plantTypeId + "\nplantType=" + type;
	}

	@Override
	public int compareTo(PlantType another) {
		final int BEFORE = -1;
		final int AFTER = 1;

		if (this.type.compareToIgnoreCase(another.type) == BEFORE) {
			return BEFORE;
		} else {
			return AFTER;
		}
	}
}
