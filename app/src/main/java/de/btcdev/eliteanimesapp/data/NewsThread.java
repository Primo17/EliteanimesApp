package de.btcdev.eliteanimesapp.data;

import de.btcdev.eliteanimesapp.services.NotificationService;

public class NewsThread extends Thread {

	private NotificationService notificationService;

	public static void getNews(NotificationService notificationService) {
		new NewsThread(notificationService).start();
	}

	private NewsThread(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void run() {
		try {
			notificationService.getNotifications();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
