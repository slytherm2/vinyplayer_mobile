package com.example.mdo3.vinylplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Duration;

/**
 * Created by Jr on 3/29/2018.
 */

public class Song implements Parcelable{
    private String title = null;
    private String duration = null;
    private String position = null;

    public Song(String title, String duration)
    {
        this.title = title;
        this.duration = duration;
        return;
    }

    public Song(String title)
    {
        this.title = title;
        return;
    }

    public Song(String title, String  position, String duration)
    {
        this.title = title;
        this.duration = duration;
        this.position = position;
        return;
    }

    protected Song(Parcel in) {
        title = in.readString();
        duration = in.readString();
        position = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    // parcelable override methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeString(position);
    }

    // setter & getter methods
    public String getDuration() { return this.duration; }
    public String getPosition() { return this.position; }
    public String getTitle() { return this.title; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setPosition(String position) { this.position = position; }
    public void setTitle(String title) {this.title = title; }

}
