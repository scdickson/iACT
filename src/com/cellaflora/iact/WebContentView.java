package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * Created by sdickson on 7/17/13.
 */

public class WebContentView extends Activity
{
    private static int REFRESH_IMAGE, STOP_IMAGE;

    private Context context;
    private WebView webview;
    private String baseUrl;
    private RelativeLayout webController;
    private ImageView controller_back, controller_stopRefresh;
    private Animation slideIn, slideOut;
    private boolean isLoading = true;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.web_content_activity);
        context = this;
        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        REFRESH_IMAGE = context.getResources().getIdentifier("com.cellaflora.iact:drawable/web_refresh", null, null);
        STOP_IMAGE = context.getResources().getIdentifier("com.cellaflora.iact:drawable/web_stop", null, null);
        Bundle arguments = getIntent().getExtras();
        baseUrl = arguments.getString("URL");
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.iact_nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        webController = (RelativeLayout) findViewById(R.id.web_controller);
        controller_back = (ImageView) findViewById(R.id.web_controller_back);
        controller_stopRefresh = (ImageView) findViewById(R.id.web_controller_refresh_stop);
        webview = (WebView) findViewById(R.id.web_view);
        webview.setWebViewClient(new WebViewClient()
        {
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                isLoading = true;
                controller_stopRefresh.setImageResource(STOP_IMAGE);

                //Log.d("fatal", url + ", " + baseUrl);
                if(!url.equals(baseUrl) && !MainActivity.webFirstLoad)
                {
                    if(webController.getVisibility() == View.GONE)
                    {
                        webController.setVisibility(View.VISIBLE);
                        //webController.startAnimation(slideIn);
                    }
                }
            }

            public void onPageFinished(WebView view, String url)
            {
                isLoading = false;
                controller_stopRefresh.setImageResource(REFRESH_IMAGE);

                if(MainActivity.webFirstLoad)
                {
                    MainActivity.webFirstLoad = false;
                    baseUrl = url;
                }

                //Log.d("fatal", url + ", " + baseUrl);
                if(url.equals(baseUrl))
                {
                    if(webController.getVisibility() == View.VISIBLE)
                    {
                        webController.setVisibility(View.GONE);
                        //webController.startAnimation(slideOut);
                    }
                }
            }
        });
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setPluginsEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setSupportZoom(true);
        webview.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress)
            {
                setProgress(progress * 100);
            }
        });

        controller_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(webview.canGoBack())
                {
                    webview.goBack();
                }
            }
        });

        controller_stopRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(isLoading)
                {
                    webview.stopLoading();
                    controller_stopRefresh.setImageResource(REFRESH_IMAGE);
                }
                else
                {
                    webview.reload();
                }
            }
        });

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

        if(savedInstanceState != null)
        {
            webview.restoreState(savedInstanceState);
        }
        else
        {
            if(arguments != null)
            {
                if(isOnline())
                {
                    webview.loadUrl(baseUrl);
                }
                else
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
                                    onBackPressed();
                                    //Intent intent = new Intent(context, MainActivity.class);
                                    //startActivity(intent);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
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

    public void onPause()
    {
        super.onPause();
        MainActivity.webFirstLoad = true;
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        webview.saveState(outState);
    }

    protected void onResume()
    {
        super.onResume();
    }
}
