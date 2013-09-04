package com.cellaflora.iact;

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
            ImageView imgMenuItemImage = (ImageView) itemView.findViewById(R.id.menu_item_image);
            txtMenuItemText.setText(getItem(position).toString());
            int id = -1;

            if(MainActivity.conference_enabled)
            {
                switch(position)
                {
                    case 0:
                        //CONFERENCE IMAGE
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/home", null, null);
                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_right);
                        itemView.startAnimation(animation);
                        break;
                    case 1:
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/home_off", null, null);
                        break;
                    case 2:
                        //CALENDAR IMAGE
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/initiatives_off", null, null);
                        break;
                    case 3:
                        //HUB IMAGE
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/find_off", null, null);
                        break;
                    case 4:
                        //Twitter Image
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/twitter_off", null, null);
                        break;
                }
            }
            else
            {
                switch(position)
                {
                    case 0:
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/home_off", null, null);
                        break;
                    case 1:
                        //CALENDAR IMAGE
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/initiatives_off", null, null);
                        break;
                    case 2:
                        //HUB IMAGE
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/find_off", null, null);
                        break;
                    case 3:
                        //Twitter Image
                        //id = context.getResources().getIdentifier("com.cellaflora.iact:drawable/twitter_off", null, null);
                        break;
                }
            }

            imgMenuItemImage.setImageResource(id);
            Typeface HelveticaNeue_Bold = Typeface.createFromAsset(context.getAssets(), "helveticaneue-bold.ttf");
            txtMenuItemText.setTypeface(HelveticaNeue_Bold);
        }
        return itemView;
    }
}
