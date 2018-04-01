package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Jr on 3/30/2018.
 */

public class Utils
{

    public static Bitmap LoadImageFromWeb(String url)
    {
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

    //check for previously saved cookies from the application
    public static boolean hasCookies(Context context)
    {
        SharedPreferences preferences = null;
        String sessionId = null;
        String userId = null;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sessionId = preferences.getString(context.getResources().getString(R.string.session_id),null);
        userId = preferences.getString(context.getResources().getString(R.string.user_id), null);

        if(sessionId != null && userId != null)
        {
            System.out.println("DEBUG: cookies are available");
            return true;
        }
        System.out.println("DEBUG: no cookies available");
        return false;
    }
}
