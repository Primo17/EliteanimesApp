package de.btcdev.eliteanimesapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.btcdev.eliteanimesapp.services.ConfigurationService;

/**
 * Klasse für alle verwendeten NetworkService-Aufgaben. Verwaltet die Adresse von EA,
 * die gesetzten Cookies und den HttpClient.
 */
@Singleton
public class NetworkService {

    public static final String eaURL = "http://www.eliteanimes.com";

    private Context context;
    private DefaultHttpClient httpclient;
    private List<Cookie> cookies;
    private String apikey = "8HB3GcTOiKm973zW9c1ioWwJa4ThDPzV";
    private boolean hasCookies = false;

    private ConfigurationService configurationService;

    /**
     * Ein neues NetworkService wird erzeugt. Dafür wird ein neuer HttpClient mit
     * passendem User-Agent gesetzt.
     *
     * @param context Context der aufrufenden Klasse, wird für String-Ressourcen
     *                benötigt.
     */
    @Inject
    public NetworkService(Context context, ConfigurationService configurationService) {
        this.context = context;
        this.configurationService = configurationService;
        httpclient = new DefaultHttpClient();
        ClientConnectionManager cmgr = httpclient.getConnectionManager();
        HttpParams param = httpclient.getParams();
        httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(
                param, cmgr.getSchemeRegistry()), param);
        param.setParameter(CoreProtocolPNames.USER_AGENT,
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        hasCookies = loadCookies();
    }

    /**
     * Ruft die übergebene Url als GET auf.
     *
     * @param url Aufzurufende URL
     * @throws EAException Bei allen Verbindungsfehlern
     */
    public void doGET(String url) throws EAException {
        try {
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null)
                entity.consumeContent();
        } catch (Exception e) {
            throw new EAException(e.getClass().toString() + ": " + e.getMessage());
        }
    }

    /**
     * Ruft die übergebene URL als POST auf und übergibt dabei alle Argumente der Liste.
     *
     * @param url  Aufzurufende URL
     * @param args Zu übergebene Argumente
     * @return Den Inhalt der geladenen Seite als String
     * @throws EAException Bei allen Verbindungsfehlern
     */
    public String doPOST(String url, List<NameValuePair> args) throws EAException {
        String input;
        try {
            HttpPost httpost = new HttpPost(url);
            httpost.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            input = convertStreamToString(is);
            entity.consumeContent();
        } catch (Exception e) {
            throw new EAException(e.getClass().toString() + ": " + e.getMessage());
        }
        return input;
    }

    public String getApikey() {
        if (apikey == null || apikey.equals("")) {
            try {
                Properties prop = new Properties();
                InputStream is = new FileInputStream("api.properties");
                prop.load(is);
                apikey = prop.getProperty("apikey");
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n" + e.getStackTrace());
            }
        }
        return apikey;
    }

    /**
     * Konvertiert den InputStream mithilfe eines StringWriters zu einem String
     * und gibt diesen zurück.
     *
     * @param is Zu konvertierender InputStream
     * @return String-Repräsentation des Streams
     */
    public String convertStreamToString(InputStream is) {
        char[] buff = new char[1024];
        Writer stringWriter = new StringWriter();
        try {
            Reader bReader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            int n;
            while ((n = bReader.read(buff)) != -1) {
                stringWriter.write(buff, 0, n);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                stringWriter.close();
                is.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return stringWriter.toString();
    }

    /**
     * Speichert die Cookies als Json-String in den SharedPreferences.
     */
    public void saveCookies() {
        CookieStore store = httpclient.getCookieStore();
        List<Cookie> cookies = store.getCookies();
        Gson gson = new Gson();
        String json = gson.toJson(cookies);
        SharedPreferences prefs = context.getSharedPreferences("cookies",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cookies", json);
        editor.apply();
    }

    /**
     * Lädt die Cookies, falls vorhanden, aus einem Json-String in den
     * CookieStore und setzt die UserID und den Benutzernamen.
     *
     * @return Wahrheitswert, ob Cookies vorhanden waren
     */
    public boolean loadCookies() {
        SharedPreferences prefs = context.getSharedPreferences("cookies",
                Context.MODE_PRIVATE);
        String json = prefs.getString("cookies", "");
        if (json.equals("")) {
            return false;
        } else {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<BasicClientCookie>>() {
            }.getType();
            List<Cookie> cookies = gson.fromJson(json, collectionType);
            CookieStore store = httpclient.getCookieStore();
            for (Cookie c : cookies) {
                store.addCookie(c);
            }
            cookies = store.getCookies();
            httpclient.setCookieStore(store);
            for (Cookie c : cookies) {
                if (c.getName().equals("user_id")) {
                    configurationService.setUserId(Integer.parseInt(c.getValue()));
                }
                if (c.getName().equals("user_name")) {
                    configurationService.setUserName(c.getValue());
                }
            }
            httpclient.getCookieStore().getCookies();
        }
        return true;
    }

    /**
     * Gibt zurück, ob das NetworkService die Login-Cookies besitzt.
     *
     * @return Wahrheitswert, ob Login-Cookies vorhanden
     */
    public boolean hasCookies() {
        cookies = httpclient.getCookieStore().getCookies();
        String username = "";
        for (Cookie c : cookies) {
            if (c.getName().equals("user_name"))
                username = c.getValue();
        }
        hasCookies = !username.equals("");
        return hasCookies;
    }

    /**
     * Löscht den Json-String der Cookies aus den SharedPreferences.
     */
    public void deleteCookies() {
        SharedPreferences prefs = context.getSharedPreferences("cookies",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("cookies");
        editor.apply();
        hasCookies = false;
    }

    public String getUserByCookie() {
        cookies = httpclient.getCookieStore().getCookies();
        String getUserName = "";
        for (Cookie c : cookies) {
            if (c.getName().equals("user_name"))
                getUserName = c.getValue();
        }
        return getUserName;
    }

    public int getIdByCookie() {
        cookies = httpclient.getCookieStore().getCookies();
        int id = 0;
        for (Cookie c : cookies) {
            if (c.getName().equals("user_id"))
                try {
                    id = Integer.parseInt(c.getValue());
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
        return id;
    }
}
