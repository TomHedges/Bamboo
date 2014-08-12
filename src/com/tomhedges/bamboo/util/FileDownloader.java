// partly from: http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog

package com.tomhedges.bamboo.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.util.Log;

public class FileDownloader {
	InputStream input;
	OutputStream output;
	HttpURLConnection connection;

	public FileDownloader() {
		input = null;
		output = null;
		connection = null;
	}

	public boolean download(Context context, String downloadFrom, String saveTo) {
		try {
			Log.d(FileDownloader.class.getName(), "Downloading from: " + downloadFrom + ", to: " + saveTo);

			URL url = new URL(downloadFrom);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.e(FileDownloader.class.getName(), "Server returned HTTP " + connection.getResponseCode()
						+ " " + connection.getResponseMessage());
				return false;
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();

			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(saveTo);

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				total += count;
				output.write(data, 0, count);
			}

			closedown();
			Log.d(FileDownloader.class.getName(), "Success downloading!");
			return true;
		} catch (Exception e) {

			closedown();
			Log.e(FileDownloader.class.getName(), e.toString());
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
