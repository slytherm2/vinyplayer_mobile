package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Jr on 3/30/2018.
 * Altered by Martin Do 4/7/2018
 */

public class Utils
{



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

    public int calcValue(double startTime, double spacing)
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
        angle = (180/Math.PI) * Math.acos(z) + offset;
        steps =(int) Math.ceil(angle/stepAngle);

        System.out.println("DEBUG: x " + x);
        System.out.println("DEBUG: y " + y);
        System.out.println("DEBUG: z " + z);
        System.out.println("DEBUG: angle " + angle);
        System.out.println("DEBUG: steps " + steps);
        return steps + 20000;
    }

    /*
    First digit - instruction
    Next digits - details
    0 - start/stop
    1 - change speed
        0 - 33
        1 - 45
    2 - change song
        XXXX - steps
    3 - return home
     */


    public static byte[] getStartStop(String command)
    {
        //size of one
        if(command.length() == 1)
            return command.getBytes();
        return null;
    }

    public static byte[] getChangeSpeed(String command)
    {
        if(command.length() == 2)
            return command.getBytes();
        return null;
    }

    public static byte[] getChangeSong(String command)
    {
        int length = command.length();

        if(length == 5)
            return command.getBytes();
            //The micro controller requires this command to have 5bytes of data
            //1 byte command
            //2-5 bytes to be the number of steps
            //1 - 9999 steps
        else if(length < 5)
        {
            StringBuilder str = new StringBuilder(command);
            while(true)
            {
                str.insert(1,"0");
                if(str.length() >= 5)
                    break;
            }
            return str.toString().getBytes();
        }
        return null;
    }

    public static byte[] getHome(String command)
    {
        if(command.length() == 1)
            return command.getBytes();
        return null;
    }
}
