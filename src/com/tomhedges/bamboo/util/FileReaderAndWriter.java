package com.tomhedges.bamboo.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.tomhedges.bamboo.model.Game;

import android.content.Context;
import android.util.Log;

public class FileReaderAndWriter {
	private Context context;

	public FileReaderAndWriter(Context context) {
		this.context = context;
	}

	public void saveObject(Object objectToSave, String filename) {
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
		}
		catch(IOException ex)
		{
			Log.e(FileReaderAndWriter.class.getName(), "Problem saving object...", ex);
		}
	}

	public Game loadGame(String filename) {
		//Must be serialised object???
		Game game = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			game = (Game) in.readObject();
			in.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		
		return game;
	}
}
