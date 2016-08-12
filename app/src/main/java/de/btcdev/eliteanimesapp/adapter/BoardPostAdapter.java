package de.btcdev.eliteanimesapp.adapter;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.btcdev.eliteanimesapp.R;
import de.btcdev.eliteanimesapp.data.EAParser;
import de.btcdev.eliteanimesapp.data.BoardPost;

public class BoardPostAdapter extends BaseAdapter {

    private LruCache<String, PostDrawable> imageCache;
    private Context context;
    private ArrayList<BoardPost> postList;
    private ArrayList<Boolean> spoilerArray;

    public BoardPostAdapter(Context context, ArrayList<BoardPost> postList,
                            ArrayList<Boolean> spoilerArray) {
        this.context = context;
        this.postList = postList;
        this.spoilerArray = spoilerArray;
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 16;
        imageCache = new LruCache<String, PostDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, PostDrawable bitmap) {
                if (bitmap.getBitmap() != null)
                    return bitmap.getBitmap().getRowBytes()
                            * bitmap.getBitmap().getHeight() / 1024;
                else
                    return 0;
            }
        };
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int position) {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return postList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView == null || convertView.getId() != R.id.forum_posts)
            rowView = inflater.inflate(R.layout.forum_posts, parent, false);
        else
            rowView = convertView;
        ImageView image = (ImageView) rowView
                .findViewById(R.id.forum_posts_image);
        TextView name = (TextView) rowView
                .findViewById(R.id.forum_posts_username);
        TextView userDate = (TextView) rowView
                .findViewById(R.id.forum_posts_userdate);
        TextView userSex = (TextView) rowView
                .findViewById(R.id.forum_posts_usergeschlecht);
        TextView date = (TextView) rowView.findViewById(R.id.forum_posts_date);
        TextView content = (TextView) rowView
                .findViewById(R.id.forum_posts_content);
        TextView signature = (TextView) rowView
                .findViewById(R.id.forum_posts_signatur);
        TextView edited = (TextView) rowView
                .findViewById(R.id.forum_posts_editiert);
        TextView online = (TextView) rowView
                .findViewById(R.id.forum_posts_useronline);
        View hline2 = rowView.findViewById(R.id.forum_posts_hline2);
        BoardPost post = postList.get(position);
        image.setImageBitmap(post.getAvatar());
        StringBuilder builder = new StringBuilder();
        if (post.getUserLevel() == 2) {
            builder.append("<font color=\"red\">");
            builder.append(post.getUserName());
            builder.append("</font>");
        } else {
            builder.append(post.getUserName());
        }
        name.setText(Html.fromHtml(builder.toString()));
        userDate.setText(post.getUserDate());
        userSex.setText(post.getSex());
        if (post.isOnline()) {
            builder = new StringBuilder();
            builder.append("<font color=\"green\">Online</font>");
        } else {
            builder = new StringBuilder();
            builder.append("<font color=\"red\">Offline</font>");
        }
        online.setText(Html.fromHtml(builder.toString()));
        date.setText(post.getDate());
        if (post.getEditedCount() != 0) {
            builder = new StringBuilder();
            builder.append(post.getEditedCount());
            builder.append(" mal editiert. Das letzte Mal am ");
            builder.append(post.getEditedTime());
            edited.setText(builder.toString());
            edited.setVisibility(View.VISIBLE);
        }
        String text = post.getText();
        if (text.contains("spoiler"))
            text = new EAParser(context).showSpoiler(
                    spoilerArray.get(position), text);
        content.setText(Html.fromHtml(text, new PostImageGetter(content), null));
        String sig = post.getSignature();
        if (sig != null && !sig.isEmpty()) {
            hline2.setVisibility(View.VISIBLE);
            signature.setVisibility(View.VISIBLE);
            signature.setText(Html.fromHtml(sig, new PostImageGetter(signature),
                    null));
        }
        return rowView;
    }

    public class PostImageGetter implements Html.ImageGetter {

        // Context c;
        TextView container;

        public PostImageGetter(TextView t) {
            // this.c = c;
            this.container = t;
        }

        @Override
        public Drawable getDrawable(String source) {
            // PostDrawable urlDrawable = new PostDrawable();
            PostDrawable urlDrawable = null;
            urlDrawable = imageCache.get(source);
            if (urlDrawable == null) {
                urlDrawable = new PostDrawable();
                // get the actual source
                ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(
                        urlDrawable);

                asyncTask.execute(source);

                // return reference to URLDrawable where I will change with
                // actual
                // image from
                // the src tag
            }
            return urlDrawable;
        }

        public class ImageGetterAsyncTask extends
                AsyncTask<String, Void, Drawable> {
            PostDrawable urlDrawable;
            String source;

            public ImageGetterAsyncTask(PostDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                source = params[0];
                return fetchDrawable(source);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                // set the correct bound according to the result from HTTP call
                if (result != null)
                    urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(),
                            result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;
                if (result != null)
                    imageCache.put(source, urlDrawable);

                // redraw the image by invalidating the container
                PostImageGetter.this.container.invalidate();

                if (result != null)
                    // For ICS
                    PostImageGetter.this.container
                            .setHeight((PostImageGetter.this.container
                                    .getHeight() + result.getIntrinsicHeight()));

                // Pre ICS
                PostImageGetter.this.container.setEllipsize(null);
            }

            /**
             * Get the Drawable from URL
             *
             * @param urlString
             * @return
             */
            public Drawable fetchDrawable(String urlString) {
                try {
                    final int reqHeight = 250;
                    final int reqWidth = reqHeight;
                    // First decode with inJustDecodeBounds=true to check
                    // dimensions
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    HttpGet httpRequest = new HttpGet(urlString);
                    HttpParams httpParameters = new BasicHttpParams();
                    int timeoutConnection = 2000;
                    HttpConnectionParams.setConnectionTimeout(httpParameters,
                            timeoutConnection);
                    int timeoutSocket = 3000;
                    HttpConnectionParams.setSoTimeout(httpParameters,
                            timeoutSocket);
                    HttpClient httpclient = new DefaultHttpClient(
                            httpParameters);
                    HttpResponse response = (HttpResponse) httpclient
                            .execute(httpRequest);
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
                            entity);
                    InputStream is = bufferedHttpEntity.getContent();
                    BitmapFactory.decodeStream(is, null, options);
                    is.close();
                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options,
                            reqWidth);
                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    is = bufferedHttpEntity.getContent();
                    Bitmap bild = BitmapFactory.decodeStream(is, null, options);
                    is.close();
                    @SuppressWarnings("deprecation")
                    Drawable drawable = new BitmapDrawable(bild);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    return drawable;
                } catch (Exception e) {
                    return null;
                }
            }

            public int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth) {
                final int width = options.outWidth;
                int inSampleSize = 1;

                if (width > reqWidth) {

                    final int halfWidth = width / 2;

                    while ((halfWidth / inSampleSize) > reqWidth) {
                        inSampleSize *= 2;
                    }
                }
                return inSampleSize;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public class PostDrawable extends BitmapDrawable {
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}