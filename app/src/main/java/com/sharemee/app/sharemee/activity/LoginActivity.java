package com.sharemee.app.sharemee.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.PrefUtils;


public class LoginActivity extends BaseActivity {

    private String usermail;
    private String password;

    AutoCompleteTextView logInUsename;
    EditText logInPassword;
    public static String PREFS_LOGIN_USERNAME_KEY = "EMAIL";
    public static String PREFS_LOGIN_PASSWORD_KEY = "PASSWORD";


    private TextView signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_login, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        // now you can do all your other stuffs

        logInUsename = (AutoCompleteTextView) findViewById(R.id.login_email);
        logInPassword = (EditText) findViewById(R.id.login_password);



        signin = (TextView) findViewById(R.id.login_new_member);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(intent);
            }
        });

        // Saving user credentials on successful login case
        PrefUtils.saveToPrefs(LoginActivity.this, PREFS_LOGIN_USERNAME_KEY, usermail);
        PrefUtils.saveToPrefs(LoginActivity.this, PREFS_LOGIN_PASSWORD_KEY, password);

// To retrieve values back
        String loggedInUserName = PrefUtils.getFromPrefs(LoginActivity.this, PREFS_LOGIN_USERNAME_KEY, "0");
        String loggedInUserPassword = PrefUtils.getFromPrefs(LoginActivity.this, PREFS_LOGIN_PASSWORD_KEY, "0");
    }

}
