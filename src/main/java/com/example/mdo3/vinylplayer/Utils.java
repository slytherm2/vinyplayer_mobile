package com.example.mdo3.vinylplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Jr on 3/30/2018.
 */

public class Utils {

    public static Bitmap LoadImageFromWeb(String url) {
        try {
            URL imageURL = new URL(url);
            InputStream is = imageURL.openConnection().getInputStream();
            Bitmap image = BitmapFactory.decodeStream(is);
            return image;

//            URL imageURL = new URL(url);
//            if(imageURL == null)
//            {
//                Log.d("Utils", "Image URL is null");
//                return null;
//            }
//            InputStream is = (InputStream) imageURL.openConnection();
//            if(imageURL == null)
//            {
//                Log.d("Utils", "Input Stream is null");
//            }
//
//            InputStream is = (InputStream) new URL(url).getContent();
//            Drawable d = Drawable.createFromStream(is, "src name");
//            return d;
        } catch (Exception e) {
            Log.d("Utils", e.toString());
            return null;
        }
    }
}
