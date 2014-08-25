package com.tomhedges.bamboo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.tomhedges.bamboo.config.Constants;
import com.tomhedges.bamboo.model.Game;
import com.tomhedges.bamboo.model.SaveGame;

import android.content.Context;
import android.util.Log;

public class FileReaderAndWriter {
	//private Context context;

	public FileReaderAndWriter() {//Context context) {
		//this.context = context;
	}

	public boolean saveObject(Object objectToSave, String filename) {
		Log.w(FileReaderAndWriter.class.getName(), "Saving object of type: " + objectToSave.getClass());
		//Must be serialised object???
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(objectToSave);
			out.close();
			Log.w(FileReaderAndWriter.class.getName(), "Object saved!");
			return true;
		}
		catch(Exception ex)
		{
			Log.e(FileReaderAndWriter.class.getName(), "Problem saving object...", ex);
			return false;
		}
	}

	public SaveGame loadSavedGame(String filename) {
		//Must be serialised object???
		SaveGame savedGame = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			savedGame = (SaveGame) in.readObject();
			in.close();
			Log.w(FileReaderAndWriter.class.getName(), "Successfully loaded saved game!");
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
