package de.btcdev.eliteanimesapp.data.models;

public class JsonError {

	private String error;
	
	public JsonError(){
		
	}
	
	public JsonError(String error){
		setError(error);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public String toString(){
		return error;
	}
}
