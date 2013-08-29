package com.cellaflora.iact;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

/**
 * Created by sdickson on 7/17/13.
 */

public class WebContentView extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_content_activity);
        Bundle arguments = getIntent().getExtras();

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_bar));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        if(arguments.getBoolean("BACK_ENABLED") == true)
        {
            int id = getResources().getIdentifier("com.cellaflora.iact:drawable/back", null, null);
            ImageButton ib = (ImageButton) findViewById(R.id.toggle_button);
            ib.setImageResource(id);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent().setClass(getApplicationContext(), LegislativeSummary.class);
                    if(intent != null)
                    {
                        startActivity(intent);
                    }
                }
            });
        }
        else
        {
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
    }

    protected void onResume()
    {
        super.onResume();
        Bundle arguments = getIntent().getExtras();
        WebView webview = (WebView) findViewById(R.id.web_view);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setPluginsEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setSupportZoom(true);

        if(arguments != null)
        {
            webview.loadUrl(arguments.getString("URL"));
        }
    }
}
