package com.cellaflora.iact;

/**
 * Created by sdickson on 8/29/13.
 */
public class Constants
{
    //Constants for Main Activity
    public static final String PARSE_APPLICATION_ID = "8mEnOlHBuG0fAFFKLgjVW7HiXicU9OfapLEmK720";
    public static final String PARSE_CLIENT_KEY = "2tKeH9HPQkpgrKCYWOl7XiFddgg6xSSIomfYf3z7";
    public static final int MAX_CACHE_SIZE = 30; //In Megabytes!
    public static final int CACHE_DECREASE_AMOUNT = 10; //In Megabytes!
    public static final int SPLASH_DELAY = 2; //In seconds!

    //Constants for About Animation
    public static final String IACT_IMAGE_LINK = "http://www.citiesandtowns.org";
    public static final String CELLAFLORA_IMAGE_LINK = "http://www.cellaflora.com";
    public static final int ABOUT_FADE_DELAY = 750; //In milliseconds!
    public static final int ABOUT_HOLD_DELAY = 2000; //In milliseconds!

    //Constants for Web Views
    public static final String CALENDAR_OF_EVENTS_URL = "http://www.citiesandtowns.org/IACTGoEvents/tabid/16102/Default.aspx";
    public static final String LEGISLATIVE_HUB_URL = "http://www.citiesandtowns.org/IACTGoLegislativeHub/tabid/16103/Default.aspx";
    public static final String TWITTER_URL = "https://mobile.twitter.com/INCitiesTowns";

    //Constants for Conference Schedule
    public static final String SAVED_CONFERENCE_FLAGS = "ict_saved_flags";
    public static final String CONFERENCE_EVENT_FILE_NAME = "iact_saved_events";
    public static final String CONFERENCE_MY_SCHEDULE_FILE_NAME = "iact_saved_myschedule";
    public static final int CONFERENCE_EVENTS_REPLACE_INTERVAL = 60;

    //Constants for Banner Ad
    public static final String AD_FILE_NAME = "iact_saved_ads";
    public static final int AD_UPDATE_INTERVAL = 5; // In minutes!
    public static final int AD_ROTATE_FREQUENCY = 30; //In seconds!

    //Constants for News and Legislative Summary
    public static final String NEWS_FILE_NAME = "iact_saved_news";
    public static final int NEWS_UPDATE_INTERVAL = 60; //In minutes!
    public static final int IMAGE_BUFFER_SIZE = 10; //In Megabytes!

}
