package com.tomhedges.bamboo.activities;

import glfont.GLFont;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;
import com.threed.jpct.util.Overlay;
import com.tomhedges.bamboo.R;

import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.Constants.PLANT_DIALOG_TYPE;
import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.Game.PlotWatered;
import com.tomhedges.bamboo.model.Game.SeedPlanted;
import com.tomhedges.bamboo.model.Game.WaterAllowanceLevel;
import com.tomhedges.bamboo.util.dao.ArrayAdapterObjectives;


public class GameUI3D extends Activity implements OnTouchListener, Observer {

	// Bamboo testing...
	private static float ONE_DEGREE = 0.0174532925f;
	private static float FORTY_FIVE_DEGREES = 0.785398163f;
	private static final int SEPERATION_DISTANCE = 5;
	private static int CLOCK_SIZE = 256;
	private static int CLOCK_PADDING = 40;
	private static int BTN_OBJ_HEIGHT = 100;
	private static int BTN_OBJ_WIDTH = 730;
	private static int BTN_WATER_HEIGHT = 100;
	private static int BTN_WATER_WIDTH = 350;
	private static int BTN_PADDING = 50;
	private static int BTN_BORDER_PADDING = 10;
	private static int FONT_PADDING = 30;
	private static int FONT_HEIGHT = 50;
	private static int INFO_PADDING = 50;
	private static int INFO_WIDTH = 590;
	private static int INFO_HEIGHT = 230;

	private int BTN_OBJ_TOP_LEFT_X;
	private int BTN_OBJ_TOP_LEFT_Y;
	private int BTN_OBJ_BOTTOM_RIGHT_X;
	private int BTN_OBJ_BOTTOM_RIGHT_Y;

	private int BTN_OBJ_BORDER_TOP_LEFT_X;
	private int BTN_OBJ_BORDER_TOP_LEFT_Y;
	private int BTN_OBJ_BORDER_BOTTOM_RIGHT_X;
	private int BTN_OBJ_BORDER_BOTTOM_RIGHT_Y;

	private int BTN_WATER_TOP_LEFT_X;
	private int BTN_WATER_TOP_LEFT_Y;
	private int BTN_WATER_BOTTOM_RIGHT_X;
	private int BTN_WATER_BOTTOM_RIGHT_Y;

	private int BTN_WATER_BORDER_TOP_LEFT_X;
	private int BTN_WATER_BORDER_TOP_LEFT_Y;
	private int BTN_WATER_BORDER_BOTTOM_RIGHT_X;
	private int BTN_WATER_BORDER_BOTTOM_RIGHT_Y;

	private int INFO_PANEL_TOP_LEFT_X;
	private int INFO_PANEL_TOP_LEFT_Y;
	private int INFO_PANEL_BOTTOM_RIGHT_X;
	private int INFO_PANEL_BOTTOM_RIGHT_Y;

	public static enum ItemSelected {
		NONE,
		PLOT,
		WATER_BUTTON,
		OBJECTIVES_BUTTON
	}
	private static final int NULL_INT = -1;
	private static final int MOVEMENT_RANGE_FOR_CLICK = 10;
	private int plotSelected = NULL_INT;
	private static final String PLOT_NAME_PREFIX = "Plot ID=";

	// Used to handle pause and resume...
	private static GameUI3D master = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(150, 175, 255);

	private boolean touchMoved = false;

	private float touchTurn = 0;
	private float touchTurnUp = 0;

	private float xpos = NULL_INT;
	private float ypos = NULL_INT;
	private float xposMem = NULL_INT;
	private float yposMem = NULL_INT;

	private Object3D hiddenObj, tube, tube2 = null;
	private Overlay overlayClockMarker, overlayYearClock, overlayButtonObjectives, overlayButtonObjectivesBorder, overlayButtonWaterBorder, overlayButtonWater, overlayInfoBack = null;
	private int fps = 0;

	private Camera camera = null;

	private Light sun = null;

	private static final int GRANULARITY = 15;
	private long blitCountDown = 500; 
	private String dots = ".";
	private int dotCounter = 1; 
	private int dotCounterLimit = 5; 
	private GLFont glfTextLabel, glfToggleWater;
	private boolean isZoomAdjusted = false;
	private boolean inWateringMode = false;

	private float lowerLimit = 0.1f;
	private float upperLimit = 1f;
	private static final float ORIG_GROWTH_AMOUNT = 0.03f;
	private float growthAmount = ORIG_GROWTH_AMOUNT;
	private boolean growing = true;
	private SimpleVector origTubePos;

	private int dayCounter = 0;
	private static final int DAY_LIMIT = 360;
	private Object3D[] objCollection;

	private String uiDateBase = "Date: ";
	private String uiSeasonBase = "Season: ";
	private String uiTemperatureBase = "Temperature: "; //degrees symbol = \u00B0
	private String uiRainfallBase = "Rainfall: ";
	private String uiWaterAllowanceBase = "Water Allowance: ";

	private String uiDate = uiDateBase;
	private String uiSeason = uiSeasonBase;
	private String uiTemperature = uiTemperatureBase;
	private String uiRainfall = uiRainfallBase;
	private String uiWaterAllowance = uiWaterAllowanceBase;
	private String uiObjectives = "Objectives";

	private static enum Orientation {
		PORTRAIT,
		LANDSCAPE
	}

	private Orientation CurrentOrientation = null;
	private RGBColor waterTextColor = RGBColor.BLACK; 
	private RGBColor waterTextColorDisabled = new RGBColor(200, 200, 200);
	private RGBColor waterAllowanceTextColor = RGBColor.BLUE; 

	private Game game;
	private ProgressDialog pDialog;
	private boolean dimensionsAvailable = false;
	private boolean gardenConstructed = false;
	private boolean texturesCreated = false;
	private int plotSelectedForMenu = NULL_INT;


	protected void onCreate(Bundle savedInstanceState) {
		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});


		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		mGLView.setOnTouchListener(this);
		registerForContextMenu(mGLView);
		setContentView(mGLView);

		game = Game.getGameDetails(this);
		game.addObserver(this);

		if (!game.isGameStarted()) {
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Setting up game...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			Log.w(GameUI3D.class.getName(), "TEST show - MADE IT HERE... p1");
			pDialog.show();
			Log.w(GameUI3D.class.getName(), "TEST show - MADE IT HERE... p1");
		}

		Log.w(GameUI3D.class.getName(), "Retrieving Matrix of plots and building local variables...");
	}

	@Override
	public void onStart() {
		Log.w(GameUI3D.class.getName(), "Starting 3D UI...");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.w(GameUI3D.class.getName(), "Resuming 3D UI...");
		super.onResume();
		mGLView.onResume();
		game.resumeGame();
	}

	@Override
	protected void onPause() {
		Log.w(GameUI3D.class.getName(), "Pausing 3D UI...");
		super.onPause();
		mGLView.onPause();
		game.pauseGame();
	}

	@Override
	protected void onStop() {
		Log.w(GameUI3D.class.getName(), "Stopping 3D UI...");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.w(GameUI3D.class.getName(), "Destroying 3D UI...");
		super.onDestroy();
		isZoomAdjusted = false;
		mGLView.onPause();
	}


	@Override
	public void update(Observable observable, Object data) {
		if (data!= null) {
			//Toast.makeText(GameUI3D.this, "Notified of updated: " + data.getClass() + " from: " + observable.toString(), Toast.LENGTH_SHORT).show();

			if (data instanceof Game.GameDate) {
				Game.GameDate gameDate = (Game.GameDate) data;
				uiDate = uiDateBase + gameDate.returnDate();
			}

			if (data instanceof Game.GameDetailsText) {
				Game.GameDetailsText gameDetailsText = (Game.GameDetailsText) data;
				// TODO NOT HANDLING IN THE 3D UI FOR THE MOMENT??
				//updateBelowTableDisplay(gameDetailsText.returnDetails());
			}

			if (data instanceof Game.PlotDetails) {
				Game.PlotDetails plotDetails = (Game.PlotDetails) data;
				Log.w(GameUI3D.class.getName(), "Updating Plot: " + plotDetails.returnPlotID());
				// TODO NOT HANDLING IN THE 3D UI FOR THE MOMENT??
			}

			if (data instanceof Game.GameStartup) {
				Log.w(GameUI3D.class.getName(), "Received update to game startup...");
				final Game.GameStartup gameStartupDetails = (Game.GameStartup) data;
				if (pDialog != null && pDialog.isShowing()) {
					//Has to be run on UI thread, as altering dialog produced there...
					GameUI3D.this.runOnUiThread(new Runnable() {
						public void run() {
							pDialog.setMessage(gameStartupDetails.returnMessage());
							if (gameStartupDetails.returnReadyToPlay()) {
								Log.w(GameUI3D.class.getName(), "TEST dismiss - MADE IT HERE... p1");
								pDialog.dismiss();
								Log.w(GameUI3D.class.getName(), "TEST dismiss - MADE IT HERE... p2");
							}
						}
					});
				}
			}

			if (data instanceof Game.ObjectiveUpdate) {
				Log.w(GameUI3D.class.getName(), "Received update on objectives...");
				Game.ObjectiveUpdate ou = (Game.ObjectiveUpdate) data;
				uiObjectives = "Objectives (" + ou.returnNumCompleted() + " of " + ou.returnTotalNum() + " completed)";
			}

			if (data instanceof SeedPlanted) {
				final Game.SeedPlanted seedPlanted = (Game.SeedPlanted) data;
				final String alertToUser;
				boolean isRemote = seedPlanted.returnIsRemote();

				if (isRemote) {
					Log.w(GameUI3D.class.getName(), "Remote plant added: Plot=" + seedPlanted.returnPlotID() + ", From=" + seedPlanted.returnUsername() + ", Plant=" + seedPlanted.returnPlantType() + ", isSponsored=" + seedPlanted.returnIsSponsored());

					if (seedPlanted.returnIsSponsored()) {
						alertToUser = "A present has blown in from " + seedPlanted.returnUsername() + "! Open your new " + seedPlanted.returnPlantType() + " to find out their news...";
					} else {
						alertToUser = "A " + seedPlanted.returnPlantType() + " from nearby player " + seedPlanted.returnUsername() + " has taken root in your garden...";
					}
				} else {
					Log.w(GameUI3D.class.getName(), "Local plant added: Plot=" + seedPlanted.returnPlotID() + ", Plant=" + seedPlanted.returnPlantType());

					alertToUser = seedPlanted.returnPlantType() + " has self-seeded in your garden";
				}
				
				//Has to be run on UI thread, as crashes otherwise??...
				GameUI3D.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI3D.this, alertToUser, Toast.LENGTH_LONG).show();
					}
				});

				final String originalGroundState = getTextureFromGroundState(seedPlanted.returnPlotID());
				objCollection[seedPlanted.returnPlotID()-1].setTexture("plot_highlight");
				Handler handler = new Handler(); 
				handler.postDelayed(new Runnable() { 
					public void run() { 
						objCollection[seedPlanted.returnPlotID()-1].setTexture(originalGroundState);
					} 
				}, 5000);
			}

			if (data instanceof Game.WeatherValues) {
				Game.WeatherValues weatherVals = (Game.WeatherValues) data;
				Log.w(GameUI3D.class.getName(), "Weather updated: Temperature=" + weatherVals.returnTemperature() + " degrees C");
				uiTemperature = uiTemperatureBase + weatherVals.returnTemperature() + "\u00B0C";
				uiRainfall = uiRainfallBase + weatherVals.returnRainfall() + "mm";
				uiSeason = uiSeasonBase + weatherVals.returnSeason().toString();
			}

			if (data instanceof Game.SeedUploaded) {
				final Game.SeedUploaded seedUploaded = (Game.SeedUploaded) data;
				Log.w(GameUI3D.class.getName(), "Seed uploaded. Message to player: " + seedUploaded.returnMessage());

				//Has to be run on UI thread, as crashes otherwise??...
				GameUI3D.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI3D.this, seedUploaded.returnMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}

			if (data instanceof Game.CompletedObjective) {
				final Game.CompletedObjective completedObjective = (Game.CompletedObjective) data;
				final String messageToDisplay = "Objective " + completedObjective.returnID() + " completed! " + completedObjective.returnMessage();
				Log.w(GameUI3D.class.getName(), "Display completed objective message to player: " + messageToDisplay);
				uiObjectives = "Objectives (" + completedObjective.returnNumCompleted() + " of " + completedObjective.returnTotalNum() + " completed)";

				//Has to be run on UI thread, as crashes otherwise??...
				GameUI3D.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI3D.this, messageToDisplay, Toast.LENGTH_LONG).show();
					}
				});
			}

			if (data instanceof Game.GardenDimensions) {
				final Game.GardenDimensions gardenDimensions = (Game.GardenDimensions) data;
				Log.w(GameUI3D.class.getName(), "Dimensions revealed: rows=" + gardenDimensions.returnRows() + ", cols=" + gardenDimensions.returnCols());
				Log.w(GameUI3D.class.getName(), "Able to build 3D view with " + gardenDimensions.returnRows() + " rows and " + gardenDimensions.returnCols() + " columns!");

				//TODO how will this work with the actual loading???
				dimensionsAvailable = true;
			}

			if (data instanceof PlotWatered) {
				final Game.PlotWatered pw = (Game.PlotWatered) data;

				String originalGroundState = getTextureFromGroundState(pw.returnPlotID());
				final String originalGroundStateForRunnable = originalGroundState;

				objCollection[pw.returnPlotID()-1].setTexture("plot_water");
				Handler handler = new Handler(); 
				handler.postDelayed(new Runnable() { 
					public void run() {
						objCollection[pw.returnPlotID()-1].setTexture(originalGroundStateForRunnable);
					} 
				}, 2500);

				//Has to be run on UI thread, as crashes otherwise??...
				GameUI3D.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(GameUI3D.this, "Plot " + pw.returnPlotID() + " watered!", Toast.LENGTH_SHORT).show();
					}
				});
			}

			if (data instanceof WaterAllowanceLevel) {
				Game.WaterAllowanceLevel wal = (Game.WaterAllowanceLevel) data;

				uiWaterAllowance = uiWaterAllowanceBase + wal.returnWaterAllowance() + "%";
				if (wal.returnWaterAllowance() == 0) {
					inWateringMode = false;
					overlayButtonWater.setTexture("water_off");
					overlayButtonWaterBorder.setTexture("water_disabled");
					waterTextColor = waterTextColorDisabled;
					waterAllowanceTextColor = RGBColor.RED;
				} else {
					if (overlayButtonWaterBorder != null) { overlayButtonWaterBorder.setTexture("button_border"); }
					waterTextColor = RGBColor.BLACK;
					waterAllowanceTextColor = RGBColor.BLUE; 
				}
			}
		}
	}

	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			xposMem = me.getX();
			yposMem = me.getY();
			performDownAction();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			if (!touchMoved) {
				performClickAction();
			}
			xpos = NULL_INT;
			ypos = NULL_INT;
			xposMem = NULL_INT;
			yposMem = NULL_INT;
			touchTurn = 0;
			touchTurnUp = 0;

			touchMoved = false;

			performUpAction();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {

			int xDiff;
			int yDiff;

			if (me.getX() > xposMem) {
				xDiff = (int) (me.getX() - xposMem);
			} else {
				xDiff = (int) (xposMem - me.getX());
			}
			if (me.getY() > yposMem) {
				yDiff = (int) (me.getY() - yposMem);
			} else {
				yDiff = (int) (yposMem - me.getY());
			}

			if ((xDiff <= MOVEMENT_RANGE_FOR_CLICK) && (yDiff <= MOVEMENT_RANGE_FOR_CLICK)) {
				touchMoved = false;
			} else {
				touchMoved = true;
			}

			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();

			if (me.getY() <= (mGLView.getMeasuredHeight()/2)) {
				touchTurn = xd / 200f;
			} else {
				touchTurn = xd / -200f;
			}
			touchTurnUp = yd / -200f;
			return true;
		}

		try {
			Thread.sleep(15);
		} catch (Exception e) {
			// No need for this...
		}

		return super.onTouchEvent(me);
	}

	private ItemSelected checkForTappedItem() {
		boolean actioned = false;
		ItemSelected tapped = ItemSelected.NONE;

		if (!actioned) {
			SimpleVector dir = Interact2D.reproject2D3DWS(camera, fb, (int) xposMem, (int) yposMem).normalize();
			Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 50000 /*or whatever*/);

			if (res[1] != null) {
				Object3D temp = (Object3D) res[1];
				if (plotSelected == NULL_INT) {
					plotSelected = Integer.parseInt(temp.getName().substring(PLOT_NAME_PREFIX.length(), temp.getName().length()));
					Logger.log("Plot selected=" + plotSelected);
				}
				tapped = ItemSelected.PLOT;
				actioned = true;
			}
		}

		if (!actioned) {
			if (xpos>=BTN_OBJ_TOP_LEFT_X && xpos<=BTN_OBJ_BOTTOM_RIGHT_X && ypos>=BTN_OBJ_TOP_LEFT_Y && ypos<=BTN_OBJ_BOTTOM_RIGHT_Y) {
				tapped = ItemSelected.OBJECTIVES_BUTTON;
				actioned = true;
			}
		}

		if (!actioned) {
			if (xpos>=BTN_WATER_TOP_LEFT_X && xpos<=BTN_WATER_BOTTOM_RIGHT_X && ypos>=BTN_WATER_TOP_LEFT_Y && ypos<=BTN_WATER_BOTTOM_RIGHT_Y) {
				tapped = ItemSelected.WATER_BUTTON;
				actioned = true;
			}
		}

		return tapped;
	}

	private void performUpAction() {
		overlayButtonObjectives.setTexture("button_back");
		plotSelected = NULL_INT;
	}

	private void performDownAction() {
		switch (checkForTappedItem()) {
		case NONE:
			//do nothing
			break;

		case OBJECTIVES_BUTTON:
			overlayButtonObjectives.setTexture("button_border");
			break;
		}
	}

	private void performClickAction() {
		Logger.log("performClickAction clicked! at xposMem=" + xposMem + ", yposMem=" + yposMem);

		switch (checkForTappedItem()) {
		case NONE:
			//do nothing
			break;

		case OBJECTIVES_BUTTON:
			overlayButtonObjectives.setTexture("button_back");
			//Toast.makeText(this, "Show objectives...", Toast.LENGTH_SHORT).show();
			showObjectivesList();
			break;

		case WATER_BUTTON:
			if (inWateringMode) {
				inWateringMode = false;
				overlayButtonWater.setTexture("water_off");
			} else {
				inWateringMode = true;
				overlayButtonWater.setTexture("water_on");
			}
			break;

		case PLOT:
			//Toast.makeText(this, "Plot clicked=" + plotSelected, Toast.LENGTH_SHORT).show();

			String originalGroundState = getTextureFromGroundState(plotSelected);
			objCollection[plotSelected-1].setTexture(originalGroundState);
			if (inWateringMode) {
				game.WaterPlotWithID(plotSelected);
			} else {
				mGLView.showContextMenu();
			}
			
			plotSelected = NULL_INT;
			break;
		}
	}

	private void showObjectivesList() {
		//based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
		// our adapter instance
		ArrayAdapterObjectives adapter = new ArrayAdapterObjectives(this, R.layout.list_element_objectives, game.getObjectiveList());

		// create a new ListView, set the adapter and item click listener
		ListView listViewItems = new ListView(this);
		listViewItems.setAdapter(adapter);
		// should make this cancel??
		//listViewItems.setOnItemClickListener(new OnItemClickListenerListViewItem());

		// put the ListView in the pop up
		AlertDialog alertDialogStores = new AlertDialog.Builder(this)
		.setView(listViewItems)
		.setTitle("Objectives")
		.show();
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {

			Logger.log("onSurfaceChanged!");

			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);

			if (!isZoomAdjusted && camera != null && mGLView.getMeasuredHeight() > mGLView.getMeasuredWidth()) {
				camera.moveCamera(Camera.CAMERA_MOVEIN, 50);
				isZoomAdjusted = true;
			}
			if (!isZoomAdjusted && camera != null && mGLView.getMeasuredWidth() > mGLView.getMeasuredHeight()) {
				camera.moveCamera(Camera.CAMERA_MOVEOUT, 50);
				isZoomAdjusted = true;
			}

			if (mGLView.getMeasuredHeight() > mGLView.getMeasuredWidth()) {
				CurrentOrientation = Orientation.PORTRAIT;
			} else {
				CurrentOrientation = Orientation.LANDSCAPE;
			}

			BTN_OBJ_TOP_LEFT_X = BTN_PADDING;
			BTN_OBJ_TOP_LEFT_Y = mGLView.getMeasuredHeight() - BTN_PADDING - BTN_OBJ_HEIGHT;
			if (CurrentOrientation == Orientation.PORTRAIT) {
				BTN_OBJ_BOTTOM_RIGHT_X = mGLView.getMeasuredWidth() - BTN_PADDING;
				BTN_OBJ_BOTTOM_RIGHT_Y = mGLView.getMeasuredHeight() - BTN_PADDING;
			} else {
				BTN_OBJ_BOTTOM_RIGHT_X = BTN_OBJ_TOP_LEFT_X + BTN_OBJ_WIDTH;
				BTN_OBJ_BOTTOM_RIGHT_Y = BTN_OBJ_TOP_LEFT_Y + BTN_OBJ_HEIGHT;
			}

			BTN_OBJ_BORDER_TOP_LEFT_X = BTN_OBJ_TOP_LEFT_X - BTN_BORDER_PADDING;
			BTN_OBJ_BORDER_TOP_LEFT_Y = BTN_OBJ_TOP_LEFT_Y - BTN_BORDER_PADDING;
			BTN_OBJ_BORDER_BOTTOM_RIGHT_X = BTN_OBJ_BOTTOM_RIGHT_X + BTN_BORDER_PADDING;
			BTN_OBJ_BORDER_BOTTOM_RIGHT_Y = BTN_OBJ_BOTTOM_RIGHT_Y + BTN_BORDER_PADDING;

			if (CurrentOrientation == Orientation.PORTRAIT) {
				BTN_WATER_TOP_LEFT_X = BTN_PADDING;
				BTN_WATER_TOP_LEFT_Y = mGLView.getMeasuredHeight() - BTN_PADDING - BTN_WATER_HEIGHT  - BTN_PADDING - BTN_OBJ_HEIGHT;
			} else {
				BTN_WATER_TOP_LEFT_X = mGLView.getMeasuredWidth() - BTN_WATER_WIDTH - BTN_PADDING;
				BTN_WATER_TOP_LEFT_Y = mGLView.getMeasuredHeight() - BTN_PADDING - BTN_WATER_HEIGHT;
			}
			BTN_WATER_BOTTOM_RIGHT_X = BTN_WATER_TOP_LEFT_X + BTN_WATER_WIDTH;
			BTN_WATER_BOTTOM_RIGHT_Y = BTN_WATER_TOP_LEFT_Y + BTN_WATER_HEIGHT;

			BTN_WATER_BORDER_TOP_LEFT_X = BTN_WATER_TOP_LEFT_X - BTN_BORDER_PADDING; 
			BTN_WATER_BORDER_TOP_LEFT_Y = BTN_WATER_TOP_LEFT_Y - BTN_BORDER_PADDING;
			BTN_WATER_BORDER_BOTTOM_RIGHT_X = BTN_WATER_BOTTOM_RIGHT_X + BTN_BORDER_PADDING;
			BTN_WATER_BORDER_BOTTOM_RIGHT_Y = BTN_WATER_BOTTOM_RIGHT_Y + BTN_BORDER_PADDING;

			INFO_PANEL_TOP_LEFT_X = BTN_PADDING - BTN_BORDER_PADDING - BTN_BORDER_PADDING;
			INFO_PANEL_TOP_LEFT_Y = BTN_PADDING - BTN_BORDER_PADDING;
			INFO_PANEL_BOTTOM_RIGHT_X = INFO_PANEL_TOP_LEFT_X + INFO_WIDTH;
			INFO_PANEL_BOTTOM_RIGHT_Y = INFO_PANEL_TOP_LEFT_Y + INFO_HEIGHT;

			if (master == null) {

				world = new World();
				world.setAmbientLight(150, 150, 150);

				sun = new Light(world);
				sun.setIntensity(150, 150, 150);

				hiddenObj = Primitives.getBox(10, (float) 0.1);
				hiddenObj.strip();
				hiddenObj.build();
				hiddenObj.rotateX(-0.8f);

				///CUT FOMR HERE - MOVED UP FOR GAME START PROCESS...

				camera = world.getCamera();
				if (mGLView.getMeasuredHeight() > mGLView.getMeasuredWidth()) {
					camera.moveCamera(Camera.CAMERA_MOVEOUT, 50);
					Logger.log("orientation: portrait!");
					isZoomAdjusted = true;
				}
				if (mGLView.getMeasuredWidth() > mGLView.getMeasuredHeight()) {
					camera.moveCamera(Camera.CAMERA_MOVEOUT, 100);
					Logger.log("orientation: landscape!");
					isZoomAdjusted = true;
				}

				SimpleVector sv = new SimpleVector();
				sv.set(hiddenObj.getTransformedCenter());
				sv.y -= 40;
				sv.z -= 40;
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = GameUI3D.this;
				}
			}

			if (dimensionsAvailable && !gardenConstructed) {
				Logger.log("Building the 3D view of the garden...");

				tube = Primitives.getPlane(3, 5);
				tube.setName("Plant: 1");
				tube.rotateY(-0.7f);
				tube.setTexture("plant1");
				tube.setTransparency(50);
				tube.strip();
				tube.build();

				int cols = game.getNumPlotCols();
				int rows = game.getNumPlotRows();
				
				float colOffset = 0 - ((((float) cols/2)+(float)1.5) * SEPERATION_DISTANCE);
				float rowOffset = 0 - ((((float) rows/2)+(float)1.5) * SEPERATION_DISTANCE);
				Logger.log("colOffset=" + colOffset + ", rowOffset=" + rowOffset);

				objCollection = new Object3D[cols * rows];

				int plotBeingCreatedID = 0;
				// outer for loop
				for (int rowCounter = 1; rowCounter <= rows; rowCounter++) {

					// inner for loop
					for (int colCounter = 1; colCounter <= cols; colCounter++) {

						plotBeingCreatedID = (((rowCounter-1) * cols) + colCounter);
						Object3D newPlot;
						newPlot = Primitives.getBox(5,0.2f);

						newPlot.setOrigin(new SimpleVector(colOffset + (SEPERATION_DISTANCE*2*(colCounter-1)),0,rowOffset + (SEPERATION_DISTANCE*2*(rowCounter-1))));
						newPlot.setName(PLOT_NAME_PREFIX + plotBeingCreatedID);

						newPlot.calcTextureWrapSpherical();

						switch (game.getPlotFrom1BasedID(plotBeingCreatedID).getGroundState()) {
						case WATER:
							newPlot.setTexture("plot_water");
							break;
						case MUD:
							newPlot.setTexture("plot_mud");
							break;
						case SAND:
							newPlot.setTexture("plot_sand");
							break;
						case SOIL:
							newPlot.setTexture("plot_soil");
							break;
						case GRAVEL:
							newPlot.setTexture("plot_gravel");
							break;
						}

						newPlot.strip();
						newPlot.build();
						newPlot.rotateY(FORTY_FIVE_DEGREES);
						newPlot.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
						hiddenObj.addChild(newPlot);
						world.addObject(newPlot);

						objCollection[plotBeingCreatedID-1] = newPlot;
						Logger.log("newPlot ID=" + newPlot.getID() + ", newPlot name=" + newPlot.getName());
					}
				}

				int id = 4;
				objCollection[id].addChild(tube); 
				tube.setOrigin(new SimpleVector(objCollection[id].getOrigin().x, objCollection[id].getOrigin().y - 8, objCollection[id].getOrigin().z));
				world.addObject(tube);

				tube2 = tube.cloneObject();
				tube2.setTexture("test plant 2");
				tube2.setName("Plant: 2");
				tube2.setScale(0.1f);
				int id2 = 15;
				objCollection[id2].addChild(tube2); 
				origTubePos = new SimpleVector(objCollection[id2].getOrigin().x, objCollection[id2].getOrigin().y - 1, objCollection[id2].getOrigin().z);
				tube2.setOrigin(origTubePos);
				world.addObject(tube2);

				overlayClockMarker = new Overlay(world, mGLView.getMeasuredWidth() - CLOCK_SIZE - CLOCK_PADDING, CLOCK_PADDING, mGLView.getMeasuredWidth() - CLOCK_PADDING, CLOCK_PADDING + CLOCK_SIZE, "clock_marker");
				overlayClockMarker.setDepth(1);
				overlayClockMarker.setTransparency(50);

				overlayYearClock = new Overlay(world, mGLView.getMeasuredWidth() - CLOCK_SIZE - CLOCK_PADDING, CLOCK_PADDING, mGLView.getMeasuredWidth() - CLOCK_PADDING, CLOCK_PADDING + CLOCK_SIZE, "year_clock_base");
				overlayYearClock.setDepth(100);
				overlayYearClock.setTransparency(50);

				overlayButtonObjectivesBorder = new Overlay(world, BTN_OBJ_BORDER_TOP_LEFT_X, BTN_OBJ_BORDER_TOP_LEFT_Y, BTN_OBJ_BORDER_BOTTOM_RIGHT_X, BTN_OBJ_BORDER_BOTTOM_RIGHT_Y, "button_border");
				overlayButtonObjectivesBorder.setDepth(8);
				overlayButtonObjectives = new Overlay(world, BTN_OBJ_TOP_LEFT_X, BTN_OBJ_TOP_LEFT_Y, BTN_OBJ_BOTTOM_RIGHT_X, BTN_OBJ_BOTTOM_RIGHT_Y, "button_back");
				overlayButtonObjectives.setDepth(6);

				overlayButtonWater = new Overlay(world,BTN_WATER_TOP_LEFT_X, BTN_WATER_TOP_LEFT_Y, BTN_WATER_BOTTOM_RIGHT_X, BTN_WATER_BOTTOM_RIGHT_Y, "water_off");
				overlayButtonWater.setDepth(10);
				overlayButtonWaterBorder = new Overlay(world, BTN_WATER_BORDER_TOP_LEFT_X, BTN_WATER_BORDER_TOP_LEFT_Y, BTN_WATER_BORDER_BOTTOM_RIGHT_X, BTN_WATER_BORDER_BOTTOM_RIGHT_Y, "button_border");
				overlayButtonWaterBorder.setDepth(50);

				overlayInfoBack = new Overlay(world, INFO_PANEL_TOP_LEFT_X, INFO_PANEL_TOP_LEFT_Y, INFO_PANEL_BOTTOM_RIGHT_X, INFO_PANEL_BOTTOM_RIGHT_Y, "button_border");
				overlayButtonWater.setDepth(14);

				gardenConstructed = true; // As we don't want to keep redoing this...
			}

			Logger.log("mGLView dimensions: height=" + mGLView.getMeasuredHeight() + ", width=" + mGLView.getMeasuredWidth());
			if (overlayClockMarker != null) { overlayClockMarker.setNewCoordinates(mGLView.getMeasuredWidth() - CLOCK_SIZE - CLOCK_PADDING, CLOCK_PADDING, mGLView.getMeasuredWidth() - CLOCK_PADDING, CLOCK_PADDING + CLOCK_SIZE); }
			if (overlayYearClock != null) { overlayYearClock.setNewCoordinates(mGLView.getMeasuredWidth() - CLOCK_SIZE - CLOCK_PADDING, CLOCK_PADDING, mGLView.getMeasuredWidth() - CLOCK_PADDING, CLOCK_PADDING + CLOCK_SIZE); }
			if (overlayButtonObjectives != null) { overlayButtonObjectives.setNewCoordinates(BTN_OBJ_TOP_LEFT_X, BTN_OBJ_TOP_LEFT_Y, BTN_OBJ_BOTTOM_RIGHT_X, BTN_OBJ_BOTTOM_RIGHT_Y); }
			if (overlayButtonObjectivesBorder != null) { overlayButtonObjectivesBorder.setNewCoordinates(BTN_OBJ_BORDER_TOP_LEFT_X, BTN_OBJ_BORDER_TOP_LEFT_Y, BTN_OBJ_BORDER_BOTTOM_RIGHT_X, BTN_OBJ_BORDER_BOTTOM_RIGHT_Y); }
			if (overlayButtonWaterBorder != null) { overlayButtonWaterBorder.setNewCoordinates(BTN_WATER_BORDER_TOP_LEFT_X, BTN_WATER_BORDER_TOP_LEFT_Y, BTN_WATER_BORDER_BOTTOM_RIGHT_X, BTN_WATER_BORDER_BOTTOM_RIGHT_Y); }
			if (overlayButtonWater != null) { overlayButtonWater.setNewCoordinates(BTN_WATER_TOP_LEFT_X, BTN_WATER_TOP_LEFT_Y, BTN_WATER_BOTTOM_RIGHT_X, BTN_WATER_BOTTOM_RIGHT_Y); }
			if (overlayInfoBack != null) { overlayInfoBack.setNewCoordinates(INFO_PANEL_TOP_LEFT_X, INFO_PANEL_TOP_LEFT_Y, INFO_PANEL_BOTTOM_RIGHT_X, INFO_PANEL_BOTTOM_RIGHT_Y); }

		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTypeface(Typeface.create((String)null, Typeface.BOLD));

			paint.setTextSize(48);
			glfTextLabel = new GLFont(paint, GLFont.SPECIAL);		
			glfToggleWater = new GLFont(paint, GLFont.SPECIAL);			

			if (!texturesCreated) {
				// Create a texture out of the icon...:-)
				Texture plotSoil = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_soil)), 64, 64));
				TextureManager.getInstance().addTexture("plot_soil", plotSoil);

				Texture plotWater = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_water)), 64, 64));
				TextureManager.getInstance().addTexture("plot_water", plotWater);

				Texture plotMud = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_mud)), 64, 64));
				TextureManager.getInstance().addTexture("plot_mud", plotMud);

				Texture plotSand = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_sand)), 64, 64));
				TextureManager.getInstance().addTexture("plot_sand", plotSand);

				Texture plotGravel = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_gravel)), 64, 64));
				TextureManager.getInstance().addTexture("plot_gravel", plotGravel);

				Texture plotHighlight = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.plot_highlight)), 64, 64));
				TextureManager.getInstance().addTexture("plot_highlight", plotHighlight);

				InputStream isRawTexture = getResources().openRawResource(R.raw.test_plant);
				Texture tpOne = new Texture(isRawTexture);  // with alpha
				TextureManager.getInstance().addTexture("plant1", tpOne);

				isRawTexture = getResources().openRawResource(R.raw.test_plant2);
				Texture tpTwo = new Texture(isRawTexture);  // with alpha
				TextureManager.getInstance().addTexture("test plant 2", tpTwo);

				isRawTexture = getResources().openRawResource(R.raw.year_clock_base);
				Texture yearClockBase = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("year_clock_base", yearClockBase);

				isRawTexture = getResources().openRawResource(R.raw.clock_marker);
				Texture clockMarkerTexture = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("clock_marker", clockMarkerTexture);

				isRawTexture = getResources().openRawResource(R.raw.button_background);
				Texture buttonBack = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("button_back", buttonBack);

				isRawTexture = getResources().openRawResource(R.raw.button_border);
				Texture buttonBorder = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("button_border", buttonBorder);

				isRawTexture = getResources().openRawResource(R.raw.toggle_water_on);
				Texture waterOn = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("water_on", waterOn);

				isRawTexture = getResources().openRawResource(R.raw.toggle_water_off);
				Texture waterOff = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("water_off", waterOff);

				isRawTexture = getResources().openRawResource(R.raw.toggle_water_disabled);
				Texture waterDisabled = new Texture(isRawTexture);
				TextureManager.getInstance().addTexture("water_disabled", waterDisabled);

				try {
					isRawTexture.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				texturesCreated = true;
			}
		}

		public void onDrawFrame(GL10 gl) {
			if (touchTurn != 0) {
				hiddenObj.rotateY(touchTurn);
				hiddenObj.rotateZ(-touchTurn);
				tube.rotateY(-(touchTurn * 1.4f));
				tube2.rotateY(-(touchTurn * 1.4f));
				touchTurn = 0;
			}

			if (touchTurnUp != 0) {
				touchTurnUp = 0;
			}

			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);

			if (blitCountDown <= 0) {
				if (dotCounter == dotCounterLimit) {
					dots="";
					dotCounter=0;
				}
				dotCounter++;
				dots = dots + ".";
				blitCountDown = 500;
			} else {
				blitCountDown -= GRANULARITY;
			}			

			glfTextLabel.blitString(fb, uiDate, INFO_PADDING, INFO_PADDING + FONT_HEIGHT, 10, RGBColor.WHITE);
			glfTextLabel.blitString(fb, uiSeason, INFO_PADDING, INFO_PADDING + FONT_HEIGHT + FONT_HEIGHT, 10, RGBColor.WHITE);
			glfTextLabel.blitString(fb, uiTemperature, INFO_PADDING, INFO_PADDING + FONT_HEIGHT + FONT_HEIGHT + FONT_HEIGHT, 10, RGBColor.WHITE);
			glfTextLabel.blitString(fb, uiRainfall, INFO_PADDING, INFO_PADDING + FONT_HEIGHT + FONT_HEIGHT + FONT_HEIGHT + FONT_HEIGHT, 10, RGBColor.WHITE);

			glfTextLabel.blitString(fb, uiObjectives, BTN_OBJ_TOP_LEFT_X + FONT_PADDING, BTN_OBJ_BOTTOM_RIGHT_Y - FONT_PADDING, 10, RGBColor.BLACK);

			String strWaterMessage;
			if (inWateringMode) {
				strWaterMessage = "Watering ON";
				waterTextColor = RGBColor.BLACK;
			} else {
				strWaterMessage = "Watering OFF";
			}
			glfToggleWater.blitString(fb, strWaterMessage, BTN_WATER_TOP_LEFT_X + FONT_PADDING, BTN_WATER_BOTTOM_RIGHT_Y - FONT_PADDING, 10, RGBColor.BLACK);

			if (CurrentOrientation == Orientation.PORTRAIT) {
				glfTextLabel.blitString(fb, uiWaterAllowance, BTN_WATER_TOP_LEFT_X + FONT_PADDING + BTN_WATER_WIDTH, BTN_WATER_BOTTOM_RIGHT_Y - FONT_PADDING, 10, waterAllowanceTextColor);
			} else {
				glfTextLabel.blitString(fb, uiWaterAllowance, BTN_WATER_TOP_LEFT_X - INFO_WIDTH, BTN_WATER_BOTTOM_RIGHT_Y - FONT_PADDING, 10, waterAllowanceTextColor);
			}

			fb.display();

			if (System.currentTimeMillis() - time >= 1000) {
				//Logger.log(fps + "fps, hO.Y=" + hiddenObj.getYAxis().y + ", hO.X=" + hiddenObj.getXAxis().x + ", hO.Z=" + hiddenObj.getZAxis().z + ", tT=" + tT + " tTU=" + tTU);
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
	}

	//	private void tickTock() {
	//		dayCounter++;
	//		if (dayCounter == DAY_LIMIT) { dayCounter = 1; }
	//
	//		if (overlayClockMarker != null) { overlayClockMarker.setRotation(-ONE_DEGREE * dayCounter);	}
	//
	//		if (tube2 != null) {
	//			SimpleVector newPos = tube2.getOrigin();
	//
	//			if (tube2.getScale() <= lowerLimit) {
	//				growing = true;
	//				tube2.setOrigin(origTubePos);
	//				tube2.setScale(0.1f);
	//				objCollection[3].setTexture("plot_soil");
	//			} else if (tube2.getScale() >= upperLimit){
	//				growing = false;
	//				objCollection[3].setTexture("plot_water");
	//			}
	//
	//			if (growing) {
	//				tube2.scale(1.01f);
	//				tube2.setOrigin(new SimpleVector(newPos.x, newPos.y - growthAmount, newPos.z));
	//			} else {
	//				tube2.scale(0.99f);
	//				tube2.setOrigin(new SimpleVector(newPos.x, newPos.y + growthAmount, newPos.z));
	//			}
	//		}
	//	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (plotSelected != NULL_INT) {
			//Toast.makeText(this, "Plot in menu=" + plotSelected, Toast.LENGTH_SHORT).show();
			plotSelectedForMenu = plotSelected;
			getMenuInflater().inflate(R.menu.plot_selected_context_menu, menu);
			if (game.getPlotFrom1BasedID(plotSelected).getPlant() == null) {
				menu.findItem(R.id.cmiUproot).setEnabled(false);
				SubMenu submenu = game.getSubMenuPlantTypes(menu.findItem(R.id.cmiPlantSeed));
			} else {
				menu.findItem(R.id.cmiPlantSeed).setEnabled(false);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//Toast.makeText(this, "Plot to work with=" + plotSelected, Toast.LENGTH_SHORT).show();
		switch (item.getItemId()) {
		case R.id.cmiPlantSeed:
			// Already handled with the sub menu...
			return true;
		case R.id.cmiUproot:
			String plantType = game.getPlotFrom1BasedID(plotSelected).getPlant().getType();
			game.uprootPlant(plotSelectedForMenu);
			Toast.makeText(GameUI3D.this, "Uprooted " + plantType, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.cmiDetails:
			//Set variable which will either be plant TYPE ID of 0 for empty plot, or plant INSTANCE ID if there is a plant there...
			Dialog dialogPlot;
			int plantID = 0;
			if (game.getPlotFrom1BasedID(plotSelectedForMenu).getPlant() != null) {
				plantID = game.getPlotFrom1BasedID(plotSelectedForMenu).getPlant().getId();
				dialogPlot = buildPlotDetailsDialog(plotSelectedForMenu, plantID, PLANT_DIALOG_TYPE.PLANT_INSTANCE);
			} else {
				dialogPlot = buildPlotDetailsDialog(plotSelectedForMenu, plantID, PLANT_DIALOG_TYPE.NONE);
			}

			//now that the dialog is set up, it's time to show it
			dialogPlot.show();

			return true;
		default:
			if (item.getGroupId() == Constants.MENU_GROUP_PLANT_TYPES) {
				//Toast.makeText(GameUI3D.this, "Touched menu item with id: " + item.getItemId(), Toast.LENGTH_SHORT).show();

				Log.w(GameUI3D.class.getName(), "Building " + plotSelectedForMenu);
				Dialog dialogPlant = buildPlantTypeDetailsDialog(item.getItemId() - Constants.PLANT_TYPE_MENU_ID_START_RANGE, plotSelectedForMenu, PLANT_DIALOG_TYPE.PLANT_TYPE);
				//now that the dialog is set up, it's time to show it  
				dialogPlant.show();

				return true;
			}

			return super.onContextItemSelected(item);
		}
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
		textPlot.setText("Touched cell - details:\n" + game.getPlotBasicFullPlotDetails(plotID));

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

		plotSelected = NULL_INT;
		return dialog;
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
					dialog.cancel();
					game.plantSeed(plantToLinkTo, plotToLinkTo);
					Toast.makeText(GameUI3D.this, "Planted " + game.getPlantTypeByPlantTypeID(plantToLinkTo).getType(), Toast.LENGTH_SHORT).show();
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

	private String getTextureFromGroundState(int plotID) {
		String textureName = "";
		switch (game.getPlotFrom1BasedID(plotID).getGroundState()) {
		case WATER:
			textureName = "plot_water";
			break;
		case GRAVEL:
			textureName = "plot_gravel";
			break;
		case MUD:
			textureName = "plot_mud";
			break;
		case SAND:
			textureName = "plot_sand";
			break;
		case SOIL:
			textureName = "plot_soil";
			break;
		}
		return textureName;
	}
}
