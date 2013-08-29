package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
    public static final String POSTS_KEY = "POSTS";
    public static final int POST_UPDATE_INTERVAL = 60; //In minutes!

    private ListView lsList;
    private ProgressDialog progressDialog;

    public static final boolean TRY_EXTERNAL = false;
    public static final int TYPE_PDF = 0;
    public static final int TYPE_DOC = 1;
    public static final String GOOGLE_DOCS_URL = "http://docs.google.com/viewer?embedded=true&url=";

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legislative_summary_activity);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");

        try
        {
            File f = new File(getFilesDir(), POSTS_KEY);
            if((f.lastModified() + (POST_UPDATE_INTERVAL * 60 * 1000)) >= System.currentTimeMillis())
            {
                posts = (ArrayList<Post>) PersistenceManager.readObject(this, POSTS_KEY);
            }
            else
            {
                int i = 1/0; //Not elegant....but generates an exception so execution can jump to catch block below.
            }
        }
        catch(Exception e)
        {
            posts = new ArrayList<Post>();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Weeklies");
            query.addDescendingOrder("updatedAt");

            progressDialog.show();

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
                        tmp.objectId = parse.getString("objectId");
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
                        PersistenceManager.writeObject(getApplicationContext(), POSTS_KEY, posts);
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
            });
        }

        lsList = (ListView) findViewById(R.id.lsList);
        LSAdapter adapter = new LSAdapter(this, posts);
        lsList.setAdapter(adapter);
        MenuItemClickListener menuListener = new MenuItemClickListener();
        lsList.setOnItemClickListener(menuListener);


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

    private void selectItem(int position)
    {
        if(posts.get(position) != null && posts.get(position).doc_url != null)
        {
            if(posts.get(position).doc_url.endsWith(".pdf"))
            {
                new downloadResource().execute(position, TYPE_PDF);
            }
            else
            {
                new downloadResource().execute(position, TYPE_DOC);
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

    private class downloadResource extends AsyncTask<Integer, Integer, Void>
    {
        File f = null;
        int position;
        int dataType;

        protected void onPreExecute()
        {
            progressDialog.show();
        }

        protected Void doInBackground(Integer... arg0)
        {
            position = arg0[0];
            dataType = arg0[1];

            try
            {
                File directory = Environment.getExternalStorageDirectory();

                if(dataType == TYPE_PDF)
                {
                    f = new File(directory + "/iact_tmp.pdf");
                }
                else if(dataType == TYPE_DOC)
                {
                    f = new File(directory + "/iact_tmp.doc");
                }

                URL u = new URL(posts.get(position).doc_url);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                //c.setDoOutput(true);
                c.connect();
                FileOutputStream out = new FileOutputStream(f);


                InputStream in = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ( (len1 = in.read(buffer)) > 0 ) {
                    out.write(buffer,0, len1);
                    //out.flush();
                }
                out.close();
            }
            catch(Exception e)
            {
                //e.printStackTrace();
                Intent load_web = new Intent().setClass(getApplicationContext(), WebContentView.class);
                load_web.putExtra("URL", GOOGLE_DOCS_URL + posts.get(position).doc_url);
                load_web.putExtra("BACK_ENABLED", true);
                if(load_web != null)
                {
                    startActivity(load_web);
                }
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            progressDialog.dismiss();
            Uri path = Uri.fromFile(f);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(TRY_EXTERNAL)
            {
                if(dataType == TYPE_PDF)
                {
                    intent.setDataAndType(path, "application/pdf");
                }
                else if(dataType == TYPE_DOC)
                {
                    intent.setDataAndType(path, "application/msword");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            else
            {
                intent = null; //Intentionally generate a null pointer exception....jumps to catch block.
            }
            try
            {
                startActivity(intent);
            }
            catch(Exception e)
            {
                //e.printStackTrace();
                Intent load_web = new Intent().setClass(getApplicationContext(), WebContentView.class);
                load_web.putExtra("URL", GOOGLE_DOCS_URL + posts.get(position).doc_url);
                load_web.putExtra("BACK_ENABLED", true);
                if(load_web != null)
                {
                    startActivity(load_web);
                }
            }
        }
    }


}
