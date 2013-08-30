package com.cellaflora.iact;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity
{

    private ListView mainMenu;
    private ImageView infoButton;
    public static ArrayList<String> menuItems;
    //public static final String[] menuItems = {"News and Legislative Summary", "Legislative Team", "Find Your Legislator", "IACT Legislative Day", "IACT Twitter Feed", null};
    public static Conference conference;
    public static ConferenceListener conference_listener = null;
    private MenuAdapter adapter;
    public static boolean conference_enabled = false;


    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        Parse.initialize(this, Constants.PARSE_APPLICATION_ID, Constants.PARSE_CLIENT_KEY);
        ParseAnalytics.trackAppOpened(getIntent());
        PushService.setDefaultPushCallback(this, LegislativeSummary.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        setContentView(R.layout.activity_main);


        menuItems = new ArrayList<String>();
        menuItems.add("News and Legislative Summary");
        menuItems.add("Calendar of Events");
        menuItems.add("IACT Legislative Hub");
        menuItems.add("Twitter");
        mainMenu = (ListView) findViewById(R.id.main_menu);
        adapter = new MenuAdapter(this, menuItems);
        mainMenu.setAdapter(adapter);
        MenuItemClickListener menuListener = new MenuItemClickListener();
        mainMenu.setOnItemClickListener(menuListener);

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageButton ib = (ImageButton) findViewById(R.id.toggle_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent().setClass(getApplicationContext(), LegislativeSummary.class);
                if(intent != null)
                {
                    startActivity(intent);
                }
            }
        });

        infoButton = (ImageView) findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Load info fragment
            }
        });
	}

    public void onResume()
    {
        super.onResume();

        if(conference_listener == null)
        {
            conference_listener = new ConferenceListener(new ConferenceHandler());
        }

        conference_listener.getConferenceStatus();

    }

    private long getDirSize()
    {
        long size = 0;
        File[] files = getFilesDir().listFiles();

        for(File f : files)
        {
            size += f.length();
        }

        return size;
    }

    protected void onDestroy()
    {
        super.onDestroy();

        try
        {
            if(getDirSize() >= (Constants.MAX_CACHE_SIZE * 1000000))
            {
                ArrayList<File> files = new ArrayList(Arrays.asList(getFilesDir().listFiles()));
                Collections.sort(files, new fileComparator());
                int i = 0;

                //Log.d("err", "Size before: " + getDirSize());
                while(getDirSize() >= ((Constants.MAX_CACHE_SIZE * 1000000) - (Constants.CACHE_DECREASE_AMOUNT * 1000000)) && i < files.size())
                {
                    if(!(files.get(i).getName().contains("iact_saved")))
                    {
                        //Log.d("err", "Deleting " + files.get(i).getName() + " to free " + files.get(i).length());
                        files.get(i).delete();
                    }
                    i++;
                }
                //Log.d("err", "Size after: " + getDirSize());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }



    private void selectItem(int position)
    {
        Intent intent = null;

        if(conference_enabled)
        {
            switch(position)
            {
                case 0: //Conference Pages
                    intent = null;
                    break;
                case 1: //News and Legislative Summary
                    intent = new Intent(this, LegislativeSummary.class);
                    break;
                case 2: //Calendar of Events
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.CALENDAR_OF_EVENTS_URL);
                    break;
                case 3: //IACT Legislative Hub
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATIVE_HUB_URL);
                    break;
                case 4: //Twitter
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.TWITTER_URL);
                    break;
            }
        }
        else
        {
            switch(position)
            {
                case 0: //News and Legislative Summary
                    intent = new Intent(this, LegislativeSummary.class);
                    break;
                case 1: //Calendar of Events
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.CALENDAR_OF_EVENTS_URL);
                    break;
                case 2: //IACT Legislative Hub
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATIVE_HUB_URL);
                    break;
                case 3: //Twitter
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.TWITTER_URL);
                    break;
            }
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

    private void setupMenu()
    {
        menuItems.add("News and Legislative Summary");
        menuItems.add("Calendar of Events");
        menuItems.add("IACT Legislative Hub");
        menuItems.add("Twitter");
        mainMenu = (ListView) findViewById(R.id.main_menu);
        adapter = new MenuAdapter(this, menuItems);
        mainMenu.setAdapter(adapter);
        MenuItemClickListener menuListener = new MenuItemClickListener();
        mainMenu.setOnItemClickListener(menuListener);
    }

    private class ConferenceHandler extends Handler
    {
        public void handleMessage(Message msg)
        {
            conference = (Conference) msg.getData().getSerializable("CONFERENCE_DATA");

            if(conference != null && conference.enabled && !conference.name.isEmpty())
            {
                if(!conference_enabled)
                {
                    conference_enabled = true;
                    menuItems = new ArrayList<String>();
                    menuItems.add(conference.name);
                    setupMenu();
                }
            }
            else
            {
                if(conference_enabled)
                {
                    conference_enabled = false;
                    menuItems = new ArrayList<String>();
                    setupMenu();
                }
            }
        }
    }

}
