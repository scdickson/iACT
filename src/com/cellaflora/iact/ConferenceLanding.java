package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by sdickson on 9/2/13.
 */
public class ConferenceLanding extends Activity
{
    private ArrayList<String> conferenceMenuItems;
    private Conference conference;
    private ConferenceMenuAdapter adapter;
    private ListView conferenceList;
    private Context context;
    private ConferenceListener conference_listener = null;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_landing);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        conferenceMenuItems = new ArrayList<String>();
        context = this;
        MainActivity.infoButton.setVisibility(View.GONE);
    }

    public void onResume()
    {
        super.onResume();

        conference_listener = new ConferenceListener(new ConferenceHandler());
        conference_listener.getConferenceStatus();
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private class ConferenceHandler extends Handler
    {
        public void handleMessage(Message msg)
        {
            conference = (Conference) msg.getData().getSerializable("CONFERENCE_DATA");

            if(conference != null && conference.enabled && !conference.name.isEmpty())
            {
                if(conference.show_daily_schedule)
                {
                    conferenceMenuItems.add("Daily Schedule");
                }

                if(conference.show_event_sponsors)
                {
                    conferenceMenuItems.add("Sponsors");
                }

                if(conference.show_event_highlights)
                {
                    conferenceMenuItems.add("Event Highlights");
                }

                if(conference.show_event_maps)
                {
                    conferenceMenuItems.add("Maps and Directions");
                }

                adapter = new ConferenceMenuAdapter(context, conferenceMenuItems);
                conferenceList = (ListView) findViewById(R.id.conference_list_view);
                conferenceList.setAdapter(adapter);
            }
            else
            {
                //MainActivity.conference_enabled = false;
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
