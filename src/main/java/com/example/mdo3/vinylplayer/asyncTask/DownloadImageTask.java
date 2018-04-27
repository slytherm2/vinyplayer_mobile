package com.example.mdo3.vinylplayer.asyncTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Jr on 3/30/2018.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private Bitmap image;

    @Override
    protected Bitmap doInBackground(String... params) {
        // params[0] = image url
        String url = params[0];

        if(url == null)
            return null;

        try {
            URL imageURL = new URL(url);
            InputStream is = imageURL.openConnection().getInputStream();
            this.image = BitmapFactory.decodeStream(is);
            return this.image;
        } catch (Exception e) {
            Log.d("DownloadImageTask", e.toString());
            return null;
        }
    }
}
