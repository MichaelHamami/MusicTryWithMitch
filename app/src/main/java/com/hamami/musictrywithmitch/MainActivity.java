package com.hamami.musictrywithmitch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hamami.musictrywithmitch.adapters.ViewPagerAdapter;
import com.hamami.musictrywithmitch.services.MediaService;
import com.hamami.musictrywithmitch.client.MediaBrowserHelper;
import com.hamami.musictrywithmitch.client.MediaBrowserHelperCallback;
import com.hamami.musictrywithmitch.ui.MediaControllerFragment;
import com.hamami.musictrywithmitch.ui.PlaylistFragment;
import com.hamami.musictrywithmitch.util.MyPreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hamami.musictrywithmitch.util.Constants.MEDIA_QUEUE_POSITION;
import static com.hamami.musictrywithmitch.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.hamami.musictrywithmitch.util.Constants.SEEK_BAR_MAX;
import static com.hamami.musictrywithmitch.util.Constants.SEEK_BAR_PROGRESS;

public class MainActivity extends AppCompatActivity implements
        IMainActivity,
        ActivityCompat.OnRequestPermissionsResultCallback,
        MediaBrowserHelperCallback {
    // Tag for debug
    private static final String TAG = "MainActivity";

    // for permission
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    // layout
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter viewPagerAdapter;

    // Songs Vars
    ArrayList<Song> songList = new ArrayList<>();
    ArrayList<File> mySongs = new ArrayList<>();
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Song songToAdd;

      // vars

      private MediaBrowserHelper mMediaBrowserHelper;
      private MyApplication mMyApplication;
      private MyPreferenceManager mMyPrefManager;
      private boolean mIsPlaying;
      private SeekBarBroadcastReceiver mSeekBarBroadcastReceiver;
      private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;
      private boolean mOnAppOpen;
      private boolean mWasConfigurationChanged = false;
      private boolean isNewPlaylist;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabLayout =  findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.main_container);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        verifyPermissions();

//        songList = retriveSongs();
        mySongs = findSongs(Environment.getExternalStorageDirectory());
        for (int i = 0; i < mySongs.size(); i++) {
            Song song = new Song(
                    mySongs.get(i),
                    mySongs.get(i).getName().replace(".mp3",""),
                    getTimeSong(mySongs.get(i))
            );
            songList.add(song);
        }
        addToMediaList(songList);


        mMyApplication = MyApplication.getInstance();
        mMyPrefManager = new MyPreferenceManager(this);

        mMediaBrowserHelper = new MediaBrowserHelper(this, MediaService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);

        ArrayList<Song> songList2 = new ArrayList<>();

        for (int i = 0; i < mySongs.size()-1; i++) {
            Song song = new Song(
                    mySongs.get(i),
                    mySongs.get(i).getName().replace(".mp3",""),
                    getTimeSong(mySongs.get(i))
            );
            songList2.add(song);
        }

        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(songList2,"3Songs"),"3songs");
        viewPagerAdapter.notifyDataSetChanged();

    }



    private void setupViewPager(ViewPager mViewPager) {
        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(songList,"AllMusic"),"AllMusic");
        mViewPager.setAdapter(viewPagerAdapter);
    }
    private void activePlaylistFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, PlaylistFragment.newInstance(songList,"AllMusic")).commit();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWasConfigurationChanged = true;
    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {

        getMediaControllerFragment().getMediaSeekBar().setMediaController(mediaController);

    }
    @Override
    protected void onResume() {
        super.onResume();
        initSeekBarBroadcastReceiver();
        initUpdateUiBroadcastReceiver();
    }
    private void initSeekBarBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update));
        mSeekBarBroadcastReceiver = new SeekBarBroadcastReceiver();
        registerReceiver(mSeekBarBroadcastReceiver,intentFilter);

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mSeekBarBroadcastReceiver != null)
        {
            unregisterReceiver(mSeekBarBroadcastReceiver);
        }
        if(mUpdateUIBroadcastReceiver != null)
        {
            unregisterReceiver(mUpdateUIBroadcastReceiver);
        }
    }

    @Override
    public void playPause()
    {
        Log.d(TAG, "playPause: called");
        if(mOnAppOpen)
        {
            if(mIsPlaying)
            {
                Log.d(TAG, "playPause: we try to pause");
                mMediaBrowserHelper.getTransportControls().pause();
            }
            else
            {
                // play song
                Log.d(TAG, "playPause: we call play song");
                mMediaBrowserHelper.getTransportControls().play();
            }
        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playPause: playlist is not null");
                onMediaSelected(
                getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playPause: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void playNext()
    {
        Log.d(TAG, "playNext: called");
        if(mOnAppOpen)
        {
                Log.d(TAG, "playNext: we try to skip to next");
                mMediaBrowserHelper.getTransportControls().skipToNext();

        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playNext: playlist is not null");
                onMediaSelected(
                        getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playNext: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void playPrev()
    {
        Log.d(TAG, "playPrev: called");
        if(mOnAppOpen)
        {
            Log.d(TAG, "playPrev: we try to skip to previous");
            mMediaBrowserHelper.getTransportControls().skipToPrevious();

        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playPrev: playlist is not null");
                onMediaSelected(
                        getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playPrev: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public MyApplication getMyApplicationInstance() {
        return mMyApplication;
    }

    @Override
    public void onMediaSelected(String playlistId,MediaMetadataCompat mediaItem,int queuePosition)
    {
        if (mediaItem != null)
        {
            Log.d(TAG,"onMediaSelected: Called: "+mediaItem.getDescription().getMediaId());

            String currentPlaylistId = getMyPreferenceManager().getPlaylistId();
            Log.d(TAG, "onMediaSelected: currentPlaylistId is: "+currentPlaylistId +"||| compare with playlistId: "+playlistId);

            Bundle bundle = new Bundle();
            bundle.putInt(MEDIA_QUEUE_POSITION,queuePosition);

            if(playlistId.equals(currentPlaylistId))
            {
                if(mMyApplication.getMediaItems().isEmpty())
                {
                    Log.d(TAG, "onMediaSelected:  the list in myApplication is empty so we subscribe again.");
                    mMediaBrowserHelper.subscribeToNewPlaylist(currentPlaylistId,playlistId);
                }

                Log.d(TAG,"onMediaSelected: its same playlist and not empty: "+playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);
            }
            else
            {
                Log.d(TAG,"onMediaSelected: its new playlist: "+playlistId);
                bundle.putBoolean(QUEUE_NEW_PLAYLIST,true);
                mMediaBrowserHelper.subscribeToNewPlaylist(currentPlaylistId,playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);

            }
            mOnAppOpen = true;
        }
        else
        {
            Log.d(TAG, "onMediaSelected: select something to play");
            Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddPlaylistMenuSelected(Song songSelected)
    {
        Log.d(TAG, "onAddPlaylistMenuSelected: Called with Song: SongName: "+songSelected.getNameSong()+" | Song FilePath: "+songSelected.getFileSong().getAbsolutePath().toString());
        Toast.makeText(this, "add to playlist menu clicked", Toast.LENGTH_SHORT).show();
        songToAdd = songSelected;
        Log.d(TAG, "onAddPlaylistMenuSelected: Song: SongName: "+songToAdd.getNameSong()+" | Song FilePath: "+songToAdd.getFileSong().getAbsolutePath().toString());
        Intent intent = new Intent(this, SelectPlayList.class);
//        intent.putExtra("selected_song",songSelected);
        intent.putStringArrayListExtra("playlistTitles",viewPagerAdapter.getFragmentTitles());
        // Pass tow argument intent and request code
        startActivityForResult(intent,4);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==4){
            Log.d(TAG, "onActivityResult: Called:");
            // code here when they back
            //Check if it is new playlist
            isNewPlaylist = data.getBooleanExtra("isNewPlaylist",false);
            Log.d(TAG, "onActivityResult: the Playlist is new?: " +isNewPlaylist);
            if(isNewPlaylist == true)
            {
                String newPlaylist = data.getStringExtra("newPlaylist");
                addNewPlaylist(newPlaylist,songToAdd);

            }
            // it is selected playlist from our list
            else
            {
                String selectedPlaylist = data.getStringExtra("selectedPlaylist");
                addSongToPlaylist(songToAdd,selectedPlaylist);
            }

        }
    }

    @Override
    public void addSongToPlaylist(Song song, String playlist) {
        Log.d(TAG, "addSongToPlaylist: Called");
        ((PlaylistFragment)(viewPagerAdapter.getItemByTitle(playlist))).addSongToList(song);
    }

    public void addNewPlaylist(String newPlaylist,Song song)
    {
        ArrayList<Song> songNewList = new ArrayList<>();
        songNewList.add(song);
        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(songNewList,newPlaylist),newPlaylist);
        viewPagerAdapter.notifyDataSetChanged();
    }


    @Override
    public MyPreferenceManager getMyPreferenceManager() {
        return mMyPrefManager;
    }

    
    @Override
    protected void onStart() {
        // when the app started
        Log.d(TAG, "onStart: Called");
        super.onStart();
        if (!getMyPreferenceManager().getPlaylistId().equals(""))
        {
            preparedLastPlayedMedia();
        }
        else
        {
            Log.d(TAG, "onStart: else called will do MediaBrowser onStart");
            mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        }
    }

    private void preparedLastPlayedMedia()
    {
        // he get data from firebase to get last media and all media.
        Log.d(TAG, "preparedLastPlayedMedia: Called");

        for(int i = 0; i<songList.size(); i++)
        {
            if(mMediaList.get(i).getDescription().getMediaId().equals(getMyPreferenceManager().getLastPlayedMedia())){
                getMediaControllerFragment().setMediaTitle(mMediaList.get(i));
            }
        }
        onFinishedGettingPreviousSessionData(mMediaList);

    }
    private void onFinishedGettingPreviousSessionData(List<MediaMetadataCompat> mediaItems){
        mMyApplication.setMediaItems(mediaItems);
        mMediaBrowserHelper.onStart(mWasConfigurationChanged);

    }

    @Override
    protected void onStop() {
        super.onStop();
        getMediaControllerFragment().getMediaSeekBar().disconnectController();
        mMediaBrowserHelper.onStop();
    }

    private ArrayList<Song> retriveSongs()
    {
        ArrayList<File> songsFiles = new ArrayList<>();
        songsFiles =  findSongs(Environment.getExternalStorageDirectory());
        ArrayList<Song> songsList = new ArrayList<>();
        for (int i = 0; i < songsFiles.size(); i++) {
            Song song = new Song(
                    songsFiles.get(i),
                    songsFiles.get(i).getName().replaceAll(" .mp3"," "),
                    getTimeSong(songsFiles.get(i))
            );
            songsList.add(song);
        }
        return songsList;
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

    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: Checking Permissions.");


        int permissionExternalMemory = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    STORAGE_PERMISSIONS,
                    1
            );
        }
    }


    private PlaylistFragment getPlaylistFragment()
    {
        PlaylistFragment PlaylistFragment = (PlaylistFragment)getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.fragment_playlist));
        if (PlaylistFragment != null)
        {
            Log.d(TAG, "getPlaylistFragment:  we get the playlistfragment");
            return PlaylistFragment;
        }
        return null;
    }
    private void initUpdateUiBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_update_ui));
        mUpdateUIBroadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(mUpdateUIBroadcastReceiver,intentFilter);

    }
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String mediaID = intent.getStringExtra(getString(R.string.broadcast_new_media_id));
            Log.d(TAG, "onReceive:  media id: "+mediaID);
            if(getPlaylistFragment() != null)
            {
                getPlaylistFragment().updateUI(mMyApplication.getMediaItem(mediaID));
            }


        }
    }

    private class SeekBarBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra(SEEK_BAR_PROGRESS,0);
            long maxProgress = intent.getLongExtra(SEEK_BAR_MAX,0);
            if (!getMediaControllerFragment().getMediaSeekBar().isTracking())
            {
                getMediaControllerFragment().getMediaSeekBar().setProgress((int)seekProgress);
                getMediaControllerFragment().getMediaSeekBar().setMax((int)maxProgress);
            }
        }
    }
    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged: called");
        // Do stuff with new metaData

        if(metadata == null)
        {
            return;
        }
        if (getMediaControllerFragment() != null)
        {
            getMediaControllerFragment().setMediaTitle(metadata);
        }

    }

    @Override
    public void onPlayBackStateChanged(PlaybackStateCompat state)
    {
        Log.d(TAG, "onPlayBackStateChanged: called");
        mIsPlaying = state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING;

        // Update UI
        if (getMediaControllerFragment() != null)
        {
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }
    }


    private MediaControllerFragment getMediaControllerFragment()
    {
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.bottom_media_controller);
        if (mediaControllerFragment != null)
        {
            return mediaControllerFragment;
        }
        return null;

    }

    private void addToMediaList(ArrayList<Song> songsList)
    {
        for (int i=0;i<songsList.size();i++)
        {
            MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                    // title = songName , artist=songTime
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songsList.get(i).getSongLength())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,songsList.get(i).getFileSong().toURI().toString())
                    .build();
            mMediaList.add(media);
        }
    }

}
