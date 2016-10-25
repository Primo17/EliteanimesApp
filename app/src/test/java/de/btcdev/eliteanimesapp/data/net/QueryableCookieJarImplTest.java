package de.btcdev.eliteanimesapp.data.net;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.btcdev.eliteanimesapp.BuildConfig;
import de.btcdev.eliteanimesapp.util.ModelFactory;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class QueryableCookieJarImplTest {

    private QueryableCookieJarImpl cookieJar;
    private HttpUrl url = HttpUrl.parse("http://www.domain.de");

    @Before
    public void setUpCookieJar() {
        cookieJar = new QueryableCookieJarImpl(
                new HashMapCookieCache(),
                new SharedPreferencesCookieStore(RuntimeEnvironment.application.getApplicationContext())
        );
    }

    @After
    public void tearDownCookieJar() {
        cookieJar.clear();
    }

    @Test
    public void roundtripWithCookie() {
        Cookie cookie = ModelFactory.getTestCookie("test", "value", false);

        cookieJar.saveFromResponse(url, Collections.singletonList(cookie));
        List<Cookie> loaded = cookieJar.loadForRequest(url);

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(cookie));
        assertThat(cookieJar.hasCookie(cookie.name(), cookie.domain()), is(true));
        assertThat(cookieJar.getCookie(cookie.name(), cookie.domain()), is(cookie));
    }

    @Test
    public void roundtripWithCookie_DifferentUrl() {
        Cookie cookie = ModelFactory.getTestCookie("test", "value", false);
        HttpUrl otherUrl = HttpUrl.parse("http://www.domain.com");

        cookieJar.saveFromResponse(url, Collections.singletonList(cookie));
        List<Cookie> loaded = cookieJar.loadForRequest(otherUrl);

        assertThat(loaded.isEmpty(), is(true));
    }

    @Test
    public void updateExistingCookies() {
        Cookie first = ModelFactory.getTestCookie("test", "first", false);
        Cookie second = ModelFactory.getTestCookie("test", "second", false);

        cookieJar.saveFromResponse(url, Collections.singletonList(first));
        cookieJar.saveFromResponse(url, Collections.singletonList(second));
        List<Cookie> loaded = cookieJar.loadForRequest(url);

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(second));
        assertThat(cookieJar.hasCookie(second.name(), second.domain()), is(true));
        assertThat(cookieJar.getCookie(second.name(), second.domain()), is(second));
    }

    @Test
    public void removeExpiredCookies() {
        Cookie cookie = ModelFactory.getTestCookie("first", "first", false);
        Cookie expired = ModelFactory.getExpiredTestCookie("second", "second", false);

        cookieJar.saveFromResponse(url, Arrays.asList(cookie, expired));
        List<Cookie> loaded = cookieJar.loadForRequest(url);

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(cookie));
        assertThat(cookieJar.hasCookie(expired.name(), expired.domain()), is(false));
    }

}
