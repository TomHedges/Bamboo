package com.tomhedges.bamboo.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class CoreSettings {

	private static CoreSettings coreSettings;
	
	private SharedPreferences sp;
	private Editor edit;

	// Private constructor
	private CoreSettings(Context context) {
		// save URL to preferences
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		edit = sp.edit();
	}

	// Singleton Factory method
	public static void createCoreSettings(Context context) {
		if(coreSettings == null){
			coreSettings = new CoreSettings(context);
		}
	}

	// Singleton Factory method (without context)
	public static CoreSettings accessCoreSettings() {
		if(coreSettings == null){
			return null;
		} else {
			return coreSettings;
		}
	}
	
	public void addSetting(String settingName, String settingValue) {
		Log.d(CoreSettings.class.getName(), "Setting Core String - Setting: " + settingName + ", Value:" + settingValue);
		edit.putString(settingName, settingValue);
		edit.commit();
		Log.d(CoreSettings.class.getName(), "Setting: " + settingName + " added/updated!");
	}

	public void addSetting(String settingName, int settingValue) {
		Log.d(CoreSettings.class.getName(), "Setting Core String - Setting: " + settingName + ", Value:" + settingValue);
		edit.putInt(settingName, settingValue);
		edit.commit();
		Log.d(CoreSettings.class.getName(), "Setting: " + settingName + " added/updated!");
	}
	
	public String checkStringSetting(String settingName) {
		String settingValue = sp.getString(settingName, null);
		Log.d(CoreSettings.class.getName(), "Retrieving setting: " + settingName + ", Value:" + settingValue);
		return settingValue;
	}
	
	public int checkIntSetting(String settingName) {
		int settingValue = sp.getInt(settingName, -1); //-1 is effectively error (setting not found)
		Log.d(CoreSettings.class.getName(), "Retrieving setting: " + settingName + ", Value:" + settingValue);
		return settingValue;
	}
}
