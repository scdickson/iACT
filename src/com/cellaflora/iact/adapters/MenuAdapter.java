package com.cellaflora.iact.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellaflora.iact.MainActivity;
import com.cellaflora.iact.R;

import java.util.ArrayList;

/**
 * Created by sdickson on 7/17/13.
 */
public class MenuAdapter extends BaseAdapter
{
    Context context;
    ArrayList<String> menuItems;
    LayoutInflater inflater;

    public MenuAdapter(Context context, ArrayList<String> menuItems)
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
        View itemView = inflater.inflate(R.layout.menu_list_item, parent, false);

        if(menuItems.get(position) != null)
        {
            TextView txtMenuItemText = (TextView) itemView.findViewById(R.id.menu_item_text);
            txtMenuItemText.setText(getItem(position).toString());
            txtMenuItemText.setTypeface(MainActivity.Futura);
            int id = -1;

            if(MainActivity.conference_enabled)
            {
                if(position == 0)
                {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_right);
                    itemView.startAnimation(animation);
                }
            }
        }
        return itemView;
    }
}
