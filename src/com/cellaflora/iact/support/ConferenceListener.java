package com.cellaflora.iact.support;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cellaflora.iact.objects.Conference;
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
    public Conference conference;
    public Handler handler;

    public ConferenceListener(Handler handler)
    {
        this.handler = handler;
    }

    public void getConferenceStatus()
    {
        conference = new Conference();
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
                        conference.sub_headline = parse.getString("Conference_Subtitle");
                        conference.name_short = parse.getString("Conference_Name_Short");
                        conference.show_daily_schedule = parse.getBoolean("show_Daily_Schedule");
                        conference.show_event_highlights = parse.getBoolean("show_Event_Highlights");
                        conference.show_event_maps = parse.getBoolean("show_Maps");
                        conference.show_event_sponsors = parse.getBoolean("show_Sponsors");

                        conference.event_highlights_url = parse.getString("link_for_Event_Highlights");
                        if(conference.event_highlights_url == null || conference.event_highlights_url.isEmpty())
                        {
                            conference.show_event_highlights = false;
                        }

                        conference.event_maps_url = parse.getString("link_for_Maps");
                        if(conference.event_maps_url == null || conference.event_maps_url.isEmpty())
                        {
                            conference.show_event_maps = false;
                        }

                        conference.event_sponsors_url = parse.getString("link_for_Sponsors");
                        if(conference.event_sponsors_url == null || conference.event_sponsors_url.isEmpty())
                        {
                            conference.show_event_sponsors = false;
                        }
                    }


                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putSerializable("CONFERENCE_DATA", conference);
                    msg.setData(data);
                    handler.sendMessage(msg);

                }
                else
                {
                    e.printStackTrace();
                }
            }
        });
}



}
