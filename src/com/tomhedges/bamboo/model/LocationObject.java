package com.tomhedges.bamboo.model;

/**
 * Holds latitude and longitude
 * 
 * @author      Tom Hedges
 */

public class LocationObject {
	private double latitude;
	private double longitude;
	
	public LocationObject() {
		latitude = 0.0;
		longitude = 0.0;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	@Override
	public String toString() {
		return "Latitude: " + latitude + "\nLongitude: " + longitude;
	}
}
