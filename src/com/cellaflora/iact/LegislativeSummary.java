package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cellaflora.iact.adapters.LSAdapter;
import com.cellaflora.iact.objects.Post;
import com.cellaflora.iact.support.PersistenceManager;
import com.cellaflora.iact.support.PullToRefreshBase;
import com.cellaflora.iact.support.PullToRefreshListView;
import com.cellaflora.iact.support.RefreshListView;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sdickson on 7/21/13.
 */
public class LegislativeSummary extends Activity
{
    public static final int TYPE_PDF = 0;
    public static final int TYPE_DOC = 1;

    private ArrayList<Post> posts = null;
    private RefreshListView lsList;
    private TextView noNews;
    private ProgressDialog progressDialog, pdfProgress;
    private Context context;
    private loadDocument lp;
    private Parcelable state;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.legislative_summary_activity);
        context = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        pdfProgress = new ProgressDialog(this);
        pdfProgress.setTitle("");
        pdfProgress.setMessage("Loading Document...");
        pdfProgress.setIndeterminate(false);
        pdfProgress.setMax(100);
        pdfProgress.setProgressNumberFormat(null);
        pdfProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        noNews = (TextView) findViewById(R.id.no_news);
        noNews.setTypeface(MainActivity.Futura);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.iact_nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageButton ib = (ImageButton) findViewById(R.id.toggle_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context, MainActivity.class);
                if(intent != null)
                {
                    startActivity(intent);
                }
            }
        });
    }

    public void onPause()
    {
        super.onPause();

        if(lsList != null)
        {
            state = lsList.onSaveInstanceState();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void onResume()
    {
        super.onResume();

        if(state != null)
        {
            //Log.d("fatal", "restore");
            lsList.onRestoreInstanceState(state);
            return;
        }

            try
            {
                File f = new File(getFilesDir(), Constants.NEWS_FILE_NAME);
                if((f.lastModified() + (Constants.NEWS_UPDATE_INTERVAL * 60 * 1000)) >= System.currentTimeMillis())
                {
                    posts = (ArrayList<Post>) PersistenceManager.readObject(this, Constants.NEWS_FILE_NAME);
                    lsList = (RefreshListView) findViewById(R.id.lsList);
                    LSAdapter adapter = new LSAdapter(this, posts, noNews);
                    lsList.setAdapter(adapter);
                    MenuItemClickListener menuListener = new MenuItemClickListener();
                    lsList.setOnItemClickListener(menuListener);
                    lsList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                        @Override
                        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                            if(isOnline())
                            {
                                loadNewsAndLegislativeData();
                            }
                            else
                            {
                                displayNoNetworkDialog();
                                lsList.onRefreshComplete();
                            }
                        }
                    });


                }
                else
                {
                    //Log.d("fatal", "else");
                    if(isOnline())
                    {
                        progressDialog.show();
                        loadNewsAndLegislativeData();
                    }
                    else
                    {
                        displayNoNetworkDialog();
                    }
                }
            }
            catch(Exception e)
            {
                //Log.d("fatal", "catch");
                if(isOnline())
                {
                    progressDialog.show();
                    loadNewsAndLegislativeData();
                }
                else
                {
                    displayNoNetworkDialog();
                }
            }

    }

    private Date fixDate(Date date)
    {
        TimeZone tz = TimeZone.getDefault();
        Date fixed = new Date(date.getTime() - tz.getRawOffset());

        if(tz.inDaylightTime(fixed))
        {
            Date dst = new Date(fixed.getTime() - tz.getDSTSavings());

            if(tz.inDaylightTime(dst))
            {
                fixed = dst;
            }
        }

        return fixed;
    }

    public void displayNoNetworkDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder
                .setMessage("The internet connection appears to be offline. Some content may not be available until a connection is made.")
                .setCancelable(false)
                .setNegativeButton("Okay",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void loadNewsAndLegislativeData()
    {
        posts = new ArrayList<Post>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Weeklies");
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> result, com.parse.ParseException e)
            {
                for(ParseObject parse : result)
                {
                    Post tmp = new Post();
                    tmp.description = parse.getString("Post_Description");
                    tmp.caption = parse.getString("Caption");
                    tmp.headline = parse.getString("Post_Headline");
                    tmp.objectId = parse.getObjectId();
                    tmp.dateline = parse.getString("Dateline");
                    tmp.isAlert = parse.getBoolean("isAlert");

                    ParseFile image = parse.getParseFile("Photo");
                    ParseFile document = parse.getParseFile("DOC");

                    if(image != null)
                    {
                        tmp.photo_url = image.getUrl();
                    }

                    if(document != null)
                    {
                        tmp.doc_url = document.getUrl();
                    }

                    posts.add(tmp);
                }
                try
                {
                    PersistenceManager.writeObject(getApplicationContext(), Constants.NEWS_FILE_NAME, posts);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }


                //Log.d("fatal", "create");
                lsList = (RefreshListView) findViewById(R.id.lsList);
                LSAdapter adapter = new LSAdapter(context, posts, noNews);
                lsList.setAdapter(adapter);
                MenuItemClickListener menuListener = new MenuItemClickListener();
                lsList.setOnItemClickListener(menuListener);
                lsList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                    @Override
                    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                        if(isOnline())
                        {
                            loadNewsAndLegislativeData();
                        }
                        else
                        {
                            displayNoNetworkDialog();
                            lsList.onRefreshComplete();
                        }
                    }
                });
                lsList.onRefreshComplete();
                if(progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }

            }
        });
    }

    private void selectItem(int position)
    {
        if(posts.get(position) != null && posts.get(position).doc_url != null)
        {
            //Log.d("fatal", posts.get(position).headline);
            if(posts.get(position).doc_url.toUpperCase().endsWith(".PDF"))
            {
                try
                {
                    File f = new File(Environment.getExternalStorageDirectory() + "/" + posts.get(position).objectId);
                    if(f == null || !f.exists())
                    {
                        if(isOnline())
                        {
                            lp = new loadDocument();
                            lp.execute(posts.get(position), TYPE_PDF);
                        }
                        else
                        {
                            displayNoNetworkDialog();
                            return;
                        }
                    }
                    else
                    {
                        Uri path = Uri.fromFile(f);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(path, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        try
                        {
                            startActivity(intent);
                        }
                        catch(ActivityNotFoundException e)
                        {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle("No PDF Viewer Found");
                            alertDialogBuilder
                                    .setMessage("You do not have an application that allows you to view PDF files. To view this file, please download Adobe Reader from the Android Market.")
                                    .setCancelable(false)
                                    .setPositiveButton("Download",new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id)
                                        {
                                            try
                                            {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
                                            }
                                            catch(ActivityNotFoundException ex)
                                            {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.adobe.reader")));
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id)
                                        {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch(Exception e)
                {
                    if(isOnline())
                    {
                        lp = new loadDocument();
                        lp.execute(posts.get(position), TYPE_PDF);
                    }
                    else
                    {
                        displayNoNetworkDialog();
                        return;
                    }
                }
            }
            else
            {
                try
                {
                    File f = new File(Environment.getExternalStorageDirectory() + "/" + posts.get(position).objectId);
                    if(f == null || !f.exists())
                    {
                        if(isOnline())
                        {
                            lp = new loadDocument();
                            lp.execute(posts.get(position), TYPE_DOC);
                        }
                        else
                        {
                            displayNoNetworkDialog();
                            return;
                        }
                    }
                    else
                    {
                        Uri path = Uri.fromFile(f);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(path, "application/msword");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        try
                        {
                            startActivity(intent);
                        }
                        catch(ActivityNotFoundException e)
                        {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle("No Microsoft Word Viewer Found");
                            alertDialogBuilder
                                    .setMessage("You do not have an application that allows you to view .doc files. To view this file, please download OfficeSuite 7 free from the Android Market.")
                                    .setCancelable(false)
                                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mobisystems.office")));
                                            } catch (ActivityNotFoundException ex) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.mobisystems.office")));
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch(Exception e)
                {
                    if(isOnline())
                    {
                        lp = new loadDocument();
                        lp.execute(posts.get(position), TYPE_DOC);
                    }
                    else
                    {
                        displayNoNetworkDialog();
                        return;
                    }
                }
            }
        }
    }

    private class MenuItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            try
            {
                selectItem((position-1));
            }
            catch(Exception e){}
        }
    }

    private class loadDocument extends AsyncTask<Object, Integer, Void>
    {
        File file;
        Post post;
        Integer file_type_flag;

        protected void onPreExecute()
        {
            super.onPreExecute();
            pdfProgress.setCanceledOnTouchOutside(false);
            pdfProgress.setCancelable(true);
            pdfProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface)
                {
                    lp.cancel(true);
                    publishProgress(0);
                }
            });
            pdfProgress.show();
        }

        protected void onProgressUpdate(Integer... progress)
        {
            super.onProgressUpdate(progress);
            pdfProgress.setProgress(progress[0]);
        }

        protected Void doInBackground(Object... arg0)
        {
            try
            {
                post = (Post) arg0[0];
                file_type_flag = (Integer) arg0[1];
                URL url = new URL(post.doc_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();
                InputStream is = connection.getInputStream();

                file = new File(Environment.getExternalStorageDirectory() + "/" + post.objectId);
                FileOutputStream fos = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;
                int bytesRead = 0;

                while(((bytesRead = is.read(data, 0, data.length)) >= 0) && !isCancelled())
                {
                    total += bytesRead;
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(data, 0, bytesRead);
                    fos.flush();
                }

                fos.close();
                is.close();

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void v)
        {
            pdfProgress.dismiss();
            if(file != null && !isCancelled())
            {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if(file_type_flag.intValue() == TYPE_PDF)
                {
                    intent.setDataAndType(path, "application/pdf");
                }
                else
                {
                    intent.setDataAndType(path, "application/msword");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try
                {
                    startActivity(intent);
                }
                catch(ActivityNotFoundException e)
                {
                    if(file_type_flag.intValue() == TYPE_PDF)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle("No PDF Viewer Found");
                        alertDialogBuilder
                                .setMessage("You do not have an application that allows you to view PDF files. To view this file, please download Adobe Reader from the Android Market.")
                                .setCancelable(false)
                                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
                                        } catch (ActivityNotFoundException ex) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.adobe.reader")));
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    else
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle("No Microsoft Word Viewer Found");
                        alertDialogBuilder
                                .setMessage("You do not have an application that allows you to view .doc files. To view this file, please download OfficeSuite 7 free from the Android Market.")
                                .setCancelable(false)
                                .setPositiveButton("Download",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id)
                                    {
                                        try
                                        {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mobisystems.office")));
                                        }
                                        catch(ActivityNotFoundException ex)
                                        {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.mobisystems.office")));
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id)
                                    {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
