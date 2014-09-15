// Based on code from http://javapapers.com/android/get-current-location-in-android/

package com.tomhedges.bamboo.util;

import com.tomhedges.bamboo.model.LocationObject;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Requests location updates from the Android OS, using GPS/Network data, and stores the updates within a LocationObject.
 * 
 * Incorporates code sourced from:  http://javapapers.com/android/get-current-location-in-android/
 * 
 * @see			LocationObject
 * @author      Tom Hedges
 */

public class LocationRetrieve implements LocationListener {
	private LocationManager locationManager;
	private Context context;
	private LocationObject objLocation;
	
	// Constructor
	public LocationRetrieve(Context context) {
		Log.d("LocationRetrieve","Creating Locator...");
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		objLocation = new LocationObject();
		connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d("LocationRetrieve","Location changed!" + location.getLatitude() + ", " + location.getLongitude() + ", from: " + location.getProvider());
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
		Log.d("LocationRetrieve","Location requested: Lat=" + objLocation.getLatitude() + ", Long=" + objLocation.getLongitude());
		return objLocation;
	}

	public void disconnect() {
		Log.d("LocationRetrieve","Removing updates...");
		locationManager.removeUpdates(this);
		Log.d("LocationRetrieve","Removed updates...");
	}
	
	public void connect() {
		Criteria crit = new Criteria();
		crit.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		String provider = locationManager.getBestProvider(crit, true);
		locationManager.requestSingleUpdate(provider, this, null);
		Log.d("LocationRetrieve","Best location=" + provider);
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 10, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1 * 60 * 1000, 10, this);
		Log.d("LocationRetrieve","Requested updates...");
	}

}
