package com.tomhedges.bamboo.model;

import com.tomhedges.bamboo.config.Constants;

public class Plot implements Constants {
	private PlantType plantType;
	private int plotId;
	private int xPosInMatrix;
	private int yPosInMatrix;
	private GroundState groundState;
	private int waterLevel;
	private int temperature;
	private int pHlevel;

	public Plot (int plotId, int xPosInMatrix, int yPosInMatrix, GroundState groundState, int waterLevel, int temperature, int pHlevel) {
		this.plotId = plotId;
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

	public GroundState getGroundState() {
		return groundState;
	}

	public void setWaterLevel(int waterLevel) {
		this.waterLevel = waterLevel;
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public void setPlant(PlantType plant) {
		this.plantType = plant;
	}

	public PlantType getPlant() {
		return plantType;
	}

	@Override
	public String toString() {
		if (plotId>0) {
			return "Plot details:\nID=" + plotId + "\nX Pos=" + xPosInMatrix +"\nY Pos=" + yPosInMatrix + "\nGroundState=" + groundState;
		} else {
			return "NO SUCH PLOT!";
		}
	}
}
