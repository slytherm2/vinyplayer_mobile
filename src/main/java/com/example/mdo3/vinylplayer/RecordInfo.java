package com.example.mdo3.vinylplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.mdo3.vinylplayer.asyncTask.AddAlbumTask;
import com.example.mdo3.vinylplayer.asyncTask.DownloadImageTask;

import org.apache.commons.lang3.concurrent.ConcurrentException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
    private String year;
    private String albumId;
    private String url;


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
            if(record == null)
                return;

            String[] params = {record.getUrl()};
            Bitmap bitmap = downloadTask.execute(params).get();

            if(bitmap != null)
                cover_ImageView.setImageBitmap(bitmap);
            else
                cover_ImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.warp_150));
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
        artist = record.getArtist();
        album = record.getAlbum();
        year = record.getYear();
        albumId = record.getId();
        url = record.getUrl();
    }

    public void addToCatalogBtn(View view)
    {
        addToCatalog();
    }

    private void addToCatalog()
    {
        System.out.println("DEBUG: Button pressed");
        Boolean result = false;
        //Save record information to device
        //UserId
        //Artist, album, Image URI, RPM speed (false = 33 1/3, true = 45rpm)
        //Song name, duration
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<String> records = Utils.prepareRecord(this, record);

        //Save the information to default xml location
        //under the tag "useremail" "SearchCat"
        if(records != null)
            result = Utils.saveInformationSearch(this, records);
        else
            Toast.makeText(this, R.string.fail_to_add, Toast.LENGTH_SHORT).show();

        System.out.println("DEBUG: Added to Catalog...Adding to database");

        //Save information to database
        //String[] params = {artist, album, year, url, albumId};
        String[] params = {albumId};

        //automatically enable bluetooth if available
        if(result)
        {
            Thread t1 = new Thread(new Runnable()
            {
                public void run()
                {
                    try {
                        AsyncTaskFactory factory = new AsyncTaskFactory();
                        AddAlbumTask addAlbumTask = (AddAlbumTask) factory.generateAsyncTask("AddAlbum",
                                RecordInfo.this);
                        Boolean result = (Boolean) addAlbumTask.execute(params).get();

                        System.out.println("DEBUG: " + result);
                    }
                    catch (InterruptedException e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                    catch (ExecutionException e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                }
            });
            t1.start();

            System.out.println("DEBUG: Complete");

            Intent intent = new Intent(this, MusicPlayer.class);
            intent.putExtra(this.getResources().getString(R.string.record), record);
            this.startActivity(intent);
        }
        else
        {
            Toast.makeText(this, R.string.duplicate_record, Toast.LENGTH_SHORT).show();
        }
    }
}
