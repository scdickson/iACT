package com.cellaflora.iact.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cellaflora.iact.R;
import com.cellaflora.iact.objects.Event;

import java.util.ArrayList;

/**
 * Created by sdickson on 9/4/13.
 */
public class ConferenceScheduleAdapter extends BaseAdapter
{
    Context context;
    LayoutInflater inflater;
    ArrayList<Event> events;

    public ConferenceScheduleAdapter(Context context, ArrayList<Event> events)
    {
        this.context = context;
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
        return itemView;
    }
}
