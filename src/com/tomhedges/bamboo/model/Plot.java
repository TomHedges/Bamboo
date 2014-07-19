package com.tomhedges.bamboo.model;

import com.tomhedges.bamboo.config.Constants;

public class Plot {
	private PlantInstance plantInstance;
	private int plotId;
	private int xPosInMatrix;
	private int yPosInMatrix;
	private Constants.GroundState groundState;
	private int waterLevel;
	private int temperature;
	private int pHlevel;

	public Plot (int plotId, int xPosInMatrix, int yPosInMatrix, Constants.GroundState groundState, int waterLevel, int temperature, int pHlevel) {
		this.plotId = plotId;
		this.plantInstance = null;
		this.xPosInMatrix = xPosInMatrix;
		this.yPosInMatrix = yPosInMatrix;
		this.groundState = groundState;
		this.waterLevel = waterLevel;
		this.temperature = temperature;
		this.pHlevel = pHlevel;
	}

	public int getXPosInMatrix() {
		return xPosInMatrix;
	}

	public int getYPosInMatrix() {
		return yPosInMatrix;
	}

	public int getPlotId() {
		return plotId;
	}

	public Constants.GroundState getGroundState() {
		return groundState;
	}

	public void setWaterLevel(int waterLevel) {
		this.waterLevel = waterLevel;
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public void setPlant(PlantInstance plantInstance) {
		this.plantInstance = plantInstance;
	}
	
	public void removePlant() {
		this.plantInstance = null;
	}

	public PlantInstance getPlant() {
		return plantInstance;
	}

	@Override
	public String toString() {
		if (plotId>0) {
			String plantInstDets = "No plant here!";
			if (plantInstance != null) {
				plantInstDets = plantInstance.toString();
			}
			return "Plot details:\nID=" + plotId + "\nX Pos=" + xPosInMatrix +"\nY Pos=" + yPosInMatrix + "\nGroundState=" + groundState + "\nPlant Instance=" + plantInstDets;
		} else {
			return "NO SUCH PLOT!";
		}
	}
}
