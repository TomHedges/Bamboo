package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.activities.GameUI3D;
import com.tomhedges.bamboo.activities.GameUI2DTextOriginal;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.Globals;
import com.tomhedges.bamboo.model.Objective;
import com.tomhedges.bamboo.model.PlantInstance;
import com.tomhedges.bamboo.model.PlantType;
import com.tomhedges.bamboo.util.ArrayAdapterObjectives;
import com.tomhedges.bamboo.util.ArrayAdapterUnlockedSeeds;
import com.tomhedges.bamboo.util.DateConverter;
import com.tomhedges.bamboo.util.dao.LocalDBDataRetrieval;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Provides the main menu for Bamboo, from which the player can take key actions and launch the game
 * 
 * @see			Game
 * @see			GameUI3D
 * @author      Tom Hedges
 */

public class LaunchFragment extends Fragment implements OnClickListener {

	private Button btnNewGame, btnLoadGame, btnNewGame3D, btnLoadGame3D, btnViewUnclockedSeeds, btnViewObjectives, btnResetObjectives, btnHelp, btnTestSeedUpload;
	private EditText etUsername;
	private Intent i;

	// to build local settings
	private LocalDBDataRetrieval localDataRetriever;

	// Storage for user preferences
	private CoreSettings coreSettings;

	//master...
	private Game game;

	private AlertDialog alert;
	private AlertDialog.Builder builder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.launch, container, false);  

		etUsername = (EditText)v.findViewById(R.id.username);

		//setup buttons
		btnNewGame = (Button)v.findViewById(R.id.launchStartNewGame2D);
		btnLoadGame = (Button)v.findViewById(R.id.launchContinueCurrentGame2D);
		btnNewGame3D = (Button)v.findViewById(R.id.launchStartNewGame3D);
		btnLoadGame3D = (Button)v.findViewById(R.id.launchContinueCurrentGame3D);
		btnViewUnclockedSeeds = (Button)v.findViewById(R.id.view_unlocked_sponsored_seeds);
		btnViewObjectives = (Button)v.findViewById(R.id.view_objective_completion);
		btnResetObjectives = (Button)v.findViewById(R.id.reset_objective_completion);
		btnHelp = (Button)v.findViewById(R.id.read_help);
		btnTestSeedUpload = (Button)v.findViewById(R.id.test_seed_upload);

		//register listeners
		btnNewGame.setOnClickListener(this);
		btnLoadGame.setOnClickListener(this);
		btnNewGame3D.setOnClickListener(this);
		btnLoadGame3D.setOnClickListener(this);
		btnViewUnclockedSeeds.setOnClickListener(this);
		btnViewObjectives.setOnClickListener(this);
		btnResetObjectives.setOnClickListener(this);
		btnHelp.setOnClickListener(this);
		btnTestSeedUpload.setOnClickListener(this);

		game = Game.getGameDetails(getActivity());
		localDataRetriever = new LocalDBDataRetrieval(getActivity());
		localDataRetriever.open();
		Globals localGlobals = localDataRetriever.getGlobals();
		etUsername.setText(localGlobals.getUsername());
		btnLoadGame.setEnabled(game.savedGameExists());
		btnLoadGame3D.setEnabled(game.savedGameExists());

		CoreSettings.createCoreSettings(getActivity());
		coreSettings = CoreSettings.accessCoreSettings();
		coreSettings.addSetting(Constants.ROOT_URL_FIELD_NAME, localGlobals.getRootURL());

		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.launchStartNewGame2D:
			game.setUsername(etUsername.getText().toString());
			i = new Intent(this.getActivity(), GameUI2DTextOriginal.class);
			game.startNewGame();
			startActivity(i);

			break;

		case R.id.launchContinueCurrentGame2D:
			i = new Intent(this.getActivity(), GameUI2DTextOriginal.class);
			game.continueExistingGame();
			startActivity(i);

			break;

		case R.id.launchStartNewGame3D:
			game.setUsername(etUsername.getText().toString());
			i = new Intent(this.getActivity(), GameUI3D.class);
			game.startNewGame();
			startActivity(i);
			break;

		case R.id.launchContinueCurrentGame3D:
			i = new Intent(this.getActivity(), GameUI3D.class);
			game.continueExistingGame();
			startActivity(i);
			break;

		case R.id.view_unlocked_sponsored_seeds:
			//based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
			// our adapter instance

			builder = new AlertDialog.Builder(getActivity());
			alert = builder.create();
			alert.setTitle("Unlocked Seeds");

			String[][] unlockedSeeds = game.getUnlockedSeedsList();
			if (unlockedSeeds.length == 0) {
				alert.setMessage("None unlocked yet!");
			} else {
				ArrayAdapterUnlockedSeeds adapterUnlockedSeeds = new ArrayAdapterUnlockedSeeds(getActivity(), R.layout.list_element_unlocked_seeds, game.getUnlockedSeedsList());

				// create a new ListView, set the adapter and item click listener
				ListView listViewItemsUnlockedSeeds = new ListView(getActivity());
				listViewItemsUnlockedSeeds.setAdapter(adapterUnlockedSeeds);

				// put the ListView in the pop up
				alert.setView(listViewItemsUnlockedSeeds);
			}
			
			alert.show();
			break;

		case R.id.view_objective_completion:
			//based on code from: http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
			// our adapter instance

			builder = new AlertDialog.Builder(getActivity());
			alert = builder.create();
			alert.setTitle("Objectives");
			
			Objective[] objs = game.getObjectiveList();
			if (objs == null || objs.length == 0) {
				alert.setMessage("None available yet!");
			} else {
				ArrayAdapterObjectives adapterObjectives = new ArrayAdapterObjectives(getActivity(), R.layout.list_element_objectives, game.getObjectiveList());

				// create a new ListView, set the adapter and item click listener
				ListView listViewItemsObjectives = new ListView(getActivity());
				listViewItemsObjectives.setAdapter(adapterObjectives);

				// put the ListView in the pop up
				alert.setView(listViewItemsObjectives);
			}
			
			alert.show();
			break;

		case R.id.reset_objective_completion:
			String message = null;
			localDataRetriever.open();
			if (localDataRetriever.resetObjectiveCompletionStatuses()) {
				message = "Reset all objectives - enjoy playing again!";
			} else {
				message = "Problem resetting objectives.";
			}
			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

			localDataRetriever.close();
			localDataRetriever = null;

			break;

		case R.id.read_help:
			builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View layout = inflater.inflate(R.layout.help_info_details, null);
			TextView helpText = (TextView)layout.findViewById(R.id.dia_help_text);
			localDataRetriever.open();
			String info = "<h1>Information</h1><p><b>Data Version:</b> " + localDataRetriever.getGlobals().getVersion() + "<br>" +
			"<b>Last Update timestamp:</b> " + new DateConverter().convertDateToString(localDataRetriever.getGlobals().getLast_updated()) + "<br>" +
			"<b>Current URL:</b> " + localDataRetriever.getGlobals().getRootURL() + "<br>" +
			"<b>Developed by:</b> Tom Hedges - Birkbeck College, University of London<br>" +
			"<b>Copyright:</b> Tom Hedges and Birkbeck College, University of London";
			localDataRetriever.close();
			helpText.setText(Html.fromHtml(info + game.getTextElement(Constants.HELPANDINFO_HELP_TYPE, Constants.HELPANDINFO_HELP_REF_MAIN), new ImageGetter(), null));
			builder.setPositiveButton("OK, let's play!", null);
			builder.setView(layout);
			alert = builder.create();
			alert.setTitle("Help and information");
			alert.show();

			break;

		case R.id.test_seed_upload:
			game.setUsername(etUsername.getText().toString());
			game.uploadSeed(new PlantInstance(new PlantType(4, "Carnation", 0, 0, 0.0, null, 0, 0, 0, 0, 0, 0, 0, null, null, null, null, null, null, null, 0, 0, 0), 0));
			Toast.makeText(getActivity(), "Upload of a Carnation seed has been attempted!", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	class ImageGetter implements Html.ImageGetter {

		public Drawable getDrawable(String source) {
			String path = coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + source;
			try {
				Drawable img = Drawable.createFromPath(path);
				img.setBounds(0,0,img.getIntrinsicWidth()*10,img.getIntrinsicHeight()*10);
				return img;
			} catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	public void onResume() {
		if (localDataRetriever != null) {
			localDataRetriever.open();
		}
		super.onResume();
		btnLoadGame.setEnabled(game.savedGameExists());
		btnLoadGame3D.setEnabled(game.savedGameExists());
	}

	@Override
	public void onPause() {
		super.onPause();

		game.setUsername(etUsername.getText().toString());

		if (localDataRetriever != null) {
			localDataRetriever.close();
		}

		if (alert != null && alert.isShowing()) {
			alert.dismiss();
		}
	}
}
