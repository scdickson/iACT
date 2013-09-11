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
                        conference.show_daily_schedule = parse.getBoolean("show_Daily_Schedule");
                        conference.show_event_highlights = parse.getBoolean("show_Event_Highlights");
                        conference.show_event_maps = parse.getBoolean("show_Maps");
                        conference.show_event_sponsors = parse.getBoolean("show_Sponsors");
                    }

                    ParseQuery<ParseObject> event_query = ParseQuery.getQuery("Conference_Schedule");
                    event_query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> result, ParseException e)
                        {
                            if (e == null)
                            {
                                conference.startDate = result.get(0).getDate("p3_Start_Time");
                                conference.endDate = result.get(result.size()-1).getDate("p3_Start_Time");

                                Message msg = new Message();
                                Bundle data = new Bundle();
                                data.putSerializable("CONFERENCE_DATA", conference);
                                msg.setData(data);
                                handler.sendMessage(msg);
                            }

                        }

                    });
                }
                else
                {
                    e.printStackTrace();
                }
            }
        });
}



}
