package com.example.mdo3.vinylplayer.asyncTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class SearchTask extends AsyncTask<Void, Void, String>
{
    private static final int THREAD_TIMEOUT = 2000;
    private String input;
//    private String artist;
//    private String album;
    private String resourceUrl;
    private String sessionId;
    private String userId;

    private final int CONTIMEOUT = 5000;

    public SearchTask(String input, String resourceUrl, String userId, String sessionId)
    {
        this.resourceUrl = resourceUrl;
        this.input = input;
        this.userId = userId;
        this.sessionId = sessionId;
    }


    @Override
    protected String doInBackground(Void... voids) {
        try
        {
            // turn input string into query string
            String charset = "UTF-8";
            String query = String.format("query=%s", URLEncoder.encode(this.input.trim(), charset));


            // task is only executable from authenticated users
            HttpsURLConnection connection = createHttpRequest(query);
            if(connection == null)
            {
                Log.d("SearchTask", "connection is null");
                return null;
            }

            // write query to POST request
            OutputStream output = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(query);
            writer.flush();
            writer.close();

            connection.connect();
            Thread.sleep(THREAD_TIMEOUT);

            // read list of records returned from POST request
            StringBuilder records = new StringBuilder();
            int responseCode = connection.getResponseCode();
            Log.d("SearchTask", "Response Code: " + String.valueOf(responseCode));
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    Log.d("SearchTask", "Received HTTP_OK");
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String nextLine;
                    while((nextLine = reader.readLine()) != null)
                    {
                        records.append(nextLine);
                    }
                    reader.close();
                    break;
                default:
                    Log.d("SearchTask", "Did not get HTTP_OK response");
            }
            connection.disconnect();
            return records.toString();
        }
        catch(IOException e)
        {
            Log.d("SearchTask", "IOException: " + e);
        }
        catch(InterruptedException e)
        {
            Log.d("SearchTask", "InterruptedException: " + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private HttpsURLConnection createHttpRequest(String query)
    {
        try
        {
            URL url = new URL(this.resourceUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); // real server
            // HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // local connection

            // allow for input and output request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty( "Content-Length", String.valueOf(query.length()));
            connection.setRequestProperty("Cookie", sessionId+";"+userId);
            //connection.setConnectTimeout(CONTIMEOUT);
            //connection.setReadTimeout(CONTIMEOUT);
            return connection;
        }
        catch(MalformedURLException e)
        {
            Log.d("SearchTask", "MalformedURLException: " + e);
            return null;
        }
        catch(IOException e) {
            Log.d("SearchTask", "IOException: " + e);
            return null;
        }
    }
}