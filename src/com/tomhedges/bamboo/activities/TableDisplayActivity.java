// Based on code from http://www.tutorialsbuzz.com/2014/02/android-building-tablelayout-at-runtime.html

package com.tomhedges.bamboo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;
import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.MatrixOfPlots;
import com.tomhedges.bamboo.model.PlantCatalogue;
import com.tomhedges.bamboo.model.Plot;

public class TableDisplayActivity extends Activity implements OnClickListener, Constants {

	private TableLayout table_layout;
	private TextView output;
	//private EditText rowno_et, colno_et;
	//private Button build_btn;
	//private int rows, cols = 0;
	private MatrixOfPlots mxPlots;
	private PlantCatalogue plantCatalogue;
	private Game game;
	private String[] plotInfo;
	private int num_rows;
	private int num_cols;
	
	private Handler handler;
	
	// Storage for user preferences
	private CoreSettings coreSettings;
	private int iteration_time_delay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_display);

		Log.w(TableDisplayActivity.class.getName(), "Retrieving Matrix of plots and building local variables...");
		mxPlots = MatrixOfPlots.getMatrix();
		num_rows = mxPlots.getNumRows();
		num_cols = mxPlots.getNumCols();
		plotInfo = new String[num_rows * num_cols];
		
		plantCatalogue = PlantCatalogue.getPlantCatalogue();

		//REMOVED as not needed to be dynamic
		//rowno_et = (EditText) findViewById(R.id.rowno_id);
		//colno_et = (EditText) findViewById(R.id.colno_id);
		//build_btn = (Button) findViewById(R.id.build_btn_id);
		table_layout = (TableLayout) findViewById(R.id.tableLayout1);
		output = (TextView) findViewById(R.id.testText);
		
		//build_btn.setOnClickListener(this);
		table_layout.setOnClickListener(this);

		// sets up intial table
		Log.w(TableDisplayActivity.class.getName(), "Building table");
		BuildTable(num_rows, num_cols);
		
		handler = new Handler();
		coreSettings = CoreSettings.accessCoreSettings();
		iteration_time_delay = coreSettings.checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
		game = Game.getGameDetails();
		updateIterationCountDisplay("Date: " + game.getDateString());
		startRepeatedActivity();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopRepeatedActivity();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startRepeatedActivity();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		//REMOVED as not needed to be dynamic
		/*		case R.id.build_btn_id:
			String rowstring = rowno_et.getText().toString();
			String colstring = colno_et.getText().toString();

			if (!rowstring.equals("") && !colstring.equals("")) {
				rows = Integer.parseInt(rowstring);
				cols = Integer.parseInt(colstring);
				table_layout.removeAllViews();
				BuildTable(rows, cols);
			}

			else {
				Toast.makeText(TableDisplayActivity.this,
						"Please Enter the row and col Numbers",
						Toast.LENGTH_SHORT).show();
			}
			break;*/

		default:
			CheckForCellTouch(v.getId());
			break;
		}

	}

	private void BuildTable(int rows, int cols) {
		Log.w(TableDisplayActivity.class.getName(), "Starting build table");
		// outer for loop
		for (int rowCounter = 1; rowCounter <= rows; rowCounter++) {

			TableRow row = new TableRow(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			// inner for loop
			for (int colCounter = 1; colCounter <= cols; colCounter++) {

				TextView tv = new TextView(this);
				//tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				tv.setLayoutParams(new LayoutParams(100, 100));
				tv.setBackgroundResource(R.drawable.cell_shape);
				tv.setPadding(5, 5, 5, 5);
				tv.setId(((rowCounter-1) * cols) + colCounter);
				Log.d(TableDisplayActivity.class.getName(), "Requesting plot @ pos: " + colCounter + "," + rowCounter + " (1-based array)");
				// Full info in cell.
				//tv.setText("R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + mxPlots.getPlot(colCounter, rowCounter).toString());
				Plot localCopy = mxPlots.getPlot(colCounter, rowCounter);
				tv.setText(localCopy.getGroundState().toString());
				plotInfo[(((rowCounter-1) * cols) + colCounter) - 1] = "R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + localCopy.toString();
				tv.setClickable(true);

				row.addView(tv);

				tv.setOnClickListener(this);

			}

			Log.w(TableDisplayActivity.class.getName(), "Finishing build table - adding to view");
			table_layout.addView(row);
			Log.w(TableDisplayActivity.class.getName(), "Added to view - all set!");
		}
	}

	private void CheckForCellTouch(int id) {
		if (id>0 && id <= (num_rows * num_cols)) {
			//Toast.makeText(TableDisplayActivity.this, "Touched cell id: " + id, Toast.LENGTH_SHORT).show();
			Toast.makeText(TableDisplayActivity.this, "Touched cell - details:\n" + plotInfo[id - 1], Toast.LENGTH_SHORT).show();
		}
	}

	private void startRepeatedActivity() {
		handler.postDelayed(runnable, iteration_time_delay);
	}

	private void stopRepeatedActivity() {
		handler.removeCallbacks(runnable);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			/* do what you need to do */
			nextIteration();
			/* and here comes the "trick" */
			handler.postDelayed(this, iteration_time_delay);
		}
	};

	private void nextIteration() {
		game.advanceDate();
		updateIterationCountDisplay("Date: " + game.getDateString());
	}
	
	private void updateIterationCountDisplay(String forDisplay) {
		output.setText(forDisplay);
	}
}