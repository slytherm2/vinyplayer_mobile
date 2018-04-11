package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    private String imageURI;

    private String userEmail;

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

        if(imageURI == null)
            information.add(null);

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

        for(String str : information)
        {
            System.out.println("DEBUG : " + str);
        }

        if(Utils.saveInformation(information))
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
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 0) {


            Uri targetUri = data.getData();
            imageURI = targetUri.getPath();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                if (targetImage != null)
                    targetImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
