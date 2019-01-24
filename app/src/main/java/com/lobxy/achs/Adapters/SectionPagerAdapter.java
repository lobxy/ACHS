package com.lobxy.achs.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lobxy.achs.Admin.Fragments.Carpentry;
import com.lobxy.achs.Admin.Fragments.Civil;
import com.lobxy.achs.Admin.Fragments.Electricity;
import com.lobxy.achs.Admin.Fragments.Fire_and_Security;
import com.lobxy.achs.Admin.Fragments.HouseKeeping;
import com.lobxy.achs.Admin.Fragments.Intercom;
import com.lobxy.achs.Admin.Fragments.Lift;
import com.lobxy.achs.Admin.Fragments.Others;
import com.lobxy.achs.Admin.Fragments.Plumbing;


public class SectionPagerAdapter extends FragmentPagerAdapter {


    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Electricity();
            case 1:
                return new Plumbing();
            case 2:
                return new Civil();
            case 3:
                return new Carpentry();
            case 4:
                return new HouseKeeping();
            case 5:
                return new Intercom();
            case 6:
                return new Fire_and_Security();
            case 7:
                return new Lift();
            case 8:
                return new Others();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 9;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Electricity";
            case 1:
                return "Plumbing";
            case 2:
                return "Civil";
            case 3:
                return "Carpentry";
            case 4:
                return "Housekeeping";
            case 5:
                return "Intercom";
            case 6:
                return "Fire and Security";
            case 7:
                return "Lift";
            case 8:
                return "Other";
            default:
                return null;
        }
    }
}
