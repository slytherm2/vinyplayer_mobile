package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mdo3.vinylplayer.asyncTask.AddAlbumTask;
import com.example.mdo3.vinylplayer.asyncTask.ImageAnalysisTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class testing extends AppCompatActivity
{
    BluetoothLESingleton leSingleton = BluetoothLESingleton.getInstance();
    private UUID SERVICE_UUID = leSingleton.getSERVICE_UUID();
    private BluetoothGatt mGatt = leSingleton.getGatt();
    private BluetoothGattService mGattService = leSingleton.getGattService();

    EditText textbox;
    EditText textbox2;
    EditText textbox3;
    byte[] data;

    private Toolbar mTopToolbar;
    private ImageView imageView;
    private Uri mImageUri;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        textbox = (EditText) findViewById(R.id.testing_textbox);
        textbox2 = (EditText) findViewById(R.id.testing_spacing);
        textbox3 = (EditText) findViewById(R.id.offset);
        imageView = (ImageView) findViewById(R.id.test_img);

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
        double temp1 = Double.valueOf(textbox.getText().toString());
        double temp2 = Double.valueOf(textbox2.getText().toString());
        double temp3 = Double.valueOf(textbox3.getText().toString());
        int x = util.calcValue(temp1, temp2, temp3);
        data = String.valueOf(x).getBytes();

        System.out.println("DEBUG: sending start time : " + temp1);
        System.out.println("DEBUG: sending spacing: " + temp2);
        System.out.println("DEBUG: sending calculated steps: " + x);

        LowEnergyBlueTooth.send(mGattService, SERVICE_UUID, mGatt, data);
    }

    public void cameraBtn(View view)
    {
        checkCamPerms();
    }

    private void checkCamPerms()
    {
        //Manifest requires camera use, user must give permission to use camera.
        //-1 = no camera permission
        //0 = camera permission granted
        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    1);
        }
        else
        {
            System.out.println("DEBUG: Calling Camera");
            callCamera();
        }
    }

    private void callCamera()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(cameraIntent, 1);
            }
        }
        else
        {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri apkURI = FileProvider.getUriForFile(
                    this,
                    this.getApplicationContext()
                            .getPackageName() + ".provider", getOutputMediaFile());
            //cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, apkURI);

            if (cameraIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(cameraIntent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Bitmap bitmap = null;
        /*if(data != null)
            bitmap = (Bitmap) data.getExtras().get("data");

        Uri uri = null;
        if(bitmap != null && data != null) {
            uri = data.getData();
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }*/

        //Bitmap resized = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
        //request code 1 = Bluetooth
        if (requestCode == 1)
        {
            try
            {

                System.out.println("DEBUG : Setting image in testing");
                Bitmap bMap = BitmapFactory.decodeFile(path);
                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bMap, "Warp" , "warp"));
                imageView.setImageBitmap(bMap);


            }
            catch(Exception e)
            {
                System.out.println("DEBUG: Exception!");
                System.out.println("DEBUG: Exception!" + e.getMessage());
            }
        }
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile()
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WARP");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("WARP", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        path = mediaFile.getAbsolutePath();
        return mediaFile;
    }
}
