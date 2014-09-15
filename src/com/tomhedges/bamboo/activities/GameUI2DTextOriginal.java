package com.tomhedges.bamboo.activities;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.PlantDialogType;
import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.Game.PlotWatered;
import com.tomhedges.bamboo.model.Game.SeedPlanted;
import com.tomhedges.bamboo.model.Game.WaterAllowanceLevel;
import com.tomhedges.bamboo.util.ArrayAdapterObjectives;

/**
 * This class is the original UI developed, using text and a basic table layout. Now superceded by GameUI3D.
 * <br>
 * Development built on original code for building a table display, from: http://www.tutorialsbuzz.com/2014/02/android-building-tablelayout-at-runtime.html
 * 
 * @see			Game
 * @see			GameUI3D
 * @author      Tom Hedges
 */

public class GameUI2DTextOriginal extends Activity implements OnClickListener, Observer {

	private TableLayout table_layout;
	private TextView aboveTableLeft, aboveTableRight, belowTable;
	private Game game;
	private String[] plotInfo;

	private Button btnObjectives;
	private ToggleButton tglWatering;

	// Progress Dialog
	private ProgressDialog pDialog;

	private int touchedPlot = 0;
	private boolean tableBuilt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		game = Game.getGameDetails(this);
		game.addObserver(this);

		if (!game.isGameStarted()) {
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Setting up game...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		setContentView(R.layout.table_display);

		Log.d(GameUI2DTextOriginal.class.getName(), "Retrieving Matrix of plots and building local variables...");

		table_layout = (TableLayout) findViewById(R.id.tableLayout1);
		aboveTableLeft = (TextView) findViewById(R.id.tvAboveTableLeft);
		aboveTableRight = (TextView) findViewById(R.id.tvAboveTableRight);
		belowTable = (TextView) findViewById(R.id.tvBelowTable);
		btnObjectives = (Button) findViewById(R.id.btnObjectives);
		tglWatering = (ToggleButton) findViewById(R.id.tglWatering);

		table_layout.setOnClickListener(this);
		btnObjectives.setOnClickListener(this);
		tglWatering.setOnClickListener(this);

		tableBuilt = false;
	}

	@Override
	public void onStart() {
		Log.d(GameUI2DTextOriginal.class.getName(), "Starting TDA...");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(GameUI2DTextOriginal.class.getName(), "Resuming TDA...");
		super.onResume();
		game.resumeGame();
	}

	@Override
	protected void onPause() {
		Log.d(GameUI2DTextOriginal.class.getName(), "Pausing TDA...");
		super.onPause();
		game.pauseGame();
	}

	@Override
	protected void onStop() {
		Log.d(GameUI2DTextOriginal.class.getName(), "Stopping TDA...");
		super.onStop();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data!= null) {
			if (data instanceof Game.GameDate) {
				final Game.GameDate gameDate = (Game.GameDate) data;
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						aboveTableLeft.setText("Date: " + gameDate.returnDate());
					}
				});
			}

			if (data instanceof Game.GameDetailsText) {
				Game.GameDetailsText gameDetailsText = (Game.GameDetailsText) data;
				updateBelowTableDisplay(gameDetailsText.returnDetails());
			}

			if (data instanceof Game.PlotDetails) {
				Game.PlotDetails plotDetails = (Game.PlotDetails) data;
				Log.d(GameUI2DTextOriginal.class.getName(), "Updating Plot: " + plotDetails.returnPlotID());
				TextView tv = (TextView) findViewById(plotDetails.returnPlotID());
				if (tv != null) {
					Log.d(GameUI2DTextOriginal.class.getName(), "Updating Plot: " + tv.getId());
					tv.setText(plotDetails.returnPlotBasicText());
				}
				plotInfo[tv.getId()-1] = "R: " + game.getYPosFromID(plotDetails.returnPlotID()) + "\nC: " + game.getXPosFromID(plotDetails.returnPlotID()) + "\n" + plotDetails.returnPlotPlotFullDetails();
			}

			if (data instanceof Game.GameStartup) {
				Log.d(GameUI2DTextOriginal.class.getName(), "Received update to game startup...");
				final Game.GameStartup gameStartupDetails = (Game.GameStartup) data;
				if (pDialog != null && pDialog.isShowing()) {
					//Has to be run on UI thread, as altering dialog produced there...
					GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
						public void run() {
							pDialog.setMessage(gameStartupDetails.returnMessage());
							if (gameStartupDetails.returnReadyToPlay()) {
								pDialog.dismiss();
							}
						}
					});
				}
			}

			if (data instanceof Game.ObjectiveUpdate) {
				Log.d(GameUI2DTextOriginal.class.getName(), "Received update on objectives...");
				final Game.ObjectiveUpdate ou = (Game.ObjectiveUpdate) data;
				//Has to be run on UI thread, as altering dialog produced there...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						btnObjectives.setText("Objectives (" + ou.returnNumCompleted() + " of " + ou.returnTotalNum() + " completed)");
					}
				});
			}

			if (data instanceof SeedPlanted) {
				final Game.SeedPlanted seedPlanted = (Game.SeedPlanted) data;
				final String alertToUser;
				boolean isRemote = seedPlanted.returnIsRemote();

				if (isRemote) {
					Log.d(GameUI2DTextOriginal.class.getName(), "Remote plant added: Plot=" + seedPlanted.returnPlotID() + ", From=" + seedPlanted.returnUsername() + ", Plant=" + seedPlanted.returnPlantType() + ", isSponsored=" + seedPlanted.returnIsSponsored());

					if (seedPlanted.returnIsSponsored()) {
						alertToUser = "A present has blown in from " + seedPlanted.returnUsername() + "! Open your new " + seedPlanted.returnPlantType() + " to find out their news...";
					} else {
						alertToUser = "A " + seedPlanted.returnPlantType() + " from nearby player " + seedPlanted.returnUsername() + " has taken root in your garden...";
					}
				} else {
					Log.d(GameUI2DTextOriginal.class.getName(), "Local plant added: Plot=" + seedPlanted.returnPlotID() + ", Plant=" + seedPlanted.returnPlantType());

					alertToUser = seedPlanted.returnPlantType() + " has self-seeded in your garden";
				}

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI2DTextOriginal.this, alertToUser, Toast.LENGTH_SHORT).show();
						final TextView tv = (TextView) findViewById(seedPlanted.returnPlotID());
						tv.setBackgroundResource(R.drawable.cell_shape_highlighted);
						Handler handler = new Handler(); 
						handler.postDelayed(new Runnable() { 
							public void run() { 
								tv.setBackgroundResource(R.drawable.cell_shape);
							} 
						}, 5000); 
					}
				});
			}

			if (data instanceof Game.WeatherValues) {
				final Game.WeatherValues weatherVals = (Game.WeatherValues) data;
				Log.d(GameUI2DTextOriginal.class.getName(), "Weather updated: Temperature=" + weatherVals.returnTemperature() + " degrees C");

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						aboveTableLeft.setText(aboveTableLeft.getText() + "\nSeason is: " + weatherVals.returnSeason().toString());
						aboveTableRight.setText("Weather........\nTemperature: " + weatherVals.returnTemperature() + "\u00B0C\nRainfall: " + weatherVals.returnRainfall() + "mm");
					}
				});
			}

			if (data instanceof Game.SeedUploaded) {
				final Game.SeedUploaded seedUploaded = (Game.SeedUploaded) data;
				Log.d(GameUI2DTextOriginal.class.getName(), "Seed uploaded. Message to player: " + seedUploaded.returnMessage());

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI2DTextOriginal.this, seedUploaded.returnMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}

			if (data instanceof Game.CompletedObjective) {
				final Game.CompletedObjective completedObjective = (Game.CompletedObjective) data;
				final String messageToDisplay = "Objective " + completedObjective.returnID() + " completed! " + completedObjective.returnMessage();
				Log.d(GameUI2DTextOriginal.class.getName(), "Display completed objective message to player: " + messageToDisplay);

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						btnObjectives.setText("Objectives (" + completedObjective.returnNumCompleted() + " of " + completedObjective.returnTotalNum() + " completed)");
						Toast.makeText(GameUI2DTextOriginal.this, messageToDisplay, Toast.LENGTH_SHORT).show();
					}
				});
			}

			if (data instanceof Game.GardenDimensions) {
				final Game.GardenDimensions gardenDimensions = (Game.GardenDimensions) data;
				Log.d(GameUI2DTextOriginal.class.getName(), "Dimensions revealed: rows=" + gardenDimensions.returnRows() + ", cols=" + gardenDimensions.returnCols());

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						plotInfo = new String[gardenDimensions.returnRows() * gardenDimensions.returnCols()];
						// sets up initial table
						Log.d(GameUI2DTextOriginal.class.getName(), "Building table with " + gardenDimensions.returnRows() + " rows and " + gardenDimensions.returnCols() + " columns!");
						BuildTable(gardenDimensions.returnRows(), gardenDimensions.returnCols());
					}
				});
			}



			if (data instanceof PlotWatered) {
				final Game.PlotWatered pw = (Game.PlotWatered) data;

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI2DTextOriginal.this, "Plot " + pw.returnPlotID() + " watered!", Toast.LENGTH_SHORT).show();
						final TextView tv = (TextView) findViewById(pw.returnPlotID());
						tv.setBackgroundResource(R.drawable.cell_shape_watered);
						Handler handler = new Handler(); 
						handler.postDelayed(new Runnable() { 
							public void run() { 
								tv.setBackgroundResource(R.drawable.cell_shape);
							} 
						}, 5000); 
					}
				});
			}

			if (data instanceof WaterAllowanceLevel) {
				final Game.WaterAllowanceLevel wal = (Game.WaterAllowanceLevel) data;

				//Has to be run on UI thread, as crashes otherwise...
				GameUI2DTextOriginal.this.runOnUiThread(new Runnable() {
					public void run() {
						if (wal.returnWaterAllowance() == 0) {
							if (tglWatering.isChecked()) {
								tglWatering.toggle();
							}
							tglWatering.setEnabled(false);
						} else {
							tglWatering.setEnabled(true);
						}
					}
				});
			}
		}
	}

	@Override
	public void onClick(final View v) {

		switch (v.getId()) {

		case R.id.btnObjectives:
			showObjectivesList();
			break;

		case R.id.tglWatering:
			if (tglWatering.isChecked()) {
				Toast.makeText(GameUI2DTextOriginal.this,
						"Watering mode ON",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(GameUI2DTextOriginal.this,
						"Watering mode OFF",
						Toast.LENGTH_SHORT).show();

			}
			break;

		default:
			if (CheckForCellTouch(v.getId())) {
				v.setBackgroundResource(R.drawable.cell_shape);
				if (tglWatering.isChecked()) {
					// end watering request to game, which will send back update
					game.WaterPlotWithID(v.getId());
				} else {
					v.showContextMenu();
				}
			}
			break;
		}

	}

	private void showObjectivesList() {
		// Based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
		// our adapter instance
		ArrayAdapterObjectives adapter = new ArrayAdapterObjectives(this, R.layout.list_element_objectives, game.getObjectiveList());

		// create a new ListView, set the adapter and item click listener
		ListView listViewItems = new ListView(this);
		listViewItems.setAdapter(adapter);

		// put the ListView in the pop up
		AlertDialog alertDialogStores = new AlertDialog.Builder(this)
		.setView(listViewItems)
		.setTitle("Objectives")
		.show();
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
			// Already handled with the sub menu...
			return true;
		case R.id.cmiUproot:
			String plantType = game.getPlotFrom1BasedID(touchedPlot).getPlant().getType();
			game.uprootPlant(touchedPlot);
			TextView tv = (TextView) findViewById(touchedPlot);
			tv.setText(game.getPlotBasicText(touchedPlot));
			plotInfo[touchedPlot-1] = "R: " + game.getYPosFromID(touchedPlot) + "\nC: " + game.getXPosFromID(touchedPlot) + "\nCell ID: " + touchedPlot + "\nPlot:\n" + game.getPlotBasicFullPlotDetails(touchedPlot);
			Toast.makeText(GameUI2DTextOriginal.this, "Uprooted " + plantType, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.cmiDetails:
			//Set variable which will either be plant TYPE ID of 0 for empty plot, or plant INSTANCE ID if there is a plant there...
			Dialog dialogPlot;
			int plantID = 0;
			if (game.getPlotFrom1BasedID(touchedPlot).getPlant() != null) {
				plantID = game.getPlotFrom1BasedID(touchedPlot).getPlant().getId();
				dialogPlot = buildPlotDetailsDialog(touchedPlot, plantID, PlantDialogType.PLANT_INSTANCE);
			} else {
				dialogPlot = buildPlotDetailsDialog(touchedPlot, plantID, PlantDialogType.NONE);
			}

			//now that the dialog is set up, it's time to show it    
			dialogPlot.show();

			return true;
		default:
			if (item.getGroupId() == Constants.MENU_GROUP_PLANT_TYPES) {
				Log.d(GameUI2DTextOriginal.class.getName(), "Building " + touchedPlot);
				Dialog dialogPlant = buildPlantTypeDetailsDialog(item.getItemId() - Constants.PLANT_TYPE_MENU_ID_START_RANGE, touchedPlot, PlantDialogType.PLANT_TYPE);
				//now that the dialog is set up, it's time to show it  
				dialogPlant.show();

				return true;
			}

			return super.onContextItemSelected(item);
		}
	}

	private void BuildTable(int rows, int cols) {
		Log.d(GameUI2DTextOriginal.class.getName(), "Starting build table");

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x - (table_layout.getPaddingLeft() + table_layout.getPaddingRight());
		
		// outer for loop
		for (int rowCounter = 1; rowCounter <= rows; rowCounter++) {

			TableRow row = new TableRow(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			// inner for loop
			for (int colCounter = 1; colCounter <= cols; colCounter++) {

				TextView tv = new TextView(this);
				tv.setLayoutParams(new LayoutParams(width/cols, width/cols)); //making plots square, so only need 'width/cols' value...??
				tv.setBackgroundResource(R.drawable.cell_shape);
				tv.setTextSize(10);
				tv.setPadding(5, 5, 5, 5);
				tv.setId(((rowCounter-1) * cols) + colCounter);
				Log.d(GameUI2DTextOriginal.class.getName(), "Requesting plot @ pos: " + colCounter + "," + rowCounter + " with ID: " + tv.getId() + " (1-based array)");

				tv.setClickable(true);

				row.addView(tv);

				tv.setOnClickListener(this);
				registerForContextMenu(tv);

			}

			Log.d(GameUI2DTextOriginal.class.getName(), "Row " + rowCounter + " completed - adding to view");
			table_layout.addView(row);
		}

		tableBuilt = true;
		Log.d(GameUI2DTextOriginal.class.getName(), "All rows added to view - all set!");
	}

	private boolean CheckForCellTouch(int id) {
		if (id>0 && id <= (game.getNumPlotRows() * game.getNumPlotCols())) {
			return true;
		} else {
			return false;
		}
	}

	private void updateBelowTableDisplay(String forDisplay) {
		belowTable.setText(forDisplay);
	}

	private Dialog buildPlantTypeDetailsDialog(int plantTypeID, int plotID, PlantDialogType plantStyle) {
		final int plotToLinkTo = plotID;
		final int plantToLinkTo = plantTypeID;
		final PlantDialogType plantStyleToLinkTo = plantStyle;

		// Based on code from http://www.helloandroid.com/tutorials/how-display-custom-dialog-your-android-application
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_plant_details);
		dialog.setTitle("Plant details");
		dialog.setCancelable(true);

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
		if (plantStyle.equals(PlantDialogType.PLANT_TYPE)) {
			buttonSelectPlant.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel(); //needs to do something!

					game.plantSeed(plantToLinkTo, plotToLinkTo);
					TextView tv = (TextView) findViewById(plotToLinkTo);
					tv.setText(game.getPlotBasicText(plotToLinkTo));
					plotInfo[plotToLinkTo-1] = "R: " + game.getYPosFromID(plotToLinkTo) + "\nC: " + game.getXPosFromID(plotToLinkTo) + "\nCell ID: " + plotToLinkTo + "\nPlot:\n" + game.getPlotBasicFullPlotDetails(plotToLinkTo);

					Toast.makeText(GameUI2DTextOriginal.this, "Planted " + game.getPlantTypeByPlantTypeID(plantToLinkTo).getType(), Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			buttonSelectPlant.setVisibility(View.GONE);
		}

		Button buttonPlotDets = (Button) dialog.findViewById(R.id.dia_plant_plot_details);
		buttonPlotDets.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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

	private Dialog buildPlotDetailsDialog(int plotID, int plantID, PlantDialogType plantStyle) {
		final int plotToLinkTo = plotID;
		final int plantToLinkTo = plantID;
		final PlantDialogType plantStyleToLinkTo = plantStyle;

		// Based on code from http://www.helloandroid.com/tutorials/how-display-custom-dialog-your-android-application
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_plot_details);
		dialog.setTitle("Plot details");
		dialog.setCancelable(true);
		
		//set up text
		TextView textPlot = (TextView) dialog.findViewById(R.id.dia_plot_text);
		textPlot.setText("Touched cell - details:\n" + plotInfo[plotID - 1]);

		//set up image view
		ImageView imgPlot = (ImageView) dialog.findViewById(R.id.dia_plot_img);
		imgPlot.setImageResource(R.drawable.ic_launcher);

		//set up buttons
		Button buttonPlantDets = (Button) dialog.findViewById(R.id.dia_plot_plant_details);

		if (plantStyle.equals(PlantDialogType.PLANT_INSTANCE) || plantStyle.equals(PlantDialogType.PLANT_TYPE)) {
			buttonPlantDets.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
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