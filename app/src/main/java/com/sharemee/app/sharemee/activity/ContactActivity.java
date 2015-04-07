package com.sharemee.app.sharemee.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.sharemee.app.sharemee.R;


public class ContactActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_contact, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs
    }

}
