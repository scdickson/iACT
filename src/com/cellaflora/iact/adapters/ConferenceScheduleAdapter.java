package com.cellaflora.iact.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellaflora.iact.ConferenceSchedule;
import com.cellaflora.iact.ConferenceSchedulePage;
import com.cellaflora.iact.Constants;
import com.cellaflora.iact.MainActivity;
import com.cellaflora.iact.R;
import com.cellaflora.iact.objects.Event;
import com.cellaflora.iact.support.PersistenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceScheduleAdapter extends BaseAdapter
{
    public static int SCHEDULE_ADD_IMAGE, SCHEDULE_REMOVE_IMAGE;

    Context context;
    LayoutInflater inflater;
    ArrayList<Event> events;
    ConferenceSchedulePage csp;

    public ConferenceScheduleAdapter(Context context, ArrayList<Event> events, ConferenceSchedulePage csp)
    {
        this.context = context;
        this.events = events;
        this.csp = csp;

        SCHEDULE_ADD_IMAGE = context.getResources().getIdentifier("com.cellaflora.iact:drawable/add_cal", null, null);
        SCHEDULE_REMOVE_IMAGE = context.getResources().getIdentifier("com.cellaflora.iact:drawable/remove_cal", null, null);
    }

    public void clearContent()
    {
        events = new ArrayList<Event>();
    }

    public void setContent(ArrayList<Event> events)
    {
        clearContent();
        this.events = events;
    }

    public int getCount()
    {
        return events.size();
    }

    public Object getItem(int position)
    {
        return events.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.conference_schedule_list_row, parent, false);
        TextView txtTitle, txtTime, txtDescription, txtAction, txtLocation;
        ImageView imgAction = (ImageView) itemView.findViewById(R.id.schedule_event_action_image);
        txtTitle = (TextView) itemView.findViewById(R.id.schedule_event_title);
        txtTitle.setTypeface(MainActivity.Futura);
        txtTime = (TextView) itemView.findViewById(R.id.schedule_event_time);
        txtTime.setTypeface(MainActivity.Futura);
        txtLocation = (TextView) itemView.findViewById(R.id.schedule_event_locaton);
        txtLocation.setTypeface(MainActivity.Futura);
        txtDescription = (TextView) itemView.findViewById(R.id.schedule_event_description);
        txtDescription.setTypeface(MainActivity.Futura);
        txtAction = (TextView) itemView.findViewById(R.id.schedule_event_action);
        txtAction.setTypeface(MainActivity.Futura);
        Event evt = (Event) getItem(position);

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
            SimpleDateFormat timeFormat = new SimpleDateFormat("k:mm", Locale.US);

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

        if(evt.location != null && !evt.location.isEmpty())
        {
            txtLocation.setText(evt.location);
        }
        else
        {
            txtLocation.setVisibility(View.GONE);
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


        return itemView;
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

                if(ConferenceSchedule.event_selector == ConferenceSchedule.EVENTS_PERSONAL)
                {
                    events.remove(evt);
                    notifyDataSetChanged();
                }

                if(events.size() == 0)
                {
                    csp.setNoEvents();
                }
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
