package com.sharemee.app.sharemee.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharemee.app.sharemee.R;
import com.sharemee.app.sharemee.util.ConnectionConfig;
import com.sharemee.app.sharemee.util.DownloadImageTask;
import com.sharemee.app.sharemee.util.JSONParser;
import com.sharemee.app.sharemee.util.PrefUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyProfileActivity extends BaseActivity {

    String idUser;
    public static String PREFS_USER_ID = "user_ID" ;
    public static String PREFS_USER_NAME = "user_name" ;

    TextView userName;
    TextView userSurname;
    TextView userMail;
    TextView deleteUser;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private String baseURL = new ConnectionConfig().getBaseURL();

    // url to get user details
    private String url_user_details = baseURL+"webservice/model/get_user_details.php";

    // url to delete user and his objects
    //private String url_user_delete = baseURL+"webservice/model/delete_user.php";
    private String url_user_delete = "http://192.168.1.34/ShareMeeWeb/webservice/model/delete_user.php";
    //URL to get image
    private String url_user_image = baseURL+"webservice/images/";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "user";
    private static final String TAG_ID_USER = "idUser";
    private static final String TAG_NAME_USER = "nameUser";
    private static final String TAG_SURNAME_USER = "surnameUser";
    private static final String TAG_MAIL_USER = "mailUser";
    private static final String TAG_IMAGE_PROFILE_PICTURE = "profilPictureUser";

    private TextView modify;
    private TextView logout;

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

        String savedUserId = PrefUtils.getFromPrefs(MyProfileActivity.this, PREFS_USER_ID, "0");
        Log.d("savedUserId",savedUserId);
        idUser = savedUserId;

        deleteUser=(TextView) findViewById(R.id.deleteUserProfile);
        userName = (TextView) findViewById(R.id.user_name);
        userSurname = (TextView) findViewById(R.id.user_surname);
        userMail = (TextView) findViewById(R.id.user_mail);

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteUser();
            }
        });

        // Loading objects in Background Thread
        new LoadUserDetails().execute();

        modify = (TextView) findViewById(R.id.modify_user_profile);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ModifyProfileActivity.class);
                startActivity(intent);
            }
        });

        logout = (TextView) findViewById(R.id.logout_user_profile);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID="0";
                PrefUtils.saveToPrefs(MyProfileActivity.this, PREFS_USER_ID, userID);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    class LoadUserDetails extends AsyncTask<String, String, JSONObject>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MyProfileActivity.this);
            pDialog.setMessage("Chargement du Profil");
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

                PrefUtils.saveToPrefs(MyProfileActivity.this, PREFS_USER_NAME, user1.getString(TAG_NAME_USER));

                //Construct full image url to get the image
                String full_image_url_1 = url_user_image + user1.getString(TAG_IMAGE_PROFILE_PICTURE);
                Log.d("image path 1", full_image_url_1);

                //The DownloadImageTask is called to get the image on the server
                if (!user1.getString(TAG_IMAGE_PROFILE_PICTURE).equals("null")) {
                    new DownloadImageTask((ImageView) findViewById(R.id.profile_picture))
                            .execute(full_image_url_1);
                }

            }catch (JSONException e){
                e.printStackTrace();
            }

                }
            });
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }

    }
    class DeleteUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MyProfileActivity.this);
            pDialog.setMessage("Suppression de l'utilisateur ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts
            Intent i1 = getIntent();
            String idUser = i1.getStringExtra(TAG_ID_USER);
            Log.d("IDUSER qu'on supp : ", idUser);


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("idUser", idUser));

            Log.d("params", params.toString());

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_user_delete,
                    "POST", params);

            Log.d("json", json.toString());

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product uupdated
            pDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }


    private void confirmDeleteUser() {

        final CharSequence[] options = { "OUI", "NON"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setTitle("Etes vous sur ? La suppression entrainera celle de tous vos objets");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("OUI"))
                {
                    new DeleteUser().execute();
                }
                else if (options[item].equals("NON")){

                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(550, 300);
    }
}
