package com.keywestnetworks.kwconnect.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.keywestnetworks.kwconnect.Fragments.EthernetFragment;
import com.keywestnetworks.kwconnect.Fragments.WirelessFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                WirelessFragment wirelessFragment = new WirelessFragment();
                return wirelessFragment;
            case 1:
                EthernetFragment ethernetFragment = new EthernetFragment();
                return ethernetFragment;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
