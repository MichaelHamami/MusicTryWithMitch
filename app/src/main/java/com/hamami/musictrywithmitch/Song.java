package com.hamami.musictrywithmitch;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Song implements Parcelable {
    private File fileSong;
    private String nameSong;
    private String SongLength;

    //constructor initializing values
    public Song(File fileSong, String nameSong, String SongLength) {
        this.fileSong = fileSong;
        this.nameSong = nameSong;
        this.SongLength = SongLength;
    }

    protected Song(Parcel in) {
        nameSong = in.readString();
        SongLength = in.readString();
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

    //getters
    public String getNameSong() {
        return nameSong;
    }

    public String getSongLength() {
        return SongLength;
    }
    public File getFileSong(){
        return fileSong;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nameSong);
        dest.writeString(SongLength);
    }
}
