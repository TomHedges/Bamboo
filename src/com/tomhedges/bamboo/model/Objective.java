package com.tomhedges.bamboo.model;

public class Objective {
	
	private int objectiveID;
	private String description;
	private String completionMessage;
	private boolean completed;
	
	public Objective(int objectiveID, String description, String completionMessage, boolean completed) {
		this.objectiveID = objectiveID;
		this.description = description;
		this.completionMessage = completionMessage;
		this.completed = completed;
	}
	
	public int getID() {
		return objectiveID;
	}

	public String getDescription() {
		return description;
	}

	public String getCompletionMessage() {
		return completionMessage;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean status) {
		completed = status;
	}
	
	@Override
	public String toString() {
		return "Objective ID=" + objectiveID + ", Desc=" + description + ", Message=" + completionMessage + ", Completed?=" + completed;
	}
}