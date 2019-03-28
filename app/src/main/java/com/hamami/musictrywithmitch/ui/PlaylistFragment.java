package com.hamami.musictrywithmitch.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.hamami.musictrywithmitch.IMainActivity;
import com.hamami.musictrywithmitch.Models.Playlist;
import com.hamami.musictrywithmitch.Models.Songs;
import com.hamami.musictrywithmitch.adapters.PlaylistRecyclerAdapter;
import com.hamami.musictrywithmitch.R;
import com.hamami.musictrywithmitch.Models.Song;
import com.hamami.musictrywithmitch.persistence.PlaylistRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistFragment extends Fragment implements PlaylistRecyclerAdapter.IMediaSelector
{
    private static final String TAG = "PlaylistFragment";

    // UI Components
    private RecyclerView mRecyclerView;

    //Vars
    // the title we will get from bundle
    private String mPlaylistTitle;
    private PlaylistRecyclerAdapter mAdapter;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Songs> songsList = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private boolean mIsPlaylistInDatabase;
    private Playlist mPlaylistFragment;

    // Repository
    private PlaylistRepository mPlaylistRepository;

//    public static PlaylistFragment newInstance(ArrayList<Songs> songsArray,String title){
public static PlaylistFragment newInstance(Playlist playlist,boolean isPlaylistInDatabase){
    Log.d(TAG, "playListFragment new Instance called!");
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songLists",playlist.getSongs());
        args.putString("title",playlist.getTitle());
        args.putBoolean("isPlaylistInDatabase",isPlaylistInDatabase);
        playlistFragment.setArguments(args);
        return playlistFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylistRepository = new PlaylistRepository(getContext());
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments!");
            if (songsList.size() ==0)
            {
                Toast.makeText(getContext(),"we get arguments",Toast.LENGTH_LONG).show();
                songsList = getArguments().getParcelableArrayList("songLists");
                addToMediaList(songsList);
                mPlaylistTitle = getArguments().getString("title");
                mPlaylistFragment = new Playlist(mPlaylistTitle,songsList);
                mIsPlaylistInDatabase = getArguments().getBoolean("isPlaylistInDatabase");

                if(mIsPlaylistInDatabase == false)
                {
                    savePlaylistToDatabase();

                }
            }
            setRetainInstance(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_playlist,container,false);
    }

    // called after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        initRecyclerView(view);

        if(savedInstanceState != null)
        {
            mAdapter.setSelectedIndex(savedInstanceState.getInt("selected_index"));
        }

    }
    private void getSelectedMediaItem(String mediaId)
    {
        for(MediaMetadataCompat mediaItem: mMediaList)
        {
            if(mediaItem.getDescription().getMediaId().equals(mediaId))
            {
                mSelectedMedia = mediaItem;
                mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mSelectedMedia));
                break;
            }
        }
    }

    public int getSelectedIndex()
    {
       return mAdapter.getSelectedIndex();
    }

    private void initRecyclerView(View view)
    {
            mRecyclerView = view.findViewById(R.id.reycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter = new PlaylistRecyclerAdapter(getActivity(),songsList,mMediaList,this);
            Log.d(TAG, "initRecyclerView: called , Song list size is:"+songsList.size()+" and MediaList size is:" +mMediaList.size());
            mRecyclerView.setAdapter(mAdapter);

            updateDataSet();

    }

    private void updateDataSet() {
        mAdapter.notifyDataSetChanged();
        if(mIMainActivity.getMyPreferenceManager().getLastPlayedArtist().equals(mPlaylistTitle)){
            getSelectedMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia());
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Override
    public void onMediaSelected(int position)
    {
        Log.d(TAG, "onSongSelected: list item is clicked! +List size is: "+mMediaList.size());
        mIMainActivity.getMyApplicationInstance().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(mPlaylistTitle,mSelectedMedia,position);
        saveLastPlayedSongProperties();

    }

    @Override
    public void onSongOptionSelected(int position,View view)
    {
        Log.d(TAG, "onSongOptionSelected: you clicked on menu good job");
        showPopup(position,view);
    }
    public void showPopup(final int postion, View view){
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.options_menu);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.playMenu:
                        Toast.makeText(getContext(), "play menu clicked", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onMenuItemClick: play menu clicked ");
                        onMediaSelected(postion);
                        return true;
                    case R.id.deleteMenu:
                        Log.d(TAG, "onMenuItemClick: delete menu  clicked ");
                        Toast.makeText(getContext(), "delete menu  clicked", Toast.LENGTH_SHORT).show();
                        deleteSongFromList(postion);
                        return true;
                    case R.id.addToPlaylistMenu:
                        Log.d(TAG, "onMenuItemClick: add to playlist menu clicked song:"+songsList.get(postion).getNameSong());
                        mIMainActivity.onAddPlaylistMenuSelected(songsList.get(postion));
                        return true;
                    case R.id.addAsFavorite:
                        Log.d(TAG, "onMenuItemClick: add to Favorite menu  clicked ");
                        Toast.makeText(getContext(), "add to Favorite menu  clicked", Toast.LENGTH_SHORT).show();
                        mIMainActivity.addSongToPlaylist(songsList.get(postion),"Favorite");
                        return true;
                    case R.id.addToQueue:
                        Log.d(TAG, "onMenuItemClick: Add to queue menu  clicked ");
                        Toast.makeText(getContext(), "Add to queue menu  clicked", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        return false;
                }
            }
        });
        //displaying the popup
        popup.show();
    }

    public void updateUI(MediaMetadataCompat mediaItem)
    {
        mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mediaItem));
        mSelectedMedia = mediaItem;
        saveLastPlayedSongProperties();
    }
    public  void addSongToList(Songs song)
    {
        // check for duplicates
        for(int i=0; i<songsList.size();i++)
        {
            if(songsList.get(i).equals(song))
            {
                Toast.makeText(getContext(), "the song is already in the playlist ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "addSongToList: the song is already in the playlist");
                return;
            }
        }
        File file = new File(song.getFileSong());

        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                // title = songName , songTime need to be changed
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,song.getFileSong().toURI().toString())
                .build();
        mMediaList.add(media);
        songsList.add(song);
        // need to be change just dont want to make crush
        // need to update database
//        mPlaylistRepository.updatePlaylist(mPlaylistFragment);
        mIMainActivity.updateToDatabase(mPlaylistFragment);
        updateDataSet();
    }
    private   void deleteSongFromList(int position)
    {
        // need to be change just don't want to make crush
        if(songsList.size() == 1)
        {
          mIMainActivity.removePlaylistFromDatabase(mPlaylistFragment);
        }

        else
        {
            songsList.remove(position);
            mMediaList.remove(position);
            mIMainActivity.updateToDatabase(mPlaylistFragment);
            updateDataSet();
        }

    }

    private void addToMediaList(ArrayList<Songs> songsList)
    {
        for (int i=0;i<songsList.size();i++)
        {
            File file = new File(songsList.get(i).getFileSong());
            MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                    // title = songName , artist=songTime
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songsList.get(i).getSongLength())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,songsList.get(i).getFileSong().toURI().toString())
                    .build();
            mMediaList.add(media);
        }
    }
    private void saveLastPlayedSongProperties()
    {
        // title is like the artist in mitch project
        mIMainActivity.getMyPreferenceManager().savePlaylistId(mPlaylistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedArtist(mPlaylistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.getDescription().getMediaId());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index",mAdapter.getSelectedIndex());
    }

    private  void savePlaylistToDatabase()
    {
        Log.d(TAG, "savePlaylistToDatabase: we try to save the playlist to the database");
        Log.d(TAG, "savePlaylistToDatabase: Title: "+mPlaylistTitle+" Songs size: "+songsList.size());
//        mPlaylistRepository.insertPlaylistTask(mPlaylistFragment);
        mIMainActivity.insertToDatabase(mPlaylistFragment);

//        ArrayList<String> playlistTitles = new ArrayList<>();
////        playlistTitles.addAll(mPlaylistRepository.getPlaylistTitles());
//        if( isThisNewPlaylist(mPlaylistFragment,playlistTitles) == true)
//        {
//            Log.d(TAG, "savePlaylistToDatabase: we insert new Playlist");
//            mPlaylistRepository.insertPlaylistTask(new Playlist(mPlaylistTitle,songsList));
//        }
//        else
//        {
//            Log.d(TAG, "savePlaylistToDatabase: this playlist :"+mPlaylistFragment.getTitle() +" are already in database");
//        }
    }
    public boolean isThisNewPlaylist(Playlist playlist,ArrayList<String> titles)
    {
        for(int i = 0; i<titles.size(); i++)
        {
            if(titles.get(i).equals(playlist.getTitle()))
            {
                return false;
            }
        }
        return true;
    }
}
