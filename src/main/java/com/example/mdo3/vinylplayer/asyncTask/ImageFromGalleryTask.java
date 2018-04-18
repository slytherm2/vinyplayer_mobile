package com.example.mdo3.vinylplayer.asyncTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mdo3.vinylplayer.Utils;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by mdo3 on 4/14/2018.
 */

public class ImageFromGalleryTask extends  AsyncTask<String, Void, Bitmap>
{
    private Bitmap image;
    private String filePath;
    private Context mContext;

    public ImageFromGalleryTask (Context context)
    {
        this.mContext = context;
    }

    @Override
    protected Bitmap doInBackground(String... params)
    {
        //System.out.println("DEBUG: inside image from gallery");
        Bitmap bitmap = null;
        this.filePath = params[0];

        try
        {
            bitmap = Utils.LoadImageFromGallery(mContext, filePath);
            if (bitmap != null)
            {
                return bitmap;
            }
        }
        catch(Exception e)
        {
            Log.d("DownloadImageTask", e.toString());
            return bitmap;
        }
        return bitmap;
    }
}
