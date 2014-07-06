package com.tomhedges.bamboo.model;

import android.util.Log;

import com.tomhedges.bamboo.config.Constants;

public class MatrixOfPlots implements Constants {

	private static MatrixOfPlots matrix;
	
	private Plot[][] plotArray;

	// Private constructor
	private MatrixOfPlots(){
		// CONVERT STRING TO ENUM value!!!
		//gsTest = GroundState.valueOf(strTesting);

		plotArray = new Plot[PLOT_MATRIX_ROWS][PLOT_MATRIX_COLUMNS];

		for (int rowCounter = 0; rowCounter<PLOT_MATRIX_ROWS; rowCounter++) {
			for (int columnCounter = 0; columnCounter<PLOT_MATRIX_COLUMNS; columnCounter++) {
				plotArray[rowCounter][columnCounter] = new Plot((rowCounter * PLOT_MATRIX_COLUMNS) + columnCounter + 1, rowCounter + 1, columnCounter + 1, PLOT_PATTERN[(rowCounter * PLOT_MATRIX_COLUMNS) + columnCounter], 10, 15, 0);
			}
		}
	}

	// Singleton Factory method
	public static MatrixOfPlots getMatrix() {
		if(matrix == null){
			matrix = new MatrixOfPlots();
		}
		return matrix;
	}
	
	public Plot getPlot(int xPos, int yPos) {
		if (xPos>=1 && xPos<=PLOT_MATRIX_COLUMNS && yPos>=1 && yPos<=PLOT_MATRIX_ROWS) {
            Log.d("Plot Matrix", "Request for plot @ pos: " + (xPos-1) + "," + (yPos-1));
            //Log.d("Plot Matrix", "Result: " + plotArray[yPos-1][xPos-1].toString());
			return plotArray[yPos-1][xPos-1];
		} else {
			return new Plot(0, 0, 0, null, 0, 0, 0);
		}
	}
}
