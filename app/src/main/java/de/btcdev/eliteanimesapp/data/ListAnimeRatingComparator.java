package de.btcdev.eliteanimesapp.data;

import java.util.Comparator;

public class ListAnimeRatingComparator implements Comparator<ListAnime> {

	/**
	 * Sortiert die übergebenen ListAnime-Objekte nach Bewertung. Bei gleicher
	 * Bewertung wird falls vorhanden nach der Token-Id sortiert.
	 */
	@Override
	public int compare(ListAnime lhs, ListAnime rhs) {
		if (lhs.getTokenId() == null || lhs.getTokenId().isEmpty()) {
			return (-1)
					* Double.compare(lhs.getRating(), rhs.getRating());
		} else {
			int result = Double.compare(lhs.getRating(), rhs.getRating());
			if (result == 0) {
				return rhs.getTokenId().compareTo(lhs.getTokenId());
			} else {
				return (-1) * result;
			}
		}
	}

}
