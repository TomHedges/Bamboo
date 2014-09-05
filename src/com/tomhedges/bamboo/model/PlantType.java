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
	private String imageGrowing;
	private String imageWilting;
	private String imageFlowering;
	private String imageFruiting;
	private String imageChilly;
	private String imageDead;
	private int sizeMax;
	private int sizeGrowthRate;
	private int sizeShrinkRate;




	public PlantType(int plantTypeId, String type, int preferredTemp, int requiredWater,
			double preferredPH, Constants.GroundState preferredGroundState, int livesFor,
			int commonnessFactor, int maturesAtAge, int floweringTarget, int flowersFor,
			int fruitingTarget, int fruitsFor, String photoPath, String imageGrowing,
			String imageWilting, String imageFlowering,	String imageFruiting, String imageChilly,
			String imageDead, int sizeMax, int sizeGrowthRate, int sizeShrinkRate) {

		//		this.sizeMax = 100; //NB - this needs to come from remote data...
		//		this.sizeGrowthRate = 15; //NB - this needs to come from remote data...
		//		this.sizeShrinkRate = 10;//NB - this needs to come from remote data...

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
		this.imageGrowing = imageGrowing;
		this.imageWilting = imageWilting;
		this.imageFlowering = imageFlowering;
		this.imageFruiting = imageFruiting;
		this.imageChilly = imageChilly;
		this.imageDead = imageDead;
		this.sizeMax = sizeMax;
		this.sizeGrowthRate = sizeGrowthRate;
		this.sizeShrinkRate = sizeShrinkRate;
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

	public String getImageGrowing() {
		return imageGrowing;
	}

	public String getImageWilting() {
		return imageWilting;
	}

	public String getImageFlowering() {
		return imageFlowering;
	}

	public String getImageFruiting() {
		return imageFruiting;
	}

	public String getImageChilly() {
		return imageChilly;
	}

	public String getImageDead() {
		return imageDead;
	}

	public int getSizeMax() {
		return sizeMax;
	}

	public int getSizeGrowthRate() {
		return sizeGrowthRate;
	}

	public int getSizeShrinkRate() {
		return sizeShrinkRate;
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
