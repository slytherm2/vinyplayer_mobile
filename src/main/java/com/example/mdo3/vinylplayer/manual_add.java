package com.example.mdo3.vinylplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static android.text.InputType.TYPE_CLASS_TEXT;

public class manual_add extends AppCompatActivity
{
    private Toolbar mTopToolbar;
    private EditText album;
    private EditText artist;
    private Switch rpm;
    private LinearLayout songList;
    private ScrollView songListScroll;

    private String albumSTR;
    private String artistSTR;
    private Boolean rpmStat;
    private ArrayList<String> information;
    private ImageView targetImage;
    private Uri imageURI;

    private String userEmail;

    private final int CHOOSER = 20;
    private final int ENABLE_CAMERA = 5;
    private final int GALLERY = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);


        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.manual_add_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        userEmail = preferences.getString(this.getResources().getString(R.string.label_email), null);

        songListScroll = (ScrollView) findViewById(R.id.ma_song_list_scroll);
        album = (EditText) findViewById(R.id.ma_album_name);
        artist = (EditText) findViewById(R.id.ma_artist_name);

        rpm = (Switch) findViewById(R.id.ma_rotation_speed);
        rpm.setText(this.getString(R.string.rpm_33));
        rpm.setChecked(false);

        songList = (LinearLayout) findViewById(R.id.ma_song_list);
        targetImage = (ImageButton) findViewById(R.id.imageButton);
        information = new ArrayList<>();
        createSongInput(); // add first input box
    }

    public void switchBtn(View view)
    {
        if(rpm.isChecked())
        {
            rpm.setText(this.getString(R.string.rpm_45));
        }
        else
        {
            rpm.setText(this.getString(R.string.rpm_33));
        }
    }

    public void submitBtn(View view)
    {
        System.out.println("DEBUG: Manual Add Submit Buttton has been pressed");

        if(userEmail != null && !userEmail.isEmpty())
            information.add(userEmail);

        albumSTR = album.getText().toString();
        information.add(albumSTR);

        artistSTR = artist.getText().toString();
        information.add(artistSTR);

        System.out.println("DEBUG: "+ imageURI);
        if(imageURI == null)
            information.add(null);
        else
            information.add(imageURI.toString());

        System.out.println("DEBUG: " + imageURI);

        rpmStat = rpm.isChecked(); //false = 33 1/3 rpm ; true = 45 rpm
        information.add(rpmStat.toString());

        //cycle through the children from the id:songlist
        for(int i = 0; i < songList.getChildCount(); i++)
        {
            //Get the song name child from the parent : songlist
            View songListChild = (View) songList.getChildAt(i);
            if(songListChild instanceof EditText)
            {
                EditText edt = (EditText) songListChild;
                System.out.println(edt.getText().toString());
                String temp = edt.getText().toString();
                if(temp!= null && !temp.isEmpty())
                    information.add(temp);
                    //songs.add(edt.getText().toString());
                else
                    continue;
            }
        }

        if(Utils.saveInformationLocal(information))
        {
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
        }
    }

    public void addSongBtn(View view)
    {
        createSongInput();
    }

    private void createSongInput()
    {
        //Layout params 1 = width, param 2 = height
        //Create edit text for song input
        EditText songInput = new EditText(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        songInput.setLayoutParams(lParams);
        songInput.setHint("Name of Song ");
        songInput.setEms(10);
        songInput.setInputType(TYPE_CLASS_TEXT);

        //create an edit text for start song input
        EditText startSong = new EditText(this);
        startSong.setLayoutParams(lParams);
        startSong.setHint("Duration of Song");
        startSong.setEms(10);
        startSong.setInputType(TYPE_CLASS_TEXT);

        //add the horizontal linear layout to the vertical linear layout
        songList.addView(songInput, songList.getChildCount());
        songList.addView(startSong, songList.getChildCount());
        scrollBottom(songListScroll);
    }

    private void scrollBottom(ScrollView scroll)
    {
        View lastChild = scroll.getChildAt(scroll.getChildCount() - 1);
        int bottom = lastChild.getBottom() + scroll.getPaddingBottom();
        int sy = scroll.getScrollY();
        int sh = scroll.getHeight();
        int delta = bottom - (sy + sh);

        scroll.smoothScrollBy(0, delta);
    }

    public void imageBtn(View view)
    {
        /*
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
        */
       Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);


        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose From...");

        Intent[] intentArray = { cameraIntent };
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooser, CHOOSER);
    }

    @Override

    //request code  = 1, result code = -1 : gallery
    //request code = 1, result code = 0: camera
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("DEBUG: "+requestCode);
        System.out.println("DEBUG: "+resultCode);

        //Intent comes from the chooser on the manual add page
        if (requestCode == CHOOSER)
        {
            //camera intent
            if (resultCode == 0)
            {
                System.out.println("DEBUG: Camera Intent");
                checkCamPerms();
            }
            //Gallery intent
            else if (resultCode == -1)
            {
                System.out.println("DEBUG: Gallery Intent");

                Uri targetUri = data.getData();
                imageURI = targetUri;
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                try {
                    if(targetUri != null)
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                    if (targetImage != null && bitmap != null)
                        targetImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkCamPerms()
    {
        //Manifest requires camera use, user must give permission to use camera.
        //-1 = no camera permission
        //0 = camera permission granted
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    ENABLE_CAMERA);
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
                startActivityForResult(cameraIntent, ENABLE_CAMERA);
            }
        }
        else
        {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(cameraIntent, ENABLE_CAMERA);
            }
        }
    }
}
