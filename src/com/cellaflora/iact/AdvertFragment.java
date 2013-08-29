package com.cellaflora.iact;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by sdickson on 7/17/13.
 */
public class AdvertFragment extends Fragment
{
    public static final int AD_ROTATE_FREQUENCY = 30; //In seconds!
    public ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
    public AdvertTimer adt = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.advert_fragment, container, false);
        return view;
    }

    public void onResume()
    {
        super.onResume();

        if(adt == null)
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Advertisement");
            query.addDescendingOrder("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> result, ParseException e)
                {
                    if(e == null)
                    {
                        for(ParseObject parse : result)
                        {
                            Advertisement tmp = new Advertisement();
                            tmp.objectId = parse.getString("objectId");
                            tmp.ad_url = parse.getString("Link");
                            tmp.lastModified = parse.getString("updatedAt");

                            ParseFile file = (ParseFile) parse.get("Ad_Image_File");
                            if(file != null)
                            {
                                tmp.image_url = file.getUrl();
                            }

                            ads.add(tmp);
                            //Log.d("Ad", tmp.toString());
                        }

                        displayAds();
                    }
                }
            });
        }
    }

    public void displayAds()
    {
        AdvertTimer adt = new AdvertTimer((ImageView) getActivity().findViewById(R.id.ad));
        adt.start();
    }

    private class AdvertTimer extends Thread
    {
        ImageView adBanner;
        boolean running = true;

        public AdvertTimer(ImageView adBanner)
        {
            this.adBanner = adBanner;
        }

        public void run()
        {
            while(running)
            {
                new loadAd().execute(adBanner);
                try
                {
                    sleep(AdvertFragment.AD_ROTATE_FREQUENCY * 1000);
                }
                catch(Exception e){}
            }
        }

    }
    private class loadAd extends AsyncTask<ImageView, Integer, Void>
    {
        ImageView ad;
        Bitmap image;

        protected Void doInBackground(ImageView... arg0)
        {
            try
            {
                ad = arg0[0];

                Random generator = new Random(System.currentTimeMillis());
                Advertisement tmp = ads.get(generator.nextInt(ads.size()));

                while(ad.getTag().equals(tmp.ad_url))
                {
                    tmp = ads.get(generator.nextInt(ads.size()));
                }

                ad.setTag(tmp.ad_url);
                URL url = new URL(tmp.image_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                image = BitmapFactory.decodeStream(input);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            if(image != null)
            {

                ad.setImageBitmap(image);
            }
        }
    }



}
