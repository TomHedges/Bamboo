// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.RepeatingActivity;
import com.tomhedges.bamboo.activities.TableDisplayActivity;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.RetrievalType;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.TableLastUpdateDates;
import com.tomhedges.bamboo.util.dao.CommentsDataSource;
import com.tomhedges.bamboo.util.dao.ConfigDataSource;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;
import com.tomhedges.bamboo.util.localdatabase.ConfigSQLiteHelper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LaunchFragment extends Fragment implements OnClickListener, Constants {

	private Button btnRepetitonTest, btnTableDisplayTest;
	private Intent i;

	// Progress Dialog
	private ProgressDialog pDialog;

	// to build local settings
	private RemoteDBTableRetrieval tableRetriever;

	private ConfigDataSource datasource;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.launch, container, false);        

		//setup buttons
		btnRepetitonTest = (Button)v.findViewById(R.id.launchRepeatingActivity);
		btnTableDisplayTest = (Button)v.findViewById(R.id.launchTableDisplayActivity);

		//register listeners
		btnRepetitonTest.setOnClickListener(this);
		btnTableDisplayTest.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.launchRepeatingActivity:
			i = new Intent(this.getActivity(), RepeatingActivity.class);
			startActivity(i);
			break;

		case R.id.launchTableDisplayActivity:
			new RetrieveData().execute();

			//i = new Intent(this.getActivity(), TableDisplayActivity.class);
			//startActivity(i);
			break;

		default:
			break;
		}
	}

	@Override
	public void onResume() {
		if (datasource != null) {
			datasource.open();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (datasource != null) {
			datasource.close();
		}
		super.onPause();
	}

	class RetrieveData extends AsyncTask<Void, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Setting up...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected String doInBackground(Void... params) {
			publishProgress("Checking for updates to local data");
			datasource = new ConfigDataSource(getActivity());
			datasource.open();
			Log.w(LaunchFragment.class.getName(), "Opened Data source!");
			Log.w(LaunchFragment.class.getName(), "Get local globals");
			Globals globalsLocal = datasource.getGlobals();

			tableRetriever = new RemoteDBTableRetrieval();
			Globals globalsRemote = tableRetriever.getGlobals();
			
			if (globalsRemote.getLast_updated().after(globalsLocal.getLast_updated())) {
				publishProgress("Updating local data with globals");
				if (datasource.writeGlobals(globalsRemote)) {
					Log.w(LaunchFragment.class.getName(), "Get local table update values");
					TableLastUpdateDates localTables = datasource.getTableUpdateDates();

					Log.w(LaunchFragment.class.getName(), "Config date (local): " + localTables.getConfig().toString());

					//Now update other local tables!!! AND WE NEED TO DO SOMETHING WITH DATA -eg. URL (use downloaded rather than constants. Shouldthis actually be read from downloaded text file???!
					
					publishProgress("Local data updated");
				} else {
					publishProgress("Local data could not be updated");
				}
			} else {
				publishProgress("All local data already up to date!");
			}
			

			return globalsRemote.toString();
		}

		protected void onProgressUpdate(String... progress) {
			pDialog.setMessage(progress[0]);

		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String retrievedData) {
			String showData;
			if (retrievedData != null){
				showData = retrievedData;
			} else {
				showData = "sorry - null!";
			}

			Toast.makeText(getActivity(), "First field in array: " + showData, Toast.LENGTH_LONG).show();



			i = new Intent(getActivity(), TableDisplayActivity.class);
			startActivity(i);
			
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			
			//make sure it gets closed?
			datasource.close();
		}

	}
}
