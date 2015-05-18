package com.sharemee.app.sharemee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.sharemee.app.sharemee.R;
/**
 *
 *This activity is where the user arrive when he opens the app for the first time
 **/

public class HomeActivity extends BaseActivity {

    Button buttonSearch, buttonMyProfile, buttonMyObject, buttonAbout;

    //creating activity and setting listener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_home, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        buttonAbout = (Button) findViewById(R.id.buttonAbout);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        buttonMyObject = (Button) findViewById(R.id.buttonMyObject);
        buttonMyProfile = (Button) findViewById(R.id.buttonMyProfile);
        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });
        buttonMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);
            }
        });
        buttonMyObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyObjectsActivity.class);
                startActivity(intent);
            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchableActivity.class);
                startActivity(intent);
            }
        });
    }

}
