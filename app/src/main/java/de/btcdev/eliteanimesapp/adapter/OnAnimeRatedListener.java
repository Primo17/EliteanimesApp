package de.btcdev.eliteanimesapp.adapter;

import de.btcdev.eliteanimesapp.data.ListAnime;


public interface OnAnimeRatedListener {
	public abstract void onAnimeRatingComplete(ListAnime anime, int status, int statusNeu);
}
