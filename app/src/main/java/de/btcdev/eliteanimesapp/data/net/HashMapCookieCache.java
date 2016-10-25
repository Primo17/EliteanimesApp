package de.btcdev.eliteanimesapp.data.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.Cookie;

public class HashMapCookieCache implements CookieCache {

    private HashMap<String, Cookie> cookies;

    public HashMapCookieCache() {
        cookies = new HashMap<>();
    }

    @Override
    public void addAll(Collection<Cookie> newCookies) {
        for(Cookie cookie : newCookies) {
            cookies.put(getKey(cookie), cookie);
        }
    }

    @Override
    public void removeAll(Collection<Cookie> toRemove) {
        for(Cookie cookie : toRemove) {
            cookies.remove(getKey(cookie));
        }
    }

    @Override
    public void clear() {
        cookies.clear();
    }

    @Override
    public boolean hasCookie(String name, String domain) {
        return cookies.containsKey(getKey(name, domain));
    }

    @Override
    public Cookie getCookie(String name, String domain) {
        return cookies.get(getKey(name, domain));
    }

    @Override
    public Iterator<Cookie> iterator() {
        return new HashMapCookieCacheIterator();
    }

    private String getKey(Cookie cookie) {
        return getKey(cookie.name(), cookie.domain());
    }

    private String getKey(String name, String host) {
        return host + ":" + name;
    }

    private class HashMapCookieCacheIterator implements Iterator<Cookie> {

        private Iterator<Cookie> iterator;

        public HashMapCookieCacheIterator() {
            iterator = cookies.values().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
