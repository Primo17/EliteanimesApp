package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.btcdev.eliteanimesapp.data.Kommentar;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.json.KommentarSerializer;

public class KommentarCacheThread extends Thread {

	private Context context;
	private SharedPreferences prefs;
	private int mode;
	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	private ArrayList<Kommentar> commentlist;

	@SuppressWarnings("unchecked")
	public KommentarCacheThread(int mode, ArrayList<Kommentar> commentlist) {
		context = Konfiguration.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		this.commentlist = (ArrayList<Kommentar>) commentlist.clone();
		start();
	}

	public void run() {
		if (mode == MODE_LOAD_CACHE) {
			// aktuell nicht mehr implementiert
		} else if (mode == MODE_SAVE_CACHE)
			saveCache();
	}

	/**
	 * Konvertiert den aktuell in der Konfiguration gespeicherten
	 * Kommentar-Cache zu einem JSON-Objekt und speichert dieses.
	 */
	public void saveCache() {
		// Konvertiere zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Kommentar.class, new KommentarSerializer())
				.setPrettyPrinting().create();
		String json = gson.toJson(commentlist);
		// speicher JSON-Repräsentation und aktuellen Benutzer
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", Konfiguration.getBenutzername(context));
		editor.putString("CommentCache", json);
		editor.apply();
	}

}
