package com.sharemee.app.sharemee.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.PrefUtils;


public class ContactActivity extends BaseActivity {

    private EditText email_user;
    private EditText name_user;
    public static String PREFS_USER_MAIL = "user_mail" ;
    public static String PREFS_USER_NAME = "user_name" ;

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

        String savedUserName = PrefUtils.getFromPrefs(ContactActivity.this, PREFS_USER_NAME, "0");
        name_user = (EditText) findViewById(R.id.your_name);
        name_user.setText(savedUserName);

        String savedUserMail = PrefUtils.getFromPrefs(ContactActivity.this, PREFS_USER_MAIL, "0");
        email_user = (EditText) findViewById(R.id.email_user);
        email_user.setText(savedUserMail);

    }

}
