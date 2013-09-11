package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.PersistenceManager;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceSchedulePage extends Fragment
{
    View view;
    Date date;
    public ArrayList<Event> content;
    ListView eventList;
    TextView noEvents;
    ConferenceScheduleAdapter adapter;
    ConferenceSchedule cs;
    EventItemClickListener listener;
    int state = ConferenceSchedule.EVENTS_ALL;

    public ConferenceSchedulePage(Date date, ConferenceSchedule cs)
    {
        content = new ArrayList<Event>();
        listener = new EventItemClickListener();
        this.cs = cs;
        this.date = date;

        for(Event evt : ConferenceSchedule.events)
        {
            if(evt.start_time.getDay() == date.getDay())
            {
                content.add(evt);
            }
        }
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        content = new ArrayList<Event>();
        this.state = state;

        if(state == ConferenceSchedule.EVENTS_PERSONAL)
        {
            for(Event evt : ConferenceSchedule.events)
            {
                if(evt.start_time.getDay() == date.getDay() && evt.isInPersonalSchedule)
                {
                    content.add(evt);
                }
            }
        }
        else if(state == ConferenceSchedule.EVENTS_ALL)
        {
            for(Event evt : ConferenceSchedule.events)
            {
                if(evt.start_time.getDay() == date.getDay())
                {
                    content.add(evt);
                }
            }
        }

        /*if(content.size() == 0)
        {
            noEvents.setVisibility(View.VISIBLE);
            eventList.setVisibility(View.GONE);
        }
        else
        {
            noEvents.setVisibility(View.GONE);
            eventList.setVisibility(View.VISIBLE);
        }*/

        adapter = new ConferenceScheduleAdapter(view.getContext(), content, cs);
        eventList.setAdapter(adapter);
        eventList.invalidateViews();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.conference_schedule_page, container, false);
        eventList = (ListView) view.findViewById(R.id.conference_event_list_view);
        noEvents = (TextView) view.findViewById(R.id.conference_schedule_no_events);
        return view;
    }

    public void onResume()
    {
        super.onResume();

        /*if(content.size() == 0)
        {
            noEvents.setVisibility(View.VISIBLE);
            eventList.setVisibility(View.GONE);
        }
        else
        {*/
            noEvents.setVisibility(View.GONE);
            eventList.setVisibility(View.VISIBLE);

            adapter = new ConferenceScheduleAdapter(view.getContext(), content, cs);
            eventList.setAdapter(adapter);

            if(Constants.CONFERENCE_EVENTS_DETAIL_ITEM_VIEW_ENABLED)
            {
                eventList.setOnItemClickListener(listener);
            }
        //}
    }

    private class EventItemClickListener implements ListView.OnItemClickListener
    {
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {

        }
    }


}