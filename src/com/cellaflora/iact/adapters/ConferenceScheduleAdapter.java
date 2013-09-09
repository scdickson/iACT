package com.cellaflora.iact.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cellaflora.iact.ConferenceSchedule;
import com.cellaflora.iact.ConferenceSchedulePage;
import com.cellaflora.iact.R;
import com.cellaflora.iact.objects.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceScheduleAdapter extends BaseAdapter
{
    Context context;
    LayoutInflater inflater;
    ArrayList<Event> events;
    ConferenceSchedule cs;

    public ConferenceScheduleAdapter(Context context, ArrayList<Event> events, ConferenceSchedule cs)
    {
        this.context = context;
        this.events = events;
        this.cs = cs;
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

    public void setDate(Date date)
    {

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
        TextView txtTitle, txtTime, txtDescription, txtAction;
        txtTitle = (TextView) itemView.findViewById(R.id.schedule_event_title);
        txtTime = (TextView) itemView.findViewById(R.id.schedule_event_time);
        txtDescription = (TextView) itemView.findViewById(R.id.schedule_event_description);
        txtAction = (TextView) itemView.findViewById(R.id.schedule_event_action);
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

        txtAction.setOnClickListener(new myScheduleListener(evt, txtAction));
        if(evt.isInPersonalSchedule)
        {
            txtAction.setText("Remove from personal schedule");
        }
        else
        {
            txtAction.setText("Add to personal schedule");
        }


        return itemView;
    }

    private class myScheduleListener implements View.OnClickListener
    {
        Event evt;
        TextView txtAction;

        public myScheduleListener(Event evt, TextView txtAction)
        {
            this.evt = evt;
            this.txtAction = txtAction;
        }

        public void onClick(View view)
        {
            if(evt.isInPersonalSchedule)
            {
                evt.isInPersonalSchedule = false;
                txtAction.setText("Add to personal schedule");

                if(ConferenceSchedule.event_selector == ConferenceSchedule.EVENTS_PERSONAL)
                {
                    events.remove(evt);
                    notifyDataSetChanged();
                }
            }
            else
            {
                evt.isInPersonalSchedule = true;
                txtAction.setText("Remove from personal schedule");
            }
        }
    }
}
