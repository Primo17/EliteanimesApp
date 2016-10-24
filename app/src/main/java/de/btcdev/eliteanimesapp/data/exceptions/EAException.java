package de.btcdev.eliteanimesapp.data.exceptions;

/**
 * Einheitliche Exception-Klasse für alle geworfenen Exceptions. Gibt einen
 * spezifischen Fehlertext an.
 */
public class EAException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Erzeugt eine neue EAException.
	 * @param message spezifischer Fehlertext
	 */
	public EAException(String message) {
		super(message);
	}

}
