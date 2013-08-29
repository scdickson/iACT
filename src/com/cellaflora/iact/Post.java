package com.cellaflora.iact;

import java.io.Serializable;

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
    public String update_time;

    public String toString()
    {
        return (objectId + ", " + headline + ", " + caption + ", " + description + ", " + update_time + ", " + doc_url + ", " + photo_url);
    }
}
