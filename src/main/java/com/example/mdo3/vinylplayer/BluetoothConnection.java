package com.example.mdo3.vinylplayer;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by micaiah on 2/3/2018.
 */
public class BluetoothConnection {
    public BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();


    public void findDevices() {
        if(adapter == null) {
            Log.d("Yo", "Device doesn't support bluetooth");
        }
        else if(!adapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // enable bluetooth using intent
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                System.out.println(deviceName);
                if(deviceName.contains("Nord")) {
                    ConnectToDevice(device);
                }
            }
        }
    }
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    public void ConnectToDevice(BluetoothDevice device) {    // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    private OutputStream outputStream;
    private InputStream inStream;
    
    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        adapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            outputStream = mmSocket.getOutputStream();
            inStream = mmSocket.getInputStream();

            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;
            int b = BUFFER_SIZE;

            while (true) {
                try {
                    bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

}
