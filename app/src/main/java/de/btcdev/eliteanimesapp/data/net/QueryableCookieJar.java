package de.btcdev.eliteanimesapp.data.net;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public interface QueryableCookieJar extends CookieJar {

    @Override
    void saveFromResponse(HttpUrl url, List<Cookie> cookies);

    @Override
    List<Cookie> loadForRequest(HttpUrl url);

    void clearSession();

    void clear();

    boolean hasCookie(String name, String host);

    Cookie getCookie(String name, String host);
}
