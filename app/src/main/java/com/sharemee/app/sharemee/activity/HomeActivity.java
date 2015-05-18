package com.sharemee.app.sharemee.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.sharemee.app.sharemee.R;

public class HomeActivity extends BaseActivity {

    Button buttonSearch,buttonMyProfile, buttonMyObject, buttonAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_home, null,false);
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
                startActivity(intent);}
        });
        buttonMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);}
        });
        buttonMyObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyObjectsActivity.class);
                startActivity(intent);}
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchableActivity.class);
                startActivity(intent);}
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
