package com.example.mdo3.vinylplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class Record implements Parcelable{
    private String artist = null;
    private String album = null;
    private ArrayList<Song> tracklist;

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

    public Record(Parcel in)
    {
        artist = in.readString();
        album = in.readString();
        tracklist = in.readArrayList(Song.class.getClassLoader());
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
    }

    // setter & getter methods
    public String getAlbum() { return this.album; }
    public String getArtist() { return this.artist; }
    public ArrayList<Song> getTracklist() { return this.tracklist; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setTrackList(ArrayList<Song> tracklist) { this.tracklist = tracklist; }

    // regular methods
    public void addSong(Song newSong) { this.tracklist.add(newSong); }
    public void addSongAtPosition(Song newSong, int position) { this.tracklist.add(position, newSong); }

}
