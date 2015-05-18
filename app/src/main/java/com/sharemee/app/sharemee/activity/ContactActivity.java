package com.sharemee.app.sharemee.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ContactActivity extends BaseActivity {

    private EditText email_user;
    private EditText name_user;
    private EditText mail_subject;
    private EditText mail_content;
    public static String PREFS_USER_MAIL = "user_mail" ;
    public static String PREFS_USER_NAME = "user_name" ;

    private String name;
    private String mail;
    private String subject;
    private String message;

    private String idObject;

    private TextView send_message;

    // Progress Dialog
    private ProgressDialog pDialog;

    private String baseURL = new ConnectionConfig().getBaseURL();

    private String url_mail = baseURL+"webservice/model/send_email.php";
    //private static String url_mail = "http://10.0.2.2/sharemee/webservice/model/send_email.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID_OBJECT = "idObject";

    public int success;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

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

        // getting product details from intent
        Intent i = getIntent();

        idObject = i.getStringExtra(TAG_ID_OBJECT);

        String savedUserName = PrefUtils.getFromPrefs(ContactActivity.this, PREFS_USER_NAME, "0");
        name_user = (EditText) findViewById(R.id.your_name);
        name_user.setText(savedUserName);

        String savedUserMail = PrefUtils.getFromPrefs(ContactActivity.this, PREFS_USER_MAIL, "0");
        email_user = (EditText) findViewById(R.id.email_user);
        email_user.setText(savedUserMail);

        send_message = (TextView) findViewById(R.id.send_message);

        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name_user = (EditText) findViewById(R.id.your_name);
                email_user = (EditText) findViewById(R.id.email_user);
                mail_subject = (EditText) findViewById(R.id.object_message);
                mail_content = (EditText) findViewById(R.id.mail_message);

                name = name_user.getText().toString();
                mail = email_user.getText().toString();
                subject = mail_subject.getText().toString();
                message = mail_content.getText().toString();

                new SendMessage().execute();
            }
        });



    }

    class SendMessage extends AsyncTask<String, String, JSONObject>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ContactActivity.this);
            pDialog.setMessage("Envoi du message en cours...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("mail", mail));
            params.add(new BasicNameValuePair("subject", subject));
            params.add(new BasicNameValuePair("message", message));
            params.add(new BasicNameValuePair("idObject", idObject));

            //check params
            Log.d("params :", params.toString());

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_mail, "POST", params);
            Log.d("json", json.toString());
            try {
                success = json.getInt(TAG_SUCCESS);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        protected void onPostExecute(JSONObject json) {

            // dismiss the dialog once product updated
            pDialog.dismiss();

            try {
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "Mail Envoyé";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    Intent intent = new Intent(context, SearchableActivity.class);
                    startActivity(intent);
                }
                else{
                    Context context = getApplicationContext();
                    CharSequence text = "Nous ne sommes pas parvenu à envoyer votre mail";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

}


