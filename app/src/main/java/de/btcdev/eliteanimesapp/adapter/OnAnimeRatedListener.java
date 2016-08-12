package de.btcdev.eliteanimesapp.adapter;

import de.btcdev.eliteanimesapp.data.ListAnime;


public interface OnAnimeRatedListener {
	void onAnimeRatingComplete(ListAnime anime, int oldStatus, int newStatus);

}
