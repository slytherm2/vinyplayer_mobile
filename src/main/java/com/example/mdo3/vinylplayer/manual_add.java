package com.example.mdo3.vinylplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import org.apache.commons.math3.geometry.euclidean.twod.Line;

import java.util.ArrayList;

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
    private ArrayList<String> songs;

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

        songListScroll = (ScrollView) findViewById(R.id.ma_song_list_scroll);
        album = (EditText) findViewById(R.id.ma_album_name);
        artist = (EditText) findViewById(R.id.ma_artist_name);

        rpm = (Switch) findViewById(R.id.ma_rotation_speed);
        rpm.setText(this.getString(R.string.rpm_33));
        rpm.setChecked(false);

        songList = (LinearLayout) findViewById(R.id.ma_song_list);
        songs = new ArrayList<>();
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
        albumSTR = album.getText().toString();
        artistSTR = artist.getText().toString();
        rpmStat = rpm.isChecked(); //false = 33 1/3 rpm ; true = 45 rpm

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
                if(!temp.isEmpty())
                    songs.add(edt.getText().toString());
                else
                    continue;
            }
            //get the respective start and stop time child from the parent and
            // Grandparent (songlist)
            if(songListChild instanceof LinearLayout)
            {
                LinearLayout songListGrandChild = (LinearLayout) songListChild;
                for(int j = 0; j < songListGrandChild.getChildCount(); j++)
                {
                    if(songListGrandChild.getChildAt(j) instanceof EditText)
                    {
                        EditText edt2 = (EditText) songListGrandChild.getChildAt(j);
                        System.out.println(edt2.getText().toString());
                        songs.add(edt2.getText().toString());
                    }
                }
            }
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

        //create a horizontal linear layout
        LinearLayout hLL = new LinearLayout(this);
        hLL.setOrientation(LinearLayout.HORIZONTAL);
        hLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        //create an edit text for start song input
        EditText startSong = new EditText(this);
        startSong.setLayoutParams(lParams);
        startSong.setHint("Start Time of Song ");
        startSong.setEms(10);
        startSong.setInputType(TYPE_CLASS_TEXT);
        hLL.addView(startSong);

        //create an edit text for stop song input
        startSong = new EditText(this);
        startSong.setLayoutParams(lParams);
        startSong.setHint("End Time of Song");
        startSong.setEms(10);
        startSong.setInputType(TYPE_CLASS_TEXT);
        hLL.addView(startSong);

        //add the horizontal linear layout to the vertical linear layout
        songList.addView(songInput, songList.getChildCount());
        songList.addView(hLL, songList.getChildCount());
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
}
