package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceMenuAdapter;
import com.cellaflora.iact.objects.Conference;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.ConferenceListener;
import com.cellaflora.iact.support.PersistenceManager;
import com.parse.Parse;
import com.parse.ParseAnalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

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
    private ProgressDialog progressDialog;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_landing);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        Parse.initialize(this, Constants.PARSE_APPLICATION_ID, Constants.PARSE_CLIENT_KEY);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        context = this;
        conferenceTitle = (TextView) findViewById(R.id.conference_title);
        conferenceTitle.setTypeface(MainActivity.Futura);

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

    public void onResume()
    {
        super.onResume();
        //progressDialog.show();
        conferenceMenuItems = new ArrayList<String>();
        conference_listener = new ConferenceListener(new ConferenceHandler());
        conference_listener.getConferenceStatus();
        //ConferenceSchedule.event_selector = ConferenceSchedule.EVENTS_ALL;
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void selectItem(int position)
    {
        Intent intent = null;

        if(conferenceMenuItems.isEmpty())
        {
            return;
        }

        if(conferenceMenuItems.get(position).equalsIgnoreCase("Daily Schedule"))
        {
            intent = new Intent(this, ConferenceSchedule.class);
        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Sponsors"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", conference.event_sponsors_url);
        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Event Highlights"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", conference.event_highlights_url);
        }
        else if(conferenceMenuItems.get(position).equalsIgnoreCase("Maps and Directions"))
        {
            intent = new Intent(this, WebContentView.class);
            intent.putExtra("URL", conference.event_maps_url);
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

            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }

            if(conference != null && conference.enabled && !conference.name.isEmpty())
            {

                if(conference.sub_headline != null && !conference.sub_headline.isEmpty())
                {
                    conferenceTitle.setText(Html.fromHtml(conference.name + "<br/><font color=\"#ac2d2d\">" + conference.sub_headline + "<font/>"));
                }
                else
                {
                    conferenceTitle.setText(conference.name);
                }

                if(conference.show_daily_schedule)
                {
                    conferenceMenuItems.add("Daily Schedule");
                }
                else
                {
                    try
                    {
                        PersistenceManager.writeObject(getApplicationContext(), Constants.CONFERENCE_EVENT_FILE_NAME, null);
                        PersistenceManager.writeObject(getApplicationContext(), Constants.CONFERENCE_MY_SCHEDULE_FILE_NAME, null);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
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
