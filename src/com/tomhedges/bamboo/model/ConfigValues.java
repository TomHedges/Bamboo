package com.tomhedges.bamboo.model;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

public class ConfigValues {
	private Date last_updated;
	private int iteration_time_delay;
	private int plot_matrix_columns;
	private int plot_matrix_rows;
	private String plot_pattern;
	
	private JSONObject jsonObject = null;
	private JSONArray jaData = null;
	
	public void setLast_updated(Date last_updated) {
		this.last_updated = last_updated;
	}
	public Date getLast_updated() {
		return last_updated;
	}
	public void setIteration_time_delay(int iteration_time_delay) {
		this.iteration_time_delay = iteration_time_delay;
	}
	public int getIteration_time_delay() {
		return iteration_time_delay;
	}
	public void setPlot_matrix_columns(int plot_matrix_columns) {
		this.plot_matrix_columns = plot_matrix_columns;
	}
	public int getPlot_matrix_columns() {
		return plot_matrix_columns;
	}
	public void setPlot_matrix_rows(int plot_matrix_rows) {
		this.plot_matrix_rows = plot_matrix_rows;
	}
	public int getPlot_matrix_rows() {
		return plot_matrix_rows;
	}
	public void setPlot_pattern(String plot_pattern) {
		this.plot_pattern = plot_pattern;
	}
	public String getPlot_pattern() {
		return plot_pattern;
	}
	
	public GroundState[] getGroundStates() {
		GroundState[] gsGroundStateArray = null;
		try {
			jsonObject = new JSONObject(plot_pattern);
			jaData = jsonObject.getJSONArray(Constants.COLUMN_CONFIG_PLOT_PATTERN);
			gsGroundStateArray = new GroundState[jaData.length()];
			for (int loopCounter = 0; loopCounter<jaData.length(); loopCounter++) {
				String strGS = jaData.getString(loopCounter);
				gsGroundStateArray[loopCounter] = GroundState.valueOf(strGS);
			}
		} catch (JSONException e) {
			Log.e(ConfigValues.class.getName(), "ERROR trying to parse local ground state data");
			e.printStackTrace();
		}
		return gsGroundStateArray;
	}

	// default
	@Override
	public String toString() {
		return "Last Updated: " + last_updated + ", iteration time delay: " + iteration_time_delay + ", plot_matrix_columns: " + plot_matrix_columns + ", plot_matrix_rows: " + plot_matrix_rows + ", plot_pattern: " + plot_pattern;
	}
}
