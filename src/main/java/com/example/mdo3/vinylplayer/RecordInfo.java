package com.example.mdo3.vinylplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordInfo extends AppCompatActivity {
    private TextView album_TextView;
    private TextView artist_TextView;
    private ImageView cover_ImageView;
    private RecyclerView tracklist_RecyclerView;
    private RecyclerView.Adapter adapter;
    private String album;
    private String artist;
    private ArrayList<Song> tracklist;
    private Record record;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);

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

        // set content
        album_TextView.setText(record.getAlbum());
        artist_TextView.setText(record.getArtist());
        adapter = new SongAdapter(this, record.getTracklist());
        tracklist_RecyclerView.setAdapter(adapter);
    }
}
