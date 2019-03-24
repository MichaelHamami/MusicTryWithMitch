package com.hamami.musictrywithmitch.async;

import android.os.AsyncTask;
import android.util.Log;

import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.persistence.PlaylistDao;

public class DeleteAsyncTask extends AsyncTask<Playlist,Void,Void> {

    private static final String TAG = "InsertAsyncTask";

    private PlaylistDao mPlaylistDao;
    public DeleteAsyncTask(PlaylistDao dao) {
        mPlaylistDao = dao;
    }

    @Override
    protected Void doInBackground(Playlist... playlists) {
        Log.d(TAG, "doInBackground: thread: " +Thread.currentThread().getName());
        Log.d(TAG, "doInBackground: Delete Async Called");
        mPlaylistDao.delete(playlists);
        return null;
    }
}
