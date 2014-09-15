package com.tomhedges.bamboo.model;

import java.util.Date;

/**
 * Contains the global settings for remote retrieval - root of URL, plus canonical last update timestamp, data version and the current username
 * 
 * @author      Tom Hedges
 */

public class Globals {
	private int version;
	private String root_url;
	private Date last_updated;
	private String username;

	public long getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getRootURL() {
		return root_url;
	}

	public void setRootURL(String url) {
		this.root_url = url;
	}

	public void setLast_updated(Date last_updated) {
		this.last_updated = last_updated;
	}

	public Date getLast_updated() {
		return last_updated;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	// default
	@Override
	public String toString() {
		return "Version: " + version + ", root_url: " + root_url + ", last_updated: " + last_updated + ", username: " + username;
	}
}
