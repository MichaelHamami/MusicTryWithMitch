package com.hamami.musictrywithmitch.persistence;

import android.content.Context;

import com.hamami.musictrywithmitch.Models.Playlist;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Playlist.class}, version = 1)

@TypeConverters({Converter.class})

public abstract class PlaylistDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "playlists10_db";

    private static PlaylistDatabase instance;

    static PlaylistDatabase getInstance(final Context context)
    {
        if (instance == null)
        {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PlaylistDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        return instance;
    }

    public abstract PlaylistDao getPlaylistDao();
}
