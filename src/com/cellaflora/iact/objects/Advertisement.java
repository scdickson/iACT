package com.cellaflora.iact.objects;

import java.io.Serializable;

/**
 * Created by sdickson on 7/17/13.
 */

public class Advertisement implements Serializable
{
    public String objectId;
    public String image_url;
    public String ad_url;
    public String lastModified;

    public String toString()
    {
        return (objectId + ", " + ad_url + ", " + image_url + ", " + lastModified);
    }
}
