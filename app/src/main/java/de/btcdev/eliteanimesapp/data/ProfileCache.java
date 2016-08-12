package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

/**
 * Klasse zur Verwaltung schon geladener Profile. Wegen besserer Effizienz
 * werden Profile gecacht. Hierzu werden fremde Profile in einer ArrayList
 * verwaltet, das eigene Profile in einer getrennten Variable.
 */
public class ProfileCache {

	private ArrayList<Profile> profilcache;
	private Profile eigenesProfile;
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
		profilcache = new ArrayList<>();
	}

	/**
	 * Gibt die ArrayList der fremden Profile zurück.
	 * 
	 * @return Cache fremder Profile
	 */
	public ArrayList<Profile> getProfilcache() {
		return profilcache;
	}

	/**
	 * Gibt das eigene Profile zurück.
	 * 
	 * @return eigenes Profile
	 */
	public Profile getEigenesProfile() {
		return eigenesProfile;
	}

	/**
	 * Setzt ein neues Profile als eigenes Profile.
	 * 
	 * @param eigenesProfile
	 *            Profile, das gesetzt werden soll
	 */
	public void setEigenesProfile(Profile eigenesProfile) {
		this.eigenesProfile = eigenesProfile;
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
		if (p.equals(eigenesProfile))
			return eigenesProfile;
		if (profilcache.contains(p)) {
			for (Profile temp : profilcache) {
				if (temp.equals(p))
					return temp;
			}
		}
		profilcache.add(p);
		return p;
	}

	public void deleteProfil(String benutzername) {
		Profile temp = new Profile(benutzername);
		if (profilcache.contains(temp))
			profilcache.remove(temp);
	}
}
