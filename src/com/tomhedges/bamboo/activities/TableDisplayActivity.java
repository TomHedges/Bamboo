// Based on code from http://www.tutorialsbuzz.com/2014/02/android-building-tablelayout-at-runtime.html

package com.tomhedges.bamboo.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;
import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.PLANT_DIALOG_TYPE;
import com.tomhedges.bamboo.model.Game;

public class TableDisplayActivity extends Activity implements OnClickListener {

	private TableLayout table_layout;
	private TextView aboveTable, belowTable;
	//private EditText rowno_et, colno_et;
	//private Button build_btn;
	//private int rows, cols = 0;
	private Game game;
	private String[] plotInfo;
	//private int num_rows;
	//private int num_cols;

	private int touchedPlot = 0;

	private Handler handler;

	private int iteration_time_delay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		game = Game.getGameDetails(this);

		setContentView(R.layout.table_display);

		Log.w(TableDisplayActivity.class.getName(), "Retrieving Matrix of plots and building local variables...");
		plotInfo = new String[game.getNumPlotRows() * game.getNumPlotCols()];

		//REMOVED as not needed to be dynamic
		//rowno_et = (EditText) findViewById(R.id.rowno_id);
		//colno_et = (EditText) findViewById(R.id.colno_id);
		//build_btn = (Button) findViewById(R.id.build_btn_id);
		table_layout = (TableLayout) findViewById(R.id.tableLayout1);
		aboveTable = (TextView) findViewById(R.id.tvAboveTable);
		belowTable = (TextView) findViewById(R.id.tvBelowTable);

		//build_btn.setOnClickListener(this);
		table_layout.setOnClickListener(this);

		// sets up initial table
		Log.w(TableDisplayActivity.class.getName(), "Building table with " + game.getNumPlotRows() + " rows and " + game.getNumPlotCols() + " columns!");
		BuildTable(game.getNumPlotRows(), game.getNumPlotCols());

		handler = new Handler();
		iteration_time_delay = game.checkIntSetting(Constants.COLUMN_CONFIG_ITERATION_DELAY);
		updateAboveTableDisplay("Date: " + game.getDateString());
	}

	@Override
	public void onStart() {
		super.onStart();
		game.startGame();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopRepeatedActivity();
		game.pauseGame();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startRepeatedActivity();
		game.resumeGame();
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
			//removed for now as replaced with context menu
			if (CheckForCellTouch(v.getId())) {
				v.showContextMenu();
			}
			break;
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (CheckForCellTouch(v.getId())) {
			touchedPlot = v.getId();
			getMenuInflater().inflate(R.menu.plot_selected_context_menu, menu);
			if (game.getPlotFrom1BasedID(v.getId()).getPlant() == null) {
				menu.findItem(R.id.cmiUproot).setEnabled(false);

				SubMenu submenu = game.getSubMenuPlantTypes(menu.findItem(R.id.cmiPlantSeed));
				// these lines replaced by line above, to decouple menu from knowledge of game objects
				//SubMenu submenu = menu.findItem(R.id.cmiPlantSeed).getSubMenu();
				//submenu.clear();
				//PlantCatalogue plantCat = PlantCatalogue.getPlantCatalogue();
				//PlantType[] plantArray = plantCat.getPlantsSimple();
				//for (int loopCounter = 0; loopCounter < plantArray.length; loopCounter++) {
				//	submenu.add(Menu.NONE, Constants.PLANT_TYPE_MENU_ID_START_RANGE + loopCounter, Menu.NONE, plantArray[loopCounter].getType());
				//}
			} else {
				menu.findItem(R.id.cmiPlantSeed).setEnabled(false);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.cmiPlantSeed:
			// Already handled with the sub menu... apparently...
			//Toast.makeText(TableDisplayActivity.this, "Touched menu item with id: " + item.getItemId() + " (Plant seend button!)", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.cmiUproot:
			String plantType = game.getPlotFrom1BasedID(touchedPlot).getPlant().getType();
			game.uprootPlant(touchedPlot);
			TextView tv = (TextView) findViewById(touchedPlot);
			tv.setText(game.getPlotBasicText(touchedPlot));
			plotInfo[touchedPlot-1] = "R: " + game.getYPosFromID(touchedPlot) + "\nC: " + game.getXPosFromID(touchedPlot) + "\nCell ID: " + touchedPlot + "\nPlot:\n" + game.getPlotBasicFullPlotDetails(touchedPlot);
			Toast.makeText(TableDisplayActivity.this, "Uprooted " + plantType, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.cmiDetails:
			//Set variable which will either be plant TYPE ID of 0 for empty plot, or plant INSTANCE ID if there is a plant there...
			Dialog dialogPlot;
			int plantID = 0;
			if (game.getPlotFrom1BasedID(touchedPlot).getPlant() != null) {
				plantID = game.getPlotFrom1BasedID(touchedPlot).getPlant().getId();
				dialogPlot = buildPlotDetailsDialog(touchedPlot, plantID, PLANT_DIALOG_TYPE.PLANT_INSTANCE);
			} else {
				dialogPlot = buildPlotDetailsDialog(touchedPlot, plantID, PLANT_DIALOG_TYPE.NONE);
			}

			//now that the dialog is set up, it's time to show it    
			dialogPlot.show();

			return true;
		default:
			if (item.getGroupId() == Constants.MENU_GROUP_PLANT_TYPES) {
				//Toast.makeText(TableDisplayActivity.this, "Touched menu item with id: " + item.getItemId(), Toast.LENGTH_SHORT).show();

				Log.w(TableDisplayActivity.class.getName(), "Building " + touchedPlot);
				Dialog dialogPlant = buildPlantTypeDetailsDialog(item.getItemId() - Constants.PLANT_TYPE_MENU_ID_START_RANGE, touchedPlot, PLANT_DIALOG_TYPE.PLANT_TYPE);
				//now that the dialog is set up, it's time to show it  
				dialogPlant.show();

				return true;
			}

			return super.onContextItemSelected(item);
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
				Log.d(TableDisplayActivity.class.getName(), "Requesting plot @ pos: " + colCounter + "," + rowCounter + " with ID: " + tv.getId() + " (1-based array)");
				// Full info in cell.
				//tv.setText("R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + mxPlots.getPlot(colCounter, rowCounter).toString());
				

				tv.setText(game.getPlotBasicText(tv.getId()));
				plotInfo[tv.getId()-1] = "R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + game.getPlotBasicFullPlotDetails(tv.getId());
				
				
				
//				Plot localCopy = game.getPlot(colCounter, rowCounter);
//				String plotText = localCopy.getGroundState().toString();
//				if (localCopy.getPlant() == null) {
//					plotText = plotText + "\nNo plant";
//				} else {
//					plotText = plotText + "\n" + localCopy.getPlant().getType();
//				}
//				tv.setText(plotText);
//				plotInfo[(((rowCounter-1) * cols) + colCounter) - 1] = "R: " + rowCounter + "\nC: " + colCounter + "\nCell ID: " + tv.getId() + "\nPlot:\n" + localCopy.toString();
				
				
				tv.setClickable(true);

				row.addView(tv);

				tv.setOnClickListener(this);
				registerForContextMenu(tv);

			}

			Log.w(TableDisplayActivity.class.getName(), "Row " + rowCounter + " completed - adding to view");
			table_layout.addView(row);
		}
		
		Log.w(TableDisplayActivity.class.getName(), "All rows added to view - all set!");
	}

	private boolean CheckForCellTouch(int id) {
		if (id>0 && id <= (game.getNumPlotRows() * game.getNumPlotCols())) {
			//turn off, as context menu has taken over!
			//Toast.makeText(TableDisplayActivity.this, "Touched cell - details:\n" + plotInfo[id - 1], Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return false;
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
		updateAboveTableDisplay("Date: " + game.getDateString());
		updateBelowTableDisplay("Local Weather:\nTemperature = " + game.getRealTemp() + "\u00B0C\nRainfall = " + game.getRealRainfall() + "mm\nNumber of remote seeds = " + game.getRemoteSeedCount());
	}

	private void updateAboveTableDisplay(String forDisplay) {
		aboveTable.setText(forDisplay);
	}

	private void updateBelowTableDisplay(String forDisplay) {
		belowTable.setText(forDisplay);
	}

	private Dialog buildPlantTypeDetailsDialog(int plantTypeID, int plotID, PLANT_DIALOG_TYPE plantStyle) {
		final int plotToLinkTo = plotID;
		final int plantToLinkTo = plantTypeID;
		final PLANT_DIALOG_TYPE plantStyleToLinkTo = plantStyle;

		// Based on code from http://www.helloandroid.com/tutorials/how-display-custom-dialog-your-android-application
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_plant_details);
		dialog.setTitle("Plant details");
		dialog.setCancelable(true);
		//there are a lot of settings, for dialog, check them all out!

		//set up text
		TextView textPlant = (TextView) dialog.findViewById(R.id.dia_plant_text);
		switch (plantStyle) {
		case PLANT_INSTANCE:
			textPlant.setText(game.getPlotFrom1BasedID(plotID).getPlant().toString());
			break;

		case PLANT_TYPE:
			textPlant.setText(game.getPlantTypeByPlantTypeID(plantTypeID).toString());
			break;
		}


		//set up image view
		ImageView imgPlant = (ImageView) dialog.findViewById(R.id.dia_plant_img);
		imgPlant.setImageResource(R.drawable.ic_launcher);

		//set up buttons
		Button buttonSelectPlant = (Button) dialog.findViewById(R.id.dia_plant_select_plant);
		if (plantStyle.equals(PLANT_DIALOG_TYPE.PLANT_TYPE)) {
			buttonSelectPlant.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel(); //needs to do something!
					
					game.plantSeed(plantToLinkTo, plotToLinkTo);
					TextView tv = (TextView) findViewById(plotToLinkTo);
					tv.setText(game.getPlotBasicText(plotToLinkTo));
					plotInfo[plotToLinkTo-1] = "R: " + game.getYPosFromID(plotToLinkTo) + "\nC: " + game.getXPosFromID(plotToLinkTo) + "\nCell ID: " + plotToLinkTo + "\nPlot:\n" + game.getPlotBasicFullPlotDetails(plotToLinkTo);

					Toast.makeText(TableDisplayActivity.this, "Planted " + game.getPlotFrom1BasedID(touchedPlot).getPlant().getType(), Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			buttonSelectPlant.setVisibility(View.GONE);
		}

		Button buttonPlotDets = (Button) dialog.findViewById(R.id.dia_plant_plot_details);
		buttonPlotDets.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(TableDisplayActivity.this, "Show plot details...", Toast.LENGTH_SHORT).show();
				dialog.cancel();
				Dialog plotDialog = buildPlotDetailsDialog(plotToLinkTo, plantToLinkTo, plantStyleToLinkTo);
				plotDialog.show();
			}
		});

		Button buttonPlantClose = (Button) dialog.findViewById(R.id.dia_plant_close);
		buttonPlantClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		return dialog;
	}

	private Dialog buildPlotDetailsDialog(int plotID, int plantID, PLANT_DIALOG_TYPE plantStyle) {
		final int plotToLinkTo = plotID;
		final int plantToLinkTo = plantID;
		final PLANT_DIALOG_TYPE plantStyleToLinkTo = plantStyle;

		// Based on code from http://www.helloandroid.com/tutorials/how-display-custom-dialog-your-android-application
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_plot_details);
		dialog.setTitle("Plot details");
		dialog.setCancelable(true);
		//there are a lot of settings, for dialog, check them all out!

		//set up text
		TextView textPlot = (TextView) dialog.findViewById(R.id.dia_plot_text);
		textPlot.setText("Touched cell - details:\n" + plotInfo[plotID - 1]);

		//set up image view
		ImageView imgPlot = (ImageView) dialog.findViewById(R.id.dia_plot_img);
		imgPlot.setImageResource(R.drawable.ic_launcher);

		//set up buttons
		Button buttonPlantDets = (Button) dialog.findViewById(R.id.dia_plot_plant_details);

		if (plantStyle.equals(PLANT_DIALOG_TYPE.PLANT_INSTANCE) || plantStyle.equals(PLANT_DIALOG_TYPE.PLANT_TYPE)) {
			buttonPlantDets.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//Toast.makeText(TableDisplayActivity.this, "Show plant details...", Toast.LENGTH_SHORT).show();
					dialog.cancel();
					Dialog plantDialog = buildPlantTypeDetailsDialog(plantToLinkTo, plotToLinkTo, plantStyleToLinkTo);
					plantDialog.show();
				}
			});
		} else {
			buttonPlantDets.setEnabled(false);
		}

		Button buttonPlotClose = (Button) dialog.findViewById(R.id.dia_plot_close);
		buttonPlotClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		touchedPlot = 0;
		return dialog;
	}
}