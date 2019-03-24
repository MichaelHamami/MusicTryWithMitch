package com.hamami.musictrywithmitch.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hamami.musictrywithmitch.persistence.Converter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "playlists")
public class Playlist implements Parcelable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @NonNull
    @ColumnInfo(name = "songs")
    @TypeConverters(Converter.class)
    private ArrayList<Songs> songs;

    public Playlist(String title,ArrayList<Songs> songs) {
        this.title = title;
        this.songs = songs;
    }

    @Ignore
    public Playlist() {

    }

    protected Playlist(Parcel in) {
        title = in.readString();
        songs = in.createTypedArrayList(Songs.CREATOR);
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Songs> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Songs> songs) {
        this.songs = songs;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "title='" + title + '\'' +
                ", songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(songs);
    }
}
