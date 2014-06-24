package com.tomhedges.bamboo.util;

import com.tomhedges.bamboo.model.LocationObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationRetrieve implements LocationListener {
	private LocationManager locationManager;
	private Context context;
	private LocationObject objLocation;
	
	// Constructor
	public LocationRetrieve(Context context) {
		Log.d("LocationRetrieve","Creating Locator...");
		this.context = context;
		objLocation = new LocationObject();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d("LocationRetrieve","Location changed!" + location.getLatitude() + ", " + location.getLongitude());
		objLocation.setLatitude(location.getLatitude());
		objLocation.setLongitude(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("LocationRetrieve","disabled...");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("LocationRetrieve","enabled...");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("LocationRetrieve","status: " + status);
	}

	public LocationObject getLocation() {
		return objLocation;
	}

	public void disconnect() {
		Log.d("LocationRetrieve","Removing updates...");
		locationManager.removeUpdates(this);
		Log.d("LocationRetrieve","Removed updates...");
	}
	
	public void connect() {
		Log.d("LocationRetrieve","Requesting updates...");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
		//locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 10, this);
		Log.d("LocationRetrieve","Requested updates...");
	}

}
