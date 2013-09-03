package com.cellaflora.iact;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sdickson on 9/3/13.
 */
public class Event implements Serializable
{
    public String objectId;
    public String title;
    public String description;
    public Date start_time, end_time;
    public String location;
    public String speakers;
}
