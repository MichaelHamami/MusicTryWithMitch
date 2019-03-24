package com.hamami.musictrywithmitch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.hamami.musictrywithmitch.Models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import static com.hamami.musictrywithmitch.util.FavoriteConstants.FAVORITES_LIST_FILE_PATHS;
import static com.hamami.musictrywithmitch.util.FavoriteConstants.FAVORITES_PLAYLIST_ID;
import static com.hamami.musictrywithmitch.util.FavoriteConstants.FAVORITES_MEDIA_QUEUE_POSITION;

public class FavoritePreferenceManager {


    private static final String TAG = "FavoritePreferenceM";

    private SharedPreferences mPreferences;

    public FavoritePreferenceManager(Context context) {
        this.mPreferences = context.getSharedPreferences("FavoritePreference",Context.MODE_PRIVATE);
    }

    public String getPlaylistId()
    {
        return FAVORITES_PLAYLIST_ID;
    }

    public void savePlaylistId(){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(FAVORITES_PLAYLIST_ID, FAVORITES_PLAYLIST_ID);
        editor.apply();
    }

    public ArrayList<Song> getSongList()
    {
        ArrayList<Song> songList = new ArrayList<>();
         String paths[] = {};
        Set<String> listPathsSongFiles = mPreferences.getStringSet(FAVORITES_LIST_FILE_PATHS, null);
        Log.d(TAG, "getSongList: "+listPathsSongFiles.toString());
        listPathsSongFiles.toArray(paths);
        Log.d(TAG, "getSongList: PathToString: "+paths.toString() + " Length/Size: "+paths.length);

        for(int i=0;i<paths.length;i++)
        {
            File file = new File(paths[i]);
            Song song = new Song(file,file.getName(),getTimeSong(file));
            songList.add(song);
        }

        return songList;
    }
    public void saveSongList()
    {
        SharedPreferences.Editor editor = mPreferences.edit();

        editor.apply();
    }

    public String getTimeSong(File file) {
        // load data file
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(file.getAbsolutePath());

        String time;
        // convert duration to minute:seconds
        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);

        String minutes = String.valueOf(dur / 60000);
        if (seconds.length() == 1) {
            time = "0" + minutes + ":0" + seconds;
        } else {
            time = "0" + minutes + ":" + seconds;
        }
//        Toast.makeText(this,time,Toast.LENGTH_LONG).show();
        // close object
        metaRetriever.release();
        return time;
    }



    public void saveQueuePosition(int position){
        Log.d(TAG, "saveQueuePosition: SAVING QUEUE INDEX: " + position);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(FAVORITES_MEDIA_QUEUE_POSITION, position);
        editor.apply();
    }

    public int getQueuePosition(){
        return mPreferences.getInt(FAVORITES_MEDIA_QUEUE_POSITION, -1);
    }


}
