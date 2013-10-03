package com.cellaflora.iact;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sdickson on 7/17/13.
 */
public class AdvertFragment extends Fragment
{
    View view;
    public ArrayList<Advertisement> ads;
    Animation clear, load;
    int imageViewWidth;
    Timer timer;
    ImageView adBanner;

    public AdvertFragment()
    {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.advert_fragment, container, false);
        clear = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_left);
        load = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_right);
        WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        imageViewWidth = display.getWidth();
        adBanner = (ImageView) view.findViewById(R.id.ad);
        return view;
    }

    public void loadAds()
    {
            ads = new ArrayList<Advertisement>();
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
                            tmp.objectId = parse.getObjectId();
                            tmp.ad_url = parse.getString("Link");
                            tmp.lastModified = parse.getString("updatedAt");

                            ParseFile file = (ParseFile) parse.get("Ad_Image_File");
                            if(file != null)
                            {
                                tmp.image_url = file.getUrl();
                            }

                            ads.add(tmp);
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

    public void displayAds()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {

            boolean running = true;
            Random generator = new Random(System.currentTimeMillis());
            Advertisement currentAd = null;

            public void run()
            {
                    if(ads.size() <= 0)
                    {
                        running = false;
                        return;
                    }

                    Advertisement tmp = ads.get(generator.nextInt(ads.size()));

                    if(currentAd != null)
                    {
                        while(tmp.objectId.equals(currentAd.objectId))
                        {
                            tmp = ads.get(generator.nextInt(ads.size()));
                        }
                    }

                    currentAd = tmp;


                    try
                    {
                        File f = new File(view.getContext().getFilesDir() + "/" + tmp.objectId);
                        if(f != null && f.exists())
                        {
                            new loadAd().execute(adBanner, f, tmp);
                        }
                        else
                        {
                            new loadAdFromParse().execute(adBanner, tmp);
                        }
                    }
                    catch(Exception e)
                    {
                        new loadAdFromParse().execute(adBanner, tmp);
                    }

            }

        },
                0,
                Constants.AD_ROTATE_FREQUENCY * 1000);
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



    private float getBitmapScalingFactor(Bitmap bm)
    {
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }

    public void onPause()
    {
        super.onPause();
        timer.cancel();
    }


    private class loadAdFromParse extends AsyncTask<Object, Integer, Void>
    {
        ImageView ad;
        Bitmap image;
        Advertisement tmp;

        protected Void doInBackground(Object... arg0)
        {
            try
            {
                ad = (ImageView) arg0[0];
                tmp = (Advertisement) arg0[1];

                ad.setTag(tmp.ad_url);
                URL url = new URL(tmp.image_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                image = BitmapFactory.decodeStream(input);

                if(imageViewWidth < 640)
                {
                    int scaleHeight = (int) (image.getHeight() * getBitmapScalingFactor(image));
                    int scaleWidth = (int) (image.getWidth() * getBitmapScalingFactor(image));
                    image = Bitmap.createScaledBitmap(image, scaleWidth, scaleHeight, true);
                }

                try {
                    FileOutputStream out = new FileOutputStream(view.getContext().getFilesDir() + "/" + tmp.objectId);
                    image.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                            try
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(tmp.ad_url));
                                startActivity(intent);
                            }
                            catch(ActivityNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        }
    }

    private class loadAd extends AsyncTask<Object, Integer, Void>
    {
        ImageView ad;
        Bitmap image;
        File f;
        Advertisement tmp;

        protected Void doInBackground(Object... arg0)
        {
            try
            {
                ad = (ImageView) arg0[0];
                f = (File) arg0[1];
                tmp = (Advertisement) arg0[2];
                image = BitmapFactory.decodeStream(new FileInputStream(f));
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
                            try
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(tmp.ad_url));
                                startActivity(intent);
                            }
                            catch(ActivityNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        }
    }



}
