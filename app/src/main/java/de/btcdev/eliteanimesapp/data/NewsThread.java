package de.btcdev.eliteanimesapp.data;

import android.content.Context;

import javax.inject.Inject;

public class NewsThread extends Thread {

	private EAParser eaParser;
	private NetworkService networkService;

	public static void getNews(NetworkService networkService) {
		new NewsThread(networkService).start();
	}

	private NewsThread(NetworkService networkService) {
		this.networkService = networkService;
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
