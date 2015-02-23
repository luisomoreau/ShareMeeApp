package com.sharemee.app.sharemee;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SplashScreen extends Activity {

    /** Durée d'affichage du SplashScreen */
    protected int _splashTime = 2000;

    private Thread splashTread;

    /** Chargement de l'Activity */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final SplashScreen sPlashScreen = this;

        /** Thread pour l'affichage du SplashScreen */
        splashTread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    synchronized(this)
                    {
                        wait(_splashTime);
                    }
                } catch(InterruptedException e) {}
                finally
                {
                    finish();
                    Intent i = new Intent();
                    i.setClass(sPlashScreen, SampleActivity.class);
                    startActivity(i);
                }
            }
        };

        splashTread.start();
    }

}
