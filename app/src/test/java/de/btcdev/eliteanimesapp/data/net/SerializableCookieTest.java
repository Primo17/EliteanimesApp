package de.btcdev.eliteanimesapp.data.net;

import org.junit.Test;

import de.btcdev.eliteanimesapp.util.ModelFactory;
import okhttp3.Cookie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SerializableCookieTest {

    @Test
    public void serializeCookie() {
        Cookie cookie = ModelFactory.getTestCookie();
        SerializableCookie serializable = new SerializableCookie(cookie);
        SerializableCookie decodedCookie = new SerializableCookie(serializable.getEncodedCookie());

        assertThat(serializable.getCookie(), is(decodedCookie.getCookie()));
    }

    @Test
    public void serializeNonPersistentCookie() {
        Cookie cookie = ModelFactory.getNonPersistentTestCookie();
        SerializableCookie serializable = new SerializableCookie(cookie);
        SerializableCookie decodedCookie = new SerializableCookie(serializable.getEncodedCookie());

        assertThat(serializable.getCookie(), is(decodedCookie.getCookie()));
    }
}
