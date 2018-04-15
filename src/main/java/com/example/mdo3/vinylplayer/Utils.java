package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jr on 3/30/2018.
 * Altered by Martin Do 4/7/2018
 */

public class Utils
{
    public static Bitmap LoadImageFromGallery(Context context, String filePath)
    {
        Bitmap bitmap = null;
        Uri imageUri = Uri.parse(filePath);
        final int READPERMISSION = 1;
        int cameraPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(context == null || filePath.isEmpty())
            return null;

        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READPERMISSION);
        }
        else
        {
            try
            {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
            }
            catch(IOException e)
            {
                Log.d("Exception", e.getMessage());
            }
        }
        return bitmap;
    }

    public static Bitmap LoadImageFromWeb(String url)
    {
        try {
            URL imageURL = new URL(url);
            InputStream is = imageURL.openConnection().getInputStream();
            Bitmap image = BitmapFactory.decodeStream(is);
            return image;

//            URL imageURL = new URL(url);
//            if(imageURL == null)
//            {
//                Log.d("Utils", "Image URL is null");
//                return null;
//            }
//            InputStream is = (InputStream) imageURL.openConnection();
//            if(imageURL == null)
//            {
//                Log.d("Utils", "Input Stream is null");
//            }
//
//            InputStream is = (InputStream) new URL(url).getContent();
//            Drawable d = Drawable.createFromStream(is, "src name");
//            return d;
        } catch (Exception e) {
            Log.d("Utils", e.toString());
            return null;
        }
    }

    //check for previously saved cookies from the application
    public static boolean hasCookies(Context context)
    {
        SharedPreferences preferences = null;
        String sessionId = null;
        String userId = null;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sessionId = preferences.getString(context.getResources().getString(R.string.session_id),null);
        userId = preferences.getString(context.getResources().getString(R.string.user_id), null);

        if(sessionId != null && userId != null && !sessionId.isEmpty() && !userId.isEmpty())
        {
            System.out.println("DEBUG: cookies are available");
            return true;
        }
        System.out.println("DEBUG: no cookies available");
        return false;
    }

    //calculate the number of steps to send to the MC
    public static int calcValue(double startTime, double spacing)
    {
        String units = "in";
        double armLength = 7.75;
        double offset = 30.0; //degrees
        double stepAngle = .005859375; //degrees

        double rpm = 33.3;
        //startTime = 0.0;

        double angle = 0.0;
        int steps = 0;

        double x = Math.pow((spacing * (rpm/60) * startTime),2);
        double y = 2 * Math.pow(armLength,2);

        double z = (x-y) / -y;
        angle = (180/Math.PI) * Math.acos(Math.toRadians(z)) + offset;
        steps =(int) Math.ceil(angle/stepAngle);

        /*
        System.out.println("DEBUG: x " + x);
        System.out.println("DEBUG: y " + y);
        System.out.println("DEBUG: z " + z);
        System.out.println("DEBUG: angle " + angle);
        System.out.println("DEBUG: steps " + steps);
        */
        return steps + 20000;
    }

    //save the information into an xml file
    //location is the default location, set by preference manager
    //USes the "LocalCat" tag
    public static boolean saveInformationLocal(ArrayList<String> info)
    {
        //Comma Separated Value
        //UserId
        //Artist, album, Image URI, RPM speed (false = 33 1/3, true = 45rpm)
        //Song name, start time of song, end time of song
        String emailTag = info.get(0);
        StringBuilder strBuilder = new StringBuilder();
        ApplicationContext contextInst = ApplicationContext.getInstance();
        Context context = contextInst.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        //start at 1 because the first one is the user email
        //the user email will be used as tag to pull up information in the future
        for(int i = 1; i < info.size(); i++)
        {
            strBuilder.append(info.get(i));
            strBuilder.append(",");
        }

        //check for exisiting local copy
        String str = preferences.getString(emailTag + context.getResources().getString(R.string.local_catalog),
                null);
        if(str != null)
        {
            strBuilder.insert(0, str);
        }

        strBuilder.append(context.getString(R.string.stop_flag));
        strBuilder.append(",");

        editor.putString(emailTag + context.getResources().getString(R.string.local_catalog),
                strBuilder.toString());
        editor.commit();

        return (context != null && preferences != null) ? true : false;
    }

    //save the information into an xml file
    //location is the default location, set by preference manager
    //USes the "SearchCat" tag
    public static boolean saveInformationSearch(ArrayList<String> info)
    {
        //Comma Separated Value
        //UserId
        //Artist, album, Image URI, RPM speed (false = 33 1/3, true = 45rpm)
        //Song name, start time of song, end time of song
        String emailTag = info.get(0);
        StringBuilder strBuilder = new StringBuilder();
        ApplicationContext contextInst = ApplicationContext.getInstance();
        Context context = contextInst.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        //start at 1 because the first one is the user email
        //the user email will be used as tag to pull up information in the future
        for(int i = 1; i < info.size(); i++)
        {
            strBuilder.append(info.get(i));
            strBuilder.append(",");
        }

        //check for exisiting local copy
        String str = preferences.getString(emailTag + context.getResources().getString(R.string.search_catalog),
                null);
        if(str != null)
        {
            strBuilder.insert(0, str);
        }

        strBuilder.append(context.getString(R.string.stop_flag));
        strBuilder.append(",");

        editor.putString(emailTag + context.getResources().getString(R.string.search_catalog),
                strBuilder.toString());
        editor.commit();

        return (context != null && preferences != null) ? true : false;
    }

    //used to convert the song times (min:sec or minsec format) into seconds
    public static int convertToSeconds(String time)
    {
        double minutes = 0;
        double seconds = 0;
        int minPos = 0;
        int secPos = 0;

        time= time.trim();
       minPos = time.indexOf(":");
       //calculate the number of seconds when user disregards : symbol
       if(minPos == -1)
       {
           if(time.length() >= 3)
           {
               int  powerCounter = time.length() - 3;
               int x = 0;
                for(int i = 0; i < time.length() - 2; i++)
                {
                    x = Character.getNumericValue(time.charAt(i));
                    minutes += x * Math.pow((double) 10, (double) powerCounter);
                    powerCounter--;
                }
                int y = time.length();
               seconds = Character.getNumericValue(time.charAt(y - 2)) * 10
                       + Character.getNumericValue(time.charAt(y - 1));
           }
       }
       //calculate the number of seconds when the user inputs the : symbol
       else
       {
           int powerCounter = minPos - 1;
           int x = 0;
            for(int i = 0; i < minPos; i++)
            {
                x = Character.getNumericValue(time.charAt(i));
                minutes += x * Math.pow((double) 10, (double) powerCounter);
                powerCounter--;
            }
            seconds = Character.getNumericValue(time.charAt(minPos + 1)) * 10
                        + Character.getNumericValue(time.charAt(minPos + 2));
       }
       // System.out.println("DEBUG: Minutes " + minutes);
       //System.out.println("DEBUG: Seconds " + seconds);
       return (int) minutes * 60 + (int) seconds;
    }
}
