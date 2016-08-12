package de.btcdev.eliteanimesapp.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CommentLruCache {

	private LruCache<String, Bitmap> avatarCache;
	private static CommentLruCache unique;
	private final int maxMemory;
	private final int cacheSize;

	private CommentLruCache() {
		maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		cacheSize = maxMemory / 8;
		

		avatarCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
			}
		};
		unique = this;
	}

	public static CommentLruCache instance() {
		if (unique == null)
			return new CommentLruCache();
		else
			return unique;
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        avatarCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return avatarCache.get(key);
	}
}
