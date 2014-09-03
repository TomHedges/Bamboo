// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.model;

import java.util.Date;

public class TableLastUpdateDates {
	private Date config;
	private Date plants;
	private Date objectives;
	private Date iterationRules;
	private Date helpAndInfo;

	public void setConfig(Date config) {
		this.config = config;
	}

	public Date getConfig() {
		return config;
	}

	public void setPlants(Date plants) {
		this.plants = plants;
	}

	public Date getPlants() {
		return plants;
	}

	public void setObjectives(Date objectives) {
		this.objectives = objectives;
	}

	public Date getObjectives() {
		return objectives;
	}

	public void setIterationRules(Date iterationRules) {
		this.iterationRules = iterationRules;
	}

	public Date getIterationRules() {
		return iterationRules;
	}

	public void setHelpAndInfo(Date helpAndInfo) {
		this.helpAndInfo = helpAndInfo;
	}

	public Date getHelpAndInfo() {
		return helpAndInfo;
	}

	@Override
	public String toString() {
		return "ConfigValues table last updated: " + getConfig() + ", Plants table last updated: " + getPlants() + ", Objectives table last updated: " + getObjectives() + ", Iteration Rules list updated: " + getIterationRules() + ", Help and Info table last update: " + getHelpAndInfo();
	}
}
