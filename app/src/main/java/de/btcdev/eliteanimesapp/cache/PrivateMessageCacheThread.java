package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.btcdev.eliteanimesapp.data.Configuration;
import de.btcdev.eliteanimesapp.data.PrivateMessage;
import de.btcdev.eliteanimesapp.json.PrivateMessageSerializer;

public class PrivateMessageCacheThread extends Thread {

	private Context context;
	private SharedPreferences prefs;
	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	private int mode;
	private ArrayList<PrivateMessage> privateMessages;

	@SuppressWarnings("unchecked")
	public PrivateMessageCacheThread(int mode, ArrayList<PrivateMessage> privateMessages) {
		context = Configuration.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		this.privateMessages = (ArrayList<PrivateMessage>) privateMessages.clone();
		start();
	}

	public void run() {
		/*if (mode == MODE_LOAD_CACHE) {
			//derzeit nicht mehr implementiert
		} else */
		if (mode == MODE_SAVE_CACHE) {
			saveCache();
		}

	}

	/**
	 * Konvertiert den aktuell in der Configuration gespeicherten PrivateMessage-Cache zu
	 * einem JSON-Objekt und speichert dieses.
	 */
	public void saveCache() {
		// Konvertiere zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(PrivateMessage.class, new PrivateMessageSerializer())
				.setPrettyPrinting().create();
		String json = gson.toJson(privateMessages);
		// speicher JSON-Repräsentation und aktuellen User
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", Configuration.getUserName(context));
		editor.putString("PNCache", json);
		editor.apply();
	}

}
