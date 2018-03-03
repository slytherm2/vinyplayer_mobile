package com.example.mdo3.vinylplayer;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
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
public class LowEnergyBlueTooth extends Activity {

    //Constants
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; //10secs
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int SUCCESS = 2;
    private static final int FAILURE = 1;
    private int mConnectionState = 0;

    //Bluetooth Variables
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mHandler = null;
    private BluetoothLeScanner btleScanner = null;
    private String ble_msg = null;
    private static String BLUETOOTH_NAME = null;
    private static String BLUETOOTH_ADDRESS = null;
    private BluetoothGatt mBluetoothGatt = null;

    //Bluetooth UUID
    private UUID SERVICE_UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c");
    private UUID CHARACTERISTIC_COUNTER_UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba");
    private UUID CHARACTERISTIC_INTERACTOR_UUID = UUID.fromString("0b89d2d4-0ea6-4141-86bb-0c5fb91ab14a");
    private UUID DESCRIPTOR_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI =
            "com.example.bluetooth.le.ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        if (DEBUG) {System.out.println("DEBUG: OnCreate");}

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();

        ble_msg = getResources().getString(R.string.BT_LE_NotFound);
        BLUETOOTH_NAME = getResources().getString(R.string.BT_name);
        BLUETOOTH_ADDRESS = getResources().getString(R.string.mc_address);


        //Checks if device has bluetooth LE capabilities
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || mBluetoothAdapter == null)
        {
            Toast.makeText(this, ble_msg, Toast.LENGTH_SHORT).show();
            returnToMain(FAILURE);
        }

        //Explicitly state access to coarse location (required for BT LE)
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        //If BT isn't enabled, ask user to enable BT
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            executeBT();
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

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btleScanner.stopScan(mScanCallback);
            }
        }, SCAN_PERIOD);
        btleScanner.startScan(createFilter(), createScanSettings(), mScanCallback);
    }

    //called after startActivityForResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(DEBUG){System.out.println("DEBUG: onActivityResult()");}

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            System.out.println("DEBUG: BT LE disabled by user");
            returnToMain(FAILURE);
        }
        else if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK)
        {
            System.out.println("DEBUG: BT LE enabled by user");
            executeBT();
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback()
    {
        public void onScanResult(int callbackType, ScanResult result)
        {
            if(DEBUG){System.out.println("DEBUG: onScanResult");}
            BluetoothDevice tmpDevice = result.getDevice();
            if(tmpDevice!=null && tmpDevice.getName().contains(BLUETOOTH_NAME))
            {
                if(DEBUG){System.out.println("DEBUG: Found " + tmpDevice.getName());}
                connectToDevice(tmpDevice);
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

    //1 = Failure
    //2 = success
    private void returnToMain(int result)
    {
        Intent returnIntent = new Intent();

        if(result == 1)
        {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        }
        else if(result == 2)
        {
            setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }

    private void connectToDevice(BluetoothDevice device) {

        if (DEBUG) {System.out.println("DEBUG: Connecting to " + device.getName());}

        cancelAdapterDiscovery(mBluetoothAdapter);
        btleScanner.stopScan(mScanCallback);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
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
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                returnToMain(SUCCESS);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                if (DEBUG) {System.out.println("DEBUG:Gatt Disconnected");}
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            if (DEBUG) {System.out.println("DEBUG: Inisde onReadRemoteRssi");}
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (DEBUG) {System.out.println("DEBUG: GATT Sucess");}
            } else {
            }
        };

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                // Handle the error
                return;
            }

            // Get the counter characteristic
            BluetoothGattCharacteristic characteristic = gatt
                    .getService(SERVICE_UUID)
                    .getCharacteristic(CHARACTERISTIC_COUNTER_UUID);

            // Enable notifications for this characteristic locally
            gatt.setCharacteristicNotification(characteristic, true);

            // Write on the config descriptor to be notified when the value changes
            BluetoothGattDescriptor descriptor =
                    characteristic.getDescriptor(DESCRIPTOR_CONFIG_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            if (DEBUG) {System.out.println("DEBUG: onCharacteristicRead");}
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                if (DEBUG) {System.out.println("DEBUG: GATT Success");}
            }
            else
            {
            }
        }

        private void readCounterCharacteristic(BluetoothGattCharacteristic
                                                       characteristic) {
            if (CHARACTERISTIC_COUNTER_UUID.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
              //  int value = Ints.fromByteArray(data);
                // Update UI
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            if (DESCRIPTOR_CONFIG_UUID.equals(descriptor.getUuid())) {
                BluetoothGattCharacteristic characteristic = gatt
                        .getService(SERVICE_UUID)
                        .getCharacteristic(CHARACTERISTIC_COUNTER_UUID);
                gatt.readCharacteristic(characteristic);
            }
        }
    };

    ArrayList<ScanFilter> createFilter()
    {
        ArrayList<ScanFilter> sfList = new ArrayList<>();
        ScanFilter.Builder sf = new ScanFilter.Builder();
        sf.setDeviceAddress(BLUETOOTH_ADDRESS);
        sfList.add(sf.build());
        return sfList;
    }

    ScanSettings createScanSettings()
    {
        ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
        builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        builderScanSettings.setReportDelay(0);
        return builderScanSettings.build();
    }

    void cancelAdapterDiscovery(BluetoothAdapter bt)
    {
        if(bt.isDiscovering())
            bt.cancelDiscovery();
        else
            return;
    }
}