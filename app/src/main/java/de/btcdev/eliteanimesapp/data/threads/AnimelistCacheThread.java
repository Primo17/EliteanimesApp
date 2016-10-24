package de.btcdev.eliteanimesapp.data.threads;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.data.json.ListAnimeSerializer;
import de.btcdev.eliteanimesapp.data.models.ListAnime;

public class AnimelistCacheThread extends Thread {

	private Context context;
	private String userName;
	private SharedPreferences prefs;
	private ArrayList<ListAnime> complete, watching, stalled,
			dropped, planned;

	@SuppressWarnings("unchecked")
	public AnimelistCacheThread(String userName, Context context, ArrayList<ListAnime> complete,
								ArrayList<ListAnime> watching,
								ArrayList<ListAnime> stalled,
								ArrayList<ListAnime> dropped, ArrayList<ListAnime> planned) {
		this.context = context;
		this.userName = userName;
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.complete = (ArrayList<ListAnime>) complete.clone();
		this.watching = (ArrayList<ListAnime>) watching.clone();
		this.stalled = (ArrayList<ListAnime>) stalled.clone();
		this.dropped = (ArrayList<ListAnime>) dropped.clone();
		this.planned = (ArrayList<ListAnime>) planned.clone();
		start();
	}

	/**
	 * Der oben gesetzte Modus wird ausgeführt.
	 */
	public void run() {
		saveCache();
	}

	/**
	 * Speichert die aktuelle Animeliste im Speicher.
	 */
	public void saveCache() {
		// Konvertiere alle Listen zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(ListAnime.class, new ListAnimeSerializer())
				.setPrettyPrinting().create();
		String jsonComplete = gson.toJson(complete);
		String jsonWatching = gson.toJson(watching);
		String jsonStalled = gson.toJson(stalled);
		String jsonDropped = gson.toJson(dropped);
		String jsonPlanned = gson.toJson(planned);
		// speicher JSON-Repräsentation und aktuellen User
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", userName);
		editor.putBoolean("AnimelistCache", true);
		editor.putString("AnimelistCacheKomplett", jsonComplete);
		editor.putString("AnimelistCacheAmSchauen", jsonWatching);
		editor.putString("AnimelistCacheKurzAufgehoert", jsonStalled);
		editor.putString("AnimelistCacheAbgebrochen", jsonDropped);
		editor.putString("AnimelistCacheGeplant", jsonPlanned);
		editor.apply();
		complete = null;
		watching = null;
		stalled = null;
		dropped = null;
		planned = null;
	}
}
