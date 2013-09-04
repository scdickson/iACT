package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.PersistenceManager;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceSchedule extends Activity
{
    private Context context;
    private ConferenceScheduleAdapter adapter;
    private ListView eventList;
    private ArrayList<Event> events;
    private ArrayList<Event> mySchedule;
    private ScheduleEventClickListener scheduleListener;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_schedule);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        context = this;
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private Date fixDate(Date date)
    {
        TimeZone tz = TimeZone.getDefault();
        Date fixed = new Date(date.getTime() - tz.getRawOffset());

        if(tz.inDaylightTime(fixed))
        {
            Date dst = new Date(fixed.getTime() - tz.getDSTSavings());

            if(tz.inDaylightTime(dst))
            {
                fixed = dst;
            }
        }

        return fixed;
    }

    public void loadEvents()
    {
        events = new ArrayList<Event>();
        mySchedule = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Conference_Schedule");
        query.addDescendingOrder("p3_Start_Time");
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> result, com.parse.ParseException e)
            {
                for(ParseObject parse : result)
                {
                    Event tmp = new Event();

                    tmp.objectId = parse.getObjectId();
                    tmp.title = parse.getString("p1_Title");
                    tmp.description = parse.getString("p2_Description");
                    tmp.location = parse.getString("p5_Location");
                    tmp.speakers = parse.getString("p6_Speakers");

                    if(parse.getDate("p3_Start_Time") != null)
                    {
                        tmp.start_time = fixDate(parse.getDate("p3_Start_Time"));
                    }

                    if(parse.getDate("p4_End_Time") != null)
                    {
                        tmp.end_time = fixDate(parse.getDate("p4_End_Time"));
                    }

                    events.add(tmp);
                }
                try
                {
                    PersistenceManager.writeObject(getApplicationContext(), Constants.CONFERENCE_EVENT_FILE_NAME, events);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }

                eventList = (ListView) findViewById(R.id.conference_event_list_view);
                adapter = new ConferenceScheduleAdapter(context, events);
                eventList.setAdapter(adapter);
                scheduleListener = new ScheduleEventClickListener();
                eventList.setOnItemClickListener(scheduleListener);
            }
        });
    }

    public void onResume()
    {
        super.onResume();
    }

    private void selectItem(int position)
    {

    }

    private class ScheduleEventClickListener implements ListView.OnItemClickListener
    {
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }
}
