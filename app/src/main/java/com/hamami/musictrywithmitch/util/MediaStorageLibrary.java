package com.hamami.musictrywithmitch.util;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.hamami.musictrywithmitch.Models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class MediaStorageLibrary {

    private static final String TAG = "MediaStorageLibrary";


    public TreeMap<String, MediaMetadataCompat> mMediaMap = new TreeMap<>();
    public List<MediaMetadataCompat> mMediaList = new ArrayList<>();
    public  ArrayList<Song> songsList = new ArrayList<>();
    MediaMetadataCompat[] mMediaStorageLibrary;





    public MediaStorageLibrary() {
        ArrayList<File> mySongs = new ArrayList<>();
        mySongs = findSongs(Environment.getExternalStorageDirectory());
        for (int i = 0; i < mySongs.size(); i++) {
            Song song = new Song(
                    mySongs.get(i),
                    mySongs.get(i).getName().replace(".mp3",""),
                    getTimeSong(mySongs.get(i))
            );
            songsList.add(song);
        }
        initMediaStorageLibrary();
        initMap();

    }
    public ArrayList<File> findSongs(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3")) {
                    al.add(singleFile);
                }
            }
        }
        return al;
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

    private void initMap(){
        for(MediaMetadataCompat media: mMediaStorageLibrary){
            String mediaId = media.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            mMediaMap.put(mediaId, media);
            mMediaList.add(media);
        }
    }

    public  List<MediaBrowserCompat.MediaItem> getPlaylistMedia(Set<String> mediaIds) {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        // VERY INEFFICIENT WAY TO DO THIS (BUT I NEED TO BECAUSE THE DATA STRUCTURE ARE NOT IDEAL)
        // RETRIEVING DATA FROM A SERVER WOULD NOT POSE THIS ISSUE
        for(String id: mediaIds){
            for (MediaMetadataCompat metadata : mMediaStorageLibrary) {
                if(id.equals(metadata.getDescription().getMediaId())){
                    result.add(
                            new MediaBrowserCompat.MediaItem(
                                    metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                }
            }
        }


        return result;
    }

    public  List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : mMediaStorageLibrary) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public TreeMap<String, MediaMetadataCompat> getTreeMap(){
        return mMediaMap;
    }

    public MediaMetadataCompat[] getMediaLibrary(){
        return mMediaStorageLibrary;
    }
    public void initMediaStorageLibrary() {
        mMediaStorageLibrary = new MediaMetadataCompat[]{new MediaMetadataCompat.Builder()
                //Thats works
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11111")
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Jim Wilson")
//                .putString(
//                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
//                        "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg")
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "CodingWithMitch Podcast #1 - Jim Wilson")
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
//                        "http://content.blubrry.com/codingwithmitch/Interview_audio_online-audio-converter.com_.mp3")
//                .build()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(0).getNameSong())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(0).getNameSong())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,songsList.get(0).getFileSong().toURI().toString())
            .build()
        ,
                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11112")
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Justin Mitchel")
                                .putString(
                                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                        "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg")
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                                        "CodingWithMitch Podcast #2 - Justin Mitchel")
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                        "http://content.blubrry.com/codingwithmitch/Justin_Mitchel_interview_audio_online-audio-converter.com_.mp3")
                                .build()
                ,

                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11113")
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Matt Tran")
                                .putString(
                                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                        "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg")
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                                        "CodingWithMitch Podcast #3 - Matt Tran")
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                        "http://content.blubrry.com/codingwithmitch/Matt_Tran_Interview_online-audio-converter.com_.mp3")
                                .build()
                ,
                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11114")
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian")
                                .putString(
                                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                        "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg")
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                                        "Some Random Test Audio")
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                        "https://s3.amazonaws.com/codingwithmitch-static-and-media/pluralsight/Processes+and+Threads/audio+test+1+(online-audio-converter.com).mp3")
                                .build()
                };
    }

}
