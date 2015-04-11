package com.sharemee.app.sharemee.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyProfileActivity extends BaseActivity {

    String idUser;

    TextView userName;
    TextView userSurname;
    TextView userMail;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to get all objects list
    private static String url_user_details = "http://sharemee.com/webservice/model/get_user_details.php";
    //private static String url_object_detail = "http://10.0.2.2/sharemee/webservice/model/get_object_details.php";

    private static String url_user_image = "http://sharemee.com/webservice/images/";
    //private static String url_object_image = "http://10.0.2.2/sharemee/webservice/images/no-image.jpg";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "user";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_NAME_USER = "nameUser";
    private static final String TAG_SURNAME_USER = "surnameUser";
    private static final String TAG_MAIL_USER = "mailUser";
    private static final String TAG_IMAGE_PROFILE_PICTURE = "profilePictureUser";

    private TextView modify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in BaseActivity
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_my_profile, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        //idUser = i.getStringExtra(TAG_ID_USER);
        idUser = "1";

        userName = (TextView) findViewById(R.id.user_name);
        userSurname = (TextView) findViewById(R.id.user_surname);
        userMail = (TextView) findViewById(R.id.user_mail);

        // Loading objects in Background Thread
        new LoadObjectDetails().execute();

        modify = (TextView) findViewById(R.id.modify_user_profile);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ModifyProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    class LoadObjectDetails extends AsyncTask<String, String, JSONObject>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MyProfileActivity.this);
            //pDialog.setMessage("Recherche de l'objet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

                    int success;

                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("idUser", idUser));

                        //check params
                        Log.d("params :", params.toString());

                        // getting product details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_user_details, "GET", params);

                        // check your log for json response
                        Log.d("Single User Profile", json.toString());

                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {

                            JSONArray productObj = json
                                    .getJSONArray(TAG_USER); // JSON Array

                            // get first product object from JSON Array
                            JSONObject object = productObj.getJSONObject(0);
                            //check object variable
                            //Log.d("First product object from Json Array", object.toString());

                            return object;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject user) {
            //get the parameter
            final JSONObject user1 = user;

            //Here is where the UI is called, thus the following code will appear in the User Interface Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context context = getBaseContext();


            try {
                Log.d("user name :", user1.getString(TAG_NAME_USER));
                //The cardviews are set
                userName.setText(user1.getString(TAG_NAME_USER));
                userSurname.setText(user1.getString(TAG_SURNAME_USER));
                userMail.setText(user1.getString(TAG_MAIL_USER));

                //Construct full image url to get the image
                String full_image_url_1 = url_user_image + user1.getString(TAG_IMAGE_PROFILE_PICTURE);
                Log.d("image path 1", full_image_url_1);

            }catch (JSONException e){
                e.printStackTrace();
            }

                }
            });
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }

    }
}