package de.btcdev.eliteanimesapp.data;

import java.util.ArrayList;

/**
 * Klasse zur Verwaltung schon geladener Profile. Wegen besserer Effizienz
 * werden Profile gecacht. Hierzu werden fremde Profile in einer ArrayList
 * verwaltet, das eigene Profil in einer getrennten Variable.
 */
public class ProfilCache {

	private ArrayList<Profil> profilcache;
	private Profil eigenesProfil;
	private static ProfilCache unique = null;

	public static ProfilCache instance() {
		if (unique == null)
			unique = new ProfilCache();
		return unique;
	}

	/**
	 * Ein neuer ProfilCache wird erzeugt.
	 */
	private ProfilCache() {
		profilcache = new ArrayList<Profil>();
	}

	/**
	 * Gibt die ArrayList der fremden Profile zurück.
	 * 
	 * @return Cache fremder Profile
	 */
	public ArrayList<Profil> getProfilcache() {
		return profilcache;
	}

	/**
	 * Gibt das eigene Profil zurück.
	 * 
	 * @return eigenes Profil
	 */
	public Profil getEigenesProfil() {
		return eigenesProfil;
	}

	/**
	 * Setzt ein neues Profil als eigenes Profil.
	 * 
	 * @param eigenesProfil
	 *            Profil, das gesetzt werden soll
	 */
	public void setEigenesProfil(Profil eigenesProfil) {
		this.eigenesProfil = eigenesProfil;
	}

	/**
	 * überprüft, ob schon ein Profil mit dem übergebenen Benutzernamen
	 * existiert und gibt dieses gegebenenfalls zurück. Wenn nicht, wird ein
	 * neues Profil mit dem übergebenen Benutzernamen erzeugt und dem Cache
	 * hinzugefügt.
	 * 
	 * @param benutzername
	 *            Benutzername, nach dem im Cache gesucht werden soll
	 * @return Profil, das neu erstellt oder gefunden wurde
	 */
	public Profil contains(String benutzername) {
		Profil p = new Profil(benutzername);
		if (p.equals(eigenesProfil))
			return eigenesProfil;
		if (profilcache.contains(p)) {
			for (Profil temp : profilcache) {
				if (temp.equals(p))
					return temp;
			}
		}
		profilcache.add(p);
		return p;
	}

	public void deleteProfil(String benutzername) {
		Profil temp = new Profil(benutzername);
		if (profilcache.contains(temp))
			profilcache.remove(temp);
	}
}
