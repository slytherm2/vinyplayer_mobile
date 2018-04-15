package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdo3.vinylplayer.asyncTask.DownloadImageTask;

import java.util.ArrayList;

public class RecordInfo extends AppCompatActivity
{
    private TextView album_TextView;
    private TextView artist_TextView;
    private ImageView cover_ImageView;
    private RecyclerView tracklist_RecyclerView;
    private RecyclerView.Adapter adapter;
    private String album;
    private String artist;
    private ArrayList<Song> tracklist;
    private Record record;
    private Toolbar mTopToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);

        //adding tool bar with back arrow to go back to activity
        //it goes to the activity listed in the android manifest
        mTopToolbar = (Toolbar) findViewById(R.id.record_info_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // get views
        album_TextView = (TextView) findViewById(R.id.record_info_album);
        artist_TextView = (TextView) findViewById(R.id.record_info_artist);
        cover_ImageView = (ImageView) findViewById(R.id.record_info_cover);
        tracklist_RecyclerView = (RecyclerView) findViewById(R.id.record_info_tracklist);


        tracklist_RecyclerView.setHasFixedSize(true);
        tracklist_RecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // get record info from intent
        Bundle extras = getIntent().getExtras();
        if (extras == null ) {
            return;
        }
        record = (Record) extras.getParcelable("record");

       AsyncTaskFactory factory = new AsyncTaskFactory();
       DownloadImageTask downloadTask = (DownloadImageTask) factory.generateAsyncTask("Download");
        try
        {
            String[] params = {record.getUrl()};
            cover_ImageView.setImageBitmap(downloadTask.execute(params).get());
        }
        catch (Exception e)
        {
            Log.d("Exception", e.getMessage());
        }

        // set content
        album_TextView.setText(record.getAlbum());
        artist_TextView.setText(record.getArtist());
        adapter = new SongAdapter(this, record.getTracklist());
        tracklist_RecyclerView.setAdapter(adapter);
    }

    public void addToCatalogBtn(View view)
    {
        //UserId
        //Artist, album, Image URI, RPM speed (false = 33 1/3, true = 45rpm)
        //Song name, duration

        System.out.println("DEBUG: Button pressed");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<String> records = new ArrayList<>();
        String temp = preferences.getString(this.getResources().getString(R.string.label_email), null);
        if(temp == null)
        {
            Toast.makeText(this, R.string.fail_to_add, Toast.LENGTH_SHORT).show();
            return;
        }
        records.add(temp); //Email Tag
        records.add(record.getArtist());
        records.add(record.getAlbum());

        System.out.println("DEBUG: " + record.getUrl());
        records.add(record.getUrl().toString());

        String rpm = record.getRpm();
        if(rpm == null)
            records.add("false");
        else
            records.add(rpm);


        for(int i = 0; i < record.getTracklist().size(); i++)
        {
            Song songObj = record.getTracklist().get(i);
            records.add(songObj.getTitle());
            records.add(songObj.getDuration());
        }

        //Save the information to default xml location
        //under the tag "useremail" "SearchCat"
        Utils.saveInformationSearch(records);

        Intent intent = new Intent(this, MusicPlayer.class);
        intent.putExtra(this.getResources().getString(R.string.record), record);
        this.startActivity(intent);
    }
}
