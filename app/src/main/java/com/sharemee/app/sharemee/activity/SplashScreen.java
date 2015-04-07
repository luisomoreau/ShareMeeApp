package com.sharemee.app.sharemee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.sharemee.app.sharemee.R;


public class SplashScreen extends Activity {

    /** Durée d'affichage du SplashScreen */
    protected int _splashTime = 3000;

    private Thread splashTread;

    /** Chargement de l'Activity */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.REVERSE);
        anim.setDuration(1400);

// Start animating the image
        final ImageView splashLogo = (ImageView) findViewById(R.id.logoSplashScreen);
        splashLogo.startAnimation(anim);

// Later.. stop the animation
        splashLogo.setAnimation(null);


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
                    i.setClass(sPlashScreen, LoginActivity.class);
                    startActivity(i);
                }
            }
        };

        splashTread.start();
    }

}
