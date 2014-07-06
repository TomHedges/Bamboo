// Original code from http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/

package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.RepeatingActivity;
import com.tomhedges.bamboo.activities.TableDisplayActivity;
import com.tomhedges.bamboo.config.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LaunchFragment extends Fragment implements OnClickListener, Constants {

	private Button btnRepetitonTest, btnTableDisplayTest;
	private Intent i;

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
			i = new Intent(this.getActivity(), TableDisplayActivity.class);
			startActivity(i);
			break;

		default:
			break;
		}
	}
}
