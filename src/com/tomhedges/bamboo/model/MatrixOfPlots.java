package com.tomhedges.bamboo.model;

import android.util.Log;

public class MatrixOfPlots {

	private static MatrixOfPlots matrix = null;
	
	private Plot[][] plotArray;
	
	int num_rows;
	int num_cols;

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
			matrix = new MatrixOfPlots(plotArray);
		}
		return true;
	}

	// Singleton access method
	public static MatrixOfPlots getMatrix() {
		return matrix;
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
	
	public int getNumRows() {
		return num_rows;
	}
	
	public int getNumCols() {
		return num_cols;
	}
}
