// Based on code from http://www.tutorialsbuzz.com/2014/02/android-building-tablelayout-at-runtime.html

package com.tomhedges.bamboo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;
import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.GroundState;
import com.tomhedges.bamboo.model.MatrixOfPlots;
import com.tomhedges.bamboo.model.Plot;

public class TableDisplayActivity extends Activity implements OnClickListener, Constants {

	private TableLayout table_layout;
	private EditText rowno_et, colno_et;
	private Button build_btn;
	private int rows, cols = 0;
	private MatrixOfPlots mxPlots;
	private String[] plotInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_display);
		
		mxPlots = MatrixOfPlots.getMatrix();
		plotInfo = new String[PLOT_MATRIX_ROWS * PLOT_MATRIX_COLUMNS];

		//REMOVED as not needed to be dynamic
		//rowno_et = (EditText) findViewById(R.id.rowno_id);
		//colno_et = (EditText) findViewById(R.id.colno_id);
		//build_btn = (Button) findViewById(R.id.build_btn_id);
		table_layout = (TableLayout) findViewById(R.id.tableLayout1);

		//build_btn.setOnClickListener(this);
		table_layout.setOnClickListener(this);

		// sets up intial table
		BuildTable(PLOT_MATRIX_ROWS, PLOT_MATRIX_COLUMNS);
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
                Log.d("Plot Matrix", "Requesting plot @ pos: " + colCounter + "," + rowCounter);
                // Full info in cell.
				//tv.setText("R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + mxPlots.getPlot(colCounter, rowCounter).toString());
                tv.setText(mxPlots.getPlot(colCounter, rowCounter).getGroundState().toString());
                plotInfo[(((rowCounter-1) * cols) + colCounter) - 1] = "R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + mxPlots.getPlot(colCounter, rowCounter).toString();
			    tv.setClickable(true);

				row.addView(tv);

				tv.setOnClickListener(this);

			}

			table_layout.addView(row);
		}
	}

	private void CheckForCellTouch(int id) {
		if (id>0 && id <= (PLOT_MATRIX_ROWS * PLOT_MATRIX_COLUMNS)) {
			//Toast.makeText(TableDisplayActivity.this, "Touched cell id: " + id, Toast.LENGTH_SHORT).show();
			Toast.makeText(TableDisplayActivity.this, "Touched cell - details:\n" + plotInfo[id - 1], Toast.LENGTH_SHORT).show();
		}
	}
} 