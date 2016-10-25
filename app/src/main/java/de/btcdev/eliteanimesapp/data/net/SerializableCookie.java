package de.btcdev.eliteanimesapp.data.net;

import android.util.Log;

import com.google.common.io.BaseEncoding;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.Cookie;

class SerializableCookie implements Serializable {

    private static final String TAG = SerializableCookie.class.getName();
    private transient Cookie cookie;

    SerializableCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    SerializableCookie(String encodedCookie) {
        cookie = decodeCookie(encodedCookie);
    }

    Cookie getCookie() {
        return cookie;
    }

    String getEncodedCookie() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            Log.d(TAG, "IOException in getEncodedCookie", e);
            return null;
        } finally {
            IOUtils.closeQuietly(objectOutputStream);
        }

        return BaseEncoding.base64().encode(byteArrayOutputStream.toByteArray());
    }

    private Cookie decodeCookie(String encodedCookie) {
        byte[] bytes = BaseEncoding.base64().decode(encodedCookie);;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableCookie) objectInputStream.readObject()).cookie;
        } catch (IOException e) {
            Log.d(TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", e);
        } finally {
           IOUtils.closeQuietly(objectInputStream);
        }
        return cookie;
    }

    private static long EXPIRED = -1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.name());
        out.writeObject(cookie.value());
        out.writeLong(cookie.persistent() ? cookie.expiresAt() : EXPIRED);
        out.writeObject(cookie.domain());
        out.writeObject(cookie.path());
        out.writeBoolean(cookie.secure());
        out.writeBoolean(cookie.httpOnly());
        out.writeBoolean(cookie.hostOnly());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Cookie.Builder builder = new Cookie.Builder();

        builder.name((String) in.readObject());
        builder.value((String) in.readObject());
        long expiresAt = in.readLong();
        if (expiresAt != EXPIRED) {
            builder.expiresAt(expiresAt);
        }
        String domain = (String) in.readObject();
        builder.domain(domain);
        builder.path((String) in.readObject());
        if (in.readBoolean())
            builder.secure();
        if (in.readBoolean())
            builder.httpOnly();
        if (in.readBoolean())
            builder.hostOnlyDomain(domain);

        cookie = builder.build();
    }
}
