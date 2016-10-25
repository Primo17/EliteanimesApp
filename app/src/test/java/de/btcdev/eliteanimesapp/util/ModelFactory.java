package de.btcdev.eliteanimesapp.util;

import org.joda.time.DateTime;

import okhttp3.Cookie;

public class ModelFactory {

    public static Cookie getTestCookie() {
        return new Cookie.Builder()
                .path("/")
                .name("name")
                .value("value")
                .expiresAt(normalizedNow().plusDays(1).getMillis())
                .httpOnly()
                .hostOnlyDomain("www.domain.de")
                .build();
    }

    public static Cookie getTestCookie(String name, String value, boolean hostOnly) {
        Cookie.Builder builder = new Cookie.Builder()
                .path("/")
                .name(name)
                .value(value)
                .expiresAt(normalizedNow().plusDays(1).getMillis())
                .httpOnly();
        if(hostOnly)
            builder.hostOnlyDomain("www.domain.de");
        else
            builder.domain("www.domain.de");
        return builder.build();
    }

    public static Cookie getTestCookie(String name, String value) {
        return getTestCookie(name, value, true);
    }

    public static Cookie getNonPersistentTestCookie() {
        return new Cookie.Builder()
                .path("/")
                .name("name")
                .value("value")
                .httpOnly()
                .hostOnlyDomain("www.domain.de")
                .build();
    }

    public static Cookie getExpiredTestCookie(String name, String value, boolean hostOnly) {
        Cookie.Builder builder = new Cookie.Builder()
                .path("/")
                .name(name)
                .value(value)
                .expiresAt(normalizedNow().minusDays(1).getMillis())
                .httpOnly();
        if(hostOnly)
            builder.hostOnlyDomain("www.domain.de");
        else
            builder.domain("www.domain.de");
        return builder.build();
    }

    public static DateTime normalizedNow() {
        return DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0);
    }
}
