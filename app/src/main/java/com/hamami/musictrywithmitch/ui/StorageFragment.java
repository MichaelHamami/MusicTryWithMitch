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
import com.hamami.musictrywithmitch.R;
import com.hamami.musictrywithmitch.adapters.PlaylistRecyclerAdapter;
import com.hamami.musictrywithmitch.adapters.StorageRecyclerAdapter;
import com.hamami.musictrywithmitch.persistence.PlaylistRepository;
import java.io.File;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StorageFragment extends Fragment implements StorageRecyclerAdapter.IMediaSelector
{
    private static final String TAG = "StorgeFragment";

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



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mPlaylistRepository = new PlaylistRepository(getContext());
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments!");
            if (songsList.size() ==0)
            {
                Toast.makeText(getContext(),"we get arguments",Toast.LENGTH_LONG).show();
                songsList = getArguments().getParcelableArrayList("songLists");
//                addToMediaList(songsList);
                mPlaylistTitle = getArguments().getString("title");
                mPlaylistFragment = new Playlist(mPlaylistTitle,songsList);
                mIsPlaylistInDatabase = getArguments().getBoolean("isPlaylistInDatabase");

//                if(mIsPlaylistInDatabase == false)
//                {
//                    savePlaylistToDatabase();
//
//                }
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

    }


    private void initRecyclerView(View view)
    {
            mRecyclerView = view.findViewById(R.id.reycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//            mAdapter = new PlaylistRecyclerAdapter(getActivity(),songsList,mMediaList,this);
            Log.d(TAG, "initRecyclerView: called , Song list size is:"+songsList.size()+" and MediaList size is:" +mMediaList.size());
            mRecyclerView.setAdapter(mAdapter);

            updateDataSet();

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
        Log.d(TAG, "onSongSelected: list item is clicked! +List size is: "+mMediaList.size());
        mIMainActivity.getMyApplicationInstance().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(mPlaylistTitle,mSelectedMedia,position);

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
                        Log.d(TAG, "onMenuItemClick: play menu clicked ");
                        onMediaSelected(postion);
                        return true;
                    case R.id.deleteMenu:
                        Log.d(TAG, "onMenuItemClick: delete menu  clicked ");
//                        deleteSongFromList(postion);
                        return true;
                    case R.id.addToPlaylistMenu:
                        Log.d(TAG, "onMenuItemClick: add to playlist menu clicked song:"+songsList.get(postion).getNameSong());
                        mIMainActivity.onAddPlaylistMenuSelected(songsList.get(postion));
                        return true;
                    case R.id.addAsFavorite:
                        Log.d(TAG, "onMenuItemClick: add to Favorite menu  clicked ");
                        mIMainActivity.addSongToPlaylist(songsList.get(postion),"Favorite");
                        return true;
                    case R.id.addToQueue:
                        Log.d(TAG, "onMenuItemClick: Add to queue menu  clicked ");
                        mIMainActivity.addSongToPlaylist(songsList.get(postion),"Queue");
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index",mAdapter.getSelectedIndex());
    }


}
