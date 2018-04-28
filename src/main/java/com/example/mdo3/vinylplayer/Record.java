package com.example.mdo3.vinylplayer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
    private String url = null; //the http url for the image aka thumbnail
    private String rpm = null; //rotations per minute false = 33 1/3, true = 45rpm
    private String year = null;
    private String albumId = null;

    public Record(String artist, String album, String url, String year, String id)
    {
        this.artist = artist;
        this.album = album;
        this.url = url;
        this.year = year;
        this.albumId = id;
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

    //Params need to be of size 5
    //String[] params = {artist, album, year, url, albumId};
    public Record(ArrayList<Song> tracklist, String[] params)
    {
        this.tracklist = tracklist;

        if(params.length == 5)
        {
            this.artist = params[0];
            this.album = params[1];
            this.year = params[2];
            this.url = params[3];
            this.albumId = params[4];
        }
        else
        {
            Log.d("Error", "Unable to set parameters, size of the parameters is not equal to 6");
        }
    }

    public Record(String artist, String album, ArrayList<Song> tracklist, String rpm, String filePath)
    {
        this.artist = artist;
        this.album = album;
        this.tracklist = tracklist;
        this.filePath = filePath;
        this.rpm = rpm;
    }

    public Record(String artist, String album, ArrayList<Song> tracklist, String rpm, String path, int flag)
    {
        this.artist = artist;
        this.album = album;
        this.tracklist = tracklist;
        if(flag == MainScreen.SPLITURL)
        {
            this.url = path;
        }
        else if(flag == MainScreen.SPLITPATH)
        {
            this.filePath = path;
        }
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
        year = in.readString();
        albumId = in.readString();
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
        dest.writeString(year);
        dest.writeString(albumId);
    }

    // setter & getter methods
    public String getAlbum() { return this.album; }
    public String getArtist() { return this.artist; }
    public ArrayList<Song> getTracklist() { return this.tracklist; }
    public String getFilePath() { return this.filePath; }
    public String getRpm() { return this.rpm; }
    public String getUrl() {return this.url; }
    public String getYear() {return this.year; }
    public String getId() { return this.albumId; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setTrackList(ArrayList<Song> tracklist) { this.tracklist = tracklist; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setRpm(String rpm) { this.rpm = rpm; }
    public void setUrl(String url) { this.url = url; }
    public void setYear(String year) {this.year = year; }
    public void setId(String id) { this.albumId = id; }

    // regular methods
    public void addSong(Song newSong) { this.tracklist.add(newSong); }
    public void addSongAtPosition(Song newSong, int position) { this.tracklist.add(position, newSong); }

}
