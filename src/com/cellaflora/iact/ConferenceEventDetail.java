package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.PersistenceManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by sdickson on 9/12/13.
 */
public class ConferenceEventDetail extends Activity
{
    public static int SCHEDULE_ADD_IMAGE, SCHEDULE_REMOVE_IMAGE;

    Context context;
    Event evt;
    TextView txtTitle, txtTime, txtDescription, txtAction, txtSpeakers, txtLocation;
    RelativeLayout bottomLayout;
    ImageView imgAction;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_event_detail);
        context = this;
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.iact_nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        txtTitle = (TextView) findViewById(R.id.schedule_detail_event_title);
        txtTitle.setTypeface(MainActivity.Futura);
        txtTime = (TextView) findViewById(R.id.schedule_detail_event_time);
        txtTime.setTypeface(MainActivity.Futura);
        txtDescription = (TextView) findViewById(R.id.schedule_detail_event_description);
        txtDescription.setTypeface(MainActivity.Futura);
        txtAction = (TextView) findViewById(R.id.schedule_detail_event_action);
        txtAction.setTypeface(MainActivity.Futura);
        txtLocation = (TextView) findViewById(R.id.schedule_detail_event_location);
        txtLocation.setTypeface(MainActivity.Futura);
        txtSpeakers = (TextView) findViewById(R.id.schedule_detail_event_speakers);
        txtSpeakers.setTypeface(MainActivity.Futura);
        imgAction = (ImageView) findViewById(R.id.schedule_detail_event_action_image);
        bottomLayout = (RelativeLayout) findViewById(R.id.schedule_event_detail_bottom_layout);
        SCHEDULE_ADD_IMAGE = getResources().getIdentifier("com.cellaflora.iact:drawable/add_cal", null, null);
        SCHEDULE_REMOVE_IMAGE = getResources().getIdentifier("com.cellaflora.iact:drawable/remove_cal", null, null);

        Intent intent = getIntent();
        if(intent != null)
        {
            evt = (Event) intent.getSerializableExtra("EVENT");
        }
    }

    public void onResume()
    {
        super.onResume();

        if(evt != null)
        {
            if(evt.title != null && !evt.title.isEmpty())
            {
                txtTitle.setText(evt.title);
            }
            else
            {
                txtTitle.setVisibility(View.GONE);
            }

            if(evt.start_time != null)
            {
                SimpleDateFormat longFormat = new SimpleDateFormat("ccc, MMMM d, h:mm", Locale.US);
                SimpleDateFormat shortFormat = new SimpleDateFormat("h:mm", Locale.US);

                if(evt.end_time == null)
                {
                    txtTime.setText(longFormat.format(evt.start_time).toLowerCase());
                }
                else
                {
                    if(evt.end_time.after(evt.start_time))
                    {
                        txtTime.setText(longFormat.format(evt.start_time) + " - " + shortFormat.format(evt.end_time));
                    }
                    else
                    {
                        txtTime.setText(longFormat.format(evt.start_time));
                    }
                }
            }
            else
            {
                txtTime.setVisibility(View.GONE);
            }

            if(evt.description != null && !evt.description.isEmpty())
            {
                txtDescription.setText(evt.description);
            }
            else
            {
                txtDescription.setVisibility(View.GONE);
            }

            if(evt.location != null && !evt.location.isEmpty())
            {
                txtLocation.setText(evt.location);
            }
            else
            {
                txtLocation.setVisibility(View.GONE);
            }

            if(evt.speakers != null && !evt.speakers.isEmpty())
            {
                txtSpeakers.setText("Speakers:\n" + evt.speakers);
            }
            else
            {
                txtSpeakers.setVisibility(View.GONE);
            }

            bottomLayout.setOnClickListener(new myScheduleListener(evt, txtAction, imgAction));
            if(isInPersonalSchedule(evt))
            {
                txtAction.setText("Remove from personal schedule");
                imgAction.setImageResource(SCHEDULE_REMOVE_IMAGE);
            }
            else
            {
                txtAction.setText("Add to personal schedule");
                imgAction.setImageResource(SCHEDULE_ADD_IMAGE);
            }
        }
    }

    private boolean isInPersonalSchedule(Event evt)
    {
        for(Event e : ConferenceSchedule.mySchedule)
        {
            if(e.equals(evt))
            {
                return true;
            }
        }

        return false;
    }

    private class myScheduleListener implements View.OnClickListener
    {
        Event evt;
        TextView txtAction;
        ImageView imgAction;

        public myScheduleListener(Event evt, TextView txtAction, ImageView imgAction)
        {
            this.evt = evt;
            this.txtAction = txtAction;
            this.imgAction = imgAction;
        }

        public void onClick(View view)
        {
            if(isInPersonalSchedule(evt))
            {
                ConferenceSchedule.mySchedule.remove(evt);
                txtAction.setText("Add to personal schedule");
                imgAction.setImageResource(SCHEDULE_ADD_IMAGE);
            }
            else
            {
                ConferenceSchedule.mySchedule.add(evt);
                txtAction.setText("Remove from personal schedule");
                imgAction.setImageResource(SCHEDULE_REMOVE_IMAGE);
            }

            try
            {
                PersistenceManager.writeObject(context, Constants.CONFERENCE_MY_SCHEDULE_FILE_NAME, ConferenceSchedule.mySchedule);
            }
            catch(Exception e){}
        }
    }

}
