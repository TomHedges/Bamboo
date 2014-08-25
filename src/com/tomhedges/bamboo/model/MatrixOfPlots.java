package com.tomhedges.bamboo.model;

import java.io.Serializable;

import com.tomhedges.bamboo.config.Constants;

import android.util.Log;

public class MatrixOfPlots implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123L;
	
	private static MatrixOfPlots matrix = null;

	private Plot[][] plotArray;
	private Neighbourhood[] neighbourhoodArray;

	private int num_rows;
	private int num_cols;

	// Private constructor
	private MatrixOfPlots(Plot[][] plotArray){
		// CONVERT STRING TO ENUM value!!!
		//strTesting = "WATER";
		//gsTest = GroundState.valueOf(strTesting);

		this.plotArray = plotArray;
		num_rows = plotArray.length;
		num_cols = plotArray[0].length;
	}

	// Singleton Factory method
	public static boolean createMatrix(Plot[][] plotArray) {
		if(matrix == null){
			Log.d(MatrixOfPlots.class.getName(), "Creating matrix...");
			matrix = new MatrixOfPlots(plotArray);
			Log.d(MatrixOfPlots.class.getName(), "...created matrix!");
			return true;
		} else {
			Log.d(MatrixOfPlots.class.getName(), "Matrix already exists, not updating");
			return false;
		}
	}

	// Singleton access method
	public static MatrixOfPlots getMatrix() {
		return matrix;
	}

	public void setNeighbourhoodMatrix(Neighbourhood[] neighbourhoodArray) {
		if (this.neighbourhoodArray == null) { this.neighbourhoodArray = neighbourhoodArray; }
	}

	public Neighbourhood[] getNeighbourhoodMatrix() {
		return neighbourhoodArray;
	}

	public Plot getPlot(int xPos, int yPos) {
		if (xPos>=1 && xPos<=num_cols && yPos>=1 && yPos<=num_rows) {
			Log.d(MatrixOfPlots.class.getName(), "Request for plot @ pos: " + (xPos-1) + "," + (yPos-1) + " (0-based array)");
			//Log.d("Plot Matrix", "Result: " + plotArray[yPos-1][xPos-1].toString());
			return plotArray[yPos-1][xPos-1];
		} else {
			return new Plot(0, 0, 0, null, 0, 0, 0);
		}
	}

	public Plot getPlot(int plotID) {
		int xPos = ((plotID-1) % num_cols) + 1;
		int yPos = ((plotID-1) / num_cols) + 1;
		return getPlot(xPos, yPos);
	}

	public Plot getNeigbouringPlot(int xPosCentral, int yPosCentral, int xShift, int yShift) {
		if ((xPosCentral+xShift)>=1 && (xPosCentral+xShift)<=num_cols && (yPosCentral+yShift)>=1 && (yPosCentral+yShift)<=num_rows) {
			return getPlot(xPosCentral+xShift, yPosCentral+yShift);
		} else {
			Log.d(MatrixOfPlots.class.getName(), "Request for off-edge plot: " + ((xPosCentral+xShift)-1) + "," + ((yPosCentral+yShift)-1) + " (0-based array)");
			int waterLevel = getPlot(xPosCentral, yPosCentral).getWaterLevel()/Constants.default_EDGE_PLOT_RESOURCE_DIVIDER;
			return new Plot(-1, -1, -1, null, waterLevel, -1, -1);
		}
	}

	public int getNumRows() {
		return num_rows;
	}

	public int getNumCols() {
		return num_cols;
	}

	public int numberOfPlotsWithPlants() {
		int numWithPlants = 0;
		for (int loopCounter = 0; loopCounter<=num_rows*num_cols; loopCounter++) {
			if (getPlot(loopCounter).getPlant() != null) {
				numWithPlants++;
			}
		}
		return numWithPlants;
	}

	public Plot[] getPlotsWithPlants() {
		Plot[] plotsWithPlants = new Plot[numberOfPlotsWithPlants()];
		if (plotsWithPlants.length > 0) {
			int plotsWithPlantsPointer = 0;
			for (int loopCounter = 0; loopCounter<=num_rows*num_cols; loopCounter++) {
				if (getPlot(loopCounter) != null) {
					plotsWithPlants[plotsWithPlantsPointer] = getPlot(loopCounter);
					plotsWithPlantsPointer++;
				}
			}
		}
		return plotsWithPlants;
	}

	public void destroy() {
		Log.d(MatrixOfPlots.class.getName(), "Destroying Matrix!");
		matrix = null;
	}
}