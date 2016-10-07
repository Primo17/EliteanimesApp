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

	private SharedPreferences prefs;
	private ArrayList<Comment> comments;
	private String userName;

	public CommentCacheThread(Context context, ArrayList<Comment> comments, String userName) {
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
		this.comments = new ArrayList<>(comments);
		this.userName = userName;
		start();
	}

	public void run() {
		saveCache();
	}

	/**
	 * Konvertiert den aktuell in der ConfigurationService gespeicherten
	 * Comment-Cache zu einem JSON-Objekt und speichert dieses.
	 */
	private void saveCache() {
		// Konvertiere zu JSON
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Comment.class, new CommentSerializer())
				.setPrettyPrinting().create();
		String json = gson.toJson(comments);
		// speicher JSON-Repräsentation und aktuellen User
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastUser", userName);
		editor.putString("CommentCache", json);
		editor.apply();
	}

}
