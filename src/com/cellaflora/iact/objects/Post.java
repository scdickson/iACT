package com.cellaflora.iact.objects;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sdickson on 7/21/13.
 */
public class Post implements Serializable
{
    public String objectId;
    public String caption;
    public String doc_url;
    public String photo_url;
    public String description;
    public String headline;
    public Date update_time;
}
