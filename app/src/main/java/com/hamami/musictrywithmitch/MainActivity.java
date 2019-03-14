package com.hamami.musictrywithmitch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

public class MainActivity extends AppCompatActivity implements IMainActivity,ActivityCompat.OnRequestPermissionsResultCallback, MediaBrowserHelperCallback {
    // Tag for debug
    private static final String TAG = "MainActivity";


    // for permission
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private View mLayout;

    ArrayList<Song> songList = new ArrayList<>();
    ArrayList<File> mySongs = new ArrayList<>();
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();

      // vars

      private MediaBrowserHelper mMediaBrowserHelper;
      private MyApplication mMyApplication;
      private MyPreferenceManager mMyPrefManager;
      private boolean mIsPlaying;
      private SeekBarBroadcastReceiver mSeekBarBroadcastReceiver;
      private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;
      private boolean mOnAppOpen;
      private boolean mWasConfigurationChanged = false;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        showReadPreview();

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



        activePlaylistFragment();
//        Toast.makeText(this,"onCreateFine",Toast.LENGTH_LONG).show();

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
    public MyPreferenceManager getMyPreferenceManager() {
        return mMyPrefManager;
    }

    
    @Override
    protected void onStart() {
        // when the app started
        super.onStart();
        // after the onCreateFinished
//        mMediaBrowserHelper.onStart(mWasConfigurationChanged);

        // we try this

        if (!getMyPreferenceManager().getPlaylistId().equals(""))
        {
            preparedLastPlayedMedia();
        }
        else
        {
            mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        }
    }

    private void preparedLastPlayedMedia()
    {
        // he get data from firebase to get last media and all media.

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

    private void activePlaylistFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, PlaylistFragment.newInstance(songList)).commit();
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

    private void requestReadPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout,"can you approve?" ,
                    Snackbar.LENGTH_INDEFINITE).setAction("okey", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, "Read Storage unvailable", Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    } // checked

    private void showReadPreview() {
        // BEGIN_INCLUDE(startCamera)
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
//            Toast.makeText(this,"Granted checked",Toast.LENGTH_LONG).show();
//            Toast.makeText(this,"Do Something",Toast.LENGTH_LONG).show();
            mySongs = findSongs(Environment.getExternalStorageDirectory());
        } else {
            // Permission is missing and must be requested.
            requestReadPermission();
        }
        // END_INCLUDE(startCamera)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
//                Toast.makeText(this,"Granted",Toast.LENGTH_LONG).show();
//                Toast.makeText(this,"Do Something",Toast.LENGTH_LONG).show();
                mySongs = findSongs(Environment.getExternalStorageDirectory());


            } else {
                // Permission request was denied.
                Toast.makeText(this,"not approved",Toast.LENGTH_LONG).show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
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
