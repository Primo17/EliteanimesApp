package de.btcdev.eliteanimesapp;

import de.btcdev.eliteanimesapp.data.EAParser;


public class Test {

	public static void main(String[] args) {
		String test = "{\"error\":\"Dies ist ein Fehler\"}";
//		String test = "hallo";
		EAParser parser = new EAParser(null);
		System.out.println(parser.isError(test));
	}

}
