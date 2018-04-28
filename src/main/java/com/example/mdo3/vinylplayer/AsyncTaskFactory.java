package com.example.mdo3.vinylplayer;

import com.example.mdo3.vinylplayer.asyncTask.*;

import android.content.Context;
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
import java.util.HashMap;

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
            case "Login":
                return null;
            case "ImageAnalysis":
                return new ImageAnalysisTask(input, url, userId, sessionId);
            default:
                return null;
        }
    }

    public AsyncTask generateAsyncTask(String type)
    {
        switch(type)
        {
            case "Search":
                return null;
            case "Login":
                //return new LoginTask();
            case "SignUp":
                return new SignUpTask();
            case "Logout":
                return new LogoutTask();
            case "Download":
                return new DownloadImageTask();
            //case "Image":
                //return new ImageFromGalleryTask();
            case "GetTracklist":
                return new GetTrackListTask();
            default:
                return null;
        }
    }

    public AsyncTask generateAsyncTask(String type, Context context)
    {
        switch(type)
        {
            case "Search":
                return null;
            case "Login":
                return new LoginTask(context);
            case "SignUp":
                return null;
            case "Logout":
                return null;
            case "Download":
                return null;
            case "Image":
                return new ImageFromGalleryTask(context);
            case "AddAlbum":
                return new AddAlbumTask(context);
            default:
                return null;
        }
    }
}

