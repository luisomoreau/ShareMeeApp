package com.sharemee.app.sharemee.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    private String usermail;
    private String password;

    private String userID;

    AutoCompleteTextView logInUsename;
    EditText logInPassword;

    public static String PREFS_USER_ID = "user_ID" ;
    public static String PREFS_USER_MAIL = "user_mail" ;

    private TextView signin;
    private Button login;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_USER_MAIL = "mailUser";
    public int success;

    private static String url_connection = "http://sharemee.com/webservice/model/check_connection.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // don’t set any content view here, since its already set in BaseActivity
        //FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //View activityView = layoutInflater.inflate(R.layout.activity_login, null, false);
        // add the custom layout of this activity to frame layout.
        //frameLayout.addView(activityView);
        // now you can do all your other stuffs

        String savedUserId = PrefUtils.getFromPrefs(LoginActivity.this, PREFS_USER_ID, "0");

        Log.d("savedUserId",savedUserId);

        logInUsename = (AutoCompleteTextView) findViewById(R.id.login_email);
        logInPassword = (EditText) findViewById(R.id.login_password);

        /*
        String loggedInUserName = PrefUtils.getFromPrefs(LoginActivity.this, PREFS_LOGIN_USERNAME_KEY, PREFS_LOGIN_USERNAME_KEY);
        String loggedInUserPassword = PrefUtils.getFromPrefs(LoginActivity.this, PREFS_LOGIN_PASSWORD_KEY, PREFS_LOGIN_PASSWORD_KEY);

        Log.d("savedusername :", loggedInUserName);
        Log.d("savedpassword :", loggedInUserPassword);*/

        // To retrieve values back
        /*
        logInUsename.setText(loggedInUserName);
        logInPassword.setText(loggedInUserPassword);*/


        login = (Button) findViewById(R.id.email_sign_in_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usermail = logInUsename.getText().toString();
                password = logInPassword.getText().toString();

                new Connection().execute();


            }
        });



        // Saving user credentials on successful login case


        signin = (TextView) findViewById(R.id.login_new_member);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(intent);
            }
        });

    }

    class Connection extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            /*
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Connexion en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            //Looper.prepare();


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mailUser", usermail));
            params.add(new BasicNameValuePair("passwordUser", password));

            //check params
            Log.d("params :", params.toString());

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_connection, "POST", params);
            Log.d("json", json.toString());
            try {
                success = json.getInt(TAG_SUCCESS);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        protected void onPostExecute(JSONObject json) {
            try {
                success = json.getInt(TAG_SUCCESS);


                if (success == 1){
                    userID = json.getString(TAG_ID_USER);
                    usermail = json.getString(TAG_USER_MAIL);
                    Log.d("UserID :",userID);
                    PrefUtils.saveToPrefs(LoginActivity.this, PREFS_USER_ID, userID);
                    PrefUtils.saveToPrefs(LoginActivity.this, PREFS_USER_MAIL, usermail);

                /*
                PrefUtils.saveToPrefs(LoginActivity.this, PREFS_LOGIN_USERNAME_KEY, usermail);
                PrefUtils.saveToPrefs(LoginActivity.this, PREFS_LOGIN_PASSWORD_KEY, password);*/
                Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                /*
                intent.putExtra(TAG_ID_USER, userID);
                Log.d("Id user recupere : ", userID );*/
                    startActivity(intent);}
                else {
                    if(success == 2){
                        Context context = getApplicationContext();
                        CharSequence text = "Mot de passe incorrect";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();}
                    else {
                        if (success == 3){
                            Context context = getApplicationContext();
                            CharSequence text = "Utilisateur non trouvé";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                        else {
                            if (success == 4) {
                                Context context = getApplicationContext();
                                CharSequence text = "Champs manquants";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                            else{
                                Context context = getApplicationContext();
                                CharSequence text = "Erreur";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}
