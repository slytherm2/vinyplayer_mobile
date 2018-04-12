package com.example.mdo3.vinylplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewSwitcher;

public class MusicPlayer extends AppCompatActivity
{
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        ////adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.mp_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void backBtn(View view)
    {
        //Make the MC play the same song again, pressed twice to go back a song?
        System.out.println("DEBUG: Back button pressed");
    }

    public void forwardBtn(View view)
    {
        //Make the MC play the next song in the list
        System.out.println("DEBUG: forward button pressed");
    }

    public void playBtn(View view)
    {
        System.out.println("DEBUG: Play button pressed");
        //Send data (command) to MC to start, switch to stop button
        ViewSwitcher tempSwitcher = (ViewSwitcher) findViewById(R.id.switch_play_stop);
        tempSwitcher.showNext(); //or switcher.showPrevious();
    }

    public void stopBtn(View view)
    {
        System.out.println("DEBUG: Stop button pressed");
        //stop song : send data (command) to MC to stop, switch button to play button
        ViewSwitcher tempSwitcher = (ViewSwitcher) findViewById(R.id.switch_play_stop);
        tempSwitcher.showNext(); //or switcher.showPrevious();
    }
}
