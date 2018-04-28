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
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jose.medina on 4/25/2018.
 */

public class GetTrackListTask extends AsyncTask<String, Void, String> {
    private String id;
    private String url;

    @Override
    protected String doInBackground(String... params) {
        try
        {
            this.id = params[0];
            this.url = params[1];

            String charset = "UTF-8";
            String query = String.format("id=%s", URLEncoder.encode(id.trim(), charset));

            // task is only executable from authenticated users
            // HttpURLConnection connection = createHttpRequest(query);
            HttpsURLConnection connection = createHttpsRequest(query);
            if(connection == null)
            {
                Log.d("GetTrackListTask", "connection is null");
                return null;
            }

            // write query to POST request
            OutputStream output = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(query);
            writer.flush();
            writer.close();

            connection.connect();

            // read list of records returned from POST request
            StringBuilder tracklist = new StringBuilder();
            int responseCode = connection.getResponseCode();
            Log.d("GetTrackListTask", "Response Code: " + String.valueOf(responseCode));
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    Log.d("GetTrackListTask", "Received HTTP_OK");
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String nextLine;
                    while((nextLine = reader.readLine()) != null)
                    {
                        tracklist.append(nextLine);
                    }
                    reader.close();
                    break;
                default:
                    Log.d("GetTrackListTask", "Did not get HTTP_OK response");
            }
            connection.disconnect();
            return tracklist.toString();
        }
        catch(Exception e)
        {
            Log.d("GetTrackListTask", "Exception: " + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private HttpURLConnection createHttpRequest(String query)
    {
        try
        {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // local connection

            // allow for input and output request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty( "Content-Length", String.valueOf(query.length()));

            return connection;
        }
        catch(MalformedURLException e)
        {
            Log.d("GetTrackListTask", "MalformedURLException: " + e);
            return null;
        }
        catch(IOException e) {
            Log.d("GetTrackListTask", "IOException: " + e);
            return null;
        }
    }

    private HttpsURLConnection createHttpsRequest(String query)
    {
        try
        {
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); // local connection

            // allow for input and output request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            connection.setRequestProperty( "Content-Length", String.valueOf(query.length()));

            return connection;
        }
        catch(MalformedURLException e)
        {
            Log.d("GetTrackListTask", "MalformedURLException: " + e);
            return null;
        }
        catch(IOException e) {
            Log.d("GetTrackListTask", "IOException: " + e);
            return null;
        }
    }
}
