package com.example.mdo3.vinylplayer.asyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.mdo3.vinylplayer.ApplicationContext;
import com.example.mdo3.vinylplayer.R;
import com.example.mdo3.vinylplayer.SignUp;

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
 * Created by mdo3 on 4/5/2018.
 */

public class SignUpTask extends AsyncTask<String, Void, Boolean >
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
    private ArrayList<String> cookieJar = new ArrayList<>();


    @Override
    protected void onPreExecute()
    {
    }

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
                saveCookieInfo(urlConnection.getHeaderFields());
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

            //creating http request with username and password
            String postParams = null;
            StringBuilder str = new StringBuilder();
            str.append(URLEncoder.encode("email", "UTF-8"));
            str.append("=");
            str.append(URLEncoder.encode(mEmail, "UTF-8"));
            str.append("&");
            str.append(URLEncoder.encode("password", "UTF-8"));
            str.append("=");
            str.append(URLEncoder.encode(mPassword, "UTF-8"));
            postParams = str.toString();

            urlConnection.setRequestProperty("Content-length", String.valueOf(postParams.length()));
            OutputStream outputPost = new BufferedOutputStream((urlConnection.getOutputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
            writer.write(postParams);
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

    private void saveCookieInfo( Map<String, List<String>> headerFields)
    {
        ApplicationContext appContext = ApplicationContext.getInstance();
        Context context = appContext.getAppContext();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        List<String> cookieHeaders = headerFields.get(context.getResources().getString(R.string.cooke_header));

        if (cookieHeaders != null)
        {
            for (String cHeader : cookieHeaders)
            {
                cookieJar.add(cHeader.substring(0, cHeader.indexOf(";")));
            }
        }
        editor.putString(context.getResources().getString(R.string.session_id), cookieJar.get(0));
        editor.putString(context.getResources().getString(R.string.user_id), cookieJar.get(1));
        editor.commit();
        System.out.println("DEBUG: Successfully saved Cookie Information");
    }
}
