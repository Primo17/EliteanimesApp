package de.btcdev.eliteanimesapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;

import javax.inject.Inject;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.NetworkService;

public class ImageService {

    public static final int commentSize = 60;
    public static final int profileSize = 120;

    private NetworkService networkService;
    private Context context;

    @Inject
    public ImageService(NetworkService networkService, Context context) {
        this.networkService = networkService;
        this.context = context;
    }

    public Bitmap getBitmapFromUrl(String url, int size) {
        final int reqHeight = size;
        final int reqWidth = size;
        Bitmap image = null;
        try {
            if (url.contains("noava.png")) {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), R.drawable.noava, options);
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth,
                        reqHeight);
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                image = BitmapFactory
                        .decodeResource(context.getResources(), R.drawable.noava, options);
            } else {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                HttpGet httpRequest = new HttpGet(url);
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters,
                        timeoutConnection);
                int timeoutSocket = 5000;
                HttpConnectionParams
                        .setSoTimeout(httpParameters, timeoutSocket);
                HttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpResponse response = (HttpResponse) httpclient
                        .execute(httpRequest);
                HttpEntity entity = response.getEntity();
                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
                        entity);
                InputStream is = bufferedHttpEntity.getContent();
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth,
                        reqHeight);
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                is = bufferedHttpEntity.getContent();
                image = BitmapFactory.decodeStream(is, null, options);
                is.close();
            }
        } catch (Exception e) {
            System.out.println(e.getClass().toString());
        }
        return image;
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
