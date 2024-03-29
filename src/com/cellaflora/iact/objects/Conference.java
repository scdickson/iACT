package com.cellaflora.iact.objects;

import java.io.Serializable;

/**
 * Created by sdickson on 8/29/13.
 */
public class Conference implements Serializable
{
    public String name;
    public String highlights_link;
    public String maps_link;
    public String sponsors_link;
    public String sub_headline;
    public String name_short;
    public boolean enabled = false;
    public boolean show_daily_schedule = false;
    public boolean show_event_highlights = false;
    public boolean show_event_maps = false;
    public boolean show_event_sponsors = false;
    public String event_highlights_url;
    public String event_maps_url;
    public String event_sponsors_url;
}
