package com.cellaflora.iact;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by sdickson on 8/29/13.
 */
public class ConferenceListener
{
    public static final int CONFERENCE_DISABLED = 0;
    public static final int CONFERENCE_ENABLED = 1;

    public Conference conference;
    public Handler handler;
    private boolean running = false;

    public ConferenceListener(Conference conference, Handler handler)
    {
        this.conference = conference;
        this.handler = handler;
        setRunning(true);
        new ConferenceAsync().execute();
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }

    private class ConferenceAsync extends AsyncTask<Void, Integer, Void>
    {
        protected Void doInBackground(Void... arg0)
        {
            ParseQuery<ParseObject> menu_query = ParseQuery.getQuery("Conference_Info");
            menu_query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> result, ParseException e)
            {
                if (e == null)
                {
                    for(ParseObject parse : result)
                    {
                            conference.enabled = parse.getBoolean("show_Conference");
                            conference.highlights_link = parse.getString("link_for_Event_Highlights");
                            conference.maps_link = parse.getString("link_for_Maps");
                            conference.sponsors_link = parse.getString("link_for_Sponsors");
                            conference.name = parse.getString("Conference_Name");
                            conference.show_daily_schedule = parse.getBoolean("show_Daily_Schedule");;
                            conference.show_event_highlights = parse.getBoolean("show_Event_Highlights");;
                            conference.show_event_maps = parse.getBoolean("show_Maps");;
                            conference.show_event_sponsors = parse.getBoolean("show_Sponsors");;
                    }
                }
            }
            });
            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message m = new Message();
            Bundle b = new Bundle();

            if(conference.enabled && !MainActivity.conference_enabled)
            {
                    b.putInt("CONFERENCE_STATUS", CONFERENCE_ENABLED);
                    m.setData(b);
                    handler.sendMessage(m);
            }
            else if(!conference.enabled && MainActivity.conference_enabled)
            {
                    b.putInt("CONFERENCE_STATUS", CONFERENCE_DISABLED);
                    m.setData(b);
                    handler.sendMessage(m);
            }

            if(running)
            {
                try
                {
                    Thread.sleep(Constants.CONFERENCE_CHECK_INTERVAL * 1000);
                    new ConferenceAsync().execute();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
