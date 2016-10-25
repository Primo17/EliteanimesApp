package de.btcdev.eliteanimesapp.data.net;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;


public class QueryableCookieJarImpl implements QueryableCookieJar {

    private CookieCache cache;
    private PersistentCookieStore store;

    public QueryableCookieJarImpl(CookieCache cache, PersistentCookieStore store) {
        this.cache = cache;
        this.store = store;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
        store.saveAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> removedCookies = new ArrayList<>();
        List<Cookie> validCookies = new ArrayList<>();

        for(Cookie cookie: cache) {
            if (isCookieExpired(cookie)) {
                removedCookies.add(cookie);
            } else if (cookie.matches(url)) {
                validCookies.add(cookie);
            }
        }
        cache.removeAll(removedCookies);
        store.removeAll(removedCookies);
        return validCookies;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override
    public void clearSession() {
        cache.clear();
        cache.addAll(store.loadAll());
    }

    @Override
    synchronized public void clear() {
        cache.clear();
        store.clear();
    }

    @Override
    public boolean hasCookie(String name, String domain) {
        return cache.hasCookie(name, domain);
    }

    @Override
    public Cookie getCookie(String name, String domain) {
        return cache.getCookie(name, domain);
    }
}
