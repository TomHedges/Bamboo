package com.tomhedges.bamboo.activities;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.Plot;
import com.tomhedges.bamboo.util.dao.RemoteDBTableRetrieval;

public class RepeatingActivity extends Activity implements OnClickListener, Constants {

	private TextView textOutput;
	private String strText = "";
	private Button btnStart, btnStop;
	private Handler handler;
	private String dtFormat = "dd.MM.yy '@' HH:mm:ss.";
	private SimpleDateFormat sdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repeated_action);

		textOutput = (TextView)findViewById(R.id.actionText);

		btnStart = (Button)findViewById(R.id.startAction);
		btnStart.setOnClickListener(this);
		btnStop = (Button)findViewById(R.id.stopAction);
		btnStop.setOnClickListener(this);

		handler = new Handler();
		sdf = new SimpleDateFormat(dtFormat);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startAction:
			stopRepeatedActivity();
			startRepeatedActivity();
			break;
		case R.id.stopAction:
			stopRepeatedActivity();
			break;
		}
	}

	private void startRepeatedActivity() {
		handler.postDelayed(runnable, ITERATION_TIME_DELAY);
	}

	private void stopRepeatedActivity() {
		handler.removeCallbacks(runnable);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			/* do what you need to do */
			activityToRepeat();
			/* and here comes the "trick" */
			handler.postDelayed(this, ITERATION_TIME_DELAY);
		}
	};

	private void activityToRepeat() {
		Date now = new Date();
		strText = sdf.format(now) + " Rep. delay = " + ITERATION_TIME_DELAY + "ms";

		appendDisplayedText(strText);
	}

	private void appendDisplayedText(String textToAppend) {
		textOutput.setText(textOutput.getText() + "\n" + textToAppend);
	}
}
