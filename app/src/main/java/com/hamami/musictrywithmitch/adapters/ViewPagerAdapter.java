package com.hamami.musictrywithmitch.adapters;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "ViewPagerAdapter";

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<String> mTitleTabs = new ArrayList<>();

//    public ViewPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments,ArrayList<String> titleTabs) {
    public ViewPagerAdapter(FragmentManager fm) {

    super(fm);
//        mFragments = fragments;
//        mTitleTabs = titleTabs;
    }

    public ArrayList<Fragment> getmFragments() {
        return mFragments;
    }

    public ArrayList<String> getFragmentTitles() {
        return mTitleTabs;
    }

    public void addFragment(Fragment fragment, String title)
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

}
