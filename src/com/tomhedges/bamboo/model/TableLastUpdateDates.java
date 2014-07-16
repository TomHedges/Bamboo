// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.model;

import java.util.Date;

public class TableLastUpdateDates {
	private Date config;
	private Date plants;

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

	@Override
	public String toString() {
		return "ConfigValues table last updated: " + getConfig() + ", Plants table last updated: " + getPlants();
	}
}
