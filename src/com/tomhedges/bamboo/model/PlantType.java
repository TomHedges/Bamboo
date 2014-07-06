package com.tomhedges.bamboo.model;

import com.tomhedges.bamboo.config.Constants;

public class PlantType implements Constants {
	private int plantTypeId;
	private String type;
	private int preferredTemp;
	private int requiredWater;
	private double preferredPH;
	private GroundState preferredGroundState;
	private int livesFor;
	private int commonnessFactor;
	private int maturesAtAge;
	private int floweringTarget;
	private int flowersFor;
	private int fruitingTarget;
	private int fruitsFor;

	public PlantType(int plantTypeId, String type, int preferredTemp, int requiredWater,
			double preferredPH, GroundState preferredGroundState, int livesFor, int commonnessFactor) {
		this.plantTypeId = plantTypeId;
		this.type = type;
		this.preferredTemp = preferredTemp;
		this.requiredWater = requiredWater;
		this.preferredPH = preferredPH;
		this.preferredGroundState = preferredGroundState;
		this.livesFor = livesFor;
		this.commonnessFactor = commonnessFactor;
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
}
