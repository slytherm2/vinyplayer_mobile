package com.example.mdo3.vinylplayer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.util.UUID;

public class testing extends AppCompatActivity
{
    BluetoothLESingleton leSingleton = BluetoothLESingleton.getInstance();
    private UUID SERVICE_UUID = leSingleton.getSERVICE_UUID();
    private BluetoothGatt mGatt = leSingleton.getGatt();
    private BluetoothGattService mGattService = leSingleton.getGattService();

    EditText textbox;
    EditText textbox2;
    byte[] data;

    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        textbox = (EditText) findViewById(R.id.testing_textbox);
        textbox2 = (EditText) findViewById(R.id.testing_spacing);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.testing_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void button(View view)
    {
        System.out.println("DEBUG: button clicked");
        byte[] data = "0".getBytes();
        System.out.println("DEBUG: Sending Data...");

        Utils util = new Utils();
        int x = util.calcValueST(Double.valueOf(textbox.getText().toString()));
        data = String.valueOf(x).getBytes();

        LowEnergyBlueTooth.send(mGattService, SERVICE_UUID, mGatt, data);
        textbox.setText("");
    }


    public void buttonTwo(View view)
    {
        System.out.println("DEBUG: button2 clicked");
        byte[] data = "0".getBytes();
        System.out.println("DEBUG: Sending Data...");

        Utils util = new Utils();
        int x = util.calValueS(Double.valueOf(textbox2.getText().toString()));
        data = String.valueOf(x).getBytes();

        LowEnergyBlueTooth.send(mGattService, SERVICE_UUID, mGatt, data);
        textbox2.setText("");
    }
}
