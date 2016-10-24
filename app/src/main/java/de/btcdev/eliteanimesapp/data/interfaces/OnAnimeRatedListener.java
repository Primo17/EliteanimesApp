package de.btcdev.eliteanimesapp.data.interfaces;

import de.btcdev.eliteanimesapp.data.models.ListAnime;


public interface OnAnimeRatedListener {
	void onAnimeRatingComplete(ListAnime anime, int oldStatus, int newStatus);

}
