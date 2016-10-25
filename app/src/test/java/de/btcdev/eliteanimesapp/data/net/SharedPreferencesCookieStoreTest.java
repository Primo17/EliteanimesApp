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
import java.util.Collection;
import java.util.Collections;

import de.btcdev.eliteanimesapp.BuildConfig;
import de.btcdev.eliteanimesapp.util.ModelFactory;
import okhttp3.Cookie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class SharedPreferencesCookieStoreTest {

    private SharedPreferencesCookieStore store;

    @Before
    public void setUpStore() {
        store = new SharedPreferencesCookieStore(RuntimeEnvironment.application.getApplicationContext());
    }

    @After
    public void TearDownStore() {
        store.clear();
    }

    @Test
    public void saveAndLoad() {
        Cookie toSave = ModelFactory.getTestCookie();

        store.saveAll(Collections.singleton(toSave));
        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(toSave));
    }

    @Test
    public void saveAll_SaveOnlyPersistentCookies() {
        Cookie toSave = ModelFactory.getTestCookie();
        Cookie notPersistent = ModelFactory.getNonPersistentTestCookie();

        store.saveAll(Arrays.asList(toSave, notPersistent));
        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(toSave));
        assertThat(loaded, not(contains(notPersistent)));
    }

    @Test
    public void removeAll_OnlyRemovesSpecifiedCookies() {
        Cookie first = ModelFactory.getTestCookie("first", "first");
        Cookie second = ModelFactory.getTestCookie("second", "second");
        Cookie third = ModelFactory.getTestCookie("third", "third");
        store.saveAll(Arrays.asList(first, second, third));

        store.removeAll(Arrays.asList(first, second));
        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(third));
    }

    @Test
    public void clear_ClearsAll() {
        Cookie first = ModelFactory.getTestCookie("first", "first");
        Cookie second = ModelFactory.getTestCookie("second", "second");
        store.saveAll(Arrays.asList(first, second));

        store.clear();
        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.isEmpty(), is(true));
    }

    @Test
    public void addAll_OnlyTheLastOfDuplicates() {
        Cookie first = ModelFactory.getTestCookie("first", "first");
        Cookie duplicate = ModelFactory.getTestCookie("first", "second");

        store.saveAll(Arrays.asList(first, duplicate));
        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(duplicate));
    }

    @Test
    public void addAll_UpdateExistingCookies() {
        Cookie first = ModelFactory.getTestCookie("test", "first");
        store.saveAll(Collections.singleton(first));
        Cookie second = ModelFactory.getTestCookie("test", "second");
        store.saveAll(Collections.singleton(second));

        Collection<Cookie> loaded = store.loadAll();

        assertThat(loaded.size(), is(1));
        assertThat(loaded, contains(second));
    }
}
