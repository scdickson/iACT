package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.ConferenceScheduleAdapter;
import com.cellaflora.iact.objects.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by sdickson on 9/12/13.
 */
public class ConferenceEventDetail extends Activity
{
    public static int SCHEDULE_ADD_IMAGE, SCHEDULE_REMOVE_IMAGE;

    Event evt;
    TextView txtTitle, txtTime, txtDescription, txtAction;
    ImageView imgAction;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_event_detail);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        txtTitle = (TextView) findViewById(R.id.schedule_detail_event_title);
        txtTime = (TextView) findViewById(R.id.schedule_detail_event_time);
        txtDescription = (TextView) findViewById(R.id.schedule_detail_event_description);
        txtAction = (TextView) findViewById(R.id.schedule_detail_event_action);
        imgAction = (ImageView) findViewById(R.id.schedule_detail_event_action_image);
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
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma", Locale.US);

                if(evt.end_time == null)
                {
                    txtTime.setText(timeFormat.format(evt.start_time).toLowerCase());
                }
                else
                {
                    if(evt.end_time.after(evt.start_time))
                    {
                        txtTime.setText(timeFormat.format(evt.start_time).toLowerCase() + " - " + timeFormat.format(evt.end_time).toLowerCase());
                    }
                    else
                    {
                        txtTime.setText(timeFormat.format(evt.start_time).toLowerCase());
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

            txtAction.setOnClickListener(new myScheduleListener(evt, txtAction, imgAction));
            if(evt.isInPersonalSchedule)
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
            if(evt.isInPersonalSchedule)
            {
                evt.isInPersonalSchedule = false;
                txtAction.setText("Add to personal schedule");
                imgAction.setImageResource(SCHEDULE_ADD_IMAGE);
            }
            else
            {
                evt.isInPersonalSchedule = true;
                txtAction.setText("Remove from personal schedule");
                imgAction.setImageResource(SCHEDULE_REMOVE_IMAGE);
            }
        }
    }

}
