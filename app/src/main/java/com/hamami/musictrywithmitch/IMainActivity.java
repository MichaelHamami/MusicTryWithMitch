package com.hamami.musictrywithmitch;

import android.support.v4.media.MediaMetadataCompat;

import com.hamami.musictrywithmitch.util.MyPreferenceManager;

import java.util.ArrayList;


public interface IMainActivity {


//    void setActionBarTitle(String title);

    void playPause();

    void playNext();

    void playPrev();

    MyApplication getMyApplicationInstance();

    void onMediaSelected(String playlistId,MediaMetadataCompat mediaItem,int queuePosition);

    MyPreferenceManager getMyPreferenceManager();

}
