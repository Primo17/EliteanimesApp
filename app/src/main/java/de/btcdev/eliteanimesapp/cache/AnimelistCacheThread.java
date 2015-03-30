package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.btcdev.eliteanimesapp.data.EAException;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.Konfiguration;
import de.btcdev.eliteanimesapp.data.ListAnime;
import de.btcdev.eliteanimesapp.data.Netzwerk;
import de.btcdev.eliteanimesapp.json.ListAnimeSerializer;

public class AnimelistCacheThread extends Thread {

	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	public static final int MODE_GET_ONLINE = 3;
	private int mode;
	private Context context;
	private SharedPreferences prefs;
	private ArrayList<ListAnime> komplett, amSchauen, kurzAufgehoert,
			abgebrochen, geplant;

	/**
	 * Lädt je nach Modus die Animeliste aus dem Speicher, speichert sie dorthin
	 * oder lädt die Animeliste aus dem Internet neu und setzt sie in der
	 * Konfiguration.
	 * 
	 * @param mode
	 *            einer der drei oben genannten Modi
	 */
	public AnimelistCacheThread(int mode) {
		context = Konfiguration.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		start();
	}

	@SuppressWarnings("unchecked")
	public AnimelistCacheThread(int mode, ArrayList<ListAnime> komplett,
			ArrayList<ListAnime> amSchauen,
			ArrayList<ListAnime> kurzAufgehoert,
			ArrayList<ListAnime> abgebrochen, ArrayList<ListAnime> geplant) {
		context = Konfiguration.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		this.komplett = (ArrayList<ListAnime>) komplett.clone();
		this.amSchauen = (ArrayList<ListAnime>) amSchauen.clone();
		this.kurzAufgehoert = (ArrayList<ListAnime>) kurzAufgehoert.clone();
		this.abgebrochen = (ArrayList<ListAnime>) abgebrochen.clone();
		this.geplant = (ArrayList<ListAnime>) geplant.clone();
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
		String jsonKomplett = gson.toJson(komplett);
		String jsonAmSchauen = gson.toJson(amSchauen);
		String jsonKurzAufgehoert = gson.toJson(kurzAufgehoert);
		String jsonAbgebrochen = gson.toJson(abgebrochen);
		String jsonGeplant = gson.toJson(geplant);
		// speicher JSON-Repräsentation und aktuellen Benutzer
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", Konfiguration.getBenutzername(context));
		editor.putBoolean("AnimelistCache", true);
		editor.putString("AnimelistCacheKomplett", jsonKomplett);
		editor.putString("AnimelistCacheAmSchauen", jsonAmSchauen);
		editor.putString("AnimelistCacheKurzAufgehoert", jsonKurzAufgehoert);
		editor.putString("AnimelistCacheAbgebrochen", jsonAbgebrochen);
		editor.putString("AnimelistCacheGeplant", jsonGeplant);
		editor.apply();
		komplett = null;
		amSchauen = null;
		kurzAufgehoert = null;
		abgebrochen = null;
		geplant = null;
	}

	/**
	 * Lädt die Animeliste des angemeldeten Benutzers aus dem Internet und
	 * speichert diese in der Konfiguration.
	 */
	public void getOnline() {
		try {
			Netzwerk netzwerk = Netzwerk.instance(context);
			EAParser eaParser = new EAParser(context);
			String input = netzwerk.getAnimelist(
					Konfiguration.getBenutzername(context), Konfiguration.getUserID(context));
			komplett = new ArrayList<ListAnime>();
			amSchauen = new ArrayList<ListAnime>();
			kurzAufgehoert = new ArrayList<ListAnime>();
			abgebrochen = new ArrayList<ListAnime>();
			geplant = new ArrayList<ListAnime>();
			eaParser.getListAnimes(input, komplett, amSchauen, kurzAufgehoert,
					abgebrochen, geplant, true);
			saveCache();
		} catch (EAException e) {
			System.out.println(e);
		}
	}
}
