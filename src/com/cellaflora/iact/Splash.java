package com.cellaflora.iact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sdickson on 7/12/13.
 */
public class Splash extends Activity
{
    TimerTask task;
    Timer timer;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            public void run()
            {
                Intent i = new Intent(Splash.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, (Constants.SPLASH_DELAY * 1000));
    }
}
