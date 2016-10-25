package de.btcdev.eliteanimesapp.frameworks;

import android.os.Build;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.btcdev.eliteanimesapp.BuildConfig;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP, manifest = Config.DEFAULT_MANIFEST)
public class OkHttpTest {

    @Test
    public void testSimpleGet() throws Exception {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse("http://httpbin.org/get");
        Request request = new Request.Builder().
                url(url)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();

        assertThat(url, is(notNullValue()));
        assertThat(response.isSuccessful(), is(true));
        assertThat(body.string(), is(notNullValue()));
    }

    @Test
    public void testSimplePost() throws Exception {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("name", "testuser")
                .add("password", "password")
                .build();
        Request postRequest = new Request.Builder()
                .url("http://httpbin.org/post")
                .post(requestBody)
                .build();

        Call call = client.newCall(postRequest);
        Response response = call.execute();

        assertThat(response.isSuccessful(), is(true));
        assertContent(response.body().string());
    }

    private void assertContent(String content, String... parameters) {
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(content).getAsJsonObject();
        JsonObject form = root.getAsJsonObject("form");
        for(int i=0; i<parameters.length; i++) {
            if(i % 2 == 0) {
                assertThat(form.has(parameters[i]), is(true));
            } else {
                assertThat(form.getAsJsonPrimitive(parameters[i-1]), is(parameters[i]));
            }
        }
    }
}
