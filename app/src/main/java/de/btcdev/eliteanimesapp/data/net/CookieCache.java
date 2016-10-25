package de.btcdev.eliteanimesapp.data.net;

import java.util.Collection;
import java.util.Iterator;

import okhttp3.Cookie;

interface CookieCache extends Iterable<Cookie> {

    void addAll(Collection<Cookie> newCookies);

    void removeAll(Collection<Cookie> toRemove);

    void clear();

    Iterator<Cookie> iterator();

    boolean hasCookie(String name, String domain);

    Cookie getCookie(String name, String domain);
}
