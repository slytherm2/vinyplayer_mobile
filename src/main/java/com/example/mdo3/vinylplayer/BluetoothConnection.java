
/**
 * Created by micaiah on 2/3/2018.
 * Edited by Martin Do on 2/20/2018
 */

package com.example.mdo3.vinylplayer;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class BluetoothConnection extends Activity
{
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private final boolean DEBUG = true;

    private final int REQUEST_ENABLE_BT = 1;
    private final int BUFFER_SIZE = 1024;
    private final String MC_CONTROL = "Nord";

    private BluetoothSocket mmSocket = null;
    private BluetoothDevice mmDevice = null;
    private OutputStream outputStream = null;
    private InputStream inStream = null;
    private Handler mHandler;

    protected void onCreate(Bundle savedInstanceState)
    {

        // Register for broadcasts when a device is discovered.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        findDevices();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (DEBUG)
                System.out.println("DEBUG: Broadcast receiver for action_found\n");


            String action = intent.getAction();
            System.out.println("DEBUG: Action " + action);
            //System.out.println("DEBUG: " + BluetoothDevice.EXTRA_DEVICE);

            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
              System.out.println("DEBUG: LE Bluetooth\n");
            } else {
                System.out.println("DEBUG: Classic Bluetooth\n");
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();

                if(DEBUG){System.out.println("DEBUG: DeviceName: "+deviceName);}

                /*
                *If we find bluetooth device with name,
                * stop discovering other BT devices
                * Connect to specific BT device
                */
                if(deviceName.equals(MC_CONTROL))
                {
                    ConnectToDevice(device);
                }
            }
        }
    };


        public void findDevices() {

            if (DEBUG)
                System.out.println("DEBUG: Starting to look for BT devices\n");

            //check if the device has bluetooth and check if its enabled
            if (adapter == null) {
                System.out.println("DEBUG: Device doesn't support Bluetooth");
            }
            if (!adapter.isEnabled()) {

                if (DEBUG)
                    System.out.println("DEBUG: BT is not enabled....enabling now\n");

                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }

            if (DEBUG)
                System.out.println("DEBUG: Searching from paired Devices\n");

            //Check existing paired devices or discover new devices
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();

                    if (DEBUG) {
                        System.out.println("DEBUG: " + deviceName);
                    }
                    if (deviceName.contains(MC_CONTROL)) {
                        ConnectToDevice(device);
                    }
                }
            }
            if (DEBUG) {
                System.out.println("DEBUG: BT discovery\n");
                System.out.println("DEBUG: " + adapter.isDiscovering());
            }

            if (adapter.isDiscovering()) {
                // Bluetooth is already in modo discovery mode, we cancel to restart it again
                adapter.cancelDiscovery();
            }

            adapter.startDiscovery();
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (DEBUG)
                System.out.println("DEBUG: onActivityResult() did the user accept BT connection?>\n");

            if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    System.out.println("User Enabled Bluetooth");
                } else if (resultCode == RESULT_CANCELED) {
                    System.out.println("User Disabled Bluetooth");
                }
            }

            //Intent returnIntent = new Intent();
            //setResult(Activity.RESULT_OK,returnIntent);
            //finish();
        }

        public void ConnectToDevice(BluetoothDevice device) {    // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            if (DEBUG)
                System.out.println("DEBUG: ConnectToDevice() connecting to BT \n");

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            adapter.cancelDiscovery();

            if (DEBUG)
                System.out.println("DEBUG: run() connect to BT \n");

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            ManageConnection(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        private void ManageConnection(BluetoothSocket mmSocket) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int numOfBytes = 0;
            int b = BUFFER_SIZE;

            if (DEBUG)
                System.out.println("DEBUG: Managing connectiong, creating streams\n");

            try {
                inStream = mmSocket.getInputStream();
            } catch (IOException io) {
                System.out.println("Exception IO: manageMyConnectedSocket(): input stream\n");
            }
            try {
                outputStream = mmSocket.getOutputStream();
            } catch (IOException io) {
                System.out.println("Exception IO: manageMyConnectedSocket(): output stream\n");
            }

            while (true) {
                try {
                    if (DEBUG)
                        System.out.println("DEBUG: ManageConnection() sending message\n");

                    outputStream.write(buffer);

                    // Share the sent message with the UI activity.
                    Message writtenMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_WRITE, -1, -1, buffer);
                    writtenMsg.sendToTarget();
                } catch (IOException e) {
                    System.out.println("Error occurred when sending data");

                    // Send a failure message back to the activity.
                    Message writeErrorMsg =
                            mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",
                            "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    mHandler.sendMessage(writeErrorMsg);
                }
            }
        }

        private void write(String s) throws IOException {
            outputStream.write(s.getBytes());
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            // Don't forget to unregister the ACTION_FOUND receiver.
            unregisterReceiver(mReceiver);
        }

        private interface MessageConstants {
            public static final int MESSAGE_READ = 0;
            public static final int MESSAGE_WRITE = 1;
            public static final int MESSAGE_TOAST = 2;

            // ... (Add other message types here as needed.)
        }
    }