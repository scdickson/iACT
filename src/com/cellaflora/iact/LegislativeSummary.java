package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdickson on 7/21/13.
 */
public class LegislativeSummary extends Activity
{
    private ArrayList<Post> posts = null;

    private PullToRefreshListView lsList;
    private ProgressDialog progressDialog, pdfProgress;
    private Context context;
    private loadPdf lp;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislative_summary_activity);
        context = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        pdfProgress = new ProgressDialog(this);
        pdfProgress.setTitle("");
        pdfProgress.setMessage("Loading PDF...");
        pdfProgress.setIndeterminate(false);
        pdfProgress.setMax(100);
        pdfProgress.setProgressNumberFormat(null);
        pdfProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageButton ib = (ImageButton) findViewById(R.id.toggle_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent().setClass(getApplicationContext(), MainActivity.class);
                if(intent != null)
                {
                    startActivity(intent);
                }
            }
        });
    }

    public void onResume()
    {
        super.onResume();
        try
        {
            File f = new File(getFilesDir(), Constants.NEWS_FILE_NAME);
            if((f.lastModified() + (Constants.NEWS_UPDATE_INTERVAL * 60 * 1000)) >= System.currentTimeMillis())
            {
                posts = (ArrayList<Post>) PersistenceManager.readObject(this, Constants.NEWS_FILE_NAME);
                lsList = (PullToRefreshListView) findViewById(R.id.lsList);
                LSAdapter adapter = new LSAdapter(this, posts);
                lsList.setAdapter(adapter);
                MenuItemClickListener menuListener = new MenuItemClickListener();
                lsList.setOnItemClickListener(menuListener);
                lsList.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    @Override
                    public void onRefresh()
                    {
                        loadNewsAndLegislativeData();
                    }
                });
            }
            else
            {
                progressDialog.show();
                loadNewsAndLegislativeData();
            }
        }
        catch(Exception e)
        {
            progressDialog.show();
            loadNewsAndLegislativeData();
        }
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
                    tmp.update_time = parse.getString("updatedAt");

                    ParseFile image = null;//(ParseFile) parse.get("Photo");
                    ParseFile document = (ParseFile) parse.get("DOC");

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

                lsList = (PullToRefreshListView) findViewById(R.id.lsList);
                LSAdapter adapter = new LSAdapter(context, posts);
                lsList.setAdapter(adapter);
                MenuItemClickListener menuListener = new MenuItemClickListener();
                lsList.setOnItemClickListener(menuListener);
                lsList.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    @Override
                    public void onRefresh()
                    {
                        loadNewsAndLegislativeData();
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
            if(posts.get(position).doc_url.endsWith(".pdf"))
            {
                try
                {
                    File f = new File(Environment.getExternalStorageDirectory() + "/" + posts.get(position).objectId);
                    if(f == null || !f.exists())
                    {
                        lp = new loadPdf();
                        lp.execute(posts.get(position));
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
                    lp = new loadPdf();
                    lp.execute(posts.get(position));
                }
            }
            else
            {
                //new downloadResource().execute(position, TYPE_DOC);
            }
        }
    }

    private class MenuItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    private class loadPdf extends AsyncTask<Object, Integer, Void>
    {
        File file;
        Post post;

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
    }
}
