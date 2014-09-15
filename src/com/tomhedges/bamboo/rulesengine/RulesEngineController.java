package com.tomhedges.bamboo.rulesengine;

import java.util.Collection;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.drools.core.android.DroolsAndroidContext;
import org.drools.core.common.DroolsObjectInputStream;
import org.kie.api.definition.rule.Rule;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.mvel2.optimizers.OptimizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.config.CoreSettings;
import com.tomhedges.bamboo.model.Game;

import android.content.Context;
import android.util.Log;

/**
 * Provides access to the rules engine - currently Drools. Using a mixture of current v6 objects and deprecated v5 objects
 * 
 * @see			Game
 * @author      Tom Hedges
 */

public class RulesEngineController {

	private static RulesEngineController rulesEngineController = null;
	private Context context;

	private static final Logger loggerForEngine = LoggerFactory.getLogger("DROOLS WITHIN ENGINE");
	private KnowledgeBase mKnowledgeBase;
	private StatefulKnowledgeSession ksession;
	private Collection<KnowledgePackage> pkgs;
	private KnowledgePackage pkgTest;
	private CoreSettings coreSettings;

	//Private Constructor
	private RulesEngineController(Context context){
		this.context = context;

		//Initialize android context and set system properties
		DroolsAndroidContext.setContext(context);
	}

	// Singleton Factory method
	public static RulesEngineController getInstance(Context context) {
		if(rulesEngineController == null){
			rulesEngineController = new RulesEngineController(context);
		}
		return rulesEngineController;
	}

	public void loadRules() {
		coreSettings = CoreSettings.accessCoreSettings();

		DroolsObjectInputStream dois = null;  
		try { 
			Log.d(RulesEngineController.class.getName(), "Loading knowledge base");

			System.setProperty("java.version", "1.6");
			System.setProperty("mvel2.disable.jit", "true");
			Log.d(RulesEngineController.class.getName(), "System: Value of property 'mvel2.disable.jit' is: " + System.getProperty("mvel2.disable.jit"));
			OptimizerFactory.setDefaultOptimizer("reflective");

			// SET to false for production, or true for testing new rules files from local copy!
			boolean useRaw = false;

			InputStream iis = null;
			File file = null;

			String filePath = coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + Constants.FILENAME_LOCAL_ITERATION_RULES;
			Log.d(RulesEngineController.class.getName(), "Checking for existence of iteration rules file: " + filePath);
			file = new File(filePath);

			if(file.exists() && !useRaw) {    
				Log.d(RulesEngineController.class.getName(), "Using downloaded iteration rules file");
				iis = new FileInputStream(file);
			} else {
				Log.d(RulesEngineController.class.getName(), "Using hardcoded iteration rules file");
				iis = context.getResources().openRawResource(R.raw.bambootestv2);
			}
			Log.d(RulesEngineController.class.getName(), "iis object is...: " + iis.toString());
			dois = new DroolsObjectInputStream(iis);
			pkgs = (Collection<KnowledgePackage>) dois.readObject();


			filePath = coreSettings.checkStringSetting(Constants.CORESETTING_LOCAL_FILEPATH) + Constants.FILENAME_LOCAL_OBJECTIVES;
			Log.d(RulesEngineController.class.getName(), "Checking for existence of objectives rule file: " + filePath);
			file = new File(filePath);
			if(file.exists() && !useRaw) {    
				Log.d(RulesEngineController.class.getName(), "Using downloaded objectives file");
				iis = new FileInputStream(file);
			} else {
				Log.d(RulesEngineController.class.getName(), "Using hardcoded objectives file");
				iis = context.getResources().openRawResource(R.raw.bambooobjectivesv1);
			}
			Log.d(RulesEngineController.class.getName(), "iis object is...: " + iis.toString());
			dois = new DroolsObjectInputStream(iis);
			pkgs.addAll((Collection<KnowledgePackage>) dois.readObject());

			Log.d(RulesEngineController.class.getName(), "Loaded rule packages: " + pkgs);
			for(KnowledgePackage pkg : pkgs) {
				Log.d(RulesEngineController.class.getName(), "Loaded rule package: " + pkg.toString());
				int counter = 0;
				for(Rule rule : pkg.getRules()) {
					Log.d(RulesEngineController.class.getName(), "Loaded Rule: " + rule.getName());
					counter++;
				}
				Log.d(RulesEngineController.class.getName(), "Package contains: " + counter + " rule(s)");
			}
			Log.d(RulesEngineController.class.getName(), "Loaded all rules from: " + pkgs.size() + " package(s)");

		}catch(Exception e) {
			Log.e(RulesEngineController.class.getName(), "Drools exception", e);
		} finally {
			if(dois!=null) {
				try {
					dois.close();
				} catch (IOException e) {
					Log.e(RulesEngineController.class.getName(), "Drools exception", e);
					e.printStackTrace();
				}
			}
		}
	}

	public void createRulesEngineSession(int temperature, int rainfall) {
		Log.d(RulesEngineController.class.getName(), "Creating Rules Engine Session");
		if (mKnowledgeBase == null) {
			mKnowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
			Log.d(RulesEngineController.class.getName(), "Adding rules...");
			mKnowledgeBase.addKnowledgePackages(pkgs);
		}
		ksession = mKnowledgeBase.newStatefulKnowledgeSession();

		Log.d(RulesEngineController.class.getName(), "Adding 'global' values...");
		ksession.setGlobal("$logger", loggerForEngine);
		ksession.setGlobal("$temperature", temperature);
		ksession.setGlobal("$rainfall", rainfall);
		Random random = new Random();
		ksession.setGlobal("$random", random);

		Log.d(RulesEngineController.class.getName(), "Rules Engine Session created!");
	}

	public void insertFact(Object toInsert) {
		ksession.insert(toInsert);
		Log.d(RulesEngineController.class.getName(), "Inserted '" + toInsert.getClass() + "' fact into Rules Engine");
	}

	public void fireRules() {
		Log.d(RulesEngineController.class.getName(), "Firing Rules!");
		try {
			ksession.fireAllRules();
		} catch (ExceptionInInitializerError e) {
			Log.e(RulesEngineController.class.getName(),"FAILED!!!", e);
		}
		Log.d(RulesEngineController.class.getName(), "Fired Rules!");

		resetRuleEngine();
	}

	private void resetRuleEngine() {
		ksession.dispose();
		Log.d(RulesEngineController.class.getName(), "Reset Rules Engine Session");
	}

	public void closedownRuleEngine() {
		if (ksession != null) {
			ksession.dispose();
			ksession.destroy();
			ksession = null;
		}
		mKnowledgeBase = null;
		Log.d(RulesEngineController.class.getName(), "Closing Rules Engine Session");
	}
}
