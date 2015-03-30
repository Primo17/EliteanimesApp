package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.PN;
import de.btcdev.eliteanimesapp.json.PNSerializer;

public class PNCacheThread extends Thread {

	private Context context;
	private SharedPreferences prefs;
	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	private int mode;
	private ArrayList<PN> pnlist;

	@SuppressWarnings("unchecked")
	public PNCacheThread(int mode, ArrayList<PN> pnlist) {
		context = Konfiguration.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		this.pnlist = (ArrayList<PN>) pnlist.clone();
		start();
	}

	public void run() {
		if (mode == MODE_LOAD_CACHE) {
			//derzeit nicht mehr implementiert
		} else if (mode == MODE_SAVE_CACHE) {
			saveCache();
		}

	}

	/**
	 * Konvertiert den aktuell in der Konfiguration gespeicherten PN-Cache zu
	 * einem JSON-Objekt und speichert dieses.
	 */
	public void saveCache() {
		// Konvertiere zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(PN.class, new PNSerializer())
				.setPrettyPrinting().create();
		String json = gson.toJson(pnlist);
		// speicher JSON-Repräsentation und aktuellen Benutzer
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", Konfiguration.getBenutzername(context));
		editor.putString("PNCache", json);
		editor.apply();
	}

}
