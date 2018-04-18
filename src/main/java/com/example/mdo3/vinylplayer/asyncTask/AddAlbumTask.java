package com.example.mdo3.vinylplayer.asyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.example.mdo3.vinylplayer.R;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mdo3 on 4/15/2018.
 * Create a post request to the /AddAlbum route on the Node.js server
 *
 */

public class AddAlbumTask extends AsyncTask<String, Void, Boolean>
{
    private final int THREAD_TIMEOUT = 2000;
    private final int HTTP_TIMEOUT = 2000;

    private Context mContext;
    private SharedPreferences preferences;
    private Resources rsrc;

    private String strURL;
    private String sessionId;
    private String userId;
    private HttpURLConnection urlConnection;

    private String artistName;
    private String artistId;
    private String album;
    private String id;
    private String thumbNail;
    private ArrayList<String> trackList;


    public AddAlbumTask(Context context)
    {
        this.mContext = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.rsrc = mContext.getResources();
    }

    @Override
    protected void onPreExecute()
    {
        this.strURL = rsrc.getString(R.string.https_url_addalbum);
        this.sessionId = preferences.getString(rsrc.getString(R.string.session_id), null);
        this.userId = preferences.getString(rsrc.getString(R.string.user_id), null);
        trackList = new ArrayList<>();
    }

    @Override
    protected Boolean doInBackground(String... params)
    {
        boolean result = false;

        try
        {
            if(strURL != null && userId != null && sessionId !=null)
            {
                urlConnection = createHttpRequest(strURL, userId, sessionId, params);
            }

            if(urlConnection != null)
            {
                urlConnection.connect();
                Thread.sleep(THREAD_TIMEOUT);
            }
            else
            {
                System.out.println("DEBUG: urlConnection == null");
                return result;
            }

            System.out.println("DEBUG: POST code " + urlConnection.getResponseCode());
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == urlConnection.HTTP_OK)
            {
                result = true;
            }
            else if (responseCode == urlConnection.HTTP_ACCEPTED)
            {
                result = true;
            }
            //Response code 404 or 500
            else
            {
                result = false;
            }
            urlConnection.disconnect();
        }
        catch(IOException e)
        {
            System.out.println("IOException : " + e);
            result = false;
        }
        catch(InterruptedException e)
        {
            System.out.println("Interrupted Exception : " + e);
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {

        if (success)
        {
        }
        else
        {
        }
    }

    @Override
    protected void onCancelled()
    {
    }

    private HttpsURLConnection createHttpRequest(String urlString,
                                                String userId,
                                                String sessionId,
                                                String[] params)
    {
        String albumId = params[0];
        try
        {
            URL url = new URL(urlString);
            //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //Remote server
            //URL url = new URL(urlString);
            HttpsURLConnection urlConnection =  (HttpsURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            urlConnection.setConnectTimeout(HTTP_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_TIMEOUT);

            StringBuilder str = new StringBuilder();
            str.append(userId);
            str.append(";");
            str.append(sessionId);
            urlConnection.setRequestProperty("Cookie", str.toString());

            //Add album route only requires album ID (discogs ID)
            String query = String.format("id=%s",
                    URLEncoder.encode(albumId, "UTF-8"));

            urlConnection.setRequestProperty("Content-length", String.valueOf(query.length()));
            OutputStream outputPost = new BufferedOutputStream((urlConnection.getOutputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            outputPost.close();
            return urlConnection;
        }
        catch(MalformedURLException error)
        {
            System.err.println("Malformed Problem: " + error);
            return null;
        }
        catch(SocketTimeoutException error)
        {
            System.err.println("Socket Problem: " + error);
            return null;
        }
        catch (IOException error)
        {
            System.err.println("IO Problem: " + error);
            return null;
        }
    }
}
