// Based on code from http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
// and from http://stackoverflow.com/questions/10860357/is-it-possible-to-read-a-file-from-internal-storage-android

package com.tomhedges.bamboo.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RetrieveOnlineFileFragment extends Fragment implements OnClickListener, Constants {

	private TextView fileData;
	private String fileRawData;

	// declare the dialog as a member field of your activity
	ProgressDialog mProgressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = inflater.inflate(R.layout.retrieve_online_file, container, false);

		fileData = (TextView)v.findViewById(R.id.fileText);
		fileRawData = "(Blank!)";
		
		Button bAdd = (Button) v.findViewById(R.id.getFile);
		bAdd.setOnClickListener(this);

		return v;
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.getFile:
			getFileAndDisplayContents();
			break;
		}
	}

	private void getFileAndDisplayContents() {
		// instantiate it within the onCreate method
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage("Downloading file!");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);

		// execute this when the downloader must be fired
		final DownloadTask downloadTask = new DownloadTask(getActivity());
		downloadTask.execute(DOWNLOAD_TEST_REMOTE_PATH);

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadTask.cancel(true);
			}
		});
	}

	// usually, subclasses of AsyncTask are declared inside the activity class.
	// that way, you can easily modify the UI thread from here
	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;
		private PowerManager.WakeLock mWakeLock;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			FileOutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return "Server returned HTTP " + connection.getResponseCode()
					+ " " + connection.getResponseMessage();
				}

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();
				output = getActivity().openFileOutput(DOWNLOAD_TEST_LOCAL_PATH, Context.MODE_PRIVATE);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow cancelling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
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
			return null;
		}
		
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        // take CPU lock to prevent CPU from going off if the user 
	        // presses the power button during download
	        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
	             getClass().getName());
	        mWakeLock.acquire();
	        mProgressDialog.show();
	    }

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        // if we get here, length is known, now set indeterminate to false
	        mProgressDialog.setIndeterminate(false);
	        mProgressDialog.setMax(100);
	        mProgressDialog.setProgress(progress[0]);
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        mWakeLock.release();
	        mProgressDialog.dismiss();
	        if (result != null) {
	            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
	        } else {
	            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
				Log.d("Retrieve & Display", "retrieved - now trying to display");
	        	accessDownloadedFile();
	        }
	    }
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void accessDownloadedFile() {
		Log.d("Retrieve & Display", "trying to open");
		int ch;
		StringBuffer fileContent = new StringBuffer("");
		FileInputStream fis;
		try {
		    fis = getActivity().openFileInput(DOWNLOAD_TEST_LOCAL_PATH);
		    try {
		        while( (ch = fis.read()) != -1)
		            fileContent.append((char)ch);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
			Log.d("Retrieve & Display", "error trying to read");
		}

		fileRawData = new String(fileContent);
		updateDisplay();
	}
	
	public void updateDisplay() {
		fileData.setText(fileRawData);
	}
}
