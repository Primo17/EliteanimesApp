package de.btcdev.eliteanimesapp.data;

import java.util.Comparator;

import de.btcdev.eliteanimesapp.data.models.ListAnime;

public class ListAnimeAlphabetComparator implements Comparator<ListAnime> {

	/**
	 * Sortiert die übergebenen ListAnime-Objekte alphabetisch.
	 */
	@Override
	public int compare(ListAnime lhs, ListAnime rhs) {
		return lhs.getTitle().compareTo(rhs.getTitle());
	}

}
