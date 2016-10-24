package de.btcdev.eliteanimesapp.data.exceptions;

public class JsonErrorException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public JsonErrorException(String message){
		super(message);
	}
}
