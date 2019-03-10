package com.hamami.musictrywithmitch.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hamami.musictrywithmitch.IMainActivity;
import com.hamami.musictrywithmitch.PlaylistRecyclerAdapter;
import com.hamami.musictrywithmitch.R;
import com.hamami.musictrywithmitch.Song;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistFragment extends Fragment implements PlaylistRecyclerAdapter.IMediaSelector
{
    private static final String TAG = "PlaylistFragment";

    // UI Components
    private RecyclerView mRecyclerView;

    //Vars
    // the title we will get from bundle
    private String playlistTitle;
    private PlaylistRecyclerAdapter mAdapter;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Song> songsList = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;

    public static PlaylistFragment newInstance(ArrayList<Song> songsArray){
        Log.d(TAG, "playListFragment new Instance called!");
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songLists",songsArray);
        playlistFragment.setArguments(args);
        return playlistFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments!");
            if (songsList.size() ==0)
            {
                Toast.makeText(getContext(),"we get arguments",Toast.LENGTH_LONG).show();
                songsList = getArguments().getParcelableArrayList("songLists");
                addToMediaList(songsList);
            }
            // only for now
            playlistTitle = "jokeForNow";

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

    private void initRecyclerView(View view)
    {
            mRecyclerView = view.findViewById(R.id.reycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter = new PlaylistRecyclerAdapter(getActivity(),songsList,mMediaList,this);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

//            songsList = retriveSongs();
//            updateDataSet();

    }

    private void updateDataSet() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Override
    public void onMediaSelected(int position)
    {
        Log.d(TAG, "onSongSelected: list item is clicked!");
        mIMainActivity.getMyApplication().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(playlistTitle,mSelectedMedia,position);
        saveLastPlayedSongProperties();

    }
    public void updateUI(MediaMetadataCompat mediaItem)
    {
        mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mediaItem));
        mSelectedMedia = mediaItem;
        saveLastPlayedSongProperties();
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
    private void saveLastPlayedSongProperties()
    {
        // title is like the artist in mitch project
        mIMainActivity.getMyPreferenceManager().savePlaylistId(playlistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedArtist(playlistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.getDescription().getMediaId());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index",mAdapter.getSelectedIndex());
    }
}
