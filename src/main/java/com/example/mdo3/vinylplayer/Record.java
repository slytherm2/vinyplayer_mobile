package com.example.mdo3.vinylplayer;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class Record {
    private String artist = null;
    private String album = null;

    public Record(String artist, String album)
    {
        this.artist = artist;
        this.album = album;
    }

    public String getAlbum() { return this.album; }
    public String getArtist() { return this.artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
}
