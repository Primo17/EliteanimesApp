package de.btcdev.eliteanimesapp.data.net;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import de.btcdev.eliteanimesapp.util.ModelFactory;
import okhttp3.Cookie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class HashMapCookieCacheTest {

    private HashMapCookieCache cache;

    @Before
    public void setUpCache() {
        cache = new HashMapCookieCache();
    }

    @Test
    public void addAndGetCookie() {
        Cookie cookie = ModelFactory.getTestCookie("test", "value");

        cache.addAll(Collections.singleton(cookie));

        assertThat(cache.hasCookie(cookie.name(), cookie.domain()), is(true));
        assertThat(cache.getCookie(cookie.name(), cookie.domain()), is(cookie));
    }

    @Test
    public void removeAll_OnlySpecifiedCookies() {
        Cookie first = ModelFactory.getTestCookie("first", "first");
        Cookie second = ModelFactory.getTestCookie("second", "second");
        Cookie third = ModelFactory.getTestCookie("third", "third");

        cache.addAll(Arrays.asList(first, second, third));
        cache.removeAll(Arrays.asList(first, second));

        assertThat(cache.hasCookie(first.name(), first.domain()), is(false));
        assertThat(cache.hasCookie(second.name(), second.domain()), is(false));
        assertThat(cache.hasCookie(third.name(), third.domain()), is(true));
    }

    @Test
    public void clear_RemovesAll() {
        Cookie first = ModelFactory.getTestCookie("first", "first");
        Cookie second = ModelFactory.getTestCookie("second", "second");

        cache.addAll(Arrays.asList(first, second));
        cache.clear();

        assertThat(cache.hasCookie(first.name(), first.domain()), is(false));
        assertThat(cache.hasCookie(second.name(), second.domain()), is(false));
    }

    @Test
    public void addAll_UpdatesDuplicates() {
        Cookie first = ModelFactory.getTestCookie("test", "first");
        Cookie second = ModelFactory.getTestCookie("test", "second");

        cache.addAll(Collections.singleton(first));
        cache.addAll(Collections.singleton(second));

        assertThat(cache.hasCookie(second.name(), second.domain()), is(true));
        assertThat(cache.getCookie(second.name(), second.domain()), is(second));
        assertThat(cache.getCookie(first.name(), first.domain()), is(not(first)));
    }

    @Test
    public void addAll_AddOnlyTheLastOfDuplicates() {
        Cookie first = ModelFactory.getTestCookie("test", "first");
        Cookie second = ModelFactory.getTestCookie("test", "second");

        cache.addAll(Arrays.asList(first, second));

        assertThat(cache.hasCookie(second.name(), second.domain()), is(true));
        assertThat(cache.getCookie(second.name(), second.domain()), is(second));
        assertThat(cache.getCookie(first.name(), first.domain()), is(not(first)));
    }
}
