package com.tomhedges.bamboo.model;

import java.io.Serializable;
import java.util.Date;

import com.tomhedges.bamboo.config.Constants;

public class RemoteSeed implements Constants, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123L;
	
	private int plantTypeId;
	private String username;
	private Date last_updated;
	private double distance;
	private boolean sponsored;
	private String message;
	private String success_copy;

	public RemoteSeed(int plantTypeId, String username, Date last_updated, double distance,
			boolean sponsored, String message, String success_copy) {
		this.plantTypeId = plantTypeId;
		this.username = username;
		this.last_updated = last_updated;
		this.distance = distance;
		this.sponsored = sponsored;
		this.message = message;
		this.success_copy = success_copy;
	}

	public int getPlantTypeId() {
		return plantTypeId;
	}

	public String getUsername() {
		return username;
	}

	public Date getLast_updated() {
		return last_updated;
	}

	public double getDistance() {
		return distance;
	}

	public boolean isSponsored() {
		return sponsored;
	}

	public String getMessage() {
		return message;
	}

	public String getSuccess_copy() {
		return success_copy;
	}

	@Override
	public String toString() {
		return "RemoteSeed: plantTypeId=" + plantTypeId + ", plantType=" + plantTypeId + ", username=" + username;
	}
}
