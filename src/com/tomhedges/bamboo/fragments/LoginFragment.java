// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.ReadCommentsActivity;
import com.tomhedges.bamboo.activities.RegisterActivity;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.util.JSONParser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginFragment extends Fragment implements OnClickListener, Constants {

	private EditText user, pass;
	private Button mSubmit, mRegister;

	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	//php login script location:

	//localhost :
	//testing on your device
	//put your local ip instead,  on windows, run CMD > ipconfig
	//or in mac's terminal type ifconfig and look for the ip under en0 or en1
	// private static final String LOGIN_URL = "http://xxx.xxx.x.x:1234/webservice/login.php";

	//testing on Emulator:
	//private static final String LOGIN_URL = "http://54.229.96.8/bamboo-test/login.php";

	//testing from a real server:
	//private static final String LOGIN_URL = "http://www.yourdomain.com/webservice/login.php";

	//JSON element ids from repsonse of php script:
	//private static final String TAG_SUCCESS = "success";
	//private static final String TAG_MESSAGE = "message";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.login, container, false);        

		//setup input fields
		user = (EditText)v.findViewById(R.id.username);
		pass = (EditText)v.findViewById(R.id.password);

		//setup buttons
		mSubmit = (Button)v.findViewById(R.id.login);
		mRegister = (Button)v.findViewById(R.id.register);

		//register listeners
		mSubmit.setOnClickListener(this);
		mRegister.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login:
			new AttemptLogin().execute();
			break;
		case R.id.register:
			//Intent i = new Intent(this.getActivity(), RegisterFragment.class);
			Intent i = new Intent(this.getActivity(), RegisterActivity.class);
			startActivity(i);
			break;

		default:
			break;
		}
	}

	class AttemptLogin extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Check for success tag
			int success;
			String username = user.getText().toString();
			String password = pass.getText().toString();
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("password", password));

				Log.d("request!", "starting");
				// getting product details by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(
						LOGIN_URL, "POST", params);

				// check your log for json response
				Log.d("Login attempt", json.toString());

				// json success tag
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					Log.d("Login Successful!", json.toString());

					// save user data
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
					Editor edit = sp.edit();
					edit.putString("username", username);
					edit.commit();

					//Intent i = new Intent(getActivity(), ReadCommentsFragment.class);
					Intent i = new Intent(getActivity(), ReadCommentsActivity.class);
					// finish(); ??? Don't know what this does
					startActivity(i);
					return json.getString(TAG_MESSAGE);
				}else{
					Log.d("Login Failure!", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;

		}
		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			if (file_url != null){
				Toast.makeText(getActivity(), file_url, Toast.LENGTH_LONG).show();
			}

		}

	}

}
