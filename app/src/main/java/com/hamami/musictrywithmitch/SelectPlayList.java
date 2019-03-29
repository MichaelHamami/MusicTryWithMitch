package com.hamami.musictrywithmitch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hamami.musictrywithmitch.Models.Songs;
import com.hamami.musictrywithmitch.adapters.SelectPlaylistRecyclerAdapter;
import com.hamami.musictrywithmitch.util.DialogCreateNewPlaylist;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectPlayList extends AppCompatActivity implements SelectPlaylistRecyclerAdapter.IPlaylistSelector, DialogCreateNewPlaylist.OnInputListener {

    private static final String TAG = "SelectPlayList";


    // UI Components
    private RecyclerView mRecyclerView;
    private Button mButtonCreate;

    
    //Vars
    private SelectPlaylistRecyclerAdapter mAdapter;

    private ArrayList<String> fragmentsTitles = new ArrayList<>();
    private Songs songSelected;
    private String thePlayList;
    private Boolean isNewPlaylist;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectplaylist);
        Log.d(TAG, "onCreate: Called");
        mRecyclerView = findViewById(R.id.reycler_view_selectPlaylist);
        mButtonCreate = findViewById(R.id.ButtonCreateNewPlaylist);

//        if(getIntent().hasExtra("selected_song"))
//        {
//             songSelected = getIntent().getParcelableExtra("selected_song");
//        }
        if(getIntent().hasExtra("playlistTitles"))
        {
            fragmentsTitles = getIntent().getStringArrayListExtra("playlistTitles");
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SelectPlaylistRecyclerAdapter(this,fragmentsTitles,this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Called: opening dialog");
                openDialog();
            }
        });
        
    }

    private void openDialog()
    {
        DialogCreateNewPlaylist dialog = new DialogCreateNewPlaylist();
        dialog.show(getSupportFragmentManager(),"DialogSelectPlaylist");
    }

    @Override
    public void onPlaylistSelected(int position) 
    {
        Log.d(TAG, "onPlaylistSelected: Called");
        isNewPlaylist = false;
        Intent intent= new Intent();
        intent.putExtra("isNewPlaylist",isNewPlaylist);
        intent.putExtra("selectedPlaylist",fragmentsTitles.get(position));
        setResult(4,intent);
        //finish must be declared here to send the result to parent activity
        finish();
    }

    @Override
    public void sendInput(String input)
    {
        Log.d(TAG, "sendInput: Got the input: "+input);
        thePlayList = input;
        isNewPlaylist = true;
        Intent intent= new Intent();
        intent.putExtra("isNewPlaylist",isNewPlaylist);
        intent.putExtra("newPlaylist",thePlayList);
        // send data
        setResult(4,intent);
//        finish must be declared here to send the result to parent activity
        finish();
    }

    @Override
    public boolean isPlaylistExists(String playlist)
    {
        for(int i=0;i<fragmentsTitles.size();i++)
        {
            if(fragmentsTitles.get(i).equals(playlist))
            {
                return true;
            }
        }
        return false;
    }
}
