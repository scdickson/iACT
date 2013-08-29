package com.cellaflora.iact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sdickson on 7/21/13.
 */
public class LSAdapter extends BaseAdapter
{
    Context context;
    ArrayList<Post> LSItems;
    LayoutInflater inflater;

    public LSAdapter(Context context, ArrayList<Post> LSItems)
    {
        this.context = context;
        this.LSItems = LSItems;
    }

    public int getCount()
    {
        return LSItems.size();
    }

    public Object getItem(int position)
    {
        return LSItems.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.ls_list_item, parent, false);
        ImageView ls_image = (ImageView) itemView.findViewById(R.id.ls_image);
        TextView ls_headline = (TextView) itemView.findViewById(R.id.ls_headline);
        TextView ls_desc = (TextView) itemView.findViewById(R.id.ls_desc);

        Post tmp = LSItems.get(position);

        if(tmp.headline != null)
        {
            ls_headline.setText(tmp.headline);
        }
        else
        {
            ls_headline.setVisibility(View.GONE);
        }

        if(tmp.description != null)
        {
            ls_desc.setText(tmp.description);
        }
        else
        {
            ls_desc.setVisibility(View.GONE);
        }

        return itemView;
    }
}
