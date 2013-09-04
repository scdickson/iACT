package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
    private TextView conferenceTitle;

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
        context = this;
        MainActivity.infoButton.setVisibility(View.GONE);
        conferenceTitle = (TextView) findViewById(R.id.conference_title);
    }

    public void onResume()
    {
        super.onResume();

        conferenceMenuItems = new ArrayList<String>();
        conference_listener = new ConferenceListener(new ConferenceHandler());
        conference_listener.getConferenceStatus();
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void selectItem(int position)
    {
        Intent intent = null;

        if(conferenceMenuItems.get(position).equalsIgnoreCase("Daily Schedule"))
        {

        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Sponsors"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", Constants.CONFERENCE_SPONSORS_URL);
        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Event Highlights"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", Constants.CONFERENCE_HIGHLIGHTS_URL);
        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Maps and Directions"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", Constants.CONFERENCE_MAPS_URL);
        }

        if(intent != null)
        {
            intent.putExtra("BACK_ENABLED", false);
            startActivity(intent);
        }
    }

    private class MenuItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    private class ConferenceHandler extends Handler
    {
        public void handleMessage(Message msg)
        {
            conference = (Conference) msg.getData().getSerializable("CONFERENCE_DATA");

            if(conference != null && conference.enabled && !conference.name.isEmpty())
            {
                conferenceTitle.setText(Html.fromHtml(conference.name + "\n<font color=\"#825957\">Indianapolis</font>"));

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
                conferenceList.setOnItemClickListener(new MenuItemClickListener());
            }
            else
            {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
