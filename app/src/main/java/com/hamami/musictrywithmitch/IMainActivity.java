package com.hamami.musictrywithmitch;

import android.support.v4.media.MediaMetadataCompat;

import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.Models.Song;
import com.hamami.musictrywithmitch.Models.Songs;
import com.hamami.musictrywithmitch.util.MyPreferenceManager;


public interface IMainActivity {


    void playPause();

    void playNext();

    void playPrev();

    MyApplication getMyApplicationInstance();

    void onMediaSelected(String playlistId,MediaMetadataCompat mediaItem,int queuePosition);

    void onAddPlaylistMenuSelected(Songs songSelected);

    void addSongToPlaylist(Songs song,String playlist);

    MyPreferenceManager getMyPreferenceManager();

    void insertToDatabase(Playlist playlist);

    void updateToDatabase(Playlist playlist);

}
