package com.example.mdo3.vinylplayer;

import com.example.mdo3.vinylplayer.asyncTask.*;
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

/**
 * Created by jose.medina on 3/24/2018.
 */

public class AsyncTaskFactory
{
    public AsyncTask generateAsyncTask(String type, String input, String url, String userId, String sessionId)
    {
        switch(type)
        {
            case "Search":
                return new SearchTask(input, url, userId, sessionId);
            default:
                return null;
        }
    }
}

