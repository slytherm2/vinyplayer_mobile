package com.example.mdo3.vinylplayer;

/**
 * Created by mdo3 on 3/19/2018.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/*
Singleton class created to store bluetooth information about the connected device
Used throughout the application to send message from any activity to the micro controller
 */
public class BluetoothLESingleton
{
        private static BluetoothLESingleton mInstance = new BluetoothLESingleton();
        private static BluetoothAdapter mBluetoothAdapter = null;
        private static BluetoothGattService mGattService = null;
        private static UUID SERVICE_UUID = null;
        private static BluetoothGatt mGatt = null;
        private static boolean connStatus = false;

        private BluetoothLESingleton()
        {
            // Private constructor to avoid new instances
        }

        public static BluetoothLESingleton getInstance()
        {
            return (mInstance != null ? mInstance : null);
        }

        public void setBluetoothAdapter(BluetoothAdapter adapter)
        {
            mBluetoothAdapter = adapter;
        }

        public BluetoothAdapter getBluetoothAdapter()
        {
            return mBluetoothAdapter;
        }

        public void setGattService(BluetoothGattService mGattService)
        {
            this.mGattService = mGattService;
        }

        public BluetoothGattService getGattService()
        {
            return this.mGattService;
        }

        public void setSERVICE_UUID(UUID uuid)
        {
            this.SERVICE_UUID = uuid;
        }
        public void setConnStatus(Boolean result)
        {
            this.connStatus = result;
        }

        public UUID getSERVICE_UUID()
        {
            return this.SERVICE_UUID;
        }

        public void setGatt(BluetoothGatt gatt)
        {
            this.mGatt = gatt;
        }

        public BluetoothGatt getGatt()
        {
            return this.mGatt;
        }

        public Boolean getConnStatus() { return this.connStatus; }

}
