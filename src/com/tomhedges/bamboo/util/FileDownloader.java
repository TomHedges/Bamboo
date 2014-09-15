package com.tomhedges.bamboo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * Downloads a list of files from remote addresses, and saves to local filepaths 
 * <br>
 * partly from: http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
 * 
 * @see			Game
 * @see			RemoteDBTableRetrieval
 * @author      Tom Hedges
 */

public class FileDownloader {
	InputStream input;
	OutputStream output;
	HttpURLConnection connection;

	public FileDownloader() {
		input = null;
		output = null;
		connection = null;
	}

	public boolean download(String downloadFrom[], String saveTo[]) {
		Log.d(FileDownloader.class.getName(), "Attempting download of " + downloadFrom.length + " files.");
		int successCounter = 0;
		int blankCounter = 0;
		for (int loopCounter = 0; loopCounter<downloadFrom.length; loopCounter++) {
			//check for file paths without filename...
			if (downloadFrom[loopCounter].endsWith("/")) {
				Log.d(FileDownloader.class.getName(), "Not attempting download of blank filename...");
				blankCounter++;
			} else {

				try {
					Log.d(FileDownloader.class.getName(), "Downloading from: " + downloadFrom[loopCounter] + ", to: " + saveTo[loopCounter]);

					File file = new File(saveTo[loopCounter]);
					if(file.exists()) {
						Log.d(FileDownloader.class.getName(), "Deleting old copy of: " + saveTo[loopCounter]);
						if (file.delete()) {
							Log.d(FileDownloader.class.getName(), "Deletion successful (" + saveTo[loopCounter] + ")");
						} else {
							Log.e(FileDownloader.class.getName(), "Could not delete: " + saveTo[loopCounter]);
						}
					}

					URL url = new URL(downloadFrom[loopCounter]);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

					// expect HTTP 200 OK, so we don't mistakenly save error report
					// instead of the file
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.e(FileDownloader.class.getName(), "Server returned HTTP " + connection.getResponseCode()
								+ " " + connection.getResponseMessage());
					} else {
						// this will be useful to display download percentage
						// might be -1: server did not report the length
						int fileLength = connection.getContentLength();

						// download the file
						input = connection.getInputStream();
						output = new FileOutputStream(saveTo[loopCounter]);

						byte data[] = new byte[4096];
						long total = 0;
						int count;
						while ((count = input.read(data)) != -1) {
							total += count;
							output.write(data, 0, count);
						}
						Log.d(FileDownloader.class.getName(), "Success downloading!");
						successCounter++;
					}

					closedown();
				} catch (Exception e) {
					closedown();
					Log.e(FileDownloader.class.getName(), e.toString());
				}
			}
		}

		if (successCounter + blankCounter == downloadFrom.length) {
			Log.d(FileDownloader.class.getName(), "Successfully downloaded " + successCounter + " of " + downloadFrom.length + " files. (With " + blankCounter + " blanks!)");
			return true;
		} else {
			Log.e(FileDownloader.class.getName(), "Downloaded " + successCounter + " of " + downloadFrom.length + " files - some not successful. Also, there were " + blankCounter + " blanks!)");
			return false;
		}
	}

	public void closedown() {
		try {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		} catch (IOException ignored) {
		}

		if (connection != null)
			connection.disconnect();
	}
}
