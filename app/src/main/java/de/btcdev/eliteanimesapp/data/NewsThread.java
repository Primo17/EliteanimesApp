package de.btcdev.eliteanimesapp.data;

import android.content.Context;

public class NewsThread extends Thread {

	@SuppressWarnings("unused")
	private Context context;
	private EAParser eaParser;
	private Netzwerk netzwerk;

	public NewsThread(Context context) {
		this.context = context;
		netzwerk = Netzwerk.instance(context);
		eaParser = new EAParser(null);
	}

	public void run() {
		try {
			eaParser.getNews(netzwerk.getNews());
		} catch (Exception e) {

		}
	}
}
