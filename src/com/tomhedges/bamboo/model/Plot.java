package com.tomhedges.bamboo.model;

import java.io.Serializable;
import android.util.Log;
import com.tomhedges.bamboo.config.Constants;

/**
 * The details of a plot, which is a "cell" within the cellular automata model.
 * It holds water, a resource which is move around as needed by the plants the Plots contain.
 * 
 * @see			PlantInstance
 * @author      Tom Hedges
 */

public class Plot implements Serializable {
	private static final long serialVersionUID = 123L;

	private PlantInstance plantInstance;
	private int plotId;
	private int xPosInMatrix;
	private int yPosInMatrix;
	private Constants.GroundState groundState;
	private int waterLevel;
	private boolean neighbourhoodCreated;

	public Plot (int plotId, int xPosInMatrix, int yPosInMatrix, Constants.GroundState groundState, int waterLevel) { //, int temperature, int pHlevel) {
		this.plotId = plotId;
		this.plantInstance = null;
		this.xPosInMatrix = xPosInMatrix;
		this.yPosInMatrix = yPosInMatrix;
		this.groundState = groundState;
		this.waterLevel = waterLevel;
		neighbourhoodCreated = false;
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
	
	public void changeWaterLevel(int waterLevelChange) {
		waterLevel = waterLevel + waterLevelChange;
	}

	public int getWaterLevel() {
		Log.d(Plot.class.getName(),"Water level in plot: " + plotId + " is: " + waterLevel);
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

	public void setNeighbourhoodCreated(boolean neighbourhoodCreated) {
		this.neighbourhoodCreated = neighbourhoodCreated;
	}

	public boolean isNeighbourhoodCreated() {
		return neighbourhoodCreated;
	}

	public boolean isPlantWatered() {
		return plantInstance.isWateredThisIteration();
	}
	
	public void setPlantWatered(boolean isWatered) {
		plantInstance.setWateredThisIteration(isWatered);
	}

	@Override
	public String toString() {
		if (plotId>0) {
			String plantInstDets = "No plant here!";
			if (plantInstance != null) {
				plantInstDets = "\n" + plantInstance.toString();
			}
			return "Plot details:\nID=" + plotId + "\nX Pos=" + xPosInMatrix +"\nY Pos=" + yPosInMatrix + "\nGroundState=" + groundState + "\nPlant Instance=" + plantInstDets;
		} else {
			return "NO SUCH PLOT!";
		}
	}
}
