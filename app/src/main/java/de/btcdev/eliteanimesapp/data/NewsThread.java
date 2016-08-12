package de.btcdev.eliteanimesapp.data;

import android.content.Context;

public class NewsThread extends Thread {

	@SuppressWarnings("unused")
	private Context context;
	private EAParser eaParser;
	private NetworkService networkService;

	public NewsThread(Context context) {
		this.context = context;
		networkService = NetworkService.instance(context);
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
