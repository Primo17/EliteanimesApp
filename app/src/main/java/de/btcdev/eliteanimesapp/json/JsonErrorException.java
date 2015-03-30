package de.btcdev.eliteanimesapp.json;

public class JsonErrorException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public JsonErrorException(String message){
		super(message);
	}
}
