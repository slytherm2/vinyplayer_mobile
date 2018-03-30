package com.example.mdo3.vinylplayer;

import android.app.Application;
import android.content.Context;

/**
 * Created by mdo3 on 3/30/2018.
 */

public class ApplicationContext extends Application
{
    private static Context mContext;
    private static ApplicationContext mInstance = new ApplicationContext();

    private ApplicationContext(){};

    public static ApplicationContext getInstance()
    {
        return (mInstance != null ? mInstance : null);
    }

    public static void setAppContext(Context context)
    {
        mContext = context;
    }

    public static Context getAppContext()
    {
        return mContext;
    }
}
