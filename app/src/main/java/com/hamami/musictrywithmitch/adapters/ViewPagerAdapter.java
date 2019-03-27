package com.hamami.musictrywithmitch.adapters;

import android.util.Log;

import com.hamami.musictrywithmitch.ui.PlaylistFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "ViewPagerAdapter";

    private ArrayList<PlaylistFragment> mFragments = new ArrayList<>();
    private ArrayList<String> mTitleTabs = new ArrayList<>();


    public ViewPagerAdapter(FragmentManager fm) {
    super(fm);
    }

    public ViewPagerAdapter(FragmentManager fm,ArrayList<PlaylistFragment> fragments,ArrayList<String> titleTabs) {
        super(fm);
        mFragments = fragments;
       mTitleTabs = titleTabs;
    }

    public ArrayList<PlaylistFragment> getFragments() {
        return mFragments;
    }

    public ArrayList<String> getFragmentTitles() {
        return mTitleTabs;
    }

    public void addFragment(PlaylistFragment fragment, String title)
    {
        mFragments.add(fragment);
        mTitleTabs.add(title);
    }
    public Fragment getItemByTitle(String title)
    {
        for(int i=0;i<mFragments.size();i++)
        {
            if(mTitleTabs.get(i).equals(title))
                return mFragments.get(i);
        }
        Log.d(TAG, "getItemByTitle: Not Found Title");
        return mFragments.get(0);
    }
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleTabs.get(position);
    }

    public int getItemPositionByTitle(String title)
    {
        for(int i=0;i<mTitleTabs.size();i++)
        {
            if(mTitleTabs.get(i).equals(title))
                return i;
        }
        return -1;
    }

}
