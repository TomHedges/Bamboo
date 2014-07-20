package com.tomhedges.bamboo.model;

import com.tomhedges.bamboo.config.Constants;

public class PlantInstance implements Constants {
	private PlantType plantType;
	private int plantInstanceId;
	private int age;
	private int floweringTargetCount;
	private boolean isFlowering;
	private int flowersForCount;
	private int fruitingTargetCount;
	private int isFruiting;
	private int fruitsForCount;
	private boolean remoteSeededPlant;
	private String originUsername;
	private String sponsoredMessage;
	private String successCopy;

	public PlantInstance(PlantType plantType, int plantInstanceId) {
		this.plantType = plantType;
		this.plantInstanceId = plantInstanceId;
		age = 0;
	}
	
	public PlantInstance(PlantType plantType, int plantInstanceId, String originUsername, String sponsoredMessage, String successCopy) {
		this.plantType = plantType;
		this.plantInstanceId = plantInstanceId;
		age = 0;
		remoteSeededPlant = true;
		this.originUsername = originUsername;
		this.sponsoredMessage = sponsoredMessage;
		this.successCopy = successCopy;
	}
	
	public int getId() {
		return plantType.getPlantTypeId();
	}
	
	public int getPlantInstanceId() {
		return plantInstanceId;
	}

	public String getType() {
		return plantType.getType();
	}

	public int getAge() {
		return age;
	}

	public int getPreferredTemp() {
		return plantType.getPreferredTemp();
	}

	public int getRequiredWater() {
		return plantType.getRequiredWater();
	}

	public GroundState getPreferredGroundState() {
		return plantType.getPreferredGroundState();
	}

	public double getPreferredPH() {
		return plantType.getPreferredPH();
	}

	public int getLivesFor() {
		return plantType.getLivesFor();
	}
	
	public int getCommonnessFactor() {
		return plantType.getCommonnessFactor();
	}

	public String getOriginUsername() {
		return originUsername;
	}

	public String getSponsoredMessage() {
		return sponsoredMessage;
	}
	public String getSuccessCopy() {
		return successCopy;
	}

	@Override
	public String toString() {
		String additional="";
		if (remoteSeededPlant) {additional = "\norigin username=" + originUsername + "\nsponsored message=" + sponsoredMessage;}
		return "PlantInstance:\ninstance_id=" + plantInstanceId + "\nplantType=" + plantType.toString() + additional;
	}
}
