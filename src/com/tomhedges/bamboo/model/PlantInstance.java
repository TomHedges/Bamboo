package com.tomhedges.bamboo.model;

import java.io.Serializable;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.PlantState;

/**
 * The details of an individual instance of a plant within the model (including its type, health, age, etc.)
 * 
 * @see			PlantType
 * @see			Plot
 * @author      Tom Hedges
 */

public class PlantInstance implements Serializable {
	private static final long serialVersionUID = 123L;
	
	private PlantType plantType;
	private Constants.PlantState plantState;
	private int plantInstanceId;
	private int age; // in days!
	private int daysInCurrentState;
	private boolean isMatureEnoughToFlower;
	private int health;
	private boolean remoteSeededPlant;
	private boolean stateUpdatedThisIteration;
	private String originUsername;
	private String sponsoredMessage;
	private String successCopy;
	private boolean wateredThisIteration;
	private int deathProbability;
	private boolean diesThisIteration;
	private int timesFlowered;
	private int sizeCurrent;

	public PlantInstance(PlantType plantType, int plantInstanceId) {
		this.plantType = plantType;
		this.plantInstanceId = plantInstanceId;
		this.age = 0;
		this.plantState = Constants.PlantState.NEW_SEED;
		this.daysInCurrentState = 0;
		this.health = Constants.default_PLANT_HEALTH_AT_PLANTING;
		this.isMatureEnoughToFlower = false;
		this.timesFlowered = 0;
		this.sizeCurrent = 1;
	}

	public PlantInstance(PlantType plantType, int plantInstanceId, String originUsername, String sponsoredMessage, String successCopy) {
		this(plantType, plantInstanceId);

		remoteSeededPlant = true;
		this.originUsername = originUsername;
		this.sponsoredMessage = sponsoredMessage;
		this.successCopy = successCopy;
		this.wateredThisIteration = false;
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

	public void updateAge() {
		age++;
		if (!isMatureEnoughToFlower && age >= plantType.getMaturesAtAge()) { isMatureEnoughToFlower = true; }
	}

	public boolean isMatureEnoughToFlower() {
		return isMatureEnoughToFlower;
	}

	public int getPreferredTemp() {
		return plantType.getPreferredTemp();
	}

	public int getRequiredWater() {
		return plantType.getRequiredWater();
	}

	public Constants.GroundState getPreferredGroundState() {
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

	public int getFloweringTarget() {
		return plantType.getFloweringTarget();
	}

	public int getFlowersFor() {
		return plantType.getFlowersFor();
	}

	public int getFruitingTarget() {
		return plantType.getFruitingTarget();
	}

	public int getFruitsFor() {
		return plantType.getFruitsFor();
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

	public void setWateredThisIteration(boolean wateredThisIteration) {
		this.wateredThisIteration = wateredThisIteration;
	}

	public boolean isWateredThisIteration() {
		return wateredThisIteration;
	}

	public void setPlantState(Constants.PlantState plantState) {
		if (plantState == this.plantState) {
			daysInCurrentState++;
		} else {
			this.plantState = plantState;
			daysInCurrentState = 1;
		}
		
		if ((plantState == PlantState.GROWING || plantState == PlantState.FLOWERING || plantState == PlantState.FRUITING) && sizeCurrent<=plantType.getSizeMax()) {
			sizeCurrent = sizeCurrent + plantType.getSizeGrowthRate();
			if (sizeCurrent>plantType.getSizeMax()) {
				sizeCurrent=plantType.getSizeMax();
			}
		} else {
			sizeCurrent = sizeCurrent - plantType.getSizeShrinkRate();
			if (sizeCurrent < 1) {
				sizeCurrent = 1;
			}
		}
	}

	public Constants.PlantState getPlantState() {
		return plantState;
	}

	public void setDaysInCurrentState(int daysInCurrentState) {
		this.daysInCurrentState = daysInCurrentState;
	}

	public int getDaysInCurrentState() {
		return daysInCurrentState;
	}

	public void setStateUpdatedThisIteration(boolean stateUpdatedThisIteration) {
		this.stateUpdatedThisIteration = stateUpdatedThisIteration;
	}

	public boolean isStateUpdatedThisIteration() {
		return stateUpdatedThisIteration;
	}

	public void changeHealth(int healthAlteration) {
		health = health + healthAlteration;
		if (health>100) { health = 100; }
		if (health<0) { health = 0; }
	}

	public int getHealth() {
		return health;
	}

	public void setDeathProbability(int deathProbability) {
		this.deathProbability = deathProbability;
	}

	public int getDeathProbability() {
		return deathProbability;
	}

	public void setDiesThisIteration(boolean diesThisIteration) {
		this.diesThisIteration = diesThisIteration;
	}

	public boolean isDiesThisIteration() {
		return diesThisIteration;
	}

	public void setTimesFlowered(int timesFlowered) {
		this.timesFlowered = timesFlowered;
	}

	public int getTimesFlowered() {
		return timesFlowered;
	}

	public int getSize() {
		return sizeCurrent;
	}

	@Override
	public String toString() {
		String additional="";
		if (remoteSeededPlant) {additional = "\norigin username=" + originUsername + "\nsponsored message=" + sponsoredMessage;}
		return "PlantInstance:\ninstance_id=" + plantInstanceId
		+ "\nplantType=" + plantType.toString()
		+ "\nplantState=" + plantState
		+ "\nDays in state=" + daysInCurrentState
		+ "\nSize=" + sizeCurrent
		+ "\nAge in days=" + age
		+ "\nIs Watered?=" + wateredThisIteration
		+ "\nFlowering Target=" + getFloweringTarget()
		+ "\nFlowers For=" + getFlowersFor()
		+ "\nFruiting Target=" + getFruitingTarget()
		+ "\nFruits For=" + getFruitsFor()
		+ "\nHealth=" + getHealth()
		+ additional;
	}
}
