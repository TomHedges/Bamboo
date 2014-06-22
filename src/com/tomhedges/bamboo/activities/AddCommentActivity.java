// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.activities;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.util.JSONParser;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddCommentActivity extends Activity implements OnClickListener, Constants {

	private EditText title, message;
	private Button  mSubmit;

	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	//php login script

	//localhost :  
	//testing on your device
	//put your local ip instead,  on windows, run CMD > ipconfig
	//or in mac's terminal type ifconfig and look for the ip under en0 or en1
	// private static final String POST_COMMENT_URL = "http://xxx.xxx.x.x:1234/webservice/addcomment.php";

	//testing on Emulator:
	//private static final String POST_COMMENT_URL = "http://10.0.2.2:1234/webservice/addcomment.php";

	//testing from a real server:
	//private static final String POST_COMMENT_URL = "http://www.mybringback.com/webservice/addcomment.php";

	//ids
	//private static final String TAG_SUCCESS = "success";
	//private static final String TAG_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_comment);

		title = (EditText)findViewById(R.id.title);
		message = (EditText)findViewById(R.id.message);

		mSubmit = (Button)findViewById(R.id.submit);
		mSubmit.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		new PostComment().execute();
	}


	class PostComment extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddCommentActivity.this);
			pDialog.setMessage("Posting Comment...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Check for success tag
			int success;
			String post_title = title.getText().toString();
			String post_message = message.getText().toString();

			//Retrieving Saved Username Data:
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddCommentActivity.this);
			String post_username = sp.getString("username", "anon");
			Log.d("Username", "Set to: " + post_username);

			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", post_username));
				params.add(new BasicNameValuePair("title", post_title));
				params.add(new BasicNameValuePair("message", post_message));

				Log.d("request!", "starting");

				//Posting user data to script 
				JSONObject json = jsonParser.makeHttpRequest(
						POST_COMMENT_URL, "POST", params);

				// full json response
				Log.d("Post Comment attempt", json.toString());

				// json success element
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					Log.d("Comment Added!", json.toString());    
					finish();
					return json.getString(TAG_MESSAGE);
				}else{
					Log.d("Comment Failure!", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;

		}

		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			if (file_url != null){
				Toast.makeText(AddCommentActivity.this, file_url, Toast.LENGTH_LONG).show();
			}

		}

	}


}
