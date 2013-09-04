package com.cellaflora.iact.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cellaflora.iact.R;

import java.util.ArrayList;

/**
 * Created by sdickson on 9/3/13.
 */
public class ConferenceMenuAdapter extends BaseAdapter
{
    Context context;
    LayoutInflater inflater;
    ArrayList<String> menuItems;

    public ConferenceMenuAdapter(Context context, ArrayList<String> menuItems)
    {
        this.context = context;
        this.menuItems = menuItems;
    }

    public int getCount()
    {
        return menuItems.size();
    }

    public Object getItem(int position)
    {
        return menuItems.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.conference_list_row, parent, false);
        TextView txtMenuItem = (TextView) itemView.findViewById(R.id.conference_menu_item);
        txtMenuItem.setText(menuItems.get(position));
        return itemView;
    }

}
