package com.cellaflora.iact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sdickson on 7/12/13.
 */
public class Splash extends Activity
{
    public static final int SPLASH_DELAY = 3;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        TimerTask task = new TimerTask()
        {
            public void run()
            {
                finish();
                Intent intent = new Intent().setClass(Splash.this, MainActivity.class);
                startActivity(intent);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_DELAY);
    }
}
