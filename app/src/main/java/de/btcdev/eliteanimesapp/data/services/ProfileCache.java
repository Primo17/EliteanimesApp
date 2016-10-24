package de.btcdev.eliteanimesapp.data.services;

import java.util.ArrayList;

import de.btcdev.eliteanimesapp.data.models.Profile;

/**
 * Klasse zur Verwaltung schon geladener Profile. Wegen besserer Effizienz
 * werden Profile gecacht. Hierzu werden fremde Profile in einer ArrayList
 * verwaltet, das eigene Profile in einer getrennten Variable.
 */
public class ProfileCache {

	private ArrayList<Profile> profileCache;
	private Profile ownProfile;
	private static ProfileCache unique = null;

	public static ProfileCache instance() {
		if (unique == null)
			unique = new ProfileCache();
		return unique;
	}

	/**
	 * Ein neuer ProfileCache wird erzeugt.
	 */
	private ProfileCache() {
		profileCache = new ArrayList<>();
	}

	/**
	 * Gibt die ArrayList der fremden Profile zurück.
	 * 
	 * @return Cache fremder Profile
	 */
	public ArrayList<Profile> getProfileCache() {
		return profileCache;
	}

	/**
	 * Gibt das eigene Profile zurück.
	 * 
	 * @return eigenes Profile
	 */
	public Profile getOwnProfile() {
		return ownProfile;
	}

	/**
	 * Setzt ein neues Profile als eigenes Profile.
	 * 
	 * @param ownProfile
	 *            Profile, das gesetzt werden soll
	 */
	public void setOwnProfile(Profile ownProfile) {
		this.ownProfile = ownProfile;
	}

	/**
	 * überprüft, ob schon ein Profile mit dem übergebenen Benutzernamen
	 * existiert und gibt dieses gegebenenfalls zurück. Wenn nicht, wird ein
	 * neues Profile mit dem übergebenen Benutzernamen erzeugt und dem Cache
	 * hinzugefügt.
	 * 
	 * @param benutzername
	 *            Benutzername, nach dem im Cache gesucht werden soll
	 * @return Profile, das neu erstellt oder gefunden wurde
	 */
	public Profile contains(String benutzername) {
		Profile p = new Profile(benutzername);
		if (p.equals(ownProfile))
			return ownProfile;
		if (profileCache.contains(p)) {
			for (Profile temp : profileCache) {
				if (temp.equals(p))
					return temp;
			}
		}
		profileCache.add(p);
		return p;
	}

	public void deleteProfil(String benutzername) {
		Profile temp = new Profile(benutzername);
		if (profileCache.contains(temp))
			profileCache.remove(temp);
	}
}
