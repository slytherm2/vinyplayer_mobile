package com.example.mdo3.vinylplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class Record implements Parcelable
{
    private String artist = null;
    private String album = null;
    private ArrayList<Song> tracklist = null; //list of songs
    private String filePath = null; //Where image is located
    private String url = null;
    private String rpm = null; //rotations per minute false = 33 1/3, true = 45rpm

    public Record(String artist, String album)
    {
        this.artist = artist;
        this.album = album;
    }

    public Record(String artist, String album, ArrayList<Song> tracklist)
    {
        this.artist = artist;
        this.album = album;
        this.tracklist = tracklist;
    }

    public Record(String artist, String album, ArrayList<Song> tracklist, String url)
    {
        this.artist = artist;
        this.album = album;
        this.tracklist = tracklist;
        this.url = url;
    }

    public Record(String artist, String album, ArrayList<Song> tracklist, String rpm, String filePath)
    {
        this.artist = artist;
        this.album = album;
        this.tracklist = tracklist;
        this.filePath = filePath;
        this.rpm = rpm;
    }


    public Record(Parcel in)
    {
        artist = in.readString();
        album = in.readString();
        tracklist = in.readArrayList(Song.class.getClassLoader());
        filePath = in.readString();
        url = in.readString();
        rpm = in.readString();
    }

    public static final Creator<Record> CREATOR = new Creator<Record>() {
        @Override
        public Record createFromParcel(Parcel in) {
            return new Record(in);
        }

        @Override
        public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    // parcelable override methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeList(tracklist);
        dest.writeString(filePath);
        dest.writeString(url);
        dest.writeString(rpm);
    }

    // setter & getter methods
    public String getAlbum() { return this.album; }
    public String getArtist() { return this.artist; }
    public ArrayList<Song> getTracklist() { return this.tracklist; }
    public String getFilePath() { return this.filePath; }
    public String getRpm() { return this.rpm; }
    public String getUrl() {return this.url; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setTrackList(ArrayList<Song> tracklist) { this.tracklist = tracklist; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setRpm(String rpm) { this.rpm = rpm; }
    public void setUrl(String url) { this.url = url; }

    // regular methods
    public void addSong(Song newSong) { this.tracklist.add(newSong); }
    public void addSongAtPosition(Song newSong, int position) { this.tracklist.add(position, newSong); }

}
