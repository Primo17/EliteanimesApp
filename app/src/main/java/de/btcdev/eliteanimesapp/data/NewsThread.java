package de.btcdev.eliteanimesapp.data;

import android.content.Context;

import javax.inject.Inject;

public class NewsThread extends Thread {

	private EAParser eaParser;
	@Inject
	NetworkService networkService;

	public NewsThread(Context context) {
		eaParser = new EAParser(null);
	}

	public void run() {
		try {
			eaParser.getNotifications(networkService.getNews());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
