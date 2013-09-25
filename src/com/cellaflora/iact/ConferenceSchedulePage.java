package com.cellaflora.iact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.objects.Event;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceSchedulePage extends Fragment
{
    View view;
    Date date;
    public ArrayList<Event> events;
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
        events = ConferenceSchedule.events;
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
            for(Event evt : ConferenceSchedule.mySchedule)
            {
                if(evt.start_time.getDay() == date.getDay())
                {
                    content.add(evt);
                }
            }
        }
        else if(state == ConferenceSchedule.EVENTS_ALL)
        {
            for(Event evt : events)
            {
                if(evt.start_time.getDay() == date.getDay())
                {
                    content.add(evt);
                }
            }
        }

        if(eventList != null && noEvents != null)
        {
            if(content.size() == 0)
            {
                setNoEvents();
            }
            else
            {
                noEvents.setVisibility(View.GONE);
                eventList.setVisibility(View.VISIBLE);
            }

            adapter = new ConferenceScheduleAdapter(view.getContext(), content, this);
            eventList.setAdapter(adapter);
            eventList.setOnItemClickListener(listener);
            eventList.invalidateViews();
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.conference_schedule_page, container, false);
        eventList = (ListView) view.findViewById(R.id.conference_event_list_view);
        noEvents = (TextView) view.findViewById(R.id.conference_schedule_no_events);
        noEvents.setTypeface(MainActivity.Futura);
        return view;
    }

    public void onResume()
    {
        super.onResume();

        if(state == ConferenceSchedule.EVENTS_PERSONAL)
        {
            content = new ArrayList<Event>();
            for(Event evt : ConferenceSchedule.mySchedule)
            {
                if(evt.start_time.getDay() == date.getDay())
                {
                    content.add(evt);
                }
            }
        }

        if(content.size() == 0)
        {
            setNoEvents();
        }
        else
        {
            noEvents.setVisibility(View.GONE);
            eventList.setVisibility(View.VISIBLE);

            adapter = new ConferenceScheduleAdapter(view.getContext(), content, this);
            eventList.setAdapter(adapter);
            eventList.setOnItemClickListener(listener);
        }
    }

    public void setNoEvents()
    {
        if(eventList != null && noEvents != null)
        {
            eventList.setVisibility(View.GONE);
            noEvents.setVisibility(View.VISIBLE);
        }
    }

    public void selectItem(int position)
    {
        ConferenceSchedule.saveState = true;
        Intent intent = new Intent(view.getContext(), ConferenceEventDetail.class);
        intent.putExtra("EVENT", content.get(position));

        if(intent != null)
            startActivity(intent);
    }

    private class EventItemClickListener implements ListView.OnItemClickListener
    {
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }



}
