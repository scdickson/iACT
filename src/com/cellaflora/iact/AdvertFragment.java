package com.cellaflora.iact;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cellaflora.iact.objects.Advertisement;
import com.cellaflora.iact.support.PersistenceManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
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
    View view;
    public ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
    public AdvertTimer adt = null;
    Animation clear, load;

    public AdvertFragment()
    {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.advert_fragment, container, false);
        clear = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_left);
        load = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_right);
        return view;
    }

    public void loadAds()
    {
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

                        try
                        {
                            PersistenceManager.writeObject(view.getContext(), Constants.AD_FILE_NAME, ads);
                        }
                        catch(Exception ex)
                        {
                            e.printStackTrace();
                        }

                        displayAds();
                    }
                }
            });
        }
    }

    public void onResume()
    {
        super.onResume();

        try
        {
            File f = new File(view.getContext().getFilesDir(), Constants.AD_FILE_NAME);
            if((f.lastModified() + (Constants.AD_UPDATE_INTERVAL * 60 * 1000)) >= System.currentTimeMillis())
            {
                ads = (ArrayList<Advertisement>) PersistenceManager.readObject(view.getContext(), Constants.AD_FILE_NAME);
                displayAds();
            }
            else
            {
                loadAds();
            }
        }
        catch(Exception e)
        {
            loadAds();
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
                    sleep(Constants.AD_ROTATE_FREQUENCY * 1000);
                }
                catch(Exception e){}
            }
        }

    }

    public void onPause()
    {
        super.onPause();

        if(adt != null)
        {
            try
            {
                adt.running = false;
                adt.join();
                adt = null;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class loadAd extends AsyncTask<ImageView, Integer, Void>
    {
        ImageView ad;
        Bitmap image;
        Advertisement tmp;

        protected Void doInBackground(ImageView... arg0)
        {
            try
            {
                ad = arg0[0];

                Random generator = new Random(System.currentTimeMillis());
                tmp = ads.get(generator.nextInt(ads.size()));

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
                clear.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation)
                    {

                    }
                    public void onAnimationEnd(Animation animation)
                    {
                        ad.setImageBitmap(image);
                        ad.startAnimation(load);
                    }
                });
                ad.startAnimation(clear);
                ad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        if(tmp.ad_url != null && !tmp.ad_url.isEmpty())
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(tmp.ad_url));
                            startActivity(intent);
                        }
                    }
                });

            }
        }
    }



}
