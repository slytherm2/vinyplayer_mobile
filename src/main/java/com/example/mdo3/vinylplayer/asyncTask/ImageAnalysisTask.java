package com.example.mdo3.vinylplayer.asyncTask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Base32;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class ImageAnalysisTask extends AsyncTask<Bitmap, Void, String> {
    private static final int THREAD_TIMEOUT = 2000;
    private Bitmap image;
    private String url;
    private String sessionId;
    private String userId;
    private String input;
    private final int TIMEOUT = 20000; //20seconds

    public ImageAnalysisTask(String input, String url, String userId, String sessionId)
    {
        this.url = url;
        this.sessionId = sessionId;
        this.userId = userId;
        this.input = input;
    }

    @Override
    protected String doInBackground(Bitmap... params) {
        this.image = params[0];

        if(this.image == null) { return null; }

        try
        {
            System.out.println("DEBUG: Inside image analysis");
            // task is only executable from authenticated users
            //HttpsURLConnection connection = createHttpRequest();
            HttpsURLConnection connection = createHttpRequest();
            if(connection == null)
            {
                Log.d("ImageAnalysisTask", "connection is null");
                return null;
            }
            connection.connect();

            // directly let .compress write binary image data
            // to the output-stream
//            OutputStream os = connection.getOutputStream();
//            this.image.compress(Bitmap.CompressFormat.PNG, 100, os);
//            os.flush();
//            os.close();

            // encode image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            this.image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] outputBytes = outputStream.toByteArray();
            String encodedImage = Base64.encodeToString(outputBytes, Base64.NO_WRAP);

            // turn input string into query string
            String charset = "UTF-8";
            String query = String.format("image=%s", URLEncoder.encode(encodedImage.trim(), charset));

            // write query to POST request
            OutputStream output = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
//            writer.write(encodedImage);
            writer.write(query);
            writer.flush();
            writer.close();

            int responseCode = connection.getResponseCode();
            StringBuilder records = new StringBuilder();
            System.out.println("DEBUG: Response code" + responseCode);
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    System.out.println("DEBUG: We gucci");
                    Log.d("SearchTask", "Received HTTP_OK");
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String nextLine;
                    while((nextLine = reader.readLine()) != null)
                    {
                        records.append(nextLine);
                        System.out.println("DEBUG: " + nextLine.toString());
                    }
                    reader.close();
                    break;
                default:
                    Log.d("SearchTask", "Did not get HTTP_OK response");
            }

            connection.disconnect();
            return records.toString();
        }
        catch(Exception e)
        {
            Log.d("ImageAnalysisTask", "Exception: " + e);
            return null;
        }
    }

    private HttpsURLConnection createHttpRequest()
    {
        try
        {
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); // real server
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // local connection

            // allow for input and output request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
//            connection.setRequestProperty("Content-Type", "image/png");
            // connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty("Cookie", this.sessionId+";"+this.userId);

            return connection;
        }
        catch(MalformedURLException e)
        {
            Log.d("ImageAnalysis", "MalformedURLException: " + e);
            return null;
        }
        catch(IOException e)
        {
            Log.d("ImageAnalysis", "IOException: " + e);
            return null;
        }
    }
}