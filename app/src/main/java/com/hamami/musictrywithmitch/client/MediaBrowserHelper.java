package com.hamami.musictrywithmitch.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;

public class MediaBrowserHelper {

    private static final String TAG = "MediaBrowserHelper";

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private final MediaBrowserSubscriptionCallBack mMediaBrowserSubscriptionCallBack;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaBrowserHelperCallback mMediaBrowserCallback;
    private boolean mWasConfigurationChanged;



    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> mediaBrowserServiceClass) {
        mContext = context;
        mMediaBrowserServiceClass = mediaBrowserServiceClass;

        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallBack = new MediaBrowserSubscriptionCallBack();
        mMediaControllerCallback = new MediaControllerCallback();
    }

    public void setMediaBrowserHelperCallback(MediaBrowserHelperCallback mediaBrowserHelperCallback)
    {
       mMediaBrowserCallback = mediaBrowserHelperCallback;
    }
    private class MediaControllerCallback extends MediaControllerCompat.Callback
    {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged: called.");

            if(mMediaBrowserCallback != null)
            {
                mMediaBrowserCallback.onPlayBackStateChanged(state);
            }

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata)
        {
            Log.d(TAG, "onMetadataChanged: called.");

            if(mMediaBrowserCallback != null)
            {
                mMediaBrowserCallback.onMetadataChanged(metadata);
            }
        }
    }

    public void subscribeToNewPlaylist(String playlistId)
    {
        mMediaBrowser.subscribe(playlistId,mMediaBrowserSubscriptionCallBack);
    }
    public void onStart(boolean wasConfigurationChanged)
    {
        mWasConfigurationChanged = wasConfigurationChanged;
        if(mMediaBrowser == null)
        {
            mMediaBrowser = new MediaBrowserCompat(
                    mContext,
                    new ComponentName(mContext,mMediaBrowserServiceClass),
                    mMediaBrowserConnectionCallback,
                    null);

            mMediaBrowser.connect();
            Log.d(TAG,"onStart: connecting to the service");
        }
    }
    public void onStop()
    {
        if (mMediaController != null)
        {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if(mMediaBrowser != null && mMediaBrowser.isConnected())
        {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        Log.d(TAG,"onStop: disconnecting from the service");
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback{
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            Log.d(TAG,"onConnected: called");

            try
            {
                // Get a MediaController for the MediaSession.
                mMediaController = new MediaControllerCompat(mContext,mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);


            }catch (RemoteException e)
            {
                Log.d(TAG,"onConnected: connection problem: "+e.toString());
                throw new RuntimeException(e);
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(),mMediaBrowserSubscriptionCallBack);
            Log.d(TAG,"onConnected: called: subscribing to: "+mMediaBrowser.getRoot());

            mMediaBrowserCallback.onMediaControllerConnected(mMediaController);

        }
    }

    public class MediaBrowserSubscriptionCallBack extends MediaBrowserCompat.SubscriptionCallback
    {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d(TAG,"onChildrenLoaded: called: "+parentId + ", "+  children.toString()+", size:"+children.size());

            if(!mWasConfigurationChanged)
            {
                for(final MediaBrowserCompat.MediaItem mediaItem : children)
                {
                    Log.d(TAG,"onChildrenLoaded: CALLED: queue item:" + mediaItem.getMediaId());
                    mMediaController.addQueueItem(mediaItem.getDescription());
                }
            }


        }
    }

    public MediaControllerCompat.TransportControls getTransportControls()
    {
        if(mMediaController == null)
        {
            Log.d(TAG,"getTransportControls: MediaController is null !");
            throw new IllegalStateException("Media Controller is null");
        }
        return mMediaController.getTransportControls();
    }

}
