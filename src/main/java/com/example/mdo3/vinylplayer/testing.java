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
    byte[] data;

    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        textbox = (EditText) findViewById(R.id.testing_textbox);

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
        byte[] data = textbox.getText().toString().getBytes();
        System.out.println("DEBUG: Sending Data...");
        FormulaClass c = new FormulaClass();
        System.out.println("DEBUG: " + c.getValue());
        data = String.valueOf(c.getValue()).getBytes();
        LowEnergyBlueTooth.send(mGattService, SERVICE_UUID, mGatt, data);
        textbox.setText("");
    }
}
