package de.btcdev.eliteanimesapp.data;

import java.util.Comparator;

public class ListAnimeAlphabetComparator implements Comparator<ListAnime> {

	/**
	 * Sortiert die übergebenen ListAnime-Objekte alphabetisch.
	 */
	@Override
	public int compare(ListAnime lhs, ListAnime rhs) {
		return lhs.getTitle().compareTo(rhs.getTitle());
	}

}
