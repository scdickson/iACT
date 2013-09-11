package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.adapters.SchedulePageAdapter;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.PersistenceManager;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceSchedule extends FragmentActivity
{
    public static final int EVENTS_ALL = 0;
    public static final int EVENTS_PERSONAL = 1;
    public static final int SELECTED = Color.parseColor("#E8BD3B");
    public static final int UNSELECTED = Color.parseColor("#ffffff");

    LinearLayout eventSelector;
    TextView eventSelectorAll, eventSelectorPersonal, txtPageDate;
    ImageView iactLogo, prevPage, nextPage;
    ProgressDialog progressDialog;
    Context context;
    EventSelectorListener selector;
    SchedulePageAdapter adapter;
    SchedulePageClickListener pageClickListener;
    ViewPager pager;
    ConferenceSchedule cs;
    SimpleDateFormat timeFormat;

    public static ArrayList<Event> events;
    public static ArrayList<Fragment> days;
    public static int event_selector = EVENTS_ALL;
    public static int current_page = 0;

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
        selector = new EventSelectorListener();
        pageClickListener = new SchedulePageClickListener();
        eventSelector = (LinearLayout) findViewById(R.id.event_selector_layout);
        eventSelectorAll = (TextView) findViewById(R.id.event_selector_all);
        eventSelectorPersonal = (TextView) findViewById(R.id.event_selector_personal);
        iactLogo = (ImageView) findViewById(R.id.menubar_logo);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        context = this;
        cs = this;
        timeFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        txtPageDate = (TextView) findViewById(R.id.schedule_current_day);
        prevPage = (ImageView) findViewById(R.id.schedule_prev_day);
        prevPage.setOnClickListener(pageClickListener);
        nextPage = (ImageView) findViewById(R.id.schedule_next_day);
        nextPage.setOnClickListener(pageClickListener);

        ImageButton ib = (ImageButton) findViewById(R.id.toggle_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context, MainActivity.class);
                if(intent != null)
                {
                    startActivity(intent);
                }
            }
        });
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

    public void onPause()
    {
        super.onPause();

        try
        {
            PersistenceManager.writeObject(getApplicationContext(), Constants.CONFERENCE_EVENT_FILE_NAME, events);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadEvents()
    {
        events = new ArrayList<Event>();
        days = new ArrayList<Fragment>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Conference_Schedule");
        query.addAscendingOrder("p3_Start_Time");
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

                days = getDayFragments();
                txtPageDate.setText("Day 1, " + timeFormat.format(events.get(0).start_time));
                adapter = new SchedulePageAdapter(getSupportFragmentManager(), days);
                pager = (ViewPager) findViewById(R.id.schedule_pager);
                pager.setAdapter(adapter);
                pager.setOnPageChangeListener(new SchedulePageListener(cs));


                if(progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
            }
        });
    }

    public ArrayList<Fragment> getDayFragments()
    {
        ArrayList<Fragment> days = new ArrayList<Fragment>();
        Date prev = null;

        for(Event evt : events)
        {
            if(prev != null)
            {
                if(prev.getDay() != evt.start_time.getDay())
                {
                    days.add(new ConferenceSchedulePage(evt.start_time, cs));
                }
            }
            else
            {
                days.add(new ConferenceSchedulePage(evt.start_time, cs));
            }

            prev = evt.start_time;
        }

        return days;
    }


    public void onResume()
    {
        super.onResume();
        iactLogo.setVisibility(View.GONE);
        eventSelector.setVisibility(View.VISIBLE);
        eventSelectorAll.setOnClickListener(selector);
        eventSelectorPersonal.setOnClickListener(selector);

        if(events == null)
        {
            try
            {
                File f = getFileStreamPath(Constants.CONFERENCE_EVENT_FILE_NAME);
                if((f.lastModified() + (Constants.CONFERENCE_EVENTS_REPLACE_INTERVAL * 60 * 1000)) >= System.currentTimeMillis())
                {
                    events = (ArrayList<Event>) PersistenceManager.readObject(getApplicationContext(), Constants.CONFERENCE_EVENT_FILE_NAME);

                    days = getDayFragments();
                    txtPageDate.setText("Day 1, " + timeFormat.format(events.get(0).start_time));
                    adapter = new SchedulePageAdapter(getSupportFragmentManager(), days);
                    pager = (ViewPager) findViewById(R.id.schedule_pager);
                    pager.setAdapter(adapter);
                    pager.setOnPageChangeListener(new SchedulePageListener(cs));
                }
                else
                {
                    progressDialog.show();
                    loadEvents();
                }
            }
            catch(Exception e)
            {
                progressDialog.show();
                loadEvents();
            }
        }
        else
        {
            txtPageDate.setText("Day 1, " + timeFormat.format(events.get(0).start_time));
            adapter = new SchedulePageAdapter(getSupportFragmentManager(), days);
            pager = (ViewPager) findViewById(R.id.schedule_pager);
            pager.setAdapter(adapter);
            pager.setOnPageChangeListener(new SchedulePageListener(cs));

            switch(event_selector)
            {
                case EVENTS_ALL:
                    setSelector(EVENTS_ALL);
                    break;
                case EVENTS_PERSONAL:
                    setSelector(EVENTS_PERSONAL);
                    break;
            }
        }


    }

    public void setSelector(int selectorState)
    {
        ConferenceSchedulePage csp = (ConferenceSchedulePage) days.get(pager.getCurrentItem());

        switch(selectorState)
        {
            case EVENTS_ALL:
                if(event_selector == EVENTS_PERSONAL)
                {
                    event_selector = EVENTS_ALL;
                    eventSelectorAll.setTextColor(SELECTED);
                    eventSelectorPersonal.setTextColor(UNSELECTED);
                    if(csp != null && csp.getState() != event_selector)
                        csp.setState(EVENTS_ALL);
                }
                break;
            case EVENTS_PERSONAL:
                if(event_selector == EVENTS_ALL)
                {
                    event_selector = EVENTS_PERSONAL;
                    eventSelectorAll.setTextColor(UNSELECTED);
                    eventSelectorPersonal.setTextColor(SELECTED);
                    if(csp != null && csp.getState() != event_selector)
                        csp.setState(EVENTS_PERSONAL);
                }
                break;
        }
    }

    private class EventSelectorListener implements TextView.OnClickListener
    {
        public void onClick(View view)
        {
            if(view.equals(eventSelectorAll))
            {
                setSelector(EVENTS_ALL);
            }
            else if(view.equals(eventSelectorPersonal))
            {
                setSelector(EVENTS_PERSONAL);
            }
        }
    }

    private class SchedulePageListener implements ViewPager.OnPageChangeListener
    {
        ConferenceSchedule cs;

        public SchedulePageListener(ConferenceSchedule cs)
        {
            this.cs = cs;
        }

        public void onPageSelected(int page){}
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}

        public void onPageScrollStateChanged(int state)
        {
            if(state == ViewPager.SCROLL_STATE_SETTLING)
            {
                if(pager.getCurrentItem() != current_page)
                {
                    current_page = pager.getCurrentItem();
                    ConferenceSchedulePage csp = (ConferenceSchedulePage) days.get(current_page);
                    txtPageDate.setText("Day " + (current_page + 1) + ", " + timeFormat.format(csp.date));

                    if(csp.getState() != event_selector)
                    {
                        csp.setState(event_selector);
                    }
                }
            }
        }
    }

    private class SchedulePageClickListener implements View.OnClickListener
    {
        public void onClick(View view)
        {
            try
            {
                current_page = pager.getCurrentItem();

                if(view.equals(prevPage))
                {
                    if(current_page > 0)
                    {
                        pager.setCurrentItem(--current_page);
                        ConferenceSchedulePage csp = (ConferenceSchedulePage) days.get(current_page);
                        txtPageDate.setText("Day " + (current_page + 1) + ", " + timeFormat.format(csp.date));

                        if(csp.getState() != event_selector)
                        {
                            csp.setState(event_selector);
                        }
                    }
                }
                else if(view.equals(nextPage))
                {
                    if(current_page < days.size())
                    {
                        pager.setCurrentItem(++current_page);
                        ConferenceSchedulePage csp = (ConferenceSchedulePage) days.get(current_page);
                        txtPageDate.setText("Day " + (current_page + 1) + ", " + timeFormat.format(csp.date));

                        if(csp.getState() != event_selector)
                        {
                            csp.setState(event_selector);
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
