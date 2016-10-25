package de.btcdev.eliteanimesapp.data.net;


import java.util.Collection;
import java.util.List;

import okhttp3.Cookie;

interface PersistentCookieStore {

    List<Cookie> loadAll();

    void saveAll(Collection<Cookie> cookies);

    void removeAll(Collection<Cookie> cookies);

    void clear();
}
