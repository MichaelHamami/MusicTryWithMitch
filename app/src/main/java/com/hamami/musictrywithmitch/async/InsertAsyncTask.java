package com.hamami.musictrywithmitch.async;

import android.os.AsyncTask;
import android.util.Log;

import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.persistence.PlaylistDao;

public class InsertAsyncTask extends AsyncTask<Playlist,Void,Void> {

    private static final String TAG = "InsertAsyncTask";

    private PlaylistDao mPlaylistDao;
    public InsertAsyncTask(PlaylistDao dao) {
        mPlaylistDao = dao;
    }

    @Override
    protected Void doInBackground(Playlist... playlists) {
        Log.d(TAG, "doInBackground: thread: " +Thread.currentThread().getName());
        Log.d(TAG, "doInBackground: InsertAsync Called");
        mPlaylistDao.insert(playlists);
        return null;
    }
}
