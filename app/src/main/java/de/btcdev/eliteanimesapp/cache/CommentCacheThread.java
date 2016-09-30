package de.btcdev.eliteanimesapp.cache;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.data.Comment;
import de.btcdev.eliteanimesapp.data.ConfigurationService;
import de.btcdev.eliteanimesapp.json.CommentSerializer;

public class CommentCacheThread extends Thread {

	private Context context;
	private SharedPreferences prefs;
	private int mode;
	public static final int MODE_LOAD_CACHE = 1;
	public static final int MODE_SAVE_CACHE = 2;
	private ArrayList<Comment> comments;

	@Inject
	ConfigurationService configurationService;

	@SuppressWarnings("unchecked")
	public CommentCacheThread(int mode, ArrayList<Comment> comments) {
		context = configurationService.getContext();
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.mode = mode;
		this.comments = (ArrayList<Comment>) comments.clone();
		start();
	}

	public void run() {
		/*if (mode == MODE_LOAD_CACHE) {
			// aktuell nicht mehr implementiert
		} else */
		if (mode == MODE_SAVE_CACHE)
			saveCache();
	}

	/**
	 * Konvertiert den aktuell in der ConfigurationService gespeicherten
	 * Comment-Cache zu einem JSON-Objekt und speichert dieses.
	 */
	public void saveCache() {
		// Konvertiere zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Comment.class, new CommentSerializer())
				.setPrettyPrinting().create();
		String json = gson.toJson(comments);
		// speicher JSON-Repräsentation und aktuellen User
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", configurationService.getUserName(context));
		editor.putString("CommentCache", json);
		editor.apply();
	}

}
