package com.hamami.musictrywithmitch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.Models.Songs;
import com.hamami.musictrywithmitch.adapters.ViewPagerAdapter;
import com.hamami.musictrywithmitch.persistence.PlaylistRepository;
import com.hamami.musictrywithmitch.services.MediaService;
import com.hamami.musictrywithmitch.client.MediaBrowserHelper;
import com.hamami.musictrywithmitch.client.MediaBrowserHelperCallback;
import com.hamami.musictrywithmitch.ui.MediaControllerFragment;
import com.hamami.musictrywithmitch.ui.PlaylistFragment;
import com.hamami.musictrywithmitch.ui.QueueFragment;
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
    ArrayList<Songs> songList = new ArrayList<>();

    ArrayList<File> mySongs = new ArrayList<>();
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Songs songToAdd;

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
      private boolean mOnStartCalled;

      // Repository object
      private PlaylistRepository mPlaylistRepository;
      private ArrayList<Playlist> mPlaylists;
      private Button button;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabLayout =  findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.main_container);

        mPlaylists = new ArrayList<>();


        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mPlaylistRepository = new PlaylistRepository(this);



        verifyPermissions();

        new GetDataTask().execute();

        final Observer <List<Playlist>> playlistObserver = new Observer<List<Playlist>>() {
            @Override
            public void onChanged(List<Playlist> playlists)
            {
                Log.d(TAG, "onChanged: called LiveData Work : FromDataBase");
                mPlaylists.clear();
                mPlaylists.addAll(playlists);
                if(mPlaylists.size() != viewPagerAdapter.getCount() || mPlaylists.size() == 0 )
                {
                    addTheFragmentsFromDataBase();
                }
            }
        };
        mPlaylistRepository.retrievePlaylistsTask().observe(this,playlistObserver);
        mPlaylistRepository.retrievePlaylistsTask();


        try {
            Log.d(TAG, "onCreate: we start thread now:");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mMyApplication = MyApplication.getInstance();
        mMyPrefManager = new MyPreferenceManager(this);

        mMediaBrowserHelper = new MediaBrowserHelper(this, MediaService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);

        mViewPager.setAdapter(viewPagerAdapter);
        setupViewPager();
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void addTheFragmentsFromDataBase()
    {
        boolean foundTitle = false;
        Log.d(TAG, "addTheFragmentsFromDataBase: We add playlist's  from Database");
        for(int i = 0; i < mPlaylists.size();i++)
        {
            for(int j=0; j<viewPagerAdapter.getFragmentTitles().size(); j++)
            {
                if(mPlaylists.get(i).getTitle().equals(viewPagerAdapter.getFragmentTitles().get(j)))
                {
                    foundTitle = true;
                }

            }
            if(foundTitle != true)
            {
                viewPagerAdapter.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
            }
            foundTitle = false;
        }
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void setupViewPager() {

        if(mPlaylists.size() != 0)
        {
            boolean foundTitle = false;
            Log.d(TAG, "setupViewPager: We get playlist's from Database Size is:"+mPlaylists.size());
            for(int i = 0; i < mPlaylists.size();i++)
            {
//               for(int j=0; j<viewPagerAdapter.getFragmentTitles().size(); j++)
//               {
//                   if(mPlaylists.get(i).getTitle().equals(viewPagerAdapter.getFragmentTitles().get(j)))
//                   {
//                       foundTitle = true;
//                   }
//
//               }
//               if(foundTitle != true)
//               {
//                   viewPagerAdapter.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
//                   viewPagerAdapter.notifyDataSetChanged();
//               }
//                foundTitle = false;
                viewPagerAdapter.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
            }
            viewPagerAdapter.notifyDataSetChanged();
        }
        else
        {
            Log.d(TAG, "setupViewPager: We get playlist from Storage");
            Playlist playlistFromStorage = retrivePlaylistFromStorage();
            viewPagerAdapter.addFragment(PlaylistFragment.newInstance(playlistFromStorage,false),playlistFromStorage.getTitle());
            ArrayList<Songs> sonlistinu = new ArrayList<>();
            sonlistinu.add(playlistFromStorage.getSongs().get(0));
            viewPagerAdapter.addFragment(PlaylistFragment.newInstance(new Playlist("Favorite",sonlistinu),false),"Favorite");
            viewPagerAdapter.notifyDataSetChanged();
        }
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

    public void  removeSongFromQueueList(MediaMetadataCompat mediaId)
    {
        Log.d(TAG, "removeSongFromQueueList: called");
        mMyApplication.removeSongFromListMedia(mediaId);
        mMediaBrowserHelper.removeQueueItemFromPlaylist(mediaId);

    }

    @Override
    public void removePlaylistFragment(Playlist playlist)
    {
        Log.d(TAG, "removePlaylistFragment: we trying to remove");
        PlaylistFragment playlistFragment = (PlaylistFragment) viewPagerAdapter.getFragments().get(viewPagerAdapter.getItemPositionByTitle(playlist.getTitle()));
        Log.d(TAG, "removePlaylistFragment: the fragment is? :"+playlistFragment);
        viewPagerAdapter.removeFragment(playlistFragment,playlist.getTitle());
        viewPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
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
    public void onAddPlaylistMenuSelected(Songs songSelected)
    {
//        Log.d(TAG, "onAddPlaylistMenuSelected: Called with Song: SongName: "+songSelected.getNameSong()+" | Song FilePath: "+songSelected.getFileSong().getAbsolutePath().toString());
        Log.d(TAG, "onAddPlaylistMenuSelected: Called with Song: SongName: "+songSelected.getNameSong()+" | Song FilePath: "+songSelected.getFileSong().toString());
        Toast.makeText(this, "add to playlist menu clicked", Toast.LENGTH_SHORT).show();
        songToAdd = songSelected;
//        Log.d(TAG, "onAddPlaylistMenuSelected: Song: SongName: "+songToAdd.getNameSong()+" | Song FilePath: "+songToAdd.getFileSong().getAbsolutePath().toString());
        Log.d(TAG, "onAddPlaylistMenuSelected: Song: SongName: "+songToAdd.getNameSong()+" | Song FilePath: "+songToAdd.getFileSong().toString());
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
    public void addSongToPlaylist(Songs song, String playlistTitle) {
        Log.d(TAG, "addSongToPlaylist: Called");
        if(playlistTitle.equals("Queue"))
        {
            Log.d(TAG, "addSongToPlaylist: we try add to queue list");
            if(viewPagerAdapter.getItemPositionByTitle(playlistTitle) != -1)
            {
                ((QueueFragment)(viewPagerAdapter.getItemByTitle(playlistTitle))).addSongToList(song);
            }
            else
            {
                ArrayList<Songs> newSongList = new ArrayList<>();
                newSongList.add(song);
                viewPagerAdapter.addFragment(QueueFragment.newInstance(new Playlist(playlistTitle,newSongList)),playlistTitle);
                viewPagerAdapter.notifyDataSetChanged();
            }

        }
        else
        {
            ((PlaylistFragment)(viewPagerAdapter.getItemByTitle(playlistTitle))).addSongToList(song);
        }

        int position = viewPagerAdapter.getItemPositionByTitle(playlistTitle);
        mViewPager.setCurrentItem(position);
    }


    public void addNewPlaylist(String newPlaylist,Songs song)
    {
        ArrayList<Songs> songNewList = new ArrayList<>();
        songNewList.add(song);
//        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(songNewList,newPlaylist),newPlaylist);
        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(new Playlist(newPlaylist,songNewList),false),newPlaylist);
        viewPagerAdapter.notifyDataSetChanged();
        int position = viewPagerAdapter.getItemPositionByTitle(newPlaylist);
        mViewPager.setCurrentItem(position);
    }


    @Override
    public MyPreferenceManager getMyPreferenceManager() {
        return mMyPrefManager;
    }

    @Override
    public void insertToDatabase(Playlist playlist)
    {
        ArrayList<String> playlistTitles = new ArrayList<>();
//        playlistTitles.addAll(mPlaylistRepository.getPlaylistTitles());
        Log.d(TAG, "savePlaylistToDatabase: we try to save the playlist to the database");
        Log.d(TAG, "savePlaylistToDatabase: Title: "+playlist.getTitle()+" Songs size: "+playlist.getSongs().size());
        if( isThisNewPlaylist(playlist,playlistTitles) == true)
        {
            Log.d(TAG, "insertToDatabase: we insert new Playlist");
            mPlaylistRepository.insertPlaylistTask(playlist);
        }
        else
        {
            Log.d(TAG, "insertToDatabase: this playlist :"+playlist.getTitle() +" are already in database");
        }

    }

    @Override
    public void updateToDatabase(Playlist playlist)
    {
        mPlaylistRepository.updatePlaylistTask(playlist);
    }

    @Override
    public void removePlaylistFromDatabase(Playlist playlist) 
    {
        Log.d(TAG, "removePlaylistFromDatabase: we trying to remove");
        PlaylistFragment playlistFragment = (PlaylistFragment) viewPagerAdapter.getFragments().get(viewPagerAdapter.getItemPositionByTitle(playlist.getTitle()));
        Log.d(TAG, "removePlaylistFromDatabase: the fragment is? :"+playlistFragment);
        mPlaylistRepository.deletePlaylist(playlist);
        viewPagerAdapter.removeFragment(playlistFragment,playlist.getTitle());
        viewPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
        
//        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(new Playlist(newPlaylist,songNewList),false),newPlaylist);
//        viewPagerAdapter.notifyDataSetChanged();
//        int position = viewPagerAdapter.getItemPositionByTitle(newPlaylist);
//        mViewPager.setCurrentItem(position);
        /// secound trysddadsa
    }


    @Override
    protected void onStart() {
        // when the app started
        Log.d(TAG, "onStart: Called");
        super.onStart();
        Log.d(TAG, "onStart: Called after super");
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
        Log.d(TAG, "preparedLastPlayedMedia: Called");

        String lastPlaylist = getMyPreferenceManager().getPlaylistId();
        Log.d(TAG, "preparedLastPlayedMedia: lastPlayed is: "+lastPlaylist);
        int position = -1;
        for ( int i =0; i<mPlaylists.size(); i++)
        {
            if(lastPlaylist.equals(mPlaylists.get(i).getTitle()))
            {
               position = i;
                break;
            }
        }
        Log.d(TAG, "preparedLastPlayedMedia: the positin is: "+position);

        if(position != -1)
        {
            songList = mPlaylists.get(position).getSongs();
            Log.d(TAG, "preparedLastPlayedMedia: songList size: "+songList.size());
            addToMediaList(songList);
            onFinishedGettingPreviousSessionData(mMediaList);
        }
        else
        {
            Log.d(TAG, "preparedLastPlayedMedia: else called will do MediaBrowser onStart");
            mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        }

//        Log.d(TAG, "preparedLastPlayedMedia: Size of songlist:  " +songList.size() + " MediaList size: "+mMediaList.size());

//        for(int i = 0; i<songList.size(); i++)
//        {
//            if(mMediaList.get(i).getDescription().getMediaId().equals(getMyPreferenceManager().getLastPlayedMedia())){
//                getMediaControllerFragment().setMediaTitle(mMediaList.get(i));
//            }
//        }
//        onFinishedGettingPreviousSessionData(mMediaList);

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

    private Playlist retrivePlaylistFromStorage()
    {
        ArrayList<File> songsFiles = new ArrayList<>();
        songsFiles =  findSongs(Environment.getExternalStorageDirectory());
        ArrayList<Songs> songsList = new ArrayList<>();
        for (int i = 0; i < songsFiles.size(); i++) {
            Songs song = new Songs(
                    songsFiles.get(i).getAbsolutePath(),
                    songsFiles.get(i).getName().replaceAll(" .mp3"," "),
                    getTimeSong(songsFiles.get(i))
            );
            songsList.add(song);
        }
        Playlist playlist = new Playlist("AllMusic",songsList);
        return playlist;
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
            Log.d(TAG, "getPlaylistFragment:  we get the playlistFragment");
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

    private void addToMediaList(ArrayList<Songs> songsList)
    {
        mMediaList.clear();
        for (int i=0;i<songsList.size();i++)
        {
            // for the new Songs Type
            File file = new File(songsList.get(i).getFileSong());
            MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                    .build();
            mMediaList.add(media);
        }
    }

    public boolean isThisNewPlaylist(Playlist playlist,ArrayList<String> titles)
    {
        Log.d(TAG, "isThisNewPlaylist: for checks size:" +mPlaylists.size());
        for(int i = 0; i<mPlaylists.size(); i++)
        {
            if(mPlaylists.get(i).getTitle().equals(playlist.getTitle()))
            {
                return false;
            }
        }
        return true;
//        for(int i = 0; i<titles.size(); i++)
//        {
//            if(titles.get(i).equals(playlist.getTitle()))
//            {
//                return false;
//            }
//        }
//        return true;
    }

    /**
     * Creating Get Data Task for Getting Data From Web
     */
    public  class  GetDataTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Progress Dialog for User Interaction

//            JSONObject jsonObject = JSONParser.getDataFromWeb();

//            x=list.size();

            x=mPlaylists.size();

            if(x==0)
                jIndex=0;
            else
                jIndex=x;

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Wait plaease");
            dialog.setMessage("we getting data from data base");
            dialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {

            //Getting data from database

            Log.d(TAG, "doInBackground: trying to get playlist from database in GetDataTasak");
            mPlaylists.addAll(mPlaylistRepository.getPlaylistAsArrayList());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if(mPlaylists.size() == 0)
            {
                Snackbar.make(findViewById(R.id.main_layout),"no Data from database", Snackbar.LENGTH_LONG).show();

            }
        }
    }
}
