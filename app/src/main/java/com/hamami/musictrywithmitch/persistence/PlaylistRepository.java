package com.hamami.musictrywithmitch.persistence;

import android.content.Context;

import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.async.DeleteAsyncTask;
import com.hamami.musictrywithmitch.async.InsertAsyncTask;
import com.hamami.musictrywithmitch.async.UpdateAsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class PlaylistRepository {

    private PlaylistDatabase mPlaylistDatabase;

    public PlaylistRepository(Context context) {
        mPlaylistDatabase = PlaylistDatabase.getInstance(context);
    }

    public void insertPlaylistTask(Playlist playlist)
    {
        new InsertAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }
    public void updatePlaylist(Playlist playlist)
    {
        new UpdateAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }

    public LiveData<List<Playlist>> retrievePlaylistsTask()
    {
        return mPlaylistDatabase.getPlaylistDao().getPlaylists();
    }

    public void deletePlaylist(Playlist playlist)
    {
        new DeleteAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }
}
