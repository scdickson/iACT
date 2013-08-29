package com.cellaflora.iact;

/**
 * Created by sdickson on 7/17/13.
 */

public class Advertisement
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
