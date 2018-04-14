package com.example.mdo3.vinylplayer.asyncTask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

public class ImageAnalysisTask extends AsyncTask<Bitmap, Void, Void> {
    private static final int THREAD_TIMEOUT = 2000;
    private Bitmap image;
    private String url;
    private String sessionId;
    private String userId;

    public ImageAnalysisTask(String url, String userId, String sessionId)
    {
        this.url = url;
        this.sessionId = sessionId;
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        this.image = params[0];

        if(this.image == null) { return null; }

        // convert image to a format that can be sent to the POST
        int size = this.image.getRowBytes() * this.image.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        this.image.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        String imageString = byteArray.toString();

        try
        {
            // task is only executable from authenticated users
//            HttpsURLConnection connection = createHttpRequest(imageString);
            HttpURLConnection connection = createHttpRequest(imageString);
            if(connection == null)
            {
                Log.d("ImageAnalysisTask", "connection is null");
                return null;
            }

            // write image to POST request
            OutputStream output = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(byteArray.toString());
            writer.flush();
            writer.close();
            output.close();

            connection.connect();
            Thread.sleep(THREAD_TIMEOUT);
            int responseCode = connection.getResponseCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    Log.d("ImageAnalysisTask", "Response Code Ok");
                    break;
                default:
            }

            connection.disconnect();
            return null;
        }
        catch(Exception e)
        {
            Log.d("ImageAnalysisTask", "Exception: " + e);
            return null;
        }
    }

    private HttpURLConnection createHttpRequest(String imageString)
    {
        try
        {
            URL url = new URL(this.url);
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); // real server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // local connection

            // allow for input and output request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "image/png");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty("Cookie", this.sessionId+";"+this.userId);
//            connection.setRequestProperty("Image", imageString);

            return connection;
        }
        catch(MalformedURLException e)
        {
            Log.d("ImageAnalysis", "MalformedURLException: " + e);
            return null;
        }
        catch(IOException e) {
            Log.d("ImageAnalysis", "IOException: " + e);
            return null;
        }
    }
}
