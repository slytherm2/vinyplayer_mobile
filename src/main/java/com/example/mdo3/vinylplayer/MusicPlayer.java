package com.example.mdo3.vinylplayer;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.mdo3.vinylplayer.asyncTask.DownloadImageTask;
import com.example.mdo3.vinylplayer.asyncTask.ImageFromGalleryTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MusicPlayer extends AppCompatActivity
{
    private Toolbar mTopToolbar;
    private Intent pastIntent;
    private Record record;
    private TextView album_artist;
    private TextView song;
    private ListView songList;
    private ArrayAdapter<String> songAdapter;
    private ArrayList<Song> songTrackList;
    private ArrayList<Double> albumSongTime;
    private ArrayList<Double> albuumSongTimeB;
    private ArrayList<String> albumSongs;
    private  ViewSwitcher tempSwitcher;
    private ImageView coverAlbumView;
    private Uri imageUri;
    private Bitmap bitmap;

    private BluetoothLESingleton btleSingleton;

    private final static int READPERM = 1;

    //Calculation variables
    private final int SONGGAPTIME = 5;
    private int songTime = 0;
    private int currentPos = 0;
    private double spacing = 0.0125; //TODO: more callibration is required
    private Song songObj;
    private double offset = 0.0;

    private int songPos = 0;

    public boolean DEBUG = false;

    private DrawerLayout drawerLayout = null;


    /*
First digit - instruction
Next digits - details
0 - start/stop
1 - change speed
0 - 33
1 - 45
2 - change song
XXXX - steps
3 - return home
4 - Anti-Skip
*/
    //MC Commands
    private int CHANGE33 = 10;
    private int CHANGE45 = 11;
    private final int STARTSTOP = 0;
    private final int SPEED = 1;
    private final int CHANGESONG = 2;
    private final int HOME = 3;
    private final int ANTISKIP = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        ////adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        /*mTopToolbar = (Toolbar) findViewById(R.id.mp_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        */

        //Main Screen passed the record object to the music player
        pastIntent = getIntent();
        record = pastIntent.getParcelableExtra(this.getResources().getString(R.string.record));
        tempSwitcher  = (ViewSwitcher) findViewById(R.id.switch_play_stop);
        album_artist = (TextView) findViewById(R.id.artist_album_TextView);
        album_artist.setText(this.getResources().getString(R.string.default_album_artist));
        song = (TextView) findViewById(R.id.song_TextView);
        song.setText(this.getResources().getString(R.string.default_song));

        btleSingleton = BluetoothLESingleton.getInstance();

        //Adding the track songs from the record to an array list to be used in the listview
        if(record != null)
            songTrackList = record.getTracklist();
        albumSongTime = new ArrayList<>();
        albumSongs = new ArrayList<>();
        songTime = 0;
        StringBuilder tempstr;
        if(songTrackList != null && songTrackList.size() > 0)
        {
            tempstr = new StringBuilder();
            albumSongTime.add((double) songTime); //The start of the first songe

            songObj = songTrackList.get(0);
            String tempValue = songObj.getPosition();
            if(tempValue != null)
            {
                tempstr.append(tempValue.toString());
                ++songPos;
            }
            else
            {
                tempstr.append(String.valueOf(++songPos));
                songObj.setPosition(String.valueOf(songPos));
            }

            tempstr.append(": ");

            tempValue = songObj.getTitle();
            if(tempValue != null)
                tempstr.append(tempValue.toUpperCase().toString());
            tempstr.append("\t");

            tempValue = songObj.getDuration();
            if(tempValue != null)
                tempstr.append(tempValue.toString());

            albumSongs.add(tempstr.toString());

            double lastTime = 0.0;
            for (int i = 1; i < songTrackList.size(); i++)
            {
                //account for the gap time between songs
                //get the duration of the previous song

                tempstr = new StringBuilder();
                songObj = songTrackList.get(i - 1);

                if(songObj.getPosition().charAt(0) == 'B')
                    lastTime = songTime;

                songTime += Utils.convertToSeconds(songObj.getDuration());
                if(i == 1)
                {
                    albumSongTime.add((double) songTime + 2.5 - lastTime);
                }
                else
                {
                    albumSongTime.add((double)songTime + (double) SONGGAPTIME - lastTime);
                }
                songObj = songTrackList.get(i);

                tempValue = songObj.getPosition();
                if(tempValue != null)
                {
                    tempstr.append(tempValue.toString());
                    ++songPos;
                }
                else
                {
                    tempstr.append(String.valueOf(++songPos));
                    songObj.setPosition(String.valueOf(songPos));
                }

                tempstr.append(". ");

                tempValue = songObj.getTitle();
                if(tempValue != null)
                    tempstr.append(tempValue.toUpperCase().toString());

                tempstr.append("\t");

                tempValue = songObj.getDuration();
                if(tempValue != null)
                    tempstr.append(tempValue.toString());

                albumSongs.add(tempstr.toString());
            }
        }
        if(DEBUG)
            System.out.println("DEBUG: " + albumSongTime);

        songList = (ListView) findViewById(R.id.mp_songlist);
        songAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                albumSongs);
        songList.setAdapter(songAdapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {
                String localSong = albumSongs.get(position).toUpperCase().toString();
                if(DEBUG)
                System.out.println("DEBUG: " + localSong);
                album_artist.setText(record.getAlbum() + " - " + record.getArtist());
                song.setText(localSong);
                currentPos = position;
                playSelectSong(position);
            }
        });


        coverAlbumView = (ImageView) findViewById(R.id.cover_ImageView);
        int cameraPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READPERM);
        }
        else
        {
            bitmap = null;
            AsyncTaskFactory factory = new AsyncTaskFactory();
            DownloadImageTask downloadTask = null;
            ImageFromGalleryTask imageTask = null;
            if(record != null)
            {
                if(record.getFilePath() != null && !record.getFilePath().isEmpty())
                {
                    imageTask = (ImageFromGalleryTask) factory.generateAsyncTask("Image", this);
                    String[] params = {record.getFilePath()};
                    try
                    {
                        bitmap = (Bitmap) imageTask.execute(params).get();
                    }
                    catch(Exception e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                }

                else if (record.getUrl() != null && !record.getUrl().isEmpty())
                {

                    if(factory != null)
                        downloadTask = (DownloadImageTask) factory.generateAsyncTask("Download");
                    if(downloadTask != null)
                    {
                        try
                        {
                            bitmap = (Bitmap) downloadTask.execute(record.getUrl().toString()).get();
                        }
                        catch (InterruptedException e)
                        {
                            Log.d("Exception", e.getMessage());
                        }
                        catch(ExecutionException e)
                        {
                            Log.d("Exception", e.getMessage());
                        }
                    }
                }
                if (bitmap != null)
                    coverAlbumView.setImageBitmap(bitmap);
            }
        }

        //Deals with the items inside the navigation drawer aka hamburger menu
        //best to use fragments when working with the navigation drawer
        drawerLayout = findViewById(R.id.music_player_drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.music_player_nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    public boolean onNavigationItemSelected(MenuItem menuItem)
                    {
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();
                        menuItem.setChecked(false);
                        System.out.println("DEBUG: " + menuItem.toString() + " has been pressed");
                        launchMenuActivity(menuItem);
                        return true;
                    }
                });

        //deals with the nav menu bar or hamburger menu
        Toolbar toolbar = (Toolbar) findViewById(R.id.mp_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener()
                {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset)
                    {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView)
                    {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView)
                    {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState)
                    {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        //TODO: uncomment when reeady
        //start at home on start up
        //sendData(HOME);
    }

        /*
    First digit - instruction
    Next digits - details
    0 - start/stop
    1 - change speed
        0 - 33
        1 - 45
    2 - change song
        XXXX - steps
    3 - return home
    4 - Anti-Skip
     */

    public void backBtn(View view)
    {
        //Make the MC play the same song again, pressed twice to go back a song?
        System.out.println("DEBUG: Back button pressed");
        prevSong();
    }

    private void prevSong()
    {
        System.out.println("DEBUG: Previous Song");
        double startTime = 0;
        int x = 0;

        if(albumSongTime.size() < currentPos)
            return;

        startTime = albumSongTime.get(currentPos);
        setInitialValues(currentPos);
        x = Utils.calcValue((double) startTime, spacing, offset);

        System.out.println("DEBUG: Song:" + albumSongs.get(currentPos));
        System.out.println("DEBUG: startTime:" + startTime);
        sendData(CHANGESONG, x);
        Toast.makeText(this, R.string.replay_song, Toast.LENGTH_SHORT).show();
    }

    public void forwardBtn(View view)
    {
        //Make the MC play the next song in the list
        System.out.println("DEBUG: forward button pressed");
        nextSong();
    }

    private void nextSong()
    {
        System.out.println("DEBUG: Next Song");
        double startTime = 0;
        int x = 0;

        //if you are at the end of the song list, cannot play next track
        if(currentPos < albumSongs.size() - 1)
        {
            currentPos++;
            song.setText(albumSongs.get(currentPos).toString());

            startTime = albumSongTime.get(currentPos);
            setInitialValues(currentPos);
            x = Utils.calcValue((double) startTime, spacing, offset);
            sendData(CHANGESONG, x);
            System.out.println("DEBUG: Song:" + albumSongs.get(currentPos));
            System.out.println("DEBUG: startTime:" + startTime);
            Toast.makeText(this, R.string.starting_song, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, R.string.end_of_playlist, Toast.LENGTH_SHORT).show();
        }
    }

    public void playBtn(View view)
    {
        System.out.println("DEBUG: Play button pressed");
        //Send data (command) to MC to start, switch to stop button
        playSong();
    }

    //play selected song track
    private void playSelectSong(int position)
    {
        System.out.println("DEBUG: Song starting");
        double startTime = 0;
        int x = 0;
        currentPos = position;

        //if it shows the play button, switch to stop button
        //if it shows the stop button, and user selects a new song, keep it at the play button
        if(tempSwitcher.getDisplayedChild() == 0)
            tempSwitcher.showNext();

        //Play the selected song
        startTime = albumSongTime.get(currentPos);
        setInitialValues(currentPos);
        x = Utils.calcValue((double) startTime, spacing, offset);
        sendData(CHANGESONG, x);
        Toast.makeText(this, R.string.starting_song, Toast.LENGTH_SHORT).show();
    }

    //resume song
    private void playSong()
    {
        System.out.println("DEBUG: Song resuming");
        tempSwitcher.showNext();

        //play the current song, after being paused
        sendData(STARTSTOP);
        System.out.println("DEBUG: " + albumSongs.get(currentPos));
        Toast.makeText(this, R.string.starting_song, Toast.LENGTH_SHORT).show();
    }

    public void stopBtn(View view)
    {
        System.out.println("DEBUG: Stop button pressed");
        //stop song : send data (command) to MC to stop, switch button to play button

        pauseSong();
    }

    private void pauseSong()
    {
        tempSwitcher.showNext(); //or switcher.showPrevious();
        sendData(STARTSTOP);
        System.out.println("DEBUG: " + albumSongs.get(currentPos));
        Toast.makeText(this, R.string.stop_song, Toast.LENGTH_SHORT).show();
    }

    public void antiSkipBtn(View view)
    {
        System.out.println("DEBUG: Anti skip button has been pressed");
        antiSkip();
    }

    private void antiSkip()
    {
        System.out.println("DEBUG: Anti Skip engaged...");
        sendData(ANTISKIP);
        Toast.makeText(this, R.string.anti_skip, Toast.LENGTH_SHORT).show();
    }

    public void homeBtn(View view)
    {
        home();
    }

    private void home()
    {
        System.out.println("DEBUG: ET Phone Home");

        if(tempSwitcher.getDisplayedChild() != 0)
            tempSwitcher.showNext();

        sendData(HOME);
        Toast.makeText(this, R.string.home_stop, Toast.LENGTH_SHORT).show();
    }

    private void sendData(int command)
    {
        byte[] data = String.valueOf(command).getBytes();

        //Sending command to embedded hardware
        LowEnergyBlueTooth.send(btleSingleton.getGattService(),
                btleSingleton.getSERVICE_UUID(),
                btleSingleton.getGatt(),
                data);
    }

    //1 - change speed
            //0 - 33
            //1 - 45
    private void sendData(int command, int data)
    {

        byte[] sendData = null;

        if(command == CHANGESONG)
        {
            sendData = String.valueOf(data).getBytes();
        }
        else if(command == SPEED)
        {
            //1 = change speed, 1 = 45rpms
            if(data == 1)
            {
                sendData = String.valueOf(11).getBytes();
            }
            //1 = change speed, 0 = 33 1/3 rpms
            else
            {
                sendData = String.valueOf(10).getBytes();
            }
        }

        if(sendData != null)
        {
            //Sending command to embedded hardware
            LowEnergyBlueTooth.send(btleSingleton.getGattService(),
                    btleSingleton.getSERVICE_UUID(),
                    btleSingleton.getGatt(),
                    sendData);
        }
        else
        {
            return;
        }
    }

    private void launchMenuActivity(MenuItem item)
    {
        //referenced to the id located on teh main_screen_drawer_view.xml
        int id = item.getItemId();

        if(id == R.id.reset_tonearm)
        {
            sendData(HOME);
        }
        else if(id == R.id.play_song)
        {
            sendData(STARTSTOP);
        }
        else if(id == R.id.back_button)
        {
            this.prevSong();
        }
        else if(id == R.id.forward_button)
        {
            this.nextSong();
        }
        else if(id == R.id.anti_skip_mp)
        {
            sendData(ANTISKIP);
        }
        else if(id == R.id.rpm_45)
        {
            sendData(CHANGE45);
        }
        else if(id == R.id.rpm_33)
        {
            sendData(CHANGE33);
        }
    }

    private void setInitialValues(int pos)
    {
        offset = 33.8;

        System.out.println("DEBUG: Artist " + record.getArtist());
        System.out.println("DEBUG: Artist " + record.getAlbum());


        //TODO: fix the issue
        if(record.getAlbum().trim().equalsIgnoreCase("Lou Gramm"))
        {
            if(record.getArtist().trim().equalsIgnoreCase("Ready Or Not"))
            {

                Song song = songTrackList.get(currentPos);
                String position = song.getPosition();
                System.out.println("DEBUG: Song position " + song.getPosition());
                if(position.length() >= 2 && position.charAt(0) == 'B')
                    spacing = .00536;
                if(position.length() >= 2 && position.charAt(0) == 'A')
                    spacing = .0049;
            }
        }
        else
        {
            spacing = .00125;
        }
    }
}
