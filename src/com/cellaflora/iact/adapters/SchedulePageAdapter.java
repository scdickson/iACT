package com.cellaflora.iact.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cellaflora.iact.ConferenceSchedule;
import com.cellaflora.iact.ConferenceSchedulePage;

import java.util.ArrayList;

/**
 * Created by sdickson on 9/7/13.
 */
public class SchedulePageAdapter extends FragmentPagerAdapter
{
    ArrayList<Fragment> days;

    public SchedulePageAdapter(FragmentManager mgr, ArrayList<Fragment> days)
    {
        super(mgr);
        this.days = days;
    }

    public Fragment getItem(int position)
    {
        return days.get(position);
    }

    @Override
    public int getCount()
    {
        return days.size();
    }
}
