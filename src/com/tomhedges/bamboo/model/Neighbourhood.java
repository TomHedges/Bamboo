package com.tomhedges.bamboo.model;

import java.util.Random;

import com.tomhedges.bamboo.config.Constants.GroundState;

public class Neighbourhood {
	private Plot centralPlot;
	private Plot[] neighbouringPlots;
	private int neighbourCounter;
	private boolean neighbouringWaterPlot;

	public Neighbourhood(Plot centralPlot, int numNeighbours) {
		this.centralPlot = centralPlot;
		neighbouringPlots = new Plot[numNeighbours];
		neighbourCounter = 0;
		neighbouringWaterPlot = false;
	}

	public Plot getCentralPlot() {
		return centralPlot;
	}

	public void addNeighbour(Plot neighbour) {
		neighbouringPlots[neighbourCounter] = neighbour;
		if (neighbour.getGroundState() == GroundState.WATER) {
			neighbouringWaterPlot = true;
		}
		neighbourCounter++;
	}

	public Plot getNeighbour(int neighbourID) {
		return neighbouringPlots[neighbourID];
	}

	public int getRandomNeighbourID() {
		int[] neighbourWithWater = new int[neighbouringPlots.length];
		int numWithWater = 0;
		int loopCounter = 0;
		for(Plot neighbour : neighbouringPlots) {
			if (neighbour.getWaterLevel() > 0) {
				neighbourWithWater[numWithWater] = loopCounter;
				numWithWater++;
			}
			loopCounter++;
		}
		
		if (numWithWater>0) {
			Random randomGenerator = new Random();
			return neighbourWithWater[randomGenerator.nextInt(numWithWater)];
		} else {
			return -1;
		}
	}

	public int getNeighbourCounter() {
		return neighbourCounter;
	}

	public boolean isNeighbouringWaterPlot() {
		return neighbouringWaterPlot;
	}
}
