package com.tomhedges.bamboo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.SaveGame;

import android.util.Log;

/**
 * Helper class to serialize and save the key objects, then retrieve and deserialize them, and cast and return to the Game.
 * 
 * @see			Game
 * @author      Tom Hedges
 */

public class FileReaderAndWriter {

	public FileReaderAndWriter() {
		
	}

	public boolean saveObject(Object objectToSave, String filename) {
		Log.d(FileReaderAndWriter.class.getName(), "Saving object of type: " + objectToSave.getClass());
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(objectToSave);
			out.close();
			Log.d(FileReaderAndWriter.class.getName(), "Object saved!");
			return true;
		}
		catch(Exception ex)
		{
			Log.e(FileReaderAndWriter.class.getName(), "Problem saving object...", ex);
			return false;
		}
	}

	public SaveGame loadSavedGame(String filename) {
		SaveGame savedGame = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			savedGame = (SaveGame) in.readObject();
			in.close();
			Log.d(FileReaderAndWriter.class.getName(), "Successfully loaded saved game!");
		}
		catch(Exception ex)
		{
			Log.e(FileReaderAndWriter.class.getName(), "Problem loading saved game...", ex);
			return null;
		}
		
		return savedGame;
	}

	public boolean deleteFile(String string) {
		File file = new File(string);
		return file.delete();
	}
}
