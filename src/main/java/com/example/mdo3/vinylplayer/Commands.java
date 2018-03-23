package com.example.mdo3.vinylplayer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;

import java.util.UUID;

/**
 * Created by mdo3 on 3/21/2018.
 */

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
public class Commands
{
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

    public static String getChangeSong(String command)
    {
        int length = command.length();

        if(length == 5)
            return command;
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
            return str.toString();
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
