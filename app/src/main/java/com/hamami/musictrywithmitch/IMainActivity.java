package com.hamami.musictrywithmitch;

import android.support.v4.media.MediaMetadataCompat;

import com.hamami.musictrywithmitch.util.MyPreferenceManager;


public interface IMainActivity {


//    void setActionBarTitle(String title);

    void playPause();

    MyApplication getMyApplication();

    void onMediaSelected(String playlistId,MediaMetadataCompat mediaItem,int queuePosition);

    MyPreferenceManager getMyPreferenceManager();

}
