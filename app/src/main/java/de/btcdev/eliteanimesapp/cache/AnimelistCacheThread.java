package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.ConfigurationService;
import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.ListAnime;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.json.ListAnimeSerializer;

public class AnimelistCacheThread extends Thread {

	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	public static final int MODE_GET_ONLINE = 3;
	private int mode;
	private Context context;
	private SharedPreferences prefs;
	private ArrayList<ListAnime> complete, watching, stalled,
			dropped, planned;

	@Inject
	ConfigurationService configurationService;
	@Inject
	NetworkService networkService;

	/**
	 * Lädt je nach Modus die Animeliste aus dem Speicher, speichert sie dorthin
	 * oder lädt die Animeliste aus dem Internet neu und setzt sie in der
	 * ConfigurationService.
	 * 
	 * @param mode
	 *            einer der drei oben genannten Modi
	 */
	public AnimelistCacheThread(int mode) {
		context = configurationService.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		start();
	}

	@SuppressWarnings("unchecked")
	public AnimelistCacheThread(int mode, ArrayList<ListAnime> complete,
								ArrayList<ListAnime> watching,
								ArrayList<ListAnime> stalled,
								ArrayList<ListAnime> dropped, ArrayList<ListAnime> planned) {
		context = configurationService.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
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
		switch (mode) {
		case MODE_LOAD_CACHE:
			//aktuell nicht mehr implementiert
			break;
		case MODE_SAVE_CACHE:
			saveCache();
			break;
		case MODE_GET_ONLINE:
			getOnline();
			break;
		}
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
		editor.putString("lastUser", configurationService.getUserName(context));
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

	/**
	 * Lädt die Animeliste des angemeldeten Benutzers aus dem Internet und
	 * speichert diese in der ConfigurationService.
	 */
	public void getOnline() {
		try {
			EAParser eaParser = new EAParser(context);
			String input = networkService.getAnimeList(
					configurationService.getUserName(context), configurationService.getUserID(context));
			complete = new ArrayList<ListAnime>();
			watching = new ArrayList<ListAnime>();
			stalled = new ArrayList<ListAnime>();
			dropped = new ArrayList<ListAnime>();
			planned = new ArrayList<ListAnime>();
			eaParser.getListAnime(input, complete, watching, stalled,
					dropped, planned, true);
			saveCache();
		} catch (EAException e) {
			System.out.println(e);
		}
	}
}
