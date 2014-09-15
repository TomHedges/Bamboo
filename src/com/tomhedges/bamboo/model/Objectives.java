package com.tomhedges.bamboo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Holds the collection of all objectives
 * 
 * @see			Game
 * @see			Objective
 * @author      Tom Hedges
 */

public class Objectives implements Serializable {
	private static final long serialVersionUID = 123L;

	private static Objectives objectives = null;
	private List<Objective> allObjectives;
	private List<Integer> changedObjectives;

	// Private constructor
	private Objectives(Objective[] incomingObjectives) {
		allObjectives = new ArrayList<Objective>();
		changedObjectives = new ArrayList<Integer>();
		
		for (Objective objective : incomingObjectives) {
			allObjectives.add(objective.getID(), objective);
		}
	}

	public static Objectives getObjectives() {
		return objectives;
	}

	public static boolean createObjectives(Objective[] incomingObjectives) {
		if (objectives == null) {
			objectives = new Objectives(incomingObjectives);
			return true;
		} else {
			return false;
		}
	}

	public Objective getObjective(int objectiveID) {
		return allObjectives.get(objectiveID);
	}

	public Objective[] getObjectiveList() {
		//Return list of objectives, minus the test objective which heads the list.
		Objective[] objectiveList = new Objective[allObjectives.size()-1];
		for (int loopCounter = 1; loopCounter<allObjectives.size(); loopCounter++) {
			objectiveList[loopCounter-1] = allObjectives.get(loopCounter);
		}
		return objectiveList;
	}

	public int getTotalNumberOfObjectives() {
		return allObjectives.size()-1;
	}
	
	public int getNumberOfCompletedObjectives() {
		int completedCounter = 0;
		for (int loopCounter = 1; loopCounter<allObjectives.size(); loopCounter++) {
			if (allObjectives.get(loopCounter).isCompleted()) {
				completedCounter++;
			}
		}
		return completedCounter;
	}

	public boolean isObjectiveComplete(int objID) {
		return allObjectives.get(objID).isCompleted();
	}

	public void setObjectiveComplete(int objID) {
		allObjectives.get(objID).setCompleted(true);
		changedObjectives.add(objID);
	}

	public int[] getRecentlyCompletedObjectives() {
		int[] recentlyCompleted = new int[changedObjectives.size()];
		if (recentlyCompleted.length > 0) {
			int loopCounter = 0;
			for (int objectiveID : changedObjectives) {
				recentlyCompleted[loopCounter] = objectiveID;
				loopCounter++;
			}
		}
		resetChangedList();
		return recentlyCompleted;
	}

	private void resetChangedList() {
		changedObjectives.clear();
	}

	public void destroy() {
		Log.d(Objectives.class.getName(), "Destroying Objectives!");
		objectives = null;
	}
}