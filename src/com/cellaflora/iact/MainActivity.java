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

import java.util.ArrayList;
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
        menuItems.add("Legislative Team");
        menuItems.add("Find Your Legislator");
        menuItems.add("IACT Legislative Day");
        menuItems.add("IACT Twitter Feed");
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

    public void onPause()
    {
        super.onPause();

        if(conference_listener != null)
        {
            conference_listener.setRunning(false);
        }
    }

    public void onResume()
    {
        super.onResume();

        if(conference_listener != null)
        {
            conference_listener.setRunning(true);
        }
        else
        {

                if(conference == null)
                {
                    conference = new Conference();
                }

                conference_listener = new ConferenceListener(conference, new ConferenceHandler());
        }
    }


    private void selectItem(int position)
    {
        Intent intent = null;

        switch(position)
        {
            case 0:
                intent = new Intent(this, LegislativeSummary.class);
                break;
            case 1:
                if(Constants.LOAD_EXTERNAL)
                {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LEGISLATIVE_TEAM_URL));
                }
                else
                {
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATIVE_TEAM_URL);
                }
                break;
            case 2:
                if(Constants.LOAD_EXTERNAL)
                {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LEGISLATIVE_INITIATIVES_URL));
                }
                else
                {
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATIVE_INITIATIVES_URL);
                }
                break;
            case 3:
                if(Constants.LOAD_EXTERNAL)
                {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LEGISLATOR_FIND_URL));
                }
                else
                {
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATOR_FIND_URL);
                }
                break;
            case 4:
                if(Constants.LOAD_EXTERNAL)
                {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LEGISLATIVE_DAY_URL));
                }
                else
                {
                    intent = new Intent(this, WebContentView.class);
                    intent.putExtra("URL", Constants.LEGISLATIVE_DAY_URL);
                }
                break;
            case 5:
                break;
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
            //final Animation in = new AlphaAnimation(0.0f, 1.0f);
            //in.setDuration(1200);

            if(msg.getData().getInt("CONFERENCE_STATUS") == ConferenceListener.CONFERENCE_ENABLED)
            {
                if(conference != null)
                {
                    conference_enabled = true;
                    menuItems.add(conference.name);
                    adapter.notifyDataSetChanged();
                    mainMenu.invalidateViews();
                    //mainMenu.startAnimation(in);
                }
            }
            else
            {
                if(conference != null)
                {
                    conference_enabled = false;
                    menuItems.remove(conference.name);
                    adapter.notifyDataSetChanged();
                    mainMenu.invalidateViews();
                    //mainMenu.startAnimation(in);
                }
            }
        }
    }
}
