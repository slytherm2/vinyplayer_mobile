package com.example.mdo3.vinylplayer;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

/*
*Activity for scanning and displaying available Bluetooth LE devices.
*StartActivityforResult() calls on OnActivityResult() once it is finished
*/
public class LowEnergyBlueTooth extends MainScreen
{
    //Constants
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 3000; //3 secs
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private int mConnectionState = 0;
    HashMap<String,BluetoothDevice> devicesList = new HashMap<>();

    //Bluetooth Variables
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mHandler = null;
    private BluetoothLeScanner btleScanner = null;
    private String ble_msg = null;
    private static String BLUETOOTH_NAME = null;
    private static String BLUETOOTH_ADDRESS = null;
    private BluetoothGatt mBluetoothGatt = null;
    private static BluetoothGattService gattService = null;
    private static final int SLEEP_TIMER = 250; //.25 seconds


    private static MainScreen mainScreen;
    private static Context context;

    //Bluetooth UUID
    private final static UUID SERVICE_UUID =
                        UUID.fromString("37313364-3030-3030-2d35-3033652d3463");
    //Service : 00002a05-0000-1000-8000-00805f9b34fb
    //Some other UUID : 37313364-3030-3030-2d35-3033652d3463

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
   /* public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI =
            "com.example.bluetooth.le.ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    */

    private boolean DEBUG = true;

    private String[] stringArray;
    private int stringArrayCount = 0;

    //singleton class to store information about the bluetooth to be used
    //throughtout the application
    BluetoothLESingleton leSingleton = BluetoothLESingleton.getInstance();
    Context staticContext;

    public void BTInitialize(Context context)
    {
        if (DEBUG) {System.out.println("DEBUG: OnCreate");}

        mainScreen = this;
        this.context = context;

        //setContentView(R.layout.please_wait);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();
        staticContext = ApplicationContext.getInstance().getAppContext();

        ble_msg = staticContext.getResources().getString(R.string.BT_LE_NotFound);
        BLUETOOTH_NAME = staticContext.getResources().getString(R.string.BT_name);

        /*//Checks if device has bluetooth LE capabilities
        if (!staticContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || mBluetoothAdapter == null)
        {
            return;
        }*/

        /*//Explicitly state access to coarse location (required for BT LE)
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);*/

        if(mBluetoothAdapter != null)
        {
            leSingleton.setBluetoothAdapter(mBluetoothAdapter);
            //If BT isn't enabled, ask user to enable BT
            if (!mBluetoothAdapter.isEnabled())
            {
               Toast.makeText(context, "Bluetooth isn't on", Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
                //Toast.makeText(context, "Searching for devices", Toast.LENGTH_SHORT).show();
                executeBT();
            }
        }
    }

    protected void executeBT()
    {
        if (DEBUG) {System.out.println("DEBUG: executeBT");}

        //prevent premature discovery
        cancelAdapterDiscovery(mBluetoothAdapter);

        //First check previously paired devices
        //if not found, look for device for 10seconds
        checkPrevBTDevices(mBluetoothAdapter, BLUETOOTH_NAME);
        scanLEDevices();
    }

    private void checkPrevBTDevices(BluetoothAdapter adapter, String btDevice)
    {
        if(DEBUG){System.out.println("DEBUG: checkPrevBTDevices()");}

        //Check existing paired devices or discover new devices
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                if (deviceName.contains(btDevice))
                {
                    connectToDevice(device);
                }
            }
        }
    }

    private void scanLEDevices()
    {
        if(DEBUG){System.out.println("DEBUG: scanLEDevices()");}
        mBluetoothAdapter.startDiscovery();
        btleScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if(btleScanner == null)
        {
            System.out.println("DEBUG: Bluetooth LE Not avaialbe");
            return;
        }

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                btleScanner.stopScan(mScanCallback);
                //returnToMain(Activity.RESULT_CANCELED);
            }
        }, SCAN_PERIOD);
        //btleScanner.startScan(createFilter(), createScanSettings(), mScanCallback);
        btleScanner.startScan(mScanCallback);
    }

    private final ScanCallback mScanCallback = new ScanCallback()
    {
        public void onScanResult(int callbackType, ScanResult result)
        {
            devicesList.put(result.getDevice().getName(),result.getDevice());
            if(devicesList.size() > 0)
            {
                BluetoothDevice targetDevice = devicesList.get(BLUETOOTH_NAME);
                if(targetDevice != null)
                {
                    connectToDevice(targetDevice);
                }
            }
        }

        public void onBatchScanResults(List<ScanResult> results)
        {
            if(DEBUG){System.out.println("DEBUG: onBatchScanResults");}
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            if(DEBUG){System.out.println("DEBUG: onScanFailed");}
        }
    };

    private void connectToDevice(BluetoothDevice device)
    {
        if (DEBUG) {System.out.println("DEBUG: Connecting to " + device.getName());}

        cancelAdapterDiscovery(mBluetoothAdapter);
        btleScanner.stopScan(mScanCallback);
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (DEBUG) {System.out.println("DEBUG: GATT Callback");}

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                if (DEBUG) {System.out.println("DEBUG: Gatt Connected");}
                leSingleton.setGatt(gatt);
                leSingleton.setConnStatus(true);
                gatt.discoverServices();
                //setButtonStatus(true);
                refreshPage();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                if (DEBUG) {System.out.println("DEBUG:Gatt Disconnected");}
                leSingleton.setConnStatus(false);
                //setButtonStatus(false);
                refreshPage();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            System.out.println(DEBUG ? "DEBUG: onServicesDiscovered()" : "");
            String str = "Connection Established";
            stringArray = str.split(" ");

            if (status != BluetoothGatt.GATT_SUCCESS)
            {
                if (DEBUG) {System.out.println("DEBUG: Bluetooth Gat Status Failed");}
                return;
            }

            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services)
            {
                if(service != null && send(service, SERVICE_UUID, mBluetoothGatt, stringArray[0].getBytes()))
                {
                    leSingleton.setGattService(service);
                    leSingleton.setSERVICE_UUID(SERVICE_UUID);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            System.out.println("DEBUG: onDescriptorWrite()");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            if (DEBUG) {System.out.println("DEBUG: Inisde onReadRemoteRssi");}
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
            }
            else
            {
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            if (DEBUG) {System.out.println("DEBUG: onCharacteristicRead");}
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
            }
            else
            {
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            //added thread sleep to slow down data transmission
            try
            {
                stringArrayCount++;
                Thread.sleep(SLEEP_TIMER);
                if (status == BluetoothGatt.GATT_SUCCESS)
                {
                    System.out.println(DEBUG ? "DEBUG: sending Data....Success" : "");
                    if (stringArrayCount < stringArray.length)
                    {
                        send(leSingleton.getGattService(), SERVICE_UUID, mBluetoothGatt,
                                stringArray[stringArrayCount].getBytes());
                    }
                }
                else
                {
                }
            }
            catch(InterruptedException e)
            {
                System.out.println("DEBUG: Interrupted Exception @ onCharacteristicWrite(): " + e);
            }
        }

        private void readCounterCharacteristic(BluetoothGattCharacteristic
                                                       characteristic)
        {
            System.out.println(DEBUG? "DEBUG: readCounterCharacteristics()" : "");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
           System.out.println("DEBUG: onCharacteristicChanged: " + characteristic.getValue());
        }
    };

    private ArrayList<ScanFilter> createFilter()
    {
        ArrayList<ScanFilter> sfList = new ArrayList<>();
        ScanFilter.Builder sf = new ScanFilter.Builder();
        sf.setDeviceAddress(BLUETOOTH_ADDRESS);
        sfList.add(sf.build());
        return sfList;
    }

    private ScanSettings createScanSettings()
    {
        ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
        builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        builderScanSettings.setReportDelay(0);
        return builderScanSettings.build();
    }

    private void cancelAdapterDiscovery(BluetoothAdapter bt)
    {
        if(bt != null && bt.isDiscovering())
            bt.cancelDiscovery();
        else
            return;
    }

    public static boolean send(BluetoothGattService mBluetoothGattService, UUID UUID_SEND,
                               BluetoothGatt mBluetoothGatt, byte[] data)
    {
        if (mBluetoothGatt == null)
        {
            System.out.println("DEBUG: sending Data....Failed");
            return false;
        }

        BluetoothGattCharacteristic characteristic =
                mBluetoothGattService.getCharacteristic(UUID_SEND);

        if (characteristic == null)
        {
            System.out.println("DEBUG: sending Data....Failed");
            return false;
        }

        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    protected void onDestroy()
    {
        cancelAdapterDiscovery(mBluetoothAdapter);
        super.onDestroy();
    }

    private void refreshPage()
    {
        Intent intent = new Intent(context, MainScreen.class);
        context.startActivity(intent);
    }

    private void setButtonStatus(boolean result)
    {
        if(result)
        {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            btn.setText(context.getResources().getString(R.string.label_con));
            btn.setEnabled(false);
        }
        else
        {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            btn.setText(context.getResources().getString(R.string.label_not_con));
            btn.setEnabled(true);
        }
    }
}