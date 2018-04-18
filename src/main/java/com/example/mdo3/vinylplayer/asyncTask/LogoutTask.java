package com.example.mdo3.vinylplayer.asyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.example.mdo3.vinylplayer.ApplicationContext;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mdo3 on 4/7/2018.
 */
/**
 * Represents an asynchronous logout task
 */

public class LogoutTask extends AsyncTask<String, Void, Boolean>
{

    private final int THREAD_TIMEOUT = 2000;
    private static final int HTTP_TIMEOUT = 10000; //10 seconds

    //http information
    private String flag;
    private String userId_email;
    private String session_pass;
    private String url;

    private String cUserId;
    private String cSessionId;

    private boolean urlResponse = false;
    private HttpURLConnection urlConnection = null;

    @Override
    protected Boolean doInBackground(String... params)
    {
        if(params.length == 3)
        {
            userId_email = params[0];
            session_pass = params[1];
            url = params[2];
        }
        else
        {
            return false;
        }

        boolean result = false;

        try {
            if(userId_email != null && session_pass !=null)
            {
                urlConnection = createHttpRequest(url, userId_email, session_pass, flag);
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
            System.out.println(urlConnection.getResponseCode() == urlConnection.HTTP_OK);

            //If user sucessfully log in with cookie, no further action required
            //if user sucessfully log in with username and password, save the cookie information from server
            //if user failed to login with cookie, login with username and pass, save cookie info
            //if user failed to login with username and pass, return to main screen
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == urlConnection.HTTP_OK)
            {
            /*
            success: save cookies information
            success: if it already exist, don't do anything
             */
            System.out.println("DEBUG: Logout Successful");
            result = true;
            }
            //Cookie has been accepted by the server
            //HTTP_ACCEPTED = user has been logged in
            else if (responseCode == urlConnection.HTTP_ACCEPTED)
            {
                System.out.println("DEBUG: Successful login w/ cookie");
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

    protected void onPostExecute(final Boolean success)
    {

        if (success)
        {
            ApplicationContext contextInst = ApplicationContext.getInstance();
            Context context = contextInst.getAppContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(context.getResources().getString(R.string.session_id), "");
            editor.putString(context.getResources().getString(R.string.user_id), "");
            editor.putString(context.getResources().getString(R.string.label_email), "");
            editor.commit();
        }
        else
        {
            return;
        }
    }

    @Override
    protected void onCancelled()
    {
        // mAuthTask = null;
        //showProgress(false);
    }

    //Flag = 1 : create http request with cookie information
    //flag = 2 : create hhtp request with user name and password
    private HttpsURLConnection createHttpRequest(String urlString, String mEmail, String mPassword, String flag)
    {
        try
        {
            //local server
            //URL url = new URL(urlString);
            //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //Remote server
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection =  (HttpsURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 5.0;Windows98;DigExt)");
            urlConnection.setConnectTimeout(HTTP_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_TIMEOUT);

            //Creating http request with cookie
            StringBuilder str = new StringBuilder();
            str.append(mEmail);
            str.append(";");
            str.append(mPassword);
            urlConnection.setRequestProperty("Cookie", str.toString());

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
