package com.hitsquadtechnologies.sifyconnect.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hitsquadtechnologies.sifyconnect.Fragments.EthernetFragment;
import com.hitsquadtechnologies.sifyconnect.Fragments.WirelessFragment;
import com.hsq.kw.packet.vo.KeywestWirelessStats;

import java.util.ArrayList;

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
