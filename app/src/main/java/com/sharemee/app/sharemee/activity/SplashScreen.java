package com.sharemee.app.sharemee.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.PrefUtils;


public class SplashScreen extends Activity {

    //Store user_ID variable declarations
    String idUser;
    public static String PREFS_USER_ID = "user_ID" ;


    /** Durée d'affichage du SplashScreen */
    protected int _splashTime = 3000;

    private Thread splashTread;

    /** Chargement de l'Activity */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //parameter
        String savedUserId = PrefUtils.getFromPrefs(SplashScreen.this, PREFS_USER_ID, "0");
        Log.d("savedUserId", savedUserId);
        idUser = savedUserId;

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
                    if (idUser.equals("0")||idUser.equals("null")){
                        Intent i = new Intent();
                        i.setClass(sPlashScreen, LoginActivity.class);
                        startActivity(i);
                    }
                    else {
                        Intent i = new Intent();
                        i.setClass(sPlashScreen, MyProfileActivity.class);
                        startActivity(i);
                    }
                }
            }
        };

        splashTread.start();
    }

}
