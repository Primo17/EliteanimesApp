package de.btcdev.eliteanimesapp.data.net;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

public class SharedPreferencesCookieStore implements PersistentCookieStore {

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesCookieStore(Context context) {
        sharedPreferences = context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE);
    }

    @Override
    public List<Cookie> loadAll() {
        List<Cookie> cookies = new ArrayList<>(sharedPreferences.getAll().size());
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            String encodedCookie = (String) entry.getValue();
            SerializableCookie cookie = new SerializableCookie(encodedCookie);
            cookies.add(cookie.getCookie());
        }
        return cookies;
    }

    @Override
    public void saveAll(Collection<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            if (cookie.persistent()) {
                SerializableCookie serializable = new SerializableCookie(cookie);
                editor.putString(createKey(cookie), serializable.getEncodedCookie());
            }
        }
        editor.apply();
    }

    @Override
    public void removeAll(Collection<Cookie> cookies){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.remove(createKey(cookie));
        }
        editor.apply();
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    private static String createKey(Cookie cookie) {
        return cookie.domain() + ":" + cookie.name();
    }
}
